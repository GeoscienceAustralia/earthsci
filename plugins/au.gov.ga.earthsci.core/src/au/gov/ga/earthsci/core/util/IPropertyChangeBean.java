package au.gov.ga.earthsci.core.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public interface IPropertyChangeBean
{
	void firePropertyChange(PropertyChangeEvent propertyChangeEvent);

	void firePropertyChange(String propertyName, Object oldValue, Object newValue);

	void addPropertyChangeListener(PropertyChangeListener listener);

	void removePropertyChangeListener(PropertyChangeListener listener);

	void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

	void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
}