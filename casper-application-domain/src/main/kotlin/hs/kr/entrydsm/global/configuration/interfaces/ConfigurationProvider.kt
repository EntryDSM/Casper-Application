package hs.kr.entrydsm.global.configuration.interfaces

import hs.kr.entrydsm.global.configuration.*

/**
 * 시스템 설정을 제공하는 인터페이스입니다.
 *
 * Infrastructure 계층에서 구현하여 런타임에 설정값을 수정할 수 있게 합니다.
 * 다양한 소스(파일, 환경변수, 데이터베이스 등)에서 설정을 읽어올 수 있습니다.
 *
 * @author kangeunchan
 * @since 2025.08.07
 */
interface ConfigurationProvider {
    
    /**
     * 파서 설정을 반환합니다.
     */
    fun getParserConfiguration(): ParserConfiguration
    
    /**
     * 계산기 설정을 반환합니다.
     */
    fun getCalculatorConfiguration(): CalculatorConfiguration
    
    /**
     * 렉서 설정을 반환합니다.
     */
    fun getLexerConfiguration(): LexerConfiguration
    
    /**
     * AST 설정을 반환합니다.
     */
    fun getASTConfiguration(): ASTConfiguration
    
    /**
     * 표현식 처리 설정을 반환합니다.
     */
    fun getExpresserConfiguration(): ExpresserConfiguration
    
    /**
     * 평가기 설정을 반환합니다.
     */
    fun getEvaluatorConfiguration(): EvaluatorConfiguration
    
    /**
     * 특정 설정을 업데이트합니다.
     */
    fun updateParserConfiguration(configuration: ParserConfiguration)
    fun updateCalculatorConfiguration(configuration: CalculatorConfiguration)
    fun updateLexerConfiguration(configuration: LexerConfiguration)
    fun updateASTConfiguration(configuration: ASTConfiguration)
    fun updateExpresserConfiguration(configuration: ExpresserConfiguration)
    fun updateEvaluatorConfiguration(configuration: EvaluatorConfiguration)
    
    /**
     * 모든 설정을 기본값으로 초기화합니다.
     */
    fun resetToDefaults()
    
    /**
     * 설정 변경 사항을 저장합니다.
     */
    fun saveConfiguration()
    
    /**
     * 설정 변경을 감지하는 리스너를 등록합니다.
     */
    fun addConfigurationChangeListener(listener: ConfigurationChangeListener)
    
    /**
     * 설정 변경 리스너를 제거합니다.
     */
    fun removeConfigurationChangeListener(listener: ConfigurationChangeListener)
    
    /**
     * 현재 설정 상태를 검증합니다.
     */
    fun validateConfiguration(): Map<String, List<String>>
    
    /**
     * 설정의 메타데이터를 반환합니다.
     */
    fun getConfigurationMetadata(): Map<String, Any>
}