package au.gov.ga.earthsci.notification.popup;

import org.eclipse.e4.core.di.InjectorFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import au.gov.ga.earthsci.notification.popup.preferences.IPopupNotificationPreferences;
import au.gov.ga.earthsci.notification.popup.preferences.PopupNotificationPreferences;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		InjectorFactory.getDefault().addBinding(IPopupNotificationPreferences.class).implementedBy(PopupNotificationPreferences.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}
	
}
