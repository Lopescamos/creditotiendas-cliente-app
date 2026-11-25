package pe.edu.upc.creditotiendas.cliente.model.response

import pe.edu.upc.creditotiendas.cliente.model.beans.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Endpoints consumidos por el cliente (rol CLIENT) — documentados en
 * docs/estado-{iam,credit,billing,store}.md del backend.
 */
interface WebService {

    // ===== IAM =====
    @POST("api/v1/authentication/sign-in")
    suspend fun signIn(@Body req: SignInRequest): Response<SignInResponse>

    @GET("api/v1/users/me")
    suspend fun me(): Response<MeResponse>

    // ===== Credit Management =====
    @GET("api/v1/credit-profiles/me")
    suspend fun myCreditProfiles(): Response<List<CreditProfileResponse>>

    @GET("api/v1/credit-profiles/{id}")
    suspend fun getCreditProfile(@Path("id") id: Long): Response<CreditProfileResponse>

    @GET("api/v1/credit-profiles/{id}/credit-sales")
    suspend fun creditSalesByProfile(@Path("id") id: Long): Response<List<CreditSaleResponse>>

    @GET("api/v1/credit-sales/{id}")
    suspend fun getCreditSale(@Path("id") id: Long): Response<CreditSaleResponse>

    // US-07: cronograma con snapshot (plan ideal)
    @GET("api/v1/credit-sales/{id}/payment-plan")
    suspend fun getPaymentPlan(@Path("id") id: Long): Response<PaymentPlanResponse>

    // US-07: cuotas reales con estado
    @GET("api/v1/credit-sales/{id}/installments")
    suspend fun installmentsBySale(@Path("id") id: Long): Response<List<InstallmentResponse>>

    // US-08: pre-check de elegibilidad
    @GET("api/v1/credit-profiles/{id}/purchase-request-eligibility")
    suspend fun checkEligibility(@Path("id") id: Long): Response<PurchaseRequestEligibilityResponse>

    // US-08: crear solicitud
    @POST("api/v1/credit-profiles/{id}/purchase-requests")
    suspend fun createPurchaseRequest(
        @Path("id") id: Long,
        @Body req: CreatePurchaseRequest
    ): Response<PurchaseRequestResponse>

    // US-09: ver estado de mis solicitudes
    @GET("api/v1/credit-profiles/{id}/purchase-requests")
    suspend fun myPurchaseRequests(@Path("id") id: Long): Response<List<PurchaseRequestResponse>>

    // ===== Store Management (catálogo activo para US-08) =====
    @GET("api/v1/stores/{storeId}/products")
    suspend fun storeCatalog(@Path("storeId") storeId: Long): Response<List<ProductResponse>>

    // ===== Billing =====
    // US-10: desglose exigible (capital + interés + mora)
    @GET("api/v1/credit-profiles/{id}/payment-due")
    suspend fun paymentDue(
        @Path("id") id: Long,
        @Query("installmentIds") installmentIds: String
    ): Response<PaymentDueResponse>

    // US-10: pagar íntegro (el backend calcula el monto, no se manda)
    @POST("api/v1/credit-profiles/{id}/payments")
    suspend fun registerPayment(
        @Path("id") id: Long,
        @Body req: RegisterPaymentRequest
    ): Response<PaymentResponse>

    // Histórico de pagos del cliente
    @GET("api/v1/credit-profiles/{id}/payments")
    suspend fun paymentsByProfile(@Path("id") id: Long): Response<List<PaymentResponse>>

    // US-06: estados de cuenta mensuales
    @GET("api/v1/credit-profiles/{id}/account-statements")
    suspend fun statementsByProfile(@Path("id") id: Long): Response<List<AccountStatementResponse>>
}
