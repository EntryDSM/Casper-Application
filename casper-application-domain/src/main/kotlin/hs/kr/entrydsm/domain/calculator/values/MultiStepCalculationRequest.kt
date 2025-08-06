package hs.kr.entrydsm.domain.calculator.values



/**
 * 다단계 수식 계산 요청을 나타내는 값 객체입니다.
 *
 * 여러 개의 계산 단계를 순차적으로 실행하여, 이전 단계의 결과를 
 * 다음 단계에서 변수로 사용할 수 있는 복합 계산 요청을 표현합니다.
 * POC 코드의 MultiStepCalculationRequest DTO를 DDD 값 객체로 구현하였습니다.
 *
 * @property variables 모든 단계에서 공통으로 사용될 초기 변수 맵
 * @property steps 수행할 계산 단계 목록
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.21
 */
data class MultiStepCalculationRequest(
    val variables: Map<String, Any?> = emptyMap(),
    val steps: List<CalculationStep> = emptyList()
) {
    
    init {
        require(steps.isNotEmpty()) { "계산 단계는 최소 1개 이상이어야 합니다" }
        require(steps.size <= 100) { "계산 단계는 최대 100개까지 허용됩니다: ${steps.size}" }
        require(variables.size <= 1000) { "변수는 최대 1000개까지 허용됩니다: ${variables.size}" }
        
        // 단계별 유효성 검사
        steps.forEachIndexed { index, step ->
            require(step.formula.isNotBlank()) { "단계 ${index + 1}의 수식이 비어있습니다" }
        }
    }

    /**
     * 새로운 변수를 추가한 요청을 생성합니다.
     *
     * @param name 변수 이름
     * @param value 변수 값
     * @return 새로운 MultiStepCalculationRequest
     */
    fun withVariable(name: String, value: Any?): MultiStepCalculationRequest {
        require(name.isNotBlank()) { "변수 이름은 비어있을 수 없습니다" }
        return copy(variables = variables + (name to value))
    }

    /**
     * 여러 변수를 추가한 요청을 생성합니다.
     *
     * @param newVariables 추가할 변수 맵
     * @return 새로운 MultiStepCalculationRequest
     */
    fun withVariables(newVariables: Map<String, Any?>): MultiStepCalculationRequest {
        return copy(variables = variables + newVariables)
    }

    /**
     * 새로운 단계를 추가한 요청을 생성합니다.
     *
     * @param step 추가할 계산 단계
     * @return 새로운 MultiStepCalculationRequest
     */
    fun withStep(step: CalculationStep): MultiStepCalculationRequest {
        return copy(steps = steps + step)
    }

    /**
     * 여러 단계를 추가한 요청을 생성합니다.
     *
     * @param newSteps 추가할 계산 단계 목록
     * @return 새로운 MultiStepCalculationRequest
     */
    fun withSteps(newSteps: List<CalculationStep>): MultiStepCalculationRequest {
        return copy(steps = steps + newSteps)
    }

    /**
     * 특정 위치에 단계를 삽입한 요청을 생성합니다.
     *
     * @param index 삽입할 위치
     * @param step 삽입할 계산 단계
     * @return 새로운 MultiStepCalculationRequest
     */
    fun insertStep(index: Int, step: CalculationStep): MultiStepCalculationRequest {
        require(index in 0..steps.size) { "인덱스가 범위를 벗어났습니다: $index (0-${steps.size})" }
        val newSteps = steps.toMutableList()
        newSteps.add(index, step)
        return copy(steps = newSteps)
    }

    /**
     * 특정 위치의 단계를 제거한 요청을 생성합니다.
     *
     * @param index 제거할 단계의 위치
     * @return 새로운 MultiStepCalculationRequest
     */
    fun removeStep(index: Int): MultiStepCalculationRequest {
        require(index in steps.indices) { "인덱스가 범위를 벗어났습니다: $index (0-${steps.size - 1})" }
        require(steps.size > 1) { "최소 1개의 단계는 유지되어야 합니다" }
        return copy(steps = steps.filterIndexed { i, _ -> i != index })
    }

    /**
     * 특정 변수를 제거한 요청을 생성합니다.
     *
     * @param name 제거할 변수 이름
     * @return 새로운 MultiStepCalculationRequest
     */
    fun withoutVariable(name: String): MultiStepCalculationRequest {
        return copy(variables = variables - name)
    }

    /**
     * 변수가 정의되어 있는지 확인합니다.
     *
     * @param name 확인할 변수 이름
     * @return 변수가 정의되어 있으면 true, 아니면 false
     */
    fun hasVariable(name: String): Boolean = name in variables

    /**
     * 변수 값을 가져옵니다.
     *
     * @param name 변수 이름
     * @return 변수 값 또는 null
     */
    fun getVariable(name: String): Any? = variables[name]

    /**
     * 특정 위치의 단계를 가져옵니다.
     *
     * @param index 단계 위치
     * @return 계산 단계
     */
    fun getStep(index: Int): CalculationStep {
        require(index in steps.indices) { "인덱스가 범위를 벗어났습니다: $index (0-${steps.size - 1})" }
        return steps[index]
    }

    /**
     * 전체 계산의 복잡도를 추정합니다.
     *
     * @return 복잡도 점수
     */
    fun estimateComplexity(): Int {
        var totalComplexity = 0
        
        // 단계별 복잡도 합산
        steps.forEach { step ->
            totalComplexity += estimateStepComplexity(step)
        }
        
        // 변수 개수에 따른 복잡도
        totalComplexity += variables.size * 2
        
        // 단계 수에 따른 복잡도 보정
        totalComplexity += steps.size * 5
        
        return totalComplexity
    }

    /**
     * 단일 단계의 복잡도를 추정합니다.
     *
     * @param step 계산 단계
     * @return 단계 복잡도 점수
     */
    private fun estimateStepComplexity(step: CalculationStep): Int {
        var complexity = 0
        
        // 수식 길이에 따른 복잡도
        complexity += (step.formula.length / 10).coerceAtMost(30)
        
        // 연산자 개수
        val operators = listOf("+", "-", "*", "/", "^", "==", "!=", "<", ">", "<=", ">=", "&&", "||", "!")
        complexity += operators.sumOf { op -> 
            step.formula.split(op).size - 1 
        } * 2
        
        // 함수 호출 개수
        val functionPattern = Regex("[a-zA-Z]+\\(")
        complexity += functionPattern.findAll(step.formula).count() * 3
        
        return complexity
    }

    /**
     * 모든 단계에서 사용되는 변수들을 추출합니다.
     *
     * @return 사용되는 변수명 집합
     */
    fun extractAllVariables(): Set<String> {
        val allVariables = mutableSetOf<String>()
        
        steps.forEach { step ->
            allVariables.addAll(extractVariablesFromFormula(step.formula))
        }
        
        return allVariables
    }

    /**
     * 수식에서 변수를 추출합니다.
     *
     * @param formula 수식 문자열
     * @return 변수명 집합
     */
    private fun extractVariablesFromFormula(formula: String): Set<String> {
        // 중괄호로 둘러싸인 변수 패턴과 일반 식별자 패턴
        val variablePatterns = listOf(
            Regex("\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}"), // {변수명}
            Regex("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b")   // 일반 식별자
        )
        
        val variables = mutableSetOf<String>()
        val reservedWords = setOf(
            "sin", "cos", "tan", "sqrt", "log", "exp", "abs", "floor", "ceil", "round",
            "min", "max", "pow", "if", "true", "false", "and", "or", "not"
        )
        
        variablePatterns.forEach { pattern ->
            pattern.findAll(formula).forEach { match ->
                val variable = if (match.groups.size > 1) match.groups[1]?.value else match.value
                if (variable != null && variable !in reservedWords) {
                    variables.add(variable)
                }
            }
        }
        
        return variables
    }

    /**
     * 누락된 변수들을 확인합니다.
     *
     * @return 누락된 변수명 집합
     */
    fun findMissingVariables(): Set<String> {
        val requiredVariables = extractAllVariables()
        val providedVariables = variables.keys
        
        // 단계별 결과 변수들도 고려
        val resultVariables = steps.mapNotNull { it.resultVariable }.toSet()
        val availableVariables = providedVariables + resultVariables
        
        return requiredVariables - availableVariables
    }

    /**
     * 사용되지 않는 변수들을 확인합니다.
     *
     * @return 사용되지 않는 변수명 집합
     */
    fun findUnusedVariables(): Set<String> {
        val requiredVariables = extractAllVariables()
        return variables.keys - requiredVariables
    }

    /**
     * 단계별 의존성을 분석합니다.
     *
     * @return 단계별 의존 변수 맵
     */
    fun analyzeDependencies(): Map<Int, Set<String>> {
        val dependencies = mutableMapOf<Int, Set<String>>()
        
        steps.forEachIndexed { index, step ->
            dependencies[index] = extractVariablesFromFormula(step.formula)
        }
        
        return dependencies
    }

    /**
     * 순환 의존성이 있는지 확인합니다.
     * DFS 알고리즘을 사용하여 모든 직간접 순환 의존성을 감지합니다.
     *
     * @return 순환 의존성이 있으면 true, 아니면 false
     */
    fun hasCircularDependency(): Boolean {
        // 의존성 그래프 구축
        val dependencyGraph = buildDependencyGraph()
        val visited = mutableSetOf<String>()
        val recursionStack = mutableSetOf<String>()
        
        // 모든 노드에서 DFS 수행
        for (node in dependencyGraph.keys) {
            if (node !in visited) {
                if (hasCycleDFS(node, dependencyGraph, visited, recursionStack)) {
                    return true
                }
            }
        }
        
        return false
    }
    
    /**
     * 의존성 그래프를 구축합니다.
     * 각 결과 변수를 키로 하고, 해당 변수가 의존하는 변수들을 값으로 하는 맵을 생성합니다.
     *
     * @return 의존성 그래프 맵
     */
    private fun buildDependencyGraph(): Map<String, Set<String>> {
        val graph = mutableMapOf<String, Set<String>>()
        val dependencies = analyzeDependencies()
        
        steps.forEachIndexed { index, step ->
            step.resultVariable?.let { resultVar ->
                val stepDependencies = dependencies[index] ?: emptySet()
                graph[resultVar] = stepDependencies
                
                // 의존하는 변수들도 그래프에 추가 (빈 의존성으로)
                stepDependencies.forEach { dep ->
                    if (dep !in graph) {
                        graph[dep] = emptySet()
                    }
                }
            }
        }
        
        return graph
    }
    
    /**
     * DFS를 사용하여 순환 의존성을 감지합니다.
     * 재귀 스택을 사용하여 현재 경로에서 이미 방문한 노드를 다시 만나면 순환으로 판단합니다.
     *
     * @param node 현재 노드
     * @param graph 의존성 그래프
     * @param visited 방문한 노드 집합
     * @param recursionStack 현재 재귀 경로의 노드 집합
     * @return 순환이 감지되면 true
     */
    private fun hasCycleDFS(
        node: String,
        graph: Map<String, Set<String>>,
        visited: MutableSet<String>,
        recursionStack: MutableSet<String>
    ): Boolean {
        visited.add(node)
        recursionStack.add(node)
        
        // 현재 노드의 모든 의존성을 검사
        val dependencies = graph[node] ?: emptySet()
        for (dependency in dependencies) {
            // 의존성이 현재 재귀 스택에 있으면 순환 감지
            if (dependency in recursionStack) {
                return true
            }
            
            // 아직 방문하지 않은 의존성에 대해 재귀 DFS 수행
            if (dependency !in visited) {
                if (hasCycleDFS(dependency, graph, visited, recursionStack)) {
                    return true
                }
            }
        }
        
        // 현재 노드 처리 완료, 재귀 스택에서 제거
        recursionStack.remove(node)
        return false
    }

    /**
     * 요청의 유효성을 검사합니다.
     *
     * @return 유효하면 true, 아니면 false
     */
    fun isValid(): Boolean {
        return try {
            steps.isNotEmpty() &&
            steps.size <= 100 &&
            variables.size <= 1000 &&
            steps.all { it.formula.isNotBlank() && it.formula.length <= 10000 } &&
            !hasCircularDependency()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 요청의 통계 정보를 반환합니다.
     *
     * @return 통계 정보 맵
     */
    fun getStatistics(): Map<String, Any> = mapOf(
        "stepCount" to steps.size,
        "variableCount" to variables.size,
        "totalComplexity" to estimateComplexity(),
        "requiredVariables" to extractAllVariables(),
        "missingVariables" to findMissingVariables(),
        "unusedVariables" to findUnusedVariables(),
        "hasCircularDependency" to hasCircularDependency(),
        "dependencies" to analyzeDependencies(),
        "isValid" to isValid()
    )

    companion object {
        /**
         * 단일 단계로 간단한 다단계 요청을 생성합니다.
         *
         * @param formula 수식
         * @return MultiStepCalculationRequest
         */
        fun singleStep(formula: String): MultiStepCalculationRequest {
            return MultiStepCalculationRequest(
                steps = listOf(CalculationStep.simple(formula))
            )
        }

        /**
         * 여러 수식으로 다단계 요청을 생성합니다.
         *
         * @param formulas 수식 목록
         * @return MultiStepCalculationRequest
         */
        fun fromFormulas(formulas: List<String>): MultiStepCalculationRequest {
            val steps = formulas.mapIndexed { index, formula ->
                CalculationStep(
                    stepName = "단계 ${index + 1}",
                    formula = formula,
                    resultVariable = if (index < formulas.size - 1) "step${index + 1}_result" else null
                )
            }
            return MultiStepCalculationRequest(steps = steps)
        }

        /**
         * 변수와 함께 다단계 요청을 생성합니다.
         *
         * @param variables 초기 변수 맵
         * @param steps 계산 단계 목록
         * @return MultiStepCalculationRequest
         */
        fun create(variables: Map<String, Any?>, steps: List<CalculationStep>): MultiStepCalculationRequest {
            return MultiStepCalculationRequest(variables, steps)
        }

        /**
         * 빌더 패턴으로 다단계 요청을 생성합니다.
         *
         * @return MultiStepCalculationRequestBuilder
         */
        fun builder(): MultiStepCalculationRequestBuilder {
            return MultiStepCalculationRequestBuilder()
        }
    }

    /**
     * 다단계 계산 요청을 구성하기 위한 빌더 클래스입니다.
     */
    class MultiStepCalculationRequestBuilder {
        private val variables = mutableMapOf<String, Any?>()
        private val steps = mutableListOf<CalculationStep>()

        fun variable(name: String, value: Any?): MultiStepCalculationRequestBuilder {
            variables[name] = value
            return this
        }

        fun variables(vars: Map<String, Any?>): MultiStepCalculationRequestBuilder {
            variables.putAll(vars)
            return this
        }

        fun step(step: CalculationStep): MultiStepCalculationRequestBuilder {
            steps.add(step)
            return this
        }

        fun step(formula: String): MultiStepCalculationRequestBuilder {
            steps.add(CalculationStep.simple(formula))
            return this
        }

        fun step(stepName: String, formula: String, resultVariable: String? = null): MultiStepCalculationRequestBuilder {
            steps.add(CalculationStep(stepName, formula, resultVariable))
            return this
        }

        fun build(): MultiStepCalculationRequest {
            return MultiStepCalculationRequest(variables.toMap(), steps.toList())
        }
    }
}