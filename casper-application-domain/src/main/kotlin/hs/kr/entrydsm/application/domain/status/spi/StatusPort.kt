package hs.kr.entrydsm.application.domain.status.spi

import hs.kr.entrydsm.application.domain.application.spi.ApplicationCommandStatusPort
import hs.kr.entrydsm.application.domain.application.spi.ApplicationQueryStatusPort

interface StatusPort : ApplicationQueryStatusPort, ApplicationCommandStatusPort