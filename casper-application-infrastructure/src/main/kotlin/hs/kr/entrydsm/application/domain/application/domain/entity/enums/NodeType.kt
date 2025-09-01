package hs.kr.entrydsm.application.domain.application.domain.entity.enums

enum class NodeType {
    ROOT,       // 최상위 루트 노드
    GROUP,      // 그룹 노드 (application, personal, grade3_1 등)
    FIELD       // 실제 필드 노드 (name, korean, math 등)
}