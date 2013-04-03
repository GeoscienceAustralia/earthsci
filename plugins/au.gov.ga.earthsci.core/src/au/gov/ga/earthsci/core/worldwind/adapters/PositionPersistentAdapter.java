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
package au.gov.ga.earthsci.core.worldwind.adapters;

import gov.nasa.worldwind.geom.Position;

import java.net.URI;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.core.persistence.IPersistentAdapter;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * An {@link IPersistentAdapter} that can be used to persist {@link Position}
 * instances
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class PositionPersistentAdapter implements IPersistentAdapter<Position>
{

	private static final String ELEMENT_NAME = "position"; //$NON-NLS-1$

	@Override
	public void toXML(Position object, Element element, URI context)
	{
		XMLUtil.appendPosition(element, ELEMENT_NAME, object);

	}

	@Override
	public Position fromXML(Element element, URI context)
	{
		return XMLUtil.getPosition(element, ELEMENT_NAME, null);
	}

}
