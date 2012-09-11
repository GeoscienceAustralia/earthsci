package au.gov.ga.earthsci.application.parts;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.layers.Layer;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class LayerListPart
{
	@Inject
	private IEclipseContext context;

	@Inject
	public void init(Composite parent)
	{
		Model worldWindModel = context.get(Model.class);

		CheckboxTableViewer viewer = CheckboxTableViewer.newCheckList(parent, SWT.NONE);
		viewer.setContentProvider(new ObservableListContentProvider());

		List<Layer> layers = worldWindModel.getLayers();
		IObservableList input = Properties.selfList(Layer.class).observe(layers);
		viewer.setInput(input);
		viewer.setAllChecked(true);

		viewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				Layer layer = (Layer) event.getElement();
				layer.setEnabled(event.getChecked());
			}
		});
	}
}
