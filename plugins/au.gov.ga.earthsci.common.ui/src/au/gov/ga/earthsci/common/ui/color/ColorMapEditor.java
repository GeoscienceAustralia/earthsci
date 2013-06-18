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
import java.beans.PropertyChangeListener;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.color.ColorMap.InterpolationMode;
import au.gov.ga.earthsci.common.color.ColorMaps;
import au.gov.ga.earthsci.common.color.MutableColorMap;
import au.gov.ga.earthsci.common.ui.viewers.NamedLabelProvider;
import au.gov.ga.earthsci.common.ui.widgets.NumericTextField;

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
 * <dd>Orient the gradient editor vertically (default)</dd>
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

	private ColorRegistry colorRegistry;

	private List<Marker> markers = new ArrayList<Marker>();
	private Color[] colors;

	// Gradient area
	private Composite gradientAreaContainer;
	private Label minText;
	private Label maxText;

	private Composite gradientContainer;
	private Canvas gradientCanvas;

	// Marker area
	private Canvas markerCanvas;

	// Options area
	private Composite optionsContainer;

	// Mode dropdown
	private ComboViewer modeCombo;
	private Button percentageBasedButton;

	// Entries table
	private TableViewer entriesTable;

	// Entry editor
	private Color currentEntryColor;
	private Double currentEntryValue;
	private NumericTextField editorValueField;
	private ColorSelector editorColorField;
	private Scale editorAlphaScale;
	private NumericTextField editorAlphaField;

	// Add/Remove buttons
	private Button addEntryButton;
	private Button removeEntryButton;

	// Nodata editor
	private Button nodataCheckBox;
	private ColorSelector nodataColorField;
	private Scale nodataAlphaScale;
	private NumericTextField nodataAlphaField;

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
		// TODO: Support vertical / horizontal style

		super(parent, style);
		setLayout(new GridLayout(3, false));

		colorRegistry = new ColorRegistry(getDisplay());

		map = new MutableColorMap(seed);

		if (minDataValue != null && maxDataValue != null)
		{
			hasDataValues = true;
			this.minDataValue = minDataValue;
			this.maxDataValue = maxDataValue;
		}

		addUIElements();
		wireListeners();
	}

	//******************************************
	// Public API
	//******************************************

	/**
	 * Set the seed map used in this editor.
	 * <p/>
	 * This will reset the editor and remove all user edits, re-initialising
	 * with the new seed map.
	 * 
	 * @param seed
	 *            The seed map to base the editor on
	 */
	public void setSeed(ColorMap seed)
	{
		map.updateTo(seed);

		setCurrentEntry(null);
		entriesTable.setSelection(null);
		removeEntryButton.setEnabled(false);
		disableEntryEditor();
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
		return map.snapshot();
	}

	@Override
	public void dispose()
	{
		super.dispose();
	}

	//******************************************
	// Wire up listener behaviour
	//******************************************

	/**
	 * Wire all the listeners that coordinate refreshes and updates between the
	 * model and the various UI elements
	 */
	private void wireListeners()
	{
		wireMapListeners();
		wireTableListeners();
		wireMarkerListeners();
		wireGradientListeners();
	}

	private void wireMapListeners()
	{
		map.addPropertyChangeListener(MutableColorMap.COLOR_MAP_ENTRY_CHANGE_EVENT, new PropertyChangeListener()
		{
			@Override
			public void propertyChange(java.beans.PropertyChangeEvent evt)
			{
				Display.getCurrent().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						entriesTable.refresh();

						populateColors();
						gradientCanvas.redraw();
					}
				});
			}
		});

		map.addPropertyChangeListener(MutableColorMap.ENTRY_MOVED_EVENT, new PropertyChangeListener()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void propertyChange(java.beans.PropertyChangeEvent evt)
			{
				Entry<Double, Color> oldEntry = (Entry<Double, Color>) evt.getOldValue();
				Entry<Double, Color> newEntry = (Entry<Double, Color>) evt.getNewValue();
				if (currentEntryValue == null || oldEntry.getKey().equals(currentEntryValue))
				{
					currentEntryValue = newEntry.getKey();

					entriesTable.refresh();
					selectTableEntry(newEntry);
				}
			}
		});

		map.addPropertyChangeListener(MutableColorMap.ENTRY_ADDED_EVENT, new PropertyChangeListener()
		{

			@Override
			public void propertyChange(java.beans.PropertyChangeEvent evt)
			{
				@SuppressWarnings("unchecked")
				Entry<Double, Color> newEntry = (Entry<Double, Color>) evt.getNewValue();
				final Marker newMarker = addMarker(newEntry);

				Display.getCurrent().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						markerCanvas.redraw(newMarker.bounds.x,
								newMarker.bounds.y,
								newMarker.bounds.width,
								newMarker.bounds.height,
								true);
					};
				});
			}
		});

		map.addPropertyChangeListener(MutableColorMap.MODE_CHANGE_EVENT, new PropertyChangeListener()
		{
			@Override
			public void propertyChange(java.beans.PropertyChangeEvent evt)
			{
				Display.getCurrent().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						populateColors();
						gradientCanvas.redraw();

						modeCombo.setSelection(new StructuredSelection(map.getMode()));
					};
				});
			}
		});

		map.addPropertyChangeListener(MutableColorMap.NODATA_CHANGE_EVENT, new PropertyChangeListener()
		{
			@Override
			public void propertyChange(java.beans.PropertyChangeEvent evt)
			{
				updateNodataEditorFromMap();
			}
		});
	}

	private void wireTableListeners()
	{
		entriesTable.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				Entry<Double, Color> selection = getSelectedTableEntry();
				if (selection != null)
				{
					setCurrentEntry(selection);
					enableEntryEditor();

					selectMarkerByValue(currentEntryValue);

					removeEntryButton.setEnabled(true);
				}
			}
		});
	}

	private void wireGradientListeners()
	{
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
			}
		});
	}

	private void wireMarkerListeners()
	{
		markerCanvas.addListener(SWT.Paint, new Listener()
		{
			@Override
			public void handleEvent(Event e)
			{
				paintMarkers(e.gc, e.display, e.getBounds());
			}
		});

		MarkerMouseListener markerMouseListener = new MarkerMouseListener();
		markerCanvas.addMouseListener(markerMouseListener);
		markerCanvas.addMouseMoveListener(markerMouseListener);
	}

	//******************************************
	// GUI elements
	//******************************************

	private void addUIElements()
	{
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
		modeLabel.setText(Messages.ColorMapEditor_ModeLabel);
		modeCombo = new ComboViewer(optionsContainer, SWT.DROP_DOWN);
		modeCombo.setContentProvider(ArrayContentProvider.getInstance());
		modeCombo.setInput(InterpolationMode.values());
		modeCombo.setLabelProvider(new NamedLabelProvider());
		modeCombo.setSelection(new StructuredSelection(map.getMode()));

		final Label modeDescription = new Label(optionsContainer, SWT.WRAP);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.horizontalIndent = 10;
		modeDescription.setLayoutData(gd);
		modeDescription.setText(map.getMode().getDescription());
		modeDescription.setFont(JFaceResources.getFontRegistry().getItalic("default")); //$NON-NLS-1$

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
					modeDescription.setText(newMode.getDescription());
				}
			}
		});

		if (hasDataValues)
		{
			percentageBasedButton = new Button(optionsContainer, SWT.CHECK);
			percentageBasedButton.setText(Messages.ColorMapEditor_UsePercentagesLabel);
			percentageBasedButton.setSelection(map.isPercentageBased());
			gd = new GridData();
			gd.horizontalSpan = 2;
			percentageBasedButton.setLayoutData(gd);
			percentageBasedButton.addSelectionListener(new SelectionListener()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					map.setValuesArePercentages(percentageBasedButton.getSelection(),
							minDataValue,
							maxDataValue);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e)
				{
					map.setValuesArePercentages(percentageBasedButton.getSelection(),
							minDataValue,
							maxDataValue);
				}
			});
		}

		addEntryEditor(optionsContainer);
		addAddRemoveButtons(optionsContainer);
		addEntriesList(optionsContainer);
		addNodataEditor(optionsContainer);
	}

	/**
	 * Add the entry editor area. Allows users to edit:
	 * <ul>
	 * <li>Value
	 * <li>Colour
	 * <li>Transparency
	 * </ul>
	 * For a single selected entry in the colour map
	 */
	private void addEntryEditor(Composite parent)
	{
		Composite editorContainer = new Composite(parent, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		editorContainer.setLayoutData(gd);
		editorContainer.setLayout(new GridLayout(7, false));

		Label valueLabel = new Label(editorContainer, SWT.NONE);
		valueLabel.setText(Messages.ColorMapEditor_EntryValueLabel);
		editorValueField = new NumericTextField(editorContainer, SWT.SINGLE | SWT.BORDER);
		editorValueField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				map.moveEntry(currentEntryValue, editorValueField.getNumber().doubleValue());
				currentEntryValue = editorValueField.getNumber().doubleValue();
			}
		});

		Label colorLabel = new Label(editorContainer, SWT.NONE);
		colorLabel.setText(Messages.ColorMapEditor_EntryColorLabel);
		editorColorField = new ColorSelector(editorContainer);
		editorColorField.addListener(new IPropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent event)
			{
				updateCurrentEntryColor();
			}
		});

		Label alphaLabel = new Label(editorContainer, SWT.NONE);
		alphaLabel.setText(Messages.ColorMapEditor_EntryAlphaLabel);

		editorAlphaScale = new Scale(editorContainer, SWT.HORIZONTAL);
		editorAlphaScale.setMinimum(0);
		editorAlphaScale.setMaximum(255);
		editorAlphaScale.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		editorAlphaField = new NumericTextField(editorContainer, SWT.BORDER, false, false);
		editorAlphaField.setMinValue(0);
		editorAlphaField.setMaxValue(255);
		gd = new GridData();
		gd.widthHint = 35;
		editorAlphaField.setLayoutData(gd);

		editorAlphaScale.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				updateCurrentEntryColor();
				editorAlphaField.setNumber(editorAlphaScale.getSelection());
			}
		});

		editorAlphaField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				editorAlphaScale.setSelection(editorAlphaField.getNumber().intValue());
				updateCurrentEntryColor();
			}
		});

		disableEntryEditor();
	}

	private void addAddRemoveButtons(Composite parent)
	{
		Composite buttonContainer = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		buttonContainer.setLayoutData(gd);
		buttonContainer.setLayout(new GridLayout(2, false));

		addEntryButton = new Button(buttonContainer, SWT.PUSH);
		addEntryButton.setText(Messages.ColorMapEditor_AddEntryLabel);
		addEntryButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				addNewEntry();
			}
		});

		removeEntryButton = new Button(buttonContainer, SWT.PUSH);
		removeEntryButton.setText(Messages.ColorMapEditor_RemoveEntryLabel);
		removeEntryButton.setEnabled(false);
		removeEntryButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				removeCurrentEntry();
			}
		});
	}

	private void addEntriesList(Composite parent)
	{
		Composite tableContainer = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		tableContainer.setLayoutData(gd);
		TableColumnLayout layout = new TableColumnLayout();
		tableContainer.setLayout(layout);

		// Not sure why, but columns and column label providers only work when SWT.VIRTUAL is used
		entriesTable = new TableViewer(tableContainer, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		entriesTable.setContentProvider(ArrayContentProvider.getInstance());
		entriesTable.getTable().setHeaderVisible(true);
		entriesTable.getTable().setLinesVisible(true);
		entriesTable.setInput(map.getEntries().entrySet());

		TableViewerColumn valueColumn = new TableViewerColumn(entriesTable, SWT.NONE);
		valueColumn.getColumn().setText(Messages.ColorMapEditor_TableValueColumnLabel);
		valueColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element)
			{
				Double value = ((Entry<Double, Color>) element).getKey();
				String result = "" + value; //$NON-NLS-1$
				if (map.isPercentageBased())
				{
					result += " (" + (int) (value * 100) + "%)"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				return result;
			}
		});
		layout.setColumnData(valueColumn.getColumn(), new ColumnPixelData(80, false));

		TableViewerColumn colorNameColumn = new TableViewerColumn(entriesTable, SWT.NONE);
		colorNameColumn.getColumn().setText(Messages.ColorMapEditor_TableColorColumnLabel);
		colorNameColumn.setLabelProvider(new ColumnLabelProvider()
		{

			@Override
			public String getText(Object element)
			{
				Color color = getColorFromElement(element);
				return "RGBA(" + color.getRed() + ", " //$NON-NLS-1$ //$NON-NLS-2$
						+ color.getGreen() + ", " //$NON-NLS-1$
						+ color.getBlue() + ", " //$NON-NLS-1$
						+ color.getAlpha() + ")"; //$NON-NLS-1$
			}

			@Override
			public String getToolTipText(Object element)
			{
				return "#" + getColorKey(getColorFromElement(element)); //$NON-NLS-1$
			}

			@SuppressWarnings("unchecked")
			private Color getColorFromElement(Object element)
			{
				return ((Entry<Double, Color>) element).getValue();
			}

			private String getColorKey(Color color)
			{
				return Integer.toHexString(color.getRGB());
			}

		});
		layout.setColumnData(colorNameColumn.getColumn(), new ColumnWeightData(100, false));

		ColumnViewerToolTipSupport.enableFor(entriesTable, ToolTip.NO_RECREATE);
	}

	/**
	 * Add an editor area for changing the NODATA colour used
	 */
	private void addNodataEditor(Composite parent)
	{
		Composite editorContainer = new Composite(parent, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		editorContainer.setLayoutData(gd);
		editorContainer.setLayout(new GridLayout(7, false));

		nodataCheckBox = new Button(editorContainer, SWT.CHECK);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 7;
		nodataCheckBox.setLayoutData(gd);
		nodataCheckBox.setText(Messages.ColorMapEditor_NoDataOptionLabel);
		nodataCheckBox.setToolTipText(Messages.ColorMapEditor_NoDataOptionTooltip);
		nodataCheckBox.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				enableNodataEditor(nodataCheckBox.getSelection());
				updateNodataColorFromEditor();
			}
		});

		Label colorLabel = new Label(editorContainer, SWT.NONE);
		colorLabel.setText("Color:"); //$NON-NLS-1$
		nodataColorField = new ColorSelector(editorContainer);
		nodataColorField.addListener(new IPropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent event)
			{
				updateNodataColorFromEditor();
			}
		});
		nodataColorField.setColorValue(toRGB(Color.BLACK));

		Label alphaLabel = new Label(editorContainer, SWT.NONE);
		alphaLabel.setText("Alpha:"); //$NON-NLS-1$

		nodataAlphaScale = new Scale(editorContainer, SWT.HORIZONTAL);
		nodataAlphaScale.setMinimum(0);
		nodataAlphaScale.setMaximum(255);
		nodataAlphaScale.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nodataAlphaScale.setSelection(255);

		nodataAlphaField = new NumericTextField(editorContainer, SWT.BORDER, false, false);
		nodataAlphaField.setMinValue(0);
		nodataAlphaField.setMaxValue(255);
		gd = new GridData();
		gd.widthHint = 35;
		nodataAlphaField.setLayoutData(gd);
		nodataAlphaField.setNumber(255);

		nodataAlphaScale.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				updateNodataColorFromEditor();
				nodataAlphaField.setNumber(nodataAlphaScale.getSelection());
			}
		});

		nodataAlphaField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				nodataAlphaScale.setSelection(nodataAlphaField.getNumber().intValue());
				updateNodataColorFromEditor();
			}
		});

		updateNodataEditorFromMap();
	}

	/**
	 * Build the gradient edit area
	 */
	private void addGradientArea()
	{
		final int gradientWidth = 40;
		final int markerAreaWidth = 40;

		// Contains label-gradient+markers-label
		gradientAreaContainer = new Composite(this, SWT.NONE);
		gradientAreaContainer.setLayout(new GridLayout(1, false));
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = gradientWidth + markerAreaWidth;
		gradientAreaContainer.setLayoutData(gd);

		minText = new Label(gradientAreaContainer, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = gradientWidth;
		minText.setLayoutData(gd);
		minText.setText("" + minDataValue); //$NON-NLS-1$
		minText.setAlignment(SWT.CENTER);

		// Contains gradient-markers
		gradientContainer = new Composite(gradientAreaContainer, SWT.BORDER);
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		gradientContainer.setLayout(layout);
		gd = new GridData(GridData.FILL_BOTH);
		gradientContainer.setLayoutData(gd);

		maxText = new Label(gradientAreaContainer, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = gradientWidth;
		maxText.setLayoutData(gd);
		maxText.setText("" + maxDataValue); //$NON-NLS-1$
		maxText.setAlignment(SWT.CENTER);

		gradientCanvas = new Canvas(gradientContainer, SWT.BORDER | SWT.NO_BACKGROUND);
		gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = gradientWidth;
		gradientCanvas.setLayoutData(gd);

		markerCanvas = new Canvas(gradientContainer, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = markerAreaWidth;
		markerCanvas.setLayoutData(gd);

		gradientAreaContainer.layout();

		populateColors();
		populateMarkers();
	}

	//**********************************************
	// Selection changes
	//**********************************************

	private void setCurrentEntry(Entry<Double, Color> entry)
	{
		if (entry != null)
		{
			currentEntryValue = entry.getKey();
			currentEntryColor = entry.getValue();
		}
		else
		{
			currentEntryValue = null;
			currentEntryColor = null;
		}
	}

	private void updateCurrentEntryColor()
	{
		currentEntryColor = fromRGB(editorColorField.getColorValue(),
				editorAlphaScale.getSelection());
		map.changeColor(currentEntryValue, currentEntryColor);
	}

	private void selectTableEntry(Entry<Double, Color> entry)
	{
		if (entry == null)
		{
			entriesTable.setSelection(null);
			return;
		}

		entriesTable.setSelection(new StructuredSelection(entry));
	}

	/**
	 * @return The currently selected entry in the entries table, or
	 *         <code>null</code> if none is selected.
	 */
	@SuppressWarnings("unchecked")
	private Entry<Double, Color> getSelectedTableEntry()
	{
		Entry<Double, Color> selection =
				(Entry<Double, Color>) ((IStructuredSelection) entriesTable.getSelection()).getFirstElement();
		return selection;
	}

	/**
	 * Select the marker at the given coordinate, if one exists
	 */
	private Marker selectMarkerByCoordinate(int x, int y)
	{
		Point p = new Point(x, y);
		Marker result = null;
		for (Marker m : markers)
		{
			m.setSelected(m.contains(p));
			if (m.selected)
			{
				result = m;
				entriesTable.setSelection(new StructuredSelection(m.getEntry()));
			}
		}
		return result;
	}

	/**
	 * Select the marker with the given value, of one exists
	 */
	private Marker selectMarkerByValue(Double value)
	{
		if (value == null)
		{
			return null;
		}

		Marker result = null;
		for (Marker m : markers)
		{
			m.setSelected(m.getValue().equals(value));
			if (m.selected)
			{
				result = m;
			}
		}
		Display.getCurrent().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				markerCanvas.redraw();
			}
		});
		return result;
	}

	private void enableEntryEditor()
	{
		editorValueField.setEnabled(true);
		editorValueField.setNumber(currentEntryValue);
		editorColorField.setEnabled(true);
		editorColorField.setColorValue(toRGB(currentEntryColor));
		editorAlphaScale.setEnabled(true);
		editorAlphaScale.setSelection(currentEntryColor.getAlpha());
		editorAlphaField.setEnabled(true);
		editorAlphaField.setNumber(currentEntryColor.getAlpha());
	}

	private void disableEntryEditor()
	{
		editorValueField.setNumber(null);
		editorValueField.setEnabled(false);
		editorColorField.setEnabled(false);
		editorAlphaScale.setSelection(255);
		editorAlphaScale.setEnabled(false);
		editorAlphaField.setNumber(null);
		editorAlphaField.setEnabled(false);
	}

	private void enableNodataEditor(boolean enable)
	{
		nodataColorField.setEnabled(enable);
		nodataAlphaScale.setEnabled(enable);
		nodataAlphaField.setEnabled(enable);
	}

	private void updateNodataColorFromEditor()
	{
		if (!nodataCheckBox.getSelection())
		{
			map.setNodataColour(null);
			return;
		}

		Color nodataColor = fromRGB(nodataColorField.getColorValue(),
				nodataAlphaScale.getSelection());
		map.setNodataColour(nodataColor);
	}

	private void updateNodataEditorFromMap()
	{
		Color nodataColor = map.getNodataColour();
		if (nodataColor == null)
		{
			nodataCheckBox.setSelection(false);
			enableNodataEditor(false);
			return;
		}

		nodataColorField.setColorValue(toRGB(nodataColor));
		nodataAlphaScale.setSelection(nodataColor.getAlpha());
		nodataAlphaField.setNumber(nodataColor.getAlpha());
	}

	//**********************************************
	// Add / Remove entries
	//**********************************************

	/**
	 * Add a new entry in the colour map that is:
	 * <ul>
	 * <li>Half way between the selected entry and the previous entry (if one
	 * exists); or
	 * <li>Half way between the selected entry and the min value (if no previous
	 * entry exists and selected != min)
	 * <li>Half way between the selected entry and the next entry (if min entry
	 * is selected); or
	 * <li>Half way between the selected entry and the max value (if min entry
	 * is selected and no other entry exists); or
	 * <li>Half way between the min value and the max value (if no entries exist
	 * in the table)
	 * </ul>
	 * 
	 */
	private void addNewEntry()
	{
		Double newEntryValue = getMinValue();

		Entry<Double, Color> selectedEntry = getSelectedTableEntry();
		if (selectedEntry == null)
		{
			// If nothing selected, either choose the first entry or the mid point
			if (map.isEmpty())
			{
				newEntryValue = getMinValue() + (getMaxValue() - getMinValue()) / 2;
			}
			else
			{
				selectedEntry = map.getFirstEntry();
			}
		}

		if (selectedEntry != null)
		{
			if (map.getSize() == 1)
			{
				// Map only contains one entry
				if (selectedEntry.getKey() == getMinValue())
				{
					// Selected entry is min value - go higher
					newEntryValue = getMinValue() + (getMaxValue() - getMinValue()) / 2;
				}
				else
				{
					// Otherwise go lower
					newEntryValue = getMinValue() + (selectedEntry.getKey() - getMinValue()) / 2;
				}
			}
			else
			{
				// Map contains more than one entry
				if (selectedEntry.equals(map.getFirstEntry()))
				{
					// Selected entry is first entry 
					if (selectedEntry.getKey() == getMinValue())
					{
						// Go between selected and next
						double nextValue = map.getNextEntry(getMinValue()).getKey();
						newEntryValue = getMinValue() + (nextValue - getMinValue()) / 2;
					}
					else
					{
						// Go between selected and min value
						newEntryValue = getMinValue() + (selectedEntry.getKey() - getMinValue()) / 2;
					}
				}
				else
				{
					// Selected entry is not first - go between previous entry
					double previousValue = map.getPreviousEntry(selectedEntry.getKey()).getKey();
					newEntryValue = previousValue + (selectedEntry.getKey() - previousValue) / 2;
				}
			}
		}

		Color newEntryColor = map.getColor(newEntryValue);
		map.addEntry(newEntryValue, newEntryColor);

	}

	private void removeCurrentEntry()
	{
		if (currentEntryValue == null)
		{
			return;
		}

		map.removeEntry(currentEntryValue);

		setCurrentEntry(null);
		entriesTable.setSelection(null);
		removeEntryButton.setEnabled(false);
		disableEntryEditor();
	}

	//**********************************
	// Data population
	//**********************************

	private void populateMarkers()
	{
		List<Marker> newMarkers = new ArrayList<Marker>(map.getSize());

		int z = 0;
		for (Entry<Double, Color> entry : map.getEntries().entrySet())
		{
			newMarkers.add(new Marker(z++, entry));
		}
		Collections.sort(newMarkers);

		markers = newMarkers;
	}

	private Marker addMarker(Entry<Double, Color> newEntry)
	{
		Marker newMarker = new Marker(0, newEntry);
		markers.add(newMarker);
		return newMarker;
	}

	private void populateColors()
	{
		Point canvasSize = gradientCanvas.getSize();
		if (canvasSize.y == 0)
		{
			colors = new Color[0];
			return;
		}

		Color[] newColors = new Color[canvasSize.y - 2 * gradientCanvas.getBorderWidth()];

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

	//**********************************
	// Painting
	//**********************************

	private void paintGradient(GC gc, Display display)
	{
		// Allow colours array to be changed mid-render without locking
		Color[] paintColors = colors;

		// TODO: I suspect this is a bad way of doing this... optimise based on 
		// interp mode - we should be able to seriously decrease the number of 
		// colours created etc. when eg. nearest_match is used 
		org.eclipse.swt.graphics.Color swtColor = null;
		org.eclipse.swt.graphics.Color backgroundColor = gradientCanvas.getBackground();
		Point size = gradientCanvas.getSize();
		for (int pixel = 0; pixel < paintColors.length; pixel++)
		{
			Color paintColor = paintColors[pixel];
			if (paintColor != null)
			{
				swtColor = toSwtColor(paintColor, backgroundColor);
			}
			else
			{
				swtColor = gradientCanvas.getBackground();
			}

			gc.setForeground(swtColor);
			gc.drawLine(0, pixel, size.x, pixel);
		}
	}

	/**
	 * Paint the current list of markers in the marker canvas
	 */
	private void paintMarkers(GC gc, Display display, Rectangle bounds)
	{
		List<Marker> markers = this.markers;

		for (Marker m : markers)
		{
			if (m.bounds.intersects(bounds))
			{
				m.paint(gc);
			}
		}
	}

	//**********************************
	// Utilities
	//**********************************

	/**
	 * @return The minimum data value for the current map
	 */
	private double getMinValue()
	{
		if (map.isPercentageBased() || !hasDataValues)
		{
			return 0.0;
		}
		return minDataValue;
	}

	/**
	 * @return The maximum data value for the current map
	 */
	private double getMaxValue()
	{
		if (map.isPercentageBased() || !hasDataValues)
		{
			return 1.0;
		}
		return maxDataValue;
	}

	private int getGradientSize()
	{
		return gradientCanvas.getSize().y - 2 * gradientCanvas.getBorderWidth();
	}

	private int getMarkerCanvasOffset()
	{
		return gradientCanvas.getBorderWidth();
	}

	/**
	 * Convert an AWT {@link Color} instance to an equivalent SWT {@link RGB}.
	 * <p/>
	 * Note that this conversion will ignore the alpha value of the AWT
	 * {@link Color} as SWT does not support alpha.
	 */
	private static RGB toRGB(Color color)
	{
		if (color == null)
		{
			return null;
		}
		return new RGB(color.getRed(), color.getGreen(), color.getBlue());
	}

	/**
	 * Convert the given SWT {@link RGB} instance to an equivalent AWT
	 * {@link Color} instance, applying the given alpha value.
	 */
	private static Color fromRGB(RGB rgb, int alpha)
	{
		if (rgb == null)
		{
			return null;
		}
		return new Color(rgb.red, rgb.green, rgb.blue, alpha);
	}

	/**
	 * Convert an AWT colour to an SWT colour.
	 * <p/>
	 * If the provided AWT colour contains transparency, will pre-multiply with
	 * the given background colour (as SWT colours do not support an alpha
	 * channel).
	 * 
	 * @param awtColor
	 *            The AWT colour to convert
	 * @param display
	 *            The display to use when creating the SWT colour
	 * @param backgroundColor
	 *            The background colour to use for pre-multiplying in the case
	 *            of an AWT colour with transparency.
	 * 
	 * @return A new SWT colour
	 */
	private org.eclipse.swt.graphics.Color toSwtColor(Color awtColor,
			org.eclipse.swt.graphics.Color backgroundColor)
	{
		String key = "" + awtColor.getRGB() + "+" + backgroundColor.getRGB().hashCode(); //$NON-NLS-1$ //$NON-NLS-2$
		if (colorRegistry.hasValueFor(key))
		{
			return colorRegistry.get(key);
		}

		int red;
		int green;
		int blue;

		if (awtColor.getAlpha() < 255 && backgroundColor != null)
		{
			// Do alpha pre-multiplication as SWT colours don't support an alpha channel
			// Use a simple (alpha + 1-alpha) combiner

			float alpha = awtColor.getAlpha() / 255.0f;

			red = (int) (awtColor.getRed() * alpha) + (int) (backgroundColor.getRed() * (1 - alpha));
			green = (int) (awtColor.getGreen() * alpha) + (int) (backgroundColor.getGreen() * (1 - alpha));
			blue = (int) (awtColor.getBlue() * alpha) + (int) (backgroundColor.getBlue() * (1 - alpha));
		}
		else
		{
			red = awtColor.getRed();
			green = awtColor.getGreen();
			blue = awtColor.getBlue();
		}

		colorRegistry.put(key, new RGB(red, green, blue));

		return colorRegistry.get(key);
	}


	//*********************************
	// Markers
	//*********************************

	/**
	 * Represents a single marker in the colour gradient
	 * <p/>
	 * Coordinates stored are relative to the
	 * {@link ColorMapEditor#markerCanvas}
	 */
	private class Marker implements Comparable<Marker>
	{
		final static int markerThickness = 4;
		final static int borderWidth = 2;
		final static int borderThickness = markerThickness + 2 * borderWidth;

		final Color unselectedBorderColor = new Color(0, 0, 0);
		final Color selectedBorderColor = new Color(100, 100, 220);

		private int zIndex; // Drawn in reverse order of zIndex: 0 = top.
		private Double value;
		private Color color;

		private int midPointY;
		private Rectangle bounds;
		private boolean selected;

		private PropertyChangeListener entryMovedListener = new PropertyChangeListener()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void propertyChange(java.beans.PropertyChangeEvent evt)
			{
				Entry<Double, Color> oldEntry = (Entry<Double, Color>) evt.getOldValue();
				if (!isThisMarker(oldEntry.getKey()))
				{
					return;
				}

				value = ((Entry<Double, Color>) evt.getNewValue()).getKey();

				final Rectangle oldBounds = bounds;
				updateBounds();

				Display.getCurrent().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						markerCanvas.redraw(oldBounds.x, oldBounds.y, oldBounds.width, oldBounds.height, true);
						markerCanvas.redraw(bounds.x, bounds.y, bounds.width, bounds.height, true);
					}
				});
			}
		};

		private PropertyChangeListener entryRemovedListener = new PropertyChangeListener()
		{
			@Override
			public void propertyChange(java.beans.PropertyChangeEvent evt)
			{
				@SuppressWarnings("unchecked")
				Entry<Double, Color> removed = (Entry<Double, Color>) evt.getOldValue();
				if (!isThisMarker(removed.getKey()))
				{
					return;
				}

				dispose();
			}
		};

		private PropertyChangeListener colorChangedListener = new PropertyChangeListener()
		{
			@Override
			public void propertyChange(java.beans.PropertyChangeEvent evt)
			{
				@SuppressWarnings("unchecked")
				Entry<Double, Color> entry = (Entry<Double, Color>) evt.getNewValue();
				if (!isThisMarker(entry.getKey()))
				{
					return;
				}

				color = entry.getValue();

				Display.getCurrent().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						markerCanvas.redraw(bounds.x, bounds.y, bounds.width, bounds.height, true);
					}
				});
			}
		};

		private ControlAdapter controlAdapter = new ControlAdapter()
		{
			@Override
			public void controlResized(ControlEvent e)
			{
				updateBounds();
			}
		};

		public Marker(int zIndex, Entry<Double, Color> entry)
		{
			this.zIndex = zIndex;
			this.value = entry.getKey();
			this.color = entry.getValue();

			markerCanvas.addControlListener(controlAdapter);
			map.addPropertyChangeListener(MutableColorMap.ENTRY_MOVED_EVENT, entryMovedListener);
			map.addPropertyChangeListener(MutableColorMap.COLOR_CHANGED_EVENT, colorChangedListener);
			map.addPropertyChangeListener(MutableColorMap.ENTRY_REMOVED_EVENT, entryRemovedListener);

			updateBounds();
		}

		private void updateBounds()
		{
			int centreX = markerCanvas.getSize().x / 2;

			double percent = (getValue() - getMinValue()) / (getMaxValue() - getMinValue());

			midPointY = (int) (percent * getGradientSize()) + getMarkerCanvasOffset();

			bounds = new Rectangle(0, midPointY - borderThickness / 2, centreX + borderWidth, borderThickness);
		}

		@Override
		public int compareTo(Marker o)
		{
			return o.zIndex - zIndex;
		}

		public void paint(GC gc)
		{
			if (selected)
			{
				gc.setForeground(toSwtColor(selectedBorderColor, markerCanvas.getBackground()));
			}
			else
			{
				gc.setForeground(toSwtColor(unselectedBorderColor, markerCanvas.getBackground()));
			}

			// Draw border
			gc.setLineWidth(borderThickness);
			gc.drawLine(0, midPointY, bounds.width, midPointY);

			// Draw colour dash
			gc.setForeground(toSwtColor(getColor(), markerCanvas.getBackground()));
			gc.setLineWidth(markerThickness);
			gc.drawLine(0, midPointY, bounds.width - borderWidth, midPointY);
		}

		public boolean contains(Point p)
		{
			return bounds.contains(p);
		}

		public void setSelected(boolean selected)
		{
			this.selected = selected;
		}

		public void setValue(double value)
		{
			map.moveEntry(getValue(), value);
		}

		public boolean isThisMarker(Double value)
		{
			return getValue().equals(value);
		}

		public Double getValue()
		{
			return value;
		}

		public Color getColor()
		{
			return color;
		}

		public Entry<Double, Color> getEntry()
		{
			return new AbstractMap.SimpleEntry<Double, Color>(value, color);
		}

		public int getZIndex()
		{
			return zIndex;
		}

		private void dispose()
		{
			map.removePropertyChangeListener(entryRemovedListener);
			markerCanvas.removeControlListener(controlAdapter);
			map.removePropertyChangeListener(colorChangedListener);
			map.removePropertyChangeListener(entryMovedListener);
			markers.remove(this);

			Display.getCurrent().asyncExec(new Runnable()
			{
				@Override
				public void run()
				{
					markerCanvas.redraw(bounds.x, bounds.y, bounds.width, bounds.height, true);
				}
			});
		}

		@Override
		public String toString()
		{
			return "Marker" + hashCode() + "[" + getValue() + ", " + getColor() + "](" + getZIndex() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
	}

	private class MarkerMouseListener extends MouseAdapter implements MouseMoveListener
	{

		private final double minValueChange = 0.001;

		private boolean mouseDown = false;
		private int lastY = -1;

		private Marker heldMarker = null;

		@Override
		public void mouseDown(MouseEvent e)
		{
			mouseDown = true;
			lastY = e.y;

			heldMarker = selectMarkerByCoordinate(e.x, e.y);
		}

		@Override
		public void mouseUp(MouseEvent e)
		{
			mouseDown = false;
			lastY = -1;

			heldMarker = null;
		}

		@Override
		public void mouseMove(MouseEvent e)
		{
			if (!mouseDown || heldMarker == null)
			{
				return;
			}

			int deltaY = e.y - lastY;

			double valueChange =
					((double) deltaY / getGradientSize()) * (getMaxValue() - getMinValue()) + getMinValue();

			double newValue = heldMarker.getValue() + valueChange;

			if (doMove(valueChange, newValue))
			{
				lastY = e.y;
				heldMarker.setValue(newValue);
				heldMarker.setSelected(true);
			}
		}

		private boolean doMove(double valueChange, double newValue)
		{
			if (Math.abs(valueChange) < minValueChange ||
					newValue < getMinValue() || newValue > getMaxValue())
			{
				return false;
			}

			// Check that we don't replace an existing map entry when moving markers around
			Entry<Double, Color> nextEntry = map.getNextEntry(newValue);
			if (nextEntry != null && Math.abs(newValue - nextEntry.getKey()) < minValueChange)
			{
				return false;
			}

			Entry<Double, Color> previousEntry = map.getNextEntry(newValue);
			if (previousEntry != null && Math.abs(newValue - previousEntry.getKey()) < minValueChange)
			{
				return false;
			}

			return true;
		}

	}
}
