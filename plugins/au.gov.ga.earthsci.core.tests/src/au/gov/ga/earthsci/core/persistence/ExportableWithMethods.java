package au.gov.ga.earthsci.core.persistence;

@Exportable
public class ExportableWithMethods
{
	@Persistant
	private int field;

	private double method;
	private float setterMethod;

	public int getField()
	{
		return field;
	}

	public void setField(int field)
	{
		this.field = field;
	}

	@Persistant
	public double getMethod()
	{
		return method;
	}

	public void setMethod(double method)
	{
		this.method = method;
	}

	@Persistant(setter = "setSetterMethodOther")
	public float getSetterMethod()
	{
		return setterMethod;
	}

	public void setSetterMethodOther(float setterMethod)
	{
		this.setterMethod = setterMethod;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithMethods ewm = (ExportableWithMethods) obj;
		return ewm.getField() == getField() && ewm.getMethod() == getMethod()
				&& ewm.getSetterMethod() == getSetterMethod();
	}
}
