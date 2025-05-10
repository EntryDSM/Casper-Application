package io.casper.convention.model

import org.gradle.internal.impldep.org.yaml.snakeyaml.TypeDescription

enum class DocCheckTaskType(

    val taskName : String,
    val description: String,
    val codeElement: CodeElement
) {

    CLASS_DOC(
        taskName = "checkClassDocs",
        description = "클래스에 KDoc 주석이 있는지 확인합니다",
        codeElement = CodeElement.CLASS
    ),
    OBJECT_DOC(
        taskName = "checkObjectDocs",
        description = "객체에 KDoc 주석이 있는지 확인합니다",
        codeElement = CodeElement.OBJECT
    ),
    INTERFACE_DOC(
        taskName = "checkInterfaceDocs",
        description = "인터페이스에 KDoc 주석이 있는지 확인합니다",
        codeElement = CodeElement.INTERFACE
    ),
    FUNCTION_DOC(
        taskName = "checkFunctionDocs",
        description = "함수에 KDoc 주석이 있는지 확인합니다",
        codeElement = CodeElement.FUNCTION
    ),
}