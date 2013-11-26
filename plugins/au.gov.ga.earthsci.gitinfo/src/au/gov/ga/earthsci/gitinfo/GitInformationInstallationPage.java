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
package au.gov.ga.earthsci.gitinfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.about.InstallationPage;

/**
 * Installation page containing git commit/describe information.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GitInformationInstallationPage extends InstallationPage
{
	private static final String GITHUB_COMMIT_URL = "https://github.com/GeoscienceAustralia/earthsci/commit/"; //$NON-NLS-1$

	@Override
	public void createControl(Composite parent)
	{
		Browser browser = new Browser(parent, SWT.NONE);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		browser.setText(generateHtml(parent.getFont()));
		browser.addLocationListener(new LocationAdapter()
		{
			@Override
			public void changing(LocationEvent event)
			{
				event.doit = false;
				Program.launch(event.location);
			}
		});
	}

	@SuppressWarnings("nls")
	private String generateHtml(Font font)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head>");
		if (font != null)
		{
			FontData[] data = font.getFontData();
			if (data != null && data.length > 0)
			{
				String fontName = data[0].getName();
				int fontSize = data[0].getHeight();
				sb.append("<style type=\"text/css\">html {font-family:\"" + fontName + "\"; font-size:" + fontSize
						+ "pt;}</style>");
			}
		}
		sb.append("</head><body>");

		if (GitInformation.isSet())
		{
			sb.append("Build time: <b>" + GitInformation.getBuildTime() + "</b><br/>");
			sb.append("<br/>");
			sb.append("Git describe result: <b>" + GitInformation.getGitDescribe() + "</b><br/>");
			sb.append("<br/>");
			sb.append("Build commit:<br/>");
			sb.append("Id: <b><a href=\"" + GITHUB_COMMIT_URL
					+ GitInformation.getCommitId() + "\" >" + GitInformation.getCommitId() + "</a></b><br/>");
			sb.append("Branch: <b>" + GitInformation.getBranch() + "</b><br/>");
			sb.append("Time: <b>" + GitInformation.getCommitTime() + "</b><br/>");
		}
		else
		{
			sb.append("No Git version information found.");
		}

		sb.append("</body></html>");
		return sb.toString();
	}
}
