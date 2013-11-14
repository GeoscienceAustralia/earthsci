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

/**
 * Type of an {@link ElementProperty}, or the type of the elements within a
 * {@link ListProperty}.
 * <p/>
 * A new instance of this type is created when an {@link ElementProperty}
 * binding is asked for a new object, or a {@link ListProperty} binding is asked
 * to insert a new object, and there is no @{@link Factory} defined on the
 * property.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.TYPE })
public @interface InstanceElementType
{
	Class<?> value();
}
