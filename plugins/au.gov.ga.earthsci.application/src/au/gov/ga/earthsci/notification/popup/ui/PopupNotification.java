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

/**
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class PopupNotification
{
	private static final String DIALOG_TITLE_STYLE_ID = "popupDialogTitle"; //$NON-NLS-1$
	private static final String DIALOG_TEXT_STYLE_ID = "popupDialogText"; //$NON-NLS-1$
	private static final String DIALOG_SHELL_STYLE_ID = "popupDialog"; //$NON-NLS-1$
	
	// how long the the tray popup is displayed after fading in (in milliseconds)
	private static final int DISPLAY_TIME = 4500;
	
	// how long each tick is when fading in (in ms)
	private static final int FADE_TIMER = 50;
	
	// how long each tick is when fading out (in ms)
	private static final int FADE_IN_STEP = 30;
	
	// how many tick steps we use when fading out 
	private static final int FADE_OUT_STEP = 8;

	// how high the alpha value is when we have finished fading in 
	private static final int FINAL_ALPHA = 225;

	private static Point shellSize = new Point(350, 100);
	
	private static List<Shell> activeShells = new ArrayList<Shell>();
	private static Shell shell;

	@Inject
	private static IStylingEngine styling;
	
	public static void show(INotification notification)
	{
		shell = new Shell(Display.getDefault().getActiveShell(), SWT.NO_FOCUS | SWT.NO_TRIM);
		//styling.setId(shell, DIALOG_SHELL_STYLE_ID);
		
		shell.setLayout(new FillLayout());
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		shell.addListener(SWT.Dispose, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				activeShells.remove(shell);
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

		shell.setSize(shellSize);

		if (Display.getDefault().getActiveShell() == null || Display.getDefault().getActiveShell().getMonitor() == null)
		{
			return;
		}

		Rectangle clientArea = Display.getDefault().getActiveShell().getMonitor().getClientArea();

		int startX = clientArea.x + clientArea.width - 352;
		int startY = clientArea.y + clientArea.height - 102;

		// move other shells up
		if (!activeShells.isEmpty())
		{
			List<Shell> modifiable = new ArrayList<Shell>(activeShells);
			Collections.reverse(modifiable);
			for (Shell shell : modifiable)
			{
				Point curLoc = shell.getLocation();
				shell.setLocation(curLoc.x, curLoc.y - 100);
				if (curLoc.y - 100 < 0)
				{
					activeShells.remove(shell);
					shell.dispose();
				}
			}
		}

		shell.setLocation(startX, startY);
		shell.setAlpha(0);
		shell.setVisible(true);

		activeShells.add(shell);

		fadeIn(shell);
	}

	private static void fadeIn(final Shell _shell)
	{
		Runnable run = new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					if (_shell == null || _shell.isDisposed())
					{
						return;
					}

					int cur = _shell.getAlpha();
					cur += FADE_IN_STEP;

					if (cur > FINAL_ALPHA)
					{
						_shell.setAlpha(FINAL_ALPHA);
						startTimer(_shell);
						return;
					}

					_shell.setAlpha(cur);
					Display.getDefault().timerExec(FADE_TIMER, this);
				}
				catch (Exception err)
				{
					err.printStackTrace();
				}
			}

		};
		Display.getDefault().timerExec(FADE_TIMER, run);
	}

	private static void startTimer(final Shell _shell)
	{
		Runnable run = new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					if (_shell == null || _shell.isDisposed())
					{
						return;
					}

					fadeOut(_shell);
				}
				catch (Exception err)
				{
					err.printStackTrace();
				}
			}

		};
		Display.getDefault().timerExec(DISPLAY_TIME, run);

	}

	private static void fadeOut(final Shell shell)
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
					cur -= FADE_OUT_STEP;

					if (cur <= 0)
					{
						shell.setAlpha(0);
						shell.dispose();
						activeShells.remove(shell);
						return;
					}

					shell.setAlpha(cur);

					Display.getDefault().timerExec(FADE_TIMER, this);

				}
				catch (Exception err)
				{
					err.printStackTrace();
				}
			}

		};
		Display.getDefault().timerExec(FADE_TIMER, run);
	}
}
