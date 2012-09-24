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
package au.gov.ga.earthsci.application.handlers;

import java.lang.reflect.InvocationTargetException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.application.preferences.PreferenceConstants;
import au.gov.ga.earthsci.application.preferences.PreferenceUtil;
import au.gov.ga.earthsci.application.preferences.ScopedPreferenceStore;

/**
 * Handler used to show the preference dialog.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ShowPreferencesHandler
{
	@Inject
	protected IExtensionRegistry registry;

	@Inject
	private Logger logger;
	
	@PostConstruct
	public void postInitialise(IEclipseContext context)
	{
		PreferenceUtil.setLogger(logger);
	}
	
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
