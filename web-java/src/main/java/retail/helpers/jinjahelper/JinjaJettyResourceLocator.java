package retail.helpers.jinjahelper;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.loader.ResourceLocator;
import com.hubspot.jinjava.loader.ResourceNotFoundException;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2013 DataStax
 */

public class JinjaJettyResourceLocator implements ResourceLocator {

    private ServletContext servletContext;

    public JinjaJettyResourceLocator(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public String getString(String fullName, Charset encoding,
                            JinjavaInterpreter interpreter) throws IOException {
        try {

            URL resource = servletContext.getResource(fullName);

            return Resources.toString(resource, Charsets.UTF_8);
        } catch(IllegalArgumentException e) {
            throw new ResourceNotFoundException("Couldn't find resource: " + fullName);
        }
    }
}
