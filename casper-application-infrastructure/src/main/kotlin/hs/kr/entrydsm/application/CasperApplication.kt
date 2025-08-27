package hs.kr.entrydsm.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Casper Application 메인 클래스
 * 
 * 동적 수식 계산 시스템을 위한 Spring Boot 애플리케이션
 * equus-application의 하드코딩된 입시 계산 로직을 동적 수식으로 대체
 */
@SpringBootApplication
class CasperApplication

/**
 * 애플리케이션의 main 함수.
 * @param args 외부에서 전달되는 명령어 인자
 */
fun main(args: Array<String>) {
    runApplication<CasperApplication>(*args)
}