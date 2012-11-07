/*******************************************************************************
 * Copyright 2012 Geoscience Australia
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

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A bridge between the OSGi LogService and SLF4j framework
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class SLF4JOSGiLogServiceBridge implements LogService
{

	private static final Logger logger = LoggerFactory.getLogger("e4"); //$NON-NLS-1$
	
	@Override
	public void log(int level, String message)
	{
		switch (level)
		{
			case LogService.LOG_DEBUG: logger.debug(message); break;
			case LogService.LOG_INFO: logger.info(message); break;
			case LogService.LOG_WARNING: logger.warn(message); break;
			case LogService.LOG_ERROR: logger.error(message); break;
			default: logger.trace(message); break; 
		}
	}

	@Override
	public void log(int level, String message, Throwable exception)
	{
		switch (level)
		{
			case LogService.LOG_DEBUG: logger.debug(message, exception); break;
			case LogService.LOG_INFO: logger.info(message, exception); break;
			case LogService.LOG_WARNING: logger.warn(message, exception); break;
			case LogService.LOG_ERROR: logger.error(message, exception); break;
			default: logger.trace(message); break; 
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void log(ServiceReference sr, int level, String message)
	{
		log(level, message);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void log(ServiceReference sr, int level, String message, Throwable exception)
	{
		log(level, message, exception);
	}

}
