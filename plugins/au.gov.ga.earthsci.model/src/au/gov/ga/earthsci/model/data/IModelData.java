package au.gov.ga.earthsci.model.data;

import java.nio.Buffer;

import org.unitsofmeasurement.unit.Unit;

import au.gov.ga.earthsci.common.buffer.BufferType;
import au.gov.ga.earthsci.common.util.IDescribed;
import au.gov.ga.earthsci.common.util.IIdentifiable;
import au.gov.ga.earthsci.common.util.INamed;

/**
 * Represents a single piece of data in a model geometry. This may be 'geometry' data (vertices,
 * texture coordinates etc.) or 'physical' data (temperature, fluid flow rates etc.), or any other
 * type of data that may be logically associated with a geometry.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IModelData extends IIdentifiable, INamed, IDescribed
{
	/**
	 * Return the value used to represent 'no data'. May be <code>null</code>.
	 * <p/>
	 * The returned type will be 
	 * 
	 * @return The no-data value for this instance
	 */
	Object getNoDataValue();
	
	/**
	 * Return the data source buffer for this model data.
	 * <p/>
	 * The buffer may be cast into appropriate sub-classes of {@link Buffer}
	 * to access the appropriate data types from the buffer.
	 * 
	 * @return The source buffer for this instance
	 */
	Buffer getSource();
	
	/**
	 * Return the type of data stored in the buffer
	 * 
	 * @return the type of data stored in the buffer
	 */
	BufferType getBufferType();
	
	/**
	 * Return the units the data is expressed in, or <code>null</code> if there are none 
	 * (e.g. greyscale intensity values).
	 * 
	 * @return the units the data is expressed in, or <code>null</code> if there are none.
	 */
	Unit<?> getUnits();
	
	/**
	 * Return whether the data has units associated with it.
	 * 
	 * @return <code>true</code> if there are units associated with this data; 
	 * <code>false</code> otherwise.
	 */
	boolean hasUnits();
}
