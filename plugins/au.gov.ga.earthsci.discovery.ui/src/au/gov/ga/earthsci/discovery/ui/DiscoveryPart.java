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

import gov.nasa.worldwind.SceneController;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.view.orbit.OrbitView;

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
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
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
import au.gov.ga.earthsci.application.parts.globe.GlobeSceneController;
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
import au.gov.ga.earthsci.discovery.ui.handler.ViewOnGlobeHandler;
import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;
import au.gov.ga.earthsci.worldwind.common.layers.Bounds;
import au.gov.ga.earthsci.worldwind.common.retrieve.SectorPolyline;
import au.gov.ga.earthsci.worldwind.common.view.orbit.FlyToOrbitViewAnimator;
import au.gov.ga.earthsci.worldwind.common.view.orbit.FlyToSectorAnimator;

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

	@Inject
	private IEventBroker eventBroker;

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

	private IDiscoveryResult lastResultMouseOver;
	private RenderableLayer mouseOverLayer;
	private GlobeSceneController mouseOverSceneController;

	private IDiscoveryResult selectedResult;
	private boolean viewOnGlobe;

	@PostConstruct
	public void init(final Composite parent, final MPart part)
	{
		context.set(DiscoveryPart.class, this);
		viewOnGlobe = ViewOnGlobeHandler.isViewOnGlobe(part);

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

		resultsSashForm.setWeights(new int[] { 2, 8 });

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

	@Focus
	private void setFocus()
	{
		searchText.setFocus();
	}

	public void setViewOnGlobe(boolean viewOnGlobe)
	{
		this.viewOnGlobe = viewOnGlobe;
		resultSelected(selectedResult);
	}

	private void performSearch()
	{
		for (IDiscovery discovery : currentDiscoveries)
		{
			discovery.cancel();
		}
		currentDiscoveries.clear();
		discoverySelected(null);

		if (searchText.getText().length() > 0)
		{
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

			if (currentDiscoveries.isEmpty())
			{
				if (MessageDialog.openQuestion(shell, Messages.DiscoveryPart_NoServicesDialogTitle,
						Messages.DiscoveryPart_NoServicesDialogMessage))
				{
					new ServicesHandler().execute(shell);
				}
			}
		}

		discoveriesViewer.setInput(currentDiscoveries);
		resultSelected(null);

		eventBroker.send(UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC, UIEvents.ALL_ELEMENT_ID);
	}

	public boolean canClearResults()
	{
		return searchText.getText().length() > 0 || !currentDiscoveries.isEmpty();
	}

	public void clearResults()
	{
		searchText.setText(""); //$NON-NLS-1$
		performSearch();
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

		resultsViewer.getTable().addMouseMoveListener(new MouseMoveListener()
		{
			@Override
			public void mouseMove(MouseEvent e)
			{
				IDiscoveryResult result = null;
				ViewerCell cell = resultsViewer.getCell(new Point(e.x, e.y));
				if (cell != null)
				{
					result = (IDiscoveryResult) cell.getElement();
				}
				resultMouseOver(result);
			}
		});
		resultsViewer.getTable().addMouseTrackListener(new MouseTrackAdapter()
		{
			@Override
			public void mouseExit(MouseEvent e)
			{
				resultMouseOver(null);
			}
		});
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
		this.selectedResult = result;
		selectionService.setSelection(result);
		displaySectorAndZoom(viewOnGlobe ? result : null, true);
	}

	private void resultDefaultSelected(IDiscoveryResult result)
	{
		IDiscoveryResultHandler handler = result.getDiscovery().getService().getProvider().getHandler();
		ContextInjectionFactory.inject(handler, context);
		handler.open(result);
	}

	private void resultMouseOver(IDiscoveryResult result)
	{
	}

	private void displaySectorAndZoom(IDiscoveryResult result, boolean zoom)
	{
		if (lastResultMouseOver != result)
		{
			lastResultMouseOver = result;

			if (mouseOverLayer == null)
			{
				mouseOverLayer = new RenderableLayer();
			}

			Bounds bounds = null;
			mouseOverLayer.removeAllRenderables();
			if (result != null)
			{
				bounds = result.getBounds();
				if (bounds != null)
				{
					mouseOverLayer.addRenderable(new SectorPolyline(bounds.toSector()));
				}
			}

			WorldWindow wwd = WorldWindowRegistry.INSTANCE.getActive();
			if (wwd != null)
			{
				SceneController sceneController = wwd.getSceneController();
				if (bounds != null && sceneController instanceof GlobeSceneController)
				{
					GlobeSceneController gsc = (GlobeSceneController) sceneController;
					if (mouseOverSceneController != gsc)
					{
						if (mouseOverSceneController != null)
						{
							mouseOverSceneController.getPostLayers().remove(mouseOverLayer);
						}
						mouseOverSceneController = gsc;
						mouseOverSceneController.getPostLayers().add(mouseOverLayer);
					}
				}
				else if (mouseOverSceneController != null)
				{
					mouseOverSceneController.getPostLayers().remove(mouseOverLayer);
					mouseOverSceneController = null;
				}
				wwd.redraw();

				if (zoom)
				{
					View view = WorldWindowRegistry.INSTANCE.getActiveView();
					if (view instanceof OrbitView && bounds != null)
					{
						OrbitView orbitView = (OrbitView) view;
						Position center = orbitView.getCenterPosition();
						Position newCenter;
						if (bounds.contains(center) && bounds.deltaLatitude.degrees > 90
								&& bounds.deltaLongitude.degrees > 90)
						{
							newCenter = center;
						}
						else
						{
							newCenter = bounds.center;
						}

						LatLon endVisibleDelta = new LatLon(bounds.deltaLatitude, bounds.deltaLongitude);
						FlyToOrbitViewAnimator animator =
								FlyToSectorAnimator.createScaledFlyToSectorAnimator(orbitView, center, newCenter,
										orbitView.getHeading(), orbitView.getPitch(), orbitView.getZoom(),
										endVisibleDelta, 10);
						orbitView.stopAnimations();
						orbitView.stopMovement();
						orbitView.addAnimator(animator);
						orbitView.firePropertyChange(AVKey.VIEW, null, orbitView);
					}
				}
			}
		}
	}
}
