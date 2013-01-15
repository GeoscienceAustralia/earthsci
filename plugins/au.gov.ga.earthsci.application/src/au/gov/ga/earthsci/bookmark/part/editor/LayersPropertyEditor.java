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
package au.gov.ga.earthsci.bookmark.part.editor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import au.gov.ga.earthsci.bookmark.BookmarkPropertyFactory;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.bookmark.properties.layer.LayersProperty;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * An {@link IBookmarkPropertyEditor} used for editing {@link IBookmarkProperty}
 * instances.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LayersPropertyEditor extends AbstractBookmarkPropertyEditor
{

	private Composite container;
	
	private CheckboxTableViewer layerTable;
	
	private Map<URI, Double> layerState;
	
	@Override
	public String getName()
	{
		return Messages.LayersPropertyEditor_EditorName;
	}

	@Override
	public String getDescription()
	{
		return Messages.LayersPropertyEditor_EditorDescription;
	}

	@Override
	public void okPressed()
	{
		Map<URI, Double> filtered = new HashMap<URI, Double>();
		for (Entry<URI, Double> entry : layerState.entrySet())
		{
			if (layerTable.getChecked(entry))
			{
				filtered.put(entry.getKey(), entry.getValue());
			}
		}
		LayersProperty property = getLayersProperty();
		property.setLayerState(filtered);
	}

	@Override
	public Control createControl(Composite parent)
	{
		container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(1, false));
		
		Label l = new Label(container, SWT.TITLE);
		l.setText(Messages.LayersPropertyEditor_LayerStateLabel);
		
		Table table = new Table(container, SWT.BORDER | 
										   SWT.H_SCROLL | 
										   SWT.V_SCROLL |
										   SWT.FULL_SELECTION |
										   SWT.CHECK);
		
		layerTable = new CheckboxTableViewer(table);
		layerTable.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		layerTable.setContentProvider(ArrayContentProvider.getInstance());
		layerTable.getTable().setHeaderVisible(true);
		layerTable.getTable().setLinesVisible(true);
		
		TableViewerColumn col = new TableViewerColumn(layerTable, SWT.NONE);
		col.getColumn().setWidth(200);
		col.getColumn().setText(Messages.LayersPropertyEditor_LayerStateLabelColumnLabel);
		col.getColumn().setResizable(true);
		col.getColumn().setMoveable(false);
		
		col = new TableViewerColumn(layerTable, SWT.NONE);
		col.getColumn().setWidth(100);
		col.getColumn().setText(Messages.LayersPropertyEditor_LayerStateOpacityColumnLabel);
		col.getColumn().setResizable(true);
		col.getColumn().setMoveable(false);
		col.setEditingSupport(new EditingSupport(col.getViewer()) {

			@Override
			protected CellEditor getCellEditor(Object element)
			{
				TextCellEditor textCellEditor = new TextCellEditor(layerTable.getTable());
				textCellEditor.setValidator(new ICellEditorValidator()
				{
					@Override
					public String isValid(Object value)
					{
						try
						{
							Double.parseDouble((String)value);
							return null;
						}
						catch (Exception e)
						{
							return Messages.LayersPropertyEditor_InvalidOpacityMessage;
						}
					}
				});
				return textCellEditor;
			}

			@Override
			protected boolean canEdit(Object element)
			{
				return true;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected Object getValue(Object element)
			{
				return Double.toString(((Entry<URI, Double>)element).getValue());
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void setValue(Object element, Object value)
			{
				((Entry<URI, Double>)element).setValue(Util.clamp(Double.parseDouble((String)value), 0, 1));
				layerTable.refresh(element, true);
			}
			
		});
		
		layerTable.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				@SuppressWarnings("unchecked")
				Entry<URI, Double> entry = (Entry<URI, Double>)cell.getElement();
				if (cell.getColumnIndex() == 0)
				{
					cell.setText(entry.getKey().toString());
				}
				else
				{
					cell.setText(Double.toString(entry.getValue()));
				}
			}
		});
		
		fillFieldsFromProperty(getProperty());
		
		return container;
	}

	@Override
	public Control getControl()
	{
		return container;
	}

	@Override
	protected IBookmarkProperty createPropertyFromCurrent()
	{
		return BookmarkPropertyFactory.createProperty(LayersProperty.TYPE);
	}

	@Override
	protected void fillFieldsFromProperty(IBookmarkProperty property)
	{
		layerState = new TreeMap<URI, Double>(((LayersProperty)property).getLayerState());
		layerTable.setInput(layerState.entrySet());
		layerTable.setAllChecked(true);
	}

	private LayersProperty getLayersProperty()
	{
		return (LayersProperty)getProperty();
	}
}
