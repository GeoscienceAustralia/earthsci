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
package au.gov.ga.earthsci.core.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

import au.gov.ga.earthsci.common.util.StringInstantiable;

/**
 * Annotation that marks a field or method as persistable. Getter methods with
 * corresponding setters can be annotated.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Inherited
public @interface Persistent
{
	/**
	 * Name to use for the XML element (or attribute) when persisting this
	 * value.
	 */
	String name() default "";

	/**
	 * True if this value should be saved as an XML attribute on the parent
	 * element instead of a child element. Only supported for
	 * {@link StringInstantiable} values/objects.
	 */
	boolean attribute() default false;

	/**
	 * Name used for the XML element for individual array elements (if this
	 * value is an array or {@link Collection} type).
	 */
	String elementName() default "";

	/**
	 * Name of the corresponding setter for the annotated getter method. This is
	 * used if the setter method name doesn't follow the standard Java Bean
	 * getter/setter naming conventions. Not used for annotated fields.
	 */
	String setter() default "";
}
