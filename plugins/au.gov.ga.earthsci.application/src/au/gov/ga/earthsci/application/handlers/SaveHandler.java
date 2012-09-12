package au.gov.ga.earthsci.application.handlers;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

public class SaveHandler
{
	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_PART) MDirtyable dirtyable)
	{
		if (dirtyable == null)
		{
			return false;
		}
		return dirtyable.isDirty();
	}

	@Execute
	public void execute(IEclipseContext context, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
			@Named(IServiceConstants.ACTIVE_PART) final MContribution contribution) throws InvocationTargetException,
			InterruptedException
	{
		final IEclipseContext pmContext = context.createChild();

		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		dialog.open();
		dialog.run(true, true, new IRunnableWithProgress()
		{
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				pmContext.set(IProgressMonitor.class.getName(), monitor);
				if (contribution != null)
				{
					//Object clientObject = contribution.getObject();
					//ContextInjectionFactory.invoke(clientObject, Persist.class, pmContext, null);
				}
			}
		});

		pmContext.dispose();
	}
}
