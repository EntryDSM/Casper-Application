package hs.kr.entrydsm.application.domain.file.presentation

import hs.kr.entrydsm.application.domain.application.usecase.FileUploadUseCase
import hs.kr.entrydsm.application.domain.file.presentation.converter.ImageFileConverter
import hs.kr.entrydsm.application.domain.file.presentation.dto.response.UploadPhotoResponse
import hs.kr.entrydsm.application.global.document.file.FileApiDocument
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/photo")
@RestController
class FileController(
    private val fileUploadUseCase: FileUploadUseCase,
) : FileApiDocument {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    override fun uploadPhoto(
        @RequestPart(name = "image") file: MultipartFile,
    ): UploadPhotoResponse {
        val photoUrl =
            fileUploadUseCase.execute(
                file.let(ImageFileConverter::transferTo),
            )
        return UploadPhotoResponse(fileName = photoUrl)
    }
}
