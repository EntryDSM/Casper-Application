package hs.kr.entrydsm.domain.expresser.interfaces

import hs.kr.entrydsm.domain.ast.entities.ASTNode
import hs.kr.entrydsm.domain.expresser.entities.FormattingOptions
import hs.kr.entrydsm.domain.expresser.values.FormattedExpression

/**
 * 표현식 출력자의 핵심 계약을 정의하는 인터페이스입니다.
 *
 * Anti-Corruption Layer 역할을 수행하여 다양한 표현식 출력 구현체들 간의
 * 호환성을 보장하며, 표현식 형식화와 출력의 핵심 기능을 표준화된 방식으로
 * 제공합니다. DDD 인터페이스 패턴을 적용하여 구현체와 클라이언트 간의
 * 결합도를 낮춥니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
interface ExpresserContract {

    /**
     * AST를 형식화된 표현식으로 변환합니다.
     *
     * @param ast 변환할 AST 노드
     * @return 형식화된 표현식
     */
    fun format(ast: ASTNode): FormattedExpression

    /**
     * AST를 특정 옵션으로 형식화합니다.
     *
     * @param ast 변환할 AST 노드
     * @param options 형식화 옵션
     * @return 형식화된 표현식
     */
    fun format(ast: ASTNode, options: FormattingOptions): FormattedExpression

    /**
     * 표현식 문자열을 재형식화합니다.
     *
     * @param expression 원본 표현식
     * @return 형식화된 표현식
     */
    fun reformat(expression: String): FormattedExpression

    /**
     * 표현식 문자열을 특정 옵션으로 재형식화합니다.
     *
     * @param expression 원본 표현식
     * @param options 형식화 옵션
     * @return 형식화된 표현식
     */
    fun reformat(expression: String, options: FormattingOptions): FormattedExpression

    /**
     * AST를 특정 형식으로 출력합니다.
     *
     * @param ast 출력할 AST 노드
     * @param format 출력 형식 ("infix", "prefix", "postfix", "latex", "mathml" 등)
     * @return 형식화된 표현식
     */
    fun express(ast: ASTNode, format: String): FormattedExpression

    /**
     * 표현식을 특정 형식으로 변환합니다.
     *
     * @param expression 원본 표현식
     * @param sourceFormat 원본 형식
     * @param targetFormat 목표 형식
     * @return 변환된 표현식
     */
    fun convert(expression: String, sourceFormat: String, targetFormat: String): FormattedExpression

    /**
     * 표현식을 수학 표기법으로 변환합니다.
     *
     * @param ast 변환할 AST 노드
     * @return 수학 표기법 표현식
     */
    fun toMathematicalNotation(ast: ASTNode): FormattedExpression

    /**
     * 표현식을 LaTeX 형식으로 변환합니다.
     *
     * @param ast 변환할 AST 노드
     * @return LaTeX 형식 표현식
     */
    fun toLaTeX(ast: ASTNode): FormattedExpression

    /**
     * 표현식을 MathML 형식으로 변환합니다.
     *
     * @param ast 변환할 AST 노드
     * @return MathML 형식 표현식
     */
    fun toMathML(ast: ASTNode): FormattedExpression

    /**
     * 표현식을 HTML 형식으로 변환합니다.
     *
     * @param ast 변환할 AST 노드
     * @return HTML 형식 표현식
     */
    fun toHTML(ast: ASTNode): FormattedExpression

    /**
     * 표현식을 JSON 형식으로 변환합니다.
     *
     * @param ast 변환할 AST 노드
     * @return JSON 형식 표현식
     */
    fun toJSON(ast: ASTNode): FormattedExpression

    /**
     * 표현식을 XML 형식으로 변환합니다.
     *
     * @param ast 변환할 AST 노드
     * @return XML 형식 표현식
     */
    fun toXML(ast: ASTNode): FormattedExpression

    /**
     * 표현식의 가독성을 향상시킵니다.
     *
     * @param expression 원본 표현식
     * @return 가독성이 향상된 표현식
     */
    fun beautify(expression: String): FormattedExpression

    /**
     * 표현식을 압축합니다 (공백 제거 등).
     *
     * @param expression 원본 표현식
     * @return 압축된 표현식
     */
    fun minify(expression: String): FormattedExpression

    /**
     * 표현식에 구문 강조를 적용합니다.
     *
     * @param expression 원본 표현식
     * @param scheme 색상 스키마
     * @return 구문 강조된 표현식
     */
    fun highlight(expression: String, scheme: String = "default"): FormattedExpression

    /**
     * 표현식의 복잡한 부분을 시각적으로 강조합니다.
     *
     * @param ast 분석할 AST 노드
     * @return 복잡도가 시각화된 표현식
     */
    fun visualizeComplexity(ast: ASTNode): FormattedExpression

    /**
     * 표현식의 실행 순서를 시각적으로 표시합니다.
     *
     * @param ast 분석할 AST 노드
     * @return 실행 순서가 표시된 표현식
     */
    fun visualizeEvaluationOrder(ast: ASTNode): FormattedExpression

    /**
     * 표현식을 단계별로 분해하여 표시합니다.
     *
     * @param ast 분해할 AST 노드
     * @return 단계별 분해 결과
     */
    fun breakdownSteps(ast: ASTNode): List<FormattedExpression>

    /**
     * 지원되는 출력 형식 목록을 반환합니다.
     *
     * @return 지원되는 형식들
     */
    fun getSupportedFormats(): Set<String>

    /**
     * 지원되는 색상 스키마 목록을 반환합니다.
     *
     * @return 지원되는 색상 스키마들
     */
    fun getSupportedColorSchemes(): Set<String>

    /**
     * 형식화 옵션을 검증합니다.
     *
     * @param options 검증할 옵션
     * @return 유효하면 true
     */
    fun validateOptions(options: FormattingOptions): Boolean

    /**
     * 특정 형식이 지원되는지 확인합니다.
     *
     * @param format 확인할 형식
     * @return 지원되면 true
     */
    fun supportsFormat(format: String): Boolean

    /**
     * 표현식의 예상 출력 크기를 추정합니다.
     *
     * @param ast 분석할 AST 노드
     * @param format 출력 형식
     * @return 예상 크기 (문자 수)
     */
    fun estimateOutputSize(ast: ASTNode, format: String): Int

    /**
     * 출력자의 설정 정보를 반환합니다.
     *
     * @return 설정 정보 맵
     */
    fun getConfiguration(): Map<String, Any>

    /**
     * 출력자의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any>

    /**
     * 출력자를 초기화합니다.
     */
    fun reset()

    /**
     * 출력자가 활성 상태인지 확인합니다.
     *
     * @return 활성 상태이면 true
     */
    fun isActive(): Boolean

    /**
     * 캐시를 관리합니다.
     *
     * @param enable 캐시 활성화 여부
     */
    fun setCachingEnabled(enable: Boolean)

    /**
     * 출력 품질 수준을 설정합니다.
     *
     * @param level 품질 수준 ("low", "medium", "high")
     */
    fun setQualityLevel(level: String)

    /**
     * 성능 최적화 모드를 설정합니다.
     *
     * @param enabled 최적화 활성화 여부
     */
    fun setOptimizationEnabled(enabled: Boolean)

    /**
     * 다국어 지원을 위한 로케일을 설정합니다.
     *
     * @param locale 로케일 (예: "ko", "en", "ja")
     */
    fun setLocale(locale: String)

    /**
     * 수식 렌더링을 위한 폰트를 설정합니다.
     *
     * @param fontFamily 폰트 패밀리
     * @param fontSize 폰트 크기
     */
    fun setFont(fontFamily: String, fontSize: Int)

    /**
     * 출력 스타일 테마를 설정합니다.
     *
     * @param theme 테마 이름
     */
    fun setTheme(theme: String)
}