package hs.kr.entrydsm.global.configuration.interfaces

/**
 * 설정 변경을 감지하는 리스너 인터페이스입니다.
 *
 * 도메인 서비스들이 설정 변경에 실시간으로 반응할 수 있도록 합니다.
 *
 * @author kangeunchan
 * @since 2025.08.07
 */
interface ConfigurationChangeListener {
    
    /**
     * 설정이 변경되었을 때 호출됩니다.
     *
     * @param configType 변경된 설정 타입
     * @param oldConfig 이전 설정
     * @param newConfig 새로운 설정
     */
    fun onConfigurationChanged(
        configType: String,
        oldConfig: Any,
        newConfig: Any
    )
    
    /**
     * 설정 변경이 실패했을 때 호출됩니다.
     *
     * @param configType 변경 시도한 설정 타입
     * @param error 발생한 오류
     */
    fun onConfigurationChangeFailed(
        configType: String,
        error: Exception
    )
    
    /**
     * 리스너가 관심있는 설정 타입들을 반환합니다.
     */
    fun getInterestedConfigTypes(): Set<String>
}