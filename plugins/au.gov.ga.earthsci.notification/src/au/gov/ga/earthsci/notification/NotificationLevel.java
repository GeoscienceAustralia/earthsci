package au.gov.ga.earthsci.notification;

import java.util.Comparator;

/**
 * An enumeration of the notification levels available in the system.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public enum NotificationLevel
{
	INFORMATION(1, Messages.NotificationLevel_Information),
	WARNING(5, Messages.NotificationLevel_Warning),
	ERROR(10, Messages.NotificationLevel_Error);

	private int severity;
	private String label;

	private NotificationLevel(int severity, String label)
	{
		this.severity = severity;
		this.label = label;
	}

	public int getSeverity()
	{
		return severity;
	}

	public String getLabel()
	{
		return label;
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
