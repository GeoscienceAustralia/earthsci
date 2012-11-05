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
package au.gov.ga.earthsci.worldwind.common.layers.delegate;

import gov.nasa.worldwind.avlist.AVList;

import org.w3c.dom.Element;

/**
 * Factory that creates instances of delegates from string definitions. Also
 * handles inserting class replacements for certain delegate classes.
 * <p>
 * All DelegateFactory instances should be implemented as singletons. This
 * function returns the singleton instance of this DelegateFactory.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IDelegateFactory
{
	/**
	 * Register a delegate class.
	 * 
	 * @param delegateClass
	 *            Class to register
	 */
	void registerDelegate(Class<? extends IDelegate> delegateClass);

	/**
	 * Register a delegate class which is to be replaced by another delegate in
	 * the createDelegate() method. The replacement class should be able to be
	 * instanciated by the same string definition as the class it is replacing.
	 * 
	 * @param fromClass
	 *            Class to be replaced
	 * @param toClass
	 *            Class to replace fromClass with
	 */
	void registerReplacementClass(Class<? extends IDelegate> fromClass, Class<? extends IDelegate> toClass);

	/**
	 * Create a new delgate from a string definition.
	 * 
	 * @param definition
	 * @return New delegate corresponding to {@code definition}
	 */
	IDelegate createDelegate(String definition, Element layerElement, AVList params);
}
