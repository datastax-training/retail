package retail.model;


import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import retail.helpers.cassandra.CassandraData;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */

@Table(name = "receipts",
        readConsistency = "LOCAL_ONE",
        writeConsistency = "LOCAL_QUORUM")

public class ReceiptDAO extends CassandraData {

    private static Mapper<ReceiptDAO> mapper = getMappingManager(ReceiptDAO.class);
    private static ReceiptAccessor accessor = mapper.getManager().createAccessor(ReceiptAccessor.class);

    @PartitionKey
    @Column(name = "receipt_id")
    private Long receiptId;
    @ClusteringColumn(0)
    @Column(name = "scan_id")
    private UUID scanId;
    @Column(name = "credit_card_number")
    private Long creditCardNumber;
    @Column(name = "credit_card_type")
    private String creditCardType;
    @Column(name = "product_id")
    private String productId;
    @Column(name = "product_name")
    private String productName;
    @Column
    private Integer quantity;
    @Column(name = "receipt_timestamp")
    private Date receiptTimestamp;
    @Column(name = "receipt_total")
    private BigDecimal receiptTotal;
    @Column(name = "register_id")
    private Integer registerId;
    @Column(name = "store_id")
    private Integer storeId;
    @Column
    private BigDecimal total;
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    public static List<ReceiptDAO> getReceiptById(long receipt_id) {
        return accessor.getReceiptById(receipt_id).all();
    }

    public static List<ReceiptDAO> getReceiptsByCreditCard(long credit_card_number) {
        return accessor.getReceiptsByCreditCard(credit_card_number).all();
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
