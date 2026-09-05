package com.example.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.engine.ContractType

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val country: String,          // e.g. "US", "UK", "IN"
    val contractType: ContractType, // Strong ContractType enum
    val dateScanned: Long,
    val pageCount: Int,
    val rawText: String
)

@Entity(
    tableName = "clauses",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["documentId"])]
)
data class ClauseEntity(
    @PrimaryKey val id: String,
    val documentId: String,       // FK -> DocumentEntity
    val orderIndex: Int,
    val text: String,
    val category: String?,        // one of the 8 taxonomy categories, null if clean
    val severity: Int,            // 1-3
    val complexityScore: Float,   // 0 - 100
    val verdictSource: String,    // "TIER1" | "TIER2" | "NONE"
    val confidence: Float?        // Tier 2 only
)

@Entity(
    tableName = "missing_protections",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["documentId"])]
)
data class MissingProtectionEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val protectionType: String,
    val isPresent: Boolean
)

@Entity(
    tableName = "negotiation_drafts",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["documentId"])]
)
data class NegotiationDraftEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val clauseId: String,
    val draftText: String
)
