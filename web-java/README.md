Note: This module requires Java 8.

To build:

    mvn install

Note: The install target is used to pull all of the dependent libs
 into the target/libs directory. The package target only builds the jar file.

To run:


    java -cp "target/retail-1.0.jar:target/lib/*" StartJetty
