@file:Suppress("PropertyName")

package com.aleques.eduzzApi


import com.aleques.eduzzApi.util.vertxHttpClient
import com.aleques.eduzzApi.util.vertxRequest
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.time.LocalDate

@Suppress("unused")
@ExperimentalSerializationApi
class EduzzApiProvider(
    private val eduzzAuthProvider: EduzzAuthData,
    private val retryDelay: Long = 1200L
) {
    companion object {
        private const val EDUZZBASEURL = "https://api2.eduzz.com/"
    }

    private val json = Json { 
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }
    private var authToken: String? = null

    private suspend fun authenticate(): Boolean {
        val (email, publicKey, apiKey) = eduzzAuthProvider
        val body = JsonObject()
            .put("email", email)
            .put("publickey", publicKey)
            .put("apikey", apiKey)
        
        val reply: EduzzAuthResponse = vertxRequest(
            method = io.vertx.core.http.HttpMethod.POST,
            url = "$EDUZZBASEURL/credential/generate_token",
            body = body
        )
        
        authToken = reply.data?.get("token")
        return true
    }

    private suspend fun checkAuth() {
        if (authToken.isNullOrBlank()) {
            authenticate()
        }
    }

    suspend fun getLastDaysSaleAmount(days: Int? = null): List<EduzzLastDaysAmount> {
        checkAuth()
        return vertxRequest<EduzzLastDaysAmountResponse>(
            method = io.vertx.core.http.HttpMethod.GET,
            url = "$EDUZZBASEURL/sale/last_days_amount",
            headers = mapOf("token" to authToken!!),
            queryParams = listOf("days" to (days?.toString() ?: ""))
        ).data
    }

    suspend fun getSale(id: Long): EduzzGetInvoiceResponse {
        checkAuth()
        return try {
            vertxRequest<EduzzGetInvoiceResponse>(
                method = io.vertx.core.http.HttpMethod.GET,
                url = "$EDUZZBASEURL/sale/get_sale/$id",
                headers = mapOf("token" to authToken!!)
            )
        } catch (e: Exception) {
            // Return empty response on error
            EduzzGetInvoiceResponse(
                success = false,
                data = emptyList(),
                profile = EduzzProfile(start = 0.0, finish = 0.0, process = 0.0)
            )
        }
    }

    suspend fun getOwnUserInfo(): EduzzUserInfo {
        checkAuth()
        return vertxRequest<EduzzGetUserResponse>(
            method = io.vertx.core.http.HttpMethod.GET,
            url = "$EDUZZBASEURL/user/get_me",
            headers = mapOf("token" to authToken!!)
        ).data.first()
    }

    suspend fun getSalesList(
        startDate: LocalDate,
        endDate: LocalDate,
        contractId: Int? = null,
        affiliateId: Int? = null,
        contentId: Int? = null,
        invoiceStatus: Int? = null,
        clientEmail: String? = null,
        clientDocument: String? = null,
        dateType: String? = null
    ): List<EduzzInvoice> {
        checkAuth()
        var done = false
        var page = 1
        val retVal = emptyList<EduzzInvoice>().toMutableList()
        
        do {
            val r: EduzzGetInvoiceResponse = vertxRequest(
                method = io.vertx.core.http.HttpMethod.GET,
                url = "$EDUZZBASEURL/sale/get_sale_list",
                headers = mapOf("token" to authToken!!),
                queryParams = listOf(
                    "start_date" to startDate.toString(),
                    "end_date" to endDate.toString(),
                    "page" to page.toString(),
                    "contract_id" to (contractId?.toString() ?: ""),
                    "affiliate_id" to (affiliateId?.toString() ?: ""),
                    "content_id" to (contentId?.toString() ?: ""),
                    "invoice_status" to (invoiceStatus?.toString() ?: ""),
                    "client_email" to (clientEmail ?: ""),
                    "client_document" to (clientDocument ?: ""),
                    "date_type" to (dateType ?: "")
                )
            )
            
            page++
            retVal += r.data
            if (r.paginator.isNullOrEmpty() || page > (r.paginator!!.getOrDefault("totalPages", 1) ?: 1)) {
                done = true
            }
        } while (!done)
        
        return retVal
    }

    suspend fun getFinancialStatementList(startDate: LocalDate, endDate: LocalDate): List<EduzzFinancialStatement> {
        checkAuth()
        var done = false
        var page = 1
        val retVal = emptyList<EduzzFinancialStatement>().toMutableList()
        
        do {
            val r: EduzzFinancialStatementResponse = vertxRequest(
                method = io.vertx.core.http.HttpMethod.GET,
                url = "$EDUZZBASEURL/financial/statement",
                headers = mapOf("token" to authToken!!),
                queryParams = listOf(
                    "start_date" to startDate.toString(),
                    "end_date" to endDate.toString(),
                    "page" to page.toString()
                )
            )
            
            page++
            retVal += r.data
            if (r.paginator.isNullOrEmpty() || page > (r.paginator!!.getOrDefault("totalPages", 1) ?: 1)) {
                done = true
            }
        } while (!done)
        
        return retVal
    }

    suspend fun getTaxDoc(id: Long): EduzzGetTaxDocResponse {
        checkAuth()
        return vertxRequest<EduzzGetTaxDocResponse>(
            method = io.vertx.core.http.HttpMethod.GET,
            url = "$EDUZZBASEURL/fiscal/get_taxdocument/$id",
            headers = mapOf("token" to authToken!!)
        )
    }

    suspend fun getTaxDocList(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        documentStatus: String? = null,
        name: String? = null,
        email: String? = null,
        saleId: Long? = null
    ): List<EduzzTaxDoc> {
        checkAuth()
        var done = false
        var page = 1
        val retVal = emptyList<EduzzTaxDoc>().toMutableList()
        
        do {
            val r: EduzzGetTaxDocListResponse = vertxRequest(
                method = io.vertx.core.http.HttpMethod.GET,
                url = "$EDUZZBASEURL/fiscal/get_taxdocumentlist",
                headers = mapOf("token" to authToken!!),
                queryParams = listOf(
                    "start_date" to (startDate?.toString() ?: ""),
                    "end_date" to (endDate?.toString() ?: ""),
                    "page" to page.toString(),
                    "per_page" to "100",
                    "document_status" to (documentStatus ?: ""),
                    "name" to (name ?: ""),
                    "email" to (email ?: ""),
                    "sale_id" to (saleId?.toString() ?: "")
                )
            )
            
            page++
            retVal += r.data
            if (r.paginator.isNullOrEmpty() || page > (r.paginator!!.getOrDefault("totalPages", 1) ?: 1)) {
                done = true
            }
        } while (!done)
        
        return retVal
    }
}
