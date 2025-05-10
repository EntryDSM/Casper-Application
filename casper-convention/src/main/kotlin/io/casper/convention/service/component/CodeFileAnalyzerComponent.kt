package io.casper.convention.service.component

import io.casper.convention.model.CodeElement
import io.casper.convention.model.DocumentationProblem
import io.casper.convention.service.CodeFileAnalyzer

class CodeFileAnalyzerComponent {

    companion object {
        /**
         * 파일 내용을 분석하여 문서화 문제를 찾습니다.
         *
         * @param filePath 파일 경로
         * @param fileContent 파일 내용
         * @param element 검사할 코드 요소 타입
         * @return 발견된 문서화 문제 목록
         */
        fun analyze(filePath: String, fileContent: String, element: CodeElement): List<DocumentationProblem> =
            CodeFileAnalyzer(filePath, fileContent).findProblems(element)
    }

}