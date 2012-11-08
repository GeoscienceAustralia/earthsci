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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.services.log.ILoggerProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * A class used to bootstrap the configuration of logging.
 * <p/>
 * Searches the current workspace for a {@value #LOGBACK_XML} configuration file. If it doesn't find one,
 * it will use a default configuration that outputs to the console. 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LoggingConfigurator
{
	private static final String LOGBACK_XML = "logback.xml"; //$NON-NLS-1$
	
	private static final Logger logger = LoggerFactory.getLogger(LoggingConfigurator.class);

	private LoggingConfigurator() {};
	
	/**
	 * Configure the logging to replace the standard workbench loggers with SLF4J bridges.
	 * 
	 * @param bundleContext The current bundle context
	 */
	public static void configure(final BundleContext bundleContext)
	{
		configureLogback();
		registerOSGiLogService(bundleContext);
		
		logger.debug("Logging configuration initialised."); //$NON-NLS-1$
	}

	private static void configureLogback()
	{
		JoranConfigurator configurator = new JoranConfigurator();
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		configurator.setContext(loggerContext);
		loggerContext.reset();
		
		configureJULBridge(loggerContext);
		
		try
		{
			InputStream configurationStream = getConfiguration();
			configurator.doConfigure(configurationStream);
		}
		catch (JoranException e)
		{
			// Use status printer to show errors if needed
		}
		
		StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
	}

	private static void configureJULBridge(LoggerContext loggerContext)
	{
		// Remove existing handlers
		java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger(""); //$NON-NLS-1$
		Handler[] handlers = rootLogger.getHandlers();
		for (int i = 0; i < handlers.length; i++)
		{
			rootLogger.removeHandler(handlers[i]);
		}
		
		// Install the bridge
		loggerContext.addListener(new LevelChangePropagator());
		SLF4JBridgeHandler.install();
	}

	private static InputStream getConfiguration()
	{
		// Look in the current workspace for a logback XML
		try
		{
			URL workspaceConfig = Platform.getConfigurationLocation().getDataArea(LOGBACK_XML);
			return workspaceConfig.openStream();
		}
		catch (IOException e)
		{

		}
		
		// Fallback to the default configuration
		return LoggingConfigurator.class.getResourceAsStream(LOGBACK_XML);
	}
	
	/**
	 * Replace the OSGi LogService implementation, and the ILoggerProvider
	 * @param bundleContext
	 */
	private static void registerOSGiLogService(BundleContext bundleContext)
	{
		bundleContext.registerService(LogService.class, new SLF4JOSGiLogServiceBridge(), null);
		bundleContext.registerService(ILoggerProvider.class, new SLF4JE4BridgeLoggerProvider(), null);
	}
}
