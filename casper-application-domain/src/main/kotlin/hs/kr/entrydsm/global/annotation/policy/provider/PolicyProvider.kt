package hs.kr.entrydsm.global.annotation.policy.provider

import hs.kr.entrydsm.global.annotation.policy.Policy
import hs.kr.entrydsm.global.annotation.policy.PolicyContract
import hs.kr.entrydsm.global.annotation.policy.PolicyResult
import hs.kr.entrydsm.global.annotation.policy.type.Scope

/**
 * DDD의 비즈니스 정책(Business Policy) 관리를 담당하는 Provider 객체입니다.
 *
 * 정책의 등록, 실행, 검색, 검증을 통해 도메인 비즈니스 규칙을
 * 명시적이고 체계적으로 관리할 수 있도록 지원합니다.
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
object PolicyProvider {

    private val policyRegistry = mutableMapOf<String, MutableSet<Class<*>>>()
    private val policyInstances = mutableMapOf<Class<*>, PolicyContract>()
    private val scopeRegistry = mutableMapOf<Scope, MutableSet<Class<*>>>()
    private val domainRegistry = mutableMapOf<String, MutableSet<Class<*>>>()
    private val policyCache = mutableMapOf<String, PolicyContract>()

    inline fun <reified T : PolicyContract> registerPolicy() {
        registerPolicy(T::class.java)
    }

    fun <T : PolicyContract> registerPolicy(policyClass: Class<T>) {
        validatePolicy(policyClass)
        
        val annotation = getPolicyAnnotation(policyClass)
        val name = annotation.name
        val domain = annotation.domain
        val scope = annotation.scope

        policyRegistry.getOrPut(name) { mutableSetOf() }.add(policyClass)
        scopeRegistry.getOrPut(scope) { mutableSetOf() }.add(policyClass)
        domainRegistry.getOrPut(domain) { mutableSetOf() }.add(policyClass)
    }

    fun executePolicy(name: String, context: Map<String, Any?>): PolicyResult {
        val policy = getPolicyByName(name)
        
        return if (policy.isApplicable(context)) {
            policy.execute(context)
        } else {
            PolicyResult(
                success = false,
                message = "정책 '$name'은 현재 컨텍스트에 적용할 수 없습니다."
            )
        }
    }

    fun executePoliciesByScope(scope: Scope, context: Map<String, Any?>): List<PolicyResult> {
        return getPoliciesByScope(scope)
            .sortedBy { it.getPriority() }
            .map { policy ->
                if (policy.isApplicable(context)) {
                    policy.execute(context)
                } else {
                    PolicyResult(
                        success = false,
                        message = "정책 '${policy.getName()}'은 현재 컨텍스트에 적용할 수 없습니다."
                    )
                }
            }
    }

    fun executePoliciesByDomain(domain: String, context: Map<String, Any?>): List<PolicyResult> {
        return getPoliciesByDomain(domain)
            .sortedBy { it.getPriority() }
            .map { policy ->
                if (policy.isApplicable(context)) {
                    policy.execute(context)
                } else {
                    PolicyResult(
                        success = false,
                        message = "정책 '${policy.getName()}'은 현재 컨텍스트에 적용할 수 없습니다."
                    )
                }
            }
    }

    fun getPolicyByName(name: String): PolicyContract {
        return policyCache.getOrPut(name) {
            val policyClasses = policyRegistry[name]
                ?: throw IllegalArgumentException("정책 '$name'을 찾을 수 없습니다.")
            
            if (policyClasses.size > 1) {
                throw IllegalArgumentException("정책 '$name'에 대해 여러 구현체가 존재합니다: ${policyClasses.map { it.simpleName }}")
            }
            
            getPolicyInstance(policyClasses.first())
        }
    }

    fun getPoliciesByScope(scope: Scope): List<PolicyContract> {
        return scopeRegistry[scope]?.map { getPolicyInstance(it) } ?: emptyList()
    }

    fun getPoliciesByDomain(domain: String): List<PolicyContract> {
        return domainRegistry[domain]?.map { getPolicyInstance(it) } ?: emptyList()
    }

    fun getAllPolicies(): List<PolicyContract> {
        return policyRegistry.values
            .flatten()
            .distinct()
            .map { getPolicyInstance(it) }
    }

    fun isPolicy(clazz: Class<*>): Boolean {
        return hasPolicyAnnotation(clazz) && PolicyContract::class.java.isAssignableFrom(clazz)
    }

    fun clearAll() {
        policyRegistry.clear()
        policyInstances.clear()
        scopeRegistry.clear()
        domainRegistry.clear()
        policyCache.clear()
    }

    fun validatePolicy(policyClass: Class<*>) {
        validatePolicyAnnotation(policyClass)
        validatePolicyContract(policyClass)
    }

    fun validatePolicyAnnotation(policyClass: Class<*>) {
        policyClass.getAnnotation(Policy::class.java)
            ?: throw IllegalArgumentException("클래스 ${policyClass.simpleName}에 @Policy 어노테이션이 없습니다.")
    }

    fun validatePolicyContract(policyClass: Class<*>) {
        if (!PolicyContract::class.java.isAssignableFrom(policyClass)) {
            throw IllegalArgumentException("클래스 ${policyClass.simpleName}는 PolicyContract 인터페이스를 구현해야 합니다.")
        }
    }

    fun getPolicyInstance(policyClass: Class<*>): PolicyContract {
        return policyInstances.getOrPut(policyClass) {
            createPolicyInstance(policyClass)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun createPolicyInstance(policyClass: Class<*>): PolicyContract {
        return policyClass.getDeclaredConstructor().newInstance() as PolicyContract
    }

    fun getPolicyAnnotation(policyClass: Class<*>): Policy {
        return policyClass.getAnnotation(Policy::class.java)
            ?: throw IllegalArgumentException("클래스 ${policyClass.simpleName}에 @Policy 어노테이션이 없습니다.")
    }

    fun hasPolicyAnnotation(clazz: Class<*>): Boolean {
        return clazz.getAnnotation(Policy::class.java) != null
    }
}