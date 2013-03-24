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
package au.gov.ga.earthsci.core.model.layer;

import gov.nasa.worldwind.layers.Layer;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.core.model.ModelStatus;
import au.gov.ga.earthsci.core.util.UTF8URLEncoder;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IntentManager;
import au.gov.ga.earthsci.intent.dispatch.DispatchFilter;
import au.gov.ga.earthsci.intent.dispatch.Dispatcher;
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
	public static void load(LayerNode layerNode, IEclipseContext context)
	{
		if (layerNode.getContentType() == null)
		{
			layerNode.setContentType(Platform.getContentTypeManager().findContentTypeFor(layerNode.getURI().getPath()));
		}
		LayerLoadIntent intent = new LayerLoadIntent(context, layerNode);
		intent.setURI(layerNode.getURI());
		intent.setContentType(layerNode.getContentType());
		intent.setExpectedReturnType(Layer.class);
		IntentManager.getInstance().start(intent, callback, context);
	}

	protected static IIntentCallback callback = new IIntentCallback()
	{
		@Override
		public void completed(final Object result, Intent intent)
		{
			final LayerLoadIntent layerIntent = (LayerLoadIntent) intent;
			if (result instanceof Layer)
			{
				layerIntent.layerNode.setStatus(ModelStatus.ok());
				layerIntent.layerNode.setLayer((Layer) result);
			}
			else
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
							MessageDialog dialog =
									new MessageDialog(shell, "Unknown layer", null,
											"Not a layer. This object can be handled as a " + filter.getName()
													+ ". Do you want to continue?", MessageDialog.QUESTION,
											new String[] { "Yes", "Cancel" }, 0);
							if (dialog.open() == 0)
							{
								Dispatcher.getInstance().dispatch(result, layerIntent.context);
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
		}

		@Override
		public void error(Exception e, Intent intent)
		{
			LayerLoadIntent layerIntent = (LayerLoadIntent) intent;
			layerIntent.layerNode.setStatus(ModelStatus.error(e.getLocalizedMessage(), e));

			//TODO cannot let this notification require acknowledgement during initial loading (layer unpersistence)
			//as it causes the parts to be created incorrectly (bad parent window perhaps?)
			NotificationManager.error(
					Messages.IntentLayerLoader_FailedLoadNotificationTitle,
					Messages.IntentLayerLoader_FailedLoadNotificationDescription
							+ UTF8URLEncoder.decode(intent.getURI().toString()) + ": " + e.getLocalizedMessage(), //$NON-NLS-1$
					NotificationCategory.FILE_IO);
		}
	};

	protected static class LayerLoadIntent extends Intent
	{
		private final IEclipseContext context;
		private final LayerNode layerNode;

		public LayerLoadIntent(IEclipseContext context, LayerNode layerNode)
		{
			this.context = context;
			this.layerNode = layerNode;
		}
	}
}
