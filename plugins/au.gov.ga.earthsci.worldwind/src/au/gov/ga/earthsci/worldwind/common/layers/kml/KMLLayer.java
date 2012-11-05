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
package au.gov.ga.earthsci.worldwind.common.layers.kml;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.ogc.kml.io.KMLDoc;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.util.layertree.KMLLayerTreeNode;
import gov.nasa.worldwind.util.tree.TreeNode;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.logging.Level;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.downloader.Downloader;
import au.gov.ga.earthsci.worldwind.common.downloader.RetrievalHandler;
import au.gov.ga.earthsci.worldwind.common.downloader.RetrievalResult;
import au.gov.ga.earthsci.worldwind.common.layers.Hierarchical;
import au.gov.ga.earthsci.worldwind.common.layers.kml.relativeio.RelativeKMLFile;
import au.gov.ga.earthsci.worldwind.common.layers.kml.relativeio.RelativeKMLInputStream;
import au.gov.ga.earthsci.worldwind.common.layers.kml.relativeio.RelativeKMZFile;
import au.gov.ga.earthsci.worldwind.common.layers.kml.relativeio.RelativeKMZInputStream;
import au.gov.ga.earthsci.worldwind.common.util.Loader;
import au.gov.ga.earthsci.worldwind.common.util.URLUtil;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * A {@link Layer} that parses and renders KML content from a provided KML
 * source.
 */
public class KMLLayer extends RenderableLayer implements Loader, Hierarchical
{
	private boolean loading = false;
	private LoadingListenerList loadingListeners = new LoadingListenerList();
	private HierarchicalListenerList hierarchicalListeners = new HierarchicalListenerList();
	private Object lock = new Object();
	private TreeNode node;

	public KMLLayer(Element domElement, AVList params)
	{
		this(getParamsFromDocument(domElement, params));
	}

	public static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (domElement == null)
		{
			String message = Logging.getMessage("nullValue.DocumentIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();

		XMLUtil.checkAndSetURLParam(domElement, params, AVKey.URL, "URL", xpath);

		return params;
	}

	public KMLLayer(AVList params)
	{
		setValues(params);
		final URL url = (URL) params.getValue(AVKey.URL);

		if (url == null)
		{
			throw new IllegalArgumentException("KML url undefined");
		}

		loading = true;
		notifyLoadingListeners();

		RetrievalHandler handler = new RetrievalHandler()
		{
			@Override
			public void handle(RetrievalResult result)
			{
				loadKml(url, result.getAsInputStream());
			}
		};
		Downloader.downloadIfModified(url, handler, handler, false);
	}

	public KMLLayer(URL sourceUrl, InputStream stream, AVList params)
	{
		setValues(params);
		setValue(AVKey.URL, sourceUrl);
		loadKml(sourceUrl, stream);
	}

	protected void loadKml(final URL url, final InputStream inputStream)
	{
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				//don't allow multiple threads to attempt load at the same time
				synchronized (lock)
				{
					loading = true;
					notifyLoadingListeners();

					try
					{
						String contentType = WWIO.makeMimeTypeForSuffix(WWIO.getSuffix(url.getPath()));
						boolean isKmz = KMLConstants.KMZ_MIME_TYPE.equals(contentType);
						File file = URLUtil.urlToFile(url);

						KMLDoc doc;
						if (file != null)
						{
							doc =
									isKmz ? new RelativeKMZFile(URLUtil.urlToFile(url), url.toString(), null)
											: new RelativeKMLFile(URLUtil.urlToFile(url), url.toString(), null);
						}
						else
						{
							InputStream stream = inputStream != null ? inputStream : WWIO.openStream(url);
							doc =
									isKmz ? new RelativeKMZInputStream(stream, url.toURI(), url.toString(), null)
											: new RelativeKMLInputStream(stream, url.toURI(), url.toString(), null);
						}

						KMLRoot root;
						try
						{
							//Attempt to create an instance of the CustomKMLRoot object, for loading
							//COLLADA models. This is done via reflection, so there's no requirement
							//for the library to be in the classpath.
							Class<?> colladaKmlRootClass =
									Class.forName("gov.nasa.worldwind.ogc.kml.custom.CustomKMLRoot");
							Constructor<?> c = colladaKmlRootClass.getConstructor(KMLDoc.class);
							root = (KMLRoot) c.newInstance(doc);
						}
						catch (Exception e)
						{
							root = new KMLRoot(doc);
						}

						root.parse();
						KMLController controller = new KMLController(root);
						addRenderable(controller);
						setName(formName(url, root));

						node = new KMLLayerTreeNode(KMLLayer.this, root);
						notifyHierarchicalListeners(node);
					}
					catch (Exception e)
					{
						String message = "Error parsing KML";
						Logging.logger().log(Level.SEVERE, message, e);
						throw new IllegalArgumentException(message, e);
					}
					finally
					{
						loading = false;
						notifyLoadingListeners();
					}
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	//from KMLViewer.java
	public static String formName(Object kmlSource, KMLRoot kmlRoot)
	{
		KMLAbstractFeature rootFeature = kmlRoot.getFeature();

		if (rootFeature != null && !WWUtil.isEmpty(rootFeature.getName()))
			return rootFeature.getName();

		if (kmlSource instanceof File)
			return ((File) kmlSource).getName();

		if (kmlSource instanceof URL)
			return ((URL) kmlSource).getPath();

		if (kmlSource instanceof String && WWIO.makeURL((String) kmlSource) != null)
			return WWIO.makeURL((String) kmlSource).getPath();

		return "KML Layer";
	}

	@Override
	public boolean isLoading()
	{
		return loading;
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		loadingListeners.add(listener);
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		loadingListeners.remove(listener);
	}

	protected void notifyLoadingListeners()
	{
		loadingListeners.notifyListeners(isLoading());
	}

	protected void notifyHierarchicalListeners(TreeNode node)
	{
		hierarchicalListeners.notifyListeners(this, node);
	}

	@Override
	public void addHierarchicalListener(HierarchicalListener listener)
	{
		synchronized (lock)
		{
			hierarchicalListeners.add(listener);
			if (node != null)
			{
				//already loaded, so notify immediately
				notifyHierarchicalListeners(node);
			}
		}
	}

	@Override
	public void removeHierarchicalListener(HierarchicalListener listener)
	{
		hierarchicalListeners.remove(listener);
	}
}
