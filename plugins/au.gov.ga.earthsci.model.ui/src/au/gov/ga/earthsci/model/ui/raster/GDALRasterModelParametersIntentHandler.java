package au.gov.ga.earthsci.model.ui.raster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.IIntentHandler;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.model.IModel;

/**
 * Intent handler that uses a wizard interface to collect parameters for use in
 * generation of {@link IModel} instances from GDAL rasters.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class GDALRasterModelParametersIntentHandler implements IIntentHandler
{

	private static final Logger logger = LoggerFactory.getLogger(GDALRasterModelParametersIntentHandler.class);

	@Override
	public void handle(Intent intent, IIntentCallback callback)
	{
		logger.debug("Handling GDAL model parameter collection intent"); //$NON-NLS-1$

		// TODO
		callback.completed(null, intent);
	}

}
