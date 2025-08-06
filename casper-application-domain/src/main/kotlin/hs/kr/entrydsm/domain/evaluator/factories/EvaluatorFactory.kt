package hs.kr.entrydsm.domain.evaluator.factories

import hs.kr.entrydsm.domain.evaluator.aggregates.ExpressionEvaluator
import hs.kr.entrydsm.domain.evaluator.services.MathFunctionService
import hs.kr.entrydsm.domain.evaluator.values.VariableBinding
import hs.kr.entrydsm.domain.factories.EnvironmentFactory
import hs.kr.entrydsm.global.annotation.factory.Factory
import hs.kr.entrydsm.global.annotation.factory.type.Complexity

/**
 * Evaluator 도메인 객체들을 생성하는 팩토리입니다.
 *
 * 평가기와 관련된 객체들을 생성하며, 도메인 규칙과 정책을 
 * 적용하여 일관된 객체 생성을 보장합니다.
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.16
 */
@Factory(context = "evaluator", complexity = Complexity.NORMAL, cache = true)
class EvaluatorFactory {
    
    private val mathFunctionService = MathFunctionService()
    
    /**
     * 빈 변수 바인딩으로 평가기를 생성합니다.
     */
    fun createEvaluator(): ExpressionEvaluator {
        return ExpressionEvaluator(
            variables = emptyMap()
        )
    }
    
    /**
     * 변수 바인딩 맵으로 평가기를 생성합니다.
     */
    fun createEvaluator(variables: Map<String, Any?>): ExpressionEvaluator {
        return ExpressionEvaluator(
            variables = variables.filterValues { it != null }.mapValues { it.value!! }
        )
    }
    
    /**
     * 변수 바인딩 리스트로 평가기를 생성합니다.
     */
    fun createEvaluator(bindings: List<VariableBinding>): ExpressionEvaluator {
        val variableMap = bindings.associate { it.name to it.value }
        return ExpressionEvaluator(
            variables = variableMap.filterValues { it != null }.mapValues { it.value!! }
        )
    }
    
    /**
     * 수학 상수가 포함된 평가기를 생성합니다.
     */
    fun createEvaluatorWithMathConstants(): ExpressionEvaluator {
        val mathConstants = VariableBinding.getMathConstants()
        return createEvaluator(mathConstants)
    }
    
    /**
     * 수학 상수와 사용자 변수가 포함된 평가기를 생성합니다.
     */
    fun createEvaluatorWithMathConstants(userVariables: Map<String, Any?>): ExpressionEvaluator {
        val mathConstants = VariableBinding.getMathConstants()
        val allVariables = mutableMapOf<String, Any?>()
        
        // 수학 상수 추가
        mathConstants.forEach { binding ->
            allVariables[binding.name] = binding.value
        }
        
        // 사용자 변수 추가 (덮어쓰기)
        allVariables.putAll(userVariables)
        
        return createEvaluator(allVariables)
    }
    
    /**
     * 변수 바인딩을 생성합니다.
     */
    fun createVariableBinding(name: String, value: Any?, isReadonly: Boolean = false): VariableBinding {
        return VariableBinding.of(name, value, isReadonly)
    }
    
    /**
     * 숫자 변수 바인딩을 생성합니다.
     */
    fun createNumberBinding(name: String, value: Double, isReadonly: Boolean = false): VariableBinding {
        return VariableBinding.ofNumber(name, value, isReadonly)
    }
    
    /**
     * 불리언 변수 바인딩을 생성합니다.
     */
    fun createBooleanBinding(name: String, value: Boolean, isReadonly: Boolean = false): VariableBinding {
        return VariableBinding.ofBoolean(name, value, isReadonly)
    }
    
    /**
     * 문자열 변수 바인딩을 생성합니다.
     */
    fun createStringBinding(name: String, value: String, isReadonly: Boolean = false): VariableBinding {
        return VariableBinding.ofString(name, value, isReadonly)
    }
    
    /**
     * 읽기 전용 변수 바인딩을 생성합니다.
     */
    fun createReadonlyBinding(name: String, value: Any?): VariableBinding {
        return VariableBinding.readonly(name, value)
    }
    
    /**
     * 상수 바인딩을 생성합니다.
     */
    fun createConstantBinding(name: String, value: Any?): VariableBinding {
        return VariableBinding.constant(name, value)
    }
    
    /**
     * 값 맵에서 변수 바인딩 리스트를 생성합니다.
     */
    fun createBindingsFromMap(valueMap: Map<String, Any?>): List<VariableBinding> {
        return VariableBinding.fromValueMap(valueMap)
    }
    
