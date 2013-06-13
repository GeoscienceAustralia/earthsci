/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package au.gov.ga.earthsci.eclipse.extras.browser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;

import au.gov.ga.earthsci.eclipse.extras.Activator;

/**
 * Preferences for the Web browser.
 */
public class WebBrowserPreference {
	protected static final String PREF_BROWSER_HISTORY = "webBrowserHistory"; //$NON-NLS-1$

	protected static final String PREF_INTERNAL_WEB_BROWSER_HISTORY = "internalWebBrowserHistory"; //$NON-NLS-1$

	public static final int INTERNAL = 0;

	public static final int EXTERNAL = 1;

	/**
	 * WebBrowserPreference constructor comment.
	 */
	private WebBrowserPreference() {
		super();
	}

	/**
	 * Returns the preference store.
	 * 
	 * @return the preference store
	 */
	protected static IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	/**
	 * Returns the Web browser history list.
	 * 
	 * @return java.util.List
	 */
	public static List<String> getInternalWebBrowserHistory() {
		String temp = getPreferenceStore().getString(
				PREF_INTERNAL_WEB_BROWSER_HISTORY);
		StringTokenizer st = new StringTokenizer(temp, "|*|"); //$NON-NLS-1$
		List<String> l = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			l.add(s);
		}
		return l;
	}

	/**
	 * Sets the Web browser history.
	 * 
	 * @param list
	 *            the history
	 */
	public static void setInternalWebBrowserHistory(List<String> list) {
		StringBuffer sb = new StringBuffer();
		if (list != null) {
			Iterator<String> iterator = list.iterator();
			while (iterator.hasNext()) {
				String s = iterator.next();
				sb.append(s);
				sb.append("|*|"); //$NON-NLS-1$
			}
		}
		IScopeContext instanceScope = InstanceScope.INSTANCE;
		IEclipsePreferences prefs = instanceScope.getNode(Activator.PLUGIN_ID);
		prefs.put(PREF_INTERNAL_WEB_BROWSER_HISTORY,
				sb.toString());
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
}