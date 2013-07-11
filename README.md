## Geoscience Australia ##

<img src="https://github.com/ga-m3dv/ga-earthsci-rcp/wiki/images/earthsci-logo.png"/>


`EarthSci` is an Eclipse RCP platform for creating applications for the visualisation of earth science data. It is an evolution of the existing [GA World Wind Suite](https://github.com/ga-m3dv/ga-worldwind-suite) built on the [NASA World Wind Java SDK](http://worldwind.arc.nasa.gov/java/).

The vision for `EarthSci` is to take the best features of the [GA World Wind Suite](https://github.com/ga-m3dv/ga-worldwind-suite) (Geological model support, WMS/WFS support, tiled data preparation, keyframe animation etc.) and combine them with the best features of the Eclipse platform (modular plugin architecture, in-built help, native windowing, model-based UI) to create a flexible platform with powerful science visualisation features. As development progresses more and more features will be added.

**For more information on the project, see the [Wiki](https://github.com/ga-m3dv/ga-earthsci-rcp/wiki)**

## Contents: ##

* [Wiki](https://github.com/ga-m3dv/ga-earthsci-rcp/wiki)
* <a href="#basics"/>Project basics</a>
* <a href="#reportBugs"/>Reporting bugs and requesting features</a>
* <a href="#contribute"/>How to contribute</a>
* <a href="#projectStructure">Project structure</a>
* <a href="#pluginsFeatures">Plugins and features</a>
* <a href="#license">License</a>
* <a href="#contact">Contact</a>

- - -

>**Important:** 
>
>`EarthSci` is still under development and is not intended for use in any production environment. 
>
>Development is ongoing - stay tuned! Feel free to help out and shape the direction of the platform.
>
>For a production-ready platform, consider the [GA World Wind Suite](https://github.com/ga-m3dv/ga-worldwind-suite).

- - -

<img src="https://github.com/ga-m3dv/ga-earthsci-rcp/wiki/images/screenshots/3dmodels.jpg"/>

<a name="basics"/>
## Project basics ##

The `EarthSci` project is built on the [Eclipse 4 RCP platform](http://www.eclipse.org/eclipse4/). 
It uses a number of core 3rd-party libraries including the [NASA World Wind Java SDK](http://worldwind.arc.nasa.gov/java/),
[GDAL](http://www.gdal.org/), and [JOGL](https://jogamp.org/jogl/www/).

A lot of the geospatial data visualisation functionality has been ported across from the [GA World Wind Suite](https://github.com/ga-m3dv/ga-worldwind-suite).

The project uses [Maven 3.0+](http://maven.apache.org) and [Tycho](http://eclipse.org/tycho/) for dependency and build management.

For information on getting up and running with the project, see the [Developer's guide](https://github.com/ga-m3dv/ga-earthsci-rcp/wiki/Developer%27s-guide).

<a name="reportBugs"/>
## Reporting bugs and requesting features ##

Our goal is to make `EarthSci` a solid platform for developing earth science visualisation applications. 
If you find any bugs/issues with the platform, or would like to see new features added, please report them 
via the [project issue tracker](https://github.com/ga-m3dv/ga-earthsci-rcp/issues).
Better yet, implement the changes yourself and then open a [pull request](http://help.github.com/send-pull-requests/).

Please note that your bug/issue/feature may already be registered in the issue tracker. Please take a quick look before
you raise a ticket to make sure you aren't duplicating an existing issue. For a good guide on writing effective issue reports,
see Allan McRae's blog post ["How to file a bug report"](http://allanmcrae.com/2011/05/how-to-file-a-bug-report/).

<a name="contribute"/>
## How to contribute ##

We welcome all contributions to the project, no matter how large or small.

The project is being managed using the ["fork+pull"](http://help.github.com/fork-a-repo/) method familiar to most github users.
To contribute patches to the codebase:

1. Fork the repository
2. Make your changes
3. Submit a pull request

We will endeavour to respond to your request ASAP.

Some ideas for how to contribute:

* Find a bug/issue/feature request in the [project issue tracker](https://github.com/ga-m3dv/ga-earthsci-rcp/issues) and fix it!
* Implement a feature/plugin you would like to see
* Conduct some user testing and report any issues you find
* Add some unit tests to the test suite
* Develop and host a plugin/feature that can be included at runtime
* Help create technical documentation and developer guides to make it easier for future developers to get involved.
* Translate some of the message bundles to make the platform accessible to a wider audience

<a name="projectStructure"/>
## Project Structure ##

The overall project structure is as follows:

    -- pom.xml				The master POM file. Will build all plugins, features and products.
    |- parent-pom.xml		A common parent POM that should be referenced by all plugin POMs.
    |- externals\			Contains all included third-party plugins (e.g. NASA World Wind)
    |- plugins\				Contains all plugin projects developed as part of the platform. This includes test plugins.
    |- features\			Contains all feature projects developed as part of the platform. This includes product features.
    |- webstart\			Contains a utility project used to generate webstart JNLP definitions for
    |- verifier\			Contains a utility project that checks the configuration of plugins and features and fails the build if it detects mis-configured projects etc. 

<a name="pluginsFeatures"/>
### Plugins and Features ###

The project follows the Eclipse application pattern, and is composed of a number of plugins (or bundles) and features (groups of plugins that are related).

<table>
	<thead><tr><td><b>Plugin</b></td><td><b>Description</b></td></tr></thead>
	<tbody>
		<tr><td>au.gov.ga.earthsci.core</td><td>The core, non-UI components of the EarthSci platform</td></tr>
		<tr><td>au.gov.ga.earthsci.core.tests</td><td>Tests for the core plugin</td></tr>
		
		<tr><td>au.gov.ga.earthsci.application</td><td>The core UI components of the EarthSci platform</td></tr>
		<tr><td>au.gov.ga.earthsci.application.tests</td><td>Tests for the application UI plugin</td></tr>
		
		<tr><td>au.gov.ga.earthsci.common</td><td>Shared, reusable non-UI components</td></tr>
		<tr><td>au.gov.ga.earthsci.common.tests</td><td>Tests for the common components</td></tr>
		<tr><td>au.gov.ga.earthsci.common.ui</td><td>Shared, reusable UI components</td></tr>

		<tr><td>au.gov.ga.earthsci.eclipse.extras</td><td>A copy of some internal Eclipse RCP components for use in the application</td></tr>
		<tr><td>au.gov.ga.earthsci.jface.extras</td><td>A copy of some internal JFace components for use in the application</td></tr>
		<tr><td>org.eclipse.ui.workbench.compatibility</td><td>Some adapters for working with the Workbench in an e4 application</td></tr>

		<tr><td>au.gov.ga.earthsci.injectable</td><td>Provides a mechanism for having arbitrary objects participate in the DI mechanism</td></tr>

		<tr><td>au.gov.ga.earthsci.intent</td><td>The Intent API</td></tr>

		<tr><td>au.gov.ga.earthsci.logging</td><td>Provides configuration to enable <a href="http://www.slf4j.org/">SLF4J</a> logging</td></tr>

		<tr><td>au.gov.ga.earthsci.catalog</td><td>The core Catalog API</td></tr>
		<tr><td>au.gov.ga.earthsci.catalog.tests</td><td>Tests for the core Catalog API</td></tr>
		<tr><td>au.gov.ga.earthsci.catalog.ui</td><td>The basic UI components for the Catalog API</td></tr>
		<tr><td>au.gov.ga.earthsci.catalog.ui.tests</td><td>Tests for the Catalog API UI components</td></tr>
		<tr><td>au.gov.ga.earthsci.catalog.dataset</td><td>A catalog implementation that reads legacy dataset.xml files</td></tr>
		<tr><td>au.gov.ga.earthsci.catalog.directory</td><td>A catalog implementation that reads from a local file system</td></tr>
		<tr><td>au.gov.ga.earthsci.catalog.wms</td><td>A catalog implementation that reads from <a href="http://www.opengeospatial.org/standards/wms">OGC WMS</a> services</td></tr>

		<tr><td>au.gov.ga.earthsci.layer.ui</td><td>Basic UI components for interacting with the Layer API</td></tr>
		<tr><td>au.gov.ga.earthsci.layer.ui.tests</td><td>Tests for the Layer UI components</td></tr>

		<tr><td>au.gov.ga.earthsci.bookmark</td><td>The core Bookmark API</td></tr>
		<tr><td>au.gov.ga.earthsci.bookmark.tests</td><td>Tests for the core Bookmark API</td></tr>
		<tr><td>au.gov.ga.earthsci.bookmark.ui</td><td>The basic UI components for the Bookmark API</td></tr>
		<tr><td>au.gov.ga.earthsci.bookmark.ui.tests</td><td>Tests for Bookmark API UI components</td></tr>

		<tr><td>au.gov.ga.earthsci.notification</td><td>The core Notification API</td></tr>
		<tr><td>au.gov.ga.earthsci.notification.tests</td><td>Tests for the core Notification API</td></tr>
		<tr><td>au.gov.ga.earthsci.notification.ui</td><td>The basic UI components for the Notification mechanism</td></tr>
		<tr><td>au.gov.ga.earthsci.notification.popup</td><td>A plugin that provides a popup notification receiver</td></tr>
		
		<tr><td>au.gov.ga.earthsci.model</td><td>The core 3D Model API</td></tr>
		<tr><td>au.gov.ga.earthsci.model.tests</td><td>Tests for the core 3D Model API</td></tr>
		<tr><td>au.gov.ga.earthsci.model.core</td><td>Basic implementations for the 3D Model API</td></tr>
		<tr><td>au.gov.ga.earthsci.model.core.tests</td><td>Tests for the basic 3D Model API implementations</td></tr>
		<tr><td>au.gov.ga.earthsci.model.ui</td><td>Basic UI components for the 3D Model API</td></tr>

		<tr><td>au.gov.ga.earthsci.worldwind</td><td>Extensions and utilities for the NASA WorldWind SDK, used to provided additional layer types etc.</td></tr>
		<tr><td>au.gov.ga.earthsci.worldwind.tests</td><td>Tests for the au.gov.ga.earthsci.worldwind plugin</td></tr>
	</tbody>
</table>

<br/>

<table>
	<thead><tr><td><b>Feature</b></td><td><b>Description</b></td></tr></thead>
	<tbody>
		<tr><td>au.gov.ga.earthsci.feature</td><td>The core feature of the EarthSci platform</td></tr>
		<tr><td>au.gov.ga.earthsci.product</td><td>The product feature of the EarthSci platform</td></tr>
		<tr><td>org.eclipse.rcp.minimal</td><td>A cut-down Eclipse RCP feature that removes unneeded plugins</td></tr>
	</tbody>
</table>

<a name="license"/>
## License ##
The `EarthSci` project is released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html) and is distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

See the License for the specific language governing permissions and limitations under the License.

The project uses third party components which may have different licenses. Please refer to individual components for more details. 

<a name="contact"/>
## Contact ##
For more information on the `EarthSci` project, please email *m3dv:at:ga.gov.au*.