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
package au.gov.ga.earthsci.layer.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;

import au.gov.ga.earthsci.layer.tree.ILayerTreeNode;
import au.gov.ga.earthsci.layer.tree.LayerNode;

/**
 * Tool control used to change layer opacity.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerOpacityToolControl
{
	/**
	 * 
	 */
	private static final String OPACITY = "opacity";
	private boolean settingScale = false;
	private Scale scale;
	private ILayerTreeNode[] selection = null;

	private SelectionListener selectionListener;

	@PostConstruct
	public void createControls(Composite parent, IEclipseContext context)
	{
		int width = 80;
		int height = 21;

		Composite child = new Composite(parent, SWT.NONE);
		child.setSize(child.computeSize(width, SWT.DEFAULT));

		scale = new Scale(child, SWT.HORIZONTAL);
		Point size = scale.computeSize(width, SWT.DEFAULT);
		scale.setSize(size);
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setSelection(100);
		scale.setLocation(0, (height - size.y) / 2);
		scale.setToolTipText("Set opacity of the selected layer(s)");
		scale.setEnabled(false);

		selectionListener = new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (settingScale)
				{
					return;
				}
				double opacity = scale.getSelection() / 100d;
				setOpacity(Arrays.asList(selection), opacity);
			}
		};
		scale.addSelectionListener(selectionListener);

		if (selection != null)
		{
			setSelection(selection);
		}
	}

	@PreDestroy
	public void tearDown()
	{
		setSelection(null); // wipes out the selection listers.
		scale.removeSelectionListener(selectionListener);
	}

	@Inject
	public void selectionChanged(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode layer)
	{
		if (layer != null)
		{
			setSelection(new ILayerTreeNode[] { layer });
		}
	}

	@Inject
	public void selectionChanged(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode[] layers)
	{
		if (layers != null)
		{
			setSelection(layers);
		}
	}

	private void setSelection(ILayerTreeNode[] selection)
	{
		if (this.selection != null)
		{
			for (ILayerTreeNode node : this.selection)
			{
				node.removePropertyChangeListener(OPACITY, opacityPropertyChangeListener);
			}
		}

		this.selection = selection;

		if (this.selection != null)
		{
			for (ILayerTreeNode node : this.selection)
			{
				node.addPropertyChangeListener(OPACITY, opacityPropertyChangeListener);
			}
			updateScale();
		}


	}

	private void updateScale()
	{
		if (scale == null)
		{
			return;
		}

		settingScale = true;
		double opacity = scale.getSelection() / 100d;
		Double o = getMinOpacity(Arrays.asList(selection), null);
		opacity = o == null ? opacity : o;
		scale.setSelection((int) (opacity * 100d));
		scale.setEnabled(selection.length > 0);
		settingScale = false;
	}

	private Double getMinOpacity(List<ILayerTreeNode> nodes, Double opacity)
	{
		for (ILayerTreeNode node : nodes)
		{
			if (node instanceof LayerNode)
			{
				LayerNode layer = (LayerNode) node;
				opacity = opacity == null ? layer.getOpacity() : Math.min(opacity, layer.getOpacity());
			}
			opacity = getMinOpacity(node.getChildren(), opacity);
		}
		return opacity;
	}

	private void setOpacity(List<ILayerTreeNode> nodes, double opacity)
	{
		if (nodes != null)
		{
			for (ILayerTreeNode node : nodes)
			{
				if (node instanceof LayerNode)
				{
					LayerNode layer = (LayerNode) node;
					layer.setOpacity(opacity);
				}
				setOpacity(node.getChildren(), opacity);
			}
		}
	}

	private PropertyChangeListener opacityPropertyChangeListener = new PropertyChangeListener()
	{
		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			updateScale();
		}
	};
}
