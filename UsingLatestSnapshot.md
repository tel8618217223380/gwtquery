How to use the latest code from trunk.
# Downloading manually #

Get latest SNAPSHOT artifacts from the Sonatype repository:

http://oss.sonatype.org/content/repositories/snapshots/com/googlecode/gwtquery/gwtquery

# Using the maven repository #
  * Periodically the trunk is compiled an deployed in the Sonatype snapshot repository, so you could download latest jar from [here](https://oss.sonatype.org/content/repositories/snapshots/com/googlecode/gwtquery/gwtquery/1.3.2-SNAPSHOT/gwtquery-1.3.2-20130212.220215-2.jar)

  * Also you could use it in your maven project just adding these lines to your pom.xml

  * Latest snapshot is compiled using last gwt-version (it should work with 2.5.x, 2.4.0, 2.3.0 and 2.2.0), so if you wanted to use it in an older gwt version you should compile by hand.

```
    <repositories>
        <repository>
            <id>sonatype</id>
            <url>http://oss.sonatype.org/content/repositories/snapshots</url>
            <snapshots><enabled>true</enabled></snapshots>
            <releases><enabled>false</enabled></releases>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>com.googlecode.gwtquery</groupId>
            <artifactId>gwtquery</artifactId>
            <version>1.4.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
```

# Compiling it #
  * Download the source code from either github or google-code
  * Compile the code using maven
  * Use the generated jar file
```
 $ git clone http://code.google.com/p/gwtquery/ 
 or 
 $ git clone git@github.com:gwtquery/gwtquery.git

 $ cd gwtquery
 $ mvn clean package -Dmaven.test.skip=true    
 $ ls -l gwtquery-core/target/gwtquery-*-SNAPSHOT.jar 
```