package au.gov.ga.earthsci.core.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class AbstractPropertyChangeBean implements IPropertyChangeBean
{
	protected final transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

	@Override
	public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
	{
		changeSupport.firePropertyChange(propertyChangeEvent);
	}

	@Override
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		changeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		changeSupport.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		changeSupport.removePropertyChangeListener(listener);
	}

	@Override
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		changeSupport.addPropertyChangeListener(propertyName, listener);
	}

	@Override
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		changeSupport.removePropertyChangeListener(propertyName, listener);
	}
}
