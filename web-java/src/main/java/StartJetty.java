import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
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

    // Set up the application.  Note that some of the application is configured
    // in SetupJinjaServletContextListener

    ResourceHandler staticResourceHandler = new ResourceHandler();
    staticResourceHandler.setResourceBase(webdirInJarURI.toExternalForm() + "/static");
    staticResourceHandler.setDirectoriesListed(false);

    // Create context handler for static resource handler.
    ContextHandler staticContextHandler = new ContextHandler();
    staticContextHandler.setContextPath("/static");
    staticContextHandler.setHandler(staticResourceHandler);


    WebAppContext webAppContext = new WebAppContext();
    webAppContext.setDescriptor(webAppContext + "/WEB-INF/web.xml");
    webAppContext.setResourceBase(webdirInJarURI.toExternalForm());

    webAppContext.setContextPath("/");
    webAppContext.setParentLoaderPriority(true);
    webAppContext.setInitParameter("dirAllowed", "false");


    // Create a handler list to store our static and servlet context handlers.
    HandlerList handlers = new HandlerList();
    handlers.setHandlers(new Handler[]{staticContextHandler, webAppContext});

    server.setHandler(handlers);
    server.start();
    server.join();
  }


}
