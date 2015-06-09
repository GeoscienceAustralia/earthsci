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
package au.gov.ga.earthsci.bookmark.ui.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
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
import au.gov.ga.earthsci.bookmark.properties.layer.LayersPropertyPersister;
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

	private Map<String, Map<String, String>> layerState;
	private List<TableViewerColumn> tableColumns;
	private Map<Integer, String> colToName;
	private CellLabelProvider labelProv;

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
		if (layerState == null)
		{
			return;
		}

		Map<String, Map<String, String>> filtered = new HashMap<String, Map<String, String>>();
		for (Entry<String, Map<String, String>> entry : layerState.entrySet())
		{
			if (layerTable.getChecked(entry))
			{
				filtered.put(entry.getKey(), entry.getValue());
			}
		}
		LayersProperty property = getLayersProperty();
		property.setLayerState(filtered);
	}

	private Collection<String> getColumnsNeeded(Map<String, Map<String, String>> elements)
	{
		Set<String> columns = new TreeSet<String>();

		for (Entry<String, Map<String, String>> entry : elements.entrySet())
		{
			for (Entry<String, String> item : entry.getValue().entrySet())
			{
				columns.add(item.getKey());
			}
		}
		columns.add("id");
		return columns;
	}

	@Override
	public Control createControl(Composite parent)
	{
		container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(1, false));

		Label l = new Label(container, SWT.TITLE);
		l.setText(Messages.LayersPropertyEditor_LayerStateLabel);

		Table table = new Table(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.CHECK);
		tableColumns = new ArrayList<TableViewerColumn>();
		colToName = new ConcurrentHashMap<Integer, String>();
		colToName.put(0, "id");
		colToName.put(1, "opacity");


		layerTable = new CheckboxTableViewer(table);
		layerTable.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		layerTable.setContentProvider(new IStructuredContentProvider()
		{

			@Override
			public void inputChanged(Viewer arg0, Object oldItem, Object newItem)
			{
				for (TableViewerColumn col : tableColumns)
				{
					col.getColumn().dispose();
				}
				tableColumns.clear();
				colToName.clear();
				if (newItem != null)
				{
					Collection<String> cols = null;
					if (newItem instanceof Map)
					{
						cols = getColumnsNeeded((Map) newItem);
					}


					for (String s : cols)
					{
						if (!s.equals("id"))
						{
							createTableColumn(s, s.equals(LayersPropertyPersister.OPACITY_ATTRIBUTE_NAME));
						}
					}
					createTableColumn("id", false);
				}
			}

			@Override
			public void dispose()
			{

			}

			@Override
			public Object[] getElements(Object element)
			{
				List items = new ArrayList();
				Map<String, Map<String, String>> stateinfo = (Map<String, Map<String, String>>) element;
				for (Entry<String, Map<String, String>> entry : stateinfo.entrySet())
				{
					items.add(entry);
				}
				return items.toArray(new Object[0]);
			}
		});



		layerTable.getTable().setHeaderVisible(true);
		layerTable.getTable().setLinesVisible(true);
		labelProv = new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				@SuppressWarnings("unchecked")
				Entry<String, Map<String, String>> entry = (Entry<String, Map<String, String>>) cell.getElement();
				int index = cell.getColumnIndex();
				String colName = colToName.get(index);
				cell.setText(colName.equals("id") ? entry.getKey() : entry.getValue().get(colName));
			}
		};

		fillFieldsFromProperty(getProperty());

		return container;
	}

	/**
	 * Creates a column with the given information
	 * 
	 * @param columnHeader
	 *            The header title for the column
	 * @param index
	 *            The index at which this column will be created.
	 * @param editSupport
	 */
	private void createTableColumn(String columnHeader, boolean editSupport)
	{
		colToName.put(layerTable.getTable().getColumnCount(), columnHeader);
		TableViewerColumn col = new TableViewerColumn(layerTable, SWT.NONE);

		col.getColumn().setWidth(200);
		col.getColumn().setText(columnHeader);
		col.getColumn().setResizable(true);
		col.getColumn().setMoveable(false);
		tableColumns.add(col);

		layerTable.setLabelProvider(labelProv);
		if (editSupport)
		{
			col.setEditingSupport(getEditorSupport(col));
		}
	}

	private EditingSupport getEditorSupport(TableViewerColumn col)
	{
		return new EditingSupport(col.getViewer())
		{

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
							Double.parseDouble((String) value);
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
				return ((Entry<String, Map<String, String>>) element).getValue().get(
						LayersPropertyPersister.OPACITY_ATTRIBUTE_NAME);
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void setValue(Object element, Object value)
			{
				double newValue = Double.parseDouble((String) value);
				((Entry<String, Map<String, String>>) element).getValue().put(
						LayersPropertyPersister.OPACITY_ATTRIBUTE_NAME, String.valueOf(Util.clamp(newValue, 0, 1)));
				layerTable.refresh(element, true);
			}

		};
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
		if (property == null)
		{
			layerState = new TreeMap<String, Map<String, String>>();
		}
		else
		{
			layerTable.setInput(((LayersProperty) property).getLayerStateInfo());
		}
		layerTable.setAllChecked(true);
	}

	private LayersProperty getLayersProperty()
	{
		return (LayersProperty) getProperty();
	}
}
