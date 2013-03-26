package au.gov.ga.earthsci.notification.dialog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import au.gov.ga.earthsci.notification.INotification;
import au.gov.ga.earthsci.notification.INotificationReceiver;
import au.gov.ga.earthsci.notification.NotificationLevel;

/**
 * A notification receiver that responds to error level notifications that
 * require acknowledgement.
 * <p/>
 * This receiver presents a modal dialog box that cannot be dismissed until the
 * user provides acknowledgement of the notification.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DialogNotificationReceiver implements INotificationReceiver
{

	@Override
	public void handle(final INotification notification)
	{
		if (notification == null || notification.getLevel() != NotificationLevel.ERROR
				|| !notification.requiresAcknowledgment())
		{
			return;
		}

		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				MessageDialog dialog =
						new MessageDialog(Display.getDefault().getActiveShell(), notification.getTitle(), null,
								notification.getText(), MessageDialog.ERROR, new String[] { notification
										.getAcknowledgementAction().getText() }, 0);
				dialog.open();

				// TODO: What if this is a long running action? 
				notification.acknowledge();
			}
		});

	}
}
