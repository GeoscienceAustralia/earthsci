package au.gov.ga.earthsci.notification;

import java.util.Comparator;

import au.gov.ga.earthsci.core.util.message.MessageSourceAccessor;

/**
 * An enumeration of the notification levels available in the system.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public enum NotificationLevel
{
	
	INFORMATION(1, "au.gov.ga.earthsci.notification.NotificationLevel.information"), //$NON-NLS-1$
	WARNING(5, "au.gov.ga.earthsci.notification.NotificationLevel.warning"), //$NON-NLS-1$
	ERROR(10, "au.gov.ga.earthsci.notification.NotificationLevel.error"); //$NON-NLS-1$
	
	static
	{
		MessageSourceAccessor.addBundle("au.gov.ga.earthsci.notification.messages"); //$NON-NLS-1$
	}
	
	private int severity;
	private String labelKey;
	
	private NotificationLevel(int severity, String labelKey)
	{
		this.severity = severity;
		this.labelKey = labelKey;
	}
	
	public int getSeverity()
	{
		return severity;
	}
	
	public String getLabel()
	{
		return MessageSourceAccessor.getMessage(labelKey);
	}
	
	/** A comparator that will sort notification levels descending by severity */
	public static Comparator<NotificationLevel> SEVERITY_DESCENDING = new Comparator<NotificationLevel>()
	{
		@Override
		public int compare(NotificationLevel o1, NotificationLevel o2)
		{
			return o2.severity - o1.severity;
		}
	};
}
