package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.AuditResult
import com.example.engine.LegalAuditEngine
import com.example.engine.MatchedRedFlag
import com.example.engine.ScannerEngine
import com.example.engine.PdfExportEngine
import com.example.data.PreferenceManager
import com.example.data.ContractRepository
import com.example.data.db.AppDatabase
import com.example.data.db.DocumentEntity
import com.example.data.db.ClauseEntity
import com.example.data.db.MissingProtectionEntity
import com.example.data.db.NegotiationDraftEntity
import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.qonversion.android.sdk.dto.entitlements.QEntitlement
import com.qonversion.android.sdk.dto.QonversionError
import com.qonversion.android.sdk.listeners.QonversionEntitlementsCallback
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.dto.products.QProduct
import com.qonversion.android.sdk.listeners.QonversionProductsCallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class RealCostBreakdown(
    val baseAmount: Double,
    val maintenanceAmount: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val currencySymbol: String
)

sealed class AuditState {
    object Idle : AuditState()
    object Scanning : AuditState()
    object Analyzing : AuditState()
    data class Result(
        val documentId: String? = null,
        val documentTitle: String = "Audited Contract",
        val matchedRedFlags: List<MatchedRedFlag>,
        val missingMandatoryClauses: List<String>,
        val isHighRisk: Boolean,
        val extractedText: String,
        val overallRiskScore: Int,
        val complexityScoreOverall: Int,
        val realCostBreakdown: RealCostBreakdown?,
        val negotiationDrafts: Map<String, String>
    ) : AuditState()
    data class Error(val message: String) : AuditState()
}

