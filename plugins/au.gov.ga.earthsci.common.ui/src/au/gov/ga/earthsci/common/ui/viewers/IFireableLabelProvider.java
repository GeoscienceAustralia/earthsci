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
package au.gov.ga.earthsci.common.ui.viewers;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

/**
 * Represents a {@link ILabelProvider} that provides access to the
 * {@link #fireLabelProviderChanged(LabelProviderChangedEvent)} method of the
 * {@link BaseLabelProvider} class.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IFireableLabelProvider extends IBaseLabelProvider
{
	void fireLabelProviderChanged(LabelProviderChangedEvent event);
}
