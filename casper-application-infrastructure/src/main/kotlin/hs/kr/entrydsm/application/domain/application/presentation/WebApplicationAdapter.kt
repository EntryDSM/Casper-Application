package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.usecase.*
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.*
import hs.kr.entrydsm.application.domain.file.presentation.converter.ImageFileConverter
import hs.kr.entrydsm.application.domain.file.presentation.dto.response.UploadImageWebResponse
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/application")
class WebApplicationAdapter(
    private val createApplicationUseCase: CreateApplicationUseCase,
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    private val getMyApplicationStatusUseCase: GetMyApplicationStatusUseCase,
) {
    @PostMapping
    fun createApplication() {
        createApplicationUseCase.execute()
    }

    @PostMapping("/photo")
    fun uploadFile(@RequestPart(name = "image") file: MultipartFile): UploadImageWebResponse {
        return UploadImageWebResponse(
            uploadPhotoUseCase.execute(
                file.let(ImageFileConverter::transferTo)
            )
        )
    }

//    @PostMapping("/final-submit")
//    fun submitApplicationFinal() = submitApplicationFinalUseCase.execute()

    @GetMapping("/status")
    fun getMyApplicationStatus(): GetApplicationStatusResponse = getMyApplicationStatusUseCase.execute()
}
