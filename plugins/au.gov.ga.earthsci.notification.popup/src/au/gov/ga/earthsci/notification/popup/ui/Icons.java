package au.gov.ga.earthsci.notification.popup.ui;

import java.util.EnumMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import au.gov.ga.earthsci.notification.NotificationLevel;

/**
 * A helper class for providing access to popup notification icons.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Icons
{

	private static final Map<NotificationLevel, Image> LEVEL_ICONS = new EnumMap<NotificationLevel, Image>(NotificationLevel.class);
	static
	{
		LEVEL_ICONS.put(NotificationLevel.INFORMATION, new Image(Display.getDefault(), Icons.class.getResourceAsStream("/icons/information.gif"))); //$NON-NLS-1$
		LEVEL_ICONS.put(NotificationLevel.ERROR, new Image(Display.getDefault(), Icons.class.getResourceAsStream("/icons/error.gif"))); //$NON-NLS-1$
		LEVEL_ICONS.put(NotificationLevel.WARNING, new Image(Display.getDefault(), Icons.class.getResourceAsStream("/icons/warning.gif"))); //$NON-NLS-1$
	}
	
	private static final Image CLOSE_ICON = new Image(Display.getDefault(), Icons.class.getResourceAsStream("/icons/close.gif")); //$NON-NLS-1$
	private static final Image CLOSE_HOVER_ICON = new Image(Display.getDefault(), Icons.class.getResourceAsStream("/icons/close_hot.gif")); //$NON-NLS-1$
	
	/**
	 * @return The icon to use for the provided notification level
	 */
	public static Image getIcon(NotificationLevel level)
	{
		return LEVEL_ICONS.get(level);
	}
	
	/**
	 * @return The icon to use for the 'close' button for popups
	 */
	public static Image getCloseIcon()
	{
		return CLOSE_ICON;
	}
	
	/**
	 * @return The icon to use for the 'close' button for popups when the mouse is hovering
	 */
	public static Image getCloseHoverIcon()
	{
		return CLOSE_HOVER_ICON;
	}
}
