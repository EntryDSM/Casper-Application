package hs.kr.entrydsm.domain.factories

import kotlin.math.*

/**
 * 계산 환경 생성을 담당하는 싱글톤 팩토리입니다.
 *
 * DRY 원칙을 적용하여 CalculatorFactory와 EvaluatorFactory에서 중복되던
 * 환경 생성 로직을 통합했습니다. 다양한 계산 환경에 필요한 상수와 변수들을
 * 일관되게 제공합니다.
 *
 * @author kangeunchan
 * @since 2025.08.06
 */
object EnvironmentFactory {
    
    /**
     * 기본 수학 상수들을 포함한 환경을 생성합니다.
     * 모든 환경의 기반이 되는 핵심 상수들을 제공합니다.
     *
     * @return 기본 상수가 포함된 환경 맵
     */
    fun createBasicEnvironment(): Map<String, Any> {
        return mapOf(
            "PI" to PI,
            "E" to E,
            "TRUE" to true,
            "FALSE" to false,
            "INFINITY" to Double.POSITIVE_INFINITY,
            "NAN" to Double.NaN
        )
    }
    
    /**
     * 수학 계산용 환경을 생성합니다.
     * 기본 환경에 추가적인 수학 상수들을 포함합니다.
     *
     * @return 수학 상수가 포함된 환경 맵
     */
    fun createMathEnvironment(): Map<String, Any> {
        val environment = createBasicEnvironment().toMutableMap()
        
        // 추가 수학 상수들
        environment["GOLDEN_RATIO"] = (1 + sqrt(5.0)) / 2
        environment["EULER_GAMMA"] = 0.5772156649015329
        environment["SQRT_2"] = sqrt(2.0)
        environment["SQRT_PI"] = sqrt(PI)
        environment["SQRT_2PI"] = sqrt(2 * PI)
        environment["LN_2"] = ln(2.0)
        environment["LN_10"] = ln(10.0)
        environment["LOG10_E"] = log10(E)
        
        return environment
    }
    
    /**
     * 과학 계산용 환경을 생성합니다.
     * 수학 환경에 물리 상수들을 추가합니다.
     *
     * @return 과학 상수가 포함된 환경 맵
     */
    fun createScientificEnvironment(): Map<String, Any> {
        val environment = createMathEnvironment().toMutableMap()
        
        // 기본 물리 상수들
        environment["LIGHT_SPEED"] = 299792458.0 // m/s
        environment["PLANCK"] = 6.62607015e-34 // J⋅s
        environment["PLANCK_REDUCED"] = 1.054571817e-34 // ℏ (J⋅s)
        environment["AVOGADRO"] = 6.02214076e23 // mol⁻¹
        environment["BOLTZMANN"] = 1.380649e-23 // J/K
        environment["GAS_CONSTANT"] = 8.314462618 // J/(mol⋅K)
        
        // 입자 물리 상수들
        environment["ELECTRON_CHARGE"] = 1.602176634e-19 // C
        environment["ELECTRON_MASS"] = 9.1093837015e-31 // kg
        environment["PROTON_MASS"] = 1.67262192369e-27 // kg
        environment["NEUTRON_MASS"] = 1.67492749804e-27 // kg
        environment["FINE_STRUCTURE"] = 7.2973525693e-3 // 무차원
        
        return environment
    }
    
    /**
     * 공학 계산용 환경을 생성합니다.
     * 과학 환경에 공학 상수들을 추가합니다.
     *
     * @return 공학 상수가 포함된 환경 맵
     */
    fun createEngineeringEnvironment(): Map<String, Any> {
        val environment = createScientificEnvironment().toMutableMap()
        
        // 공학 상수들
        environment["GRAVITY"] = 9.80665 // m/s² (표준 중력가속도)
        environment["ATMOSPHERIC_PRESSURE"] = 101325.0 // Pa (표준 대기압)
        environment["WATER_DENSITY"] = 1000.0 // kg/m³ (물의 밀도, 4°C)
        environment["AIR_DENSITY"] = 1.225 // kg/m³ (공기 밀도, 15°C, 1 atm)
        environment["SOUND_SPEED_AIR"] = 343.0 // m/s (공기 중 음속, 20°C)
        environment["STEFAN_BOLTZMANN"] = 5.670374419e-8 // W/(m²⋅K⁴)
        
        return environment
    }
    
