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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.core.util.UTF8URLEncoder;
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
	public static void load(URI uri, ICatalogTreeNode placeholder, IEclipseContext context)
	{
		load(uri, null, placeholder, context);
	}

	public static void load(URI uri, IContentType contentType, ICatalogTreeNode placeholder, IEclipseContext context)
	{
		if (contentType == null)
		{
			//TODO this should only happen for file/jar/resourcebundle/... uris
			try
			{
				contentType = Platform.getContentTypeManager().findContentTypeFor(uri.getPath());
			}
			catch (Exception e)
			{
				//ignore
			}
		}
		CatalogLoadIntent intent = new CatalogLoadIntent(placeholder, context);
		intent.setURI(uri);
		intent.setContentType(contentType);
		intent.setExpectedReturnType(ICatalogTreeNode.class);
		IntentManager.getInstance().start(intent, callback, context);
	}

	protected static IIntentCallback callback = new IIntentCallback()
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
									new MessageDialog(shell, "Unknown catalog", null,
											"Not a catalog. This object can be handled as a " + filter.getName()
													+ ". Do you want to continue?", MessageDialog.QUESTION,
											new String[] { "Yes", "Cancel" }, 0);
							if (dialog.open() == 0)
							{
								Dispatcher.getInstance().dispatch(result, catalogIntent.context);
							}
						}
					});
				}
				else
				{
					error(new Exception("Expected " + ICatalogTreeNode.class.getSimpleName() + ", got " //$NON-NLS-1$ //$NON-NLS-2$
							+ result.getClass().getSimpleName()), intent);
				}
			}
		}

		@Override
		public void error(Exception e, Intent intent)
		{
			//TODO cannot let this notification require acknowledgement during initial loading (catalog unpersistence)
			//as it causes the parts to be created incorrectly (bad parent window perhaps?)
			NotificationManager.error(
					"Failed to load catalog",
					"Failed to load catalog from URI " + UTF8URLEncoder.decode(intent.getURI().toString())
							+ ": " + e.getLocalizedMessage(), //$NON-NLS-1$
					NotificationCategory.FILE_IO, e);

			CatalogLoadIntent catalogIntent = (CatalogLoadIntent) intent;
			ErrorCatalogTreeNode errorNode = new ErrorCatalogTreeNode(intent.getURI(), e);
			errorNode.setRemoveable(true);
			replaceWithNode(catalogIntent, errorNode);
		}

		private void replaceWithNode(CatalogLoadIntent intent, ICatalogTreeNode node)
		{
			synchronized (intent)
			{
				ICatalogTreeNode placeholder = intent.replacement != null ? intent.replacement : intent.placeholder;
				if (placeholder.getParent() == null)
				{
					throw new IllegalStateException("Placeholder parent cannot be null"); //$NON-NLS-1$
				}
				intent.replacement = node;
				if (placeholder.getLabel() != null)
				{
					node.setLabel(placeholder.getLabel());
				}
				placeholder.getParent().replaceChild(placeholder, node);
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
