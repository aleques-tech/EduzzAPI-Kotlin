@file:UseSerializers(EduzzDateTimeSerializer::class, EduzzAmericanFmtDateSerializer::class)
@file:Suppress("PropertyName")

package com.aleques.eduzzApi


import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.delay
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.http.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import java.util.concurrent.TimeUnit


private object EduzzAmericanFmtDateSerializer : KSerializer<LocalDate> {

    private val inputFmt = DateTimeFormatter.ofPattern("yyyy[-][/]MM[-][/]dd")
    override val descriptor
        get() = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString(), inputFmt)
    }

    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.format(inputFmt))
}

object EduzzBrazilianFmtDateSerializer : KSerializer<Date> {
    private val inputFmt = SimpleDateFormat("dd/MM/yyyy")
    override val descriptor
        get() = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Date {
        return inputFmt.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Date) = encoder.encodeString(inputFmt.format(value))
}

object EduzzDateTimeSerializer : KSerializer<Date> {

    //    private val inputFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd[[ ]['T']HH:mm[:ss][X][XXX][X]]")
    private val inputFormat = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC"))
    private val outputFormat = inputFormat
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("ZonedDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Date {
        val originStr = decoder.decodeString()
        val str = originStr.replace(" ", "T")

        val parsedStr = try {
            inputFormat.parse(str)
        } catch (ex: DateTimeParseException) {
            inputFormat.parse(Instant.parse(str.plus("Z")).toString())
        }

        return Date.from(Instant.from(parsedStr))

    }

    override fun serialize(encoder: Encoder, value: Date) = encoder.encodeString(
        outputFormat.format(value.toInstant())
    )
}

/*
object GsonEduzzTypeAdapter : TypeAdapter<LocalDateTime?>() {
    override fun write(p0: JsonWriter, p1:Date?) {
        if (p1 == null) {
            p0.nullValue()
        } else {
            p0.value(p1.format(EduzzDateSerializer.outputFmt))
        }
    }

    override fun read(p0: JsonReader):Date? {
        return when (p0.peek()) {
            NULL -> {
                p0.nextNull()
                null
            }
            else -> {
                val str = p0.nextString()
                LocalDateTime.parse(str, EduzzDateSerializer.inputFmt)
            }
        }
    }


}
*/

@Serializable
data class EduzzInvoice(
    var sale_id: Long,
    var contract_id: Long? = null,
    var date_create: Date,
    var date_payment: Date? = null,
    var date_update: Date? = null,
    var sale_status: Int,
    var sale_status_name: String? = null,
    var sale_item_id: Long? = null,
    var sale_item_discount: Double? = null,
    var client_first_access_id: String? = null,
    var sale_item_discount_coupon_key: String? = null,
    var sale_recuperacao: Int,
    var sale_funil_infinito: Int?,
    var sale_amount_win: Double,
    var sale_net_gain: Double? = null,
    var sale_coop: Double? = null,
    var sale_partner: Double? = null,
    var sale_fee: Double? = null,
    var sale_others: Double? = null,
    var sale_total: Double? = null,
    var refund_type: String? = null,
    var refund_value: Double? = null,
    var refund_partial_value: Double? = null,
    var sale_payment_method: String? = null,
    var client_id: Long,
    var client_name: String,
    var client_email: String,
    var client_document: String? = null,
    var client_street: String? = null,
    var client_addressnumber: String? = null,
    var client_complement: String? = null,
    var client_neighborhood: String? = null,
    var client_zip_code: String? = null,
    var client_city: String? = null,
    var client_district: String? = null,
    var client_telephone: String? = null,
    var client_telephone2: String? = null,
    var client_cel: String,
    var producer_id: Long,
    var affiliate_id: Long? = null,
    var producer_name: String,
    var affiliate_name: String? = null,
    var utm_source: String? = null,
    var utm_campaign: String? = null,
    var utm_medium: String? = null,
    var utm_content: String? = null,
    var tracker: String? = null,
    var tracker2: String? = null,
    var tracker3: String? = null,
    var content_id: Long? = null,
    var content_title: String? = null,
    var content_type_id: Int? = null,
    var sku: String? = null,
    var installments: Int? = 1,
    var student_name: String? = null,
    var student_cel: String? = null,
    var student_email: String? = null,
    var student_document: String? = null,
    var card_number: String? = null,
    var due_date: String? = null,
    var invoice_items: List<EduzzInvoiceItem>? = null,
    var sale_coop_detail: List<EduzzSaleCoopDetail>? = null,
    var recipient: EduzzRecipient? = null,
    var sale_affiliate_detail: List<EduzzSaleAffiliateDetail>? = null

)

