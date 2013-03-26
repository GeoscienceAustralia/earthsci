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
package au.gov.ga.earthsci.bookmark.part.editor;

import java.util.ArrayList;
import java.util.List;

import au.gov.ga.earthsci.common.collection.ArrayListTreeMap;


/**
 * An abstract base class for {@link IBookmarkEditor} implementations.
 * <p/>
 * Provides convenience implementations of some methods, as well as a
 * {@link #validate(String, boolean, IBookmarkEditorMessage)} method that
 * provides a convenient way to manage validation notifications etc. as fields
 * change.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public abstract class AbstractBookmarkEditor implements IBookmarkEditor
{
	private List<IBookmarkEditorListener> listeners = new ArrayList<IBookmarkEditorListener>();

	private ArrayListTreeMap<String, IBookmarkEditorMessage> validationMessages =
			new ArrayListTreeMap<String, IBookmarkEditorMessage>();

	/**
	 * Validate the given field using the provided validation expression
	 * 
	 * @param fieldId
	 *            The ID of the field to validate. Validation messages will be
	 *            ordered by field ID.
	 * @param validationRule
	 *            The boolean expression that determine's the fields validity
	 * @param message
	 *            The message to emit in the case of an invalid field
	 */
	protected synchronized void validate(String fieldId, boolean validationRule, IBookmarkEditorMessage message)
	{
		if (validationRule)
		{
			markValid(fieldId, message);
		}
		else
		{
			markInvalid(fieldId, message);
		}
	}

	private void markValid(String fieldId, IBookmarkEditorMessage message)
	{
		if (isValid())
		{
			return;
		}

		boolean changed = validationMessages.removeSingle(fieldId, message);
		if (!changed)
		{
			return;
		}

		if (validationMessages.count(fieldId) == 0)
		{
			validationMessages.remove(fieldId);
		}

		fireValidationEvent(isValid());
	}

	private void markInvalid(String fieldId, IBookmarkEditorMessage message)
	{
		if (validationMessages.containsKey(fieldId) && validationMessages.get(fieldId).contains(message))
		{
			return;
		}

		validationMessages.putSingle(fieldId, message);
		fireInvalidEvent();
	}

	@Override
	public boolean isValid()
	{
		return validationMessages.isEmpty();
	}

	@Override
	public IBookmarkEditorMessage[] getMessages()
	{
		return validationMessages.flatValues().toArray(new IBookmarkEditorMessage[0]);
	}

	private synchronized void fireValidationEvent(boolean valid)
	{
		if (valid)
		{
			fireValidEvent();
		}
		else
		{
			fireInvalidEvent();
		}
	}

	/**
	 * Fire an event to all listeners notifying them that the editor is in an
	 * invalid state
	 */
	private synchronized void fireInvalidEvent()
	{
		IBookmarkEditorMessage[] messages = getMessages();
		for (IBookmarkEditorListener l : listeners)
		{
			l.editorInvalid(this, messages);
		}
	}

	/**
	 * Fire an event to all listeners notifying them that the editor is in a
	 * valid state
	 */
	private synchronized void fireValidEvent()
	{
		for (IBookmarkEditorListener l : listeners)
		{
			l.editorValid(this);
		}
	}

	@Override
	public void addListener(IBookmarkEditorListener listener)
	{
		if (listener == null)
		{
			return;
		}
		listeners.add(listener);
	}

	@Override
	public void removeListener(IBookmarkEditorListener listener)
	{
		if (listener == null)
		{
			return;
		}
		listeners.remove(listener);
	}

	@Override
	public void cancelPressed()
	{
		// Subclasses may override to perform cleanup as necessary
	}


}
