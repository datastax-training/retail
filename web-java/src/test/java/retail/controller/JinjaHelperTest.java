package retail.controller;

import junit.framework.TestCase;
import retail.helpers.jinjahelper.JinjaHelper;

import java.lang.reflect.Method;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */
public class JinjaHelperTest extends TestCase {

    public void testMakeURLNoParams() throws Exception {
        String url = JinjaHelper.makeURL("http://www.datastax.com");
        assertEquals("http://www.datastax.com", url);
    }

    public void testMakeURL1Param() throws Exception {
        String url = JinjaHelper.makeURL("http://www.datastax.com", "q", "somewhere");
        assertEquals("http://www.datastax.com?q=somewhere", url);
    }

    public void testMakeURL2Params() throws Exception {
        String url = JinjaHelper.makeURL("http://www.datastax.com", "q", "somewhere", "fq", "else");
        assertEquals("http://www.datastax.com?q=somewhere&fq=else", url);
    }

    public void testMakeURL2ParamsWithAmpersand() throws Exception {
        String url = JinjaHelper.makeURL("http://www.datastax.com", "q", "somewhere", "burger", "A & W");
        assertEquals("http://www.datastax.com?q=somewhere&burger=A+%26+W", url);
    }

    public void testMakeURL2ParamsWithAmpersandAndUnicode() throws Exception {
        String url = JinjaHelper.makeURL("http://www.datastax.com", "q", "L’Oréal", "burger", "A & W");
        assertEquals("http://www.datastax.com?q=L%E2%80%99Or%C3%A9al&burger=A+%26+W", url);
    }

    public String aFunction (String foo) {
            return "Hello there " + foo;
    }

    public void testReflection() throws NoSuchMethodException {

        Class u = JinjaHelper.class;
        Method m = u.getMethod("makeURL", String.class, String[].class);
        assertNotNull(m);

    }


}