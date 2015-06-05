# Stable release #

GwtQuery stable release is 1.4.2, it includes the core classes, and plugins included in the core (Events, Effects, Transformations, Queue, Widgets and Deferred)

Download: [gwtquery-1.4.2.jar](http://repo1.maven.org/maven2/com/googlecode/gwtquery/gwtquery/1.4.2/gwtquery-1.4.2.jar)

Supported GWT version: 2.2.0, 2.3.0, 2.4.0, 2.5.0+ and 2.6.0

Release date: Mar 10 2014

## Stable release for old GWT version ##
GwtQuery 1.3.+ is not compatible with a version of GWT less than 2.2.0. If you want to use GwtQuery with old versions of GWT, you have to download the appropriate classifier of GwtQuery 1.1.0 for the gwt version you are using:
  * **gquery for gwt-2.1.0/2.1.1**. [gwtquery-1.1.0-2.1.0.jar](http://repo1.maven.org/maven2/com/googlecode/gwtquery/gwtquery/1.1.0/gwtquery-1.1.0-2.1.0.jar)
  * **gquery for gwt-2.0.1/2.0.3/2.0.4**. [gwtquery-1.1.0-2.0.1.jar](http://repo1.maven.org/maven2/com/googlecode/gwtquery/gwtquery/1.1.0/gwtquery-1.1.0-2.0.1.jar)

## Maven ##

Maven user has to add the following sections in the pom.xml :
```
 <dependencies>
   <dependency>
     <groupId>com.googlecode.gwtquery</groupId>
     <artifactId>gwtquery</artifactId>
     <version>1.4.2</version>
     <!-- If you are using old versions of gwt, use version 1.1.0 of GwtQuery and uncomment the appropriate line -->
     <!-- <version>1.1.0</version> -->
     <!-- <classifier>2.1.0</classifier> -->
     <!-- <classifier>2.0.1</classifier> -->
     <scope>provided</scope>
   </dependency>
 </dependencies>
```

# Snapshot #

The snapshot version includes last code features and fixes, note that this is built frequently and sometimes could be broken.
  * Download 1.4.3-SNAPSHOT from the [sonatype repository](https://oss.sonatype.org/content/repositories/snapshots/com/googlecode/gwtquery/gwtquery/1.4.3-SNAPSHOT/)

  * Maven users of the SNAPSHOT version you have to add these lines:
```
  <repositories>
    <repository>
      <id>sonatype-snapshots</id>
      <url>http://oss.sonatype.org/content/repositories/snapshots</url>
      <snapshots><enabled>true</enabled></snapshots>
      <releases><enabled>false</enabled></releases>
    </repository>
  </repositories>

 <dependencies>
   <dependency>
     <groupId>com.googlecode.gwtquery</groupId>
     <artifactId>gwtquery</artifactId>
     <version>1.4.3-SNAPSHOT</version>
     <scope>provided</scope>
   </dependency>
 </dependencies>
```

# Archetype #
> The archetype generates a Gwt project with all set to use GwtQuery.
  * It is available in central repositories of maven: [gquery-archetype-1.3.2.jar](http://repo1.maven.org/maven2/com/googlecode/gwtquery/gquery-archetype/1.3.2/gquery-archetype-1.3.2-sources.jar)

# Plugins #

> Download them from this page:
> http://code.google.com/p/gwtquery-plugins/downloads/list

> Maven users have to add this repositories to the pom.xml:
```
  <repositories>
    <!-- Repository for gquery plugins -->
    <repository>
      <id>gwtquery-plugins</id>
      <url>http://gwtquery-plugins.googlecode.com/svn/mavenrepo</url>
    </repository>
  </repositories>
```
