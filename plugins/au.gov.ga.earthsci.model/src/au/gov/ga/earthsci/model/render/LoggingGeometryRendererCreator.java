package au.gov.ga.earthsci.model.render;

import au.gov.ga.earthsci.model.geometry.IModelGeometry;

/**
 * An {@link IModelGeometryRendererCreator} used to crate instances of the
 * {@link LoggingGeometryRenderer}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class LoggingGeometryRendererCreator implements IModelGeometryRendererCreator
{

	@Override
	public String getName()
	{
		return "Debug logging renderer"; //$NON-NLS-1$
	}

	@Override
	public String getDescription()
	{
		return "Log geometry information to the debug output"; //$NON-NLS-1$
	}

	@Override
	public boolean supports(IModelGeometry geometry)
	{
		return true;
	}

	@Override
	public IModelGeometryRenderer createRenderer(IModelGeometry geometry) throws IllegalArgumentException
	{
		return new LoggingGeometryRenderer(geometry);
	}

}