class MainViewModel(
    application: Application,
    private val scannerEngine: ScannerEngine,
    private val legalAuditEngine: LegalAuditEngine,
    private val preferenceManager: PreferenceManager = PreferenceManager(application),
    private val contractRepository: ContractRepository = ContractRepository(AppDatabase.getDatabase(application).contractDao()),
    private val pdfExportEngine: PdfExportEngine = PdfExportEngine(application)
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<AuditState>(AuditState.Idle)
    val uiState: StateFlow<AuditState> = _uiState.asStateFlow()
    val auditState: StateFlow<AuditState> = _uiState.asStateFlow() // Backwards-compatible alias

    val isDarkMode: StateFlow<Boolean> = preferenceManager.isDarkMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isProUser = MutableStateFlow(false)

    val biometricLock: Flow<Boolean> = preferenceManager.biometricLock

    private val _proProductPrice = MutableStateFlow<String?>(null)
    val proProductPrice: StateFlow<String?> = _proProductPrice.asStateFlow()

    val savedDocuments: Flow<List<DocumentEntity>> = contractRepository.allDocuments

    private val _freeScansRemaining = MutableStateFlow(PreferenceManager.MAX_FREE_DAILY_SCANS)
    val freeScansRemaining: StateFlow<Int> = _freeScansRemaining.asStateFlow()

    private val _checkedClauses = MutableStateFlow<Set<String>>(emptySet())
    val checkedClauses: StateFlow<Set<String>> = _checkedClauses.asStateFlow()

    fun toggleClauseSelection(clauseId: String, isSelected: Boolean) {
        val currentSet = _checkedClauses.value.toMutableSet()
        if (isSelected) {
            currentSet.add(clauseId)
        } else {
            currentSet.remove(clauseId)
        }
        _checkedClauses.value = currentSet
    }

    init {
        checkProStatus()
        observeDataStore()
        loadProducts()
    }

    fun loadProducts() {
        Qonversion.shared.products(object : QonversionProductsCallback {
            override fun onSuccess(products: Map<String, QProduct>) {
                val qProduct = products[com.example.Constants.PRO_PRODUCT_ID]
                val formatted = qProduct?.prettyPrice?.let { "$it/month" }
                if (formatted != null) {
                    _proProductPrice.value = formatted
                }
            }

            override fun onError(error: QonversionError) {
                // Keep default or retry on paywall open
            }
        })
    }

    fun restorePurchases(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Qonversion.shared.restore(object : QonversionEntitlementsCallback {
            override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                val premium = entitlements[com.example.Constants.PRO_ENTITLEMENT_ID]
                isProUser.value = premium?.isActive == true
                onSuccess()
            }

            override fun onError(error: QonversionError) {
                onError(error.description)
            }
        })
    }

    private fun observeDataStore() {
        viewModelScope.launch {
            preferenceManager.freeScansRemaining.collect { remaining ->
                _freeScansRemaining.value = remaining
            }
        }
    }

    fun attemptScan(onSuccess: () -> Unit, onLimitReached: () -> Unit) {
        if (isProUser.value) {
            onSuccess()
            return
        }

        viewModelScope.launch {
            val allowed = preferenceManager.tryConsumeFreeScan()
            if (allowed) {
                onSuccess()
            } else {
                onLimitReached()
            }
        }
    }

    private fun checkProStatus() {
        Qonversion.shared.checkEntitlements(object : QonversionEntitlementsCallback {
            override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                val proAccess = entitlements[com.example.Constants.PRO_ENTITLEMENT_ID]
                isProUser.value = proAccess?.isActive == true
            }

            override fun onError(error: QonversionError) {
                // Log or handle error if needed
            }
        })
    }

    fun purchasePro(activity: Activity) {
        Qonversion.shared.products(object : QonversionProductsCallback {
            override fun onSuccess(products: Map<String, QProduct>) {
                val qProduct = products[com.example.Constants.PRO_PRODUCT_ID] 
                
                if (qProduct != null) {
                    Qonversion.shared.purchase(activity, qProduct, object : QonversionEntitlementsCallback {
                        override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                            val premium = entitlements[com.example.Constants.PRO_ENTITLEMENT_ID]
                            isProUser.value = premium?.isActive == true
                        }
                        override fun onError(error: QonversionError) {
                            // Let the UI handle Toast for error
                        }
                    })
                }
            }

            override fun onError(error: QonversionError) {
                // Error handling in UI
            }
        })
    }

    fun toggleTheme() {
        viewModelScope.launch {
            preferenceManager.setDarkMode(!isDarkMode.value)
        }
    }

    fun getScannerClient() = scannerEngine.getScannerClient()

    fun processScannedDocuments(uris: List<Uri>) {
        if (uris.isEmpty()) {
            _uiState.value = AuditState.Error("No documents scanned.")
            return
        }
        viewModelScope.launch {
            try {
                _uiState.value = AuditState.Scanning
                val extractedText = scannerEngine.extractTextFromUris(uris)
                if (extractedText.isBlank()) {
                    _uiState.value = AuditState.Error("Could not extract any text from the scanned documents.")
                    return@launch
                }

                val userCountry = preferenceManager.defaultCountry.first()
                val userContractType = preferenceManager.defaultContractType.first()

                _uiState.value = AuditState.Analyzing
                val result = legalAuditEngine.analyzeContract(
                    rawText = extractedText,
                    country = userCountry,
                    contractType = userContractType
                )
                val calculatedScore = result.overallRiskScore

                val drafts = result.matchedRedFlags.associate { flag ->
                    flag.displayName to com.example.engine.NegotiationTemplateEngine.generateDraft(
                        category = flag.displayName,
                        clauseText = flag.matchedSnippet.ifBlank { flag.explanation },
                        isPro = isProUser.value
                    )
                }

                // Determine sensible title
                val firstCleanLine = extractedText.lines().firstOrNull { it.isNotBlank() && it.trim().length > 3 }?.take(45)?.trim() ?: "Audited Contract"
                val docTitle = if (firstCleanLine.length > 5) firstCleanLine else "Audited Contract"
                val docId = UUID.randomUUID().toString()

                // Persist full audit result to Room Database with user settings
                val docEntity = DocumentEntity(
                    id = docId,
                    title = docTitle,
                    country = userCountry,
                    contractType = userContractType,
                    dateScanned = System.currentTimeMillis(),
                    pageCount = uris.size,
                    rawText = extractedText
                )

                val clauseEntities = result.matchedRedFlags.mapIndexed { idx, flag ->
                    ClauseEntity(
                        id = UUID.randomUUID().toString(),
                        documentId = docId,
                        orderIndex = idx,
                        text = flag.matchedSnippet.ifBlank { flag.explanation },
                        category = flag.category,
                        severity = flag.severity,
                        complexityScore = 0f,
                        verdictSource = "TIER1",
                        confidence = null
                    )
                }

                val missingEntities = result.missingMandatoryClauses.map { missing ->
                    MissingProtectionEntity(
                        id = UUID.randomUUID().toString(),
                        documentId = docId,
                        protectionType = missing,
                        isPresent = false
                    )
                }

                val draftEntities = drafts.map { (cat, text) ->
                    NegotiationDraftEntity(
                        id = UUID.randomUUID().toString(),
                        documentId = docId,
                        clauseId = cat,
                        draftText = text
                    )
                }

                contractRepository.saveFullAuditResult(
                    document = docEntity,
                    clauses = clauseEntities,
                    missingProtections = missingEntities,
                    negotiationDrafts = draftEntities
                )

                _uiState.value = AuditState.Result(
                    documentId = docId,
                    documentTitle = docTitle,
                    matchedRedFlags = result.matchedRedFlags,
                    missingMandatoryClauses = result.missingMandatoryClauses,
                    isHighRisk = result.isHighRisk,
                    extractedText = extractedText,
                    overallRiskScore = calculatedScore,
                    complexityScoreOverall = 0,
                    realCostBreakdown = null,
                    negotiationDrafts = drafts
                )
            } catch (e: Exception) {
                _uiState.value = AuditState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }

    fun processExtractedText(text: String, title: String = "Imported Document") {
        if (text.isBlank()) {
            _uiState.value = AuditState.Error("Document text is empty.")
            return
        }
        viewModelScope.launch {
            try {
                val userCountry = preferenceManager.defaultCountry.first()
                val userContractType = preferenceManager.defaultContractType.first()

                _uiState.value = AuditState.Analyzing
                val result = legalAuditEngine.analyzeContract(
                    rawText = text,
                    country = userCountry,
                    contractType = userContractType
                )
                val calculatedScore = result.overallRiskScore

                val drafts = result.matchedRedFlags.associate { flag ->
                    flag.displayName to com.example.engine.NegotiationTemplateEngine.generateDraft(
                        category = flag.displayName,
                        clauseText = flag.matchedSnippet.ifBlank { flag.explanation },
                        isPro = isProUser.value
                    )
                }

                val docId = UUID.randomUUID().toString()
                val docEntity = DocumentEntity(
                    id = docId,
                    title = title,
                    country = userCountry,
                    contractType = userContractType,
                    dateScanned = System.currentTimeMillis(),
                    pageCount = 1,
                    rawText = text
                )

                val clauseEntities = result.matchedRedFlags.mapIndexed { idx, flag ->
                    ClauseEntity(
                        id = UUID.randomUUID().toString(),
                        documentId = docId,
                        orderIndex = idx,
                        text = flag.matchedSnippet.ifBlank { flag.explanation },
                        category = flag.category,
                        severity = flag.severity,
                        complexityScore = 0f,
                        verdictSource = "TIER1",
                        confidence = null
                    )
                }

                val missingEntities = result.missingMandatoryClauses.map { missing ->
                    MissingProtectionEntity(
                        id = UUID.randomUUID().toString(),
                        documentId = docId,
                        protectionType = missing,
                        isPresent = false
                    )
                }

                val draftEntities = drafts.map { (cat, draft) ->
                    NegotiationDraftEntity(
                        id = UUID.randomUUID().toString(),
                        documentId = docId,
                        clauseId = cat,
                        draftText = draft
                    )
                }

                contractRepository.saveFullAuditResult(
                    document = docEntity,
                    clauses = clauseEntities,
                    missingProtections = missingEntities,
                    negotiationDrafts = draftEntities
                )

                _uiState.value = AuditState.Result(
                    documentId = docId,
                    documentTitle = title,
                    matchedRedFlags = result.matchedRedFlags,
                    missingMandatoryClauses = result.missingMandatoryClauses,
                    isHighRisk = result.isHighRisk,
                    extractedText = text,
                    overallRiskScore = calculatedScore,
                    complexityScoreOverall = 0,
                    realCostBreakdown = null,
                    negotiationDrafts = drafts
                )
            } catch (e: Exception) {
                _uiState.value = AuditState.Error(e.localizedMessage ?: "Failed to analyze document")
            }
        }
    }

    fun loadSavedDocument(documentId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AuditState.Analyzing
                val document = contractRepository.getDocumentById(documentId)
                if (document == null) {
                    _uiState.value = AuditState.Error("Document not found.")
                    return@launch
                }
                val clauses = contractRepository.getClausesForDocumentSync(documentId)
                val missingProtections = contractRepository.getMissingProtectionsForDocumentSync(documentId)
                val drafts = contractRepository.getNegotiationDraftsForDocumentSync(documentId)

                val redFlags = clauses.map { clause ->
                    val displayName = clause.category?.replace("_", " ")?.lowercase()?.split(" ")?.joinToString(" ") { word ->
                        word.replaceFirstChar { it.uppercase() }
                    } ?: "Flagged Clause"

                    MatchedRedFlag(
                        category = clause.category ?: "FLAG",
                        displayName = displayName,
                        matchedSnippet = clause.text,
                        severity = clause.severity,
                        explanation = clause.text
                    )
                }

                val missingList = missingProtections.filter { !it.isPresent }.map { it.protectionType }
                val draftsMap = drafts.associate { it.clauseId to it.draftText }

                val calculatedScore = com.example.engine.AuditResult.calculateRiskScore(redFlags, missingList)

                _uiState.value = AuditState.Result(
                    documentId = document.id,
                    documentTitle = document.title,
                    matchedRedFlags = redFlags,
                    missingMandatoryClauses = missingList,
                    isHighRisk = redFlags.any { it.severity == 3 },
                    extractedText = document.rawText,
                    overallRiskScore = calculatedScore,
                    complexityScoreOverall = 0,
                    realCostBreakdown = null,
                    negotiationDrafts = draftsMap
                )
            } catch (e: Exception) {
                _uiState.value = AuditState.Error("Failed to load saved document: ${e.localizedMessage}")
            }
        }
    }

    fun deleteSavedDocument(documentId: String) {
        viewModelScope.launch {
            contractRepository.deleteDocumentById(documentId)
        }
    }

    fun exportCurrentAuditPdf(): Uri? {
        val currentResult = _uiState.value as? AuditState.Result ?: return null
        return pdfExportEngine.generateAuditReport(
            documentTitle = currentResult.documentTitle,
            isHighRisk = currentResult.isHighRisk,
            riskScore = currentResult.overallRiskScore,
            redFlags = currentResult.matchedRedFlags,
            missingClauses = currentResult.missingMandatoryClauses
        )
    }

    fun reset() {
        _uiState.value = AuditState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        legalAuditEngine.close()
    }
}

class MainViewModelFactory(
    private val application: Application,
    private val scannerEngine: ScannerEngine,
    private val legalAuditEngine: LegalAuditEngine,
    private val preferenceManager: PreferenceManager
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(application, scannerEngine, legalAuditEngine, preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
