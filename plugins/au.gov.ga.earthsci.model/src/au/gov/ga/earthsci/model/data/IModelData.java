package au.gov.ga.earthsci.model.data;

import java.util.UUID;

import org.unitsofmeasurement.unit.Unit;

import au.gov.ga.earthsci.core.util.IDescribed;
import au.gov.ga.earthsci.core.util.IDirtyable;
import au.gov.ga.earthsci.core.util.INamed;

/**
 * Represents a single piece of data in a model geometry. This may be 'geometry' data (vertices,
 * texture coordinates etc.) or 'physical' data (temperature, fluid flow rates etc.), or any other
 * type of data that may be logically associated with a geometry.
 * 
 * <p/>
 * 
 * @param T The type of data accessed by this instance (Float, Integer etc.)
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IModelData<T> extends INamed, IDescribed, IDirtyable
{
	/**
	 * Return the globally unique ID for this model data, unique across a single application session.
	 * <p/>
	 * The result is guaranteed to be unique for a single application session, but should not be
	 * persisted between sessions.
	 * 
	 * @return The unique ID for this model data
	 */
	UUID getUUID();
	
	/**
	 * Return the value used to represent 'no data'. May be <code>null</code>.
	 * 
	 * @return The no-data value for this instance
	 */
	T getNoDataValue();
	
	/**
	 * Return the data source for this instance.
	 * 
	 * @return The data source for this instance
	 */
	IModelDataSource<T> getSource();
	
	/**
	 * Return the units the data is expressed in, or <code>null</code> if there are none 
	 * (e.g. greyscale intensity values).
	 * 
	 * @return the units the data is expressed in, or <code>null</code> if there are none.
	 */
	Unit<?> getUnit();
	
}
