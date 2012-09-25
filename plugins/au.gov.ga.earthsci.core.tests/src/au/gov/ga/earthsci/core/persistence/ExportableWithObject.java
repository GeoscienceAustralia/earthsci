package au.gov.ga.earthsci.core.persistence;

@Exportable
public class ExportableWithObject
{
	@Persistant
	private ExportableWithAttribute exportableObject;

	public ExportableWithAttribute getExportableObject()
	{
		return exportableObject;
	}

	public void setExportableObject(ExportableWithAttribute exportableObject)
	{
		this.exportableObject = exportableObject;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithObject ewo = (ExportableWithObject) obj;
		return ewo.exportableObject.equals(exportableObject);
	}
}
