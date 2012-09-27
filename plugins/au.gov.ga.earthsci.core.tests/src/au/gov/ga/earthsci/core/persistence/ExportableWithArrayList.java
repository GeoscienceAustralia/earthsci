package au.gov.ga.earthsci.core.persistence;

import java.util.ArrayList;

@Exportable
public class ExportableWithArrayList
{
	@Persistant
	private ArrayList<Integer> arrayList = new ArrayList<Integer>();

	public ArrayList<Integer> getArrayList()
	{
		return arrayList;
	}

	public void setArrayList(ArrayList<Integer> arrayList)
	{
		this.arrayList = arrayList;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithArrayList ewc = (ExportableWithArrayList) obj;
		return ewc.arrayList.equals(arrayList);
	}
}
