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

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.retrieve.RetrievalService;
import gov.nasa.worldwind.retrieve.Retriever;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import au.gov.ga.earthsci.application.IFireableLabelProvider;
import au.gov.ga.earthsci.application.IconLoader;
import au.gov.ga.earthsci.application.LoadingIconAnimator;
import au.gov.ga.earthsci.application.LoadingIconFrameListener;
import au.gov.ga.earthsci.core.model.layer.FolderNode;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.model.layer.LayerNode;
import au.gov.ga.earthsci.worldwind.common.retrieve.ExtendedRetrievalService;
import au.gov.ga.earthsci.worldwind.common.retrieve.ExtendedRetrievalService.RetrievalListener;
import au.gov.ga.earthsci.worldwind.common.retrieve.RetrievalListenerHelper;

/**
 * Label provider for the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerTreeLabelProvider extends ObservableMapLabelProvider implements ILabelDecorator,
		IStyledLabelProvider, IFireableLabelProvider, RetrievalListener, LoadingIconFrameListener
{
	private final IconLoader iconLoader = new IconLoader(this);
	private final Map<Layer, Integer> layerRetrieverCount = new HashMap<Layer, Integer>();
	private final Map<Layer, ILayerTreeNode> retrievingElements = new HashMap<Layer, ILayerTreeNode>();

	private boolean disposed = false;

	private final Color informationColor;
	private final Color legendColor;
	private final Font subscriptFont;
	private final Styler informationStyler = new Styler()
	{
		@Override
		public void applyStyles(TextStyle textStyle)
		{
			textStyle.foreground = informationColor;
			textStyle.font = subscriptFont;
		}
	};
	private final Styler legendStyler = new Styler()
	{
		@Override
		public void applyStyles(TextStyle textStyle)
		{
			textStyle.foreground = legendColor;
			textStyle.font = subscriptFont;
		}
	};

	public LayerTreeLabelProvider(IObservableMap[] attributeMaps)
	{
		super(attributeMaps);
		informationColor = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
		legendColor = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
		FontData[] fontDatas = Display.getDefault().getSystemFont().getFontData();
		for (FontData fontData : fontDatas)
		{
			fontData.setStyle(SWT.BOLD);
			fontData.setHeight((int) (fontData.getHeight() * 0.8));
		}
		subscriptFont = new Font(Display.getDefault(), fontDatas);

		RetrievalService rs = WorldWind.getRetrievalService();
		if (rs instanceof ExtendedRetrievalService)
		{
			((ExtendedRetrievalService) rs).addRetrievalListener(this);
		}
	}

	@Override
	public void dispose()
	{
		//because this object is acting as both the decorator and the provider,
		//dispose is called twice, causing a NPE in the super class
		//workaround: set a flag when disposed, disabling multiple disposals
		if (disposed)
		{
			return;
		}
		disposed = true;

		super.dispose();
		iconLoader.dispose();
		subscriptFont.dispose();
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		if (element instanceof LayerNode)
		{
			LayerNode layer = (LayerNode) element;
			String label = layer.getLabelOrName();
			if (layer.getOpacity() < 1)
			{
				label += String.format(" (%d%%)", (int) (layer.getOpacity() * 100)); //$NON-NLS-1$
			}
			return label;
		}
		else if (element instanceof FolderNode)
		{
			FolderNode folder = (FolderNode) element;
			return folder.getLabelOrName();
		}
		return super.getColumnText(element, columnIndex);
	}

	@Override
	public Image getImage(Object element)
	{
		if (element instanceof ILayerTreeNode)
		{
			ILayerTreeNode node = (ILayerTreeNode) element;
			if (isLayerNodeRetrieving(node))
			{
				return LoadingIconAnimator.get().getCurrentFrame();
			}
			URL imageURL = node.getIconURL();
			if (imageURL != null)
			{
				return iconLoader.getImage(element, imageURL);
			}
		}
		return super.getImage(element);
	}

	@Override
	public Image decorateImage(Image image, Object element)
	{
		return null;
	}

	@Override
	public String decorateText(String text, Object element)
	{
		return text;
	}

	@Override
	public StyledString getStyledText(Object element)
	{
		StyledString string = new StyledString(getColumnText(element, 0));
		if (element instanceof ILayerTreeNode)
		{
			ILayerTreeNode layerNode = (ILayerTreeNode) element;
			if (layerNode.getInfoURL() != null || layerNode.getLegendURL() != null)
			{
				string.append(" "); //$NON-NLS-1$
				if (layerNode.getInfoURL() != null)
				{
					string.append(" i", informationStyler); //$NON-NLS-1$
				}
				if (layerNode.getLegendURL() != null)
				{
					string.append(" L", legendStyler); //$NON-NLS-1$
				}
			}
		}
		return string;
	}

	@Override
	public void fireLabelProviderChanged(LabelProviderChangedEvent event)
	{
		super.fireLabelProviderChanged(event);
	}

	@Override
	public void beforeRetrieve(Retriever retriever)
	{
		Layer layer = RetrievalListenerHelper.getLayer(retriever);
		if (layer != null)
		{
			synchronized (layerRetrieverCount)
			{
				boolean wasEmpty = layerRetrieverCount.isEmpty();
				Integer count = layerRetrieverCount.get(layer);
				boolean fireChange = count == null;
				count = count == null ? 0 : count;
				layerRetrieverCount.put(layer, count + 1);
				if (wasEmpty)
				{
					LoadingIconAnimator.get().addListener(this);
				}
				if (fireChange)
				{
					fireLabelProviderChangedFor(true);
				}
			}
		}
	}

	@Override
	public void afterRetrieve(Retriever retriever)
	{
		Layer layer = RetrievalListenerHelper.getLayer(retriever);
		if (layer != null)
		{
			synchronized (layerRetrieverCount)
			{
				Integer count = layerRetrieverCount.get(layer);
				if (count != null)
				{
					if (count <= 1)
					{
						layerRetrieverCount.remove(layer);
						retrievingElements.remove(layer);
						fireLabelProviderChangedFor(true);
					}
					else
					{
						layerRetrieverCount.put(layer, count - 1);
					}
				}
				if (layerRetrieverCount.isEmpty())
				{
					LoadingIconAnimator.get().removeListener(this);
				}
			}
		}
	}

	@Override
	public void nextFrame(Image image)
	{
		fireLabelProviderChangedFor(false);
	}

	private void fireLabelProviderChangedFor(boolean allElements)
	{
		synchronized (layerRetrieverCount)
		{
			final Object[] elements = allElements ? null : retrievingElements.values().toArray();
			Display.getDefault().asyncExec(new Runnable()
			{
				@Override
				public void run()
				{
					fireLabelProviderChanged(new LabelProviderChangedEvent(LayerTreeLabelProvider.this, elements));
				}
			});
		}
	}

	private boolean isLayerNodeRetrieving(ILayerTreeNode node)
	{
		if (node instanceof LayerNode)
		{
			Layer layer = ((LayerNode) node).getLayer();
			synchronized (layerRetrieverCount)
			{
				if (layerRetrieverCount.containsKey(layer))
				{
					retrievingElements.put(layer, node);
					return true;
				}
			}
		}
		return false;
	}
}
