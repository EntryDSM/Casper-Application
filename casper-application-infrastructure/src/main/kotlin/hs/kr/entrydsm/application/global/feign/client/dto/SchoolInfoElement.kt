package hs.kr.entrydsm.application.global.feign.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 나이스 API 학교 정보 응답을 위한 데이터 클래스 입니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SchoolInfoElement(
    val schoolInfo: List<SchoolInfo>?,
)

/**
 * 학교 정보를 담는 데이터 클래스 입니다.
 *
 * @property head 헤더 정보
 * @property row 학교 정보 리스트
 */
data class SchoolInfo(
    val head: List<Head>?,
    val row: List<Row>?,
)

/**
 * 나이스 API 응답의 헤더 정보를 담는 데이터 클래스 입니다.
 *
 * @property listTotalCount 총 아이템 수
 * @property result 응답 결과
 */
data class Head(
    @JsonProperty("list_total_count")
    val listTotalCount: Long?,
    @JsonProperty("RESULT")
    val result: Result?,
)

/**
 * 나이스 API 응답 결과를 담는 데이터 클래스 입니다.
 *
 * @property code 응답 코드
 * @property message 응답 메시지
 */
data class Result(
    @JsonProperty("CODE")
    val code: String,
    @JsonProperty("MESSAGE")
    val message: String,
)

/**
 * 나이스 API 학교 정보 상세를 담는 데이터 클래스 입니다.
 *
 * @property atptOfcdcScCode 시도교육청코드
 * @property atptOfcdcScNm 시도교육청명
 * @property sdSchulCode 표준학교코드
 * @property schulNm 학교명
 * @property engSchulNm 영문학교명
 * @property schulKndScNm 학교종류명
 * @property lctnScNm 소재지명
 * @property juOrgNm 관할조직명
 * @property fondScNm 설립명
 * @property orgRdnzc 도로명우편번호
 * @property orgRdnma 도로명주소
 * @property orgRdnda 도로명상세주소
 * @property orgTelno 전화번호
 * @property hmpgAdres 홈페이지주소
 * @property coeduScNm 남녀공학구분명
 * @property orgFaxno 팩с번호
 * @property hsScNm 고등학교구분명
 * @property indstSpeclCccclExstYn 산업체특별학급존재여부
 * @property hsGnrlBusnsScNm 고등학교일반실업구분명
 * @property spclyPurpsHsOrdNm 특수목적고등학교계열명
 * @property eneBfeSehfScNm 입학전형구분명
 * @property dghtScNm 주야구분명
 * @property fondYmd 설립일자
 * @property foasMemrd 개교기념일
 * @property loadDtm 수정일
 */
data class Row(
    @JsonProperty("ATPT_OFCDC_SC_CODE")
    val atptOfcdcScCode: String,
    @JsonProperty("ATPT_OFCDC_SC_NM")
    val atptOfcdcScNm: String,
    @JsonProperty("SD_SCHUL_CODE")
    val sdSchulCode: String,
    @JsonProperty("SCHUL_NM")
    val schulNm: String,
    @JsonProperty("ENG_SCHUL_NM")
    val engSchulNm: String? = null,
    @JsonProperty("SCHUL_KND_SC_NM")
    val schulKndScNm: String,
    @JsonProperty("LCTN_SC_NM")
    val lctnScNm: String,
    @JsonProperty("JU_ORG_NM")
    val juOrgNm: String,
    @JsonProperty("FOND_SC_NM")
    val fondScNm: String,
    @JsonProperty("ORG_RDNZC")
    val orgRdnzc: String,
    @JsonProperty("ORG_RDNMA")
    val orgRdnma: String,
    @JsonProperty("ORG_RDNDA")
    val orgRdnda: String,
    @JsonProperty("ORG_TELNO")
    val orgTelno: String,
    @JsonProperty("HMPG_ADRES")
    val hmpgAdres: String,
    @JsonProperty("COEDU_SC_NM")
    val coeduScNm: String,
    @JsonProperty("ORG_FAXNO")
    val orgFaxno: String? = null,
    @JsonProperty("HS_SC_NM")
    val hsScNm: Any?,
    @JsonProperty("INDST_SPECL_CCCCL_EXST_YN")
    val indstSpeclCccclExstYn: String,
    @JsonProperty("HS_GNRL_BUSNS_SC_NM")
    val hsGnrlBusnsScNm: Any?,
    @JsonProperty("SPCLY_PURPS_HS_ORD_NM")
    val spclyPurpsHsOrdNm: Any?,
    @JsonProperty("ENE_BFE_SEHF_SC_NM")
    val eneBfeSehfScNm: String,
    @JsonProperty("DGHT_SC_NM")
    val dghtScNm: String,
    @JsonProperty("FOND_YMD")
    val fondYmd: String,
    @JsonProperty("FOAS_MEMRD")
    val foasMemrd: String,
    @JsonProperty("LOAD_DTM")
    val loadDtm: String,
)