    /**
     * 수학 함수 서비스를 생성합니다.
     */
    fun createMathFunctionService(): MathFunctionService {
        return MathFunctionService()
    }
    
    /**
     * 기본 환경 변수들을 생성합니다.
     */
    fun createDefaultEnvironment(): Map<String, Any?> {
        return EnvironmentFactory.createBasicEnvironment().mapValues { it.value }
    }
    
    /**
     * POC 코드의 CalculatorService와 유사한 기능을 가진 평가기를 생성합니다.
     */
    fun createCalculatorServiceStyleEvaluator(
        maxFormulaLength: Int = 5000,
        enableOptimization: Boolean = true
    ): ExpressionEvaluator {
        val variables = mutableMapOf<String, Any?>()
        
        // POC의 기본 수학 상수들 추가
        variables.putAll(createDefaultEnvironment())
        
        // 추가 수학 함수 상수들
        variables["ABS"] = "function"
        variables["SQRT"] = "function"
        variables["POW"] = "function"
        variables["LOG"] = "function"
        variables["LOG10"] = "function"
        variables["EXP"] = "function"
        variables["SIN"] = "function"
        variables["COS"] = "function"
        variables["TAN"] = "function"
        
        return ExpressionEvaluator(
            variables = variables.filterValues { it != null }.mapValues { it.value!! }
        )
    }
    
    /**
     * POC 코드의 RealLRParser와 함께 사용할 평가기를 생성합니다.
     */
    fun createLRParserCompatibleEvaluator(
        variables: Map<String, Any?> = emptyMap()
    ): ExpressionEvaluator {
        val allVariables = mutableMapOf<String, Any?>()
        
        // POC의 기본 변수들
        allVariables.putAll(createDefaultEnvironment())
        allVariables.putAll(variables)
        
        return ExpressionEvaluator(
            variables = allVariables.filterValues { it != null }.mapValues { it.value!! }
        )
    }
    
    /**
     * 과학 계산용 환경 변수들을 생성합니다.
     */
    fun createScientificEnvironment(): Map<String, Any?> {
        return EnvironmentFactory.createScientificEnvironment().mapValues { it.value }
    }
    
    /**
     * 통계 계산용 환경 변수들을 생성합니다.
     */
    fun createStatisticalEnvironment(): Map<String, Any?> {
        return EnvironmentFactory.createStatisticalEnvironment().mapValues { it.value }
    }
    
    /**
     * 사용자 정의 환경을 생성합니다.
     */
    fun createCustomEnvironment(customVariables: Map<String, Any?>): Map<String, Any?> {
        return EnvironmentFactory.createCustomEnvironment(
            customVariables.filterValues { it != null }.mapValues { it.value!! },
            EnvironmentFactory.createBasicEnvironment()
        ).mapValues { it.value }
    }
    
    /**
     * 팩토리 통계를 반환합니다.
     */
    fun getFactoryStatistics(): Map<String, Any> {
        return mapOf(
            "totalEvaluatorsCreated" to createdEvaluatorCount,
            "totalBindingsCreated" to createdBindingCount,
            "mathFunctionServiceCreated" to createdMathServiceCount,
            "factoryComplexity" to Complexity.NORMAL.name,
            "cacheEnabled" to true
        )
    }
    
    companion object {
        private var createdEvaluatorCount = 0L
        private var createdBindingCount = 0L
        private var createdMathServiceCount = 0L
        
        /**
         * 싱글톤 팩토리 인스턴스를 반환합니다.
         */
        @JvmStatic
        fun getInstance(): EvaluatorFactory = EvaluatorFactory()
        
        /**
         * 빠른 평가기 생성 편의 메서드입니다.
         */
        @JvmStatic
        fun quickCreateEvaluator(): ExpressionEvaluator {
            return getInstance().createEvaluator()
        }
        
        /**
         * 수학 상수 포함 평가기 생성 편의 메서드입니다.
         */
        @JvmStatic
        fun quickCreateMathEvaluator(): ExpressionEvaluator {
            return getInstance().createEvaluatorWithMathConstants()
        }
        
        /**
         * 과학 계산용 평가기 생성 편의 메서드입니다.
         */
        @JvmStatic
        fun quickCreateScientificEvaluator(): ExpressionEvaluator {
            val factory = getInstance()
            return factory.createEvaluator(factory.createScientificEnvironment())
        }
    }
    
    init {
        createdEvaluatorCount++
    }
}