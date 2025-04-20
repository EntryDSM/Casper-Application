package hs.kr.entrydsm.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CasperApplication

fun main(args: Array<String>) {
    runApplication<CasperApplication>(*args)
}
