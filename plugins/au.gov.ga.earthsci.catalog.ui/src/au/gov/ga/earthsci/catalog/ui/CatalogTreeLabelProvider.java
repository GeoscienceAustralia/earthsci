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
package au.gov.ga.earthsci.catalog.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
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
import au.gov.ga.earthsci.application.ImageRegistry;
import au.gov.ga.earthsci.application.LoadingIconHelper;
import au.gov.ga.earthsci.catalog.ErrorCatalogTreeNode;
import au.gov.ga.earthsci.catalog.ICatalogModel;
import au.gov.ga.earthsci.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.catalog.LoadingCatalogTreeNode;
import au.gov.ga.earthsci.common.collection.HashSetHashMap;
import au.gov.ga.earthsci.common.collection.SetMap;
import au.gov.ga.earthsci.common.ui.viewers.IControlProvider;
import au.gov.ga.earthsci.common.util.ILabeled;
import au.gov.ga.earthsci.common.util.INamed;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.worldwind.ITreeModel;

/**
 * A {@link IControlProvider} for the catalog browser tree
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Creatable
public class CatalogTreeLabelProvider extends LabelProvider implements ILabelDecorator, IFireableLabelProvider,
		IStyledLabelProvider
{
	private final org.eclipse.jface.resource.ImageRegistry decoratedImageCache =
			new org.eclipse.jface.resource.ImageRegistry();

	@Inject
	private ICatalogBrowserController controller;

	private IconLoader iconLoader = new IconLoader(this);
	private LoadingIconHelper nodeLoader = new LoadingIconHelper(this);

	@Inject
	private ICatalogModel catalogModel;

	@Inject
	private ITreeModel layerModel;

	private boolean disposed = false;

	@PostConstruct
	public void postConstruct()
	{
		setupListeners();
		addListeners();

		informationColor = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
		FontData[] fontDatas = Display.getDefault().getSystemFont().getFontData();
		for (FontData fontData : fontDatas)
		{
			fontData.setStyle(SWT.BOLD);
			fontData.setHeight((int) (fontData.getHeight() * 0.8));
		}
		subscriptFont = new Font(Display.getDefault(), fontDatas);
	}

	@PreDestroy
	public void preDestroy()
	{
		removeListeners();
		subscriptFont.dispose();
	}

	@Override
	public Image getImage(final Object element)
	{
		if (!(element instanceof ICatalogTreeNode))
		{
			return null;
		}

		if (element instanceof LoadingCatalogTreeNode)
		{
			return nodeLoader.getLoadingIcon(element);
		}

		ICatalogTreeNode node = (ICatalogTreeNode) element;
		URL url = node.getIconURL();
		return getImage(element, url);
	}

	@Override
	public String getText(Object element)
	{
		if (!(element instanceof ICatalogTreeNode))
		{
			if (element instanceof ILabeled)
			{
				return ((ILabeled) element).getLabelOrName();
			}
			else if (element instanceof INamed)
			{
				return ((INamed) element).getName();
			}
			return element.toString();
		}

		ICatalogTreeNode node = (ICatalogTreeNode) element;
		if (element instanceof LoadingCatalogTreeNode || element instanceof ErrorCatalogTreeNode)
		{
			return node.getName();
		}
		return node.getLabelOrName();
	}

	@Override
	public void dispose()
	{
		if (disposed)
		{
			return;
		}
		disposed = true;

		super.dispose();
		decoratedImageCache.dispose();
		iconLoader.dispose();

		//TODO we probably want to call dispose on the CatalogTreeNodeControlProviderRegistry at some point
		//but maybe not here because it feels wrong to dispose of a static factory's resources in a
		//non-static context (ie every time the catalog part is closed).
	}

	@Override
	public Image decorateImage(Image image, Object element)
	{
		if (!(element instanceof ICatalogTreeNode) || !((ICatalogTreeNode) element).isLayerNode())
		{
			return null;
		}

		ICatalogTreeNode node = (ICatalogTreeNode) element;

		if (!controller.existsInLayerModel(node.getLayerURI()))
		{
			return null;
		}

		return getDecoratedIcon(image);
	}

	@Override
	public String decorateText(String text, Object element)
	{
		if (!(element instanceof ICatalogTreeNode) || !((ICatalogTreeNode) element).isLayerNode())
		{
			return null;
		}

		ICatalogTreeNode node = (ICatalogTreeNode) element;

		if (!controller.existsInLayerModel(node.getLayerURI()))
		{
			return null;
		}
		return text + "*"; //$NON-NLS-1$
	}

	private Image getImage(Object element, URL imageURL)
	{
		if (imageURL == null)
		{
			return null;
		}

		return iconLoader.getImage(element, imageURL);
	}

	private Image getDecoratedIcon(Image base)
	{
		String key = base.hashCode() + ""; //$NON-NLS-1$

		if (base.isDisposed())
		{
			decoratedImageCache.remove(key);
			return null;
		}

		Image decorated = decoratedImageCache.get(key);
		if (decorated != null)
		{
			return decorated;
		}

		decorated =
				new DecorationOverlayIcon(base, ImageRegistry.getInstance().getDescriptor(
						ImageRegistry.DECORATION_INCLUDED), IDecoration.BOTTOM_RIGHT).createImage();
		decoratedImageCache.put(key, decorated);
		return decorated;
	}

	@Override
	public StyledString getStyledText(Object element)
	{
		StyledString string = new StyledString(getText(element));
		if (element instanceof ICatalogTreeNode)
		{
			ICatalogTreeNode node = (ICatalogTreeNode) element;
			URL infoURL = node.getInformationURL();
			if (infoURL != null)
			{
				string.append("  i", informationStyler); //$NON-NLS-1$
			}
		}
		return string;
	}

	@Override
	public void fireLabelProviderChanged(LabelProviderChangedEvent event)
	{
		super.fireLabelProviderChanged(event);
	}

	private PropertyChangeListener catalogModelChildrenListener;
	private PropertyChangeListener catalogModelURIListener;
	private PropertyChangeListener layerModelChildrenListener;
	private PropertyChangeListener layerModelURIListener;

	private void setupListeners()
	{
		final SetMap<URI, ICatalogTreeNode> uriElements = new HashSetHashMap<URI, ICatalogTreeNode>();

		catalogModelChildrenListener = new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				List<?> oldChildren = (List<?>) evt.getOldValue();
				List<?> newChildren = (List<?>) evt.getNewValue();
				addOrRemoveNodes(oldChildren, false);
				addOrRemoveNodes(newChildren, true);
			}

			private void addOrRemoveNodes(List<?> nodes, boolean add)
			{
				if (nodes != null)
				{
					for (Object n : nodes)
					{
						if (n instanceof ICatalogTreeNode)
						{
							ICatalogTreeNode node = (ICatalogTreeNode) n;
							URI uri = node.getLayerURI();
							if (uri != null)
							{
								if (add)
								{
									uriElements.putSingle(uri, node);
								}
								else
								{
									uriElements.removeSingle(uri, node);
								}
							}
						}
					}
				}
			}
		};

		catalogModelURIListener = new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				Object source = evt.getSource();
				if (source instanceof ICatalogTreeNode)
				{
					ICatalogTreeNode node = (ICatalogTreeNode) source;
					URI oldURI = (URI) evt.getOldValue();
					URI newURI = (URI) evt.getNewValue();
					if (oldURI != null)
					{
						uriElements.removeSingle(oldURI, node);
					}
					if (newURI != null)
					{
						uriElements.putSingle(newURI, node);
					}
				}
			}
		};

		layerModelChildrenListener = new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				Set<URI> changedURIs = new HashSet<URI>();
				List<?> oldChildren = (List<?>) evt.getOldValue();
				List<?> newChildren = (List<?>) evt.getNewValue();
				addURIsToSet(oldChildren, changedURIs);
				addURIsToSet(newChildren, changedURIs);
				updateElementsForURIs(changedURIs, uriElements);
			}

			private void addURIsToSet(List<?> nodes, Set<URI> list)
			{
				if (nodes != null)
				{
					for (Object n : nodes)
					{
						if (n instanceof ILayerTreeNode)
						{
							ILayerTreeNode node = (ILayerTreeNode) n;
							URI uri = node.getURI();
							if (uri != null)
							{
								list.add(uri);
							}
						}
					}
				}
			}
		};

		layerModelURIListener = new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				URI oldURI = (URI) evt.getOldValue();
				URI newURI = (URI) evt.getNewValue();
				Set<URI> uris = new HashSet<URI>();
				if (oldURI != null)
				{
					uris.add(oldURI);
				}
				if (newURI != null)
				{
					uris.add(newURI);
				}
				updateElementsForURIs(uris, uriElements);
			}
		};
	}

	private void updateElementsForURIs(Collection<URI> uris, SetMap<URI, ICatalogTreeNode> uriElements)
	{
		Set<ICatalogTreeNode> elements = new HashSet<ICatalogTreeNode>();
		for (URI uri : uris)
		{
			Set<ICatalogTreeNode> nodes = uriElements.get(uri);
			if (nodes != null)
			{
				elements.addAll(nodes);
			}
		}
		Object[] elementsArray = elements.toArray();
		fireLabelProviderChanged(new LabelProviderChangedEvent(CatalogTreeLabelProvider.this, elementsArray));
	}

	private void addListeners()
	{
		catalogModel.getRoot().addDescendantPropertyChangeListener("children", catalogModelChildrenListener); //$NON-NLS-1$
		catalogModel.getRoot().addDescendantPropertyChangeListener("layerURI", catalogModelURIListener); //$NON-NLS-1$
		layerModel.getRootNode().addDescendantPropertyChangeListener("children", layerModelChildrenListener); //$NON-NLS-1$
		layerModel.getRootNode().addDescendantPropertyChangeListener("uRI", layerModelURIListener); //$NON-NLS-1$
	}

	private void removeListeners()
	{
		catalogModel.getRoot().removePropertyChangeListener("children", catalogModelChildrenListener); //$NON-NLS-1$
		catalogModel.getRoot().removePropertyChangeListener("layerURI", catalogModelURIListener); //$NON-NLS-1$
		layerModel.getRootNode().removePropertyChangeListener("children", layerModelChildrenListener); //$NON-NLS-1$
		layerModel.getRootNode().removePropertyChangeListener("uRI", layerModelURIListener); //$NON-NLS-1$
	}

	private Color informationColor;
	private Font subscriptFont;
	private final Styler informationStyler = new Styler()
	{
		@Override
		public void applyStyles(TextStyle textStyle)
		{
			textStyle.foreground = informationColor;
			textStyle.font = subscriptFont;
		}
	};
}
