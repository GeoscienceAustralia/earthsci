/**
 * Provides classes to configure the application logging mechanism to use SLF4J rather than the default
 * workbench provided mechanisms.
 * <p/>
 * Provides classes to replace:
 * <ul>
 * 	<li>The e4 {@link org.eclipse.e4.core.services.log.Logger} service,
 *  <li>The {@link org.eclipse.e4.core.services.log.ILoggerProvider} factory, and
 *  <li>The OSGi {@link org.osgi.service.log.LogService}
 * </ul>
 * 
 * The bundle {@link au.gov.ga.earthsci.logging.Activator} is responsible for bootstrapping the configuration
 * on startup via the {@link au.gov.ga.earthsci.logging.LoggingConfigurator} 
 */
package au.gov.ga.earthsci.logging;

