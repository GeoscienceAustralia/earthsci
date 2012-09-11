package au.gov.ga.earthsci.model;

public class DataModelImpl implements IDataModel
{
	@Override
	public String getWord()
	{
		return "Hello there from " + getClass();
	}
}
