@file:UseSerializers(EduzzDateTimeSerializer::class, EduzzAmericanFmtDateSerializer::class)
@file:Suppress("PropertyName")

import kotlinx.serialization.ValidationException
import java.time.LocalDate

package com.aleques.eduzzApi

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.time.LocalDate
import java.util.*

@Serializable
@SerialName("EduzzInvoice")
data class EduzzInvoice(
    @SerialName("sale_id")
    @Required
    var sale_id: Long,
    var contract_id: Long? = null,
    var date_create: Date,
    var date_payment: Date? = null,
    var date_update: Date? = null,
    var date_credit: Date? = null,
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
    var sale_alumy_fee: Double? = null,
    var sale_total: Double? = null,
    var sale_total_interest: Double? = null,
    var refund_type: String? = null,
    var refund_date: Date? = null,
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
    var transaction_currency:  String? = null,
    var transaction_locale: String? = null,
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
    var invoice_item_discount_value: Double? = null,
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
) {
    fun validate() {
        if (data?.get("token").isNullOrEmpty()) {
            throw ValidationException("Invalid auth response: missing token")
        }
    }
}

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
) {
    fun validate() {
        if (data.isEmpty()) {
            throw ValidationException("Invalid user response: empty data")
        }
    }
}


@Serializable
data class EduzzGetInvoiceResponse(
    var success: Boolean,
    var data: List<EduzzInvoice>,
    var profile: EduzzProfile,
    var paginator: Map<String, Int?>? = emptyMap()
) {
    fun validate() {
        if (data.any { it.sale_id <= 0 }) {
            throw ValidationException("Invalid invoice response: invalid sale_id")
        }
    }
}

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
) {
    fun validate() {
        if (data.document_id == null || data.document_id!! <= 0) {
            throw ValidationException("Invalid tax doc response: invalid document_id")
        }
    }
}

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
) {
    fun validate() {
        if (data.any { it.date > LocalDate.now() }) {
            throw ValidationException("Invalid last days amount response: future date")
        }
    }
}


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
) {
    fun validate() {
        if (data.any { it.statement_value < 0 }) {
            throw ValidationException("Invalid financial statement response: negative value")
        }
    }
}
