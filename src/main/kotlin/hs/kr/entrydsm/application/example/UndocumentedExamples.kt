package hs.kr.entrydsm.application.example

/**
 * 문서화되지 않은 요소들을 보여주는 예제 클래스입니다.
 * 이 클래스는 KDoc 주석 검사 시스템을 테스트하기 위해 사용됩니다.
 */
class UndocumentedExamples {
    /**
     * 이전에 문서화되지 않았던 함수의 예시입니다.
     * 간단한 문자열을 반환합니다.
     *
     * @return "This function has no KDoc" 문자열
     */
    fun undocumentedFunction(): String {
        return "This function has no KDoc"
    }

    /**
     * 매개변수가 있는 함수 예시입니다.
     * 문자열의 길이를 반환합니다.
     *
     * @param param 길이를 계산할 문자열
     * @return 문자열의 길이
     */
    fun anotherUndocumentedFunction(param: String): Int {
        return param.length
    }

    /**
     * 비공개 헬퍼 함수입니다.
     * 콘솔에 메시지를 출력합니다.
     */
    private fun privateUndocumentedFunction() {
        println("Private function without KDoc")
    }
}

/**
 * 유틸리티 기능을 제공하는 싱글톤 객체입니다.
 * 이 객체는 정적 메소드와 속성을 그룹화합니다.
 */
object UndocumentedObject {
    /**
     * 객체의 예시 속성입니다.
     */
    val someProperty = "No KDoc here"

    /**
     * 객체 내의 유틸리티 함수입니다.
     * 간단한 메시지를 콘솔에 출력합니다.
     */
    fun objectFunction() {
        println("Function in undocumented object")
    }
}


interface UndocumentedInterface {
    /**
     * 인터페이스에서 정의된 메소드입니다.
     * 구현 클래스에서 이 메소드를 구현해야 합니다.
     */
    fun interfaceMethod()

    /**
     * 인터페이스에서 정의된 속성입니다.
     * 구현 클래스에서 이 속성에 대한 접근자를 제공해야 합니다.
     */
    val interfaceProperty: String
}
