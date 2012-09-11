package au.gov.ga.earthsci.application.handlers;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

public class ShowViewHandler
{
	@Inject
	private EPartService partService;

	@Execute
	public void execute(IEclipseContext context, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell)
			throws InvocationTargetException, InterruptedException
	{
		final ShowViewDialog dialog = new ShowViewDialog(shell, partService);
		dialog.open();
		if (dialog.getReturnCode() != Window.OK)
			return;

		for (MPart part : dialog.getSelection())
		{
			partService.showPart(part, PartState.ACTIVATE);
		}
	}
}