@Serializable
data class EduzzRecipient(
    var recipient_id: Long,
    var recipient_name: String,
    var recipient_email: String,
    var recipient_document: String,
    var recipient_cel: String
)

@Serializable
data class EduzzSaleCoopDetail(
    var coproducer_id: Long,
    var coproducer_email: String,
    var coproducer_name: String,
    var sale_net_gain: Double? = null,
    var is_affiliate_manager: Int
)

@Serializable
data class EduzzSaleAffiliateDetail(
    var affiliate_id: Long?, var affiliate_email: String?, var affiliate_name: String?, var sale_net_gain: Double?
)

@Serializable
data class EduzzInvoiceItem(
    var invoice_item_id: Long,
    var invoice_item_description: String,
    var invoice_item_unit_value: Double,
    var invoice_item_quantity: Double,
    var invoice_item_value: Double,
    var invoice_item_discount_value: Double,
    var invoice_item_freight_type: String? = null,
    var invoice_item_freight_deadline: String? = null,
    var invoice_item_content_id: Long,
    var tracking_code: String? = null
)

@Serializable
data class EduzzProfile(
    var start: Double,
    var token: String? = null,
    var token_valid_until: Date? = null,
    var finish: Double,
    var process: Double
)


@Serializable
data class EduzzAuthResponse(
    var success: Boolean, var data: Map<String, String?>?, var profile: EduzzProfile
)

@Serializable
data class EduzzUserInfo(
    var user_id: Long,
    var company_name: String? = null,
    var fantasy: String? = null,
    var taxid: String? = null,
    var api_key: String? = null,
    var date_user_create: Date? = null,
    var email: String? = null,
    var telephone: String? = null,
    var telephone2: String? = null,
    var cellphone: String? = null,
    var address_street: String? = null,
    var address_number: String? = null,
    var address_complement: String? = null,
    var address_zip: String? = null,
    var address_district: String? = null,
    var about: String? = null,
    var photo: String? = null,
    var contact_phone: String? = null,
    var contact_email: String? = null,
    var contact_facebook: String? = null,
    var contact_skype: String? = null,
    var vip: Int? = null,
    var is_producer: Int? = null,
    var anticipation_in_days: Int? = null,
    var ativodistrib: Int? = null,
    var is_elite: Int? = null,
    var elite_id: Int? = null,
    var elite_belt: String? = null
)

@Serializable
data class EduzzGetUserResponse(
    var success: Boolean,
    var data: List<EduzzUserInfo>,
    var profile: EduzzProfile,
    var paginator: Map<String, String?>? = emptyMap()
)


@Serializable
data class EduzzGetInvoiceResponse(
    var success: Boolean,
    var data: List<EduzzInvoice>,
    var profile: EduzzProfile,
    var paginator: Map<String, Int?>? = emptyMap()
)

@Serializable
data class EduzzTaxDoc(
    var document_id: Long?,
    var document_name: String?,
    var document_status: String?,
    var document_type: String?,
    var document_basevalue: Double?,
    var document_emissiondate: Date?,
    var document_referencedate: Date?,
    var document_processingdate: Date?,
    var document_file_status: Double?,
    var document_auxvalue1: String?,
    var document_auxvalue2: String?,
    var document_auxvalue3: String?,
    var document_auxvalue4: String?,
    var document_auxvalue5: String?,
    var document_auxparam1: String?,
    var document_auxparam2: String?,
    var document_auxparam3: String?,
    var document_auxparam4: String?,
    var document_auxparam5: String?,
    var document_taxbase: String?,
    var document_taxbasepercent: String?,
    var document_taxvalue: String?,
    var source_taxtype: String?,
    var source_company_name: String?,
    var source_document: String?,
    var source_ie: String?,
    var source_im: String?,
    var source_email: String?,
    var source_street: String?,
    var source_number: String?,
    var source_complement: String?,
    var source_district: String?,
    var source_zipcode: String?,
    var source_city: String?,
    var source_uf: String?,
    var source_tel: String?,
    var source_tel2: String?,
    var source_cell: String?,
    var destination_company_name: String?,
    var destination_taxtype: String?,
    var destination_taxid: String?,
    var destination_ie: String?,
    var destination_im: String?,
    var destination_email: String?,
    var destination_street: String?,
    var destination_number: String?,
    var destination_complement: String?,
    var destination_district: String?,
    var destination_zipcode: String?,
    var destination_city: String?,
    var destination_country_foreing: String?, /// <- misspelling in the original api :-P
    var destination_country: String?,
    var destination_uf: String?,
    var destination_tel: String?,
    var destination_tel2: String?,
    var destination_cell: String?,
    var sale_id: Long? = null,
    var date_payment: Date? = null,
    var client_id: Long? = null,
    var client_name: String? = null,
    var client_document: String? = null,
    var client_email: String? = null,
    var client_street: String? = null,
    var client_addressnumber: String? = null,
    var client_complement: String? = null,
    var client_neighborhood: String? = null,
    var client_zip_code: String? = null,
    var client_telephone: String? = null,
    var client_telephone2: String? = null,
    var client_cellphone: String? = null,
    var client_city: String? = null,
    var client_state: String? = null,
    var content_id: Long? = null,
    var content_title: String? = null,
    var generated_by_new_system: Boolean? = null,
    var sale_total: Double? = null,
    var document_items: List<EduzzTaxDocumentItem>? = null
)

