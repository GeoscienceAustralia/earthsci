package au.gov.ga.earthsci.application.parts;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.layers.Layer;

import java.beans.PropertyChangeEvent;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class LayerListPart
{
	@Inject
	private IEclipseContext context;
	
	@Inject
	private Model worldWindModel;

	@Inject
	public void init(Composite parent)
	{
		CheckboxTableViewer viewer = CheckboxTableViewer.newCheckList(parent, SWT.NONE);

		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		viewer.setContentProvider(contentProvider);

		IObservableSet knownElements = contentProvider.getKnownElements();
		final IObservableMap enableds = BeanProperties.value("enabled").observeDetail(knownElements); //$NON-NLS-1$

		ILabelProvider labelProvider = new ObservableMapLabelProvider(enableds)
		{
			@Override
			public String getColumnText(Object element, int columnIndex)
			{
				Layer layer = (Layer) element;
				return layer.getName();
			}
		};
		viewer.setLabelProvider(labelProvider);

		viewer.setCheckStateProvider(new ICheckStateProvider()
		{
			@Override
			public boolean isGrayed(Object element)
			{
				return false;
			}

			@Override
			public boolean isChecked(Object element)
			{
				Layer layer = (Layer) element;
				return layer.isEnabled();
			}
		});

		List<Layer> layers = worldWindModel.getLayers();
		IObservableList input = Properties.selfList(Layer.class).observe(layers);
		viewer.setInput(input);

		viewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				Layer layer = (Layer) event.getElement();
				layer.setEnabled(event.getChecked());
				//TODO THIS SUCKS!: but in AbstractLayer, the "Enabled" property is changed not "enabled"  
				layer.propertyChange(new PropertyChangeEvent(layer, "enabled", !event.getChecked(), event.getChecked())); //$NON-NLS-1$
			}
		});
	}
}
