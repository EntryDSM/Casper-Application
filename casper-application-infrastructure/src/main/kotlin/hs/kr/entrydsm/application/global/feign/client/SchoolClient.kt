package hs.kr.entrydsm.application.global.feign.client

import hs.kr.entrydsm.application.global.feign.FeignConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * 나이스 교육정보 개방 포털 API SchoolClient 입니다.
 */
@FeignClient(name = "SchoolClient", url = "\${url.school}", configuration = [FeignConfig::class])
interface SchoolClient {
    /**
     * 학교 코드로 학교 정보를 조회합니다.
     *
     * @param schoolCode 학교 코드
     * @param key API KEY
     * @param type 응답 타입
     * @param pageIndex 페이지 인덱스
     * @param pageSize 페이지 사이즈
     * @param schoolKind 학교 종류
     * @return 학교 정보
     */
    @GetMapping
    fun getSchoolBySchoolCode(
        @RequestParam("SD_SCHUL_CODE") schoolCode: String,
        @RequestParam("KEY") key: String,
        @RequestParam("Type") type: String = "json",
        @RequestParam("pIndex") pageIndex: Int = 1,
        @RequestParam("pSize") pageSize: Int = 100,
        @RequestParam("SCHUL_KND_SC_NM") schoolKind: String = "중학교",
    ): String?

    /**
     * 학교 이름으로 학교 리스트를 조회합니다.
     *
     * @param schoolName 학교 이름
     * @param key API KEY
     * @param type 응답 타입
     * @param pageIndex 페이지 인덱스
     * @param pageSize 페이지 사이즈
     * @param schoolKind 학교 종류
     * @return 학교 리스트
     */
    @GetMapping
    fun getSchoolListBySchoolName(
        @RequestParam("SCHUL_NM") schoolName: String,
        @RequestParam("KEY") key: String,
        @RequestParam("Type") type: String = "json",
        @RequestParam("pIndex") pageIndex: Int = 1,
        @RequestParam("pSize") pageSize: Int = 100,
        @RequestParam("SCHUL_KND_SC_NM") schoolKind: String = "중학교",
    ): String?
}
