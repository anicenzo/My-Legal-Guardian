package com.example.data

import com.example.data.db.ContractDao
import com.example.data.db.DocumentEntity
import com.example.data.db.ClauseEntity
import com.example.data.db.MissingProtectionEntity
import com.example.data.db.NegotiationDraftEntity
import kotlinx.coroutines.flow.Flow

class ContractRepository(private val contractDao: ContractDao) {

    val allDocuments: Flow<List<DocumentEntity>> = contractDao.getAllDocuments()
    val allClauses: Flow<List<ClauseEntity>> = contractDao.getAllClauses()

    fun getDocumentByIdFlow(id: String): Flow<DocumentEntity?> {
        return contractDao.getDocumentByIdFlow(id)
    }

    suspend fun getDocumentById(id: String): DocumentEntity? {
        return contractDao.getDocumentById(id)
    }

    fun getClausesForDocument(documentId: String): Flow<List<ClauseEntity>> {
        return contractDao.getClausesForDocument(documentId)
    }

    suspend fun getClausesForDocumentSync(documentId: String): List<ClauseEntity> {
        return contractDao.getClausesForDocumentSync(documentId)
    }

    fun getMissingProtectionsForDocument(documentId: String): Flow<List<MissingProtectionEntity>> {
        return contractDao.getMissingProtectionsForDocument(documentId)
    }

    suspend fun getMissingProtectionsForDocumentSync(documentId: String): List<MissingProtectionEntity> {
        return contractDao.getMissingProtectionsForDocumentSync(documentId)
    }

    fun getNegotiationDraftsForDocument(documentId: String): Flow<List<NegotiationDraftEntity>> {
        return contractDao.getNegotiationDraftsForDocument(documentId)
    }

    suspend fun getNegotiationDraftsForDocumentSync(documentId: String): List<NegotiationDraftEntity> {
        return contractDao.getNegotiationDraftsForDocumentSync(documentId)
    }

    suspend fun saveFullAuditResult(
        document: DocumentEntity,
        clauses: List<ClauseEntity>,
        missingProtections: List<MissingProtectionEntity>,
        negotiationDrafts: List<NegotiationDraftEntity>
    ) {
        contractDao.saveFullAuditResult(document, clauses, missingProtections, negotiationDrafts)
    }

    suspend fun deleteDocumentById(id: String) {
        contractDao.deleteDocumentById(id)
    }
}
