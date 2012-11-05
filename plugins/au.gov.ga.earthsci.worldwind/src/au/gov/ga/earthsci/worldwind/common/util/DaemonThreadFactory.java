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
package au.gov.ga.earthsci.worldwind.common.util;

import java.util.concurrent.ThreadFactory;

/**
 * A {@link ThreadFactory} that yields low-priority daemon threads.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DaemonThreadFactory implements ThreadFactory
{
	private String threadName = "Daemon Thread";

	public DaemonThreadFactory()
	{
	}

	public DaemonThreadFactory(String threadName)
	{
		this.threadName = threadName;
	}

	@Override
	public Thread newThread(Runnable r)
	{
		return newThread(r, threadName);
	}

	public static Thread newThread(Runnable r, String threadName)
	{
		Thread thread = new Thread(r);
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setName(threadName + "-" + thread.getId());
		return thread;
	}
}
