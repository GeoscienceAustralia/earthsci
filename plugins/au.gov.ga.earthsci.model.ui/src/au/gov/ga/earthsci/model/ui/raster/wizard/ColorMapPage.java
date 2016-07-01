package au.gov.ga.earthsci.model.ui.raster.wizard;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.color.ColorMaps;
import au.gov.ga.earthsci.common.ui.color.ColorMapEditor;
import au.gov.ga.earthsci.common.ui.util.SWTUtil;
import au.gov.ga.earthsci.common.ui.viewers.NamedLabelProvider;
import au.gov.ga.earthsci.model.core.parameters.IColorMapParameters;
import au.gov.ga.earthsci.model.core.raster.GDALRasterModel;

/**
 * A wizard page that allows configuration of colour options for a
 * {@link GDALRasterModel}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class ColorMapPage extends AbstractWizardPage<IColorMapParameters>
{
	private final boolean required;
	private final String[] properties;

	private Button useDefaultButton;
	private Button useColorMapButton;
	private ComboViewer propertySelector;
	private ComboViewer colorMapSelector;
	private ColorMapEditor editor;

	public ColorMapPage(IColorMapParameters params)
	{
		this(params, true, null);
	}

	public ColorMapPage(IColorMapParameters params, boolean required, String[] properties)
	{
		super(params, "Colour", "Configure how the model is coloured");
		this.required = required;
		this.properties = properties;
	}

	@Override
	void addContents(Composite container)
	{
		if (!required)
		{
			useDefaultButton = new Button(container, SWT.RADIO);
			useDefaultButton.setText("Use default colour(s)");
			GridData radiogd = new GridData(GridData.FILL_HORIZONTAL);
			radiogd.verticalIndent = 0;
			useDefaultButton.setLayoutData(radiogd);
			useDefaultButton.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					enableColorMap(false);
				}
			});

			useColorMapButton = new Button(container, SWT.RADIO);
			useColorMapButton.setText("Choose a colour map:");
			radiogd = new GridData(GridData.FILL_HORIZONTAL);
			radiogd.verticalIndent = 5;
			useColorMapButton.setLayoutData(radiogd);
			useColorMapButton.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					enableColorMap(true);
				}
			});
		}
		else
		{
			Label mapSelectorLabel = new Label(container, SWT.NONE);
			mapSelectorLabel.setText("Choose a colour map to start from:");
		}

		colorMapSelector = new ComboViewer(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		colorMapSelector.setContentProvider(ArrayContentProvider.getInstance());
		List<ColorMap> maps = ColorMaps.get();
		colorMapSelector.setInput(maps);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		colorMapSelector.getCombo().setLayoutData(gd);
		colorMapSelector.setSelection(new StructuredSelection(maps.get(0)));
		colorMapSelector.setLabelProvider(new NamedLabelProvider());
		colorMapSelector.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				ColorMap selectedMap = (ColorMap) ((IStructuredSelection) event.getSelection()).getFirstElement();
				editor.setSeed(selectedMap);
			}
		});

		editor = new ColorMapEditor(maps.get(0), container, SWT.NONE);

		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 100;
		gd.heightHint = 100;
		editor.setLayoutData(gd);

		if (properties != null)
		{
			Label propertiesLabel = new Label(container, SWT.NONE);
			propertiesLabel.setText("Coloured property (painted variable):");

			propertySelector = new ComboViewer(container, SWT.DROP_DOWN);
			propertySelector.setContentProvider(ArrayContentProvider.getInstance());
			propertySelector.setInput(properties);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			propertySelector.getCombo().setLayoutData(gd);
			propertySelector.setLabelProvider(new LabelProvider());
		}

		if (!required)
		{
			useDefaultButton.setSelection(true);
			enableColorMap(false);
		}
	}

	protected void enableColorMap(boolean enabled)
	{
		SWTUtil.setEnabled(editor, enabled);
		SWTUtil.setEnabled(colorMapSelector.getControl(), enabled);
		if (propertySelector != null)
		{
			SWTUtil.setEnabled(propertySelector.getControl(), enabled);
		}
	}

	@Override
	void validate()
	{
		// Do nothing
	}

	@Override
	public void bind()
	{
		params.setColorMap(useDefaultButton != null && useDefaultButton.getSelection() ? null : editor.createColorMap());
		if (propertySelector != null)
		{
			params.setPaintedVariable(propertySelector.getCombo().getText());
		}
	}

}
