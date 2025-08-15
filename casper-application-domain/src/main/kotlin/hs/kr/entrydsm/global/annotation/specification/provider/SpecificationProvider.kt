package hs.kr.entrydsm.global.annotation.specification.provider

import hs.kr.entrydsm.global.annotation.specification.Specification
import hs.kr.entrydsm.global.annotation.specification.SpecificationContract
import hs.kr.entrydsm.global.annotation.specification.SpecificationResult
import hs.kr.entrydsm.global.annotation.specification.type.Priority

/**
 * DDD의 Specification 패턴 관리를 담당하는 Provider 객체입니다.
 *
 * 비즈니스 규칙(Specification)의 등록, 검증, 검색을 통해 도메인 로직을
 * 명시적이고 체계적으로 관리할 수 있도록 지원합니다.
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
object SpecificationProvider {

    private val specificationRegistry = mutableMapOf<String, MutableSet<Class<*>>>()
    private val specificationInstances = mutableMapOf<Class<*>, SpecificationContract<*>>()
    private val domainRegistry = mutableMapOf<String, MutableSet<Class<*>>>()
    private val priorityRegistry = mutableMapOf<Priority, MutableSet<Class<*>>>()
    private val specificationCache = mutableMapOf<String, SpecificationContract<*>>()

    /**
     * 타입 안전성을 위한 인라인 함수로 명세를 등록합니다.
     *
     * @param T 등록할 명세 클래스 타입
     */
    inline fun <reified T : SpecificationContract<*>> registerSpecification() {
        registerSpecification(T::class.java)
    }

    /**
     * 명세 클래스를 등록합니다.
     *
     * @param specificationClass 등록할 명세 클래스
     * @param T 명세 클래스 타입
     * @throws IllegalArgumentException 어노테이션이 없거나 인터페이스를 구현하지 않은 경우
     */
    fun <T : SpecificationContract<*>> registerSpecification(specificationClass: Class<T>) {
        validateSpecification(specificationClass)
        
        val annotation = getRuleAnnotation(specificationClass)
        val name = annotation.name
        val domain = annotation.domain
        val priority = annotation.priority

        specificationRegistry.getOrPut(name) { mutableSetOf() }.add(specificationClass)
        domainRegistry.getOrPut(domain) { mutableSetOf() }.add(specificationClass)
        priorityRegistry.getOrPut(priority) { mutableSetOf() }.add(specificationClass)
    }

    /**
     * 특정 명세를 사용하여 객체를 검증합니다.
     *
     * @param name 사용할 명세의 이름
     * @param candidate 검증할 객체
     * @param T 검증 대상 객체의 타입
     * @return 검증 결과
     * @throws IllegalArgumentException 명세를 찾을 수 없는 경우
     */
    fun <T> validateWithSpecification(name: String, candidate: T): SpecificationResult<T> {
        val specification = getSpecificationByName<T>(name)
        val success = specification.isSatisfiedBy(candidate)
        val message = if (success) "검증 성공" else specification.getErrorMessage(candidate)
        
        return SpecificationResult(
            success = success,
            message = message,
            specification = specification
        )
    }

    /**
     * 도메인에 속한 여러 명세들을 사용하여 객체를 검증합니다.
     *
     * @param domain 대상 도메인 이름
     * @param candidate 검증할 객체
     * @param priority 특정 우선순위의 명세만 사용할 경우 지정
     * @param T 검증 대상 객체의 타입
     * @return 각 명세에 대한 검증 결과 리스트 (우선순위 내림차순)
     */
    fun <T> validateWithSpecifications(
        domain: String,
        candidate: T,
        priority: Priority? = null
    ): List<SpecificationResult<T>> {
        val specifications = getSpecificationsByDomain<T>(domain)
            .let { specs ->
                if (priority != null) {
                    specs.filter { it.getPriority() == priority }
                } else {
                    specs
                }
            }
            .sortedByDescending { it.getPriority() }

        return specifications.map { specification ->
            val success = specification.isSatisfiedBy(candidate)
            val message = if (success) "검증 성공" else specification.getErrorMessage(candidate)
            
            SpecificationResult(
                success = success,
                message = message,
                specification = specification
            )
        }
    }

    /**
     * 도메인의 모든 명세를 사용하여 객체를 검증합니다.
     *
     * @param domain 대상 도메인 이름
     * @param candidate 검증할 객체
     * @param failFast 첫 번째 실패 시 즉시 false 반환 여부
     * @param T 검증 대상 객체의 타입
     * @return 모든 명세를 만족하면 true, 하나라도 실패하면 false
     */
    fun <T> validateWithAllSpecifications(
        domain: String,
        candidate: T,
        failFast: Boolean = false
    ): Boolean {
        val specifications = getSpecificationsByDomain<T>(domain)
            .sortedByDescending { it.getPriority() }

        for (specification in specifications) {
            if (!specification.isSatisfiedBy(candidate)) {
                if (failFast) return false
            }
        }
        
        return true
    }

    /**
     * 여러 명세를 논리 연산자로 결합하여 새로운 복합 명세를 생성합니다.
     *
     * @param names 결합할 명세들의 이름 리스트
     * @param operator 결합 연산자 (AND 또는 OR)
     * @param T 검증 대상 객체의 타입
     * @return 결합된 복합 명세
     * @throws IllegalArgumentException 결합할 명세가 없거나 명세를 찾을 수 없는 경우
     */
    fun <T> combineSpecifications(
        names: List<String>,
        operator: CombineOperator = CombineOperator.AND
    ): SpecificationContract<T> {
        if (names.isEmpty()) {
            throw IllegalArgumentException("결합할 명세가 없습니다.")
        }
        
        val specifications = names.map { getSpecificationByName<T>(it) }
        
        return when (operator) {
            CombineOperator.AND -> specifications.reduce { acc, spec -> acc.and(spec) }
            CombineOperator.OR -> specifications.reduce { acc, spec -> acc.or(spec) }
        }
    }

    /**
     * 이름을 통해 명세를 조회합니다.
     *
     * @param name 조회할 명세의 이름
     * @param T 대상 객체의 타입
     * @return 명세 인스턴스
     * @throws IllegalArgumentException 명세를 찾을 수 없거나 중복 구현체가 있는 경우
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getSpecificationByName(name: String): SpecificationContract<T> {
        return specificationCache.getOrPut(name) {
            val specificationClasses = specificationRegistry[name]
                ?: throw IllegalArgumentException("명세 '$name'을 찾을 수 없습니다.")
            
            if (specificationClasses.size > 1) {
                throw IllegalArgumentException("명세 '$name'에 대해 여러 구현체가 존재합니다: ${specificationClasses.map { it.simpleName }}")
            }
            
            getSpecificationInstance(specificationClasses.first())
        } as SpecificationContract<T>
    }

    /**
     * 도메인에 속한 모든 명세들을 조회합니다.
     *
     * @param domain 대상 도메인 이름
     * @param T 대상 객체의 타입
     * @return 도메인에 속한 명세 리스트
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getSpecificationsByDomain(domain: String): List<SpecificationContract<T>> {
        return domainRegistry[domain]?.map { getSpecificationInstance(it) as SpecificationContract<T> } ?: emptyList()
    }

    /**
     * 특정 우선순위에 해당하는 모든 명세들을 조회합니다.
     *
     * @param priority 대상 우선순위
     * @param T 대상 객체의 타입
     * @return 해당 우선순위에 속한 명세 리스트
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getSpecificationsByPriority(priority: Priority): List<SpecificationContract<T>> {
        return priorityRegistry[priority]?.map { getSpecificationInstance(it) as SpecificationContract<T> } ?: emptyList()
    }

    /**
     * 등록된 모든 명세들을 조회합니다.
     *
     * @return 등록된 모든 명세 리스트
     */
    fun getAllSpecifications(): List<SpecificationContract<*>> {
        return specificationRegistry.values
            .flatten()
            .distinct()
            .map { getSpecificationInstance(it) }
    }

    /**
     * 주어진 클래스가 유효한 명세 클래스인지 확인합니다.
     *
     * @param clazz 확인할 클래스
     * @return 유효한 명세 클래스이면 true, 그렇지 않으면 false
     */
    fun isSpecification(clazz: Class<*>): Boolean {
        return hasRuleAnnotation(clazz) && SpecificationContract::class.java.isAssignableFrom(clazz)
    }

    /**
     * 모든 등록된 명세와 캐시를 지웁니다.
     */
    fun clearAll() {
        specificationRegistry.clear()
        specificationInstances.clear()
        domainRegistry.clear()
        priorityRegistry.clear()
        specificationCache.clear()
    }

    /**
     * 명세 클래스의 유효성을 검증합니다.
     *
     * @param specificationClass 검증할 명세 클래스
     * @throws IllegalArgumentException 어노테이션이 없거나 인터페이스를 구현하지 않은 경우
     */
    fun validateSpecification(specificationClass: Class<*>) {
        validateRuleAnnotation(specificationClass)
        validateSpecificationContract(specificationClass)
    }

    /**
     * 명세 클래스에 @Specification 어노테이션이 있는지 검증합니다.
     *
     * @param specificationClass 검증할 명세 클래스
     * @throws IllegalArgumentException @Specification 어노테이션이 없는 경우
     */
    fun validateRuleAnnotation(specificationClass: Class<*>) {
        specificationClass.getAnnotation(Specification::class.java)
            ?: throw IllegalArgumentException("클래스 ${specificationClass.simpleName}에 @Specification 어노테이션이 없습니다.")
    }

    /**
     * 명세 클래스가 SpecificationContract 인터페이스를 구현하는지 검증합니다.
     *
     * @param specificationClass 검증할 명세 클래스
     * @throws IllegalArgumentException SpecificationContract 인터페이스를 구현하지 않은 경우
     */
    fun validateSpecificationContract(specificationClass: Class<*>) {
        if (!SpecificationContract::class.java.isAssignableFrom(specificationClass)) {
            throw IllegalArgumentException("클래스 ${specificationClass.simpleName}는 SpecificationContract 인터페이스를 구현해야 합니다.")
        }
    }

    /**
     * 명세 클래스의 인스턴스를 조회하거나 생성합니다.
     *
     * @param specificationClass 대상 명세 클래스
     * @return 명세 인스턴스 (캐시됨)
     */
    fun getSpecificationInstance(specificationClass: Class<*>): SpecificationContract<*> {
        return specificationInstances.getOrPut(specificationClass) {
            createSpecificationInstance(specificationClass)
        }
    }

    /**
     * 명세 클래스의 새로운 인스턴스를 생성합니다.
     *
     * @param specificationClass 인스턴스를 생성할 명세 클래스
     * @return 새로 생성된 명세 인스턴스
     * @throws RuntimeException 인스턴스 생성에 실패한 경우
     */
    @Suppress("UNCHECKED_CAST")
    fun createSpecificationInstance(specificationClass: Class<*>): SpecificationContract<*> {
        return specificationClass.getDeclaredConstructor().newInstance() as SpecificationContract<*>
    }

    /**
     * 명세 클래스에서 @Specification 어노테이션을 가져옵니다.
     *
     * @param specificationClass 대상 명세 클래스
     * @return @Specification 어노테이션 인스턴스
     * @throws IllegalArgumentException @Specification 어노테이션이 없는 경우
     */
    fun getRuleAnnotation(specificationClass: Class<*>): Specification {
        return specificationClass.getAnnotation(Specification::class.java)
            ?: throw IllegalArgumentException("클래스 ${specificationClass.simpleName}에 @Specification 어노테이션이 없습니다.")
    }

    /**
     * 클래스에 @Specification 어노테이션이 있는지 확인합니다.
     *
     * @param clazz 확인할 클래스
     * @return @Specification 어노테이션이 있으면 true, 없으면 false
     */
    fun hasRuleAnnotation(clazz: Class<*>): Boolean {
        return clazz.getAnnotation(Specification::class.java) != null
    }
}