package au.gov.ga.earthsci.model.ui.raster;

import javax.inject.Inject;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.gdal.gdal.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.IIntentHandler;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.model.IModel;
import au.gov.ga.earthsci.model.core.raster.GDALRasterModelParameters;
import au.gov.ga.earthsci.model.ui.raster.wizard.RasterModelParametersWizard;

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

	@Inject
	private Shell parentShell;

	@Override
	public void handle(final Intent intent, final IIntentCallback callback)
	{
		logger.debug("Handling GDAL model parameter collection intent"); //$NON-NLS-1$

		final Dataset dataset = (Dataset) intent.getExtra("dataset");
		final GDALRasterModelParameters params = new GDALRasterModelParameters(dataset);

		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				WizardDialog dialog = new WizardDialog(parentShell, new RasterModelParametersWizard(dataset, params));
				dialog.setPageSize(400, 400);
				dialog.open();

				if (dialog.getReturnCode() == WizardDialog.OK)
				{
					callback.completed(params, intent);
				}
				else
				{
					callback.aborted(intent);
				}
			}
		});
	}
}
