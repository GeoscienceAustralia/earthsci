package au.gov.ga.earthsci.notification.popup.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import au.gov.ga.earthsci.notification.INotification;
import au.gov.ga.earthsci.notification.INotificationAction;
import au.gov.ga.earthsci.notification.NotificationLevel;
import au.gov.ga.earthsci.notification.popup.Messages;
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
	private static final int NUM_COLUMNS = 3;
	private static final String POPUPS_CSS = "/css/popups.css"; //$NON-NLS-1$

	// Style ID constants
	private static final String DIALOG_TITLE_STYLE_CLASS = "popupDialogTitle"; //$NON-NLS-1$
	private static final String DIALOG_TEXT_STYLE_CLASS = "popupDialogText"; //$NON-NLS-1$
	private static final String DIALOG_IMAGE_STYLE_CLASS = "popupDialogImage"; //$NON-NLS-1$
	private static final String DIALOG_LINK_STYLE_CLASS = "popupDialogLink"; //$NON-NLS-1$
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
	
	@Inject
	private static Logger logger;
	
	/** Whether the plugin-specifc CSS has been loaded */
	private static AtomicBoolean cssLoaded = new AtomicBoolean(false);
	
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
		PopupNotification.preferences = preferences;
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
		
		initialiseShell(notification);

		Composite inner = initialiseInner(notification);

		addLevelImage(notification,inner);
		addTitleLabel(notification, inner);
		addCloseButton(notification, inner);
		addTextLabel(notification, inner);
		addActionLinks(notification, inner);
		
		adjustExistingPopups();
		
		applyCSSStyling();
		
		shell.setVisible(true);

		activePopups.add(this);

		fadeIn();
	}

	/**
	 * Move existing popups up the screen to make way for the new popup
	 */
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
					popup.close();
				}
				else
				{
					popup.shell.setLocation(currentLocation.x, newY);
				}
			}
		}
	}

	/**
	 * Add the notification text to the dialog
	 */
	private Label addTextLabel(INotification notification, Composite inner)
	{
		Label text = new Label(inner, SWT.WRAP);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = NUM_COLUMNS;
		text.setLayoutData(gd);
		text.setText(notification.getText());
		
		setCSSClass(text, DIALOG_TEXT_STYLE_CLASS, notification.getLevel());
		
		return text;
	}

	/**
	 * Add the notification title to the dialog
	 */
	private CLabel addTitleLabel(INotification notification, Composite inner)
	{
		CLabel titleLabel = new CLabel(inner, SWT.NONE);
		titleLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
		titleLabel.setText(notification.getTitle());
		
		setCSSClass(titleLabel, DIALOG_TITLE_STYLE_CLASS, notification.getLevel());
		
		return titleLabel;
	}

	private Label addCloseButton(INotification notification, Composite inner)
	{
		final Label imageLabel = new Label(inner, SWT.FLAT);
		imageLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END));
		imageLabel.setImage(Icons.getCloseIcon());
		imageLabel.setToolTipText(Messages.PopupNotification_CloseTooltip);
		imageLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDown(MouseEvent e)
			{
				close();
			}
		});
		imageLabel.addMouseTrackListener(new MouseTrackAdapter()
		{
			@Override
			public void mouseEnter(MouseEvent e)
			{
				imageLabel.setImage(Icons.getCloseHoverIcon());
			}
			
			@Override
			public void mouseExit(MouseEvent e)
			{
				imageLabel.setImage(Icons.getCloseIcon());
			}
		});
		return imageLabel;
	}
	
	/**
	 * Add an image representing the notification level
	 */
	private CLabel addLevelImage(INotification notification, Composite inner)
	{
		CLabel imageLabel = new CLabel(inner, SWT.NONE);
		imageLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_BEGINNING));
		imageLabel.setImage(Icons.getIcon(notification.getLevel()));
		setCSSClass(imageLabel, DIALOG_IMAGE_STYLE_CLASS, notification.getLevel());
		
		return imageLabel;
	}

	/**
	 * Add a link for each notification action present on the notification
	 */
	private List<Link> addActionLinks(INotification notification, Composite inner)
	{
		
		List<Link> result = new ArrayList<Link>();
		
		if (notification.getActions() == null || notification.getActions().length == 0)
		{
			return result;
		}
		
		for (final INotificationAction action : notification.getActions())
		{
			Link link = new Link(inner, SWT.NONE);

			GridData gd = new GridData(GridData.FILL_BOTH);
			gd.horizontalSpan = NUM_COLUMNS;
			link.setLayoutData(gd);
			
			link.setToolTipText(action.getTooltip());
			link.setText("- <a>" + action.getText() + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
			link.addSelectionListener(new SelectionListener()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					action.run();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e)
				{
					action.run();
				}
			});
			
			setCSSClass(link, DIALOG_LINK_STYLE_CLASS, notification.getLevel());
			
			result.add(link);
		}
		
		return result;
	}
	
	/**
	 * Create the shell that will be used to display the dialog
	 */
	private void initialiseShell(INotification notification)
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

	/**
	 * Initialise the grid that will hold the dialog components
	 */
	private Composite initialiseInner(INotification notification)
	{
		final Composite inner = new Composite(shell, SWT.NONE);
		GridLayout gl = new GridLayout(NUM_COLUMNS, false);
		gl.marginLeft = 5;
		gl.marginTop = 0;
		gl.marginRight = 5;
		gl.marginBottom = 5;
		inner.setLayout(gl);
		
		setCSSClass(inner, DIALOG_SHELL_STYLE_CLASS, notification.getLevel());
		
		return inner;
	}
	
	/**
	 * Setup the CSS class attribute for the provided widget, adding the appropriate level class
	 */
	private void setCSSClass(Widget widget, String mainClass, NotificationLevel notificationLevel)
	{
		WidgetElement.setCSSClass(widget, mainClass + " " + getLevelClass(notificationLevel)); //$NON-NLS-1$
	}
	
	/**
	 * @return The CSS class to use for the provided notification level
	 */
	private String getLevelClass(NotificationLevel level)
	{
		return level == NotificationLevel.INFORMATION ? DIALOG_INFORMATION_CLASS : level == NotificationLevel.WARNING ? DIALOG_WARNING_CLASS : DIALOG_ERROR_CLASS;
	}
	
	/**
	 * Apply the styling to the shell and all of its children
	 */
	private void applyCSSStyling()
	{
		CSSEngine cssEngine = WidgetElement.getEngine(Display.getCurrent());
		
		if (!cssLoaded.get())
		{
			try
			{
				cssEngine.parseStyleSheet(getClass().getResourceAsStream(POPUPS_CSS));
			}
			catch (IOException e)
			{
				logger.error(e, "Exception occurred while loading popup notification CSS"); //$NON-NLS-1$
			}
			cssLoaded.set(true);
		}
		
		cssEngine.applyStyles(shell, true);
	}
	
	/**
	 * @return <code>true</code> if there is no active shell or monitor in the current display hierachy
	 */
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
						close();
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
	 * Close the popup
	 */
	public void close()
	{
		dispose();
		activePopups.remove(this);
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
