package hs.kr.entrydsm.domain.examcode.interfaces

/**
 * 거리 계산의 기준이 되는 위치 정보에 대한 인터페이스입니다.
 *
 * @author chaedohun
 * @since 2025.08.26
 */
interface BaseLocationUseCase {
    /**
     * 기준 위도
     */
    val baseLat: Double

    /**
     * 기준 경도
     */
    val baseLon: Double
}
