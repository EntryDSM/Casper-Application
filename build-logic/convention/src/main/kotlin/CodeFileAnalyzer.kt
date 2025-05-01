import kotlin.io.path.Path

/**
 * 코드 파일을 분석하여 KDoc 주석이 없는 코드 요소를 찾는 분석기 클래스입니다.
 * 파일 내용을 줄 단위로 검사하여 클래스, 객체, 인터페이스, 함수 등의 선언을 찾고
 * 해당 선언 앞에 KDoc 주석이 있는지 확인합니다.
 */
class CodeFileAnalyzer(
    /**
     * 분석할 파일의 경로
     */
    private val filePath: String,
    
    /**
     * 분석할 파일의 내용
     */
    private val fileContent: String
) {
    /**
     * 발견된 문서화 문제 목록
     */
    private val foundProblems = mutableListOf<DocumentationProblem>()
    
    /**
     * 위치에서 줄 번호를 빠르게 찾기 위한 맵
     */
    private val lineMap = createLineMap()
    
    /**
     * 파일의 줄 번호를 빠르게 찾기 위한 맵을 생성합니다.
     * 파일 내 각 문자 위치에 대한 줄 번호를 저장합니다.
     *
     * @return 문자 위치에서 줄 번호로의 매핑
     */
    private fun createLineMap(): Map<Int, Int> {
        val map = mutableMapOf<Int, Int>()
        var lineNumber = 1
        
        fileContent.forEachIndexed { index, char ->
            if (char == '\n') {
                lineNumber++
            }
            map[index] = lineNumber
        }
        
        return map
    }
    
    /**
     * 파일 내 모든 코드 요소를 찾고 문서화 여부를 확인합니다.
     * 클래스, 객체, 인터페이스, 함수 등을 순차적으로 검사합니다.
     *
     * @return 발견된 문서화 문제 목록
     */
    fun findClasses(): List<DocumentationProblem> {
        fileContent.lines().forEachIndexed { index, line ->
            val lineNumber = index + 1
            val trimmedLine = line.trim()
            
            if (trimmedLine.startsWith("class ")) {
                val className = extractName(trimmedLine, "class ")
                checkDocumentation(CodeElement.CLASS, className, lineNumber)
            }
            else if (trimmedLine.startsWith("object ")) {
                val objectName = extractName(trimmedLine, "object ")
                checkDocumentation(CodeElement.OBJECT, objectName, lineNumber)
            }
            else if (trimmedLine.startsWith("interface ")) {
                val interfaceName = extractName(trimmedLine, "interface ")
                checkDocumentation(CodeElement.INTERFACE, interfaceName, lineNumber)
            }
            else if (trimmedLine.startsWith("fun ") && !trimmedLine.contains("private ")) {
                val functionName = extractFunctionName(trimmedLine)
                checkDocumentation(CodeElement.FUNCTION, functionName, lineNumber)
            }
        }
        
        return foundProblems
    }
    
    /**
     * 코드 선언에서 이름 부분을 추출합니다.
     *
     * @param line 코드 줄
     * @param prefix 제거할 접두사 (예: "class ", "object ")
     * @return 추출된 이름
     */
    private fun extractName(line: String, prefix: String): String {
        val afterPrefix = line.substring(prefix.length)
        val endIndex = afterPrefix.indexOfFirst { it == ' ' || it == '(' || it == ':' || it == '<' }
        return if (endIndex >= 0) afterPrefix.substring(0, endIndex) else afterPrefix
    }
    
    /**
     * 함수 선언에서 함수 이름을 추출합니다.
     *
     * @param line 함수 선언이 포함된 코드 줄
     * @return 추출된 함수 이름
     */
    private fun extractFunctionName(line: String): String {
        val afterFun = line.substring(4).trim()
        val parenIndex = afterFun.indexOf('(')
        return if (parenIndex >= 0) afterFun.substring(0, parenIndex).trim() else afterFun
    }
    
    /**
     * 코드 요소 위에 KDoc 주석이 있는지 확인합니다.
     * 주석이 없으면 문제 목록에 추가합니다.
     *
     * @param element 검사할 코드 요소 타입
     * @param elementName 요소 이름
     * @param lineNumber 요소가 선언된 줄 번호
     */
    private fun checkDocumentation(
        element: CodeElement,
        elementName: String,
        lineNumber: Int
    ) {
        var currentLine = lineNumber - 1
        var hasComment = false
        
        while (currentLine > 0) {
            val previousLine = fileContent.lines().getOrNull(currentLine - 1)?.trim() ?: ""
            
            if (previousLine.isEmpty()) {
                break
            } else if (previousLine.endsWith(DocConstants.KDOC_END)) {
                hasComment = true
                break
            }
            
            currentLine--
        }
        
        if (!hasComment) {
            val fileName = Path(filePath).fileName.toString()
            foundProblems.add(
                DocumentationProblem(
                    element = element,
                    elementName = elementName,
                    filePath = filePath,
                    fileName = fileName,
                    lineNumber = lineNumber
                )
            )
        }
    }
}
