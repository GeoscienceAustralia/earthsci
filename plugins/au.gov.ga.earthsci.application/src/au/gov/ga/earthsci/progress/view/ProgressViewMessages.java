package au.gov.ga.earthsci.progress.view;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ProgressViewMessages extends NLS
{
	private static final String BUNDLE_NAME = "au.gov.ga.earthsci.progress.view.progressViewMessages"; //$NON-NLS-1$
	public static String ProgressView_JobNameColumnLabel;
	public static String ProgressView_JobProgressColumnLabel;
	public static String ProgressView_JobStateColumnLabel;
	public static String ProgressView_JobTaskColumnLabel;
	public static String ProgressView_RunningStatusLabel;
	public static String ProgressView_SleepingStatusLabel;
	public static String ProgressView_UnknownProgress;
	public static String ProgressView_WaitingStatusLabel;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ProgressViewMessages.class);
	}

	private ProgressViewMessages()
	{
	}
}
