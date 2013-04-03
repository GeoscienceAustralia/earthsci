/**
 * Provides a centralised mechanism for retrieving resources identified by {@link java.net.URL}s.
 * <p/>
 * The retrieval API provides a number of components used together to easily retrieve resources from URLs with integration
 * into the Eclipse Jobs API, as well as the EarthSci Notification API. 
 *
 * <dl>
 * 	<dt>{@link IRetrievalService}</dt> 
 * 	<dd>The primary service interface to use for retrieving resources</dd>
 *  <dt>{@link IRetrieval}</dt>
 *  <dd>
 *  	An object that performs the resource retrieval. 
 *  	Clients can add listeners to this to be informed of retrieval completion etc.
 * 	</dd>
 *  <dt>{@link IRetrievalResult}</dt>
 *  <dd>The result of a retrieval. Contains convenience methods to access the result in a number of formats.</dd>
 *  <dt>{@link IRetriever}</dt>
 *  <dd>Implementations of this interface provide retrieval of particular URL protocols.</dd>
 * </dl>
 * 
 * A common pattern for using the API is as follows:
 * 
 * <pre>
 * &#64Inject
 * IRetrievalService retrievalService;
 * ...
 * IRetrieval retrieval = retrievalService.retrieve(this, resourceUrl);
 * IRetrievalResult result = retrieval.waitAndGetResult();
 * //do something
 * </pre>
 * 
 * This will block the current thread until a result is available, which might take some time depending on resource size, caching policies and
 * how busy the current job manager is. For more asynchronous use, consider the following:
 * 
 * <pre>
 * &#64Inject
 * IRetrievalService retrievalService;
 * ...
 * IRetrieval retrieval = retrievalService.retrieve(this, resourceUrl);
 * retrieval.addListener(new RetrievalAdapter()
 * {
 *     public void complete(IRetrieval retrieval)
 *     {
 *         IRetrievalResult result = retrieval.getResult();
 *         //do something
 *     }
 * }
 * retrieval.start();
 * </pre>
 */
package au.gov.ga.earthsci.core.retrieve;

