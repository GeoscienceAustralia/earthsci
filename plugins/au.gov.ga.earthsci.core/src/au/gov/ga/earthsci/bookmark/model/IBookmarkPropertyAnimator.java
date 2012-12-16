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
package au.gov.ga.earthsci.bookmark.model;

/**
 * An interface for animators able to animate between two {@link IBookmarkProperty}s of the
 * same type over a given time period.
 * <p/>
 * To use the animator, call {@link #init()} to initialise start- and end-times, and then 
 * make repeated calls to {@link #applyFrame()} as appropriate to apply the animated state.
 * <p/>
 * Calling {@link #dispose()} will stop the animator and perform any disposal required. An animator 
 * should not be called after {@link #dispose()} is called.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBookmarkPropertyAnimator
{
	/**
	 * Return the start state property of this animator
	 * 
	 * @return The start state of the animator
	 */
	IBookmarkProperty getStart();
	
	/**
	 * Return the end state property of this animator
	 * 
	 * @return The end state of the animator
	 */
	IBookmarkProperty getEnd();
	
	/**
	 * @return The duration (in milliseconds) this animator will run for
	 */
	long getDuration();
	
	/**
	 * Start this animator and initialise start and end times. 
	 */
	void init();
	
	/**
	 * @return Whether this animator has been initialised correctly
	 */
	boolean isInitialised();
	
	/**
	 * Apply the next frame of the animator, based on the current system time
	 */
	void applyFrame();
	
	/**
	 * Perform any required disposal of this animator
	 */
	void dispose();
	
}
