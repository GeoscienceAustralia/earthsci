package au.gov.ga.earthsci.model.data;

import java.nio.Buffer;
import java.util.Iterator;

/**
 * The underlying data source within an {@link IModelData} instance.
 * <p/>
 * Supports grouped access to the data to cluster logically related data together. This can be
 * used, for example, to access the 4 elements of a vector at once.
 * 
 * @param T The type of data accessed by this data source 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IModelDataSource<T>
{
	
	/**
	 * Return an iterator that iterates over each value in the data source,
	 * ignoring value grouping etc.
	 * 
	 * @return An iterator that iterators over each value in the data source
	 * and that ignores data grouping.
	 */
	Iterator<T> getValueIterator();
	
	/**
	 * Return an iterator that iterates over each group in the data source.
	 * <p/>
	 * Groups can be used to return logically related data items at once (for example,
	 * return the 4 elements of a vector grouped together).
	 * 
	 * @return An iterator that iterates over each group in the data source.
	 */
	Iterator<T[]> getGroupIterator();
	
	/**
	 * Return the size of groups returned by the group iterator.
	 * <p/>
	 * If the data source does not use grouping, 
	 * @return
	 */
	int getGroupSize();
	
	/**
	 * Return a {@link Buffer} view of this data source. This may be the underlying
	 * 'backing' buffer, or a created 'view' of the data, as appropriate.
	 * 
	 * @return a Buffer containing this data source's data.
	 */
	Buffer getBuffer();
	
}
