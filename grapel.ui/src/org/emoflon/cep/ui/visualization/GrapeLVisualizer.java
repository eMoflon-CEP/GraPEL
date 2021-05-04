package org.emoflon.cep.ui.visualization;

import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.emoflon.cep.grapel.EditorGTFile;
import org.emoflon.cep.grapel.Event;
import org.emoflon.cep.grapel.EventPattern;
import org.emoflon.ibex.gt.editor.gT.EditorPattern;
import org.emoflon.ibex.gt.editor.ui.visualization.GTPlantUMLGenerator;
import org.moflon.core.ui.visualisation.EMoflonPlantUMLGenerator;
import org.moflon.core.ui.visualisation.common.EMoflonDiagramTextProvider;

public class GrapeLVisualizer implements EMoflonDiagramTextProvider {
	
	@Override
	public boolean supportsEditor(IEditorPart editor) {
		try {
			return Optional.of(editor) //
					.flatMap(maybeCast(XtextEditor.class))
					.map(e -> e.getDocument().readOnly(res -> res.getContents().get(0)))
					.filter(e -> e instanceof EditorGTFile)
					.flatMap(maybeCast(EditorGTFile.class))
					.isPresent();
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean supportsSelection(ISelection selection) {
		return true;
	}

	@Override
	public String getDiagramBody(IEditorPart editor, ISelection selection) {
		Optional<EditorGTFile> file = this.loadFileFromEditor(editor);
		if (!file.isPresent()) {
			return EMoflonPlantUMLGenerator.emptyDiagram();
		}
		return visualizeSelection(selection, file.get());
	}
	
	/**
	 * Returns the visualization of the selection.
	 *
	 * @param selection
	 *            the selection
	 * @param patterns
	 *            the editor patterns
	 * @return the PlantUML code for the visualization
	 */
	private static String visualizeSelection(final ISelection selection, final EditorGTFile file) {
		org.emoflon.ibex.gt.editor.gT.EditorGTFile gtPatterns = (org.emoflon.ibex.gt.editor.gT.EditorGTFile)file;
		
		if (file.getEvents() == null && file.getEventPatterns() == null && gtPatterns.getPatterns() == null) {
			return GTPlantUMLGenerator.visualizeNothing();
		}
		
		if (file.getEvents().isEmpty() && file.getEventPatterns().isEmpty() && gtPatterns.getPatterns().isEmpty()) {
			return GTPlantUMLGenerator.visualizeNothing();
		}
		
		
		// Visualize selected event or event pattern
		Optional<EObject> object = determineSelection(selection, file);
		if (object.isPresent()) {
			if(object.get() instanceof Event) {
				try {
					return GrapeLPlantUMLGenerator.visualizeEvent((Event)object.get());
				} catch(Exception e) {
					return GTPlantUMLGenerator.visualizeNothing();
				}
			} else if(object.get() instanceof EventPattern) {
				try {
					return GrapeLPlantUMLGenerator.visualizeEventPattern((EventPattern)object.get());
				} catch(Exception e) {
					return GTPlantUMLGenerator.visualizeNothing();
				}
			} else if(object.get() instanceof EditorPattern) {
				try {
					return GTPlantUMLGenerator.visualizeSelectedPattern((EditorPattern)object.get());
				} catch(Exception e) {
					return GTPlantUMLGenerator.visualizeNothing();
				}
			}
		} else {
			//Visualize everything + dependencies in an abstract and compact fashion
			try {
				return GrapeLPlantUMLGenerator.visualizeDependencies(file);
			} catch(Exception e) {
				return GTPlantUMLGenerator.visualizeNothing();
			}
		}
		
		return EMoflonPlantUMLGenerator.emptyDiagram();
	}
	
	/**
	 * Checks whether there is a rule with the name being equal to the current
	 * selected text.
	 *
	 * @param selection
	 *            the current selection
	 * @param patterns
	 *            the patters
	 * @return an {@link Optional} for a {@link EditorPattern}
	 */
	private static Optional<EObject> determineSelection(final ISelection selection,
			final EditorGTFile file) {
		
		if (selection instanceof TextSelection) {
			TextSelection textSelection = (TextSelection) selection;
			// For the TextSelection documents start with line 0.
			int selectionStart = textSelection.getStartLine() + 1;
			int selectionEnd = textSelection.getEndLine() + 1;

			for (final Event event : file.getEvents()) {
				ICompositeNode object = NodeModelUtils.getNode(event);
				if (selectionStart >= object.getStartLine() && selectionEnd <= object.getEndLine()) {
					return Optional.of(event);
				}
			}
			
			for (final EventPattern eventpattern : file.getEventPatterns()) {
				ICompositeNode object = NodeModelUtils.getNode(eventpattern);
				if (selectionStart >= object.getStartLine() && selectionEnd <= object.getEndLine()) {
					return Optional.of(eventpattern);
				}
			}
			
			for (final EditorPattern pattern : file.getPatterns()) {
				ICompositeNode object = NodeModelUtils.getNode(pattern);
				if (selectionStart >= object.getStartLine() && selectionEnd <= object.getEndLine()) {
					return Optional.of(pattern);
				}
			}

		}
		return Optional.empty();
	}
	
	/**
	 * Loads the file from the given editor.
	 *
	 * @param editor
	 *            the editor
	 * @return an {@link Optional} for the {@link GraphTransformationFile}
	 */
	private Optional<EditorGTFile> loadFileFromEditor(final IEditorPart editor) {
		try {
			return Optional.of(editor) //
					.flatMap(maybeCast(XtextEditor.class))
					.map(e -> e.getDocument().readOnly(res -> res.getContents().get(0)))
					.flatMap(maybeCast(EditorGTFile.class));
		} catch (Exception e) {
			return Optional.empty();
		}
	}
	

}
