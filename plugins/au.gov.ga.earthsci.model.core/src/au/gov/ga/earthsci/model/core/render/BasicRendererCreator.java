package au.gov.ga.earthsci.model.core.render;

import org.eclipse.e4.core.di.annotations.Creatable;

import au.gov.ga.earthsci.model.geometry.IModelGeometry;
import au.gov.ga.earthsci.model.geometry.IVertexBasedGeometry;
import au.gov.ga.earthsci.model.render.IModelGeometryRenderer;
import au.gov.ga.earthsci.model.render.IModelGeometryRendererCreator;
import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;

/**
 * A {@link IModelGeometryRendererCreator} that creates instances of the
 * {@link BasicRenderer}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
@Creatable
public class BasicRendererCreator implements IModelGeometryRendererCreator
{

	@Override
	public String getName()
	{
		return "Basic renderer";
	}

	@Override
	public String getDescription()
	{
		return "A basic renderer that supports points, lines and surfaces";
	}

	@Override
	public boolean supports(IModelGeometry geometry)
	{
		return geometry != null && geometry instanceof IVertexBasedGeometry;
	}

	@Override
	public IModelGeometryRenderer createRenderer(IModelGeometry geometry) throws IllegalArgumentException
	{
		return new BasicRenderer((IVertexBasedGeometry) geometry, WorldWindowRegistry.INSTANCE);
	}

}
