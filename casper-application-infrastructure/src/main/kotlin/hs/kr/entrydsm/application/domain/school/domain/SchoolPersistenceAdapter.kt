package hs.kr.entrydsm.application.domain.school.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import hs.kr.entrydsm.application.domain.school.domain.entity.SchoolCacheRedisEntity
import hs.kr.entrydsm.application.domain.school.domain.repository.SchoolCacheRepository
import hs.kr.entrydsm.application.global.feign.client.SchoolClient
import hs.kr.entrydsm.application.global.feign.client.dto.SchoolInfoElement
import hs.kr.entrydsm.domain.school.aggregate.School
import hs.kr.entrydsm.domain.school.interfaces.SchoolContract
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SchoolPersistenceAdapter(
    private val schoolClient: SchoolClient,
    private val schoolCacheRepository: SchoolCacheRepository,
) : SchoolContract {
    @Value("\${neis.key}")
    lateinit var apiKey: String

    override fun querySchoolBySchoolCode(school: String): School? {
        if (schoolCacheRepository.existsById(school)) {
            val schoolCache = schoolCacheRepository.findById(school).get()
            schoolCache.run {
                return School(
                    code = code,
                    name = name,
                    tel = tel,
                    type = type,
                    address = address,
                    regionName = regionName,
                )
            }
        }

        val school =
            schoolClient.getSchoolBySchoolCode(schoolCode = school, key = apiKey)?.let { response ->
                val mapper = ObjectMapper().registerKotlinModule()
                val responseObject = mapper.readValue<SchoolInfoElement>(response)
                responseObject.schoolInfo?.getOrNull(1)?.row?.map {
                    School(
                        code = it.sdSchulCode,
                        name = it.schulNm,
                        tel = it.orgTelno,
                        type = it.schulKndScNm,
                        address = it.orgRdnma,
                        regionName = it.lctnScNm,
                    )
                }?.firstOrNull()
            }
        return school?.let { saveInCache(it) }
    }

    override fun querySchoolListBySchoolName(schoolName: String): List<School> {
        return schoolClient.getSchoolListBySchoolName(schoolName = schoolName, key = apiKey)?.let { response ->
            val mapper = ObjectMapper().registerKotlinModule()
            val responseObject = mapper.readValue<SchoolInfoElement>(response)
            responseObject.schoolInfo?.getOrNull(1)?.row?.map {
                School(
                    code = it.sdSchulCode,
                    name = it.schulNm,
                    tel = it.orgTelno,
                    type = it.schulKndScNm,
                    address = it.orgRdnma,
                    regionName = it.lctnScNm,
                )
            }
        } ?: emptyList()
    }

    private fun saveInCache(school: School): School {
        val schoolCache =
            SchoolCacheRedisEntity(
                code = school.code,
                name = school.name,
                tel = school.tel,
                type = school.type,
                address = school.address,
                regionName = school.regionName,
            )

        schoolCacheRepository.save(schoolCache)
        return school
    }
}
