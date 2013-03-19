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
package au.gov.ga.earthsci.intent.locator;

import java.net.URL;

import au.gov.ga.earthsci.intent.Intent;

/**
 * Defines a locator which can translate an {@link Intent}'s URI to a URL from
 * which the resource can be retrieved.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IIntentResourceLocator
{
	/**
	 * If possible, convert the given intent's URI to a URL from which the
	 * resource can be retrieved. If this locator doesn't know how to convert
	 * the Intent's URI, it should return null.
	 * 
	 * @param intent
	 *            Intent whose URI to locate
	 * @return URL of the Intent's URI, or null if the URI isn't recognized
	 */
	URL locate(Intent intent);
}
