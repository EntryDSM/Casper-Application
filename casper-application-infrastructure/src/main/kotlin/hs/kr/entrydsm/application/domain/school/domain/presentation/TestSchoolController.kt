package hs.kr.entrydsm.application.domain.school.domain.presentation

import hs.kr.entrydsm.application.global.feign.client.SchoolClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * School API 테스트를 위한 컨트롤러입니다.
 */
@RestController
@RequestMapping("/test/schools")
class TestSchoolController(
    private val schoolClient: SchoolClient,
) {
    @Value("\${neis.key}")
    lateinit var apiKey: String

    /**
     * 학교 코드로 학교 정보를 조회합니다.
     *
     * @param schoolCode 학교 코드
     * @return 학교 정보
     */
    @GetMapping("/code")
    fun getSchoolByCode(
        @RequestParam("school_code") schoolCode: String,
    ): String? {
        return schoolClient.getSchoolBySchoolCode(schoolCode = schoolCode, key = apiKey)
    }

    /**
     * 학교 이름으로 학교 리스트를 조회합니다.
     *
     * @param schoolName 학교 이름
     * @return 학교 리스트
     */
    @GetMapping("/name")
    fun getSchoolByName(
        @RequestParam("school_name") schoolName: String,
    ): String? {
        return schoolClient.getSchoolListBySchoolName(schoolName = schoolName, key = apiKey)
    }
}
