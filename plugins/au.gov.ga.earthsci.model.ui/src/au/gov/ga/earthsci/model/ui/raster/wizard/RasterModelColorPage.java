package au.gov.ga.earthsci.model.ui.raster.wizard;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gdal.gdal.Dataset;

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.color.ColorMaps;
import au.gov.ga.earthsci.common.ui.color.ColorMapEditor;
import au.gov.ga.earthsci.common.ui.viewers.NamedLabelProvider;
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

	private ComboViewer colorMapSelector;
	private ColorMapEditor editor;

	protected RasterModelColorPage(Dataset dataset, GDALRasterModelParameters params)
	{
		super(dataset, params, "Colour", "Configure how the model is coloured");
	}

	@Override
	void addContents(Composite container)
	{
		Label mapSelectorLabel = new Label(container, SWT.NONE);
		mapSelectorLabel.setText("Choose a color map to start from:");

		colorMapSelector = new ComboViewer(container, SWT.DROP_DOWN);
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
	}

	@Override
	void validate()
	{
		// Do nothing
	}

	@Override
	void bind()
	{
		params.setColorMap(editor.createColorMap());
	}

}
