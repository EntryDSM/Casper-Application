package hs.kr.entrydsm.global.annotation.exceptions

import hs.kr.entrydsm.global.exception.DomainException
import hs.kr.entrydsm.global.exception.ErrorCode

/**
 * Annotation 패키지에서 발생하는 예외를 처리하는 클래스입니다.
 *
 * DDD 어노테이션의 등록, 검증, 조회 과정에서 발생하는 오류를 처리합니다.
 * Policy, Factory, Entity, Aggregate, Specification 등의 관리에서
 * 어노테이션 누락, 인터페이스 미구현, 중복 등록 등의 오류를 포함합니다.
 *
 * @property annotationType 오류와 관련된 어노테이션 타입 (선택사항)
 * @property className 오류와 관련된 클래스명 (선택사항)
 * @property contractType 구현해야 하는 인터페이스 타입 (선택사항)
 * @property name 등록/조회할 이름 (선택사항)
 * @property implementations 중복된 구현체들 (선택사항)
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.15
 */
class AnnotationException(
    errorCode: ErrorCode,
    val annotationType: String? = null,
    val className: String? = null,
    val contractType: String? = null,
    val name: String? = null,
    val implementations: List<String>? = null,
    message: String = buildAnnotationMessage(errorCode, annotationType, className, contractType, name, implementations),
    cause: Throwable? = null
) : DomainException(errorCode, message, cause) {

    companion object {
        /**
         * Annotation 오류 메시지를 구성합니다.
         *
         * @param errorCode 오류 코드
         * @param annotationType 어노테이션 타입
         * @param className 클래스명
         * @param contractType 인터페이스 타입
         * @param name 이름
         * @param implementations 구현체들
         * @return 구성된 메시지
         */
        private fun buildAnnotationMessage(
            errorCode: ErrorCode,
            annotationType: String?,
            className: String?,
            contractType: String?,
            name: String?,
            implementations: List<String>?
        ): String {
            val baseMessage = errorCode.description
            val details = mutableListOf<String>()
            
            annotationType?.let { details.add("어노테이션: @$it") }
            className?.let { details.add("클래스: $it") }
            contractType?.let { details.add("인터페이스: $it") }
            name?.let { details.add("이름: $it") }
            implementations?.let { details.add("구현체: ${it.joinToString(", ")}") }
            
            return if (details.isNotEmpty()) {
                "$baseMessage (${details.joinToString(", ")})"
            } else {
                baseMessage
            }
        }

        /**
         * 어노테이션 누락 오류를 생성합니다.
         *
         * @param annotationType 누락된 어노테이션 타입
         * @param className 대상 클래스명
         * @return AnnotationException 인스턴스
         */
        fun annotationMissing(annotationType: String, className: String): AnnotationException {
            return AnnotationException(
                errorCode = ErrorCode.ANNOTATION_MISSING,
                annotationType = annotationType,
                className = className
            )
        }

        /**
         * 인터페이스 미구현 오류를 생성합니다.
         *
         * @param className 대상 클래스명
         * @param contractType 구현해야 하는 인터페이스 타입
         * @return AnnotationException 인스턴스
         */
        fun contractNotImplemented(className: String, contractType: String): AnnotationException {
            return AnnotationException(
                errorCode = ErrorCode.CONTRACT_NOT_IMPLEMENTED,
                className = className,
                contractType = contractType
            )
        }

        /**
         * 정책 찾을 수 없음 오류를 생성합니다.
         *
         * @param name 찾을 수 없는 정책 이름
         * @return AnnotationException 인스턴스
         */
        fun policyNotFound(name: String): AnnotationException {
            return AnnotationException(
                errorCode = ErrorCode.POLICY_NOT_FOUND,
                name = name
            )
        }

        /**
         * 여러 구현체 존재 오류를 생성합니다.
         *
         * @param name 이름
         * @param implementations 구현체 목록
         * @return AnnotationException 인스턴스
         */
        fun multipleImplementations(name: String, implementations: List<String>): AnnotationException {
            return AnnotationException(
                errorCode = ErrorCode.MULTIPLE_IMPLEMENTATIONS,
                name = name,
                implementations = implementations
            )
        }

        /**
         * 팩토리 등록되지 않음 오류를 생성합니다.
         *
         * @param className 대상 클래스명
         * @return AnnotationException 인스턴스
         */
        fun factoryNotRegistered(className: String): AnnotationException {
            return AnnotationException(
                errorCode = ErrorCode.FACTORY_NOT_REGISTERED,
                className = className
            )
        }

        /**
         * 캐시 키 필요 오류를 생성합니다.
         *
         * @return AnnotationException 인스턴스
         */
        fun cacheKeyRequired(): AnnotationException {
            return AnnotationException(
                errorCode = ErrorCode.CACHE_KEY_REQUIRED
            )
        }

        /**
         * 유효하지 않은 애그리게이트 루트 오류를 생성합니다.
         *
         * @param className 애그리게이트 루트 클래스명
         * @return AnnotationException 인스턴스
         */
        fun invalidAggregateRoot(className: String): AnnotationException {
            return AnnotationException(
                errorCode = ErrorCode.INVALID_AGGREGATE_ROOT,
                className = className
            )
        }

        /**
         * 명세 찾을 수 없음 오류를 생성합니다.
         *
         * @param name 찾을 수 없는 명세 이름
         * @return AnnotationException 인스턴스
         */
        fun specificationNotFound(name: String): AnnotationException {
            return AnnotationException(
                errorCode = ErrorCode.SPECIFICATION_NOT_FOUND,
                name = name
            )
        }

        /**
         * 결합할 명세가 없음 오류를 생성합니다.
         *
         * @return AnnotationException 인스턴스
         */
        fun combineSpecificationsEmpty(): AnnotationException {
            return AnnotationException(
                errorCode = ErrorCode.COMBINE_SPECIFICATIONS_EMPTY
            )
        }
    }

    /**
     * Annotation 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 어노테이션, 클래스, 인터페이스, 이름, 구현체 정보가 포함된 맵
     */
    fun getAnnotationInfo(): Map<String, Any?> {
        val info = mutableMapOf<String, Any?>()
        
        annotationType?.let { info["annotationType"] = it }
        className?.let { info["className"] = it }
        contractType?.let { info["contractType"] = it }
        name?.let { info["name"] = it }
        implementations?.let { info["implementations"] = it }
        
        return info
    }

    /**
     * 전체 오류 정보를 구조화된 맵으로 반환합니다.
     *
     * @return 기본 오류 정보와 Annotation 정보가 결합된 맵
     */
    fun toCompleteErrorInfo(): Map<String, String> {
        val baseInfo = super.toErrorInfo().toMutableMap()
        val annotationInfo = getAnnotationInfo()
        
        annotationInfo.forEach { (key, value) ->
            baseInfo[key] = when (value) {
                is List<*> -> value.joinToString(", ")
                else -> value?.toString() ?: ""
            }
        }
        
        return baseInfo
    }

    override fun toString(): String {
        val annotationDetails = getAnnotationInfo()
        return if (annotationDetails.isNotEmpty()) {
            "${super.toString()}, annotation=${annotationDetails}"
        } else {
            super.toString()
        }
    }
}