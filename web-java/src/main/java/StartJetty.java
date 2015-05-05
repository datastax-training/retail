import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.net.URL;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class StartJetty {

  private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StartJetty.class);
  private static final String WEBAPP_DIR = "webapp" ;

  public static void main(String[] args) throws Exception
  {

    // Using full class names to avoid confusion between Log4j and SLF4j
    org.apache.log4j.BasicConfigurator.configure();

    // Change this to a different level for more output
    org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.INFO);

    //
    // The web resources get copied into the jar or class directories
    // because we declared them as resources in Maven
    // This is how we get the uri to it.  Even works in the debugger
    //

    URL webdirInJarURI = StartJetty.class.getClassLoader().getResource(WEBAPP_DIR);

    if (webdirInJarURI == null) {
      throw new Exception("Can't locate " + WEBAPP_DIR);
    }

    logger.info("Web Resources Directory: " + webdirInJarURI.toExternalForm());

    Server server = new Server(5002);

    WebAppContext context = new WebAppContext();
    context.setDescriptor(context + "/WEB-INF/web.xml");
    context.setResourceBase(webdirInJarURI.toExternalForm());

    context.setContextPath("/");
    context.setParentLoaderPriority(true);

    server.setHandler(context);
    server.start();
    server.join();
  }


}
