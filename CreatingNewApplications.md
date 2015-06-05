


This page describes how to create a new mavenized gwt-application which uses gQuery,  from scratch, and using the gquery-archetype.

## Steps ##

  * Install maven and put the mvn command in your path
  * Run the following commands in a terminal
  * This command generates a project ready to work with gwt-2.5.1 if you wanted an old version edit the file pom.xml and set the version, or use an older version of the archetype.
```
mvn archetype:generate  -DarchetypeGroupId=com.googlecode.gwtquery \
                        -DarchetypeArtifactId=gquery-archetype  \
                        -DarchetypeVersion=1.3.3 \
                        -DgroupId=com.mycompany \
                        -DartifactId=myproject \
                        -DprojectName=MyProject 

```
  * Change to the new created folder and check that the project compiles
```
  cd myproject/
  mvn clean package
```
  * Run the project in develop mode
```
  mvn gwt:run
```

## Importing the project in Eclipse ##

The archetype generates a project ready to use in eclipse, but before importing it you have to install the following plugins:
  * Google plugin for eclipse, update-site: http://dl.google.com/eclipse/plugin/3.6(Helios) http://dl.google.com/eclipse/plugin/3.7(Indigo) http://dl.google.com/eclipse/plugin/4.2(Juno)
  * Sonatype Maven plugin, Update-site: http://m2eclipse.sonatype.org/site/m2e
Then you can import the project in your eclipse using either:
    * File -> Import -> Existing Projects into Workspace
    * File -> Import -> Existing maven projects
After this you should be able to run the project in development mode and the gwt test unit.
  * Right click on the project -> Run as -> Web Application
  * Right click on the test class -> Run as -> GWT JUnit Test