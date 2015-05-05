package playlist.controller;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class JinjaServlet extends HttpServlet {

    public String render(String resource_name , Map<String,Object> context) throws IOException {
        Jinjava jinjava = (Jinjava) getServletContext().getAttribute("jinjava");
        URL resource = getServletContext().getResource(resource_name);
        String template = Resources.toString(resource, Charsets.UTF_8);

        return jinjava.render(template, context);

    }
}
