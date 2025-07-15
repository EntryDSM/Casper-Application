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

    inline fun <reified T : SpecificationContract<*>> registerSpecification() {
        registerSpecification(T::class.java)
    }

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

    @Suppress("UNCHECKED_CAST")
    fun <T> getSpecificationsByDomain(domain: String): List<SpecificationContract<T>> {
        return domainRegistry[domain]?.map { getSpecificationInstance(it) as SpecificationContract<T> } ?: emptyList()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getSpecificationsByPriority(priority: Priority): List<SpecificationContract<T>> {
        return priorityRegistry[priority]?.map { getSpecificationInstance(it) as SpecificationContract<T> } ?: emptyList()
    }

    fun getAllSpecifications(): List<SpecificationContract<*>> {
        return specificationRegistry.values
            .flatten()
            .distinct()
            .map { getSpecificationInstance(it) }
    }

    fun isSpecification(clazz: Class<*>): Boolean {
        return hasRuleAnnotation(clazz) && SpecificationContract::class.java.isAssignableFrom(clazz)
    }

    fun clearAll() {
        specificationRegistry.clear()
        specificationInstances.clear()
        domainRegistry.clear()
        priorityRegistry.clear()
        specificationCache.clear()
    }

    fun validateSpecification(specificationClass: Class<*>) {
        validateRuleAnnotation(specificationClass)
        validateSpecificationContract(specificationClass)
    }

    fun validateRuleAnnotation(specificationClass: Class<*>) {
        specificationClass.getAnnotation(Specification::class.java)
            ?: throw IllegalArgumentException("클래스 ${specificationClass.simpleName}에 @Specification 어노테이션이 없습니다.")
    }

    fun validateSpecificationContract(specificationClass: Class<*>) {
        if (!SpecificationContract::class.java.isAssignableFrom(specificationClass)) {
            throw IllegalArgumentException("클래스 ${specificationClass.simpleName}는 SpecificationContract 인터페이스를 구현해야 합니다.")
        }
    }

    fun getSpecificationInstance(specificationClass: Class<*>): SpecificationContract<*> {
        return specificationInstances.getOrPut(specificationClass) {
            createSpecificationInstance(specificationClass)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun createSpecificationInstance(specificationClass: Class<*>): SpecificationContract<*> {
        return specificationClass.getDeclaredConstructor().newInstance() as SpecificationContract<*>
    }

    fun getRuleAnnotation(specificationClass: Class<*>): Specification {
        return specificationClass.getAnnotation(Specification::class.java)
            ?: throw IllegalArgumentException("클래스 ${specificationClass.simpleName}에 @Specification 어노테이션이 없습니다.")
    }

    fun hasRuleAnnotation(clazz: Class<*>): Boolean {
        return clazz.getAnnotation(Specification::class.java) != null
    }
}