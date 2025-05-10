package io.casper.convention.exception

import io.casper.convention.model.CodeElement
import org.gradle.api.GradleException


/**
 * 문서화 관련 예외를 처리하기 위한 예외 클래스
 * KDoc 문서화 검사 실패 시 발생
 * gradle 플러그인 모듈은 global Exception을 만들기 어려워서 따로 클래스로 만듦
 *
 * @param element 검사 대상 코드 요소
 * @param message 예외 메시지
 */
class DocumentationException(
    val element: CodeElement,
    message: String
) : GradleException(message) {
    companion object {
        /**
         * 문서화 부재 예외를 생성
         *
         * @param element 문서화가 부재한 코드 요소
         * @return 생성된 DocumentationException 인스턴스
         */
        fun missingDocumentation(element: CodeElement): DocumentationException {
            return DocumentationException(
                element = element,
                message = "일부 ${element.friendlyName}에 KDoc 주석이 없습니다. 자세한 내용은 로그를 확인하세요."
            )
        }
    }
}