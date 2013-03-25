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

import java.net.URI;
import java.util.Iterator;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog which lists the parts (or "Views") in this application, and allows the
 * user to select one or multiple.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ShowViewDialog extends Dialog
{
	private final MApplication application;
	private MPartDescriptor[] selection = new MPartDescriptor[] {};

	public ShowViewDialog(Shell parentShell, MApplication application)
	{
		super(parentShell);
		this.application = application;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new FillLayout());

		TableViewer viewer = new TableViewer(composite, SWT.NONE);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new PartLabelProvider());

		//TODO this part collection is simply a list of parts that have been shown before
		//need to improve this collection so that it removes duplicates, and also contains
		//parts that are registered but have never been displayed
		viewer.setInput(application.getDescriptors());

		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection s = (IStructuredSelection) event.getSelection();
				selection = new MPartDescriptor[s.size()];
				Iterator<?> iterator = s.iterator();
				for (int i = 0; iterator.hasNext(); i++)
				{
					MPartDescriptor part = (MPartDescriptor) iterator.next();
					selection[i++] = part;
				}
			}
		});

		return composite;
	}

	public MPartDescriptor[] getSelection()
	{
		return selection;
	}

	private class PartLabelProvider extends LabelProvider
	{
		@Override
		public String getText(Object element)
		{
			MPartDescriptor part = (MPartDescriptor) element;
			return part.getLabel();
		}

		@Override
		public Image getImage(Object element)
		{
			MPartDescriptor part = (MPartDescriptor) element;
			String uri = part.getIconURI();
			try
			{
				return ImageDescriptor.createFromURL(new URI(uri).toURL()).createImage();
			}
			catch (Exception e)
			{
				return null;
			}
		}
	}
}
