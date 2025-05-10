package io.casper.convention.service

import io.casper.convention.model.CodeElement
import io.casper.convention.model.DocumentationProblem
import io.casper.convention.util.DocConstants
import kotlin.io.path.Path

/**
 * 코드 파일을 분석하여 KDoc 주석이 없는 코드 요소를 찾는 분석기 클래스입니다.
 * 함수형 프로그래밍 스타일을 적용하여 상태 변경을 최소화합니다.
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
     * 파일 내용을 한 번만 라인으로 분리하여 재사용
     */
    private val lines = fileContent.lines()
    
    /**
     * 파일 내 모든 코드 요소를 찾고 문서화 여부를 확인합니다.
     *
     * @param element 검사할 코드 요소 타입
     * @return 발견된 문서화 문제 목록
     */
    fun findProblems(element: CodeElement): List<DocumentationProblem> {
        val foundProblems = mutableListOf<DocumentationProblem>()
        val fileName = Path(filePath).fileName.toString()
        
        lines.forEachIndexed { index, line ->
            val lineNumber = index + 1
            val trimmedLine = line.trim()
            
            // 요소 키워드로 검색 (클래스, 함수 등)
            val keyword = when(element) {
                CodeElement.CLASS -> KEYWORD_CLASS
                CodeElement.INTERFACE -> KEYWORD_INTERFACE
                CodeElement.FUNCTION -> KEYWORD_FUNCTION
                CodeElement.OBJECT -> KEYWORD_OBJECT
                CodeElement.PROPERTY -> KEYWORD_PROPERTY
            }
            
            val pattern = "^$keyword\\s+".toRegex()
            if (pattern.containsMatchIn(trimmedLine)) {
                val name = extractElementName(trimmedLine, element)
                
                // KDoc 주석 여부 확인
                if (!hasKDocComment(lineNumber)) {
                    foundProblems.add(
                        DocumentationProblem(
                            element = element,
                            elementName = name,
                            filePath = filePath,
                            fileName = fileName,
                            lineNumber = lineNumber
                        )
                    )
                }
            }
        }
        
        return foundProblems
    }
    
    /**
     * 코드 선언에서 요소 이름을 추출합니다.
     *
     * @param line 코드 라인
     * @param element 코드 요소 타입
     * @return 추출된 요소 이름
     */
    private fun extractElementName(line: String, element: CodeElement): String {
        return when (element) {
            CodeElement.FUNCTION -> extractFunctionName(line)
            else -> {
                val prefix = when(element) {
                    CodeElement.CLASS -> PREFIX_CLASS
                    CodeElement.INTERFACE -> PREFIX_INTERFACE
                    CodeElement.OBJECT ->  PREFIX_OBJECT
                    CodeElement.PROPERTY -> if (line.startsWith(PREFIX_VAL)) PREFIX_VAL else PREFIX_VAR
                    else -> error("확인할 수 없는 요소 타입: $element")
                }
                
                extractNameWithPrefix(line, prefix)
            }
        }
    }
    
    /**
     * 코드 선언에서 이름 부분을 추출합니다.
     *
     * @param line 코드 줄
     * @param prefix 제거할 접두사 (예: "class ", "object ")
     * @return 추출된 이름
     */
    private fun extractNameWithPrefix(line: String, prefix: String): String {
        val afterPrefix = line.substring(line.indexOf(prefix) + prefix.length)
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
        val afterFun = line.substring(line.indexOf(PREFIX_FUNCTION) + 4).trim()
        val parenIndex = afterFun.indexOf('(')
        return if (parenIndex >= 0) afterFun.substring(0, parenIndex).trim() else afterFun
    }
    
    /**
     * 코드 요소 위에 KDoc 주석이 있는지 확인합니다.
     *
     * @param lineNumber 요소가 선언된 줄 번호
     * @return KDoc 주석이 있으면 true, 없으면 false
     */
    private fun hasKDocComment(lineNumber: Int): Boolean {
        var currentLine = lineNumber - 1
        
        while (currentLine > 0) {
            val previousLine = lines.getOrNull(currentLine - 1)?.trim() ?: ""
            
            if (previousLine.isEmpty()) {
                return false
            } else if (previousLine.endsWith(DocConstants.KDOC_END)) {
                return true
            }
            
            currentLine--
        }
        
        return false
    }
    
    companion object {

        const val KEYWORD_CLASS = "class"
        const val KEYWORD_INTERFACE = "interface"
        const val KEYWORD_FUNCTION = "fun"
        const val KEYWORD_OBJECT = "object"
        const val KEYWORD_PROPERTY = "(val|var)"

        const val PREFIX_CLASS = "class "
        const val PREFIX_INTERFACE = "interface "
        const val PREFIX_FUNCTION = "fun "
        const val PREFIX_OBJECT = "object "
        const val PREFIX_VAL = "val "
        const val PREFIX_VAR = "var "
    }
}
