package au.gov.ga.earthsci.model.data;

import java.nio.ByteBuffer;

import org.unitsofmeasurement.unit.Unit;

import au.gov.ga.earthsci.common.buffer.BufferType;
import au.gov.ga.earthsci.common.util.IDescribed;
import au.gov.ga.earthsci.common.util.IIdentifiable;
import au.gov.ga.earthsci.common.util.INamed;

/**
 * Represents a single piece of data in a model geometry. This may be 'geometry'
 * data (vertices, texture coordinates etc.) or 'physical' data (temperature,
 * fluid flow rates etc.), or any other type of data that may be logically
 * associated with a geometry.
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
	 * Used in conjunction with {@link #getBufferType()} to give access to
	 * underlying source data
	 * 
	 * @return The source buffer for this instance
	 */
	ByteBuffer getSource();

	/**
	 * Return the type of data stored in the buffer
	 * 
	 * @return the type of data stored in the buffer
	 */
	BufferType getBufferType();

	/**
	 * Return the number of values contained in this data
	 * 
	 * @return the number of values contained in the data
	 */
	int getNumberOfValues();

	/**
	 * Return the group size of values in this data
	 * <p/>
	 * For example, if the data holds vertex information, group size would be 3
	 * indicating that values should be grouped into 3-tuples ({@code [x,y,z]}).
	 * 
	 * @return the group size of values in the data
	 */
	int getGroupSize();

	/**
	 * Return the number of groups contained in the data.
	 * <p/>
	 * This is a convenience method and is usually derived from the number of
	 * values and group size.
	 * 
	 * @return The number of groups contained in the data.
	 */
	int getNumberOfGroups();

	/**
	 * Return the units the data is expressed in, or <code>null</code> if there
	 * are none (e.g. greyscale intensity values).
	 * 
	 * @return the units the data is expressed in, or <code>null</code> if there
	 *         are none.
	 */
	// TODO: Should units be returned in the an array that matches group size so 
	// a unit can be assigned per-value in the tuple? 
	Unit<?> getUnits();

	/**
	 * Return whether the data has units associated with it.
	 * 
	 * @return <code>true</code> if there are units associated with this data;
	 *         <code>false</code> otherwise.
	 */
	boolean hasUnits();
}
