package au.gov.ga.earthsci.model.ui.raster.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gdal.gdal.Dataset;

import au.gov.ga.earthsci.model.core.raster.GDALRasterModelParameters;

/**
 * A wizard page that allows the user to down-sample raster data before model
 * generation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class RasterModelSubSamplePage extends AbstractRasterModelPage
{

	protected RasterModelSubSamplePage(Dataset dataset, GDALRasterModelParameters params)
	{
		super(dataset, params, Messages.RasterModelSubSamplePage_PageTitle, Messages.RasterModelSubSamplePage_PageDescription);
	}

	private Text subsampleField;

	@Override
	void addContents(Composite container)
	{
		Group subsampleGroup =
				addGroup(
						Messages.RasterModelSubSamplePage_SubsampleGroupTitle,
						Messages.RasterModelSubSamplePage_SubsampleGroupDescription,
						container,
						1, true);

		Label subsampleLabel = new Label(subsampleGroup, SWT.NONE);
		subsampleLabel.setText(Messages.RasterModelSubSamplePage_SubsampleFactorLabel);

		subsampleField = new Text(subsampleGroup, SWT.BORDER);
		subsampleField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		registerField(subsampleField);
	}

	@Override
	void validate()
	{
		if (!isIntegerOrEmpty(subsampleField.getText()))
		{
			markInvalid(subsampleField, Messages.RasterModelSubSamplePage_SubsampleFactorValidationMessage);
		}
	}

	@Override
	void bind()
	{
		params.setSubsample(getIntegerOrNull(subsampleField.getText()));
	}

}
