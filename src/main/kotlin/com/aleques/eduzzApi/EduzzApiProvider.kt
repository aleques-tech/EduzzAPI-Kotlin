@file:Suppress("PropertyName")

package com.aleques.eduzzApi


import com.aleques.eduzzApi.util.defaultEduzzApiHttpClientBuilder
import com.aleques.eduzzApi.util.eduzzSvcRetry
import com.aleques.eduzzApi.util.setHttpClient
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.*
import java.time.LocalDate


interface EduzzService {
    @FormUrlEncoded
    @POST("credential/generate_token")
    suspend fun authenticate(
        @Field("email") email: String, @Field("publickey") publickey: String, @Field("apikey") apikey: String
    ): EduzzAuthResponse

    @GET("/user/get_me")
    suspend fun getMe(@Header("token") token: String?): EduzzGetUserResponse

    @GET("/sale/get_sale_list")
    suspend fun listSales(
        @Header("token") token: String?,
        @Query("start_date") startDate: LocalDate? = null,
        @Query("end_date") endDate: LocalDate? = null,
        @Query("page") page: Int?,
        @Query("contract_id") cId: Int? = null,
        @Query("affiliate_id") affId: Int? = null,
        @Query("content_id") contId: Int? = null,
        @Query("invoice_status") invStatus: Int? = null,
        @Query("client_email") email: String? = null,
        @Query("client_document") cliDoc: String? = null,
        @Query("date_type") dateType: String? = null,
    ): EduzzGetInvoiceResponse

    @GET("/sale/last_days_amount")
    suspend fun getLastDaysAmount(
        @Header("token") token: String?, @Query("days") howManyDays: Int?
    ): EduzzLastDaysAmountResponse

    @GET("/financial/statement")
    suspend fun getFinancialStatement(
        @Header("token") token: String?,
        @Query("start_date") sd: LocalDate,
        @Query("end_date") ed: LocalDate,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int? = 100
    ): EduzzFinancialStatementResponse

    @GET("/sale/get_sale/{id}")
    suspend fun getSale(@Header("token") token: String?, @Path("id") saleId: Long): EduzzGetInvoiceResponse


    @GET("/fiscal/get_taxdocument/{id}")
    suspend fun getTaxDocument(@Header("token") token: String?, @Path("id") saleId: Long): EduzzGetTaxDocResponse

    @GET("/fiscal/get_taxdocumentlist")
    suspend fun getTaxDocumentList(
        @Header("token") token: String?,
        @Query("start_date") startDate: LocalDate? = null,
        @Query("end_date") endDate: LocalDate? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = 100,
        @Query("document_status") documentStatus: String? = null,
        @Query("name") name: String? = null,
        @Query("email") email: String? = null,
        @Query("sale_id") saleId: Long? = null
    ): EduzzGetTaxDocListResponse
}

@Suppress("unused")
@ExperimentalSerializationApi
class EduzzApiProvider(
    private val eduzzAuthProvider: EduzzAuthData,
    httpClient: OkHttpClient = defaultEduzzApiHttpClientBuilder.build()
) {
    companion object {
        private const val EDUZZBASEURL = "https://api2.eduzz.com/"
        private val contentType = "application/json; charset=utf-8".toMediaType()
    }

    //    val gson = GsonBuilder()
//        .registerTypeAdapter(LocalDateTime::class.java,  GsonEduzzTypeAdapter)
//        .create()

    private val json = Json { isLenient = false; ignoreUnknownKeys = false }

    private val retrofit = setHttpClient(
        Retrofit.Builder().baseUrl(EDUZZBASEURL)
//        .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(json.asConverterFactory(contentType)),
        httpClient
    ).build()
    private val service = retrofit.create(EduzzService::class.java)
    private var authToken: String? = null


    private suspend fun authenticate(): Boolean {
        val (email, publicKey, apiKey) = eduzzAuthProvider
        val reply = eduzzSvcRetry { service.authenticate(email, publicKey, apiKey) }
        //println("status: ${reply.isSuccessful} ${reply.body()}")
        authToken = reply.data?.get("token")
//        println(authToken)
        return true
    }

    private suspend fun checkAuth() {
        if (authToken.isNullOrBlank()) {
            authenticate()
        }
    }


    suspend fun getLastDaysSaleAmount(days: Int? = null): List<EduzzLastDaysAmount> {
        checkAuth()
        val r = eduzzSvcRetry { service.getLastDaysAmount(authToken, days) }
        return r.data
    }

    suspend fun getSale(id: Long): EduzzGetInvoiceResponse {
        checkAuth()
        return eduzzSvcRetry { service.getSale(authToken, id) }
    }

    suspend fun getOwnUserInfo(): EduzzUserInfo {
        checkAuth()
        return eduzzSvcRetry { service.getMe(authToken).data.first() }
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
            val r = eduzzSvcRetry {
                service.listSales(
                    authToken,
                    startDate,
                    endDate,
                    page,
                    contractId,
                    affiliateId,
                    contentId,
                    invoiceStatus,
                    clientEmail,
                    clientDocument,
                    dateType
                )
            }

            page++
            retVal += r.data
            if (r.paginator.isNullOrEmpty() || page > (r.paginator!!.getOrDefault("totalPages", 1) ?: 1)) done =
                true
        } while (!done)
        return retVal
    }

    suspend fun getFinancialStatementList(startDate: LocalDate, endDate: LocalDate): List<EduzzFinancialStatement> {
        checkAuth()
        var done = false
        var page = 1
        val retVal = emptyList<EduzzFinancialStatement>().toMutableList()
        do {
            val r = eduzzSvcRetry { service.getFinancialStatement(authToken, startDate, endDate, page) }
            page++
            retVal += r.data
            if (r.paginator.isNullOrEmpty() || page > (r.paginator!!.getOrDefault("totalPages", 1) ?: 1)) done = true
        } while (!done)
        return retVal
    }

    suspend fun getTaxDoc(id: Long): EduzzGetTaxDocResponse {
        checkAuth()
        return eduzzSvcRetry { service.getTaxDocument(authToken, id) }
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
            val r = eduzzSvcRetry {
                service.getTaxDocumentList(
                    authToken, startDate, endDate, page, 100, documentStatus, name, email, saleId
                )
            }
            page++
            retVal += r.data
            if (r.paginator.isNullOrEmpty() || page > (r.paginator!!.getOrDefault("totalPages", 1) ?: 1)) {
                done = true
            }
        } while (!done)
        return retVal
    }
}
