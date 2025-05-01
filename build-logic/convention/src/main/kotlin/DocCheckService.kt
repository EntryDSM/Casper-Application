import org.gradle.api.Project
import java.io.File

/**
 * 문서화 검사를 실행하는 서비스 클래스입니다.
 * 프로젝트 내 Kotlin 소스 파일을 찾아 KDoc 주석 검사를 수행하고 결과를 보고합니다.
 */
class DocCheckService(
    private val project: Project
) {
    private val reporter = DocReporter(project.logger)
    
    /**
     * 특정 타입의 코드 요소에 대한 문서화 검사를 실행합니다.
     * 프로젝트 내 모든 Kotlin 소스 파일을 검사하고 결과를 보고합니다.
     *
     * @param element 검사할 코드 요소 타입
     * @return 검사 성공 여부 (true: 모든 요소에 KDoc 주석 있음, false: 주석 없는 요소 있음)
     */
    fun checkDocumentation(element: CodeElement): Boolean {
        reporter.reportStart(element)
        
        val sourceFiles = getKotlinSourceFiles()
        if (sourceFiles.isEmpty()) {
            project.logger.warn("코틀린 소스 파일을 찾을 수 없습니다.")
            return true
        }
        
        val allProblems = mutableListOf<DocumentationProblem>()
        sourceFiles.forEachIndexed { index, file ->
            reporter.reportProgress(index + 1, sourceFiles.size, file.name)
            
            val analyzer = CodeFileAnalyzer(
                filePath = file.absolutePath,
                fileContent = file.readText()
            )
            
            val problems = analyzer.findClasses()
            allProblems.addAll(problems)
        }
        
        if (allProblems.isEmpty()) {
            reporter.reportSuccess(element)
            return true
        } else {
            reporter.reportProblems(element, allProblems)
            return false
        }
    }
    
    /**
     * 프로젝트 내 모든 Kotlin 소스 파일을 찾아 반환합니다.
     *
     * @return Kotlin 소스 파일 목록
     */
    private fun getKotlinSourceFiles(): List<File> {
        val fileTree = project.fileTree(DocConstants.SRC_FOLDER) {
            include(DocConstants.KOTLIN_FILES)
        }
        
        return fileTree.files.toList()
    }
}
