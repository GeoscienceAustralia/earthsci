package au.gov.ga.earthsci.core.persistence;

import java.util.ArrayList;
import java.util.Collection;

@Exportable
public class ExportableWithCollection
{
	@Persistant
	private Collection<Integer> collection = new ArrayList<Integer>();

	public Collection<Integer> getCollection()
	{
		return collection;
	}

	public void setCollection(Collection<Integer> collection)
	{
		this.collection = collection;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithCollection ewc = (ExportableWithCollection) obj;
		return ewc.collection.equals(collection);
	}
}
