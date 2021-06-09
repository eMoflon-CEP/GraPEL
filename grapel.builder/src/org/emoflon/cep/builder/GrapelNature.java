package org.emoflon.cep.builder;

import java.util.Collection;

import org.emoflon.ibex.gt.editor.ui.builder.GTNature;

import com.google.common.collect.Lists;

/**
 * Project Nature for GraPEL
 */
public class GrapelNature extends GTNature { //implements IProjectNature {
	/**
	 * NATURE_ID for GraPEL Projects
	 */
	public static final String NATURE_ID = "org.emoflon.cep.builder.nature"; // Override from GT to GraPEL

	/**
	 * Returns the list of required builders for the nature.
	 * @return the builder IDs
	 */
	public static Collection<String> getRequiredBuilders() {
		return Lists.newArrayList(GrapelBuilder.BUILDER_ID); // Override from GT to GraPEL
	}
	
}
