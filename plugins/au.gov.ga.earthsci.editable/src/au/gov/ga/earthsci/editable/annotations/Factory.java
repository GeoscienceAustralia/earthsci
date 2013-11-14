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
package au.gov.ga.earthsci.editable.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ListProperty;

import au.gov.ga.earthsci.editable.IFactory;

/**
 * {@link IFactory} implementation to use when creating a new instance of a
 * {@link ModelProperty}'s element (for {@link ElementProperty}s or elements
 * within {@link ListProperty}s).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.TYPE })
public @interface Factory
{
	Class<? extends IFactory<?>> value();
}
