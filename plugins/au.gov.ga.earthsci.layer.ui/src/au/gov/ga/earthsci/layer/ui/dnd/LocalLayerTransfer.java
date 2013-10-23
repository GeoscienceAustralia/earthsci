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
package au.gov.ga.earthsci.layer.ui.dnd;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.layer.ILayerTreeNode;

/**
 * A {@link Transfer} implementation that allows for the transfer of
 * {@link ILayerTreeNode} instances without resorting to a serialise-deserialise
 * operation.
 * <p/>
 * This {@link Transfer} type is intended for use only within the layer
 * mechanism. For use between application, use the {@link LayerTransfer} version
 * instead.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class LocalLayerTransfer extends ByteArrayTransfer
{

	private static final Logger logger = LoggerFactory.getLogger(LocalLayerTransfer.class);

	private static LocalLayerTransfer instance = new LocalLayerTransfer();
	private static final String TYPE_NAME = "local-layer-node-transfer-format"; //$NON-NLS-1$
	private static final int TYPEID = registerType(TYPE_NAME);

	private static final Map<String, LayerTransferData> cachedTransfers =
			new ConcurrentHashMap<String, LayerTransferData>();

	/**
	 * Returns the singleton gadget transfer instance.
	 */
	public static LocalLayerTransfer getInstance()
	{
		return instance;
	}

	/**
	 * Avoid explicit instantiation
	 */
	private LocalLayerTransfer()
	{
	}


	@Override
	protected int[] getTypeIds()
	{
		return new int[] { TYPEID };
	}

	@Override
	protected String[] getTypeNames()
	{
		return new String[] { TYPE_NAME };
	}

	@Override
	protected void javaToNative(Object object, TransferData transferData)
	{
		byte[] bytes = store((LayerTransferData) object).getBytes();
		if (bytes != null)
		{
			super.javaToNative(bytes, transferData);
		}
	}

	@Override
	protected Object nativeToJava(TransferData transferData)
	{
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		return get(new String(bytes));
	}

	private String store(LayerTransferData data)
	{
		UUID key = UUID.randomUUID();
		cachedTransfers.put(key.toString(), data);
		return key.toString();
	}

	@SuppressWarnings("nls")
	private LayerTransferData get(String key)
	{
		if (logger.isTraceEnabled())
		{
			logger.trace("Looking up transfer for key {}", key);
			logger.trace(cachedTransfers.containsKey(key) ? "Found transfer for key " + key : "No transfer found");
		}
		return cachedTransfers.remove(key);
	}

	@Override
	protected boolean validate(Object object)
	{
		return object instanceof LayerTransferData &&
				((LayerTransferData) object).getLayers() != null &&
				((LayerTransferData) object).getLayers().length > 0;
	}
}
