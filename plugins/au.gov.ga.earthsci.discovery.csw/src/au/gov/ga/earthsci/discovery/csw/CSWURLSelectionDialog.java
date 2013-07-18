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
package au.gov.ga.earthsci.discovery.csw;

import java.net.URL;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog used to select a URL to open from a CSW discovery result.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CSWURLSelectionDialog extends StatusDialog
{
	private final List<URL> urls;
	private URL selectedUrl;
	private URL finalUrl;
	private Button okButton;
	private Button getCapabilitiesButton;
	private org.eclipse.swt.widgets.List urlList;
	private Label finalUrlLabel;

	public CSWURLSelectionDialog(Shell parent, List<URL> urls)
	{
		super(parent);
		this.urls = urls;
	}

	public URL getSelectedUrl()
	{
		return selectedUrl;
	}

	public void setSelectedUrl(URL selectedUrl)
	{
		this.selectedUrl = selectedUrl;
	}

	public URL getFinalUrl()
	{
		return finalUrl;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		validate();
	}

	@Override
	protected void setShellStyle(int newShellStyle)
	{
		super.setShellStyle(newShellStyle | SWT.RESIZE);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		initializeDialogUnits(comp);
		GridLayout layout = new GridLayout();
		layout.marginTop = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);

		urlList = new org.eclipse.swt.widgets.List(comp, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		urlList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		int maxCountOfWms = 0;
		int selectedIndex = -1;
		for (int i = 0; i < urls.size(); i++)
		{
			URL url = urls.get(i);
			String urlString = url.toString();
			urlList.add(urlString);
			int countOfWms = countStringOccurences(urlString.toLowerCase(), "wms"); //$NON-NLS-1$
			if (countOfWms > maxCountOfWms)
			{
				maxCountOfWms = countOfWms;
				selectedIndex = i;
			}
		}
		if (selectedIndex < 0 && !urls.isEmpty())
		{
			selectedIndex = 0;
		}
		if (selectedUrl != null)
		{
			int indexOfSelectedUrl = urls.indexOf(selectedUrl);
			selectedIndex = indexOfSelectedUrl >= 0 ? indexOfSelectedUrl : selectedIndex;
		}
		if (selectedIndex >= 0)
		{
			urlList.select(selectedIndex);
		}
		urlList.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				validate();
			}
		});

		getCapabilitiesButton = new Button(comp, SWT.CHECK);
		getCapabilitiesButton.setText(Messages.CSWURLSelectionDialog_GetCapabilitiesButtonText);
		getCapabilitiesButton.setSelection(true);
		getCapabilitiesButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				validate();
			}
		});

		finalUrlLabel = new Label(comp, SWT.NONE);
		finalUrlLabel.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		comp.setLayout(layout);

		Dialog.applyDialogFont(comp);
		return comp;
	}

	private void validate()
	{
		int selectedIndex = urlList.getSelectionIndex();
		URL oldSelectedUrl = selectedUrl;
		selectedUrl = selectedIndex < 0 ? null : urls.get(selectedIndex);
		if (oldSelectedUrl != selectedUrl)
		{
			updateGetCapabilitiesButton();
		}
		updateFinalUrl();
		finalUrlLabel.setText(finalUrl == null ? "" : finalUrl.toString().replace("&", "&&")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		boolean valid = selectedUrl != null;
		okButton.setEnabled(valid);
	}

	private void updateGetCapabilitiesButton()
	{
		if (selectedUrl == null)
		{
			return;
		}

		String lower = selectedUrl.toString().toLowerCase();
		if (lower.contains("request=getcapabilities")) //$NON-NLS-1$
		{
			getCapabilitiesButton.setSelection(true);
			getCapabilitiesButton.setEnabled(false);
		}
		else
		{
			getCapabilitiesButton.setEnabled(true);
			boolean containsOGC = lower.contains("wms") || lower.contains("wcs") || lower.contains("wfs"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			getCapabilitiesButton.setSelection(containsOGC);
		}
	}

	private void updateFinalUrl()
	{
		if (selectedUrl == null)
		{
			finalUrl = null;
			return;
		}
		else if (!getCapabilitiesButton.getSelection() || !getCapabilitiesButton.isEnabled())
		{
			finalUrl = selectedUrl;
			return;
		}

		String urlString = selectedUrl.toString();
		urlString = addQueryParameter(urlString, "request=GetCapabilities"); //$NON-NLS-1$

		try
		{
			finalUrl = new URL(urlString);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			finalUrl = selectedUrl;
		}
	}

	private static String addQueryParameter(String urlString, String queryParameter)
	{
		int indexOfQuestion = urlString.indexOf('?');
		if (indexOfQuestion < 0)
		{
			return urlString + "?" + queryParameter; //$NON-NLS-1$
		}
		else
		{
			String prefix = urlString.substring(0, indexOfQuestion + 1);
			String suffix = urlString.substring(indexOfQuestion + 1);
			if (suffix.length() > 0)
			{
				queryParameter += "&"; //$NON-NLS-1$
			}
			return prefix + queryParameter + suffix;
		}
	}

	private static int countStringOccurences(String string, String substring)
	{
		int count = 0;
		int start = 0;
		int length = substring.length();
		while ((start = string.indexOf(substring, start) + length) >= length)
		{
			count++;
		}
		return count;
	}
}
