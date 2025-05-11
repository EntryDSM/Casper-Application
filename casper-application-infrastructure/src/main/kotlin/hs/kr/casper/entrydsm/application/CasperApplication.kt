package hs.kr.casper.entrydsm.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * CasperApplication 클래스.
 * 이 클래스는 Spring Boot 애플리케이션의 진입점 역할을 합니다.
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
