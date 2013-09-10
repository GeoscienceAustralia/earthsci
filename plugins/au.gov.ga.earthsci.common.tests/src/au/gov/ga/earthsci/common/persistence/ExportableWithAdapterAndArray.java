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
package au.gov.ga.earthsci.common.persistence;

import java.net.URI;
import java.util.Arrays;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.common.persistence.IPersistentAdapter;

@Exportable
public class ExportableWithAdapterAndArray
{
	@Persistent
	@Adapter(AdaptableAdapter.class)
	private Adaptable[] adaptables;

	public Adaptable[] getAdaptables()
	{
		return adaptables;
	}

	public void setAdaptables(Adaptable[] adaptables)
	{
		this.adaptables = adaptables;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithAdapterAndArray ewa = (ExportableWithAdapterAndArray) obj;
		return Arrays.equals(ewa.adaptables, adaptables);
	}

	public static class Adaptable
	{
		private String value;

		public String getValue()
		{
			return value;
		}

		public void setValue(String value)
		{
			this.value = value;
		}

		@Override
		public boolean equals(Object obj)
		{
			Adaptable a = (Adaptable) obj;
			return a.value.equals(value);
		}
	}

	private static class AdaptableAdapter implements IPersistentAdapter<Adaptable>
	{
		@Override
		public void toXML(Adaptable object, Element element, URI context)
		{
			element.setAttribute("value", object.getValue());
		}

		@Override
		public Adaptable fromXML(Element element, URI context)
		{
			Adaptable adaptable = new Adaptable();
			adaptable.setValue(element.getAttribute("value"));
			return adaptable;
		}
	}
}
