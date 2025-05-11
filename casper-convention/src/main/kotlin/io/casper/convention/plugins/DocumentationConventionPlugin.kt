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
        project.logger.lifecycle("문서화 규칙 플러그인을 적용합니다: ${project.name}")
        registerDocCheckTasks(project)
    }


    private fun registerDocCheckTasks(project: Project) {
        val registeredTasks = mutableListOf<TaskProvider<out Task>>()

        DocCheckTaskType.values().forEach { taskType ->
            val taskName = taskType.taskName
            
            // 이미 같은 이름의 태스크가 있는지 확인
            if (project.tasks.findByName(taskName) == null) {
                val task = project.tasks.register<DocCheckTask>(taskName) {
                    group = DocConstants.DOC_GROUP
                    description = taskType.description
                    codeElement.set(taskType.codeElement)
                }
                registeredTasks.add(task)
                project.logger.lifecycle("${project.name}에 ${taskName} 태스크를 등록했습니다.")
            } else {
                project.logger.lifecycle("${project.name}에 이미 ${taskName} 태스크가 있습니다.")
                registeredTasks.add(project.tasks.named(taskName))
            }
        }

        // 모든 문서화 검사를 한 번에 실행하는 태스크
        if (project.tasks.findByName(CHECK_ALL_DOCS_TASK) == null) {
            project.tasks.register(CHECK_ALL_DOCS_TASK) {
                group = DocConstants.CHECK_GROUP
                description = "모든 코드 요소의 KDoc 주석 여부를 확인합니다"
                
                // 등록된 모든 검사 태스크에 의존
                dependsOn(registeredTasks)
            }
            project.logger.lifecycle("${project.name}에 ${CHECK_ALL_DOCS_TASK} 태스크를 등록했습니다.")
        }
        
        // 프로젝트 평가 후에 check 태스크가 있으면 checkAllDocs를 의존성으로 추가
        project.afterEvaluate {
            project.tasks.findByName("check")?.dependsOn(CHECK_ALL_DOCS_TASK)
            project.logger.lifecycle("${project.name}의 check 태스크에 ${CHECK_ALL_DOCS_TASK} 의존성을 추가했습니다.")
        }

    }
    companion object {
        const val CHECK_ALL_DOCS_TASK = "checkAllDocs"
    }
}
