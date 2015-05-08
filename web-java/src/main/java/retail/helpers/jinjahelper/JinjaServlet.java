package retail.helpers.jinjahelper;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class JinjaServlet extends HttpServlet {

    // Returns a UTF-8 String
    public byte[] render(String resource_name , Map<String,Object> context) throws IOException {
        Jinjava jinjava = (Jinjava) getServletContext().getAttribute("jinjava");
        URL resource = getServletContext().getResource(resource_name);
        String template = Resources.toString(resource, Charsets.UTF_8);

        return jinjava.render(template, context).getBytes("UTF-8");
    }
}
