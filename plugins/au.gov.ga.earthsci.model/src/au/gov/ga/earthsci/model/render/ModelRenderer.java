package au.gov.ga.earthsci.model.render;

import org.eclipse.e4.core.di.annotations.Creatable;

import au.gov.ga.earthsci.model.IModel;
import au.gov.ga.earthsci.model.geometry.IModelGeometry;

/**
 * A render service used to render a given model
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Creatable
public class ModelRenderer
{
	/**
	 * Render the provided model and all of its geometries.
	 * 
	 * @param model The model to render
	 */
	public void render(IModel model)
	{
		if (model == null)
		{
			return;
		}
		
		for (IModelGeometry geom : model.getGeometries())
		{
			if (geom == null || geom.getRenderer() == null)
			{
				continue;
			}
			geom.getRenderer().render();
		}
	}
	
}
