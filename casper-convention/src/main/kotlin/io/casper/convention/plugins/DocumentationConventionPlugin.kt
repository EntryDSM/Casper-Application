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
 * 코드 요소(클래스, 함수, 프로퍼티 등)에 KDoc 주석이 있는지 확인하는 태스크를 제공합니다.
 */
class DocumentationConventionPlugin : Plugin<Project> {
    /**
     * 플러그인을 프로젝트에 적용합니다.
     * 각 코드 요소별 문서화 확인 태스크와 통합 태스크를 등록하고, 빌드 라이프사이클에 연결합니다.
     *
     * @param project 플러그인이 적용될 프로젝트
     */
    override fun apply(project: Project) {
        project.logger.lifecycle("문서화 규칙 플러그인을 적용합니다: ${project.name}")
        val registeredTasks = registerIndividualDocCheckTasks(project)
        registerAggregateTask(project, registeredTasks)
        connectToCheckLifecycle(project)
    }

    /**
     * 각 코드 요소(클래스, 함수, 프로퍼티 등)별로 개별 문서화 확인 태스크를 등록합니다.
     * DocCheckTaskType에 정의된 각 유형에 대해 DocCheckTask를 생성합니다.
     *
     * @param project 태스크가 등록될 프로젝트
     * @return 등록된 태스크 프로바이더 목록
     */
    private fun registerIndividualDocCheckTasks(project: Project): List<TaskProvider<out Task>> {
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
        return registeredTasks
    }

    /**
     * 모든 개별 문서화 확인 태스크를 한 번에 실행하는 통합 태스크를 등록합니다.
     * 이 태스크는 모든 개별 태스크에 의존하므로, 하나의 명령으로 모든 확인을 실행할 수 있습니다.
     *
     * @param project 태스크가 등록될 프로젝트
     * @param individualTasks 의존할 개별 태스크 목록
     */
    private fun registerAggregateTask(project: Project, individualTasks: List<TaskProvider<out Task>>) {
        // 모든 문서화 검사를 한 번에 실행하는 태스크
        if (project.tasks.findByName(CHECK_ALL_DOCS_TASK) == null) {
            project.tasks.register(CHECK_ALL_DOCS_TASK) {
                group = DocConstants.CHECK_GROUP
                description = "모든 코드 요소의 KDoc 주석 여부를 확인합니다"

                // 등록된 모든 검사 태스크에 의존
                dependsOn(individualTasks)
            }
            project.logger.lifecycle("${project.name}에 ${CHECK_ALL_DOCS_TASK} 태스크를 등록했습니다.")
        }
    }

    /**
     * 문서화 확인 태스크를 프로젝트의 기본 check 태스크에 연결합니다.
     * 이를 통해 일반적인 빌드 라이프사이클에 문서화 확인이 포함됩니다.
     *
     * @param project 연결될 프로젝트
     */
    private fun connectToCheckLifecycle(project: Project) {
        // 프로젝트 평가 후에 check 태스크가 있으면 checkAllDocs를 의존성으로 추가
        project.afterEvaluate {
            project.tasks.findByName("check")?.dependsOn(CHECK_ALL_DOCS_TASK)
            project.logger.lifecycle("${project.name}의 check 태스크에 ${CHECK_ALL_DOCS_TASK} 의존성을 추가했습니다.")
        }
    }

    /**
     * 플러그인 내에서 사용되는 상수 값들을 정의합니다.
     */
    companion object {
        /**
         * 모든 문서화 확인 태스크를 실행하는 통합 태스크의 이름입니다.
         */
        const val CHECK_ALL_DOCS_TASK = "checkAllDocs"
    }
}