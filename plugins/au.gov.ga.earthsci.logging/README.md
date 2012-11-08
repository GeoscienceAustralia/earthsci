# au.gov.ga.earthsci.logging #

## Important ##
The SLF4J and LogBack dependencies are included in this plugin as classpath
JARs rather than plugins in order to solve a dependency cycle that broke the
Tycho reactor build.

Ideally these libs would exist as plugins in the /externals folder, but until
Tycho dependency resolution is improved this is the easiest way to make it work.