package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.usecase.FileUploadUseCase
import hs.kr.entrydsm.application.domain.file.presentation.converter.ImageFileConverter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/photo")
@RestController
class FileController(
    private val fileUploadUseCase: FileUploadUseCase
) {
    @PostMapping
    fun uploadPhoto(@RequestPart(name = "image") file: MultipartFile): ResponseEntity<Map<String, String>> {
        val photoUrl = fileUploadUseCase.execute(
            file.let(ImageFileConverter::transferTo)
        )
        return ResponseEntity.ok(mapOf("photo_url" to photoUrl))
    }
}