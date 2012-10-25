
/**
 * Provides a centralised mechanism for retrieving resources identified by {@link java.net.URL}s.
 * <p/>
 * The retrieval API provides a number of components used together to easily retrieve resources from URLs with integration
 * into the Eclipse Jobs API, as well as the EarthSci Notification API. 
 *
 * <dl>
 * 	<dt>{@link au.gov.ga.earthsci.core.retrieve.IRetrievalService}</dt> 
 * 	<dd>The primary service interface to use for retrieving resources</dd>
 *  <dt>{@link au.gov.ga.earthsci.core.retrieve.RetrievalJob}</dt>
 *  <dd>
 *  	A {@link org.eclipse.core.runtime.jobs.Job} that performs the resource retrieval. 
 *  	Clients can use standard listening mechanisms to be informed of retrieval completion etc.
 * 	</dd>
 *  <dt>{@link au.gov.ga.earthsci.core.retrieve.IRetrievalResult}</dt>
 *  <dd>The result of a retrieval. Contains convenience methods to access the result in a number of formats.</dd>
 * </dl>
 * 
 * A common pattern for using the API is as follows:
 * 
 * <pre>
 * &#64Inject
 * IRetrievalService retrievalService;
 * ...
 * RetrievalJob job = retrievalService.retrieve(resourceUrl);
 * IRetrievalResult result = job.waitAndGetResult();
 * if (result.isSuccessful())
 * {
 *     // do something
 * }
 * </pre>
 * 
 * This will block the current thread until a result is available, which might take some time depending on resource size, caching policies and
 * how busy the current job manager is. For more asynchronous use, consider the following:
 * 
 * <pre>
 * &#64Inject
 * IRetrievalService retrievalService;
 * ...
 * RetrievalJob job = retrievalService.retrieve(resourceUrl);
 * job.addJobChangeListener(new JobChangeAdapter(){
 * 		private void done(JobChangeEvent e)
 * 		{
 *          if (event.getResult() != Status.OK_STATUS)
 *          {
 *          	// do something
 *          }
 *          
 * 			IRetrievalResult result = job.getResult();
 * 			if (result.isSuccessful())
 * 			{
 * 			    // do something
 *			}
 * 		}
 * });
 * </pre>
 */
package au.gov.ga.earthsci.core.retrieve;
