STIAM-Sender
============

STIAM-Sender is a proof-of-concept implementation of a [SAML2](https://www.oasis-open.org/committees/tc_home.php?wg_abbrev=security)-Attribute-Authority (AA)
supporting "extended" attribute queries (containing included authentication 
statements).

This implementation is based on [eGovernment Switzerland](http://www.ech.ch)'s 
standard [eCH 168](http://www.ech.ch/vechweb/page?p=dossier&documentNumber=eCH-0168)
and has accompanied the creation of
[eCH 174](http://www.ech.ch/vechweb/page?p=dossier&documentNumber=eCH-0174).

A brief overview of the architecture and components can be found in 
doc/architecture.pdf, otherwise, use the source.

Installation Instructions
-------------------------

### Prerequisites
To build STIAM-Sender, the following requirements need to be satisfied:

* STIAM-Sender source tree
* JAVA JDK 1.7 (older versions won't work)
* Maven >= 3.0.4

You probably already have the source tree for STIAM-Sender as you are reading 
this document. The other requirements are to be installed according to the usual 
procedures of the operating system in use for building and deployment. 

As an example, to install the two packages in Ubuntu, the following commands 
can be used:

    apt-get update 
    apt-get install openjdk-7-jdk maven

### Pre-build Configuration
The build-process for STIAM-Sender performs a multitude of tests during build.
A lot of these tests require appropriate configuration to work properly, thus
requiring the correct configuration of STIAM-Sender *prior* to initiating 
a build.

Currently, the simplest way to configure the STIAM-Sender is to copy the 
directory doc/sample-configuration to its default location, a directory called 
".stiam-aa" in the user's homedirectory:

    cp -r doc/sample-configuration ~/.stiam-aa

A similar approach should be possible on non-UNIX-like systems, however this has 
not been tested.

#### Alternate Configuration Location
Note: This is a tedious process and currently strongly discouraged.

It is possible to specify an alternate location for configuration data by using
the system property "stiam.config". In this case however, the pom.xml has to be 
adjusted to set this property while running the build process:

    ..
    <build>
      ..
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>2.18</version>
        <configuration>
          <systemPropertyVariables>
            <stiam.config>${basedir}/doc/sample-configuration/aa.properties</stiam.config>
          </systemPropertyVariables>
        </configuration>
      </plugin>
     </plugins>
    </build>
    ..

Also, in the aa.properties-file mentioned in the given stiam.config-property, 
all filepaths have to be adjusted to the correct location. Furthermore, for all 
subsequent invocations of either the STIAM-Sender Attribute Authority or the 
QuerySender component, it has to be ensured that the system property is 
also set.

#### Configuration Options
Refer to the documentation provided in the different configuration files.

### Building
Performing a clean build of the STIAM-Sender is done using maven and should be
quite straightforward:

    mvn clean install

This should, after a while, generate a "target"-directory, containing, among 
others, the following two files:

* aa-1.0.0.war
* aa-1.0.0-jetty-console.war

With this, building of the application was successful.

### Deployment
There are two possibilities for deployment:

#### Use the Console (recommended for testing)
STIAM-Sender includes a graphical console for testing which can easily be 
started using the following command from the source-tree directory:

    java -jar target/aa-1.0.0-jetty-console.war

In the appearing console window, the server's port can be configured and the 
included Jetty-server can be started and stopped. After starting the server,
the default location of the attribute service (AS) should be opened 
automatically in your browser:

 http://localhost:[choosen port]/

A greeting-page should appear, telling that the STIAM-Sender AS is ready to
serve attribute-queries.

Note: in headless mode, the console window does not appear but the server is 
directly started on port 8080. The greeting-page should then be accessible at

 http://localhost:8080/

or even remotely by using the fully qualified hostname.

#### Deploy to a Container
The other option is to deploy the STIAM-Sender to a standard servlet container
like Jetty or Tomcat. For this, both of the above mentioned, generated war files
can be used. Refer to the documentation of the container about how to proceed.

Testing with QuerySender
------------------------
After successful deployment of the STIAM-Sender, it can be tested with the 
integrated QuerySender-tool provided with the distribution.

To issue a test-query to STIAM-Sender's AS, it's best to start the QuerySender
using maven (thus satisfying automatically all dependencies):

    mvn exec:java -Dexec.mainClass="ch.bfh.ti.ictm.iam.stiam.aa.util.QuerySender"

Note: Also for the QuerySender, proper configuration must exist, see previous
sections about configuration.

Logging
-------
STIAM-Sender uses SLF4J for logging, permitting simple "plug-and-play" exchange
of the logging backend. In the default build, it is configured to use JAVA's
java.util.logging-Framework. In order to obtain a higher level of verbosity
in the logs of STIAM-Sender (internals only get logged at DEBUG-level), JAVA's
system wide logging.properties has to be adjusted. Refer to JAVA's documentation
on logging for the details - in short, the following has to be done:

* Set the property "ch.bfh.ti.ictm.iam.stiam.aa.level = FINEST"
* Adjust the level of the handler, for example: 
  "java.util.logging.ConsoleHandler.level = FINEST"

Generating Keys for Signature and Validation
--------------------------------------------
The current distribution of STIAM-Sender comes with a pregenerated 
public/private keypair in the aa.jks-file of the example configuration.
It is thus not necessary to generate new keys to test the functionality. 
In order to easily generate a new keypair, the following call to JAVA's keytool
can be made:

    keytool -genkey -keyalg RSA -alias stiam-aa -keystore aa.jks -storepass secret -validity 360 -keysize 2048

At the interactive prompts of keytool, all questions can be answered with 
[return], accepting the defaults - or adjusted to personal preferences. Refer to
keytool's documentation for further information.

After generating new keys, the current keystore-file has to be replaced with the 
new one. 

*Important:* New keys will result in different queries and thus the parameters
of the unit tests (in aa-test.properties) have to be adjusted before re-running 
a new build. Otherwise, building will not be possible. 

Open Issues
-----------
The implementation of the proof-of-concept has been finished and it has met the
given requirements. There are some open issues left which are listed here for 
the sake of completeness:

* A deeper investigation of possible error cases in the message handling 
  performed by AttributeService should be done.
* Make configuration _directory_ (not file) configurable via 
  stiam.config-system-property.
* The classes ListProperties and Attribute miss proper Testsuites, these should
  be trivial to create
