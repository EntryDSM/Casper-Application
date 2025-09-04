package hs.kr.entrydsm.application.global.document.pdf.config

/**
 * PDF 생성 시 사용할 폰트 설정을 관리하는 객체입니다.
 * 
 * 한글과 영문을 모두 지원하는 폰트들의 목록을 정의하며,
 * PDF 문서에서 텍스트가 올바르게 렌더링되도록 보장합니다.
 */
object Font {
    /**
     * PDF에서 사용할 폰트 파일 목록입니다.
     * 
     * 한글 지원을 위한 KoPubWorld Dotum 폰트 패밀리와
     * 영문 지원을 위한 DejaVuSans 폰트를 포함합니다.
     */
    val fonts =
        listOf(
            "KoPubWorld Dotum Light.ttf",
            "KoPubWorld Dotum Bold.ttf",
            "KoPubWorld Dotum Medium.ttf",
            "DejaVuSans.ttf",
        )
}
