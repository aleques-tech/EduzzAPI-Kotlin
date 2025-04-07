@file:Suppress("PropertyName")

package com.aleques.eduzzApi


import com.aleques.eduzzApi.util.eduzzSvcRetry
import com.aleques.eduzzApi.util.vertxRequest
import io.vertx.core.http.HttpMethod.GET
import io.vertx.core.json.JsonObject
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

        val reply: EduzzAuthResponse = eduzzSvcRetry {
            val response = vertxRequest(
                method = io.vertx.core.http.HttpMethod.POST,
                url = "$EDUZZBASEURL/credential/generate_token",
                body = body
            )
            it.updateFromHeaders(response.headers)
            response
        }

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
        return eduzzSvcRetry {
            val response = vertxRequest<EduzzLastDaysAmountResponse>(
                method = GET,
                url = "$EDUZZBASEURL/sale/last_days_amount",
                headers = mapOf("token" to authToken!!),
                queryParams = listOf("days" to (days?.toString() ?: ""))
            )
            it.updateFromHeaders(response.headers)
            response.data
        }
    }

    suspend fun getSale(id: Long): EduzzGetInvoiceResponse {
        checkAuth()
        return try {
            eduzzSvcRetry {
                val response = vertxRequest<EduzzGetInvoiceResponse>(
                    method = GET,
                    url = "$EDUZZBASEURL/sale/get_sale/$id",
                    headers = mapOf("token" to authToken!!)
                )
                it.updateFromHeaders(response.headers)
                response
            }
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
        return eduzzSvcRetry {
            val response = vertxRequest<EduzzGetUserResponse>(
                method = GET,
                url = "$EDUZZBASEURL/user/get_me",
                headers = mapOf("token" to authToken!!)
            )
            it.updateFromHeaders(response.headers)
            response.data.first()
        }
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
            val r: EduzzGetInvoiceResponse = eduzzSvcRetry {
                val response = vertxRequest(
                    method = GET,
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
                it.updateFromHeaders(response.headers)
                response
            }

            page++
            retVal += r.data
            if (r.paginator.isNullOrEmpty() || page > (r.paginator!!.getOrDefault(
                    "totalPages",
                    1
                ) ?: 1)
            ) {
                done = true
            }
        } while (!done)

        return retVal
    }

    suspend fun getFinancialStatementList(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<EduzzFinancialStatement> {
        checkAuth()
        var done = false
        var page = 1
        val retVal = emptyList<EduzzFinancialStatement>().toMutableList()

        do {
            val r: EduzzFinancialStatementResponse = eduzzSvcRetry {
                val response = vertxRequest(
                    method = GET,
                    url = "$EDUZZBASEURL/financial/statement",
                    headers = mapOf("token" to authToken!!),
                    queryParams = listOf(
                        "start_date" to startDate.toString(),
                        "end_date" to endDate.toString(),
                        "page" to page.toString()
                    )
                )
                it.updateFromHeaders(response.headers)
                response
            }

            page++
            retVal += r.data
            if (r.paginator.isNullOrEmpty() || page > (r.paginator!!.getOrDefault(
                    "totalPages",
                    1
                ) ?: 1)
            ) {
                done = true
            }
        } while (!done)

        return retVal
    }

    suspend fun getTaxDoc(id: Long): EduzzGetTaxDocResponse {
        checkAuth()
        return eduzzSvcRetry(retryDelay) {
            vertxRequest<EduzzGetTaxDocResponse>(
                method = GET,
                url = "$EDUZZBASEURL/fiscal/get_taxdocument/$id",
                headers = mapOf("token" to authToken!!)
            )
        }
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
            val r: EduzzGetTaxDocListResponse = eduzzSvcRetry {
                val response = vertxRequest(
                    method = GET,
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
                it.updateFromHeaders(response.headers)
                response
            }

            page++
            retVal += r.data
            if (r.paginator.isNullOrEmpty() || page > (r.paginator!!.getOrDefault(
                    "totalPages",
                    1
                ) ?: 1)
            ) {
                done = true
            }
        } while (!done)

        return retVal
    }
}
