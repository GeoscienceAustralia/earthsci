package au.gov.ga.earthsci.model.bounds;

import java.util.Collection;

import au.gov.ga.earthsci.common.math.vector.Vector3;

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
	 * Return whether the provided point is included in this bounding volume.
	 * 
	 * @param point
	 *            The point to test
	 * 
	 * @return <code>true</code> if the provided point falls within this
	 *         bounding volume; <code>false</code> otherwise.
	 */
	boolean contains(Vector3 point);

	/**
	 * Return whether <em>all</em> of the provided points fall within this
	 * bounding volume.
	 * <p/>
	 * Note that (depending on the implementation) this may be more efficient
	 * than testing each point individually via multiple calls to
	 * {@link #contains(point)}. In general it is safe to assume it will never
	 * be <em>less</em> efficient.
	 * 
	 * @param points
	 *            The points to test
	 * 
	 * @return <code>true</code> if <em>all</em> of the provided points are
	 *         contained within the bounding volume; <code>false</code> if
	 *         <em>any</em> of the provided points fall outside the volume.
	 */
	boolean containsAll(Vector3... points);

	/**
	 * See {@link #containsAll(Vector3...)}
	 * 
	 * @see #containsAll(Vector3...)
	 * @see #containsAny(Vector3...)
	 */
	boolean containsAll(Collection<Vector3> points);

	/**
	 * Return whether <em>any</em> of the provided points fall within this
	 * bounding volume.
	 * <p/>
	 * Note that (depending on the implementation) this may be more efficient
	 * than testing each point individually via multiple calls to
	 * {@link #contains(Vector3)}. In general it is safe to assume it will never
	 * be <em>less</em> efficient.
	 * 
	 * @param points
	 *            The points to test
	 * 
	 * @return <code>true</code> if <em>any</em> of the provided points are
	 *         contained within the bounding volume; <code>false</code> if
	 *         <em>all</em> of the provided points fall outside the volume.
	 */
	boolean containsAny(Vector3... points);

	/**
	 * See {@link #containsAny(Vector3...)}
	 * 
	 * @see #containsAny(Vector3...)
	 * @see #containsAll(Vector3...)
	 */
	boolean containsAny(Collection<Vector3> points);
}
