package au.gov.ga.earthsci.model.render;

import au.gov.ga.earthsci.model.geometry.IModelGeometry;

/**
 * An interface for classes that can render a single {@link IModelGeometry} instance
 *  
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IModelGeometryRenderer
{
	
	/**
	 * Render the geometry associated with this instance.
	 */
	void render();

	/**
	 * Return the geometry this renderer is associated with.
	 * 
	 * @return The geometry this renderer is associated with
	 */
	IModelGeometry getGeometry();
}