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
package au.gov.ga.earthsci.retrieve.retriever;

import static org.junit.Assert.*;

import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.ga.earthsci.core.retrieve.IRetrievalMonitor;
import au.gov.ga.earthsci.core.retrieve.IRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.retriever.HTTPRetriever;

/**
 * Unit tests for the {@link HTTPRetriever} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class HTTPRetrieverTest
{

	private final HTTPRetriever classUnderTest = new HTTPRetriever();
	
	private IRetrievalMonitor monitor;
	
	private Mockery mockContext;
	
	@BeforeClass
	public static void initialise()
	{
		createHttpServer();
		startServer();
	}
	
	@AfterClass
	public static void destroy()
	{
		stopServer();
	}
	
	@Before
	public void setup()
	{
		mockContext = new Mockery();
		
		monitor = mockContext.mock(IRetrievalMonitor.class);
	}
	
	@Test
	public void testSupportsNullURL() throws Exception
	{
		URL url = null;
		assertFalse(classUnderTest.supports(url));
	}
	
	@Test
	public void testSupportsFileURL() throws Exception
	{
		URL url = new URL("file://somefile.txt");
		assertFalse(classUnderTest.supports(url));
	}
	
	@Test
	public void testSupportsHttpURL() throws Exception
	{
		URL url = new URL("http://somewhere.com/somefile.txt");
		assertTrue(classUnderTest.supports(url));
	}
	
	@Test
	public void testSupportsHttpsURL() throws Exception
	{
		URL url = new URL("https://somewhere.com/somefile.txt");
		assertTrue(classUnderTest.supports(url));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRetrieveNullURL() throws Exception
	{
		URL url = null;
		classUnderTest.retrieve(url, monitor);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRetrieveFileURL() throws Exception
	{
		URL url = new URL("file://somefile.txt");
		classUnderTest.retrieve(url, monitor);
	}
	
	@Test
	public void testRetrieveHttpURLWithSuccessKnownLength() throws Exception
	{
		Assume.assumeTrue(httpServerIsAvailable());
		
		String expectedResult = "success!";
		
		setServerResponse("/success", 200, expectedResult, false);

		mockContext.checking(new Expectations() {{{
			oneOf(monitor).notifyStarted();
			oneOf(monitor).notifyConnecting();
			oneOf(monitor).notifyConnected();
			oneOf(monitor).notifyReading();
			oneOf(monitor).notifyCompleted(with(true));
		}}});
		
		
		URL url = createHttpURL("/success");
		
		IRetrievalResult result = classUnderTest.retrieve(url, monitor);
		
		assertNotNull(result);
		assertTrue(result.isSuccessful());
		assertNull(result.getException());
		assertNull(result.getMessage());
		assertEquals(expectedResult, result.getAsString());
	}
	
	@Test
	public void testRetrieveHttpURLWithSuccessUnknownLength() throws Exception
	{
		Assume.assumeTrue(httpServerIsAvailable());
		
		String expectedResult = "success!";
		
		setServerResponse("/success", 200, expectedResult, true);

		mockContext.checking(new Expectations() {{{
			oneOf(monitor).notifyStarted();
			oneOf(monitor).notifyConnecting();
			oneOf(monitor).notifyConnected();
			oneOf(monitor).notifyReading();
			oneOf(monitor).notifyCompleted(with(true));
		}}});
		
		
		URL url = createHttpURL("/success");
		
		IRetrievalResult result = classUnderTest.retrieve(url, monitor);
		
		assertNotNull(result);
		assertTrue(result.isSuccessful());
		assertNull(result.getException());
		assertNull(result.getMessage());
		assertEquals(expectedResult, result.getAsString());
	}
	
	@Test
	public void testRetrieveHttpURLWithFail404() throws Exception
	{
		Assume.assumeTrue(httpServerIsAvailable());
		
		mockContext.checking(new Expectations() {{{
			oneOf(monitor).notifyStarted();
			oneOf(monitor).notifyConnecting();
			never(monitor).notifyConnected();
			never(monitor).notifyReading();
			oneOf(monitor).notifyCompleted(with(false));
		}}});
		
		URL url = createHttpURL("/404");
		
		IRetrievalResult result = classUnderTest.retrieve(url, monitor);
		
		assertNotNull(result);
		assertFalse(result.isSuccessful());
		assertNotNull(result.getException());
		assertNotNull(result.getMessage());
		assertEquals(null, result.getAsString());
	}
	
	@Test
	public void testRetrieveHttpURLWithFail403() throws Exception
	{
		Assume.assumeTrue(httpServerIsAvailable());
		
		String expectedResult = "success!";
		
		setServerResponse("/fail", 403, expectedResult, true);

		mockContext.checking(new Expectations() {{{
			oneOf(monitor).notifyStarted();
			oneOf(monitor).notifyConnecting();
			never(monitor).notifyConnected();
			never(monitor).notifyReading();
			oneOf(monitor).notifyCompleted(with(false));
		}}});
		
		URL url = createHttpURL("/fail");
		
		IRetrievalResult result = classUnderTest.retrieve(url, monitor);
		
		assertNotNull(result);
		assertFalse(result.isSuccessful());
		assertNotNull(result.getException());
		assertNotNull(result.getMessage());
		assertEquals(null, result.getAsString());
	}
	
	// TODO: Move this code somewhere more reusable
	
	private static InetSocketAddress serverAddress;
	private static Class<?> serverClass;
	private static Object serverInstance;
	
	private static URL createHttpURL(String relativePath) throws Exception
	{
		URL url = new URL("http://" + serverAddress.getHostName() + ":" + serverAddress.getPort() + relativePath);
		return url;
	}
	
	/**
	 * Check that the Sun JVM HttpServer class is present. Used to filter tests
	 * that depend on this class.
	 */
	private static boolean httpServerIsAvailable()
	{
		try
		{
			ClassLoader classLoader = HTTPRetriever.class.getClassLoader();
			Class.forName("com.sun.net.httpserver.HttpServer", false, classLoader);
			Class.forName("com.sun.net.httpserver.HttpHandler", false, classLoader);
			Class.forName("com.sun.net.httpserver.HttpExchange", false, classLoader);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	private static void createHttpServer()
	{
		if (!httpServerIsAvailable())
		{
			return;
		}
		
		try
		{
			serverAddress = new InetSocketAddress("localhost", chooseAvailablePort());
			serverClass = Class.forName("com.sun.net.httpserver.HttpServer");
			serverInstance = serverClass.getMethod("create", InetSocketAddress.class, int.class).invoke(serverClass, serverAddress, 0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static int chooseAvailablePort()
	{
		for (int port = 8796; port < 9000; port++)
		{
			try
			{
				ServerSocket ss = new ServerSocket(port);
				ss.setReuseAddress(true);
				ss.close();
				return port;
			}
			catch (Exception e)
			{
				
			}
		}
		return -1;
	}
	
	private static void startServer()
	{
		try
		{
			serverClass.getMethod("start").invoke(serverInstance);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void stopServer()
	{
		try
		{
			serverClass.getMethod("stop", int.class).invoke(serverInstance, 0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void setServerResponse(final String path, final int responseCode, final String response, final boolean unknownContentLength)
	{
		if (!httpServerIsAvailable())
		{
			return;
		}
		
		try
		{
			final Class<?> handlerClass = Class.forName("com.sun.net.httpserver.HttpHandler");
			final Class<?> exchangeClass = Class.forName("com.sun.net.httpserver.HttpExchange");
			
			Object handler = Proxy.newProxyInstance(HTTPRetriever.class.getClassLoader(), new Class[] {handlerClass}, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
				{
					try
					{
						if (!method.getName().equalsIgnoreCase("handle"))
						{
							return null;
						}
						
						Object httpExchange = args[0];
						
						// Send headers
						long responseLength = unknownContentLength ? 0 : response == null ? -1 : response.getBytes().length;
						exchangeClass.getMethod("sendResponseHeaders", int.class, long.class).invoke(httpExchange, responseCode, responseLength);
						
						// Write response
						OutputStream responseBody = (OutputStream)exchangeClass.getMethod("getResponseBody").invoke(httpExchange);
						responseBody.write(response.getBytes());
						
						exchangeClass.getMethod("close").invoke(httpExchange);
					}
					catch (Exception e)
					{
						// Should never get here
						e.printStackTrace();
					}
					
					return null;
				}
				
			});
			
			serverClass.getMethod("createContext", String.class, handlerClass).invoke(serverInstance, path, handler);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
