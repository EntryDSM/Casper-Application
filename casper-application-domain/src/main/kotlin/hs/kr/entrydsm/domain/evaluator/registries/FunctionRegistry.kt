package hs.kr.entrydsm.domain.evaluator.registries

import hs.kr.entrydsm.domain.evaluator.interfaces.FunctionEvaluator
import hs.kr.entrydsm.domain.evaluator.functions.*

/**
 * 함수 레지스트리 클래스입니다.
 *
 * 모든 사용 가능한 함수들을 등록하고 관리하며,
 * 함수명으로 함수 평가기를 조회할 수 있습니다.
 *
 * @author kangeunchan
 * @since 2025.08.06
 */
class FunctionRegistry {
    
    private val functions = mutableMapOf<String, FunctionEvaluator>()
    
    init {
        registerDefaultFunctions()
    }
    
    /**
     * 기본 함수들을 등록합니다.
     */
    private fun registerDefaultFunctions() {
        // 기본 수학 함수들
        register(AbsFunction())
        register(SqrtFunction())
        register(RoundFunction())
        register(MinFunction())
        register(MaxFunction())
        register(SumFunction())
        register(AvgFunction())
        register(AverageFunction())
        register(IfFunction())
        register(PowFunction())
        
        // 삼각함수들
        register(SinFunction())
        register(CosFunction())
        register(TanFunction())
        
        // 로그 함수들
        register(LogFunction())
        register(Log10Function())
        register(ExpFunction())
    }
    
    /**
     * 함수를 등록합니다.
     *
     * @param evaluator 등록할 함수 평가기
     */
    fun register(evaluator: FunctionEvaluator) {
        functions[evaluator.getFunctionName().uppercase()] = evaluator
    }
    
    /**
     * 함수를 조회합니다.
     *
     * @param name 함수명
     * @return 함수 평가기 또는 null
     */
    fun get(name: String): FunctionEvaluator? {
        return functions[name.uppercase()]
    }
    
    /**
     * 함수가 등록되어 있는지 확인합니다.
     *
     * @param name 함수명
     * @return 등록되어 있으면 true, 아니면 false
     */
    fun contains(name: String): Boolean {
        return name.uppercase() in functions
    }
    
    /**
     * 등록된 모든 함수명을 반환합니다.
     *
     * @return 함수명 집합
     */
    fun getAllFunctionNames(): Set<String> {
        return functions.keys.toSet()
    }
    
    /**
     * 등록된 함수 개수를 반환합니다.
     *
     * @return 함수 개수
     */
    fun size(): Int = functions.size
    
    /**
     * 함수를 제거합니다.
     *
     * @param name 제거할 함수명
     * @return 제거된 함수 평가기 또는 null
     */
    fun unregister(name: String): FunctionEvaluator? {
        return functions.remove(name.uppercase())
    }
    
    /**
     * 모든 함수를 제거합니다.
     */
    fun clear() {
        functions.clear()
    }
    
    /**
     * 함수 정보를 반환합니다.
     *
     * @return 함수 정보 맵
     */
    fun getFunctionInfo(): Map<String, Map<String, Any>> {
        return functions.mapValues { (_, evaluator) ->
            mapOf(
                "name" to evaluator.getFunctionName(),
                "description" to evaluator.getDescription(),
                "supportedArgumentCounts" to (evaluator.getSupportedArgumentCounts() ?: listOf("variable"))
            )
        }
    }
    
    companion object {
        /**
         * 기본 함수 레지스트리 인스턴스를 생성합니다.
         *
         * @return FunctionRegistry 인스턴스
         */
        fun createDefault(): FunctionRegistry = FunctionRegistry()
    }
}