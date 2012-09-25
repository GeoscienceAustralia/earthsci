package au.gov.ga.earthsci.core.persistence;

import java.util.Arrays;

@Exportable
public class ExportableWithDoubleArray
{
	@Persistant
	private ExportableWithCollection[][] collectionArray;

	public ExportableWithCollection[][] getCollectionArray()
	{
		return collectionArray;
	}

	public void setCollectionArray(ExportableWithCollection[][] collectionArray)
	{
		this.collectionArray = collectionArray;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithDoubleArray ewda = (ExportableWithDoubleArray) obj;
		if (ewda.collectionArray.length != collectionArray.length)
			return false;
		for (int i = 0; i < collectionArray.length; i++)
		{
			if (!Arrays.equals(collectionArray[i], ewda.collectionArray[i]))
				return false;
		}
		return true;
	}
}
