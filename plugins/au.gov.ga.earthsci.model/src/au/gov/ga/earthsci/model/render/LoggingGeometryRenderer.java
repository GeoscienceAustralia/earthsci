package au.gov.ga.earthsci.model.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.model.geometry.IModelGeometry;

/**
 * A diagnostic / debug implementation of the {@link IModelGeometryRenderer}
 * that simply logs geometry information to the standard debug logger.
 * <p/>
 * Important: This renderer performs no graphical rendering of geometry.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class LoggingGeometryRenderer implements IModelGeometryRenderer
{

	private static final Logger logger = LoggerFactory.getLogger(LoggingGeometryRenderer.class);

	private IModelGeometry geometry;

	public LoggingGeometryRenderer(IModelGeometry geometry)
	{
		this.geometry = geometry;
	}

	@Override
	public void render()
	{
		if (geometry == null)
		{
			logger.debug("Rendered geometry: {}", geometry); //$NON-NLS-1$
		}
		else
		{
			logger.debug("Rendered geometry: {} ({})", geometry.getName(), geometry.getId()); //$NON-NLS-1$
		}
	}

	@Override
	public IModelGeometry getGeometry()
	{
		return geometry;
	}

}
