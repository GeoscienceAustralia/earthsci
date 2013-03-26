package au.gov.ga.earthsci.core.preferences;

import org.eclipse.swt.widgets.Composite;

/**
 * A field editor for adding space to a preference page.
 * 
 * @see http://www.eclipse.org/articles/Article-Field-Editors/field_editors.html
 */
public class SpacerFieldEditor extends LabelFieldEditor
{
	// Implemented as an empty label field editor.
	public SpacerFieldEditor(Composite parent)
	{
		super("", parent); //$NON-NLS-1$
	}
}
