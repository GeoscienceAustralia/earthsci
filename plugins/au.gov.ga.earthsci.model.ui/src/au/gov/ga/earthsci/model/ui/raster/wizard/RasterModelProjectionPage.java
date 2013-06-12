package au.gov.ga.earthsci.model.ui.raster.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.ogr.ogrConstants;
import org.gdal.osr.SpatialReference;

import au.gov.ga.earthsci.common.spatial.SpatialReferences.SpatialReferenceSummary;
import au.gov.ga.earthsci.common.ui.dialogs.SpatialReferenceSelectorDialog;
import au.gov.ga.earthsci.common.ui.util.SWTUtil;
import au.gov.ga.earthsci.common.util.Util;
import au.gov.ga.earthsci.model.core.raster.GDALRasterModelParameters;

/**
 * A wizard page that collects source projection information in the case where
 * the original raster dataset does not contain that information, or where the
 * user wishes to override the projection for some reason.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class RasterModelProjectionPage extends AbstractRasterModelPage
{
	private Button useExistingButton;
	private Button usePreDefinedButton;
	private Button useUserDefinedButton;

	private Composite preDefinedContainer;
	private Composite userDefinedContainer;

	private SpatialReferenceSummary selectedSummary = SpatialReferenceSummary.WGS84;
	private Text srsText;

	protected RasterModelProjectionPage(Dataset dataset, GDALRasterModelParameters params)
	{
		super(dataset, params, Messages.RasterModelProjectionPage_PageTitle, Messages.RasterModelProjectionPage_PageDescription);
	}

	@Override
	void addContents(final Composite container)
	{

		final Group group =
				addGroup(
						Messages.RasterModelProjectionPage_GroupTitle,
						Messages.RasterModelProjectionPage_GroupDescription,
						container,
						1,
						false);

		group.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL));


		if (hasSourceSRS())
		{
			addUseExistingOption(group);
		}

		addUsePreDefinedOption(group);
		addUseUserDefinedOption(group);

		initialiseButtonState();
	}

	private void initialiseButtonState()
	{
		if (hasSourceSRS())
		{
			useExistingButton.setSelection(true);
			disable(preDefinedContainer);
			disable(userDefinedContainer);
		}
		else
		{
			usePreDefinedButton.setSelection(true);
			enable(preDefinedContainer);
			disable(userDefinedContainer);
		}
	}

	private void addUseExistingOption(final Group group)
	{
		GridData radiogd = new GridData(GridData.FILL_HORIZONTAL);
		radiogd.verticalIndent = 5;

		useExistingButton = new Button(group, SWT.RADIO);
		useExistingButton.setText(Messages.RasterModelProjectionPage_UseExistingButtonText);
		useExistingButton.setFont(group.getFont());
		useExistingButton.setLayoutData(radiogd);
		useExistingButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (useExistingButton.getSelection())
				{
					disable(preDefinedContainer);
					disable(userDefinedContainer);
				}
				reValidate();
				group.layout(true, true);
			}
		});


		Label useExistingDescription = new Label(group, SWT.WRAP);
		useExistingDescription
				.setText(Messages.RasterModelProjectionPage_UseExistingDescription);
		useExistingDescription.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void addUsePreDefinedOption(final Group group)
	{
		usePreDefinedButton = new Button(group, SWT.RADIO);
		usePreDefinedButton.setText(Messages.RasterModelProjectionPage_UsePreDefinedButtonText);
		usePreDefinedButton.setFont(group.getFont());
		GridData radiogd = new GridData(GridData.FILL_HORIZONTAL);
		radiogd.verticalIndent = 5;
		usePreDefinedButton.setLayoutData(radiogd);
		usePreDefinedButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (usePreDefinedButton.getSelection())
				{
					enable(preDefinedContainer);
					disable(userDefinedContainer);
				}
				reValidate();
				group.layout(true, true);
			}
		});

		Label usePreDefinedDescription = new Label(group, SWT.WRAP);
		usePreDefinedDescription
				.setText(Messages.RasterModelProjectionPage_UsePreDefinedDescription);
		usePreDefinedDescription.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		preDefinedContainer = new Composite(group, SWT.NONE);
		preDefinedContainer.setLayout(new GridLayout(2, false));
		preDefinedContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button selectButton = new Button(preDefinedContainer, SWT.PUSH);
		selectButton.setText(Messages.RasterModelProjectionPage_SelectSRSButtonText);
		selectButton.setToolTipText(Messages.RasterModelProjectionPage_SelectSRSButtonTooltip);

		final Label selectedLabel = new Label(preDefinedContainer, SWT.NONE | SWT.BORDER);
		selectedLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		selectedLabel.setText(selectedSummary.toString());

		selectButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				SpatialReferenceSelectorDialog dialog = new SpatialReferenceSelectorDialog(getShell());
				dialog.open();
				if (dialog.getSelected() != null)
				{
					selectedSummary = dialog.getSelected();
				}
				selectedLabel.setText(selectedSummary.toString());
			}
		});
	}

	private void addUseUserDefinedOption(final Group group)
	{
		useUserDefinedButton = new Button(group, SWT.RADIO);
		useUserDefinedButton.setText(Messages.RasterModelProjectionPage_UseUserDefinedButtonText);
		useUserDefinedButton.setFont(group.getFont());
		GridData radiogd = new GridData(GridData.FILL_HORIZONTAL);
		radiogd.verticalIndent = 5;
		useUserDefinedButton.setLayoutData(radiogd);
		useUserDefinedButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (useUserDefinedButton.getSelection())
				{
					disable(preDefinedContainer);
					enable(userDefinedContainer);
				}
				reValidate();
				group.layout(true, true);
			}
		});

		Label useUserDefinedDescription = new Label(group, SWT.WRAP);
		useUserDefinedDescription
				.setText(Messages.RasterModelProjectionPage_UseUserDefinedDescription);
		useUserDefinedDescription.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		userDefinedContainer = new Composite(group, SWT.NONE);
		userDefinedContainer.setLayout(new GridLayout(1, false));
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		userDefinedContainer.setLayoutData(gd);

		srsText = new Text(userDefinedContainer, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		srsText.setLayoutData(gd);
		registerField(srsText);
	}

	private void enable(Composite container)
	{
		SWTUtil.setEnabled(container, true);
	}

	private void disable(Composite container)
	{
		SWTUtil.setEnabled(container, false);
	}

	private boolean hasSourceSRS()
	{
		return !Util.isEmpty(dataset.GetProjection());
	}

	@Override
	void validate()
	{
		if (!useUserDefinedButton.getSelection())
		{
			return;
		}

		gdal.PushErrorHandler("CPLQuietErrorHandler"); //$NON-NLS-1$
		SpatialReference ref = new SpatialReference();
		boolean valid;
		try
		{
			valid = ref.SetFromUserInput(srsText.getText().trim()) == ogrConstants.OGRERR_NONE;
		}
		catch (RuntimeException e)
		{
			valid = false;
		}
		if (!valid)
		{
			markInvalid(srsText, Messages.RasterModelProjectionPage_InvalidSRSMessage);
		}
		gdal.PopErrorHandler();
	}

	@Override
	void bind()
	{
		if (hasSourceSRS() && useExistingButton.getSelection())
		{
			params.setSourceProjection(dataset.GetProjection());
		}
		else if (usePreDefinedButton.getSelection())
		{
			params.setSourceProjection(selectedSummary.getEpsg());
		}
		else if (useUserDefinedButton.getSelection())
		{
			params.setSourceProjection(srsText.getText().trim());
		}
	}

}
