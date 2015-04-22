package techsupply.flagship

case class MetagenerData(minSampleId: Int, maxSampleId: Int, sampleValues: List[SV])

case class SV(sampleId: Int, fieldValues: SDS)

case class SDS(scan_duration_seconds: BigDecimal, scan_qty: String, product_id: String, item_discount: BigDecimal)
