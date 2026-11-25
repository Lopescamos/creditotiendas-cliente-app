package pe.edu.upc.creditotiendas.cliente.model.beans

data class InstallmentResponse(
    val id: Long,
    val creditSaleId: Long,
    val creditProfileId: Long,
    val installmentNumber: Int,
    val dueDate: String,
    val capital: Double,
    val interest: Double,
    val installmentAmount: Double,
    val remainingBalance: Double,
    val status: String,                     // PENDING | OVERDUE | PAID_ON_TIME | PAID_LATE
    val paidAt: String?
)

data class ScheduledInstallment(
    val number: Int,
    val dueDate: String,
    val principal: Double,
    val interest: Double,
    val installment: Double,
    val remainingBalance: Double
)

data class PaymentPlanResponse(
    val installmentCount: Int,
    val installmentAmount: Double,
    val totalInterest: Double,
    val totalAmount: Double,
    val schedule: List<ScheduledInstallment>
)
