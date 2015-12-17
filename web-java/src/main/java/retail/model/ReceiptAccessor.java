package retail.model;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;

/**
 * DataStax Academy Sample Application
 * <p/>
 * Copyright 2015 DataStax
 */

@Accessor
public interface ReceiptAccessor {

    @Query("select * from receipts_by_credit_card where credit_card_number = :cc_no")
    Result<ReceiptDAO> getReceiptsByCreditCard(@Param("cc_no") long cc_no);

    @Query("select * from receipts where receipt_id = :id")
    Result<ReceiptDAO> getReceiptById(@Param("id") long receipt_id);
}
