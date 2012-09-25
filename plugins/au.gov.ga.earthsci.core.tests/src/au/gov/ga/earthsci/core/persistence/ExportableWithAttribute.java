package au.gov.ga.earthsci.core.persistence;

@Exportable
public class ExportableWithAttribute
{
	@Persistant(attribute = true)
	private int attribute;

	public int getAttribute()
	{
		return attribute;
	}

	public void setAttribute(int attribute)
	{
		this.attribute = attribute;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithAttribute ewa = (ExportableWithAttribute) obj;
		return ewa.attribute == attribute;
	}
}
