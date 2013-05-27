package au.gov.ga.earthsci.model.ui.raster.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.gdal.gdal.Dataset;

import au.gov.ga.earthsci.common.ui.color.ColorMapEditor;
import au.gov.ga.earthsci.model.core.raster.GDALRasterModel;
import au.gov.ga.earthsci.model.core.raster.GDALRasterModelParameters;

/**
 * A wizard page that allows configuration of colour options for a
 * {@link GDALRasterModel}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class RasterModelColorPage extends AbstractRasterModelPage
{

	private ColorMapEditor editor;

	protected RasterModelColorPage(Dataset dataset, GDALRasterModelParameters params)
	{
		super(dataset, params, "Colour", "Configure how the model is coloured");
	}

	@Override
	void addContents(Composite container)
	{
		editor = new ColorMapEditor(container, SWT.NONE);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 100;
		gd.heightHint = 100;
		editor.setLayoutData(gd);
	}

	@Override
	void validate()
	{
		// TODO Auto-generated method stub

	}

	@Override
	void bind()
	{
		// TODO Auto-generated method stub

	}

}
