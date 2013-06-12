package au.gov.ga.earthsci.notification;

import java.util.Comparator;

import org.eclipse.core.runtime.IStatus;

/**
 * An enumeration of the notification levels available in the system.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public enum NotificationLevel
{
	INFORMATION(1, Messages.NotificationLevel_Information, IStatus.INFO),
	WARNING(5, Messages.NotificationLevel_Warning, IStatus.WARNING),
	ERROR(10, Messages.NotificationLevel_Error, IStatus.ERROR);

	private final int severity;
	private final String label;
	private final int statusSeverity;

	private NotificationLevel(int severity, String label, int statusSeverity)
	{
		this.severity = severity;
		this.label = label;
		this.statusSeverity = statusSeverity;
	}

	public int getSeverity()
	{
		return severity;
	}

	public String getLabel()
	{
		return label;
	}

	/**
	 * @return {@link IStatus} severity of this notification level. One of
	 *         <ul>
	 *         <li>{@link IStatus#INFO}</li>
	 *         <li>{@link IStatus#WARNING}</li>
	 *         <li>{@link IStatus#ERROR}</li>
	 *         </ul>
	 */
	public int getStatusSeverity()
	{
		return statusSeverity;
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
