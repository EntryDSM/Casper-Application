import org.gradle.kotlin.dsl.register

/**
 * Casper 프로젝트의 KDoc 문서화 규칙을 정의하는 Gradle 플러그인입니다.
 * 
 * 이 플러그인은 다음 기능을 제공합니다:
 * 1. 클래스, 객체, 인터페이스, 함수 등의 코드 요소에 KDoc 주석이 있는지 검사하는 태스크
 * 2. 모든 문서화 검사를 한 번에 실행하는 태스크
 * 3. 빌드 과정에 문서화 검사를 통합
 */

/**
 * 클래스 문서화 검사 태스크
 * 모든 클래스에 KDoc 주석이 있는지 확인합니다.
 */
val classCheck = tasks.register<DocCheckTask>("checkClassDocs") {
    group = DocConstants.DOC_GROUP
    description = "클래스에 KDoc 주석이 있는지 확인합니다"
    codeElement.set(CodeElement.CLASS)
}

/**
 * 객체 문서화 검사 태스크
 * 모든 객체(object)에 KDoc 주석이 있는지 확인합니다.
 */
val objectCheck = tasks.register<DocCheckTask>("checkObjectDocs") {
    group = DocConstants.DOC_GROUP
    description = "객체에 KDoc 주석이 있는지 확인합니다"
    codeElement.set(CodeElement.OBJECT)
}

/**
 * 인터페이스 문서화 검사 태스크
 * 모든 인터페이스에 KDoc 주석이 있는지 확인합니다.
 */
val interfaceCheck = tasks.register<DocCheckTask>("checkInterfaceDocs") {
    group = DocConstants.DOC_GROUP
    description = "인터페이스에 KDoc 주석이 있는지 확인합니다"
    codeElement.set(CodeElement.INTERFACE)
}

/**
 * 함수 문서화 검사 태스크
 * 모든 공개 함수에 KDoc 주석이 있는지 확인합니다.
 */
val functionCheck = tasks.register<DocCheckTask>("checkFunctionDocs") {
    group = DocConstants.DOC_GROUP
    description = "함수에 KDoc 주석이 있는지 확인합니다"
    codeElement.set(CodeElement.FUNCTION)
}

/**
 * 모든 문서화 검사를 한 번에 실행하는 태스크
 * 모든 코드 요소(클래스, 객체, 인터페이스, 함수 등)에 KDoc 주석이 있는지 확인합니다.
 */
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

/**
 * 빌드 검증 과정에 문서화 검사 포함
 * 프로젝트 빌드 시 문서화 검사를 자동으로 실행합니다.
 */
tasks.named("check") {
    dependsOn("checkAllDocs")
}