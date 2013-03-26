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
package au.gov.ga.earthsci.intent;

import org.eclipse.core.runtime.content.IContentType;

/**
 * Contains helper methods for dealing with {@link IContentType}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ContentTypeHelper
{
	/**
	 * Search the list of content types for the closest type that is a kind of
	 * the given content type. Closest means the smallest distance between the
	 * given content type and a content type in it's ancestry.
	 * 
	 * @see #ancestryDistance(IContentType, IContentType)
	 * 
	 * @param contentType
	 *            Content type who's ancestry to search for a content type in
	 *            the collection
	 * @param contentTypes
	 *            Collection of content types to search
	 * @return Closest content type from <code>contentTypes</code> that the
	 *         <code>contentType</code> is a kind of
	 */
	public static IContentType closestMatching(IContentType contentType, Iterable<IContentType> contentTypes)
	{
		if (contentType == null || contentTypes == null)
		{
			return null;
		}

		int minDistance = Integer.MAX_VALUE;
		IContentType closest = null;
		for (IContentType test : contentTypes)
		{
			int distance = ancestryDistance(contentType, test);
			if (distance >= 0 && distance < minDistance)
			{
				minDistance = distance;
				closest = test;
			}
		}
		return closest;
	}

	/**
	 * Search the list of content types for the closest type that is a kind of
	 * the given content type, and return the ancestry distance to that type.
	 * Closest means the smallest distance between the given content type and a
	 * content type in it's ancestry.
	 * <p/>
	 * Returns -1 if no matching content type could be found.
	 * 
	 * @see #ancestryDistance(IContentType, IContentType)
	 * 
	 * @param contentType
	 *            Content type who's ancestry to search for a content type in
	 *            the collection
	 * @param contentTypes
	 *            Collection of content types to search
	 * @return Ancestry distance to the closest content type from
	 *         <code>contentTypes</code> that the <code>contentType</code> is a
	 *         kind of, or -1 if no matching content type could be found
	 */
	public static int distanceToClosestMatching(IContentType contentType, Iterable<IContentType> contentTypes)
	{
		if (contentType == null || contentTypes == null)
		{
			return -1;
		}

		int minDistance = Integer.MAX_VALUE;
		for (IContentType test : contentTypes)
		{
			int distance = ancestryDistance(contentType, test);
			if (distance >= 0 && distance < minDistance)
			{
				minDistance = distance;
			}
		}
		return minDistance == Integer.MAX_VALUE ? -1 : minDistance;
	}

	/**
	 * Calculate the distance between the child content type and the parent,
	 * searching the child's ancestry.
	 * <p/>
	 * For example:
	 * <ul>
	 * <li>Returns 0 if child == parent</li>
	 * <li>Returns 1 if child's base type == parent</li>
	 * <li>etc...</li>
	 * </ul>
	 * 
	 * Returns -1 if the parent doesn't exist in the child's ancestry.
	 * 
	 * @param child
	 * @param parent
	 * @return The distance between the child and the parent in the child's
	 *         ancestry, or -1 if the parent could not be found
	 */
	public static int ancestryDistance(IContentType child, IContentType parent)
	{
		return ancestryDistance(child, parent, 0);
	}

	private static int ancestryDistance(IContentType child, IContentType parent, int position)
	{
		if (child == null || parent == null)
		{
			return -1;
		}
		if (child.equals(parent))
		{
			return position;
		}
		return ancestryDistance(child.getBaseType(), parent, position++);
	}
}