@Serializable
data class EduzzGetTaxDocListResponse(
    var success: Boolean,
    var data: List<EduzzTaxDoc>,
    var profile: EduzzProfile,
    var paginator: Map<String, Int?>? = null
)

@Serializable
data class EduzzGetTaxDocResponse(
    var success: Boolean, var data: EduzzTaxDoc, var profile: EduzzProfile, var paginator: Map<String, Int?>? = null
)

@Serializable
data class EduzzTaxDocumentItem(
    var docitem_id: Long?,
    var docitem_content_id: Long?,
    var docitem_description: String?,
    var docitem_taxprodservcode: String?,
    var docitem_value: Double?,
    var docitem_auxaparam1: String?,
    var docitem_auxaparam2: String?,
    var docitem_tipofrete: String?,
    var docitem_qtde: Double?,
    var docitem_sale_id: Long?
)

@Serializable
data class EduzzLastDaysAmount(
    var date: LocalDate,
    var sale_discount: Double,
    var sale_amount_win: Double,
    var sale_net_gain: Double,
    var sale_coop: Double,
    var sale_fee: Double,
    var sale_others: Double,
    var sale_total: Double
)

@Serializable
data class EduzzLastDaysAmountResponse(
    var success: Boolean, var data: List<EduzzLastDaysAmount>, var profile: EduzzProfile
)


@Serializable
data class EduzzFinancialStatement(
    var statement_id: Long,
    @Serializable(EduzzBrazilianFmtDateSerializer::class) var statement_date: Date,
    var statement_description: String,
    var statement_document: String?,
    var statement_value: Double
)

@Serializable
data class EduzzFinancialStatementResponse(
    var success: Boolean,
    var data: List<EduzzFinancialStatement>,
    var profile: EduzzProfile,
    var paginator: Map<String, Int?>? = emptyMap()
)

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
        @Query("start_date") sd: String,
        @Query("end_date") ed: String,
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
        @Query("start_date") sd: String,
        @Query("end_date") ed: String,
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
    private val httpClient: OkHttpClient = OkHttpClient.Builder().connectTimeout(50, TimeUnit.SECONDS)
        .writeTimeout(50, TimeUnit.SECONDS).readTimeout(50, TimeUnit.SECONDS).build()
) {
    companion object {
        private const val EDUZZBASEURL = "https://api2.eduzz.com/"
        private val contentType = "application/json; charset=utf-8".toMediaType()
    }

    //    val gson = GsonBuilder()
//        .registerTypeAdapter(LocalDateTime::class.java,  GsonEduzzTypeAdapter)
//        .create()
    private fun getInterceptor(builder: Retrofit.Builder): Retrofit.Builder {
        return builder.client(httpClient)
    }

    private val json = Json { isLenient = false; ignoreUnknownKeys = false }

    private val retrofit = getInterceptor(
        Retrofit.Builder().baseUrl(EDUZZBASEURL)
//        .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(json.asConverterFactory(contentType))
    ).build()
    private val service = retrofit.create(EduzzService::class.java)
    private var authToken: String? = null

    private suspend fun <T> eduzzSvcRetry(service: suspend () -> T): T {
        while (true) {
            try {
                return service()
            } catch (e: HttpException) {
                val code = e.code()
                if (code == 500 || code == 429) {
                    delay(600)
                    continue
                }
                throw e
            }
        }
    }

    private suspend fun authenticate(): Boolean {
        val (email, publicKey, apiKey) = eduzzAuthProvider
        val reply = eduzzSvcRetry { service.authenticate(email, publicKey, apiKey) }
        //println("status: ${reply.isSuccessful} ${reply.body()}")
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
        startDate: String,
        endDate: String,
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

    suspend fun getFinancialStatementList(startDate: String, endDate: String): List<EduzzFinancialStatement> {
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
