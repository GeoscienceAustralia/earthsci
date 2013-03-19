package au.gov.ga.earthsci.model.geometry;

import java.util.UUID;

import au.gov.ga.earthsci.core.util.IDescribed;
import au.gov.ga.earthsci.core.util.IDirtyable;
import au.gov.ga.earthsci.core.util.INamed;

/**
 * Represents a single geometry within a model (e.g. a single surface, volume, pointset etc.)
 * <p/>
 * Instances have a globally unique ID that allows them to be identified within a single application
 * session.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IModelGeometry extends INamed, IDescribed, IDirtyable
{

	/**
	 * Return the globally unique ID of this geometry.
	 * <p/>
	 * The result is guaranteed to be unique within a single application session, but should not
	 * be persisted.
	 * 
	 * @return The globally unique ID of this geometry unique within a single application session.
	 */
	UUID getUUID();

}
