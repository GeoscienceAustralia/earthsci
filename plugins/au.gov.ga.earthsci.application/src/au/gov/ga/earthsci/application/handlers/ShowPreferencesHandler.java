package au.gov.ga.earthsci.application.handlers;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.application.preferences.PreferenceConstants;
import au.gov.ga.earthsci.application.preferences.ScopedPreferenceStore;

public class ShowPreferencesHandler
{
	protected static final String ELMT_PAGE = "page"; //$NON-NLS-1$
	protected static final String ATTR_ID = "id"; //$NON-NLS-1$
	protected static final String ATTR_CATEGORY = "category"; //$NON-NLS-1$
	protected static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	protected static final String ATTR_NAME = "name"; //$NON-NLS-1$

	@Inject
	protected Logger logger;
	@Inject
	protected IExtensionRegistry registry;

	@Execute
	public void execute(IEclipseContext context, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell)
			throws InvocationTargetException, InterruptedException
	{
		PreferenceManager pm = configurePreferences(context);
		PreferenceDialog dialog = new PreferenceDialog(shell, pm);
		dialog.setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, PreferenceConstants.QUALIFIER_ID));
		dialog.create();
		dialog.getTreeViewer().setComparator(new ViewerComparator());
		dialog.getTreeViewer().expandAll();
		dialog.open();
	}

	private PreferenceManager configurePreferences(IEclipseContext context)
	{
		PreferenceManager pm = new PreferenceManager();
		IContributionFactory factory = context.get(IContributionFactory.class);

		for (IConfigurationElement elmt : registry
				.getConfigurationElementsFor(PreferenceConstants.PAGES_EXTENSION_POINT))
		{
			if (!elmt.getName().equals(ELMT_PAGE))
			{
				logger.warn("unexpected element: {0}", elmt.getName());
				continue;
			}
			else if (isEmpty(elmt.getAttribute(ATTR_ID)) || isEmpty(elmt.getAttribute(ATTR_NAME)))
			{
				logger.warn("missing id and/or name: {}", elmt.getNamespaceIdentifier());
				continue;
			}
			PreferenceNode pn = null;
			if (elmt.getAttribute(ATTR_CLASS) != null)
			{
				IPreferencePage page = null;
				try
				{
					String prefPageURI = getClassURI(elmt.getNamespaceIdentifier(), elmt.getAttribute(ATTR_CLASS));
					Object object = factory.create(prefPageURI, context);
					if (!(object instanceof IPreferencePage))
					{
						logger.error("Expected instance of IPreferencePage: {0}", elmt.getAttribute(ATTR_CLASS));
						continue;
					}
					page = (IPreferencePage) object;
				}
				catch (ClassNotFoundException e)
				{
					logger.error(e);
					continue;
				}
				ContextInjectionFactory.inject(page, context);
				if ((page.getTitle() == null || page.getTitle().isEmpty()) && elmt.getAttribute(ATTR_NAME) != null)
				{
					page.setTitle(elmt.getAttribute(ATTR_NAME));
				}
				pn = new PreferenceNode(elmt.getAttribute(ATTR_ID), page);
			}
			else
			{
				pn =
						new PreferenceNode(elmt.getAttribute(ATTR_ID), new EmptyPreferencePage(
								elmt.getAttribute(ATTR_NAME)));
			}
			if (isEmpty(elmt.getAttribute(ATTR_CATEGORY)))
			{
				pm.addToRoot(pn);
			}
			else
			{
				IPreferenceNode parent = findNode(pm, elmt.getAttribute(ATTR_CATEGORY));
				if (parent == null)
				{
					pm.addToRoot(pn);
				}
				else
				{
					parent.add(pn);
				}
			}
		}

		return pm;
	}

	private IPreferenceNode findNode(PreferenceManager pm, String categoryId)
	{
		for (Object o : pm.getElements(PreferenceManager.POST_ORDER))
		{
			if (o instanceof IPreferenceNode && ((IPreferenceNode) o).getId().equals(categoryId))
			{
				return (IPreferenceNode) o;
			}
		}
		return null;
	}

	private String getClassURI(String definingBundleId, String spec) throws ClassNotFoundException
	{
		if (spec.startsWith("platform:") || spec.startsWith("bundleclass:")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			return spec;
		}
		return "bundleclass://" + definingBundleId + "/" + spec; //$NON-NLS-1$ //$NON-NLS-2$
		//return "platform:/plugin/" + definingBundleId + '/' + spec;
	}

	private boolean isEmpty(String value)
	{
		return value == null || value.trim().isEmpty();
	}

	static class EmptyPreferencePage extends PreferencePage
	{
		public EmptyPreferencePage(String title)
		{
			setTitle(title);
			noDefaultAndApplyButton();
		}

		@Override
		protected Control createContents(Composite parent)
		{
			return new Label(parent, SWT.NONE);
		}
	}
}
