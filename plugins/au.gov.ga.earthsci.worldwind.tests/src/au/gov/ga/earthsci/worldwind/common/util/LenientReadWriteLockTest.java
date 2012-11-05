package au.gov.ga.earthsci.worldwind.common.util;

import static org.junit.Assert.assertTrue;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link LenientReadWriteLock} 
 */
public class LenientReadWriteLockTest
{
	private LenientReadWriteLock classToBeTested;
	
	private List<Thread> threads = new ArrayList<Thread>();
	private ThreadFactory threadFactory = new ThreadFactory()
	{
		@Override
		public Thread newThread(Runnable r) 
		{
			Thread result= new Thread(r);
			result.setDaemon(true);
			threads.add(result);
			return result;
		};
	};
	
	@Before
	public void setup()
	{
		classToBeTested = new LenientReadWriteLock();
	}
	
	@After
	@SuppressWarnings("deprecation")
	public void teardown()
	{
		// Destroy all threads
		for (Thread thread :  threads)
		{
			thread.stop();
		}
		threads.clear();
	}
	
	@Test
	public void testMultipleReadsOk() throws Exception
	{
		Thread reader1 = threadFactory.newThread(new ReadeTask());
		Thread reader2 = threadFactory.newThread(new ReadeTask());
		
		reader1.start();
		Thread.sleep(100);
		reader2.start();
		Thread.sleep(100);
		
		assertTrue(reader1.getState() != State.WAITING);
		assertTrue(reader2.getState() != State.WAITING);
	}
	
	@Test
	public void testReadBlocksWriteSeparateThread() throws Exception
	{
		Thread reader = threadFactory.newThread(new ReadeTask());
		Thread writer = threadFactory.newThread(new WriteTask());
		
		reader.start();
		Thread.sleep(100);
		writer.start();
		Thread.sleep(100);
		
		assertTrue(reader.getState() != State.WAITING);
		assertTrue(writer.getState() == State.WAITING);
	}
	
	@Test
	public void testWriteBlocksReadSeparateThread() throws Exception
	{
		Thread reader = threadFactory.newThread(new ReadeTask());
		Thread writer = threadFactory.newThread(new WriteTask());
		
		writer.start();
		Thread.sleep(100);
		reader.start();
		Thread.sleep(100);
		
		assertTrue(reader.getState() == State.WAITING);
		assertTrue(writer.getState() != State.WAITING);
	}
	
	@Test
	public void testWriteBlocksWriteSeparateThread() throws Exception
	{
		Thread writer1 = threadFactory.newThread(new WriteTask());
		Thread writer2 = threadFactory.newThread(new WriteTask());
		
		writer2.start();
		Thread.sleep(100);
		writer1.start();
		Thread.sleep(100);
		
		assertTrue(writer1.getState() == State.WAITING);
		assertTrue(writer2.getState() != State.WAITING);
	}
	
	@Test
	public void testWriteAllowsWriteSameThread() throws Exception
	{
		Thread thread = threadFactory.newThread(new WriteWriteTask());
		
		thread.start();
		Thread.sleep(100);
		
		assertTrue(thread.getState() != State.WAITING);
	}
	
	@Test
	public void testWriteAllowsReadSameThread() throws Exception
	{
		Thread thread = threadFactory.newThread(new WriteReadTask());
		
		thread.start();
		Thread.sleep(100);
		
		assertTrue(thread.getState() != State.WAITING);
	}
	
	@Test
	public void testReadAllowsWriteSameThread() throws Exception
	{
		Thread thread = threadFactory.newThread(new ReadWriteTask());
		
		thread.start();
		Thread.sleep(100);
		
		assertTrue(thread.getState() != State.WAITING);
	}
	
	@Test
	public void testReadAllowsReadSameThread() throws Exception
	{
		Thread thread = threadFactory.newThread(new ReadReadTask());
		
		thread.start();
		Thread.sleep(100);
		
		assertTrue(thread.getState() != State.WAITING);
	}
	
	private class ReadeTask implements Runnable
	{
		@Override
		public void run()
		{
			classToBeTested.readLock().lock();
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				classToBeTested.readLock().unlock();
			}
		}
	}
	
	private class WriteTask implements Runnable
	{
		@Override
		public void run()
		{
			classToBeTested.writeLock().lock();
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				classToBeTested.writeLock().unlock();
			}
		}
	}
	
	private class WriteWriteTask implements Runnable
	{
		@Override
		public void run()
		{
			classToBeTested.writeLock().lock();
			classToBeTested.writeLock().lock();
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				classToBeTested.writeLock().unlock();
			}
		}
	}
	
	private class WriteReadTask implements Runnable
	{
		@Override
		public void run()
		{
			classToBeTested.writeLock().lock();
			classToBeTested.readLock().lock();
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				classToBeTested.writeLock().unlock();
			}
		}
	}
	
	private class ReadReadTask implements Runnable
	{
		@Override
		public void run()
		{
			classToBeTested.writeLock().lock();
			classToBeTested.readLock().lock();
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				classToBeTested.writeLock().unlock();
			}
		}
	}
	
	private class ReadWriteTask implements Runnable
	{
		@Override
		public void run()
		{
			classToBeTested.writeLock().lock();
			classToBeTested.readLock().lock();
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				classToBeTested.writeLock().unlock();
			}
		}
	}
		
}
