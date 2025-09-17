package hs.kr.entrydsm.application.domain.application.domain.repository

import hs.kr.entrydsm.application.domain.application.domain.entity.PrototypeNodeJpaEntity
import hs.kr.entrydsm.application.domain.application.domain.entity.enums.NodeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface PrototypeNodeJpaRepository : JpaRepository<PrototypeNodeJpaEntity, UUID> {
    fun findAllByPrototypeIdOrderByNodeLevelAscSortOrderAsc(prototypeId: UUID): List<PrototypeNodeJpaEntity>

    fun findAllByPrototypeIdAndNodeType(
        prototypeId: UUID,
        nodeType: NodeType,
    ): List<PrototypeNodeJpaEntity>

    fun findByPrototypeIdAndNodeTypeAndParentNodeIdIsNull(
        prototypeId: UUID,
        nodeType: NodeType,
    ): PrototypeNodeJpaEntity?

    fun findAllByPrototypeIdAndParentNodeId(
        prototypeId: UUID,
        parentNodeId: UUID,
    ): List<PrototypeNodeJpaEntity>

    @Query(
        """
        WITH RECURSIVE tree_path AS (
            SELECT node_id, prototype_id, parent_node_id, node_name, node_type, node_level,
                   field_category, field_type, required, description, sort_order,
                   CAST(node_name AS VARCHAR(500)) as path,
                   CAST(sort_order AS VARCHAR(500)) as order_path
            FROM prototype_nodes 
            WHERE prototype_id = :prototypeId AND parent_node_id IS NULL
            
            UNION ALL
            
            SELECT n.node_id, n.prototype_id, n.parent_node_id, n.node_name, n.node_type, n.node_level,
                   n.field_category, n.field_type, n.required, n.description, n.sort_order,
                   CONCAT(tp.path, '.', n.node_name) as path,
                   CONCAT(tp.order_path, '.', LPAD(n.sort_order, 3, '0')) as order_path
            FROM prototype_nodes n
            INNER JOIN tree_path tp ON n.parent_node_id = tp.node_id
        )
        SELECT * FROM tree_path ORDER BY order_path
    """,
        nativeQuery = true,
    )
    fun findTreeByPrototypeId(
        @Param("prototypeId") prototypeId: UUID,
    ): List<PrototypeNodeJpaEntity>

    fun deleteAllByPrototypeId(prototypeId: UUID)
}
