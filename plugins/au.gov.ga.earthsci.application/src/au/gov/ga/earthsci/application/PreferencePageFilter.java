/*******************************************************************************
 * Copyright 2015 Geoscience Australia
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
package au.gov.ga.earthsci.application;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.PlatformUI;

/**
 * Preference page blacklist filter.
 *
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PreferencePageFilter
{
	public final static String[] BLACKLIST = new String[] { "org.eclipse.help.ui.browsersPreferencePage", //$NON-NLS-1$
			"org.eclipse.ui.preferencePages.Workbench" //$NON-NLS-1$
	};

	public static void filter()
	{
		PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
		for (String remove : BLACKLIST)
		{
			pm.remove(remove);
		}
	}
}
