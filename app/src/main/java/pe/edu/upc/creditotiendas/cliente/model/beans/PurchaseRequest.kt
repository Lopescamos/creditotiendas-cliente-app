package pe.edu.upc.creditotiendas.cliente.model.beans

data class PurchaseRequestEligibilityResponse(
    val eligible: Boolean,
    val profileActive: Boolean,
    val firstSalePaidOnTime: Boolean,
    val noOverdueInstallments: Boolean,
    val withinAvailableCredit: Boolean,
    val availableCredit: Double,
    val failureReasons: List<String>?
)

data class CreatePurchaseRequest(val items: List<SaleLineItem>)

data class PurchaseRequestResponse(
    val id: Long,
    val creditProfileId: Long,
    val storeId: Long,
    val items: List<SaleLineItem>?,
    val totalAmount: Double,
    val status: String,                     // PENDING | APPROVED | REJECTED
    val requestedAt: String,
    val respondedAt: String?,
    val rejectionReason: String?,
    val generatedSaleId: Long?
)
