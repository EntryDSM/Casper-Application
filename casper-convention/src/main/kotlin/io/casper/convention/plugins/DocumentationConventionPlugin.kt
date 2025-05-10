package io.casper.convention.plugins

import io.casper.convention.tasks.DocCheckTask
import io.casper.convention.model.DocCheckTaskType
import io.casper.convention.util.DocConstants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

/**
 * Casper 프로젝트의 KDoc 문서화 규칙을 정의하는 Gradle 플러그인입니다.
 */
class DocumentationConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            // 현재 프로젝트에 태스크 등록
            registerDocTasks(this)

            // 모든 서브프로젝트에도 태스크 등록
            subprojects {
                registerDocTasks(this)
            }
        }
    }


    private fun Project.registerDocTasks(project: Project) {
        with(project) {
            val registeredTasks = mutableListOf<TaskProvider<out Task>>()

            DocCheckTaskType.values().forEach { taskType ->
                val task = tasks.register<DocCheckTask>(taskType.taskName) {
                    group = DocConstants.DOC_GROUP
                    description = taskType.description
                    codeElement.set(taskType.codeElement)
                }
                registeredTasks.add(task)
            }

            // 모든 문서화 검사를 한 번에 실행하는 태스크
    tasks.register("checkAllDocs") {
        group = DocConstants.CHECK_GROUP
        description = "모든 코드 요소의 KDoc 주석 여부를 확인합니다"

        // 등록된 모든 검사 태스크에 의존
        dependsOn(registeredTasks)
    }

    // 빌드 검증 과정에 문서화 검사 포함
    tasks.named("check") {
        dependsOn("checkAllDocs")
    }
}
}
}