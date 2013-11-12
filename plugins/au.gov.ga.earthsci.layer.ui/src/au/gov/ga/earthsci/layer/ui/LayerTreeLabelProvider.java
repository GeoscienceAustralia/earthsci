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

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import au.gov.ga.earthsci.application.IconLoader;
import au.gov.ga.earthsci.application.ImageRegistry;
import au.gov.ga.earthsci.common.ui.util.SWTUtil;
import au.gov.ga.earthsci.common.ui.util.TextStyler;
import au.gov.ga.earthsci.common.ui.viewers.IFireableLabelProvider;
import au.gov.ga.earthsci.core.retrieve.IRetrieval;
import au.gov.ga.earthsci.core.retrieve.IRetrievalListener;
import au.gov.ga.earthsci.core.retrieve.IRetrievalService;
import au.gov.ga.earthsci.core.retrieve.IRetrievalServiceListener;
import au.gov.ga.earthsci.core.retrieve.RetrievalAdapter;
import au.gov.ga.earthsci.core.retrieve.RetrievalServiceFactory;
import au.gov.ga.earthsci.layer.elevation.IElevationModelLayer;
import au.gov.ga.earthsci.layer.tree.FolderNode;
import au.gov.ga.earthsci.layer.tree.ILayerNode;
import au.gov.ga.earthsci.layer.tree.ILayerTreeNode;
import au.gov.ga.earthsci.layer.tree.LayerNode;

