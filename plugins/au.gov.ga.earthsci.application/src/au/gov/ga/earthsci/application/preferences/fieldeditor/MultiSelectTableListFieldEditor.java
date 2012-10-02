package au.gov.ga.earthsci.application.preferences.fieldeditor;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * A {@link FieldEditor} that provides a tabular display of items from a list.
 * These items can be selected using checkboxes, and the list of selected items
 * will be bound to the named preference.
 * <p/>
 * List items can be rendered as table items using the {@link ITableItemCreator}
 * interface. The default implementation uses the list item's
 * {@link Object#toString()} method.
 * <p/>
 * List items are stored in the preferences store as a comma separated list
 * 
 * @param E
 *            The type of object that will be selected using this field editor
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class MultiSelectTableListFieldEditor<E> extends FieldEditor
{

	private static final String SOURCE_OBJECT_KEY =
			"au.gov.ga.earthsci.application.preferences.fieldeditor.MultiSelectTableListFieldEditor.sourceItem"; //$NON-NLS-1$

	private static final String NONE_SELECTED = "none"; //$NON-NLS-1$
	
	private final ITableItemCreator<E> DEFAULT_TABLE_ITEM_CREATOR = new ITableItemCreator<E>()
	{
		@Override
		public TableItem createTableItem(Table table, E object)
		{
			if (object == null)
			{
				return null;
			}
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(new String[] { object.toString() });
			return ti;
		}
	};

	/** The table control */
	private Table table;

	/** Table column names. May be <code>null</code>. */
	private String[] columnNames;

	/** The list backing this editor */
	private List<E> backingList;

	/** The item creator to use for converting list items to table rows */
	private ITableItemCreator<E> tableItemCreator = DEFAULT_TABLE_ITEM_CREATOR;

	/** The item serialiser to use for converting selected items to strings */
	private IItemSerializer<E> itemSerialiser;

	/**
	 * Whether to default to all checked, in the absence of overriding
	 * preferences
	 */
	private boolean defaultAllChecked = true;

	/** Composite containing select all/none buttons */
	private Composite buttonBox;
	
	/** Select all elements in the list */
	private Button selectAllButton;
	
	/** Deselect all elements in the list */
	private Button selectNoneButton;
	
	/**
	 * Creates a table backed by the provided list. The list item toString()
	 * method will be used to render values.
	 */
	public MultiSelectTableListFieldEditor(String keyName, List<E> backingList, IItemSerializer<E> itemSerializer,
			Composite parent)
	{
		this(keyName, backingList, null, null, itemSerializer, parent);
	}

	/**
	 * Creates a table backed by the provided list. The list item toString()
	 * method will be used to render values.
	 */
	public MultiSelectTableListFieldEditor(String keyName, List<E> backingList, String[] columnNames,
			ITableItemCreator<E> tableItemCreator, IItemSerializer<E> itemSerializer, Composite parent)
	{
		setPreferenceName(keyName);

		this.backingList = backingList;
		this.columnNames = columnNames;
		this.tableItemCreator = tableItemCreator == null ? DEFAULT_TABLE_ITEM_CREATOR : tableItemCreator;
		this.itemSerialiser = itemSerializer;

		doFillIntoGrid(parent, 1);
	}

	@Override
	protected void adjustForNumColumns(int numColumns)
	{
		GridData gd = (GridData) table.getLayoutData();
		gd.horizontalSpan = numColumns;
		gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns)
	{
		getTableControl(parent);

		GridData gd = new GridData();
		gd.horizontalSpan = numColumns + 1;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		table.setLayoutData(gd);
		
		buttonBox = getButtonBoxControl(parent);
        gd = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER);
        gd.horizontalAlignment = SWT.CENTER;
        buttonBox.setLayoutData(gd);
	}

	@Override
	protected void doLoad()
	{
		if (table != null)
		{
			if (!getPreferenceStore().contains(getPreferenceName()) && defaultAllChecked)
			{
				selectAll();
			}
			else
			{
				loadFromString(getPreferenceStore().getString(getPreferenceName()));
			}
		}
	}

	@Override
	protected void doLoadDefault()
	{
		if (table != null)
		{
			String defaultString = getPreferenceStore().getDefaultString(getPreferenceName());
			if (!defaultString.isEmpty())
			{
				loadFromString(defaultString);
			}
			else if (defaultAllChecked)
			{
				selectAll();
			}
		}
	}

	private void selectAll()
	{
		for (TableItem i : table.getItems())
		{
			i.setChecked(true);
		}
	}
	
	private void selectNone()
	{
		for (TableItem i : table.getItems())
		{
			i.setChecked(false);
		}
	}

	private void loadFromString(String csv)
	{
		// Special case
		if (NONE_SELECTED.equals(csv))
		{
			selectNone();
			return;
		}
		
		String[] selected = csv.split(","); //$NON-NLS-1$

		for (String s : selected)
		{
			E object = itemSerialiser.fromString(s);
			if (object == null)
			{
				continue;
			}
			for (TableItem i : table.getItems())
			{
				if (object.equals(i.getData(SOURCE_OBJECT_KEY)))
				{
					i.setChecked(true);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doStore()
	{
		StringBuffer result = new StringBuffer();
		TableItem[] items = table.getItems();
		int checkedItemCount = 0;
		for (int i = 0; i < items.length; i++)
		{
			if (!items[i].getChecked())
			{
				continue;
			}

			if (checkedItemCount != 0)
			{
				result.append(',');
			}
			result.append(itemSerialiser.asString((E) items[i].getData(SOURCE_OBJECT_KEY)));
			checkedItemCount++;
		}

		try
		{
			// If the preference is stored as an empty string it is removed from the store. 
			// In this case, "None selected" is different from "not set". Hence the special keyword.
			if (checkedItemCount == 0)
			{
				result.append(NONE_SELECTED);
			}
			
			getPreferenceStore().setValue(getPreferenceName(), result.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public int getNumberOfControls()
	{
		return 1;
	}

	@Override
	public void setEnabled(boolean enabled, Composite parent)
	{
		super.setEnabled(enabled, parent);
		table.setEnabled(enabled);
	}

	/**
	 * @return The table control for this editor, creating it if needed.
	 */
	private Table getTableControl(Composite parent)
	{
		if (table != null)
		{
			return table;
		}

		table = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK | SWT.MULTI);

		table.setLinesVisible(true);
		table.setHeaderVisible(columnNames != null);

		for (String columnName : columnNames)
		{
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(columnName);
		}

		for (E object : backingList)
		{
			TableItem tableItem = tableItemCreator.createTableItem(table, object);
			tableItem.setData(SOURCE_OBJECT_KEY, object);
		}

		for (TableColumn column : table.getColumns())
		{
			column.pack();
		}

		return table;
	}

	/**
	 * Returns this field editor's button box containing the select all / none
	 * buttons
	 */
	private Composite getButtonBoxControl(Composite parent)
	{
		if (buttonBox == null)
		{
			buttonBox = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginWidth = 0;
			buttonBox.setLayout(layout);
			createButtons(buttonBox);
			buttonBox.addDisposeListener(new DisposeListener()
			{
				@Override
				public void widgetDisposed(DisposeEvent event)
				{
					selectAllButton = null;
					selectNoneButton = null;
					buttonBox = null;
				}
			});

		}
		else
		{
			checkParent(buttonBox, parent);
		}

		return buttonBox;
	}
    
    /**
     * Creates the Add, Remove, Up, and Down button in the given button box.
     *
     * @param box the box for the buttons
     */
    private void createButtons(Composite box) {
        selectAllButton = createPushButton(box, "All");
        selectAllButton.addSelectionListener(new SelectionAdapter()
		{
        	@Override
        	public void widgetSelected(SelectionEvent e)
        	{
        		selectAll();
        	}
		});
        
        selectNoneButton = createPushButton(box, "None");
        selectNoneButton.addSelectionListener(new SelectionAdapter()
		{
        	@Override
        	public void widgetSelected(SelectionEvent e)
        	{
        		selectNone();
        	}
		});
    }

    /**
     * Helper method to create a push button.
     * 
     * @param parent the parent control
     * @param key the resource name used to supply the button's label text
     * @return Button
     */
    private Button createPushButton(Composite parent, String label) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        button.setFont(parent.getFont());
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        int widthHint = convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH);
        data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
        button.setLayoutData(data);
        //button.addSelectionListener(getSelectionListener());
        return button;
    }
	
	/**
	 * A strategy interface used to create a table item from the provided
	 * object.
	 * <p/>
	 * The created table items should have column text that matches any column
	 * headers.
	 */
	public static interface ITableItemCreator<E>
	{
		TableItem createTableItem(Table parent, E object);
	}

	/**
	 * A strategy interface used to serialize list items into a comma-separated
	 * list stored in the preferences store.
	 */
	public static interface IItemSerializer<E>
	{
		String asString(E object);

		E fromString(String string);
	}
}
