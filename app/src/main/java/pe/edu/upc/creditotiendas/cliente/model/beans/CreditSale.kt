package pe.edu.upc.creditotiendas.cliente.model.beans

data class SaleLineItem(
    val productId: Long,
    val productName: String?,
    val quantity: Int,
    val unitPrice: Double
)

data class CreditSaleResponse(
    val id: Long,
    val creditProfileId: Long,
    val storeId: Long,
    val items: List<SaleLineItem>?,
    val totalAmount: Double,
    val installmentCount: Int,
    val graceDays: Int,
    val modality: String,                   // SINGLE_PAYMENT | INSTALLMENTS
    val interestRateSnapshot: Double?,
    val interestRateTypeSnapshot: String?,
    val capitalizationPeriodSnapshot: String?,
    val lateInterestRateSnapshot: Double?,
    val lateInterestRateTypeSnapshot: String?,
    val status: String,                     // ACTIVE | OVERDUE | PAID_ON_TIME | PAID_LATE
    val saleDate: String?
)
