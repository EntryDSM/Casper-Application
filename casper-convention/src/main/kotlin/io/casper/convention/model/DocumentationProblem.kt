package io.casper.convention.model

import io.casper.convention.util.DocMessageTemplates

/**
 * 문서화 문제를 나타내는 데이터 클래스입니다.
 * 코드 베이스에서 KDoc 주석이 누락된 요소에 대한 정보를 저장합니다.
 */
data class DocumentationProblem(
    /**
     * 문제가 발견된 코드 요소 유형
     */
    val element: CodeElement,

    /**
     * 요소의 이름 (클래스명, 함수명 등)
     */
    val elementName: String,

    /**
     * 파일의 전체 경로
     */
    val filePath: String,

    /**
     * 파일의 이름 (경로 제외)
     */
    val fileName: String,

    /**
     * 코드 요소가 선언된 줄 번호
     */
    val lineNumber: Int
) {
    /**
     * 사용자 친화적인 오류 메시지를 생성합니다.
     * 
     * @return 파일명과 줄 번호가 포함된 형식화된 오류 메시지
     */
    fun toUserFriendlyMessage(): String =
        DocMessageTemplates.USER_FRIENDLY_MESSAGE.format(fileName, lineNumber, element.helpMessage.format(elementName))
    
    /**
     * 개발자를 위한 상세 오류 메시지를 생성합니다.
     * 
     * @return 파일 경로와 줄 번호가 포함된 상세 오류 메시지
     */
    fun toDetailedMessage(): String =
        DocMessageTemplates.DETAILED_MESSAGE.format(element.friendlyName, elementName, filePath, lineNumber)
    
    /**
     * 로그 출력용 짧은 메시지를 생성합니다.
     * 
     * @return 간결한 형식의 로그 메시지
     */
    fun toLogMessage(): String =
        DocMessageTemplates.LOG_MESSAGE.format(element.friendlyName, elementName, fileName, lineNumber)
}