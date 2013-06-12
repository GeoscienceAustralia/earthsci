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
package au.gov.ga.earthsci.discovery;


/**
 * Provider of UI labels for {@link IDiscoveryResult}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IDiscoveryResultLabelProvider
{
	/**
	 * @return Number of lines to show for any results; must be constant
	 */
	int getLineCount();

	/**
	 * Title for the {@link IDiscoveryResult}. Shown on one line in bold in the
	 * UI.
	 * 
	 * @param result
	 *            Result to get the title for
	 * @return Result's title
	 */
	String getTitle(IDiscoveryResult result);

	/**
	 * Description for the {@link IDiscoveryResult}. Shown over multiple lines
	 * in the UI.
	 * 
	 * @param result
	 *            Result to get the description for
	 * @return Result's description
	 */
	String getDescription(IDiscoveryResult result);

	/**
	 * Tool tip for the {@link IDiscoveryResult}. String should be returned as
	 * HTML content; it will be inserted between opening and closing
	 * &lt;body&gt; tags.
	 * 
	 * @param result
	 *            Result to get the tool tip for
	 * @return Result's tool tip
	 */
	String getToolTip(IDiscoveryResult result);
}
