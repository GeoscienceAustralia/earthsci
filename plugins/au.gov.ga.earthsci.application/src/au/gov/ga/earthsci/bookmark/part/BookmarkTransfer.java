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
package au.gov.ga.earthsci.bookmark.part;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.bookmark.model.IBookmark;

/**
 * A transfer object for transferring {@link IBookmark} objects
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarkTransfer extends ByteArrayTransfer
{

	private static final Logger logger = LoggerFactory.getLogger(BookmarkTransfer.class);

	private static final String TYPE_NAME = "layer-node-transfer-format"; //$NON-NLS-1$
	private static final int TYPE_ID = registerType(TYPE_NAME);

	private static final BookmarkTransfer INSTANCE;
	static
	{
		INSTANCE = new BookmarkTransfer();
	}

	public static BookmarkTransfer getInstance()
	{
		return INSTANCE;
	}

	private BookmarkTransfer()
	{
	}

	@Override
	protected int[] getTypeIds()
	{
		return new int[] { TYPE_ID };
	}

	@Override
	protected String[] getTypeNames()
	{
		return new String[] { TYPE_NAME };
	}

	@Override
	protected void javaToNative(Object object, TransferData transferData)
	{
		byte[] bytes = toByteArray((BookmarkTransferData) object);
		if (bytes != null)
		{
			super.javaToNative(bytes, transferData);
		}
	}

	@Override
	protected Object nativeToJava(TransferData transferData)
	{
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		return fromByteArray(bytes);
	}

	protected BookmarkTransferData fromByteArray(byte[] bytes)
	{
		if (bytes == null)
		{
			return null;
		}

		InputStream is = new ByteArrayInputStream(bytes);
		try
		{
			return BookmarkTransferData.load(is);
		}
		catch (Exception e)
		{
			return null;
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	protected byte[] toByteArray(BookmarkTransferData data)
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try
		{
			BookmarkTransferData.save(data, os);
			return os.toByteArray();
		}
		catch (Exception e)
		{
			logger.error("Exception while exporting bookmark transfer data", e); //$NON-NLS-1$
			return null;
		}
		finally
		{
			try
			{
				os.close();
			}
			catch (IOException e)
			{
			}
		}
	}

}
