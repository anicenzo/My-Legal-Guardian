package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.AuditResult
import com.example.engine.LegalAuditEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContractViewModel(
    private val legalAuditEngine: LegalAuditEngine
) : ViewModel() {

    private val _auditResult = MutableStateFlow<AuditResult?>(null)
    val auditResult: StateFlow<AuditResult?> = _auditResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun auditDocument(text: String) {
        if (text.isBlank()) {
            _error.value = "Document text is empty."
            return
        }

        _isAnalyzing.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // Cleanly delegates to the newly simplified analyzeContract function
                val result = legalAuditEngine.analyzeContract(text)
                _auditResult.value = result
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to analyze contract."
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun clearResult() {
        _auditResult.value = null
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        legalAuditEngine.close()
    }
}
