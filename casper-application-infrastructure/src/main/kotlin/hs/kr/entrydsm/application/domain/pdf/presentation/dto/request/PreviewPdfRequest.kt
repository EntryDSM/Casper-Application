package hs.kr.entrydsm.application.domain.pdf.presentation.dto.request

/**
 * PDF 미리보기 요청 DTO
 * 
 * 프론트에서 IndexedDB에 저장된 임시 데이터를 전달받아
 * 미리보기 PDF를 생성하기 위한 요청 객체입니다.
 */
data class PreviewPdfRequest(
    /**
     * 원서 정보
     * 
     * 지원자의 개인정보, 전형유형, 학력상태, 학교정보, 
     * 자기소개서, 학업계획서 등이 포함됩니다.
     */
    val application: Map<String, Any>,
    
    /**
     * 성적 정보
     * 
     * 중학교 성적, 출석, 봉사활동 등의 정보가
     * Key-Value 형태로 포함됩니다.
     */
    val scores: Map<String, Any>
)
