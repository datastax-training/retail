package retail.model;

import com.datastax.driver.core.ResultSet;
import junit.framework.TestCase;
import retail.helpers.jsonoutput.GoogleJsonTimesliceView;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */
public class TimeSliceDAOTest extends TestCase {

    public void testGetTimeSlice() throws Exception {
        ResultSet resultset = TimeSliceDAO.getTimeSlice("hotproducts", 100);
        String json = GoogleJsonTimesliceView.getGoogleJsonTimesliceView(resultset);
        assertEquals("",json);
    }
}