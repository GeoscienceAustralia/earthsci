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
package au.gov.ga.earthsci.common.ui.util;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * A listener that listens to new frame events on the
 * {@link LoadingIconAnimator}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILoadingIconFrameListener
{
	/**
	 * Called when the {@link LoadingIconAnimator} animates to the next frame.
	 * <p/>
	 * Note that this is called by a non-UI thread; UI updates should be
	 * executed on the UI thread using {@link Display#asyncExec(Runnable)}.
	 * 
	 * @param image
	 */
	void nextFrame(Image image);
}
