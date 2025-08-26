package hs.kr.entrydsm.domain.examcode.interfaces

/**
 * 수험번호를 부여하는 기능에 대한 인터페이스입니다.
 *
 * @author chaedohun
 * @since 2025.08.26
 */
interface GrantExamCodesContract {

    /**
     * 수험번호 부여 프로세스를 실행합니다.
     */
    suspend fun execute()
}
