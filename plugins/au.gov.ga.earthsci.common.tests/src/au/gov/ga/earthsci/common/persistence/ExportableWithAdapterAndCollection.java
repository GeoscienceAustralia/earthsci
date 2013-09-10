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
import java.util.Collection;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.common.persistence.IPersistentAdapter;

@Exportable
public class ExportableWithAdapterAndCollection
{
	@Persistent
	@Adapter(AdaptableAdapter.class)
	private Collection<Adaptable> adaptables;

	public Collection<Adaptable> getAdaptables()
	{
		return adaptables;
	}

	public void setAdaptables(Collection<Adaptable> adaptables)
	{
		this.adaptables = adaptables;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithAdapterAndCollection ewa = (ExportableWithAdapterAndCollection) obj;
		return ewa.adaptables.equals(adaptables);
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
