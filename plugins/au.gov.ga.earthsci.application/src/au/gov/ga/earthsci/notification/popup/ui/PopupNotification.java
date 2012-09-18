package au.gov.ga.earthsci.notification.popup.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.notification.INotification;
import au.gov.ga.earthsci.notification.popup.preferences.IPopupNotificationPreferences;

/**
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class PopupNotification
{
	// Style ID constants
	private static final String DIALOG_TITLE_STYLE_ID = "popupDialogTitle"; //$NON-NLS-1$
	private static final String DIALOG_TEXT_STYLE_ID = "popupDialogText"; //$NON-NLS-1$
	private static final String DIALOG_SHELL_STYLE_ID = "popupDialog"; //$NON-NLS-1$
	
	/** How long each tick is when fading (in ms) */
	private static final int FADE_TICK = 50;
	
	/** The amount to increment the alpha channel by on each tick when fading in */
	private static final int FADE_IN_ALPHA_STEP = 30;
	
	/** The amount to decrement the alpha channel by on each tick when fading out */
	private static final int FADE_OUT_ALPHA_STEP = 8;

	/** How high the alpha value is when the popup has finished fading in */ 
	private static final int FINAL_ALPHA = 225;

	/** The amount of padding to leave around a popup */
	private static final int PADDING = 2;
	
	/** The size of the popup to create */
	private static final Point POPUP_SIZE = new Point(350, 100);
	
	/** The list of currently active popups */
	private static List<PopupNotification> activePopups = new ArrayList<PopupNotification>();

	/** The styling engine to use for applying CSS styles */
	//@Inject
	private static IStylingEngine styling;
	
	/** Preferences used to control the appearance and behaviour of the popups */
	@Inject
	private static IPopupNotificationPreferences preferences;
	
	/**
	 * Show the given notification as a popup using the current popup preferences and styling 
	 * 
	 * @param notification The notification to show
	 */
	public static void show(INotification notification)
	{
		PopupNotification pn = new PopupNotification();
		pn.showPopupNotification(notification);
	}

	/** An appropriately styled shell used to create the popup */
	private Shell shell;
	
	/**
	 * No-arg constructor used for dependency injection
	 */
	PopupNotification()
	{
		// DO NOTHING
	}
	
	/**
	 * Create a new popup notification for the given notification object
	 * 
	 * @param notification The notification to use
	 */
	private void showPopupNotification(INotification notification)
	{
		if (noActiveMonitorExists())
		{
			return;
		}
		
		shell = new Shell(Display.getDefault().getActiveShell(), SWT.NO_FOCUS | SWT.NO_TRIM);
		//styling.setId(shell, DIALOG_SHELL_STYLE_ID);
		
		shell.setLayout(new FillLayout());
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		shell.addListener(SWT.Dispose, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				activePopups.remove(PopupNotification.this);
		 	}
		});

		final Composite inner = new Composite(shell, SWT.NONE);

		GridLayout gl = new GridLayout(2, false);
		gl.marginLeft = 5;
		gl.marginTop = 0;
		gl.marginRight = 5;
		gl.marginBottom = 5;
		inner.setLayout(gl);

		CLabel imgLabel = new CLabel(inner, SWT.NONE);
		imgLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_BEGINNING));
		//imgLabel.setImage(type.getImage());

		CLabel titleLabel = new CLabel(inner, SWT.NONE);
		titleLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
		titleLabel.setText(notification.getTitle());
		//styling.setId(titleLabel, DIALOG_TITLE_STYLE_ID);

		Label text = new Label(inner, SWT.WRAP);
		//styling.setId(text, DIALOG_TEXT_STYLE_ID);
		
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		text.setLayoutData(gd);
		text.setText(notification.getText());

		shell.setSize(POPUP_SIZE);

		// move other shells up
		if (!activePopups.isEmpty())
		{
			List<PopupNotification> modifiable = new ArrayList<PopupNotification>(activePopups);
			Collections.reverse(modifiable);
			for (PopupNotification popup : modifiable)
			{
				Point currentLocation = popup.shell.getLocation();
				int newY = currentLocation.y - POPUP_SIZE.y;
				if (newY < 0)
				{
					activePopups.remove(popup);
					popup.dispose();
				}
				else
				{
					popup.shell.setLocation(currentLocation.x, newY);
				}
			}
		}

		Rectangle clientArea = Display.getDefault().getActiveShell().getMonitor().getClientArea();

		int startX = clientArea.x + clientArea.width - POPUP_SIZE.x - PADDING;
		int startY = clientArea.y + clientArea.height - POPUP_SIZE.y - PADDING;
		
		shell.setLocation(startX, startY);
		shell.setAlpha(0);
		shell.setVisible(true);

		activePopups.add(this);

		fadeIn();
	}

	private boolean noActiveMonitorExists()
	{
		return Display.getDefault().getActiveShell() == null || Display.getDefault().getActiveShell().getMonitor() == null;
	}
	
	/**
	 * Fade in this popup over the configured duration
	 */
	private void fadeIn()
	{
		Runnable run = new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					if (shell == null || shell.isDisposed())
					{
						return;
					}

					int popupAlpha = shell.getAlpha();
					popupAlpha += FADE_IN_ALPHA_STEP;

					if (popupAlpha > FINAL_ALPHA)
					{
						shell.setAlpha(FINAL_ALPHA);
						startDisplayTimer();
						return;
					}

					shell.setAlpha(popupAlpha);
					Display.getDefault().timerExec(FADE_TICK, this);
				}
				catch (Exception err)
				{
					err.printStackTrace();
				}
			}

		};
		Display.getDefault().timerExec(FADE_TICK, run);
	}

	/**
	 * Start the display timer for this popup
	 */
	private void startDisplayTimer()
	{
		Runnable run = new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					if (shell == null || shell.isDisposed())
					{
						return;
					}

					fadeOut();
				}
				catch (Exception err)
				{
					err.printStackTrace();
				}
			}

		};
		Display.getDefault().timerExec(preferences.getDisplayDuration(), run);
	}

	/**
	 * Fade out this popup over the configured duration
	 */
	private void fadeOut()
	{
		final Runnable run = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (shell == null || shell.isDisposed())
					{
						return;
					}

					int cur = shell.getAlpha();
					cur -= FADE_OUT_ALPHA_STEP;

					if (cur <= 0)
					{
						shell.setAlpha(0);
						shell.dispose();
						activePopups.remove(this);
						return;
					}

					shell.setAlpha(cur);

					Display.getDefault().timerExec(FADE_TICK, this);

				}
				catch (Exception err)
				{
					err.printStackTrace();
				}
			}
		};
		Display.getDefault().timerExec(FADE_TICK, run);
	}
	
	/**
	 * Perform required cleanup
	 */
	public void dispose()
	{
		if (shell != null)
		{
			shell.dispose();
		}
	}
	
}
