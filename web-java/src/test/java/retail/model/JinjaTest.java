package retail.model;

import com.google.common.collect.Maps;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import junit.framework.TestCase;
import retail.helpers.jinjahelper.JinjaHelper;

import java.util.*;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

/**
 * Some Tests to test the Jinja library itself, and understand its
 * functionality.
 */

public class JinjaTest extends TestCase {

  Jinjava jinJava = null;

  public void setUp() throws Exception {
    super.setUp();
    jinJava = new Jinjava();
  }

  public void testMapForLoopKeyOnlyYieldsOnlyValues() throws Exception {
    Map<String, Object> context = Maps.newHashMap();

    String template = "" +
            "{% for key in amap %}" +
            "key: {{ key }}, value: \n" +
            "{% endfor %}";


    Map<String, String> amap = new HashMap<>();
    amap.put("key one", "value one");
    amap.put("key two", "value two");

    context.put("amap", amap);
    String output = jinJava.render(template, context);
    assertEquals("key: value two, value: \n" +
            "key: value one, value: \n", output);
  }


  public void testListOfMaps() throws Exception {
    Map<String, Object> context = Maps.newHashMap();

    String template = "" +
            "{% for thing in listofmaps %}" +
            "name: {{ thing.name }}, size: {{ thing.size }}\n" +
            "{% endfor %}";


    List<Map<String, String>> listofmaps = new ArrayList<>();
    Map<String, String> amap = new HashMap<>();
    amap.put("name", "rock");
    amap.put("size", "2 cm");
    listofmaps.add(amap);
    amap = new HashMap<>();
    amap.put("name", "coffee");
    amap.put("size", "20 oz");
    listofmaps.add(amap);

    context.put("listofmaps", listofmaps);
    String output = jinJava.render(template, context);
    assertEquals("name: rock, size: 2 cm\n" +
            "name: coffee, size: 20 oz\n", output);
  }


  public void testMakeURLFunction() throws Exception {
    Map<String, Object> context = Maps.newHashMap();

    Jinjava localJinjava = new Jinjava();
    ELFunctionDefinition f = new ELFunctionDefinition("", "makeURL",
            JinjaHelper.class, "makeURL", String.class, String[].class);
    localJinjava.getGlobalContext().registerFunction(f);

    context.put("myvar","This & That");
    String template = "URL: {{ makeURL(\"http://www.datastax.com\"," +
            "\"literal\",\"doesitwork?\"," +
            "\"variable\",myvar) }}";

    String output = localJinjava.render(template, context);
    assertEquals("URL: http://www.datastax.com?literal=doesitwork%3F&variable=This+%26+That",output);

  }
}
