EventBridge
===========

EventBridge is a flexible event framework that can be used for local and network communication.
More information can be found on the [wiki page](http://quartercode.com/wiki/index.php?title=EventBridge).

License
-------

Copyright (c) 2014 QuarterCode <http://www.quartercode.com/>

EventBridge may be used under the terms of the GNU Lesser General Public License (LGPL) v3.0. See the LICENSE.md file or https://www.gnu.org/licenses/lgpl-3.0.txt for details.

Compilation
-----------

We use maven to handle our dependencies and build, so you need the Java JDK and Maven for compiling the sourcecode.

* Download & install [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* Download & install [Maven 3](http://maven.apache.org/download.cgi).
* Check out this repository (clone or download).
* Navigate to the project folder of this repository which contains a `pom.xml` and run:

        mvn clean install

Builds
------

* EventBridge is built by a [Jenkins job](http://ci.quartercode.com/job/EventBridge/) on the QuarterCode Jenkins instance.
* Finished builds can be downloaded from the [QuarterCode DL website](http://quartercode.com/dl/projects/details?projectId=EventBridge).
* Builds are also available on the [QuarterCode maven repository](http://repo.quartercode.com).
  In order to use EventBridge in another maven project, the following lines must be added to the project's pom:

        <repositories>
            ...
            <repository>
                <id>quartercode-repository</id>
                <url>http://repo.quartercode.com/content/groups/public/</url>
            </repository>
            ...
        </repositories>

        ...

        <dependencies>
            ...
            <dependency>
                <groupId>com.quartercode</groupId>
                <artifactId>eventbridge</artifactId>
                <version>...</version>
            </dependency>
            ...
        </dependencies>
