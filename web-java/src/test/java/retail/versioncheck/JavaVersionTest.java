package retail.versioncheck;


import junit.framework.TestCase;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 *  Tests to check that we are running on the correct version of Java
 *
 */

public class JavaVersionTest extends TestCase {

  public static final String MINIMUM_JAVA_VERSION = "1.7";

  public void testJavaVersion() {

    String javaVersion = System.getProperty("java.version");
    System.out.printf("Current Java Version: %s\n", javaVersion);

    assertTrue("The Java Version must be > " + MINIMUM_JAVA_VERSION, javaVersion.compareTo(MINIMUM_JAVA_VERSION) > 0);
  }

}
