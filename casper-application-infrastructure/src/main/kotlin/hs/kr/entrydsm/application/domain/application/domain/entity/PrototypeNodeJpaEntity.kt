package hs.kr.entrydsm.application.domain.application.domain.entity

import hs.kr.entrydsm.application.domain.application.domain.entity.enums.NodeType
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "prototype_nodes")
class PrototypeNodeJpaEntity(
    
    @Id
    @Column(name = "node_id", columnDefinition = "BINARY(16)")
    val nodeId: UUID,
    
    @Column(name = "prototype_id", columnDefinition = "BINARY(16)", nullable = false)
    val prototypeId: UUID,
    
    @Column(name = "parent_node_id", columnDefinition = "BINARY(16)", nullable = true)
    val parentNodeId: UUID?,
    
    @Column(name = "node_name", nullable = false, length = 100)
    val nodeName: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false, length = 20)
    val nodeType: NodeType,
    
    @Column(name = "node_level", nullable = false)
    val nodeLevel: Int,
    
    @Column(name = "field_category", length = 20)
    val fieldCategory: String?, // "application" or "score" (FIELD일 때만)
    
    @Column(name = "field_type", length = 20)
    val fieldType: String?, // "string", "number", "boolean" (FIELD일 때만)
    
    @Column(name = "required", nullable = true)
    val required: Boolean?, // 필수 여부 (FIELD일 때만)
    
    @Column(name = "description", columnDefinition = "TEXT")
    val description: String?, // 설명 (FIELD일 때만)
    
    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int, // 같은 레벨에서 정렬 순서
    
    @Column(name = "created_at", nullable = false)
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
) {
    
    @PreUpdate
    fun preUpdate() {
        updatedAt = java.time.LocalDateTime.now()
    }
    
    protected constructor() : this(
        nodeId = UUID.randomUUID(),
        prototypeId = UUID.randomUUID(),
        parentNodeId = null,
        nodeName = "",
        nodeType = NodeType.ROOT,
        nodeLevel = 0,
        fieldCategory = null,
        fieldType = null,
        required = null,
        description = null,
        sortOrder = 0
    )
}