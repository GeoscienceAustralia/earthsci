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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import au.gov.ga.earthsci.application.widgets.PositionEditor;
import au.gov.ga.earthsci.application.widgets.PositionEditor.PositionChangedEvent;
import au.gov.ga.earthsci.application.widgets.PositionEditor.PositionEditorListener;
import au.gov.ga.earthsci.application.widgets.Vec4Editor;
import au.gov.ga.earthsci.application.widgets.Vec4Editor.Vec4ChangedEvent;
import au.gov.ga.earthsci.application.widgets.Vec4Editor.Vec4EditorListener;
import au.gov.ga.earthsci.bookmark.BookmarkPropertyFactory;
import au.gov.ga.earthsci.bookmark.model.IBookmark;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.bookmark.properties.camera.CameraProperty;
import au.gov.ga.earthsci.bookmark.ui.editor.IBookmarkEditorMessage.Level;

/**
 * An {@link IBookmarkPropertyEditor} used for viewing/editing a
 * {@link CameraProperty} associated with a {@link IBookmark}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CameraPropertyEditor extends AbstractBookmarkPropertyEditor
{
	private static final String EYE_POSITION_FIELD = "camera.eye"; //$NON-NLS-1$
	private static final String LOOKAT_POSITION_FIELD = "camera.lookat"; //$NON-NLS-1$
	private static final String UP_VECTOR_FIELD = "camera.up"; //$NON-NLS-1$
	private static final String INVALID_POSITION_ERROR = "camera.position.invalid"; //$NON-NLS-1$
	protected static final String INVALID_VEC4_ERROR = "camera.up.invalid"; //$NON-NLS-1$

	private Composite container;

	private PositionEditor eyePositionEditor;
	private PositionEditor lookatPositionEditor;
	private Vec4Editor upVectorEditor;

	@Override
	public void okPressed()
	{
		CameraProperty property = (CameraProperty) getProperty();
		if (property == null || container == null)
		{
			return;
		}

		property.setEyePosition(eyePositionEditor.getPositionValue());
		property.setLookatPosition(lookatPositionEditor.getPositionValue());
		property.setUpVector(upVectorEditor.getVec4Value());
	}

	@Override
	protected IBookmarkProperty createPropertyFromCurrent()
	{
		return BookmarkPropertyFactory.createProperty(CameraProperty.TYPE);
	}

	@Override
	protected void fillFieldsFromProperty(IBookmarkProperty property)
	{
		if (property != null)
		{
			eyePositionEditor.setPositionValue(((CameraProperty) property).getEyePosition());
			lookatPositionEditor.setPositionValue(((CameraProperty) property).getLookatPosition());
			upVectorEditor.setVec4Value(((CameraProperty) property).getUpVector());
		}
		else
		{
			eyePositionEditor.setPositionValue(null);
			lookatPositionEditor.setPositionValue(null);
			upVectorEditor.setVec4Value(null);
		}
	}

	@Override
	public Control createControl(Composite parent)
	{
		container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));

		CameraProperty camera = (CameraProperty) getProperty();

		eyePositionEditor =
				addPositionEditor(camera == null ? null : camera.getEyePosition(), EYE_POSITION_FIELD,
						Messages.CameraPropertyEditor_EyePositionLabel,
						Messages.CameraPropertyEditor_InvalidEyePositionMessage);

		lookatPositionEditor =
				addPositionEditor(camera == null ? null : camera.getLookatPosition(), LOOKAT_POSITION_FIELD,
						Messages.CameraPropertyEditor_LookatPositionLabel,
						Messages.CameraPropertyEditor_InvalidLookatPositionMessage);

		upVectorEditor =
				addVec4Editor(camera == null ? null : camera.getUpVector(), UP_VECTOR_FIELD,
						Messages.CameraPropertyEditor_UpVectorLabel,
						Messages.CameraPropertyEditor_InvalidUpVectorMessage);
		return container;
	}

	private PositionEditor addPositionEditor(final Position pos, final String fieldId, final String labelText,
			final String invalidMessage)
	{
		Label label = new Label(container, SWT.NONE);
		label.setText(labelText);
		label.setFont(JFaceResources.getBannerFont());

		PositionEditor editor = new PositionEditor(container, SWT.NONE);
		if (pos != null)
		{
			editor.setPositionValue(pos);
		}
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		editor.setLayoutData(gd);
		editor.addPositionEditorListener(new PositionEditorListener()
		{
			@Override
			public void positionChanged(PositionChangedEvent e)
			{
				validate(fieldId, e.isValid(), new BookmarkEditorMessage(Level.ERROR, INVALID_POSITION_ERROR,
						invalidMessage));
			}
		});

		return editor;
	}

	private Vec4Editor addVec4Editor(final Vec4 vec, final String fieldId, final String labelText,
			final String invalidMessage)
	{
		Label label = new Label(container, SWT.NONE);
		label.setText(labelText);
		label.setFont(JFaceResources.getBannerFont());

		Vec4Editor editor = new Vec4Editor(container, SWT.NONE);
		if (vec != null)
		{
			editor.setVec4Value(vec);
		}

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		editor.setLayoutData(gd);
		editor.addVec4EditorListener(new Vec4EditorListener()
		{
			@Override
			public void vec4Changed(Vec4ChangedEvent e)
			{
				validate(fieldId, e.isValid(), new BookmarkEditorMessage(Level.ERROR, INVALID_VEC4_ERROR,
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
		return Messages.CameraPropertyEditor_EditorTitle;
	}

	@Override
	public String getDescription()
	{
		return Messages.CameraPropertyEditor_EditorDescription;
	}
}
