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
package au.gov.ga.earthsci.common.ui.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.eclipse.ui.preferences.WorkingCopyManager;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.service.prefs.BackingStoreException;

/**
 * {@link PreferenceDialog} subclass that only saves/cancels pages for which
 * controls have been created.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LazyPreferenceDialog extends PreferenceDialog implements IWorkbenchPreferenceContainer
{
	private final Set<IPreferencePage> pagesWithCreatedControls = new HashSet<IPreferencePage>();
	private final Collection<Job> updateJobs = new ArrayList<Job>();
	private IWorkingCopyManager workingCopyManager;

	public LazyPreferenceDialog(Shell parentShell, PreferenceManager manager)
	{
		super(parentShell, manager);
	}

	@Override
	protected void createPageControl(IPreferencePage page, Composite parent)
	{
		super.createPageControl(page, parent);
		pagesWithCreatedControls.add(page);
	}

	@Override
	protected void okPressed()
	{
		SafeRunnable.run(new SafeRunnable()
		{
			private boolean errorOccurred;

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.ISafeRunnable#run()
			 */
			@Override
			public void run()
			{
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				errorOccurred = false;
				boolean hasFailedOK = false;
				try
				{
					// Notify all the pages and give them a chance to abort
					Iterator<?> nodes = getPreferenceManager().getElements(PreferenceManager.PRE_ORDER)
							.iterator();
					while (nodes.hasNext())
					{
						IPreferenceNode node = (IPreferenceNode) nodes.next();
						IPreferencePage page = node.getPage();
						if (page != null && pagesWithCreatedControls.contains(page))
						{
							if (!page.performOk())
							{
								hasFailedOK = true;
								return;
							}
						}
					}
				}
				catch (Exception e)
				{
					handleException(e);
				}
				finally
				{
					//Don't bother closing if the OK failed
					if (hasFailedOK)
					{
						setReturnCode(FAILED);
						getButton(IDialogConstants.OK_ID).setEnabled(true);
						return;
					}

					if (!errorOccurred)
					{
						//Give subclasses the choice to save the state of the
						//preference pages.
						handleSave();
					}
					setReturnCode(OK);
					close();
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
			 */
			@Override
			public void handleException(Throwable e)
			{
				errorOccurred = true;

				Policy.getLog().log(new Status(IStatus.ERROR, Policy.JFACE, 0, e.toString(), e));

				setSelectedNodePreference(null);
				String message = JFaceResources.getString("SafeRunnable.errorMessage"); //$NON-NLS-1$

				Policy.getStatusHandler().show(
						new Status(IStatus.ERROR, Policy.JFACE, message, e),
						JFaceResources.getString("Error")); //$NON-NLS-1$

			}
		});


		if (getReturnCode() == FAILED)
		{
			return;
		}

		if (workingCopyManager != null)
		{
			try
			{
				workingCopyManager.applyChanges();
			}
			catch (BackingStoreException e)
			{
				String msg = e.getMessage();
				if (msg == null)
				{
					msg = WorkbenchMessages.FilteredPreferenceDialog_PreferenceSaveFailed;
				}
				StatusUtil
						.handleStatus(
								WorkbenchMessages.PreferencesExportDialog_ErrorDialogTitle
										+ ": " + msg, e, StatusManager.SHOW, //$NON-NLS-1$
								getShell());
			}
		}

		// Run the update jobs
		Iterator<Job> updateIterator = updateJobs.iterator();
		while (updateIterator.hasNext())
		{
			updateIterator.next().schedule();
		}
	}

	@Override
	protected void cancelPressed()
	{
		// Inform all pages that we are cancelling
		Iterator<?> nodes = getPreferenceManager().getElements(PreferenceManager.PRE_ORDER).iterator();
		final boolean[] cancelOK = new boolean[] { true };
		while (nodes.hasNext())
		{
			final IPreferenceNode node = (IPreferenceNode) nodes.next();
			final IPreferencePage page = getPage(node);
			if (page != null && pagesWithCreatedControls.contains(page))
			{
				SafeRunnable.run(new SafeRunnable()
				{
					@Override
					public void run()
					{
						if (!page.performCancel())
						{
							cancelOK[0] = false;
						}
					}
				});
				if (!cancelOK[0])
				{
					return;
				}
			}
		}

		// Give subclasses the choice to save the state of the preference pages if needed
		handleSave();

		setReturnCode(CANCEL);
		close();
	}

	@Override
	public boolean openPage(String preferencePageId, Object data)
	{
		setCurrentPageId(preferencePageId);
		IPreferencePage page = getCurrentPage();
		if (page instanceof PreferencePage)
		{
			((PreferencePage) page).applyData(data);
		}
		return true;
	}

	public final void setCurrentPageId(final String preferencePageId)
	{
		final IPreferenceNode node = findNodeMatching(preferencePageId);
		if (node != null)
		{
			getTreeViewer().setSelection(new StructuredSelection(node));
			showPage(node);
		}
	}

	@Override
	public IWorkingCopyManager getWorkingCopyManager()
	{
		if (workingCopyManager == null)
		{
			workingCopyManager = new WorkingCopyManager();
		}
		return workingCopyManager;
	}

	@Override
	public void registerUpdateJob(Job job)
	{
		updateJobs.add(job);
	}
}
