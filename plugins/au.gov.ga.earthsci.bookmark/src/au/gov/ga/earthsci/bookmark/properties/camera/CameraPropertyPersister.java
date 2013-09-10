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
package au.gov.ga.earthsci.bookmark.properties.camera;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.bookmark.IBookmarkPropertyCreator;
import au.gov.ga.earthsci.bookmark.IBookmarkPropertyExporter;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.common.persistence.PersistenceException;
import au.gov.ga.earthsci.common.persistence.Persister;
import au.gov.ga.earthsci.common.util.Validate;
import au.gov.ga.earthsci.common.util.XmlUtil;
import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;

/**
 * An {@link IBookmarkPropertyCreator} that can create a {@link CameraProperty}
 * instance
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CameraPropertyPersister implements IBookmarkPropertyCreator, IBookmarkPropertyExporter
{
	private static final String CAMERA_ELEMENT_NAME = "camera"; //$NON-NLS-1$

	private static final String[] SUPPORTED_TYPES = new String[] { CameraProperty.TYPE };

	private static final Logger logger = LoggerFactory.getLogger(CameraPropertyPersister.class);

	private Persister persister;

	public CameraPropertyPersister()
	{
		persister = new Persister();
		persister.setIgnoreMissing(false);
		persister.setIgnoreNulls(false);
		persister.registerNamedExportable(CameraProperty.class, CAMERA_ELEMENT_NAME);
	}

	@Override
	public String[] getSupportedTypes()
	{
		return SUPPORTED_TYPES;
	}

	@Override
	public IBookmarkProperty createFromCurrentState(String type)
	{
		View view = WorldWindowRegistry.INSTANCE.getActiveView();
		if (view == null)
		{
			return null;
		}

		Position eyePosition = view.getCurrentEyePosition();

		Vec4 center = view.getCenterPoint();
		Globe globe = view.getGlobe();
		Position lookatPosition = globe.computePositionFromPoint(center);

		Vec4 upVector = view.getUpVector();

		return new CameraProperty(eyePosition, lookatPosition, upVector);
	}

	@Override
	public void exportToXML(IBookmarkProperty property, Element propertyElement)
	{
		if (property == null)
		{
			return;
		}
		Validate.isTrue(property.getType().equals(CameraProperty.TYPE),
				"CameraPropertyPersister can only be used for camera properties"); //$NON-NLS-1$
		Validate.notNull(propertyElement, "A property element is required"); //$NON-NLS-1$
		try
		{
			persister.save(property, propertyElement, null);
		}
		catch (PersistenceException e)
		{
			logger.error("Exception while saving camera bookmark property", e); //$NON-NLS-1$
		}
	}

	@Override
	public IBookmarkProperty createFromXML(String type, Element propertyElement)
	{
		Validate.isTrue(CameraProperty.TYPE.equals(type),
				"CameraPropertyPersister can only be used for camera properties"); //$NON-NLS-1$
		if (propertyElement == null)
		{
			return null;
		}
		try
		{
			return (CameraProperty) persister.load(
					XmlUtil.getChildElementByTagName(0, CAMERA_ELEMENT_NAME, propertyElement), null);
		}
		catch (PersistenceException e)
		{
			logger.error("Exception while loading camera bookmark property", e); //$NON-NLS-1$
		}
		return null;
	}
}
