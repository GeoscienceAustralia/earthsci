package au.gov.ga.earthsci.notification.ui.handlers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;

import au.gov.ga.earthsci.notification.ui.NotificationPart;
import au.gov.ga.earthsci.notification.ui.NotificationPart.Grouping;

/**
 * A handler that receives group-by events from the {@link NotificationPart}
 * menu
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GroupByHandler
{
	public static final String ID = "au.gov.ga.earthsci.notification.part.groupby"; //$NON-NLS-1$

	public static final String GROUP_BY_NONE_MENU_ID =
			"au.gov.ga.earthsci.notification.notifications.viewmenu.group.none"; //$NON-NLS-1$
	public static final String GROUP_BY_LEVEL_MENU_ID =
			"au.gov.ga.earthsci.notification.notifications.viewmenu.group.level"; //$NON-NLS-1$
	public static final String GROUP_BY_CATEGORY_MENU_ID =
			"au.gov.ga.earthsci.notification.notifications.viewmenu.group.category"; //$NON-NLS-1$

	private static final Map<String, Grouping> idToGrouping = new HashMap<String, Grouping>();
	static
	{
		idToGrouping.put(GROUP_BY_NONE_MENU_ID, Grouping.NONE);
		idToGrouping.put(GROUP_BY_LEVEL_MENU_ID, Grouping.LEVEL);
		idToGrouping.put(GROUP_BY_CATEGORY_MENU_ID, Grouping.CATEGORY);
	}

	public static Grouping getGroupingForMenuItemId(String id)
	{
		if (!idToGrouping.containsKey(id))
		{
			return Grouping.NONE;
		}
		return idToGrouping.get(id);
	}

	@Execute
	public void execute(MMenuItem item, NotificationPart part)
	{
		if (!item.isSelected())
		{
			return;
		}
		part.setGrouping(getGroupingForMenuItemId(item.getElementId()));
	}

}
