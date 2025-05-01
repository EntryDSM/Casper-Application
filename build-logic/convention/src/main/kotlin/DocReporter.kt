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
     * 콘솔 출력용 ANSI 색상 코드 맵
     */
    private val colors = mapOf(
        "red" to "\u001B[31m",
        "green" to "\u001B[32m",
        "yellow" to "\u001B[33m",
        "blue" to "\u001B[34m",
        "purple" to "\u001B[35m",
        "cyan" to "\u001B[36m",
        "reset" to "\u001B[0m"
    )
    
    /**
     * 작업 시작 메시지를 출력합니다.
     *
     * @param element 검사 중인 코드 요소 타입
     */
    fun reportStart(element: CodeElement) {
        logger.lifecycle("")
        logger.lifecycle("${colors["blue"]}✨ ${element.friendlyName} 문서화 검사 시작...${colors["reset"]}")
        logger.lifecycle("${colors["blue"]}=================================================${colors["reset"]}")
    }
    
    /**
     * 작업 성공 메시지를 출력합니다.
     * 모든 검사가 통과했을 때 호출됩니다.
     *
     * @param element 검사를 완료한 코드 요소 타입
     */
    fun reportSuccess(element: CodeElement) {
        logger.lifecycle("")
        logger.lifecycle("${colors["green"]}")
        logger.lifecycle("")
    }
    
    fun reportProblems(element: CodeElement, problems: List<DocumentationProblem>) {
        logger.lifecycle("")
        logger.lifecycle("${colors["red"]}${problems.size}개의 ${element.friendlyName}에 KDoc 주석이 없습니다.${colors["reset"]}")
        logger.lifecycle("${colors["red"]}=================================================${colors["reset"]}")
        
        // 파일별로 그룹화하여 출력
        val problemsByFile = problems.groupBy { it.fileName }
        
        problemsByFile.forEach { (fileName, fileProblems) ->
            logger.lifecycle("")
            logger.lifecycle("${colors["yellow"]}📄 $fileName${colors["reset"]}")
            
            fileProblems.forEach { problem ->
                logger.error("  ${colors["red"]}→ 라인 ${problem.lineNumber}: ${problem.elementName}${colors["reset"]}")
            }
        }
        
        // 도움말 메시지 출력
        logger.lifecycle("")
        logger.lifecycle("${colors["cyan"]}💡 도움말: KDoc 주석은 다음과 같이 작성할 수 있습니다:${colors["reset"]}")
        logger.lifecycle("${colors["cyan"]}/**${colors["reset"]}")
        logger.lifecycle("${colors["cyan"]} * 이 ${element.friendlyName}이 무슨 일을 하는지 설명합니다.${colors["reset"]}")
        logger.lifecycle("${colors["cyan"]} */${colors["reset"]}")
        logger.lifecycle("")
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
        logger.lifecycle("${colors["blue"]}검사 중... ($percentage%) - $fileName${colors["reset"]}")
    }
}
