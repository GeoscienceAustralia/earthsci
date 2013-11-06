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
package au.gov.ga.earthsci.catalog;

import java.net.URI;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.util.UTF8URLEncoder;
import au.gov.ga.earthsci.intent.AbstractIntentCallback;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IntentManager;
import au.gov.ga.earthsci.intent.dispatch.DispatchFilter;
import au.gov.ga.earthsci.intent.dispatch.Dispatcher;
import au.gov.ga.earthsci.notification.NotificationCategory;
import au.gov.ga.earthsci.notification.NotificationManager;

/**
 * Catalog loading helper which uses the Intent system for loading catalogs from
 * files/URIs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class IntentCatalogLoader
{
	private static final Object replaceNodeSemaphore = new Object();
	private final static Logger logger = LoggerFactory.getLogger(IntentCatalogLoader.class);

	public static void load(URI uri, ICatalogTreeNode placeholder, IEclipseContext context)
	{
		load(uri, null, placeholder, context);
	}

	public static void load(URI uri, IContentType contentType, ICatalogTreeNode placeholder, IEclipseContext context)
	{
		CatalogLoadIntent intent = new CatalogLoadIntent(placeholder, context);
		intent.setURI(uri);
		intent.setContentType(contentType);
		intent.setExpectedReturnType(ICatalogTreeNode.class);
		IntentManager.getInstance().start(intent, callback, context);
	}

	protected static IIntentCallback callback = new AbstractIntentCallback()
	{
		@Override
		public void completed(final Object result, Intent intent)
		{
			final CatalogLoadIntent catalogIntent = (CatalogLoadIntent) intent;
			if (result instanceof ICatalogTreeNode)
			{
				ICatalogTreeNode node = (ICatalogTreeNode) result;
				replaceWithNode(catalogIntent, node);
			}
			else
			{
				final DispatchFilter filter = Dispatcher.getInstance().findFilter(result);
				if (filter != null)
				{
					final Shell shell = catalogIntent.context.get(Shell.class);
					shell.getDisplay().asyncExec(new Runnable()
					{
						@Override
						public void run()
						{
							MessageDialog dialog =
									new MessageDialog(shell,
											Messages.IntentCatalogLoader_UnknownCatalogDialogTitle, null,
											Messages.bind(Messages.IntentCatalogLoader_UnknownCatalogDialogMessage,
													filter.getName()),
											MessageDialog.QUESTION,
											new String[] {
													Messages.IntentCatalogLoader_UnknownCatalogDialogYes,
													Messages.IntentCatalogLoader_UnknownCatalogDialogNo
											}, 0);
							if (dialog.open() == 0)
							{
								replaceWithNode(catalogIntent, null);
								Dispatcher.getInstance().dispatch(result, catalogIntent, catalogIntent.context);
							}
						}
					});
				}
				else
				{
					error(new Exception(
							Messages.bind(Messages.IntentCatalogLoader_UnknownResultMessage,
									ICatalogTreeNode.class.getSimpleName(),
									result.getClass().getSimpleName())),
							intent);
				}
			}
		}

		@Override
		public void error(Exception e, Intent intent)
		{
			//TODO cannot let this notification require acknowledgement during initial loading (catalog unpersistence)
			//as it causes the parts to be created incorrectly (bad parent window perhaps?)
			String title = Messages.bind(Messages.IntentCatalogLoader_FailedToLoadCatalogTitle,
					UTF8URLEncoder.decode(intent.getURI().toString()));
			String message = Messages.bind(Messages.IntentCatalogLoader_FailedToLoadCatalogMessage,
					UTF8URLEncoder.decode(intent.getURI().toString()),
					e.getLocalizedMessage());

			NotificationManager.error(title, message, NotificationCategory.FILE_IO, e);
			logger.error(message, e);

			CatalogLoadIntent catalogIntent = (CatalogLoadIntent) intent;
			ErrorCatalogTreeNode errorNode = new ErrorCatalogTreeNode(intent.getURI(), title, e);
			errorNode.setRemoveable(true);
			replaceWithNode(catalogIntent, errorNode);
		}

		@Override
		public void canceled(Intent intent)
		{
			CatalogLoadIntent catalogIntent = (CatalogLoadIntent) intent;
			ErrorCatalogTreeNode errorNode = new ErrorCatalogTreeNode(
					intent.getURI(),
					new Exception(Messages.IntentCatalogLoader_CatalogLoadCanceledMessage));
			errorNode.setRemoveable(true);
			replaceWithNode(catalogIntent, errorNode);
		}

		@Override
		public void aborted(Intent intent)
		{
			replaceWithNode((CatalogLoadIntent) intent, null);
		}

		private void replaceWithNode(CatalogLoadIntent intent, ICatalogTreeNode node)
		{
			//only allow one node to be replaced at a time; synchronize on a static object:
			synchronized (replaceNodeSemaphore)
			{
				ICatalogTreeNode placeholder = intent.replacement != null ? intent.replacement : intent.placeholder;
				if (placeholder.getParent() == null)
				{
					//If placeholder parent is null, probably means the placeholder has been removed from
					//the tree by the user, which means it shouldn't be replaced by the actual loaded node.
					return;
				}

				intent.replacement = node;
				if (node != null)
				{
					if (placeholder.getLabel() != null)
					{
						node.setLabel(placeholder.getLabel());
					}
					placeholder.getParent().replaceChild(placeholder, node);
				}
				else
				{
					//replaced with null means aborted; just remove the placeholder
					placeholder.removeFromParent();
				}
			}
		}
	};

	protected static class CatalogLoadIntent extends Intent
	{
		private final ICatalogTreeNode placeholder;
		private final IEclipseContext context;
		private ICatalogTreeNode replacement;

		public CatalogLoadIntent(ICatalogTreeNode placeholder, IEclipseContext context)
		{
			this.placeholder = placeholder;
			this.context = context;
		}
	}
}
