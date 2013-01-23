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

import gov.nasa.worldwind.layers.Layer;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import au.gov.ga.earthsci.application.IFireableLabelProvider;
import au.gov.ga.earthsci.application.IconLoader;
import au.gov.ga.earthsci.application.ImageRegistry;
import au.gov.ga.earthsci.core.model.layer.FolderNode;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.model.layer.LayerNode;
import au.gov.ga.earthsci.core.retrieve.IRetrieval;
import au.gov.ga.earthsci.core.retrieve.IRetrievalListener;
import au.gov.ga.earthsci.core.retrieve.IRetrievalService;
import au.gov.ga.earthsci.core.retrieve.IRetrievalServiceListener;
import au.gov.ga.earthsci.core.retrieve.RetrievalAdapter;
import au.gov.ga.earthsci.core.retrieve.RetrievalServiceFactory;
import au.gov.ga.earthsci.core.util.SWTUtil;

/**
 * Label provider for the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerTreeLabelProvider extends DecoratingStyledCellLabelProvider
{
	private final LayerTreeLabelProviderDelegate delegate;
	private final IRetrievalService retrievalService;
	private final Set<IRetrieval> refreshingRetrievals = new HashSet<IRetrieval>();

	private static final Color DOWNLOAD_BACKGROUND_COLOR;
	private static final Color DOWNLOAD_FOREGROUND_COLOR;
	private static final int DOWNLOAD_WIDTH = 50;

	static
	{
		Color listBackground = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		boolean darken = SWTUtil.shouldDarken(listBackground);
		DOWNLOAD_BACKGROUND_COLOR = darken ? SWTUtil.darker(listBackground) : SWTUtil.lighter(listBackground);
		DOWNLOAD_FOREGROUND_COLOR =
				darken ? SWTUtil.darker(DOWNLOAD_BACKGROUND_COLOR) : SWTUtil.lighter(DOWNLOAD_BACKGROUND_COLOR);
	}

	public LayerTreeLabelProvider(IObservableMap[] attributeMaps)
	{
		this(new LayerTreeLabelProviderDelegate(attributeMaps));
	}

	private LayerTreeLabelProvider(LayerTreeLabelProviderDelegate delegate)
	{
		super(delegate, delegate, null);
		this.delegate = delegate;
		this.retrievalService = RetrievalServiceFactory.getServiceInstance();
		retrievalService.addListener(retrievalServiceListener);
	}

	void packup()
	{
		retrievalService.removeListener(retrievalServiceListener);
	}

	@Override
	public void update(ViewerCell cell)
	{
		super.update(cell);

		//ensure that the paint method is called too:
		Rectangle bounds = cell.getBounds();
		getViewer().getControl().redraw(bounds.x, bounds.y, bounds.width, bounds.height, true);
	}

	@Override
	protected void paint(Event event, Object element)
	{
		if (element instanceof LayerNode)
		{
			IRetrieval[] retrievals = retrievalService.getRetrievals(((LayerNode) element).getLayer());
			if (retrievals.length > 0)
			{
				GC gc = event.gc;
				Color oldBackground = gc.getBackground();
				Color oldForeground = gc.getForeground();

				float percentage = 0;
				for (IRetrieval retrieval : retrievals)
				{
					percentage += Math.max(0, retrieval.getPercentage());
				}
				percentage /= retrievals.length;

				int height = event.height / 2;
				int width = (int) (DOWNLOAD_WIDTH * percentage);
				gc.setBackground(DOWNLOAD_BACKGROUND_COLOR);
				gc.setForeground(DOWNLOAD_FOREGROUND_COLOR);
				gc.fillRectangle(event.x + event.width, event.y + (event.height - height) / 2, width, height);
				gc.drawRectangle(event.x + event.width, event.y + (event.height - height) / 2, DOWNLOAD_WIDTH, height);

				gc.setBackground(oldBackground);
				gc.setForeground(oldForeground);
			}
		}
		super.paint(event, element);
	}

	private void refreshCallers(final IRetrieval retrieval)
	{
		//don't queue up multiple updates for the same retrieval, as this floods the UI thread with asyncExec's
		if (refreshingRetrievals.contains(retrieval))
		{
			return;
		}

		List<Object> elements = new ArrayList<Object>();
		Object[] callers = retrieval.getCallers();
		for (Object caller : callers)
		{
			if (caller instanceof ILayerTreeNode)
			{
				elements.add(caller);
			}
			else if (caller instanceof Layer)
			{
				WeakReference<LayerNode> weak = delegate.weakLayerToNodeMap.get(caller);
				if (weak != null)
				{
					LayerNode node = weak.get();
					if (node != null)
					{
						elements.add(node);
					}
				}
			}
		}

		if (!elements.isEmpty())
		{
			final Object[] array = elements.toArray();
			final ColumnViewer viewer = getViewer();
			if (viewer != null && !viewer.getControl().isDisposed())
			{
				refreshingRetrievals.add(retrieval);
				viewer.getControl().getDisplay().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						if (!viewer.getControl().isDisposed())
						{
							viewer.update(array, null);
						}
						refreshingRetrievals.remove(retrieval);
					}
				});
			}
		}
	}

	private IRetrievalServiceListener retrievalServiceListener = new IRetrievalServiceListener()
	{
		@Override
		public void retrievalAdded(IRetrieval retrieval)
		{
			refreshCallers(retrieval);
			retrieval.addListener(retrievalListener);
		}

		@Override
		public void retrievalRemoved(IRetrieval retrieval)
		{
			refreshCallers(retrieval);
			retrieval.removeListener(retrievalListener);
		}
	};

	private IRetrievalListener retrievalListener = new RetrievalAdapter()
	{
		@Override
		public void callersChanged(IRetrieval retrieval)
		{
			refreshCallers(retrieval);
		}

		@Override
		public void progress(IRetrieval retrieval)
		{
			refreshCallers(retrieval);
		}
	};

	private static class LayerTreeLabelProviderDelegate extends ObservableMapLabelProvider implements ILabelDecorator,
			IStyledLabelProvider, IFireableLabelProvider
	{
		private WeakHashMap<Layer, WeakReference<LayerNode>> weakLayerToNodeMap =
				new WeakHashMap<Layer, WeakReference<LayerNode>>();

		private final IconLoader iconLoader = new IconLoader(this);

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

		public LayerTreeLabelProviderDelegate(IObservableMap[] attributeMaps)
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

				if (layer.getLayer() != null)
				{
					weakLayerToNodeMap.put(layer.getLayer(), new WeakReference<LayerNode>(layer));
				}

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
				
				if (!node.getStatus().isOk())
				{
					return ImageRegistry.getInstance().get(ImageRegistry.ICON_ERROR);
				}
				
				
				URL imageURL = node.getIconURL();
				if (imageURL != null)
				{
					return iconLoader.getImage(element, imageURL);
				}
				else
				{
					if (element instanceof LayerNode)
					{
						return ImageRegistry.getInstance().get(ImageRegistry.ICON_FILE);
					}
					return ImageRegistry.getInstance().get(ImageRegistry.ICON_FOLDER);
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
	}
}
