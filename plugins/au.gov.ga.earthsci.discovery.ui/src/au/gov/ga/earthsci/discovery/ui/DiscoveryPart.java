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
package au.gov.ga.earthsci.discovery.ui;

import javax.annotation.PostConstruct;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import au.gov.ga.earthsci.application.ImageRegistry;
import au.gov.ga.earthsci.discovery.DiscoveryParameters;
import au.gov.ga.earthsci.discovery.DiscoveryServiceManager;
import au.gov.ga.earthsci.discovery.IDiscovery;
import au.gov.ga.earthsci.discovery.IDiscoveryService;

/**
 * The Discovery UI part.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DiscoveryPart
{
	private Text searchText;
	private TableViewer serverViewer;
	private TableViewer resultsViewer;

	@PostConstruct
	public void init(final Composite parent)
	{
		parent.setLayout(new GridLayout(1, true));

		Composite searchComposite = new Composite(parent, SWT.NONE);
		searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		searchComposite.setLayout(createGridLayout(2, 0));

		searchText = new Text(searchComposite, SWT.SEARCH);
		searchText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		searchText.setMessage("Search text");

		Button searchButton = new Button(searchComposite, SWT.PUSH);
		searchButton.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
		searchButton.setText("Search");
		searchButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				System.out.println("HELLO!");
				IDiscoveryService service = DiscoveryServiceManager.getServices().get(0);
				DiscoveryParameters parameters = new DiscoveryParameters();
				parameters.setQuery("australia");
				IDiscovery discovery = service.createDiscovery(parameters);
				discovery.start();
			}
		});


		SashForm sashForm = new SashForm(parent, SWT.VERTICAL | SWT.SMOOTH);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashForm.setLayout(new FillLayout());


		Composite serversComposite = new Composite(sashForm, SWT.NONE);
		serversComposite.setLayout(createGridLayout(1, 0));

		Composite serversHeaderComposite = new Composite(serversComposite, SWT.NONE);
		serversHeaderComposite.setLayout(createGridLayout(2, 0));
		serversHeaderComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label serversLabel = new Label(serversHeaderComposite, SWT.NONE);
		serversLabel.setText("Servers:");
		serversLabel.setLayoutData(new GridData(SWT.LEFT, SWT.END, false, false));

		ToolBar serversToolbar = new ToolBar(serversHeaderComposite, SWT.HORIZONTAL);
		serversToolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		ToolItem addServer = new ToolItem(serversToolbar, SWT.PUSH);
		addServer.setImage(ImageRegistry.getInstance().get(ImageRegistry.ICON_ADD));

		serverViewer = new TableViewer(serversComposite);
		serverViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));


		Composite resultsComposite = new Composite(sashForm, SWT.NONE);
		resultsComposite.setLayout(createGridLayout(1, 0));

		Label resultsLabel = new Label(resultsComposite, SWT.NONE);
		resultsLabel.setText("Results:");

		resultsViewer = new TableViewer(resultsComposite);
		resultsViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));


		sashForm.setWeights(new int[] { 3, 7 });
	}

	private static GridLayout createGridLayout(int numColumns, int margins)
	{
		GridLayout layout = new GridLayout(numColumns, false);
		layout.marginWidth = layout.marginHeight = margins;
		return layout;
	}
}
