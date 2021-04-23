package org.emoflon.cep.ui.wizard

import com.google.inject.Inject

import org.eclipse.core.resources.IProject
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.resource.FileExtensionProvider
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import org.eclipse.core.resources.IFile

// TODO: This a test class. Needs to be updated to fit a better GraPEL Style
/**
 * Create the files with initial content.
 */
class GrapelNewFileInitalContents {
	@Inject
	FileExtensionProvider fileExtensionProvider

	/**
	 * Init project files.
	 */
	def generateInitialContents(IFileSystemAccess2 fsa, IProject project) {
		val packagePath = project.name.replace(".", "/")

		// Add a file with an example rule.
		fsa.generateFile(
			"src/" + packagePath + "/grapeLDefinition." + fileExtensionProvider.primaryFileExtension,
			this.getFileContent("examplePattern")
		)

		// Add a .gitignore
		fsa.generateFile(
			".gitignore",
			'''
				/bin/
				/src-gen/
			'''
		)
	}

	/**
	 * Generate an example .grpl file content based on the given parameters.
	 */
	def String getFileContent(String metaModel, String ruleName, String ruleBody) {
		'''
			import "«metaModel»"
			
			pattern «ruleName»() {
				«ruleBody»
			}
		'''
	}

	/**
	 * Generate an Ecore example .grpl file content.
	 */
	def String getFileContent(String ruleName) {
		this.getFileContent("http://www.eclipse.org/emf/2002/Ecore", ruleName, "object: EObject")
	}

	/**
	 * Init the file with example content.
	 */
	def void initFileContent(IFile file) {
		val fileName = file.getName()
		val ruleName = fileName.substring(0, 1).toLowerCase() + fileName.substring(1, fileName.length() - 3)
		val content = new ByteArrayInputStream(this.getFileContent(ruleName).getBytes(StandardCharsets.UTF_8))
		file.setContents(content, true, true, null)
	}
}