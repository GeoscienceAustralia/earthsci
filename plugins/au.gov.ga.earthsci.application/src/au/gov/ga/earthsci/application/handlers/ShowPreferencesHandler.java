package au.gov.ga.earthsci.application.handlers;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.application.preferences.PreferenceConstants;
import au.gov.ga.earthsci.application.preferences.PreferenceUtil;
import au.gov.ga.earthsci.application.preferences.ScopedPreferenceStore;

public class ShowPreferencesHandler
{
	@Inject
	protected IExtensionRegistry registry;

	@Execute
	public void execute(IEclipseContext context, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell)
			throws InvocationTargetException, InterruptedException
	{
		PreferenceManager pm = PreferenceUtil.createLegacyPreferenceManager(context, registry);
		PreferenceDialog dialog = new PreferenceDialog(shell, pm);
		dialog.setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, PreferenceConstants.QUALIFIER_ID));
		dialog.create();
		dialog.getTreeViewer().setComparator(new ViewerComparator());
		dialog.getTreeViewer().expandAll();
		dialog.open();
	}
}
