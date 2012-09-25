package au.gov.ga.earthsci.core.persistence;

import java.util.Arrays;

@Exportable
public class ExportableWithArray
{
	@Persistant
	private double[] array;

	public double[] getArray()
	{
		return array;
	}

	public void setArray(double[] array)
	{
		this.array = array;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithArray ewa = (ExportableWithArray) obj;
		return Arrays.equals(ewa.array, array);
	}
}
