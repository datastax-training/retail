package playlist.model;

import com.google.common.collect.Maps;
import com.hubspot.jinjava.Jinjava;
import junit.framework.TestCase;
import playlist.controller.MyResourceLocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
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
    amap.put("key one","value one");
    amap.put("key two","value two");

    context.put("amap", amap);
    String output = jinJava.render(template, context);
    assertEquals("key: value two, value: \n" +
            "key: value one, value: \n", output);
  }

  public void testMapForLoopKeyValueYieldsOnlyValues() throws Exception {
    Map<String, Object> context = Maps.newHashMap();

    String template = "" +
            "{% for key, value in amap %}" +
            "key: {{ key }}, value: {{ value }}\n" +
            "{% endfor %}";


    Map<String, String> amap = new HashMap<>();
    amap.put("key one","value one");
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


    List<Map<String,String>> listofmaps = new ArrayList<>();
    Map<String, String> amap = new HashMap<>();
    amap.put("name","rock");
    amap.put("size", "2 cm");
    listofmaps.add(amap);
    amap = new HashMap<>();
    amap.put("name", "coffee");
    amap.put("size", "20 oz");
    listofmaps.add(amap);

    context.put("listofmaps", listofmaps);
    String output = jinJava.render(template, context);
    assertEquals("name: rock, size: 2 cm\n" +
            "name: coffee, size: 20 oz\n", output);  }

}
