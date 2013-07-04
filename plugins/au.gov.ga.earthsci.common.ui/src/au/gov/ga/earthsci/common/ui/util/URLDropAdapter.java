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
package au.gov.ga.earthsci.common.ui.util;

import java.io.File;
import java.net.MalformedURLException;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.URLTransfer;

/**
 * Copy of {@link au.gov.ga.earthsci.common.ui.util.p2.ui.dialogs.URLDropAdapter}
 * with slight modifications.
 */
public abstract class URLDropAdapter extends DropTargetAdapter
{
	private boolean convertFileToURL = false;

	protected URLDropAdapter(boolean convertFileToURL)
	{
		this.convertFileToURL = convertFileToURL;
	}

	@Override
	public void dragEnter(DropTargetEvent e)
	{
		if (!dropTargetIsValid(e))
		{
			e.detail = DND.DROP_NONE;
			return;
		}
		if (e.detail == DND.DROP_NONE)
		{
			e.detail = DND.DROP_LINK;
		}
	}

	@Override
	public void dragOperationChanged(DropTargetEvent e)
	{
		if (e.detail == DND.DROP_NONE)
		{
			e.detail = DND.DROP_LINK;
		}
	}

	@Override
	public void drop(DropTargetEvent event)
	{
		if (dropTargetIsValid(event))
		{
			String urlText = getURLText(event);
			if (urlText != null)
			{
				handleDrop(urlText, event);
				return;
			}
		}
		event.detail = DND.DROP_NONE;
	}

	private String getURLText(DropTargetEvent event)
	{
		if (URLTransfer.getInstance().isSupportedType(event.currentDataType))
		{
			return (String) URLTransfer.getInstance().nativeToJava(event.currentDataType);
		}
		if (convertFileToURL && FileTransfer.getInstance().isSupportedType(event.currentDataType))
		{
			String[] names = (String[]) FileTransfer.getInstance().nativeToJava(event.currentDataType);
			if (names != null && names.length == 1)
			{
				try
				{
					return new File(names[0]).toURI().toURL().toString();
				}
				catch (MalformedURLException e)
				{
					return names[0];
				}
			}
		}
		return null;
	}

	/**
	 * Determine whether the drop target is valid. Subclasses may override.
	 * 
	 * @param event
	 *            the drop target event
	 * @return <code>true</code> if drop should proceed, <code>false</code> if
	 *         it should not.
	 */
	protected boolean dropTargetIsValid(DropTargetEvent event)
	{
		if (URLTransfer.getInstance().isSupportedType(event.currentDataType) && dropTargetDataIsValid(event))
		{
			return true;
		}
		if (!convertFileToURL)
		{
			return false;
		}
		if (FileTransfer.getInstance().isSupportedType(event.currentDataType))
		{
			String[] names = (String[]) FileTransfer.getInstance().nativeToJava(event.currentDataType);
			return names != null && names.length == 1;
		}
		return false;
	}

	/**
	 * Determine whether the drop target data is valid. On some platforms this
	 * cannot be detected, in which which case we return true.
	 * 
	 * @param event
	 *            the drop target event
	 * @return <code>true</code> if data is valid, (or can not be determined),
	 *         <code>false</code> otherwise.
	 */
	protected boolean dropTargetDataIsValid(DropTargetEvent event)
	{
		if (Util.isWindows())
		{
			return URLTransfer.getInstance().nativeToJava(event.currentDataType) != null;
		}
		return true;
	}

	/**
	 * Handle the drop with the given text as the URL.
	 * 
	 * @param urlText
	 *            The url text specified by the drop. It is never
	 *            <code>null</code>.
	 * @param event
	 *            the originating drop target event.
	 */
	protected abstract void handleDrop(String urlText, DropTargetEvent event);
}
