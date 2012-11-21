package au.gov.ga.earthsci.application.preferences.fieldeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class ListenableRadioGroupFieldEditor extends RadioGroupFieldEditor
{
	private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
	private String value;

	protected ListenableRadioGroupFieldEditor()
	{
		super();
	}

	public ListenableRadioGroupFieldEditor(String name, String labelText, int numColumns, String[][] labelAndValues,
			Composite parent, boolean useGroup)
	{
		super(name, labelText, numColumns, labelAndValues, parent, useGroup);
	}

	public ListenableRadioGroupFieldEditor(String name, String labelText, int numColumns, String[][] labelAndValues,
			Composite parent)
	{
		super(name, labelText, numColumns, labelAndValues, parent);
	}

	@Override
	protected void doLoad()
	{
		super.doLoad();
		notifyListeners(getPreferenceStore().getString(getPreferenceName()));
	}

	@Override
	protected void doLoadDefault()
	{
		super.doLoadDefault();
		notifyListeners(getPreferenceStore().getDefaultString(getPreferenceName()));
	}

	@Override
	protected void fireValueChanged(String property, Object oldValue, Object newValue)
	{
		super.fireValueChanged(property, oldValue, newValue);
		if (newValue instanceof String)
		{
			notifyListeners((String) newValue);
		}
	}

	public String getStringValue()
	{
		return value;
	}

	public void addListener(ChangeListener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(ChangeListener listener)
	{
		listeners.remove(listener);
	}

	protected void notifyListeners(String newValue)
	{
		this.value = newValue;
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).valueChanged(newValue);
		}
	}

	public interface ChangeListener
	{
		void valueChanged(String newValue);
	}
}
