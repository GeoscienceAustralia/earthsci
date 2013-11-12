/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.layer.intent;

import gov.nasa.worldwind.layers.Layer;

import java.net.URI;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.util.UTF8URLEncoder;
import au.gov.ga.earthsci.core.model.ModelStatus;
import au.gov.ga.earthsci.intent.AbstractIntentCallback;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IntentManager;
import au.gov.ga.earthsci.intent.dispatch.DispatchFilter;
import au.gov.ga.earthsci.intent.dispatch.Dispatcher;
import au.gov.ga.earthsci.layer.IPersistentLayer;
import au.gov.ga.earthsci.layer.LegacyLayerHelper;
import au.gov.ga.earthsci.layer.Messages;
import au.gov.ga.earthsci.layer.tree.ILayerNode;
import au.gov.ga.earthsci.notification.NotificationCategory;
import au.gov.ga.earthsci.notification.NotificationManager;

/**
 * Layer loading helper which uses the Intent system for loading layers from
 * files/URIs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class IntentLayerLoader
{
	public final static String LAYER_URI_KEY = "au.gov.ga.earthsci.layer.intent.uri"; //$NON-NLS-1$
	private final static Logger logger = LoggerFactory.getLogger(IntentLayerLoader.class);

	/**
	 * Start an Intent to load a layer from a URI, and set the loaded layer on
	 * the given layer node.
	 * 
	 * @param uri
	 *            URI to load the layer resource from
	 * @param layerNode
	 *            Layer node to set the loaded layer on
	 * @param context
	 *            Eclipse context
	 */
	public static void load(URI uri, ILayerNode layerNode, IEclipseContext context)
	{
		if (layerNode.isLayerSet())
		{
			return;
		}

		LayerLoadIntent intent = new LayerLoadIntent(context, layerNode);
		intent.setURI(uri);
		intent.setExpectedReturnType(Layer.class);
		layerNode.setLoading(true);
		IntentManager.getInstance().start(intent, callback, context);
	}

	/**
	 * Intent callback for the intent.
	 */
	protected static IIntentCallback callback = new AbstractIntentCallback()
	{
		@Override
		public void completed(final Object result, Intent intent)
		{
			final LayerLoadIntent layerIntent = (LayerLoadIntent) intent;
			layerIntent.layerNode.setLoading(false);
			if (result instanceof Layer)
			{
				layerIntent.layerNode.setStatus(ModelStatus.ok());
				Layer layer = (Layer) result;
				layer.setValue(LAYER_URI_KEY, intent.getURI());
				IPersistentLayer persistentLayer = LegacyLayerHelper.wrap(layer);
				layerIntent.layerNode.setLayer(persistentLayer);
			}
			else if (result != null)
			{
				layerIntent.layerNode.removeFromParent();
				final DispatchFilter filter = Dispatcher.getInstance().findFilter(result);
				if (filter != null)
				{
					final Shell shell = layerIntent.context.get(Shell.class);
					shell.getDisplay().asyncExec(new Runnable()
					{
						@Override
						public void run()
						{
							if (MessageDialog.openConfirm(shell, Messages.IntentLayerLoader_UnknownLayerTitle,
									Messages.bind(Messages.IntentLayerLoader_UnknownLayerMessage, filter.getName())))
							{
								Dispatcher.getInstance().dispatch(result, layerIntent, layerIntent.context);
							}
						}
					});
				}
				else
				{
					error(new Exception("Expected " + Layer.class.getSimpleName() + ", got " //$NON-NLS-1$ //$NON-NLS-2$
							+ result.getClass().getSimpleName()), intent);
				}
			}
			else
			{
				error(new Exception("Intent produced null result"), intent); //$NON-NLS-1$
			}
		}

		@Override
		public void error(Exception e, Intent intent)
		{
			String uriString = intent.getURI() != null ? UTF8URLEncoder.decode(intent.getURI().toString()) : "null"; //$NON-NLS-1$
			String title = Messages.IntentLayerLoader_FailedLoadNotificationTitle;
			String message =
					Messages.IntentLayerLoader_FailedLoadNotificationDescription + uriString
							+ ": " + e.getLocalizedMessage(); //$NON-NLS-1$

			LayerLoadIntent layerIntent = (LayerLoadIntent) intent;
			layerIntent.layerNode.setStatus(ModelStatus.error(message, e));
			layerIntent.layerNode.setLoading(false);

			//cannot let this notification require acknowledgement during initial loading (layer unpersistence)
			//as it causes the parts to be created incorrectly (bad parent window perhaps?)
			NotificationManager.error(title, message, NotificationCategory.FILE_IO, e);
			logger.error(message, e);
		}

		@Override
		public void canceled(Intent intent)
		{
			LayerLoadIntent layerIntent = (LayerLoadIntent) intent;
			Exception e = new Exception(Messages.IntentLayerLoader_LoadCanceledDescription);
			layerIntent.layerNode.setStatus(ModelStatus.error(e.getLocalizedMessage(), e));
			layerIntent.layerNode.setLoading(false);
		}

		@Override
		public void aborted(Intent intent)
		{
			LayerLoadIntent layerIntent = (LayerLoadIntent) intent;
			layerIntent.layerNode.setLoading(false);
			layerIntent.layerNode.removeFromParent();
		}
	};

	/**
	 * Intent subclass that stores a layer node and Eclipse context.
	 */
	protected static class LayerLoadIntent extends Intent
	{
		private final IEclipseContext context;
		private final ILayerNode layerNode;

		public LayerLoadIntent(IEclipseContext context, ILayerNode layerNode)
		{
			this.context = context;
			this.layerNode = layerNode;
		}
	}
}
