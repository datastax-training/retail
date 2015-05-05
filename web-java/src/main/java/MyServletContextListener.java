import com.hubspot.jinjava.Jinjava;
import playlist.controller.MyResourceLocator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2013 DataStax
 */

public class MyServletContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent e){

        ServletContext servletContext = e.getServletContext();
        Jinjava jinjava = new Jinjava();
        jinjava.setResourceLocator(new MyResourceLocator(servletContext));
        servletContext.setAttribute("jinjava", jinjava);
    }
    public void contextDestroyed(ServletContextEvent e){
        System.out.println("Destroyed"); }
}

