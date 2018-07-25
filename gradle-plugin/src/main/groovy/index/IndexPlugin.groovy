package index

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy

class IndexPlugin implements Plugin<Project> {

	private static String GENERATED_A_DIR = "generated/source/a"

	void apply(Project project) {

		// Add the extension object
		def extension = project.extensions.create('assets', IndexExtension)

		createTaskCopyAssets(project, extension)
		createTaskGenerateA(project, extension)

		project.afterEvaluate {
			project.configure(project) {
				generateA.dependsOn copyAssets
				compileJava.dependsOn generateA

				project.sourceSets*.java*.srcDir project.file("${project.buildDir}/$GENERATED_A_DIR")
			}
		}
	}

	private static void createTaskCopyAssets(Project project, IndexExtension extension) {

		Copy copyAssets = project.task('copyAssets', type: Copy)

		project.afterEvaluate {

			resolvePaths(project, extension)

			copyAssets.configure {
				doFirst {
					println "packaging: ${extension.srcAssets} -> ${extension.genAssets}"
				}
				from(extension.srcAssets)
				into(extension.genAssets)
			}
		}
	}

	private static void createTaskGenerateA(Project project, IndexExtension extension) {

		Task generateA = project.task('generateA')

		project.afterEvaluate {

			def assetDir = project.file(extension.genAssets)
			def generatedDir = project.file("${project.buildDir}/$GENERATED_A_DIR")
			def packageName = extension.packageName

			generateA.configure {
				inputs.dir(assetDir)
				outputs.dir(generatedDir)
				doLast {
					new IndexBuilder(assetDir, generatedDir, packageName, 'A').build()
				}
			}
		}
	}

	private static void resolvePaths(Project project, IndexExtension extension) {
		if (extension.srcAssets == null)
			extension.srcAssets = "${project.file('assets')}"
		if (extension.genAssets == null)
			extension.genAssets = "${project.buildDir}/generated/assets"
	}

}


class IndexExtension {
	String srcAssets = null
	String genAssets = null
	String packageName = ""
}
