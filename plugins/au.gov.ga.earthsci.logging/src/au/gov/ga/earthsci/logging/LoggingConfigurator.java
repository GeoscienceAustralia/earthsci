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

import org.eclipse.core.internal.runtime.PlatformLogWriter;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.services.log.ILoggerProvider;
import org.eclipse.equinox.log.ExtendedLogService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * A class used to bootstrap the configuration of logging.
 * <p/>
 * Searches the current workspace for a {@value #LOGBACK_XML} configuration
 * file. If it doesn't find one, it will use a default configuration that
 * outputs to the console.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LoggingConfigurator
{

	private static final String LOGBACK_XML = "logback.xml"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(LoggingConfigurator.class);

	private static int defaultLoggingLevel;

	private LoggingConfigurator()
	{
	};

	/**
	 * Configure the logging to replace the standard workbench loggers with
	 * SLF4J bridges.
	 * 
	 * @param bundleContext
	 *            The current bundle context
	 */
	public static void configure(final BundleContext bundleContext)
	{
		configureLogback();
		registerOSGiLogService(bundleContext);
		bypassRuntimeLog(bundleContext);

		logger.debug("Logging configuration initialised."); //$NON-NLS-1$
	}

	/**
	 * @return The current global logging level:
	 *         <ol>
	 *         <li>Trace
	 *         <li>Debug
	 *         <li>Info
	 *         <li>Warn
	 *         <li>Error
	 *         <li>Fatal
	 *         </ol>
	 */
	public static int getGlobalLoggingLevel()
	{
		ch.qos.logback.classic.Logger root =
				(ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

		return toLoggingLevel(root.getLevel());
	}

	/**
	 * @return The default global logging level as set in the configuration
	 *         files
	 * 
	 * @see #getGlobalLoggingLevel()
	 */
	public static int getDefaultLoggingLevel()
	{
		return defaultLoggingLevel;
	}

	private static int toLoggingLevel(Level l)
	{
		switch (l.levelInt)
		{
		case Level.TRACE_INT:
			return 1;
		case Level.DEBUG_INT:
			return 2;
		case Level.INFO_INT:
			return 3;
		case Level.WARN_INT:
			return 4;
		case Level.ERROR_INT:
			return 5;
		default:
			return 5;
		}
	}

	/**
	 * Set the global logging level, overriding any settings found in the
	 * configuration files
	 * 
	 * @param l
	 *            The level to set:
	 *            <ol>
	 *            <li>Trace
	 *            <li>Debug
	 *            <li>Info
	 *            <li>Warn
	 *            <li>Error
	 *            <li>Fatal
	 *            </ol>
	 */
	public static void setGlobalLoggingLevel(int l)
	{
		ch.qos.logback.classic.Logger root =
				(ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

		Level level = Level.DEBUG;
		switch (l)
		{
		case 1:
			level = Level.TRACE;
			break;
		case 2:
			level = Level.DEBUG;
			break;
		case 3:
			level = Level.INFO;
			break;
		case 4:
			level = Level.WARN;
			break;
		case 5:
			level = Level.ERROR;
			break;
		case 6:
			level = Level.ERROR;
			break;
		}
		root.setLevel(level);
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

		ch.qos.logback.classic.Logger root =
				(ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		defaultLoggingLevel = toLoggingLevel(root.getLevel());
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
		LevelChangePropagator levelChangePropagator = new LevelChangePropagator();
		levelChangePropagator.setResetJUL(true);
		loggerContext.addListener(levelChangePropagator);
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
	 * 
	 * @param bundleContext
	 */
	private static void registerOSGiLogService(BundleContext bundleContext)
	{
		bundleContext.registerService(LogService.class, new SLF4JOSGiLogServiceBridge(), null);
		bundleContext.registerService(ILoggerProvider.class, new SLF4JE4BridgeLoggerProvider(), null);
		bundleContext.registerService(ExtendedLogService.class, new SLF4JExtendedLogService(), null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
	private static void bypassRuntimeLog(BundleContext context)
	{
		ServiceReference packageAdminRef =
				context.getServiceReference(org.osgi.service.packageadmin.PackageAdmin.class.getName());
		org.osgi.service.packageadmin.PackageAdmin packageAdmin =
				(org.osgi.service.packageadmin.PackageAdmin) context.getService(packageAdminRef);
		PlatformLogWriter writer =
				new PlatformLogWriter(new SLF4JExtendedLogService(), packageAdmin, context.getBundle());

		RuntimeLogBypass.apply(writer);
	}
}
