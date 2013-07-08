/*******************************************************************************
 * Copyright 2013 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.logging;

import org.eclipse.equinox.log.ExtendedLogService;
import org.eclipse.equinox.log.Logger;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;

/**
 * An {@link ExtendedLogService} implementation that delegates logging to the
 * SLF4J logging mechanism.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class SLF4JExtendedLogService extends SLF4JOSGiLogServiceBridge implements ExtendedLogService
{

	@SuppressWarnings("nls")
	@Override
	public void log(Object context, int level, String message)
	{
		log(level, (context == null ? "" : context.getClass().getCanonicalName() + " - ") + message);
	}

	@SuppressWarnings("nls")
	@Override
	public void log(Object context, int level, String message, Throwable exception)
	{
		log(level, (context == null ? "" : context.getClass().getCanonicalName() + " - ") + message, exception);
	}

	@Override
	public boolean isLoggable(int level)
	{
		switch (level)
		{
		case LogService.LOG_DEBUG:
			return logger.isDebugEnabled();
		case LogService.LOG_INFO:
			return logger.isInfoEnabled();
		case LogService.LOG_WARNING:
			return logger.isWarnEnabled();
		case LogService.LOG_ERROR:
			return logger.isErrorEnabled();
		default:
			return logger.isTraceEnabled();
		}
	}

	@Override
	public String getName()
	{
		return logger.getName();
	}

	@Override
	public Logger getLogger(String loggerName)
	{
		return this;
	}

	@Override
	public Logger getLogger(Bundle bundle, String loggerName)
	{
		return this;
	}


}
