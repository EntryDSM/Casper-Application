package hs.kr.entrydsm.application.example

import org.springframework.stereotype.Service

/**
 * KDoc 주석 검사를 테스트하기 위한 예제 서비스 클래스입니다.
 * 이 클래스는 다양한 함수 타입과 주석 패턴을 보여주기 위한 목적으로 작성되었습니다.
 *
 * @author EntryDSM
 * @since 1.0.0
 */
@Service
class DocumentationExampleService {
    /**
     * 매개변수 없는 공개 함수의 예제입니다.
     * 간단한 문자열을 반환합니다.
     *
     * @return 표준 인사말 문자열
     */
    fun publicFunction(): String {
        return "Hello from public function"
    }

    /**
     * 매개변수가 있는 공개 함수 예제입니다.
     * 입력받은 이름으로 개인화된 인사말을 생성하여 반환합니다.
     *
     * @param name 인사할 대상의 이름
     * @return 이름을 포함한 개인화된 인사말
     * @throws IllegalArgumentException 이름이 빈 문자열인 경우
     */
    fun publicFunctionWithParam(name: String): String {
        return "Hello, $name!"
    }

    /**
     * 문자열을 대문자로 변환하는 비공개 헬퍼 함수입니다.
     * 내부적으로만 사용되며 주로 [publicFunctionWithParam]에서 호출됩니다.
     *
     * @param text 대문자로 변환할 문자열
     * @return 대문자로 변환된 문자열
     * @see publicFunctionWithParam
     */
    private fun privateFunction(text: String): String {
        return text.uppercase()
    }

    /**
     * 주어진 문자열을 지정된 횟수만큼 중복시키는 내부 유틸리티 함수입니다.
     * 텍스트 패턴을 반복해야 할 때 사용합니다.
     *
     * @param text 중복할 문자열
     * @param times 중복 횟수 (음수인 경우 빈 문자열 반환)
     * @return 지정된 횟수만큼 중복된 문자열
     * @sample
     * duplicateText("Hello", 3) // 결과: "HelloHelloHello"
     */
    private fun duplicateText(
        text: String,
        times: Int,
    ): String {
        return text.repeat(times)
    }
}

/**
 * 사용자 정보를 저장하기 위한 데이터 클래스입니다.
 * API 응답 및 서비스 간 데이터 전송에 사용됩니다.
 *
 * @property id 사용자의 고유 식별자
 * @property name 사용자의 실명 또는 닉네임
 * @property email 사용자의 이메일 주소 (통신 용도)
 * @constructor 모든 필수 속성을 가진 사용자 DTO 인스턴스를 생성합니다
 */
data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
)

/**
 * 문자열 조작을 위한 유틸리티 함수를 제공하는 오브젝트입니다.
 * 애플리케이션 전반에서 사용되는 문자열 관련 공통 기능을 중앙화합니다.
 *
 * @since 1.0.0
 */
object StringUtils {
    /**
     * 문자열이 비어 있거나 null인지 확인합니다.
     * 유효성 검사에 유용하게 사용됩니다.
     *
     * @param text 확인할 문자열 (null 가능)
     * @return 문자열이 null이거나 빈 문자열이면 true, 그렇지 않으면 false
     */
    fun isEmpty(text: String?): Boolean {
        return text.isNullOrEmpty()
    }

    /**
     * 문자열의 좌우 공백을 제거합니다.
     * 사용자 입력 정제에 유용합니다.
     *
     * @param text 처리할 문자열
     * @return 좌우 공백이 제거된 문자열
     * @throws NullPointerException text가 null인 경우
     */
    fun trim(text: String): String {
        return text.trim()
    }
}
