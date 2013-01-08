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
package au.gov.ga.earthsci.bookmark.part.editor;

import gov.nasa.worldwind.geom.Position;

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
import au.gov.ga.earthsci.bookmark.BookmarkPropertyFactory;
import au.gov.ga.earthsci.bookmark.part.editor.IBookmarkEditorMessage.Level;
import au.gov.ga.earthsci.bookmark.properties.camera.CameraProperty;

/**
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CameraPropertyEditor extends AbstractBookmarkPropertyEditor
{
	private static final String EYE_POSITION_FIELD = "camera.eye";
	private static final String LOOKAT_POSITION_FIELD = "camera.lookat";
	private static final String INVALID_POSITION_ERROR = "camera.position.invalid";

	private Composite container;

	private PositionEditor eyePositionEditor;
	private PositionEditor lookatPositionEditor;
	
	@Override
	public void okPressed()
	{
		CameraProperty property = (CameraProperty)getProperty();
		if (property == null)
		{
			return;
		}
		
		property.setEyePosition(eyePositionEditor.getPositionValue());
		property.setLookatPosition(lookatPositionEditor.getPositionValue());
	}

	@Override
	public void restoreOriginalValues()
	{
		fillEditorsFromProperty((CameraProperty)getProperty());
	}

	@Override
	public void fillFromCurrent()
	{
		CameraProperty current = (CameraProperty) BookmarkPropertyFactory.createProperty(CameraProperty.TYPE);
		fillEditorsFromProperty(current);
	}
	
	private void fillEditorsFromProperty(CameraProperty property)
	{
		if (property != null)
		{
			eyePositionEditor.setPositionValue(property.getEyePosition());
			lookatPositionEditor.setPositionValue(property.getLookatPosition());
		}
		else
		{
			eyePositionEditor.setPositionValue(null);
			lookatPositionEditor.setPositionValue(null);
		}
	}
	
	@Override
	public Control createControl(Composite parent)
	{
		container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));
		
		CameraProperty camera = (CameraProperty)getProperty();
		
		eyePositionEditor = addPositionEditor(camera == null ? null : camera.getEyePosition(), 
											  EYE_POSITION_FIELD, 
											  "Eye position", 
											  "Invalid eye position");
		
		lookatPositionEditor = addPositionEditor(camera == null ? null : camera.getLookatPosition(),
												 LOOKAT_POSITION_FIELD,
												 "Lookat position",
												 "Invalid lookat position");
		
		return container;
	}

	private PositionEditor addPositionEditor(final Position pos, final String fieldId, final String labelText, final String invalidMessage)
	{
		Label label = new Label(container, SWT.BOLD);
		label.setText(labelText);
		label.setFont(JFaceResources.getBannerFont());
		
		PositionEditor editor = new PositionEditor(container, SWT.NONE);
		if (getProperty() != null)
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
				validate(fieldId, e.isValid(), new BookmarkEditorMessage(Level.ERROR, INVALID_POSITION_ERROR, invalidMessage));
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
		return "Camera";
	}
	
	@Override
	public String getDescription()
	{
		return "Edit camera properties (position, viewing direction etc.)";
	}
}
