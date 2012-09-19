/*******************************************************************************
 * Copyright 2012 Geoscience Australia
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
package au.gov.ga.earthsci.application.parts;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.layers.Layer;

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

import au.gov.ga.earthsci.core.worldwind.TreeModel;

/**
 * Part which views the current layer state in the World Wind {@link Model}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerListPart
{
	@Inject
	private IEclipseContext context;

	@Inject
	private TreeModel worldWindModel;

	@Inject
	public void init(Composite parent)
	{
		CheckboxTableViewer viewer = CheckboxTableViewer.newCheckList(parent, SWT.NONE);

		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		viewer.setContentProvider(contentProvider);

		IObservableSet knownElements = contentProvider.getKnownElements();
		final IObservableMap enableds = BeanProperties.value("enabled").observeDetail(knownElements); //$NON-NLS-1$
		final IObservableMap opacities = BeanProperties.value("opacity").observeDetail(knownElements); //$NON-NLS-1$

		final IObservableMap[] attributeMap = new IObservableMap[] { enableds, opacities };
		ILabelProvider labelProvider = new ObservableMapLabelProvider(attributeMap)
		{
			@Override
			public String getColumnText(Object element, int columnIndex)
			{
				Layer layer = (Layer) element;
				return String.format("%s (%d%%)", layer.getName(), (int) (layer.getOpacity() * 100));
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
			}
		});
	}
}
