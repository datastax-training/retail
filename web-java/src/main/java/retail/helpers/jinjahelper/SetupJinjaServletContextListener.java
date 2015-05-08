package retail.helpers.jinjahelper;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2013 DataStax
 */

public class SetupJinjaServletContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent e){

        ServletContext servletContext = e.getServletContext();
        Jinjava jinjava = new Jinjava();
        jinjava.setResourceLocator(new JinjaJettyResourceLocator(servletContext));

        // Register the helper JinjaHelper.mkURL to Jinja so it can be referenced
        // in templates

        ELFunctionDefinition makeURLFunction = new ELFunctionDefinition("", "makeURL",
                JinjaHelper.class, "makeURL", String.class, String[].class);
        jinjava.getGlobalContext().registerFunction(makeURLFunction);
        servletContext.setAttribute("jinjava", jinjava);
    }
    public void contextDestroyed(ServletContextEvent e) {

    }
}

