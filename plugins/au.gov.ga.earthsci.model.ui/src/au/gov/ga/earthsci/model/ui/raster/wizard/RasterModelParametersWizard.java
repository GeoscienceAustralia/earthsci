package au.gov.ga.earthsci.model.ui.raster.wizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.gdal.gdal.Dataset;

import au.gov.ga.earthsci.model.core.raster.GDALRasterModelParameters;

/**
 * A wizard used to collect {@link GDALRasterModelParameters} used for creating
 * a model instance from a raster dataset.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class RasterModelParametersWizard extends Wizard
{

	private Dataset dataset;
	private GDALRasterModelParameters params;

	/**
	 * Create a new wizard backed by the given parameters object.
	 * <p/>
	 * Fields will be initialised from the values contained on the parameters
	 * object.
	 * 
	 * @param dataset
	 *            The raster dataset parameters are being collected for
	 * @param params
	 *            The backing parameters. Values will be initialised from those
	 *            found on the object.
	 */
	public RasterModelParametersWizard(Dataset dataset, GDALRasterModelParameters params)
	{
		this.dataset = dataset;
		this.params = params;

		setWindowTitle(Messages.RasterModelParametersWizard_WizardTitle);
		setNeedsProgressMonitor(false);
	}

	@Override
	public void addPages()
	{
		addPage(new DatasetBandSelectPage(dataset, params));
		addPage(new ProjectionPage(params, dataset.GetProjection()));
		addPage(new ColorMapPage(params));
		addPage(new OtherInformationPage(params));
	}

	@Override
	public boolean performFinish()
	{
		for (IWizardPage page : getPages())
		{
			if (page instanceof AbstractWizardPage)
			{
				((AbstractWizardPage<?>) page).bind();
			}
		}
		return true;
	}

	/**
	 * Get the parameters object populated with collected values
	 */
	public GDALRasterModelParameters getRasterModelParams()
	{
		return params;
	}
}
