package io.casper.convention.service

import io.casper.convention.model.CodeElement
import io.casper.convention.model.DocumentationProblem
import io.casper.convention.service.component.CodeFileAnalyzerComponent
import io.casper.convention.util.DocConstants
import org.gradle.api.Project
import java.io.File

/**
 * KDoc 주석 검사 서비스 클래스입니다.
 * 소스 코드 파일을 분석하여 KDoc 주석이 없는 코드 요소를 찾습니다.
 * 함수형 프로그래밍 스타일을 적용하여 상태 변경을 최소화합니다.
 */
class DocCheckService(
    private val project: Project
) {
    // 단일 인스턴스로 의존성 주입
    private val reporter = DocReporter(project.logger)

    /**
     * 특정 타입의 코드 요소에 대한 문서화 검사를 실행합니다.
     * 프로젝트 내 모든 Kotlin 소스 파일을 검사하고 결과를 보고합니다.
     *
     * @param element 검사할 코드 요소 타입
     * @return 검사 성공 여부 (true: 모든 요소에 KDoc 주석 있음, false: 주석 없는 요소 있음)
     */
    fun checkDocumentation(element: CodeElement): Boolean {
        reporter.reportStart(element)

        val sourceFiles = findKotlinSourceFiles()
        if (sourceFiles.isEmpty()) {
            project.logger.warn("코틀린 소스 파일을 찾을 수 없습니다.")
            return true
        }
        val problem = findDocumentation(sourceFiles, element)
        return reportResults(element, problem)
    }

    /**
     * 프로젝트 내 소스 파일들을 분석하여 문서화 문제를 찾습니다.
     *
     * @param sourceFile 분석할 소스 파일 목록
     * @param element 검사할 코드 요소 타입
     * @return 발견된 문서화 문제 목록
     */
    private fun findDocumentation(sourceFile : List<File>,element: CodeElement): List<DocumentationProblem> {
        return sourceFile
            .mapIndexed { index, file ->
                reporter.reportProgress(index +1, sourceFile.size, file.name)

                CodeFileAnalyzerComponent.analyze(file.absolutePath, file.readText(), element)
            }
            .flatten()
    }
    /**
     * 분석 결과를 보고하고 검사 성공 여부를 결정합니다.
     *
     * @param element 검사된 코드 요소 타입
     * @param problems 발견된 문서화 문제 목록
     * @return 검사 성공 여부 (true: 문제 없음, false: 문제 있음)
     */
    private fun reportResults(element: CodeElement, problems: List<DocumentationProblem>): Boolean {
        return when {
            problems.isEmpty() -> {
                reporter.reportSuccess(element)
                true
            }

            else -> {
                reporter.reportProblems(element, problems)
                false
            }
        }
    }
    
    /**
     * 프로젝트 내 모든 Kotlin 소스 파일을 찾아 반환합니다.
     * 확장 함수를 사용하여 가독성을 개선했습니다.
     *
     * @return Kotlin 소스 파일 목록
     */
    private fun findKotlinSourceFiles(): List<File> =
        project.fileTree(DocConstants.SRC_FOLDER) {
            include(DocConstants.KOTLIN_FILES)
        }.files.toList()
}
