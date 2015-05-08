package retail.helpers.jinjahelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */
public class JinjaHelper {
    // The elements are in pairs ("q","\"title:usb\"","fq","somepredicate")
    public static String makeURL(String url, String ... elements) throws UnsupportedEncodingException {

        StringBuilder s = new StringBuilder(url);

        char separator = '?';
        for (int i = 0; i < elements.length; i += 2) {
            s.append(separator)
                    .append(elements[i])
                    .append('=')
                    .append(URLEncoder.encode(elements[i+1],"UTF-8"));
           separator = '&';
        }
        return s.toString();
    }

}
