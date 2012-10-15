## Geoscience Australia ##
# EarthSci RCP #

`EarthSci RCP` is an Eclipse RCP platform for creating applications for the visualisation of earth science data. It is an evolution of the existing [GA World Wind Suite](https://github.com/ga-m3dv/ga-worldwind-suite) built on the [NASA World Wind Java SDK](http://worldwind.arc.nasa.gov/java/).

The vision for `EarthSci` is to take the best features of the [GA World Wind Suite](https://github.com/ga-m3dv/ga-worldwind-suite) (Geological model support, WMS/WFS support, tiled data preparation, keyframe animation etc.) and combine them with the best features of the Eclipse platform (modular plugin architecture, in-built help, native windowing, model-based UI) to create a flexible platform with powerful science visualisation features. As development progresses more and more features will be added.

# For more information on the project, see the [Wiki](https://github.com/ga-m3dv/ga-earthsci-rcp/wiki) #

- - -
**Important:** 

`EarthSci RCP` is still in pre-alpha development and is not intended for use in any production environment. 

Development is ongoing - stay tuned! Feel free to help out and shape the direction of the platform.

For a production-ready platform, consider the [GA World Wind Suite](https://github.com/ga-m3dv/ga-worldwind-suite).
- - -

## Project Structure ##

The overall project structure is as follows:

    -- pom.xml				The master POM file. Will build all plugins, features and products.
    |- parent-pom.xml		A common parent POM that should be referenced by all plugin POMs.
    |- externals\			Contains all included third-party plugins (e.g. NASA World Wind)
    |- plugins\				Contains all plugin projects developed as part of the platform. This includes test plugins.
    |- features\			Contains all feature projects developed as part of the platform. This includes product features.
    |- webstart\			Contains a utility project used to generate webstart JNLP definitions for 

### Plugins ###

<table>
	<thead><tr><td><b>Plugin</b></td><td><b>Description</b></td></tr></thead>
	<tbody>
		<tr><td>au.gov.ga.earthsci.core</td><td>The core, non-UI components of the EarthSci platform</td></tr>
		<tr><td>au.gov.ga.earthsci.core.tests</td><td>Tests for the core plugin</td></tr>
		<tr><td>au.gov.ga.earthsci.application</td><td>The core UI components of the EarthSci platform</td></tr>
		<tr><td>au.gov.ga.earthsci.notification.popup</td><td>A plugin that provides a popup notification receiver</td></tr>
	</tbody>
</table>

### Features ###

<table>
	<thead><tr><td><b>Feature</b></td><td><b>Description</b></td></tr></thead>
	<tbody>
		<tr><td>au.gov.ga.earthsci.feature</td><td>The core feature of the EarthSci platform</td></tr>
		<tr><td>au.gov.ga.earthsci.product</td><td>The product feature of the EarthSci platform</td></tr>
	</tbody>
</table>

## License ##
The `GA EarthSci RCP` project is released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html) and is distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

See the License for the specific language governing permissions and limitations under the License.

## Contact ##
For more information on the `EarthSci RCP` project, please email *m3dv:at:ga.gov.au*.