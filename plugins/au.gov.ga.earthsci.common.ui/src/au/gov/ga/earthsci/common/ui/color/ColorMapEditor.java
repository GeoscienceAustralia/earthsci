/*******************************************************************************
 * Copyright 2013 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.common.ui.color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.color.ColorMap.InterpolationMode;
import au.gov.ga.earthsci.common.color.ColorMaps;
import au.gov.ga.earthsci.common.color.MutableColorMap;
import au.gov.ga.earthsci.common.ui.viewers.NamedLabelProvider;

/**
 * A widget that allows for the editing of a {@link ColorMap}, using a gradient
 * editor.
 * <p/>
 * The editor can be initialised with a {@link ColorMap} instance that will be
 * used to seed configuration values. {@link ColorMap} instances can be created
 * from the editor using {@link #createColorMap()}.
 * <p/>
 * <b>Supported Styles</b>
 * <dl>
 * <dt>{@link SWT#BORDER}</dt>
 * <dd>Apply a border around the editor</dd>
 * <dt>{@link SWT#VERTICAL}</dt>
 * <dd>Orient the gradient editor vertically</dd>
 * </dl>
 * 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ColorMapEditor extends Composite
{

	private double minDataValue = 0.0;
	private double maxDataValue = 1.0;
	private boolean hasDataValues = false;

	private MutableColorMap map;

	private List<Marker> markers = new ArrayList<Marker>();
	private Color[] colors;

	private Composite gradientAreaContainer;
	private Label minText;
	private Label maxText;

	private Composite gradientContainer;
	private Canvas gradientCanvas;

	private Composite optionsContainer;
	private ComboViewer modeCombo;
	private Button percentageBasedButton;

	/**
	 * Create a new {@link ColorMap} editor widget with a default seed map.
	 * 
	 * @param parent
	 *            The parent composite for the editor
	 * 
	 * @param style
	 *            The style to apply to this editor
	 */
	public ColorMapEditor(Composite parent, int style)
	{
		this(ColorMaps.getRGBRainbowMap(), parent, style);
	}


	/**
	 * Create a new {@link ColorMap} editor widget with the given seed map.
	 * <p/>
	 * The created map will be forced to use percentage values. To provide the
	 * option to use absolute values, use the constructor
	 * {@link #ColorMapEditor(ColorMap, Double, Double, Composite, int)} and
	 * provide the data value range.
	 * 
	 * @param seed
	 *            The seed map
	 * @param parent
	 *            The parent composite for the editor
	 * @param style
	 *            The style to apply to this editor
	 */
	public ColorMapEditor(ColorMap seed, Composite parent, int style)
	{
		this(seed, null, null, parent, style);
	}

	/**
	 * Create a new {@link ColorMap} editor widget with the given seed map and
	 * optional data value range.
	 * <p/>
	 * If a data value range is provided, the user will be able to create a map
	 * whose entries are absolute values (rather than percentages).
	 * 
	 * @param seed
	 *            The see map to base the editor on
	 * @param minDataValue
	 *            The minimum data value to use when an absolute value colour
	 *            map is used
	 * @param maxDataValue
	 *            The maximum data value to use when an absolute value colour
	 *            map is used
	 * @param parent
	 *            The parent composite for the editor
	 * @param style
	 *            The style to apply to this editor
	 */
	public ColorMapEditor(ColorMap seed, Double minDataValue, Double maxDataValue, Composite parent, int style)
	{
		super(parent, style);
		setLayout(new GridLayout(2, false));

		map = new MutableColorMap(seed);

		if (minDataValue != null && maxDataValue != null)
		{
			hasDataValues = true;
			this.minDataValue = minDataValue;
			this.maxDataValue = maxDataValue;
		}

		addGradientArea();
		addOptionsArea();
	}

	/**
	 * Build the options editing
	 */
	private void addOptionsArea()
	{
		optionsContainer = new Composite(this, SWT.BORDER);
		optionsContainer.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL));

		optionsContainer.setLayout(new GridLayout(2, false));

		Label modeLabel = new Label(optionsContainer, SWT.NONE);
		modeLabel.setText("Mode:");
		modeCombo = new ComboViewer(optionsContainer, SWT.DROP_DOWN);
		modeCombo.setContentProvider(ArrayContentProvider.getInstance());
		modeCombo.setInput(InterpolationMode.values());
		modeCombo.setLabelProvider(new NamedLabelProvider());
		modeCombo.setSelection(new StructuredSelection(map.getMode()));
		modeCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				InterpolationMode newMode =
						(InterpolationMode) ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (newMode != map.getMode())
				{
					map.setMode(newMode);
					populateColors();
					gradientCanvas.redraw();
				}
			}
		});

		percentageBasedButton = new Button(optionsContainer, SWT.CHECK);
		percentageBasedButton.setText("Use percentages");
		percentageBasedButton.setSelection(map.isPercentageBased());
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		percentageBasedButton.setLayoutData(gd);
	}


	/**
	 * Build the gradient edit area
	 */
	private void addGradientArea()
	{


		gradientAreaContainer = new Composite(this, SWT.NONE);
		gradientAreaContainer.setLayout(new GridLayout(1, true));
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = 40;
		gradientAreaContainer.setLayoutData(gd);

		minText = new Label(gradientAreaContainer, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 30;
		minText.setLayoutData(gd);
		minText.setText("" + minDataValue);
		minText.setAlignment(SWT.CENTER);

		gradientContainer = new Composite(gradientAreaContainer, SWT.BORDER);
		gradientContainer.setLayout(new FillLayout());
		gd = new GridData(GridData.FILL_BOTH);
		gradientContainer.setLayoutData(gd);

		maxText = new Label(gradientAreaContainer, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 30;
		maxText.setLayoutData(gd);
		maxText.setText("" + maxDataValue);
		maxText.setAlignment(SWT.CENTER);

		gradientCanvas = new Canvas(gradientContainer, SWT.NONE);
		gradientCanvas.setBackground(JFaceColors.getActiveHyperlinkText(getDisplay()));

		gradientCanvas.addControlListener(new ControlAdapter()
		{
			@Override
			public void controlResized(ControlEvent e)
			{
				populateColors();
			}
		});

		gradientCanvas.addListener(SWT.Paint, new Listener()
		{
			@Override
			public void handleEvent(Event e)
			{
				paintGradient(e.gc, e.display);
				paintMarkers(e.gc, e.display);
			}
		});

		for (Entry<Double, Color> entry : map.getEntries().entrySet())
		{
			Marker marker = new Marker(entry.getKey(), entry.getValue());
			markers.add(marker);
		}

		Collections.sort(markers);

		gradientCanvas.layout();
		populateColors();
	}

	private void populateColors()
	{
		Color[] newColors = new Color[gradientCanvas.getBounds().height];

		double minValue = getMinValue();
		double maxValue = getMaxValue();

		for (int i = 0; i < newColors.length; i++)
		{
			double pixelValue = ((double) i / newColors.length) * (maxValue - minValue) + minValue;
			Color color = map.getColor(pixelValue, minValue, maxValue);
			newColors[i] = color;
		}

		colors = newColors;
	}

	private void paintGradient(GC gc, Display display)
	{
		Rectangle rect = gradientCanvas.getBounds();

		// Allow colors array to be changed mid-render without locking
		Color[] paintColors = colors;

		// TODO: I suspect this is a bad way of doing this... optimise based on 
		// interp mode - we should be able to seriously decrease the number of 
		// colours created etc. when eg. nearest_match is used 
		org.eclipse.swt.graphics.Color swtColor = null;
		org.eclipse.swt.graphics.Color backgroundColor = gradientCanvas.getBackground();
		for (int pixel = 0; pixel < rect.height; pixel++)
		{
			Color paintColor = paintColors[pixel];
			if (paintColor != null)
			{
				int red;
				int green;
				int blue;

				if (paintColor.getAlpha() < 255)
				{
					// Do alpha pre-multiplication as SWT colors don't support an alpha channel
					float alpha = paintColor.getAlpha() / 255.0f;

					red = (int) (paintColor.getRed() * alpha) + (int) (backgroundColor.getRed() * (1 - alpha));
					green = (int) (paintColor.getGreen() * alpha) + (int) (backgroundColor.getGreen() * (1 - alpha));
					blue = (int) (paintColor.getBlue() * alpha) + (int) (backgroundColor.getBlue() * (1 - alpha));
				}
				else
				{
					red = paintColor.getRed();
					green = paintColor.getGreen();
					blue = paintColor.getBlue();
				}

				swtColor = new org.eclipse.swt.graphics.Color(display, red, green, blue);
			}
			else
			{
				swtColor = gradientCanvas.getBackground();
			}

			gc.setForeground(swtColor);
			gc.drawLine(rect.x, pixel, rect.width, pixel);

			if (paintColor != null)
			{
				swtColor.dispose();
			}
		}
	}

	private void paintMarkers(GC gc, Display display)
	{
		// TODO
	}

	/**
	 * Create a new {@link ColorMap} instance from the configuration captured in
	 * this editor.
	 * 
	 * @return A new {@link ColorMap} instance created from the configuration
	 *         captured in this editor.
	 */
	public ColorMap createColorMap()
	{
		return null;
	}

	private double getMinValue()
	{
		if (map.isPercentageBased() || !hasDataValues)
		{
			return 0.0;
		}
		return minDataValue;
	}

	private double getMaxValue()
	{
		if (map.isPercentageBased() || !hasDataValues)
		{
			return 1.0;
		}
		return maxDataValue;
	}

	/**
	 * Represents a single marker in the color gradient
	 */
	private class Marker implements Comparable<Marker>
	{
		public double value;
		public Color color;

		public Marker(double value, Color color)
		{

		}

		@Override
		public int compareTo(Marker o)
		{
			return (int) (value - o.value);
		}
	}

	@Override
	public void dispose()
	{
		super.dispose();
	}
}
