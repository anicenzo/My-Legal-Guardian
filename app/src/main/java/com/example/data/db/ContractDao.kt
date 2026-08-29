package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ContractDao {

    // --- Document Queries & Inserts ---
    @Query("SELECT * FROM documents ORDER BY dateScanned DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: String): DocumentEntity?

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    fun getDocumentByIdFlow(id: String): Flow<DocumentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)

    // --- Clause Queries & Inserts ---
    @Query("SELECT * FROM clauses")
    fun getAllClauses(): Flow<List<ClauseEntity>>

    @Query("SELECT * FROM clauses WHERE documentId = :documentId ORDER BY orderIndex ASC")
    fun getClausesForDocument(documentId: String): Flow<List<ClauseEntity>>

    @Query("SELECT * FROM clauses WHERE documentId = :documentId ORDER BY orderIndex ASC")
    suspend fun getClausesForDocumentSync(documentId: String): List<ClauseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClauses(clauses: List<ClauseEntity>)

    // --- Missing Protection Queries & Inserts ---
    @Query("SELECT * FROM missing_protections WHERE documentId = :documentId")
    fun getMissingProtectionsForDocument(documentId: String): Flow<List<MissingProtectionEntity>>

    @Query("SELECT * FROM missing_protections WHERE documentId = :documentId")
    suspend fun getMissingProtectionsForDocumentSync(documentId: String): List<MissingProtectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissingProtections(protections: List<MissingProtectionEntity>)

    // --- Negotiation Draft Queries & Inserts ---
    @Query("SELECT * FROM negotiation_drafts WHERE documentId = :documentId")
    fun getNegotiationDraftsForDocument(documentId: String): Flow<List<NegotiationDraftEntity>>

    @Query("SELECT * FROM negotiation_drafts WHERE documentId = :documentId")
    suspend fun getNegotiationDraftsForDocumentSync(documentId: String): List<NegotiationDraftEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNegotiationDrafts(drafts: List<NegotiationDraftEntity>)

    // --- Complete Transaction to save an Audited Document ---
    @Transaction
    suspend fun saveFullAuditResult(
        document: DocumentEntity,
        clauses: List<ClauseEntity>,
        missingProtections: List<MissingProtectionEntity>,
        negotiationDrafts: List<NegotiationDraftEntity>
    ) {
        insertDocument(document)
        insertClauses(clauses)
        insertMissingProtections(missingProtections)
        insertNegotiationDrafts(negotiationDrafts)
    }
}
