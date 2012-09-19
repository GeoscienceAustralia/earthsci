package au.gov.ga.earthsci.notification.popup.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
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
import au.gov.ga.earthsci.notification.NotificationLevel;
import au.gov.ga.earthsci.notification.popup.preferences.IPopupNotificationPreferences;

/**
 * A popup notification widget that renders a notification in a nice fading popup.
 * <p/>
 * Implementation is inspired by the hexapixel tutorial available 
 * <a href="http://hexapixel.com/2009/06/30/creating-a-notification-popup-widget">here</a>.
 * 
 * @see http://hexapixel.com/2009/06/30/creating-a-notification-popup-widget
 * @author James Navin (james.navin@ga.gov.au)
 */
public class PopupNotification
{
	// Style ID constants
	private static final String DIALOG_TITLE_STYLE_CLASS = "popupDialogTitle"; //$NON-NLS-1$
	private static final String DIALOG_TEXT_STYLE_CLASS = "popupDialogText"; //$NON-NLS-1$
	private static final String DIALOG_IMAGE_STYLE_CLASS = "popupDialogImage"; //$NON-NLS-1$
	private static final String DIALOG_SHELL_STYLE_CLASS = "popupDialog"; //$NON-NLS-1$
	
	private static final String DIALOG_INFORMATION_CLASS = "information"; //$NON-NLS-1$
	private static final String DIALOG_WARNING_CLASS = "warning"; //$NON-NLS-1$
	private static final String DIALOG_ERROR_CLASS = "error"; //$NON-NLS-1$
	
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
	
	/** Preferences used to control the appearance and behaviour of the popups */
	@Inject
	private static IPopupNotificationPreferences preferences;
	
	/**
	 * Show the given notification as a popup using the current popup preferences and styling 
	 * 
	 * @param notification The notification to show
	 */
	public static void show(INotification notification, IPopupNotificationPreferences preferences)
	{
		PopupNotification pn = new PopupNotification(preferences);
		pn.showPopupNotification(notification);
	}

	/** An appropriately styled shell used to create the popup */
	private Shell shell;
	
	/**
	 * No-arg constructor used for dependency injection
	 */
	PopupNotification(IPopupNotificationPreferences preferences)
	{
		this.preferences = preferences;
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
		
		initialiseShell();

		Composite inner = initialiseInner();

		CLabel imageLabel = addImageLabel(notification,inner);
		CLabel titleLabel = addTitleLabel(notification, inner);
		Label textLabel = addTextLabel(notification, inner);

		adjustExistingPopups();

		String levelClass = notification.getLevel() == NotificationLevel.INFORMATION ? DIALOG_INFORMATION_CLASS : notification.getLevel() == NotificationLevel.WARNING ? DIALOG_WARNING_CLASS : DIALOG_ERROR_CLASS;
		WidgetElement.setCSSClass(shell, DIALOG_SHELL_STYLE_CLASS + " " + levelClass);
		WidgetElement.setCSSClass(titleLabel, DIALOG_TITLE_STYLE_CLASS + " " + levelClass);
		WidgetElement.setCSSClass(imageLabel, DIALOG_IMAGE_STYLE_CLASS + " " + levelClass);
		WidgetElement.setCSSClass(textLabel, DIALOG_TEXT_STYLE_CLASS + " " + levelClass);
		WidgetElement.getEngine(Display.getCurrent()).applyStyles(shell, true);
		
		shell.setVisible(true);

		activePopups.add(this);

		fadeIn();
	}

	private void adjustExistingPopups()
	{
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
	}

	private Label addTextLabel(INotification notification, Composite inner)
	{
		Label text = new Label(inner, SWT.WRAP);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		text.setLayoutData(gd);
		text.setText(notification.getText());
		return text;
	}

	private CLabel addTitleLabel(INotification notification, Composite inner)
	{
		CLabel titleLabel = new CLabel(inner, SWT.NONE);
		titleLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
		titleLabel.setText(notification.getTitle());
		return titleLabel;
	}

	private CLabel addImageLabel(INotification notification, Composite inner)
	{
		CLabel imgLabel = new CLabel(inner, SWT.NONE);
		imgLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_BEGINNING));
		//imgLabel.setImage(type.getImage());
		return imgLabel;
	}

	private void initialiseShell()
	{
		shell = new Shell(getRootShell(Display.getDefault().getActiveShell()), SWT.NO_FOCUS | SWT.NO_TRIM);
		
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
		shell.setSize(POPUP_SIZE);
		shell.setAlpha(0);
		
		Rectangle clientArea = Display.getDefault().getActiveShell().getMonitor().getClientArea();

		int startX = clientArea.x + clientArea.width - POPUP_SIZE.x - PADDING;
		int startY = clientArea.y + clientArea.height - POPUP_SIZE.y - PADDING;
		
		shell.setLocation(startX, startY);
	}

	private Composite initialiseInner()
	{
		final Composite inner = new Composite(shell, SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		gl.marginLeft = 5;
		gl.marginTop = 0;
		gl.marginRight = 5;
		gl.marginBottom = 5;
		inner.setLayout(gl);
		return inner;
	}
	
	private boolean noActiveMonitorExists()
	{
		return Display.getDefault().getActiveShell() == null || Display.getDefault().getActiveShell().getMonitor() == null;
	}
	
	/**
	 * Helper method that finds the root shell of the current shell.
	 * <p/>
	 * Used in the case where the active shell is e.g. a dialog box 
	 */
	private static Shell getRootShell(Composite s)
	{
		if (s.getParent() == null)
		{
			return (Shell)s;
		}
		return getRootShell(s.getParent());
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
				catch (Exception e)
				{
					e.printStackTrace();
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

					int popupAlpha = shell.getAlpha();
					popupAlpha -= FADE_OUT_ALPHA_STEP;

					if (popupAlpha <= 0)
					{
						shell.setAlpha(0);
						dispose();
						activePopups.remove(this);
						return;
					}
					shell.setAlpha(popupAlpha);
					Display.getDefault().timerExec(FADE_TICK, this);
				}
				catch (Exception e)
				{
					e.printStackTrace();
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
