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

    /**
     * 타입 안전성을 위한 인라인 함수로 정책을 등록합니다.
     *
     * @param T 등록할 정책 클래스 타입
     */
    inline fun <reified T : PolicyContract> registerPolicy() {
        registerPolicy(T::class.java)
    }

    /**
     * 정책 클래스를 등록합니다.
     *
     * @param policyClass 등록할 정책 클래스
     * @param T 정책 클래스 타입
     * @throws IllegalArgumentException 어노테이션이 없거나 인터페이스를 구현하지 않은 경우
     */
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

    /**
     * 특정 이름의 정책을 실행합니다.
     *
     * @param name 실행할 정책의 이름
     * @param context 정책 실행에 필요한 컨텍스트 정보
     * @return 정책 실행 결과
     * @throws IllegalArgumentException 정책을 찾을 수 없는 경우
     */
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

    /**
     * 특정 범위(Scope)에 속한 모든 정책들을 실행합니다.
     *
     * @param scope 대상 범위 (GLOBAL, DOMAIN, AGGREGATE, ENTITY)
     * @param context 정책 실행에 필요한 컨텍스트 정보
     * @return 각 정책에 대한 실행 결과 리스트 (우선순위 오름차순)
     */
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

    /**
     * 특정 도메인에 속한 모든 정책들을 실행합니다.
     *
     * @param domain 대상 도메인 이름
     * @param context 정책 실행에 필요한 컨텍스트 정보
     * @return 각 정책에 대한 실행 결과 리스트 (우선순위 오름차순)
     */
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

    /**
     * 이름을 통해 정책을 조회합니다.
     *
     * @param name 조회할 정책의 이름
     * @return 정책 인스턴스
     * @throws IllegalArgumentException 정책을 찾을 수 없거나 중복 구현체가 있는 경우
     */
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

    /**
     * 특정 범위(Scope)에 속한 모든 정책들을 조회합니다.
     *
     * @param scope 대상 범위 (GLOBAL, DOMAIN, AGGREGATE, ENTITY)
     * @return 해당 범위에 속한 정책 리스트
     */
    fun getPoliciesByScope(scope: Scope): List<PolicyContract> {
        return scopeRegistry[scope]?.map { getPolicyInstance(it) } ?: emptyList()
    }

    /**
     * 특정 도메인에 속한 모든 정책들을 조회합니다.
     *
     * @param domain 대상 도메인 이름
     * @return 도메인에 속한 정책 리스트
     */
    fun getPoliciesByDomain(domain: String): List<PolicyContract> {
        return domainRegistry[domain]?.map { getPolicyInstance(it) } ?: emptyList()
    }

    /**
     * 등록된 모든 정책들을 조회합니다.
     *
     * @return 등록된 모든 정책 리스트
     */
    fun getAllPolicies(): List<PolicyContract> {
        return policyRegistry.values
            .flatten()
            .distinct()
            .map { getPolicyInstance(it) }
    }

    /**
     * 주어진 클래스가 유효한 정책 클래스인지 확인합니다.
     *
     * @param clazz 확인할 클래스
     * @return 유효한 정책 클래스이면 true, 그렇지 않으면 false
     */
    fun isPolicy(clazz: Class<*>): Boolean {
        return hasPolicyAnnotation(clazz) && PolicyContract::class.java.isAssignableFrom(clazz)
    }

    /**
     * 모든 등록된 정책과 캐시를 지웁니다.
     */
    fun clearAll() {
        policyRegistry.clear()
        policyInstances.clear()
        scopeRegistry.clear()
        domainRegistry.clear()
        policyCache.clear()
    }

    /**
     * 정책 클래스의 유효성을 검증합니다.
     *
     * @param policyClass 검증할 정책 클래스
     * @throws IllegalArgumentException 어노테이션이 없거나 인터페이스를 구현하지 않은 경우
     */
    fun validatePolicy(policyClass: Class<*>) {
        validatePolicyAnnotation(policyClass)
        validatePolicyContract(policyClass)
    }

    /**
     * 정책 클래스에 @Policy 어노테이션이 있는지 검증합니다.
     *
     * @param policyClass 검증할 정책 클래스
     * @throws IllegalArgumentException @Policy 어노테이션이 없는 경우
     */
    fun validatePolicyAnnotation(policyClass: Class<*>) {
        policyClass.getAnnotation(Policy::class.java)
            ?: throw IllegalArgumentException("클래스 ${policyClass.simpleName}에 @Policy 어노테이션이 없습니다.")
    }

    /**
     * 정책 클래스가 PolicyContract 인터페이스를 구현하는지 검증합니다.
     *
     * @param policyClass 검증할 정책 클래스
     * @throws IllegalArgumentException PolicyContract 인터페이스를 구현하지 않은 경우
     */
    fun validatePolicyContract(policyClass: Class<*>) {
        if (!PolicyContract::class.java.isAssignableFrom(policyClass)) {
            throw IllegalArgumentException("클래스 ${policyClass.simpleName}는 PolicyContract 인터페이스를 구현해야 합니다.")
        }
    }

    /**
     * 정책 클래스의 인스턴스를 조회하거나 생성합니다.
     *
     * @param policyClass 대상 정책 클래스
     * @return 정책 인스턴스 (캐시됨)
     */
    fun getPolicyInstance(policyClass: Class<*>): PolicyContract {
        return policyInstances.getOrPut(policyClass) {
            createPolicyInstance(policyClass)
        }
    }

    /**
     * 정책 클래스의 새로운 인스턴스를 생성합니다.
     *
     * @param policyClass 인스턴스를 생성할 정책 클래스
     * @return 새로 생성된 정책 인스턴스
     * @throws RuntimeException 인스턴스 생성에 실패한 경우
     */
    @Suppress("UNCHECKED_CAST")
    fun createPolicyInstance(policyClass: Class<*>): PolicyContract {
        return policyClass.getDeclaredConstructor().newInstance() as PolicyContract
    }

    /**
     * 정책 클래스에서 @Policy 어노테이션을 가져옵니다.
     *
     * @param policyClass 대상 정책 클래스
     * @return @Policy 어노테이션 인스턴스
     * @throws IllegalArgumentException @Policy 어노테이션이 없는 경우
     */
    fun getPolicyAnnotation(policyClass: Class<*>): Policy {
        return policyClass.getAnnotation(Policy::class.java)
            ?: throw IllegalArgumentException("클래스 ${policyClass.simpleName}에 @Policy 어노테이션이 없습니다.")
    }

    /**
     * 클래스에 @Policy 어노테이션이 있는지 확인합니다.
     *
     * @param clazz 확인할 클래스
     * @return @Policy 어노테이션이 있으면 true, 없으면 false
     */
    fun hasPolicyAnnotation(clazz: Class<*>): Boolean {
        return clazz.getAnnotation(Policy::class.java) != null
    }
}