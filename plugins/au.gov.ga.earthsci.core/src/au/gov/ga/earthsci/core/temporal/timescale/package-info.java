/**
 * Contains components that can be used to describe an arbitrary real-world time scale.
 * 
 * <p/>
 * 
 * The primary class in this package is the {@link au.gov.ga.earthsci.core.temporal.timescale.ITimeScale},
 * which represents an arbitrary real-world time scale. 
 * 
 * Examples of real-world timescales include:
 * <ul>
 * <li>Geologic time scale (Eons, Eras, Periods, Epochs etc.)
 * <li>Human history time scale (Middle ages, Renaissance, Modern history etc.)
 * <li>Gregorian calendar time scale (A single span that uses days, months and years)
 * </ul>
 * 
 * An {@link au.gov.ga.earthsci.core.temporal.timescale.ITimeScale} is composed of a hierarchy of 
 * {@link au.gov.ga.earthsci.core.temporal.timescale.ITimePeriod}s, which exist at a number of defined
 * {@link au.gov.ga.earthsci.core.temporal.timescale.ITimeScaleLevel}s.
 * 
 * <p/>
 * 
 * {@link au.gov.ga.earthsci.core.temporal.timescale.ITimePeriod}s may overlap within a time scale - 
 * in this case it may be up to the client to apply a heuristic to resolve multiple periods into 
 * a single instance when attempting to choose a period for a time instant.
 * The default {@link au.gov.ga.earthsci.core.temporal.timescale.BasicTimePeriod} implementation
 * includes a pluggable filter mechanism that allows heuristics to be built into the
 * time scale itself - for example it might make sense to always choose the period that 
 * 'most contains' the given time instant.
 * 
 * <p/>
 * 
 * A common use of the {@link au.gov.ga.earthsci.core.temporal.timescale.ITimeScale} is to retrieve
 * a human-readable label for a given time instant <em>in the context of a particular time scale</em>.
 * For example - it might make sense to use a Gregorian Calendar time scale to get a date-based label
 * for a time instant, and a Human History time scale to get a label that represents the period
 * in human history in which that time instant falls.
 * 
 */
package au.gov.ga.earthsci.core.temporal.timescale;

