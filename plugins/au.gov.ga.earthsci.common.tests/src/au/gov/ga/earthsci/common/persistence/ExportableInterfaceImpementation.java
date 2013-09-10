package au.gov.ga.earthsci.common.persistence;

@Exportable
public class ExportableInterfaceImpementation implements NonExportableInterface
{
	private String name = "name value";

	@Persistent
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableInterfaceImpementation eii = (ExportableInterfaceImpementation) obj;
		return eii.name.equals(name);
	}
}
