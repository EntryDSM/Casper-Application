package io.casper.convention.plugins

import io.casper.convention.tasks.DocCheckTask
import io.casper.convention.model.CodeElement
import io.casper.convention.util.DocConstants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

/**
 * Casper 프로젝트의 KDoc 문서화 규칙을 정의하는 Gradle 플러그인입니다.
 */
class DocumentationConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            // 코드 요소별 문서화 검사 태스크 등록
            val classCheck = tasks.register<DocCheckTask>("checkClassDocs") {
                group = DocConstants.DOC_GROUP
                description = "클래스에 KDoc 주석이 있는지 확인합니다"
                codeElement.set(CodeElement.CLASS)
            }

            val objectCheck = tasks.register<DocCheckTask>("checkObjectDocs") {
                group = DocConstants.DOC_GROUP
                description = "객체에 KDoc 주석이 있는지 확인합니다"
                codeElement.set(CodeElement.OBJECT)
            }

            val interfaceCheck = tasks.register<DocCheckTask>("checkInterfaceDocs") {
                group = DocConstants.DOC_GROUP
                description = "인터페이스에 KDoc 주석이 있는지 확인합니다"
                codeElement.set(CodeElement.INTERFACE)
            }

            val functionCheck = tasks.register<DocCheckTask>("checkFunctionDocs") {
                group = DocConstants.DOC_GROUP
                description = "함수에 KDoc 주석이 있는지 확인합니다"
                codeElement.set(CodeElement.FUNCTION)
            }

            // 모든 문서화 검사를 한 번에 실행하는 태스크
            tasks.register("checkAllDocs") {
                group = DocConstants.CHECK_GROUP
                description = "모든 코드 요소의 KDoc 주석 여부를 확인합니다"
                
                // 모든 개별 검사 태스크에 의존
                dependsOn(
                    classCheck,
                    objectCheck,
                    interfaceCheck,
                    functionCheck
                )
            }

            // 빌드 검증 과정에 문서화 검사 포함
            tasks.named("check") {
                dependsOn("checkAllDocs")
            }
        }
    }
}
