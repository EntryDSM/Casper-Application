package hs.kr.entrydsm.application.domain.school.domain.presentation

import hs.kr.entrydsm.application.global.feign.client.SchoolClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test/schools")
class TestSchoolController(
    private val schoolClient: SchoolClient,
) {
    @Value("\${neis.key}")
    lateinit var apiKey: String

    @GetMapping("/code")
    fun getSchoolByCode(
        @RequestParam("school_code") schoolCode: String,
    ): String? {
        return schoolClient.getSchoolBySchoolCode(schoolCode = schoolCode, key = apiKey)
    }

    @GetMapping("/name")
    fun getSchoolByName(
        @RequestParam("school_name") schoolName: String,
    ): String? {
        return schoolClient.getSchoolListBySchoolName(schoolName = schoolName, key = apiKey)
    }
}
