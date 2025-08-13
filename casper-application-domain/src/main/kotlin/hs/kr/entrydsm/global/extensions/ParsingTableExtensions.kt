package hs.kr.entrydsm.global.extensions

import hs.kr.entrydsm.domain.lexer.entities.TokenType
import hs.kr.entrydsm.domain.parser.values.ParsingTable

/**
 * ParsingTable 클래스를 위한 분석, 통계, 출력 확장 함수들입니다.
 * 
 * 이 확장 함수들은 개발 도구, 디버깅, 분석 목적으로 사용되며,
 * 핵심 파싱 로직과 분리하여 관심사를 명확히 합니다.
 *
 * @author kangeunchan
 * @since 2025.08.13
 */

/**
 * 테이블의 크기 정보를 반환합니다.
 *
 * @return 크기 정보 맵
 */
fun ParsingTable.getSizeInfo(): Map<String, Int> = mapOf(
    "stateCount" to states.size,
    "actionCount" to actionTable.size,
    "gotoCount" to gotoTable.size,
    "terminalCount" to terminals.size,
    "nonTerminalCount" to nonTerminals.size,
    "acceptStateCount" to acceptStates.size
)

/**
 * 테이블의 메모리 사용량을 추정합니다.
 *
 * @return 메모리 사용량 정보 맵 (바이트)
 */
fun ParsingTable.getMemoryUsage(): Map<String, Long> {
    val stateMemory = states.size * 1000L // 상태당 대략 1KB
    val actionMemory = actionTable.size * 100L // 액션당 대략 100B
    val gotoMemory = gotoTable.size * 100L // goto당 대략 100B
    
    return mapOf(
        "states" to stateMemory,
        "actions" to actionMemory,
        "gotos" to gotoMemory,
        "total" to (stateMemory + actionMemory + gotoMemory)
    )
}

/**
 * 테이블의 압축률을 계산합니다.
 *
 * @return 압축률 (0.0 ~ 1.0)
 */
fun ParsingTable.getCompressionRatio(): Double {
    val totalCells = states.size * (terminals.size + nonTerminals.size)
    val usedCells = actionTable.size + gotoTable.size
    return if (totalCells > 0) 1.0 - (usedCells.toDouble() / totalCells) else 0.0
}

/**
 * 테이블을 압축합니다 (빈 엔트리 제거).
 *
 * @return 압축된 파싱 테이블
 */
fun ParsingTable.compress(): ParsingTable {
    // 실제로는 이미 압축된 형태이므로 자기 자신을 반환
    return this
}

/**
 * 테이블을 텍스트 형태로 출력합니다.
 *
 * @return 테이블 문자열
 */
fun ParsingTable.toTableString(): String = buildString {
    appendLine("LR Parsing Table:")
    appendLine("States: ${states.size}")
    appendLine("Terminals: ${terminals.joinToString(", ")}")
    appendLine("Non-terminals: ${nonTerminals.joinToString(", ")}")
    appendLine()
    
    appendLine("Action Table:")
    terminals.forEach { terminal ->
        appendLine("  $terminal:")
        states.keys.sorted().forEach { stateId ->
            val action = getAction(stateId, terminal)
            if (action != null) {
                appendLine("    $stateId: $action")
            }
        }
    }
    
    appendLine()
    appendLine("Goto Table:")
    nonTerminals.forEach { nonTerminal ->
        appendLine("  $nonTerminal:")
        states.keys.sorted().forEach { stateId ->
            val goto = getGoto(stateId, nonTerminal)
            if (goto != null) {
                appendLine("    $stateId: $goto")
            }
        }
    }
    
    val conflicts = getConflicts()
    if (conflicts.isNotEmpty()) {
        appendLine()
        appendLine("Conflicts:")
        conflicts.forEach { (type, details) ->
            appendLine("  $type:")
            details.forEach { detail ->
                appendLine("    $detail")
            }
        }
    }
}

/**
 * 테이블의 통계 정보를 반환합니다.
 *
 * @return 통계 정보 맵
 */
fun ParsingTable.getStatistics(): Map<String, Any> = mapOf<String, Any>(
    "stateCount" to states.size,
    "actionEntries" to actionTable.size,
    "gotoEntries" to gotoTable.size,
    "terminalCount" to terminals.size,
    "nonTerminalCount" to nonTerminals.size,
    "acceptStateCount" to acceptStates.size,
    "conflictCount" to getConflicts().values.sumOf { it.size },
    "isLR1Valid" to isLR1Valid(),
    "compressionRatio" to getCompressionRatio(),
    "memoryUsage" to (getMemoryUsage()["total"] ?: 0L),
    "averageActionsPerState" to if (states.isNotEmpty()) actionTable.size.toDouble() / states.size else 0.0,
    "averageGotosPerState" to if (states.isNotEmpty()) gotoTable.size.toDouble() / states.size else 0.0
)

/**
 * 테이블의 상세 요약 정보를 반환합니다.
 *
 * @return 상세 요약 문자열
 */
fun ParsingTable.toDetailedString(): String = buildString {
    append("ParsingTable(")
    append("states=${states.size}, ")
    append("actions=${actionTable.size}, ")
    append("gotos=${gotoTable.size}")
    val conflictCount = getConflicts().values.sumOf { it.size }
    if (conflictCount > 0) {
        append(", conflicts=$conflictCount")
    }
    if (!isLR1Valid()) {
        append(", INVALID")
    }
    append(")")
}