package retail.model;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.util.List;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */
public class ReceiptDAOTest extends TestCase {

    public void setUp() throws Exception {
        // TODO - insert receipt in database
        super.setUp();
//        CassandraData.getSession().execute("insert into retail.receipts ( ... ) values (...)");
    }

    public void testGetReceiptById() throws Exception {
        List<ReceiptDAO> receipt = ReceiptDAO.getReceiptById(1431062929675L);
        assertNotNull(receipt);
        assertEquals(6, receipt.get(0).getStoreId().intValue());
    }

    public void testGetReceiptsByCreditCard() throws Exception {
        List<ReceiptDAO> receipt = ReceiptDAO.getReceiptsByCreditCard(5584474442969093L);
        assertNotNull(receipt);
        assertEquals(6, receipt.get(0).getStoreId().intValue());

    }
}