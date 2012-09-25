package au.gov.ga.earthsci.core.persistence;

import java.net.URI;

import org.w3c.dom.Element;

@Exportable
public class ExportableWithAdapter
{
	@Persistant
	@Adapter(AdaptableAdapter.class)
	private Adaptable adaptable;

	public Adaptable getAdaptable()
	{
		return adaptable;
	}

	public void setAdaptable(Adaptable adaptable)
	{
		this.adaptable = adaptable;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithAdapter ewa = (ExportableWithAdapter) obj;
		return ewa.adaptable.equals(adaptable);
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

	public static class AdaptableAdapter implements IPersistantAdapter<Adaptable>
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
