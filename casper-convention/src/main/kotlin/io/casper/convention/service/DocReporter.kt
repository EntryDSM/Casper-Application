package io.casper.convention.service

import io.casper.convention.model.CodeElement
import io.casper.convention.model.DocumentationProblem
import io.casper.convention.util.DocConstants.COLORS
import org.gradle.api.logging.Logger

/**
 * 문서화 문제를 예쁘게 출력해주는 리포터 클래스입니다.
 * 콘솔에 색상이 있는 로그 메시지를 출력하여 문서화 검사 결과를 가시적으로 보여줍니다.
 */
class DocReporter(
    /**
     * Gradle 로깅 인터페이스
     */
    private val logger: Logger
) {
    /**
     * 작업 시작 메시지를 출력합니다.
     *
     * @param element 검사 중인 코드 요소 타입
     */
    fun reportStart(element: CodeElement) = with(logger) {
        lifecycle("")
        lifecycle("${COLORS["blue"]}✨ ${element.friendlyName} 문서화 검사 시작...${COLORS["reset"]}")
        lifecycle("${COLORS["blue"]}=================================================${COLORS["reset"]}")
    }
    
    /**
     * 작업 성공 메시지를 출력합니다.
     * 모든 검사가 통과했을 때 호출됩니다.
     *
     * @param element 검사를 완료한 코드 요소 타입
     */
    fun reportSuccess(element: CodeElement) = with(logger) {
        lifecycle("")
        lifecycle("${COLORS["green"]}✅ 모든 ${element.friendlyName}에 KDoc 주석이 있습니다!${COLORS["reset"]}")
        lifecycle("")
    }
    
    /**
     * 발견된 문제를 보고합니다.
     *
     * @param element 검사한 코드 요소 타입
     * @param problems 발견된 문서화 문제 목록
     */
    fun reportProblems(element: CodeElement, problems: List<DocumentationProblem>) {
        // 헤더 출력
        logger.lifecycle("")
        logger.lifecycle("${COLORS["red"]}❌ ${problems.size}개의 ${element.friendlyName}에 KDoc 주석이 없습니다.${COLORS["reset"]}")
        logger.lifecycle("${COLORS["red"]}=================================================${COLORS["reset"]}")
        
        // 파일별로 그룹화하여 출력
        problems.groupBy { it.fileName }
            .forEach { (fileName, fileProblems) ->
                reportFileProblems(fileName, fileProblems)
            }
        
        // 도움말 메시지 출력
        reportHelpMessage(element)
    }
    
    /**
     * 단일 파일의 문제를 출력합니다.
     *
     * @param fileName 파일 이름
     * @param problems 해당 파일의 문제 목록
     */
    private fun reportFileProblems(fileName: String, problems: List<DocumentationProblem>) {
        logger.lifecycle("")
        logger.lifecycle("${COLORS["yellow"]}📄 $fileName${COLORS["reset"]}")
        
        problems.forEach { problem ->
            logger.error("  ${COLORS["red"]}→ 라인 ${problem.lineNumber}: ${problem.elementName}${COLORS["reset"]}")
        }
    }
    
    /**
     * KDoc 주석 작성 도움말을 출력합니다.
     *
     * @param element 검사한 코드 요소 타입
     */
    fun reportHelpMessage(element: CodeElement) = with(logger) {
        lifecycle("")
        lifecycle("${COLORS["cyan"]}💡 도움말: KDoc 주석은 다음과 같이 작성할 수 있습니다:${COLORS["reset"]}")
        lifecycle("${COLORS["cyan"]}/**${COLORS["reset"]}")
        lifecycle("${COLORS["cyan"]} * 이 ${element.friendlyName}이 무슨 일을 하는지 설명합니다.${COLORS["reset"]}")
        lifecycle("${COLORS["cyan"]} */${COLORS["reset"]}")
        lifecycle("")
    }
    
    /**
     * 파일 검사 진행 상황을 출력합니다.
     *
     * @param current 현재까지 처리한 파일 수
     * @param total 전체 파일 수
     * @param fileName 현재 검사 중인 파일 이름
     */
    fun reportProgress(current: Int, total: Int, fileName: String) {
        val percentage = (current * 100) / total
        logger.lifecycle("${COLORS["blue"]}검사 중... ($percentage%) - $fileName${COLORS["reset"]}")
    }
    
    /**
     * 모듈 검사 시작 메시지를 출력합니다.
     *
     * @param moduleName 검사 중인 모듈 이름
     */
    fun reportModuleStart(moduleName: String) {
        logger.lifecycle("")
        logger.lifecycle("${COLORS["purple"]}📦 모듈 검사: $moduleName${COLORS["reset"]}")
        logger.lifecycle("${COLORS["purple"]}--------------------------------${COLORS["reset"]}")
    }
    
    /**
     * 모듈 검사 결과 요약을 출력합니다.
     *
     * @param moduleName 모듈 이름
     * @param success 검사 성공 여부
     * @param problemCount 발견된 문제 수
     */
    fun reportModuleResult(moduleName: String, success: Boolean, problemCount: Int) {
        val status = if (success) "${COLORS["green"]}✅ 성공${COLORS["reset"]}" 
                     else "${COLORS["red"]}❌ 실패${COLORS["reset"]}"
        
        logger.lifecycle("$moduleName: $status (문제 수: $problemCount)")
    }
    
    /**
     * 전체 검사 결과 요약을 출력합니다.
     *
     * @param totalModules 검사한 모듈 수
     * @param successModules 검사 성공 모듈 수
     * @param totalFiles 검사한 파일 수
     * @param totalProblems 발견된 전체 문제 수
     */
    fun reportSummary(totalModules: Int, successModules: Int, totalFiles: Int, totalProblems: Int) {
        logger.lifecycle("")
        logger.lifecycle("${COLORS["blue"]}📊 검사 결과 요약${COLORS["reset"]}")
        logger.lifecycle("${COLORS["blue"]}=================================${COLORS["reset"]}")
        logger.lifecycle("총 모듈 수: $totalModules")
        logger.lifecycle("검사 성공 모듈 수: $successModules")
        logger.lifecycle("검사 실패 모듈 수: ${totalModules - successModules}")
        logger.lifecycle("총 파일 수: $totalFiles")
        logger.lifecycle("총 문제 수: $totalProblems")
    }
}
