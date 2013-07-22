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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import au.gov.ga.earthsci.application.Activator;
import au.gov.ga.earthsci.common.ui.dialogs.StackTraceDialog;
import au.gov.ga.earthsci.common.ui.information.IInformationProvider;
import au.gov.ga.earthsci.common.ui.information.InformationProviderHoverInformationControlManager;
import au.gov.ga.earthsci.common.ui.widgets.PageLinks;
import au.gov.ga.earthsci.common.ui.widgets.PageListener;
import au.gov.ga.earthsci.discovery.DiscoveryParameters;
import au.gov.ga.earthsci.discovery.DiscoveryServiceManager;
import au.gov.ga.earthsci.discovery.IDiscovery;
import au.gov.ga.earthsci.discovery.IDiscoveryListener;
import au.gov.ga.earthsci.discovery.IDiscoveryResult;
import au.gov.ga.earthsci.discovery.IDiscoveryResultHandler;
import au.gov.ga.earthsci.discovery.IDiscoveryResultLabelProvider;
import au.gov.ga.earthsci.discovery.IDiscoveryService;
import au.gov.ga.earthsci.discovery.ui.handler.ServicesHandler;

/**
 * The Discovery UI part.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DiscoveryPart implements IDiscoveryListener, PageListener
{
	@Inject
	@Optional
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Inject
	private IEclipseContext context;

	@Inject
	private ESelectionService selectionService;

	private Text searchText;
	private Button searchButton;
	private SashForm resultsSashForm;

	private TableViewer discoveriesViewer;
	private Composite resultsComposite;
	private Composite resultsViewerComposite;
	private TableViewer resultsViewer;
	private PageLinks pageLinks;

	private final List<IDiscovery> currentDiscoveries = new ArrayList<IDiscovery>();
	private final DiscoveryResultContentProvider discoveryContentProvider = new DiscoveryResultContentProvider();

	@PostConstruct
	public void init(final Composite parent)
	{
		parent.setLayout(new GridLayout(1, true));


		Composite searchComposite = new Composite(parent, SWT.NONE);
		searchComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		searchComposite.setLayout(createGridLayout(2, 0));

		searchText = new Text(searchComposite, SWT.SEARCH);
		searchText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		searchText.setMessage(Messages.DiscoveryPart_SearchPlaceholder);
		searchText.addTraverseListener(new TraverseListener()
		{
			@Override
			public void keyTraversed(TraverseEvent e)
			{
				if (e.detail == SWT.TRAVERSE_RETURN)
				{
					performSearch();
				}
			}
		});

		searchButton = new Button(searchComposite, SWT.PUSH);
		searchButton.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
		searchButton.setText(Messages.DiscoveryPart_Search);
		searchButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				performSearch();
			}
		});
		searchText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				searchButton.setEnabled(searchText.getText().length() != 0);
			}
		});
		searchButton.setEnabled(false);


		Composite resultsRootComposite = new Composite(parent, SWT.NONE);
		resultsRootComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		resultsRootComposite.setLayout(createGridLayout(1, 0));

		resultsSashForm = new SashForm(resultsRootComposite, SWT.VERTICAL | SWT.SMOOTH);
		resultsSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		discoveriesViewer = new TableViewer(resultsSashForm, SWT.BORDER);
		discoveriesViewer.setContentProvider(new ArrayContentProvider());
		discoveriesViewer.setLabelProvider(new DiscoveryLabelProvider());
		discoveriesViewer.setComparator(new DiscoveryComparator());

		resultsComposite = new Composite(resultsSashForm, SWT.NONE);
		resultsComposite.setLayout(createGridLayout(1, 0));

		resultsViewerComposite = new Composite(resultsComposite, SWT.NONE);
		resultsViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		resultsViewerComposite.setLayout(new FillLayout());

		createResultsViewer();

		pageLinks = new PageLinks(resultsComposite, SWT.NONE);
		pageLinks.setPageCount(20);
		pageLinks.setSelectedPage(4);
		pageLinks.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		pageLinks.addPageListener(this);
		pageLinks.setVisible(false);
		((GridData) pageLinks.getLayoutData()).exclude = true;

		resultsSashForm.setWeights(new int[] { 3, 7 });

		new TableViewerSelectionHelper<IDiscovery>(discoveriesViewer, IDiscovery.class)
		{
			@Override
			protected void itemSelected(IDiscovery selection)
			{
				discoverySelected(selection);
			}

			@Override
			protected void itemDefaultSelected(IDiscovery selection)
			{
				discoveryDefaultSelected(selection);
			}
		};
	}

	private void performSearch()
	{
		for (IDiscovery discovery : currentDiscoveries)
		{
			discovery.cancel();
		}
		currentDiscoveries.clear();
		discoverySelected(null);

		for (IDiscoveryService service : DiscoveryServiceManager.getServices())
		{
			if (service.isEnabled())
			{
				DiscoveryParameters parameters = new DiscoveryParameters();
				parameters.setQuery(searchText.getText());
				IDiscovery discovery = service.createDiscovery(parameters);
				if (discovery != null)
				{
					currentDiscoveries.add(discovery);
					discovery.addListener(DiscoveryPart.this);
					discovery.start();
				}
			}
		}

		discoveriesViewer.setInput(currentDiscoveries);

		if (currentDiscoveries.isEmpty())
		{
			if (MessageDialog.openQuestion(shell, Messages.DiscoveryPart_NoServicesDialogTitle,
					Messages.DiscoveryPart_NoServicesDialogMessage))
			{
				new ServicesHandler().execute(shell);
			}
		}
	}

	private void createResultsViewer()
	{
		if (resultsViewer != null)
		{
			resultsViewer.getControl().dispose();
		}

		resultsViewer = new TableViewer(resultsViewerComposite, SWT.BORDER | SWT.FULL_SELECTION);
		resultsViewer.setContentProvider(discoveryContentProvider);
		resultsViewer.getTable().setLinesVisible(true);
		resultsViewerComposite.layout();

		//keep the column width in sync with the table width
		final TableColumn resultsColumn = new TableColumn(resultsViewer.getTable(), SWT.LEFT);
		Listener resizeListener = new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				resultsColumn.setWidth(resultsViewer.getTable().getClientArea().width);
			}
		};
		resultsViewer.getControl().addListener(SWT.Resize, resizeListener);
		resultsViewer.getControl().addListener(SWT.Paint, resizeListener);

		new TableViewerSelectionHelper<IDiscoveryResult>(resultsViewer, IDiscoveryResult.class)
		{
			@Override
			protected void itemSelected(IDiscoveryResult selection)
			{
				resultSelected(selection);
			}

			@Override
			protected void itemDefaultSelected(IDiscoveryResult selection)
			{
				resultDefaultSelected(selection);
			}
		};
	}

	private void discoverySelected(IDiscovery discovery)
	{
		createResultsViewer();

		boolean pageLinksVisible = false;
		if (discovery != null)
		{
			pageLinksVisible = discovery.getPageSize() != 0;
			if (pageLinksVisible)
			{
				int resultCount = discovery.getResultCount();
				if (resultCount == IDiscovery.UNKNOWN)
				{
					pageLinks.setPageCount(PageLinks.UNKNOWN_PAGE_COUNT);
				}
				else
				{
					int pageCount = ((resultCount - 1) / discovery.getPageSize()) + 1;
					pageLinks.setPageCount(pageCount);
				}
				pageLinks.setSelectedPage(0);
				pageChanged(0);
			}

			IDiscoveryResultLabelProvider labelProvider = discovery.getLabelProvider();
			resultsViewer.setLabelProvider(new DiscoveryResultLabelProvider(labelProvider));

			//enable focusable tooltips
			IInformationProvider provider = new DiscoveryResultInformationProvider(resultsViewer, labelProvider);
			IInformationControlCreator creator = new DiscoveryResultInformationControlCreator();
			InformationProviderHoverInformationControlManager.install(resultsViewer.getControl(), provider, creator);
		}
		pageLinks.setVisible(pageLinksVisible);
		((GridData) pageLinks.getLayoutData()).exclude = !pageLinksVisible;
		resultsComposite.layout();

		resultsViewer.setInput(discovery);
	}

	private void discoveryDefaultSelected(IDiscovery discovery)
	{
		if (discovery.getError() != null)
		{
			Throwable e = discovery.getError();
			IStatus status = new Status(IStatus.ERROR, Activator.getBundleName(), e.getLocalizedMessage(), e);
			StackTraceDialog.openError(shell, Messages.DiscoveryPart_Error, null, status);
		}
	}

	private static GridLayout createGridLayout(int numColumns, int margins)
	{
		GridLayout layout = new GridLayout(numColumns, false);
		layout.marginWidth = layout.marginHeight = margins;
		return layout;
	}

	@Override
	public void resultCountChanged(final IDiscovery discovery)
	{
		final ColumnViewer viewer = discoveriesViewer;
		if (!viewer.getControl().isDisposed())
		{
			viewer.getControl().getDisplay().asyncExec(new Runnable()
			{
				@Override
				public void run()
				{
					if (!viewer.getControl().isDisposed())
					{
						viewer.refresh(discovery);
					}
				}
			});
		}
	}

	@Override
	public void resultAdded(IDiscovery discovery, IDiscoveryResult result)
	{
	}

	@Override
	public void pageChanged(int page)
	{
		discoveryContentProvider.setPage(page);
	}

	private void resultSelected(IDiscoveryResult result)
	{
		selectionService.setSelection(result);
	}

	private void resultDefaultSelected(IDiscoveryResult result)
	{
		IDiscoveryResultHandler handler = result.getDiscovery().getService().getProvider().getHandler();
		ContextInjectionFactory.inject(handler, context);
		handler.open(result);
	}
}
