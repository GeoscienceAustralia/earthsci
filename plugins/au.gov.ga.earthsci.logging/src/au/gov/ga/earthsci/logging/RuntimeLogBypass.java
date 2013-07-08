package au.gov.ga.earthsci.logging;

import java.lang.reflect.Method;

import org.eclipse.core.internal.runtime.PlatformLogWriter;
import org.eclipse.core.internal.runtime.RuntimeLog;



/**
 * Hack to remove RuntimeLog behaviour
 */
public final class RuntimeLogBypass
{

	/**
	 * Apply the bypass to the RuntimeLog
	 */
	@SuppressWarnings("nls")
	public static void apply(PlatformLogWriter plw)
	{
		try
		{
			Method m = RuntimeLog.class.getDeclaredMethod("setLogWriter", PlatformLogWriter.class);
			m.setAccessible(true);
			m.invoke(null, new Object[] { plw });
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}



}
