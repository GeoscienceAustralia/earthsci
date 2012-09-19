package au.gov.ga.earthsci.application.preferences.fieldeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class ListenableBooleanFieldEditor extends BooleanFieldEditor
{
	private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();

	public ListenableBooleanFieldEditor()
	{
		super();
	}

	public ListenableBooleanFieldEditor(String name, String label, Composite parent)
	{
		super(name, label, parent);
	}

	public ListenableBooleanFieldEditor(String name, String labelText, int style, Composite parent)
	{
		super(name, labelText, style, parent);
	}

	@Override
	protected void doLoad()
	{
		super.doLoad();
		notifyListeners(getBooleanValue());
	}

	@Override
	protected void doLoadDefault()
	{
		super.doLoadDefault();
		notifyListeners(getBooleanValue());
	}

	@Override
	protected void fireValueChanged(String property, Object oldValue, Object newValue)
	{
		super.fireValueChanged(property, oldValue, newValue);
		notifyListeners(getBooleanValue());
	}

	public void addListener(ChangeListener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(ChangeListener listener)
	{
		listeners.remove(listener);
	}

	protected void notifyListeners(boolean newValue)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).valueChanged(newValue);
		}
	}

	public interface ChangeListener
	{
		void valueChanged(boolean newValue);
	}
}
