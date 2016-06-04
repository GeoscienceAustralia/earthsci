package au.gov.ga.earthsci.model.ui.raster.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;

import au.gov.ga.earthsci.model.core.raster.GDALRasterModelParameters;

/**
 * A wizard page that allows the user to select which raster band to use for
 * model generation
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class DatasetBandSelectPage extends AbstractWizardPage<GDALRasterModelParameters>
{
	private final Dataset dataset;
	private Text scaleField;
	private Text offsetField;
	private Combo bandDropdown;
	private Text subsampleField;

	protected DatasetBandSelectPage(Dataset dataset, GDALRasterModelParameters params)
	{
		super(params, Messages.RasterModelBandSelectPage_PageTitle,
				Messages.RasterModelBandSelectPage_PageDescription);
		this.dataset = dataset;
	}

	@Override
	protected void addContents(Composite container)
	{
		// Raster band select
		addRasterBandGroup(container);

		// Scale and offset
		addScaleOffsetGroup(container);

		// Subsample
		addSubsampleGroup(container);
	}

	private void addRasterBandGroup(Composite container)
	{
		Group rasterBandGroup = addGroup(Messages.RasterModelBandSelectPage_BandSelectGroupTitle,
				Messages.RasterModelBandSelectPage_BandSelectGroupDescription,
				container);

		Label bandLabel = new Label(rasterBandGroup, SWT.NONE);
		bandLabel.setText(Messages.RasterModelBandSelectPage_BandSelectDropdownLabel);

		bandDropdown = createBandDropdown(rasterBandGroup);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		bandDropdown.setLayoutData(gd);
	}

	private void addScaleOffsetGroup(Composite container)
	{
		Group scaleOffsetBandGroup = addGroup(Messages.RasterModelBandSelectPage_ScaleOffsetGroupTitle,
				Messages.RasterModelBandSelectPage_ScaleOffsetGroupDescription,
				container);

		Label scaleLabel = new Label(scaleOffsetBandGroup, SWT.NONE);
		scaleLabel.setText(Messages.RasterModelBandSelectPage_ScaleFieldLabel);

		scaleField = new Text(scaleOffsetBandGroup, SWT.SINGLE | SWT.BORDER);
		scaleField.setText(params.getScaleFactor() == null ? "" : params.getScaleFactor().toString()); //$NON-NLS-1$
		scaleField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		registerField(scaleField);

		Label offsetLabel = new Label(scaleOffsetBandGroup, SWT.NONE);
		offsetLabel.setText(Messages.RasterModelBandSelectPage_OffsetFieldLabel);

		offsetField = new Text(scaleOffsetBandGroup, SWT.SINGLE | SWT.BORDER);
		offsetField.setText(params.getOffset() == null ? "" : params.getOffset().toString()); //$NON-NLS-1$
		offsetField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		registerField(offsetField);
	}

	private void addSubsampleGroup(Composite container)
	{
		Group subsampleGroup =
				addGroup(
						Messages.RasterModelBandSelectPage_SubsampleGroupTitle,
						Messages.RasterModelBandSelectPage_SubsampleGroupDescription,
						container,
						1, true);

		Label subsampleLabel = new Label(subsampleGroup, SWT.NONE);
		subsampleLabel.setText(Messages.RasterModelBandSelectPage_SubsampleFactorLabel);

		subsampleField = new Text(subsampleGroup, SWT.BORDER);
		subsampleField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		registerField(subsampleField);
	}

	public Combo createBandDropdown(Composite parent)
	{
		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.SINGLE | SWT.READ_ONLY);

		for (int i = 1; i <= dataset.GetRasterCount(); i++)
		{
			Band band = dataset.GetRasterBand(i);
			if (band == null)
			{
				continue;
			}
			String entry =
					Messages.RasterModelBandSelectPage_BandDropdownPrefix + band.GetBand() + " " //$NON-NLS-1$
							+ band.GetDescription();
			combo.add(entry);
		}

		combo.select(params.getElevationBandIndex() - 1);

		return combo;
	}

	@Override
	public void bind()
	{
		params.setElevationBandIndex(bandDropdown.getSelectionIndex() + 1); // Bands are 1-indexed
		params.setScaleFactor(getDoubleOrNull(scaleField.getText()));
		params.setOffset(getDoubleOrNull(offsetField.getText()));
		params.setSubsample(getIntegerOrNull(subsampleField.getText()));
	}

	@Override
	protected void validate()
	{
		if (!isNumericOrEmpty(scaleField.getText()))
		{
			markInvalid(scaleField, Messages.RasterModelBandSelectPage_InvalidScaleMessage);
		}

		if (!isNumericOrEmpty(offsetField.getText()))
		{
			markInvalid(offsetField, Messages.RasterModelBandSelectPage_InvalidOffsetMessage);
		}

		if (!isIntegerOrEmpty(subsampleField.getText()))
		{
			markInvalid(subsampleField, Messages.RasterModelBandSelectPage_SubsampleFactorValidationMessage);
		}
	}
}