    /**
     * 통계 계산용 환경을 생성합니다.
     * 기본 환경에 통계 관련 상수들을 추가합니다.
     *
     * @return 통계 상수가 포함된 환경 맵
     */
    fun createStatisticalEnvironment(): Map<String, Any> {
        val environment = createBasicEnvironment().toMutableMap()
        
        // 통계 상수들
        environment["SQRT_2"] = sqrt(2.0)
        environment["SQRT_PI"] = sqrt(PI)
        environment["SQRT_2PI"] = sqrt(2 * PI)
        environment["LN_2"] = ln(2.0)
        environment["LN_10"] = ln(10.0)
        environment["LOG10_E"] = log10(E)
        environment["GOLDEN_RATIO"] = (1 + sqrt(5.0)) / 2
        environment["EULER_GAMMA"] = 0.5772156649015329
        
        // 확률 분포 관련 상수들
        environment["NORMAL_CONSTANT"] = 1.0 / sqrt(2 * PI)
        environment["CHI_SQUARE_CONSTANT"] = ln(2.0) / 2
        
        return environment
    }
    
    /**
     * 사용자 정의 환경을 생성합니다.
     * 기본 환경에 사용자가 제공한 변수들을 추가합니다.
     *
     * @param customVariables 사용자 정의 변수들
     * @param baseEnvironment 기반이 될 환경 (기본값: 기본 환경)
     * @return 사용자 정의 변수가 포함된 환경 맵
     */
    fun createCustomEnvironment(
        customVariables: Map<String, Any>,
        baseEnvironment: Map<String, Any> = createBasicEnvironment()
    ): Map<String, Any> {
        return baseEnvironment.toMutableMap().apply { 
            putAll(customVariables) 
        }
    }
    
    /**
     * 지정된 환경 타입에 따라 환경을 생성합니다.
     *
     * @param environmentType 환경 타입
     * @param customVariables 추가할 사용자 정의 변수들 (선택적)
     * @return 지정된 타입의 환경 맵
     */
    fun createEnvironment(
        environmentType: EnvironmentType,
        customVariables: Map<String, Any> = emptyMap()
    ): Map<String, Any> {
        val baseEnvironment = when (environmentType) {
            EnvironmentType.BASIC -> createBasicEnvironment()
            EnvironmentType.MATH -> createMathEnvironment()
            EnvironmentType.SCIENTIFIC -> createScientificEnvironment()
            EnvironmentType.ENGINEERING -> createEngineeringEnvironment()
            EnvironmentType.STATISTICAL -> createStatisticalEnvironment()
        }
        
        return if (customVariables.isNotEmpty()) {
            createCustomEnvironment(customVariables, baseEnvironment)
        } else {
            baseEnvironment
        }
    }
    
    /**
     * 환경 타입을 정의하는 열거형입니다.
     */
    enum class EnvironmentType {
        BASIC,
        MATH,
        SCIENTIFIC,
        ENGINEERING,
        STATISTICAL
    }
    
    /**
     * 사용 가능한 모든 상수들의 목록을 반환합니다.
     *
     * @return 상수명과 설명이 포함된 맵
     */
    fun getAvailableConstants(): Map<String, String> = mapOf(
        "PI" to "원주율 (3.14159...)",
        "E" to "자연상수 (2.71828...)",
        "GOLDEN_RATIO" to "황금비 (1.61803...)",
        "EULER_GAMMA" to "오일러-마스케로니 상수 (0.57721...)",
        "LIGHT_SPEED" to "광속 (299,792,458 m/s)",
        "PLANCK" to "플랑크 상수 (6.626... × 10⁻³⁴ J⋅s)",
        "AVOGADRO" to "아보가드로 수 (6.022... × 10²³ mol⁻¹)",
        "BOLTZMANN" to "볼츠만 상수 (1.380... × 10⁻²³ J/K)",
        "GAS_CONSTANT" to "기체상수 (8.314 J/(mol⋅K))",
        "GRAVITY" to "표준 중력가속도 (9.80665 m/s²)",
        "ATMOSPHERIC_PRESSURE" to "표준 대기압 (101,325 Pa)"
    )
    
    /**
     * 환경의 통계 정보를 반환합니다.
     *
     * @param environmentType 환경 타입
     * @return 통계 정보 맵
     */
    fun getEnvironmentInfo(environmentType: EnvironmentType): Map<String, Any> {
        val environment = createEnvironment(environmentType)
        return mapOf(
            "type" to environmentType.name,
            "constantCount" to environment.size,
            "constants" to environment.keys.sorted(),
            "description" to when (environmentType) {
                EnvironmentType.BASIC -> "기본 수학 상수들"
                EnvironmentType.MATH -> "수학 계산용 상수들"
                EnvironmentType.SCIENTIFIC -> "과학 계산용 상수들"
                EnvironmentType.ENGINEERING -> "공학 계산용 상수들"
                EnvironmentType.STATISTICAL -> "통계 계산용 상수들"
            }
        )
    }
}