package io.casper.convention.service

import io.casper.convention.model.CodeElement
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

        // 파일 분석 및 결과 처리 (함수형 스타일로 개선)
        val problems = sourceFiles
            .mapIndexed { index, file ->
                // 진행 상황 보고
                reporter.reportProgress(index + 1, sourceFiles.size, file.name)
                
                // 파일 분석
                CodeFileAnalyzer.analyze(file.absolutePath, file.readText(), element)
            }
            .flatten()

        // 검사 결과 처리
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
