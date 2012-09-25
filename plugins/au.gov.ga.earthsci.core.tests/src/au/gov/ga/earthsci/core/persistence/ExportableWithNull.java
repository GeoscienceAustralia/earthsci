package au.gov.ga.earthsci.core.persistence;

@Exportable
public class ExportableWithNull
{
	@Persistant
	private String string = null;

	public String getString()
	{
		return string;
	}

	public void setString(String string)
	{
		this.string = string;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithNull ewn = (ExportableWithNull) obj;
		if (ewn.string == string)
		{
			return true;
		}
		return ewn.string.equals(string);
	}
}
