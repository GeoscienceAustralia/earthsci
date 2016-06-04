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
package au.gov.ga.earthsci.bookmark.properties.exaggeration;

import au.gov.ga.earthsci.bookmark.Messages;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.common.persistence.Exportable;
import au.gov.ga.earthsci.common.persistence.Persistent;

/**
 * A bookmark property that contains information on vertical exaggeration
 * 
 * @author Michael de Hoog
 */
@Exportable
public class ExaggerationProperty implements IBookmarkProperty
{
	public static final String TYPE = "au.gov.ga.earthsci.bookmark.properties.exaggeration"; //$NON-NLS-1$

	/** The vertical exaggeration */
	@Persistent
	private double exaggeration = 1d;

	/**
	 * For DI use only
	 */
	public ExaggerationProperty()
	{
	}

	public ExaggerationProperty(double exaggeration)
	{
		this.exaggeration = exaggeration;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public String getName()
	{
		return Messages.ExaggerationProperty_Name;
	}

	public double getExaggeration()
	{
		return exaggeration;
	}

	public void setExaggeration(double exaggeration)
	{
		this.exaggeration = exaggeration;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString()
	{
		return "ExaggerationProperty {type: " + TYPE + ", exaggeration: " + exaggeration + "}";
	}
}
