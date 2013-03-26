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
import gov.nasa.worldwind.util.WWIO;

import java.io.IOException;
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

import au.gov.ga.earthsci.core.retrieve.IRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.IRetrieverMonitor;
import au.gov.ga.earthsci.core.retrieve.RetrievalProperties;
import au.gov.ga.earthsci.core.retrieve.RetrievalStatus;
import au.gov.ga.earthsci.core.retrieve.retriever.HttpRetriever;

/**
 * Unit tests for the {@link HttpRetriever} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class HTTPRetrieverTest
{

	private final HttpRetriever classUnderTest = new HttpRetriever();

	private IRetrieverMonitor monitor;

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

		monitor = mockContext.mock(IRetrieverMonitor.class);

		mockContext.checking(new Expectations()
		{
			{
				{
					// Monitor 
					allowing(monitor);
				}
			}
		});
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

	@Test(expected = NullPointerException.class)
	public void testRetrieveNullURL() throws Exception
	{
		URL url = null;

		mockContext.checking(new Expectations()
		{
			{
				{
					oneOf(monitor).updateStatus(RetrievalStatus.STARTED);
				}
			}
		});

		classUnderTest.retrieve(url, monitor, new RetrievalProperties(false, false), null);
	}

	@Test(expected = NullPointerException.class)
	public void testRetrieveFileURL() throws Exception
	{
		URL url = new URL("file://somefile.txt");

		mockContext.checking(new Expectations()
		{
			{
				{
					oneOf(monitor).updateStatus(RetrievalStatus.STARTED);
				}
			}
		});

		classUnderTest.retrieve(url, monitor, new RetrievalProperties(false, false), null);
	}

	@Test
	public void testRetrieveHttpURLWithSuccessKnownLength() throws Exception
	{
		Assume.assumeTrue(httpServerIsAvailable());

		final String expectedResult = "success!";

		setServerResponse("/success", 200, expectedResult, false);

		URL url = createHttpURL("/success");

		IRetrievalResult result =
				classUnderTest.retrieve(url, monitor, new RetrievalProperties(false, false), null).result;

		assertNotNull(result);
		assertNull(result.getError());
		String string = WWIO.readStreamToString(result.getData().getInputStream(), "UTF-8");
		assertEquals(expectedResult, string);
	}

	@Test(expected = IOException.class)
	public void testRetrieveHttpURLWithFail404() throws Exception
	{
		Assume.assumeTrue(httpServerIsAvailable());

		URL url = createHttpURL("/404");
		classUnderTest.retrieve(url, monitor, new RetrievalProperties(false, false), null);
	}

	@Test(expected = IOException.class)
	public void testRetrieveHttpURLWithFail403() throws Exception
	{
		Assume.assumeTrue(httpServerIsAvailable());

		String expectedResult = "success!";

		setServerResponse("/fail", 403, expectedResult, true);

		URL url = createHttpURL("/fail");
		classUnderTest.retrieve(url, monitor, new RetrievalProperties(false, false), null);
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
			ClassLoader classLoader = HttpRetriever.class.getClassLoader();
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
			serverInstance =
					serverClass.getMethod("create", InetSocketAddress.class, int.class).invoke(serverClass,
							serverAddress, 0);
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

	private static void setServerResponse(final String path, final int responseCode, final String response,
			final boolean unknownContentLength)
	{
		if (!httpServerIsAvailable())
		{
			return;
		}

		try
		{
			final Class<?> handlerClass = Class.forName("com.sun.net.httpserver.HttpHandler");
			final Class<?> exchangeClass = Class.forName("com.sun.net.httpserver.HttpExchange");

			Object handler =
					Proxy.newProxyInstance(HttpRetriever.class.getClassLoader(), new Class[] { handlerClass },
							new InvocationHandler()
							{
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
										long responseLength =
												unknownContentLength ? 0 : response == null ? -1
														: response.getBytes().length;
										exchangeClass.getMethod("sendResponseHeaders", int.class, long.class).invoke(
												httpExchange, responseCode, responseLength);

										// Write response
										OutputStream responseBody =
												(OutputStream) exchangeClass.getMethod("getResponseBody").invoke(
														httpExchange);
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
