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
package au.gov.ga.earthsci.application.parts.layer;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;

import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.model.layer.LayerNode;
import au.gov.ga.earthsci.core.tree.ITreeNode;

/**
 * Tool control used to change layer opacity.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerOpacityToolControl
{
	private boolean settingScale = false;
	private Scale scale;
	private TreeViewer viewer;
	private boolean connected = false;

	@PostConstruct
	public void createControls(Composite parent)
	{
		//TODO BUG: for some reason, after the layer part is closed and reopened, the
		//injected context is "anonymous", and doesn't contain the TreeViewer
		
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

		connectViewerToScale();
	}

	@Inject
	@Optional
	private void setViewer(TreeViewer viewer)
	{
		this.viewer = viewer;
		connectViewerToScale();
	}

	private void connectViewerToScale()
	{
		if (!connected && viewer != null && scale != null)
		{
			connected = true;

			viewer.addSelectionChangedListener(new ISelectionChangedListener()
			{
				@Override
				public void selectionChanged(SelectionChangedEvent event)
				{
					settingScale = true;
					double opacity = scale.getSelection() / 100d;
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					List<?> selectionList = selection.toList();
					ILayerTreeNode[] nodes = selectionList.toArray(new ILayerTreeNode[selectionList.size()]);
					Double o = getMinOpacity(nodes, null);
					opacity = o == null ? opacity : o;
					scale.setSelection((int) (opacity * 100d));
					settingScale = false;
				}
			});

			scale.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					if (settingScale)
					{
						return;
					}
					double opacity = scale.getSelection() / 100d;
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					List<?> selectionList = selection.toList();
					ILayerTreeNode[] nodes = selectionList.toArray(new ILayerTreeNode[selectionList.size()]);
					setOpacity(nodes, opacity);
				}
			});

			scale.setEnabled(true);
		}
	}

	private Double getMinOpacity(ITreeNode<ILayerTreeNode>[] nodes, Double opacity)
	{
		for (ITreeNode<ILayerTreeNode> node : nodes)
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

	private void setOpacity(ITreeNode<ILayerTreeNode>[] nodes, double opacity)
	{
		for (ITreeNode<ILayerTreeNode> node : nodes)
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
