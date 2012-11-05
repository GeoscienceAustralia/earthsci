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
package au.gov.ga.earthsci.worldwind.common.layers.curtain.delegate;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.AbstractDelegateFactory;

/**
 * Factory which creates delegates for the {@link DelegatorTiledCurtainLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CurtainDelegateFactory extends AbstractDelegateFactory
{
	private static CurtainDelegateFactory instance = new CurtainDelegateFactory();

	public static CurtainDelegateFactory get()
	{
		return instance;
	}

	private CurtainDelegateFactory()
	{
		super();

		//register the specific delegates applicable to this Image factory
		registerDelegate(CurtainURLRequesterDelegate.class);
		registerDelegate(CurtainLocalRequesterDelegate.class);
		registerDelegate(CurtainTextureTileFactoryDelegate.class);
	}
}