/**
 * Label provider for the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerTreeLabelProvider extends DecoratingStyledCellLabelProvider implements IFireableLabelProvider
{
	private final LayerTreeLabelProviderDelegate delegate;
	private final IRetrievalService retrievalService;
	private final Set<IRetrieval> refreshingRetrievals = new HashSet<IRetrieval>();

	private Color downloadBackgroundColor;
	private Color downloadForegroundColor;
	private static final int DOWNLOAD_WIDTH = 50;

	public LayerTreeLabelProvider()
	{
		this(new LayerTreeLabelProviderDelegate());
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
		if (downloadBackgroundColor != null)
		{
			downloadBackgroundColor.dispose();
			downloadForegroundColor.dispose();
			downloadBackgroundColor = null;
			downloadForegroundColor = null;
		}
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
			LayerNode layerNode = (LayerNode) element;
			Layer layer = layerNode.getLayer();
			List<IRetrieval> retrievals = Arrays.asList(retrievalService.getRetrievals(layer));
			if (retrievals.size() == 0 && layer instanceof IElevationModelLayer)
			{
				ElevationModel elevationModel = ((IElevationModelLayer) layer).getElevationModel();
				retrievals = new ArrayList<IRetrieval>();
				addRetrievalsForElevationModel(elevationModel, retrievals);
			}
			if (retrievals.size() > 0)
			{
				if (downloadBackgroundColor == null)
				{
					Color listBackground = event.display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
					boolean darken = SWTUtil.shouldDarken(listBackground);
					downloadBackgroundColor = darken ? SWTUtil.darker(listBackground) : SWTUtil.lighter(listBackground);
					downloadForegroundColor =
							darken ? SWTUtil.darker(downloadBackgroundColor) : SWTUtil.lighter(downloadBackgroundColor);
				}

				GC gc = event.gc;
				Color oldBackground = gc.getBackground();
				Color oldForeground = gc.getForeground();

				float percentage = 0;
				for (IRetrieval retrieval : retrievals)
				{
					percentage += Math.max(0, retrieval.getPercentage());
				}
				percentage /= retrievals.size();

				int height = event.height / 2;
				int width = (int) (DOWNLOAD_WIDTH * percentage);
				gc.setBackground(downloadBackgroundColor);
				gc.setForeground(downloadForegroundColor);
				gc.fillRectangle(event.x + event.width, event.y + (event.height - height) / 2, width, height);
				gc.drawRectangle(event.x + event.width, event.y + (event.height - height) / 2, DOWNLOAD_WIDTH, height);

				gc.setBackground(oldBackground);
				gc.setForeground(oldForeground);
			}
		}
		super.paint(event, element);
	}

	private void addRetrievalsForElevationModel(ElevationModel elevationModel, List<IRetrieval> retrievals)
	{
		IRetrieval[] array = retrievalService.getRetrievals(elevationModel);
		if (array.length > 0)
		{
			retrievals.addAll(Arrays.asList(array));
		}
		if (elevationModel instanceof CompoundElevationModel)
		{
			CompoundElevationModel cem = (CompoundElevationModel) elevationModel;
			for (ElevationModel child : cem.getElevationModels())
			{
				addRetrievalsForElevationModel(child, retrievals);
			}
		}
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
			else if (caller instanceof Layer || caller instanceof ElevationModel)
			{
				WeakReference<ILayerNode> weak = delegate.weakLayerToNodeMap.get(caller);
				if (weak == null)
				{
					weak = delegate.weakElevationModelToNodeMap.get(caller);
				}
				if (weak != null)
				{
					ILayerNode node = weak.get();
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

	@Override
	public void fireLabelProviderChanged(LabelProviderChangedEvent event)
	{
		super.fireLabelProviderChanged(event);
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

	private static class LayerTreeLabelProviderDelegate extends LabelProvider implements ILabelDecorator,
			IStyledLabelProvider, IFireableLabelProvider
	{
		private WeakHashMap<Layer, WeakReference<ILayerNode>> weakLayerToNodeMap =
				new WeakHashMap<Layer, WeakReference<ILayerNode>>();
		private WeakHashMap<ElevationModel, WeakReference<ILayerNode>> weakElevationModelToNodeMap =
				new WeakHashMap<ElevationModel, WeakReference<ILayerNode>>();

		private final IconLoader iconLoader = new IconLoader(this);

		private boolean disposed = false;

		private final Font subscriptFont;
		private final TextStyler informationStyler = new TextStyler();
		private final TextStyler legendStyler = new TextStyler();

		public LayerTreeLabelProviderDelegate()
		{
			FontData[] fontDatas = Display.getDefault().getSystemFont().getFontData();
			for (FontData fontData : fontDatas)
			{
				fontData.setStyle(SWT.BOLD);
				fontData.setHeight((int) (fontData.getHeight() * 0.8));
			}
			subscriptFont = new Font(Display.getDefault(), fontDatas);
			informationStyler.style.foreground = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
			informationStyler.style.font = subscriptFont;
			legendStyler.style.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
			legendStyler.style.font = subscriptFont;
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
		public String getText(Object element)
		{
			if (element instanceof ILayerNode)
			{
				ILayerNode layerNode = (ILayerNode) element;
				Layer layer = layerNode.getGrandLayer();

				if (layer != null)
				{
					WeakReference<ILayerNode> layerNodeReference = new WeakReference<ILayerNode>(layerNode);
					weakLayerToNodeMap.put(layer, layerNodeReference);
					if (layer instanceof IElevationModelLayer)
					{
						ElevationModel elevationModel = ((IElevationModelLayer) layer).getElevationModel();
						addElevationModelToMap(elevationModel, layerNodeReference);
					}
				}

				String label = layerNode.getLabelOrName();
				if (layerNode.getOpacity() < 1)
				{
					label += String.format(" (%d%%)", (int) (layerNode.getOpacity() * 100)); //$NON-NLS-1$
				}

				return label;
			}
			else if (element instanceof FolderNode)
			{
				FolderNode folder = (FolderNode) element;
				return folder.getLabelOrName();
			}
			return super.getText(element);
		}

		private void addElevationModelToMap(ElevationModel elevationModel, WeakReference<ILayerNode> layerNodeReference)
		{
			weakElevationModelToNodeMap.put(elevationModel, layerNodeReference);
			if (elevationModel instanceof CompoundElevationModel)
			{
				CompoundElevationModel cem = (CompoundElevationModel) elevationModel;
				for (ElevationModel child : cem.getElevationModels())
				{
					addElevationModelToMap(child, layerNodeReference);
				}
			}
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
			StyledString string = new StyledString(getText(element));
			if (element instanceof ILayerTreeNode)
			{
				ILayerTreeNode layerNode = (ILayerTreeNode) element;
				if (layerNode.getInformationURL() != null || layerNode.getLegendURL() != null)
				{
					string.append(" "); //$NON-NLS-1$
					if (layerNode.getInformationURL() != null)
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
