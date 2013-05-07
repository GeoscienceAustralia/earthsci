package au.gov.ga.earthsci.model.ui.raster.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gdal.gdal.Dataset;

import au.gov.ga.earthsci.common.util.Util;
import au.gov.ga.earthsci.model.core.raster.GDALRasterModelParameters;

/**
 * A wizard page for collecting additional information about a raster model
 * (name, description etc.)
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class RasterModelOtherInformationPage extends AbstractRasterModelPage
{

	protected RasterModelOtherInformationPage(Dataset dataset, GDALRasterModelParameters params)
	{
		super(dataset, params, Messages.RasterModelOtherInformationPage_PageTitle,
				Messages.RasterModelOtherInformationPage_PageDescription);
	}

	private Text nameField;
	private Text descriptionField;

	@Override
	void addContents(Composite container)
	{
		Group nameDescriptionGroup =
				addGroup(Messages.RasterModelOtherInformationPage_NameDescriptionGroupTitle,
						Messages.RasterModelOtherInformationPage_NameDescriptionGroupDescription,
						container);

		Label nameLabel = new Label(nameDescriptionGroup, SWT.NONE);
		nameLabel.setText(Messages.RasterModelOtherInformationPage_NameFieldLabel);

		nameField = new Text(nameDescriptionGroup, SWT.BORDER);
		nameField.setText(params.getModelName());
		nameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		registerField(nameField);

		Label descriptionLabel = new Label(nameDescriptionGroup, SWT.NONE);
		descriptionLabel.setText(Messages.RasterModelOtherInformationPage_DescriptionFieldLabel);

		descriptionField = new Text(nameDescriptionGroup, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		descriptionField.setText(params.getModelDescription());
		GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		gd.heightHint = 100;
		descriptionField.setLayoutData(gd);
		registerField(descriptionField);
	}

	@Override
	void validate()
	{
		// No validation required - all fields are optional
	}

	@Override
	void bind()
	{
		if (!Util.isEmpty(nameField.getText()))
		{
			params.setModelName(nameField.getText());
		}
		if (!Util.isEmpty(descriptionField.getText()))
		{
			params.setModelDescription(descriptionField.getText());
		}
	}

}
