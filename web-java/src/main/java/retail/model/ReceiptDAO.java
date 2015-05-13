package retail.model;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import retail.helpers.cassandra.CassandraData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */

public class ReceiptDAO extends CassandraData {

    // static prepared statements

    private static PreparedStatement get_receipt_by_id_ps = null;
    private static PreparedStatement get_receipt_by_cc_ps = null;

    // Note we need to use CamelCase for jinja support
    // Also - only use the boxed types for loadBeanFromRow

    private Long receiptId;
    private UUID scanId;
    private Long creditCardNumber;
    private String creditCardType;
    private String productId;
    private String productName;
    private Integer quantity;
    private Date receiptTimestamp;
    private BigDecimal receiptTotal;
    private Integer registerId;
    private Integer storeId;
    private BigDecimal total;
    private BigDecimal unitPrice;

    private ReceiptDAO(Row row) {
        // This constructor loads all of the fields of the row using the
        // superclass method loadBeanFromRow.

        loadBeanFromRow(row);
    }

    public static List<ReceiptDAO> getReceiptById(long receipt_id) {

        if (get_receipt_by_id_ps == null) {
            get_receipt_by_id_ps = getSession().prepare("select * from receipts where receipt_id = ?");
        }

        BoundStatement boundStatement = get_receipt_by_id_ps.bind(receipt_id);

        ResultSet resultSet = getSession().execute(boundStatement);

        if (resultSet.isExhausted()) {
            return null;
        }

        List<ReceiptDAO> receiptScans = new ArrayList<>(resultSet.getAvailableWithoutFetching());
        for (Row row: resultSet) {
            receiptScans.add(new ReceiptDAO(row));
        }

        return receiptScans;
    }

    public static List<ReceiptDAO> getReceiptsByCreditCard(long credit_card_number) {

        if (get_receipt_by_cc_ps == null) {
            get_receipt_by_cc_ps = getSession().prepare("select * from receipts_by_credit_card where credit_card_number = ?");
        }

        BoundStatement boundStatement = get_receipt_by_cc_ps.bind(credit_card_number);

        ResultSet resultSet = getSession().execute(boundStatement);

        if (resultSet.isExhausted()) {
            return null;
        }

        List<ReceiptDAO> receipts = new ArrayList<>(resultSet.getAvailableWithoutFetching());
        for (Row row: resultSet) {
            receipts.add(new ReceiptDAO(row));
        }

        return receipts;


    }

    public Long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
    }

    public UUID getScanId() {
        return scanId;
    }

    public void setScanId(UUID scanId) {
        this.scanId = scanId;
    }

    public Long getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(Long creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getCreditCardType() {
        return creditCardType;
    }

    public void setCreditCardType(String creditCardType) {
        this.creditCardType = creditCardType;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Date getReceiptTimestamp() {
        return receiptTimestamp;
    }

    public void setReceiptTimestamp(Date receiptTimestamp) {
        this.receiptTimestamp = receiptTimestamp;
    }

    public BigDecimal getReceiptTotal() {
        return receiptTotal;
    }

    public void setReceiptTotal(BigDecimal receiptTotal) {
        this.receiptTotal = receiptTotal;
    }

    public Integer getRegisterId() {
        return registerId;
    }

    public void setRegisterId(Integer registerId) {
        this.registerId = registerId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
