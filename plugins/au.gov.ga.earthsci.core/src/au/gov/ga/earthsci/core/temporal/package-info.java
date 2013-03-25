/**
 * The top-level package for components that deal with real-world time.
 * 
 * <p/>
 * 
 * Real-world time in the system is represented by {@link au.gov.ga.earthsci.core.temporal.BigTime}
 * instances. These allow nanosecond resolution times to be stored at magnitudes that exceed geologic
 * time lengths.
 * <br/>
 * Along with a time, each {@link au.gov.ga.earthsci.core.temporal.BigTime} instance has an associated
 * resolution, which is an indication of the scale at which the time can be used. For example, 
 * a {@link au.gov.ga.earthsci.core.temporal.BigTime} instance used to represent a geologic time may
 * have a resolution of millions of years, while an instance used to represent a timestep in a weather
 * simulation might have resolution of seconds or even milliseconds.
 * <br/>
 * The {@link au.gov.ga.earthsci.core.temporal.BigTime} class provides methods for
 * converting to/from the standard {@link java.util.Date} time representation where possible 
 * (e.g. where the time instant falls in the range supported by the JUD representation). 
 * 
 * <p/>
 * 
 * The central concept of the <em>current real-world time</em> within the application is maintained
 * in the {@link au.gov.ga.earthsci.core.temporal.Chronos} singleton. This can be considered the
 * point of truth for what is the currently set real-world time. The time maintained on the 
 * {@link au.gov.ga.earthsci.core.temporal.Chronos} instant may be set via a UI element 
 * (e.g. a time slider), or programmatically (e.g. via some automated transition). When the current
 * real-world time changes on the {@link au.gov.ga.earthsci.core.temporal.Chronos} instance, all
 * registered listeners will be notified to allow them to update and react accordingly.
 * 
 * <p/>
 * 
 * <b>Note:</b> A distinction needs to be made between current <em>physical time</em> (being the 
 * wall clock time available from {@link System#currentTimeMillis()}), and current <em>real-world time</em>
 * (being the virtual point along the infinite timeline that the application uses for the purposes of
 * visualisation and/or analysis).
 * 
 * <p/>
 * 
 * The notion of an object that can respond to changes in real-world time is captured by the
 * {@link au.gov.ga.earthsci.core.temporal.ITemporal} interface. Classes that implement this 
 * interface are able to apply their state to the world at a given real-world time instant, and
 * can give an indication of over what time range they have state to apply.
 * 
 */
package au.gov.ga.earthsci.core.temporal;

