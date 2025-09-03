package hs.kr.entrydsm.domain.status.interfaces

/**
 * 상태 관련 모든 계약을 통합하는 메인 계약 인터페이스입니다.
 * 
 * 상태 조회(Query)와 상태 변경(Command) 계약을 모두 상속받아
 * 상태 관련 모든 기능에 대한 단일 진입점을 제공합니다.
 */
interface StatusContract : ApplicationQueryStatusContract, ApplicationCommandStatusContract
