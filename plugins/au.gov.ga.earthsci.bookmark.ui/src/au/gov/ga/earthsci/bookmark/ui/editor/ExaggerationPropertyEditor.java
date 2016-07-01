/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.bookmark.ui.editor;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import au.gov.ga.earthsci.application.widgets.DoubleEditor;
import au.gov.ga.earthsci.application.widgets.DoubleEditor.DoubleChangedEvent;
import au.gov.ga.earthsci.application.widgets.DoubleEditor.DoubleEditorListener;
import au.gov.ga.earthsci.bookmark.BookmarkPropertyFactory;
import au.gov.ga.earthsci.bookmark.model.IBookmark;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.bookmark.properties.exaggeration.ExaggerationProperty;
import au.gov.ga.earthsci.bookmark.ui.editor.IBookmarkEditorMessage.Level;

/**
 * An {@link IBookmarkPropertyEditor} used for viewing/editing a
 * {@link ExaggerationProperty} associated with a {@link IBookmark}.
 * 
 * @author Michael de Hoog
 */
public class ExaggerationPropertyEditor extends AbstractBookmarkPropertyEditor
{
	private static final String EXAGGERATION_FIELD = "exaggeration"; //$NON-NLS-1$
	private static final String INVALID_EXAGGERATION_ERROR = "exaggeration.invalid"; //$NON-NLS-1$

	private Composite container;

	private DoubleEditor exaggerationEditor;

	@Override
	public void okPressed()
	{
		ExaggerationProperty property = (ExaggerationProperty) getProperty();
		if (property == null || container == null)
		{
			return;
		}

		Double exaggeration = exaggerationEditor.getDoubleValue();
		if (exaggeration == null)
		{
			exaggeration = 1d;
		}
		property.setExaggeration(exaggeration);
	}

	@Override
	protected IBookmarkProperty createPropertyFromCurrent()
	{
		return BookmarkPropertyFactory.createProperty(ExaggerationProperty.TYPE);
	}

	@Override
	protected void fillFieldsFromProperty(IBookmarkProperty property)
	{
		if (property != null)
		{
			exaggerationEditor.setDoubleValue(((ExaggerationProperty) property).getExaggeration());
		}
		else
		{
			exaggerationEditor.setDoubleValue(null);
		}
	}

	@Override
	public Control createControl(Composite parent)
	{
		container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));

		ExaggerationProperty exaggeration = (ExaggerationProperty) getProperty();

		exaggerationEditor =
				addDoubleEditor(exaggeration == null ? null : exaggeration.getExaggeration(), EXAGGERATION_FIELD,
						Messages.ExaggerationPropertyEditor_ExaggerationLabel,
						Messages.ExaggerationPropertyEditor_InvalidMessage);
		return container;
	}

	private DoubleEditor addDoubleEditor(final Double value, final String fieldId, final String labelText,
			final String invalidMessage)
	{
		Label label = new Label(container, SWT.NONE);
		label.setText(labelText);
		label.setFont(JFaceResources.getBannerFont());

		DoubleEditor editor = new DoubleEditor(container, SWT.NONE);
		if (value != null)
		{
			editor.setDoubleValue(value);
		}
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		editor.setLayoutData(gd);
		editor.addDoubleEditorListener(new DoubleEditorListener()
		{
			@Override
			public void doubleChanged(DoubleChangedEvent e)
			{
				validate(fieldId, e.isValid(), new BookmarkEditorMessage(Level.ERROR, INVALID_EXAGGERATION_ERROR,
						invalidMessage));
			}
		});

		return editor;
	}

	@Override
	public Control getControl()
	{
		return container;
	}

	@Override
	public String getName()
	{
		return Messages.ExaggerationPropertyEditor_EditorName;
	}

	@Override
	public String getDescription()
	{
		return Messages.ExaggerationPropertyEditor_EditorDescription;
	}
}
