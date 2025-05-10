package io.casper.convention.tasks

import io.casper.convention.exception.DocumentationException
import io.casper.convention.model.CodeElement
import io.casper.convention.service.DocCheckService
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * KDoc 문서화 검사를 수행하는 Gradle 태스크 클래스입니다.
 * 특정 유형의 코드 요소에 대한 KDoc 주석 검사를 수행하고
 * 결과에 따라 빌드 성공 또는 실패를 처리합니다.
 */
abstract class DocCheckTask : DefaultTask() {
    /**
     * 검사할 코드 요소 타입
     * Gradle 태스크 설정에서 주입됩니다.
     */
    @get:Input
    abstract val codeElement: Property<CodeElement>
    
    /**
     * 태스크 실행 시 호출되는 메소드입니다.
     * 문서화 검사를 수행하고 결과를 처리합니다.
     * 
     * @throws GradleException 문서화 검사 실패 시 발생
     */
    @TaskAction
    fun check() {
        val element = codeElement.get()
        
        val checkService = DocCheckService(project)
        val success = checkService.checkDocumentation(element)
        
        if (!success) {
            throw DocumentationException.missingDocumentation(element)
        }
    }
}