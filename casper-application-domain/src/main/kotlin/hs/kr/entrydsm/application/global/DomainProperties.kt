package hs.kr.entrydsm.application.global

import hs.kr.entrydsm.application.global.exception.DomainExceptions
import java.util.*

object DomainProperties : Properties() {
    override fun getProperty(key: String): String {
        return super.getProperty(key) ?: throw DomainExceptions.NotInitializationProperties()
    }
}
