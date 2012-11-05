package au.gov.ga.earthsci.progress.part;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "au.gov.ga.earthsci.progress.part.messages"; //$NON-NLS-1$
	public static String ProgressPart_JobNameColumnLabel;
	public static String ProgressPart_JobProgressColumnLabel;
	public static String ProgressPart_JobStateColumnLabel;
	public static String ProgressPart_JobTaskColumnLabel;
	public static String ProgressPart_RunningStatusLabel;
	public static String ProgressPart_SleepingStatusLabel;
	public static String ProgressPart_UnknownProgress;
	public static String ProgressPart_WaitingStatusLabel;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
