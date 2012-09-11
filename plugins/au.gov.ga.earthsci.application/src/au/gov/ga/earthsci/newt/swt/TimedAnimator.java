package au.gov.ga.earthsci.newt.swt;

import javax.media.opengl.GLAutoDrawable;

import com.jogamp.opengl.util.Animator;

public class TimedAnimator extends Animator
{
	protected int period = 1000; //ms
	protected PeriodTimer timer;

	public TimedAnimator()
	{
		super();
	}

	public TimedAnimator(GLAutoDrawable drawable)
	{
		super(drawable);
	}

	public TimedAnimator(ThreadGroup tg, GLAutoDrawable drawable)
	{
		super(tg, drawable);
	}

	public TimedAnimator(ThreadGroup tg)
	{
		super(tg);
	}

	public int getPeriod()
	{
		return period;
	}

	public void setPeriod(int period)
	{
		this.period = period;
	}

	@Override
	public synchronized boolean start()
	{
		pauseAfterPeriod();
		return super.start();
	}

	@Override
	public synchronized boolean resume()
	{
		//System.out.println("RESUMING");
		pauseAfterPeriod();
		return super.resume();
	}

	@Override
	public synchronized boolean pause()
	{
		//System.out.println("PAUSING");
		return super.pause();
	}
	
	@Override
	protected void display()
	{
		super.display();
		//System.out.println("DISPLAY!");
	}

	protected void pauseAfterPeriod()
	{
		if (timer == null)
		{
			timer = new PeriodTimer(period);
		}
		else
		{
			timer.pauseIn(period);
		}
	}

	protected class PeriodTimer implements Runnable
	{
		private final static long msToNs = 1000000l;
		protected Long pauseTime;

		public PeriodTimer(long delay)
		{
			pauseIn(delay);
			Thread thread = new Thread(threadGroup, this, "Animator pauser");
			thread.setDaemon(true);
			thread.start();
		}

		public void pauseIn(long delay)
		{
			synchronized (this)
			{
				long currentNanos = System.nanoTime();
				pauseTime = currentNanos + delay * msToNs;
				notifyAll();
			}
		}

		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					long currentNanos = System.nanoTime();
					long sleepTime;
					synchronized (this)
					{
						sleepTime = pauseTime == null ? -1 : pauseTime - currentNanos;
						sleepTime /= msToNs;
						if (sleepTime <= 0)
						{
							pause();
							wait();
						}
					}
					if (sleepTime > 0)
					{
						Thread.sleep(sleepTime);
					}
				}
				catch (InterruptedException e)
				{
				}
			}
		}
	}
}
