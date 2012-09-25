package au.gov.ga.earthsci.core.persistence;

@Exportable
public class ExportableWithNamedPersistant
{
	@Persistant(name = "namedField")
	private int field;

	public int getField()
	{
		return field;
	}

	public void setField(int field)
	{
		this.field = field;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithNamedPersistant ewnp = (ExportableWithNamedPersistant) obj;
		return ewnp.field == field;
	}
}
