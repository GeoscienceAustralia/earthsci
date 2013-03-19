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

import au.gov.ga.earthsci.core.model.ModelStatus;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IIntentCaller;
import au.gov.ga.earthsci.intent.IntentManager;
import au.gov.ga.earthsci.intent.dispatch.Dispatcher;

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
		IntentManager.getInstance().start(intent, caller, context);
	}

	protected static IIntentCaller caller = new IIntentCaller()
	{
		@Override
		public void completed(Intent intent, Object result)
		{
			LayerLoadIntent layerIntent = (LayerLoadIntent) intent;
			if (result instanceof Layer)
			{
				layerIntent.layerNode.setStatus(ModelStatus.ok());
				layerIntent.layerNode.setLayer((Layer) result);
			}
			else
			{
				layerIntent.layerNode.removeFromParent();
				Dispatcher.getInstance().dispatch(result, layerIntent.context);
			}
		}

		@Override
		public void error(Intent intent, Exception e)
		{
			LayerLoadIntent layerIntent = (LayerLoadIntent) intent;
			layerIntent.layerNode.setStatus(ModelStatus.error(e.getLocalizedMessage(), e));

			//TODO this causes an error on load, and makes all parts fail to display
			//perhaps because the dialog error window is open when the parts are created?
			/*NotificationManager.error(
					Messages.IntentLayerLoader_FailedLoadNotificationTitle,
					Messages.IntentLayerLoader_FailedLoadNotificationDescription
							+ UTF8URLEncoder.decode(intent.getURI().toString()) + ": " + e.getLocalizedMessage(), //$NON-NLS-1$
					NotificationCategory.FILE_IO);*/
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
