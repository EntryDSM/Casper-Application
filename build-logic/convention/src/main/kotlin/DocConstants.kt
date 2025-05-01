/**
 * KDoc 문서화 기능을 위한 상수들을 정의하는 객체입니다.
 */
object DocConstants {
    /**
     * 문서화 태스크 그룹 이름
     */
    const val DOC_GROUP = "documentation"
    
    /**
     * 검증 태스크 그룹 이름
     */
    const val CHECK_GROUP = "verification"
    
    /**
     * 소스 파일 검색 기본 경로
     */
    const val SRC_FOLDER = "src/"
    
    /**
     * Kotlin 파일 검색 패턴
     */
    const val KOTLIN_FILES = "**/*.kt"
    
    /**
     * KDoc 주석 종료 문자열
     */
    const val KDOC_END = "*/"
}
