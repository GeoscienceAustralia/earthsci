package au.gov.ga.earthsci.notification;

/**
 * An interface that provides a callback that can be executed when a user responds to 
 * a notification
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface INotificationAction
{
	/**
	 * @return The text to use for the action. This may be used for a hyperlink etc.
	 */
	String getText();
	
	/**
	 * @return The tooltip to use for the action (if applicable)
	 */
	String getTooltip();
	
	/**
	 * Execute the callback for this action
	 */
	void run();
}
