package au.gov.ga.earthsci.application.handlers;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class ShowViewDialog extends Dialog
{
	private final EPartService partService;
	private MPart[] selection = new MPart[] {};

	public ShowViewDialog(Shell parentShell, EPartService partService)
	{
		super(parentShell);
		this.partService = partService;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new FillLayout());

		ListViewer viewer = new ListViewer(composite, SWT.NONE);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new PartLabelProvider());
		Collection<MPart> parts = partService.getParts();
		viewer.setInput(parts);

		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection s = (IStructuredSelection) event.getSelection();
				selection = new MPart[s.size()];
				Iterator<?> iterator = s.iterator();
				for (int i = 0; iterator.hasNext(); i++)
				{
					MPart part = (MPart) iterator.next();
					selection[i++] = part;
				}
			}
		});

		return composite;
	}

	public MPart[] getSelection()
	{
		return selection;
	}

	private class PartLabelProvider extends LabelProvider
	{
		@Override
		public String getText(Object element)
		{
			MPart part = (MPart) element;
			return part.getLabel();
		}

		@Override
		public Image getImage(Object element)
		{
			return super.getImage(element);
		}
	}
}
