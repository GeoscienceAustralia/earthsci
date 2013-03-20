package au.gov.ga.earthsci.model.render;

import au.gov.ga.earthsci.core.util.IDescribed;
import au.gov.ga.earthsci.core.util.INamed;
import au.gov.ga.earthsci.model.geometry.IModelGeometry;

/**
 * A creator interface that can create {@link IModelGeometryRenderer} instances for given
 * {@link IModelGeometry} instances.
 * 
 * <p/>
 * 
 * Provides a human-readable name and description of the renderer that can be used to populate UI
 * elements etc.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IModelGeometryRendererCreator extends INamed, IDescribed
{
	/**
	 * Return whether this creator can be used to create a renderer for the
	 * supplied geometry.
	 * 
	 * @param geometry The geometry a renderer is required for
	 * 
	 * @return <code>true</code> if this creator can be used to create a renderer
	 * for the supplied geometry; <code>false</code> otherwise.
	 */
	boolean supports(IModelGeometry geometry);

	/**
	 * Create and return a renderer to use for the given geometry.
	 * 
	 * @param geometry The geometry to create a renderer for
	 * 
	 * @return A new renderer to use for the provided geometry.
	 * 
	 * @throws IllegalArgumentException if this creator cannot be used for the provided geometry.
	 * 
	 * @see #supports(IModelGeometry)
	 */
	IModelGeometryRenderer createRenderer(IModelGeometry geometry) throws IllegalArgumentException;
}
