/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package au.gov.ga.earthsci.newt.swt;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.SceneController;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.WorldWindowImpl;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.event.InputHandler;
import gov.nasa.worldwind.event.NoOpInputHandler;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.PerformanceStatistic;

import java.awt.GraphicsDevice;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesChooser;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import au.gov.ga.earthsci.newt.awt.WorldWindowNewtDrawableAWT;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.swt.NewtCanvasSWT;

/**
 * {@link WorldWindow} implementation that uses a {@link NewtCanvasAWT} for
 * rendering. To use this class, the {@link AVKey#WORLD_WINDOW_CLASS_NAME}
 * configuration property must be the name of a class that implements the
 * {@link WorldWindowNewtDrawableAWT} interface.
 * <p/>
 * Most of this implementation is copied from the WorldWindowGLCanvas source.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class WorldWindowNewtCanvasSWT extends NewtCanvasSWT implements WorldWindow, PropertyChangeListener
{
	/**
	 * Returns a {@link GLCapabilities} identifying default graphics features to
	 * request. The capabilities instance returned requests a frame buffer with
	 * 8 bits each of red, green, blue and alpha, a 24-bit depth buffer, double
	 * buffering, and if the Java property "gov.nasa.worldwind.stereo.mode" is
	 * set to "device", device-supported stereo.
	 * 
	 * @return a new capabilities instance identifying desired graphics
	 *         features.
	 */
	protected static GLCapabilities getCaps()
	{
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));

		caps.setAlphaBits(8);
		caps.setRedBits(8);
		caps.setGreenBits(8);
		caps.setBlueBits(8);
		caps.setDepthBits(24);
		caps.setDoubleBuffered(true);

		// Determine whether we should request a stereo canvas
		String stereo = System.getProperty(AVKey.STEREO_MODE);
		if ("device".equals(stereo))
			caps.setStereo(true);

		return caps;
	}

	/** The drawable to which {@link WorldWindow} methods are delegated. */
	protected final WorldWindowNewtDrawableSWT wwd; // WorldWindow interface delegates to wwd
	protected final GLWindow window;
	//protected final Redrawer redrawer = new Redrawer();
	protected final TimedAnimator animator;

	/**
	 * Constructs a new <code>WorldWindowGLCanvas</code> on the default graphics
	 * device and shares graphics resources with another
	 * <code>WorldWindow</code>.
	 * 
	 * @param shareWith
	 *            a <code>WorldWindow</code> with which to share graphics
	 *            resources. May be null, in which case resources are not
	 *            shared.
	 * 
	 * @see GLCanvas#GLCanvas(GLCapabilities, GLCapabilitiesChooser, GLContext,
	 *      GraphicsDevice)
	 */
	public WorldWindowNewtCanvasSWT(Composite parent, int style, WorldWindow shareWith)
	{
		super(parent, style, GLWindow.create(getCaps()));
		this.window = (GLWindow) getNEWTChild();
		if (shareWith != null)
		{
			this.window.setSharedContext(shareWith.getContext());
		}
		this.animator = new TimedAnimator(window);
		this.animator.start();
		addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				animator.stop();
			}
		});

		try
		{
			this.wwd =
					((WorldWindowNewtDrawableSWT) WorldWind.createConfigurationComponent(AVKey.WORLD_WINDOW_CLASS_NAME));
			this.wwd.initDrawable(window, this);
			if (shareWith != null)
				this.wwd.initGpuResourceCache(shareWith.getGpuResourceCache());
			else
				this.wwd.initGpuResourceCache(WorldWindowImpl.createGpuResourceCache());
			this.createView();
			this.createDefaultInputHandler();
			WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
			this.wwd.endInitialization();
		}
		catch (Exception e)
		{
			String message = Logging.getMessage("Awt.WorldWindowGLSurface.UnabletoCreateWindow");
			Logging.logger().severe(message);
			throw new WWRuntimeException(message, e);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		//noinspection StringEquality
		if (evt.getPropertyName() == WorldWind.SHUTDOWN_EVENT)
			this.shutdown();
	}

	@Override
	public void shutdown()
	{
		WorldWind.removePropertyChangeListener(WorldWind.SHUTDOWN_EVENT, this);
		this.wwd.shutdown();
	}

	/**
	 * Constructs and attaches the {@link View} for this
	 * <code>WorldWindow</code>.
	 */
	protected void createView()
	{
		this.setView((View) WorldWind.createConfigurationComponent(AVKey.VIEW_CLASS_NAME));
	}

	/**
	 * Constructs and attaches the {@link InputHandler} for this
	 * <code>WorldWindow</code>.
	 */
	protected void createDefaultInputHandler()
	{
		this.setInputHandler((InputHandler) WorldWind.createConfigurationComponent(AVKey.INPUT_HANDLER_CLASS_NAME));
	}

	@Override
	public InputHandler getInputHandler()
	{
		return this.wwd.getInputHandler();
	}

	@Override
	public void setInputHandler(InputHandler inputHandler)
	{
		if (this.wwd.getInputHandler() != null)
			this.wwd.getInputHandler().setEventSource(null); // remove this window as a source of events

		this.wwd.setInputHandler(inputHandler != null ? inputHandler : new NoOpInputHandler());
		if (inputHandler != null)
			inputHandler.setEventSource(this);
	}

	@Override
	public SceneController getSceneController()
	{
		return this.wwd.getSceneController();
	}

	@Override
	public void setSceneController(SceneController sceneController)
	{
		this.wwd.setSceneController(sceneController);
	}

	@Override
	public GpuResourceCache getGpuResourceCache()
	{
		return this.wwd.getGpuResourceCache();
	}

	@Override
	public void redraw()
	{
		animator.resume();
	}

	@Override
	public void redrawNow()
	{
		this.wwd.redrawNow();
	}

	@Override
	public void setModel(Model model)
	{
		// null models are permissible
		this.wwd.setModel(model);
	}

	@Override
	public Model getModel()
	{
		return this.wwd.getModel();
	}

	@Override
	public void setView(View view)
	{
		// null views are permissible
		if (view != null)
			this.wwd.setView(view);
	}

	@Override
	public View getView()
	{
		return this.wwd.getView();
	}

	@Override
	public void setModelAndView(Model model, View view)
	{ // null models/views are permissible
		this.setModel(model);
		this.setView(view);
	}

	@Override
	public void addRenderingListener(RenderingListener listener)
	{
		this.wwd.addRenderingListener(listener);
	}

	@Override
	public void removeRenderingListener(RenderingListener listener)
	{
		this.wwd.removeRenderingListener(listener);
	}

	@Override
	public void addSelectListener(SelectListener listener)
	{
		this.wwd.getInputHandler().addSelectListener(listener);
		this.wwd.addSelectListener(listener);
	}

	@Override
	public void removeSelectListener(SelectListener listener)
	{
		this.wwd.getInputHandler().removeSelectListener(listener);
		this.wwd.removeSelectListener(listener);
	}

	@Override
	public void addPositionListener(PositionListener listener)
	{
		this.wwd.addPositionListener(listener);
	}

	@Override
	public void removePositionListener(PositionListener listener)
	{
		this.wwd.removePositionListener(listener);
	}

	@Override
	public void addRenderingExceptionListener(RenderingExceptionListener listener)
	{
		this.wwd.addRenderingExceptionListener(listener);
	}

	@Override
	public void removeRenderingExceptionListener(RenderingExceptionListener listener)
	{
		this.wwd.removeRenderingExceptionListener(listener);
	}

	@Override
	public Position getCurrentPosition()
	{
		return this.wwd.getCurrentPosition();
	}

	@Override
	public PickedObjectList getObjectsAtCurrentPosition()
	{
		return this.wwd.getSceneController() != null ? this.wwd.getSceneController().getPickedObjectList() : null;
	}

	@Override
	public PickedObjectList getObjectsInSelectionBox()
	{
		return this.wwd.getSceneController() != null ? this.wwd.getSceneController().getObjectsInPickRectangle() : null;
	}

	@Override
	public Object setValue(String key, Object value)
	{
		return this.wwd.setValue(key, value);
	}

	@Override
	public AVList setValues(AVList avList)
	{
		return this.wwd.setValues(avList);
	}

	@Override
	public Object getValue(String key)
	{
		return this.wwd.getValue(key);
	}

	@Override
	public Collection<Object> getValues()
	{
		return this.wwd.getValues();
	}

	@Override
	public Set<Map.Entry<String, Object>> getEntries()
	{
		return this.wwd.getEntries();
	}

	@Override
	public String getStringValue(String key)
	{
		return this.wwd.getStringValue(key);
	}

	@Override
	public boolean hasKey(String key)
	{
		return this.wwd.hasKey(key);
	}

	@Override
	public Object removeKey(String key)
	{
		return this.wwd.removeKey(key);
	}

	@Override
	public synchronized void addPropertyChangeListener(PropertyChangeListener listener)
	{
		this.wwd.addPropertyChangeListener(listener);
	}

	@Override
	public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		this.wwd.addPropertyChangeListener(propertyName, listener);
	}

	@Override
	public synchronized void removePropertyChangeListener(PropertyChangeListener listener)
	{
		this.wwd.removePropertyChangeListener(listener);
	}

	@Override
	public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		this.wwd.removePropertyChangeListener(listener);
	}

	@Override
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		this.wwd.firePropertyChange(propertyName, oldValue, newValue);
	}

	@Override
	public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
	{
		this.wwd.firePropertyChange(propertyChangeEvent);
	}

	@Override
	public AVList copy()
	{
		return this.wwd.copy();
	}

	@Override
	public AVList clearList()
	{
		return this.wwd.clearList();
	}

	@Override
	public void setPerFrameStatisticsKeys(Set<String> keys)
	{
		this.wwd.setPerFrameStatisticsKeys(keys);
	}

	@Override
	public Collection<PerformanceStatistic> getPerFrameStatistics()
	{
		return this.wwd.getPerFrameStatistics();
	}

	@Override
	public GLContext getContext()
	{
		return window.getContext();
	}

	public GLWindow getWindow()
	{
		return window;
	}
}
