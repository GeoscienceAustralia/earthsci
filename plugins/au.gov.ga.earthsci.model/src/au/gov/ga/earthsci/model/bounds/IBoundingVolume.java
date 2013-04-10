package au.gov.ga.earthsci.model.bounds;

import java.util.Collection;

import javax.swing.text.Position;

/**
 * An interface that represents a bounding volume for a model and/or it's
 * geometries.
 * <p/>
 * Implementations may use appropriate bounding shapes (spheres, prisms etc.) to
 * most closely match their enclosed geometries. Implementations may also decide
 * whether points at the volume boundary are considered to be within the volume
 * or not.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBoundingVolume
{
	/**
	 * Return whether the provided geographic position is included in this
	 * bounding volume.
	 * 
	 * @param point
	 *            The position to test
	 * 
	 * @return <code>true</code> if the provided position falls within this
	 *         bounding volume; <code>false</code> otherwise.
	 */
	boolean contains(Position point);

	/**
	 * Return whether <em>all</em> of the provided positions fall within this
	 * bounding volume.
	 * <p/>
	 * Note that (depending on the implementation) this may be more efficient
	 * than testing each point individually via multiple calls to
	 * {@link #contains(Position)}. In general it is safe to assume it will
	 * never be <em>less</em> efficient.
	 * 
	 * @param points
	 *            The positions to test
	 * 
	 * @return <code>true</code> if <em>all</em> of the provided points are
	 *         contained within the bounding volume; <code>false</code> if
	 *         <em>any</em> of the provided positions fall outside the volume.
	 */
	boolean containsAll(Position... points);

	/**
	 * See {@link #containsAll(Position...)}
	 * 
	 * @see #containsAll(Position...)
	 * @see #containsAny(Position...)
	 */
	boolean containsAll(Collection<Position> points);

	/**
	 * Return whether <em>any</em> of the provided positions fall within this
	 * bounding volume.
	 * <p/>
	 * Note that (depending on the implementation) this may be more efficient
	 * than testing each point individually via multiple calls to
	 * {@link #contains(Position)}. In general it is safe to assume it will
	 * never be <em>less</em> efficient.
	 * 
	 * @param points
	 *            The positions to test
	 * 
	 * @return <code>true</code> if <em>any</em> of the provided points are
	 *         contained within the bounding volume; <code>false</code> if
	 *         <em>all</em> of the provided positions fall outside the volume.
	 */
	boolean containsAny(Position... points);

	/**
	 * See {@link #containsAny(Position...)}
	 * 
	 * @see #containsAny(Position...)
	 * @see #containsAll(Position...)
	 */
	boolean containsAny(Collection<Position> points);
}
