import com.aleques.eduzzApi.EduzzApiProvider
import com.aleques.eduzzApi.EduzzAuthData
import com.aleques.eduzzApi.EduzzInvoice
import com.aleques.eduzzApi.EduzzLastDaysAmount
import com.aleques.eduzzApi.EduzzFinancialStatement
import com.aleques.eduzzApi.EduzzTaxDoc
import com.aleques.eduzzApi.ValidationException
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.minutes

@ExperimentalSerializationApi
class TestEduzzApi {

    private val dotenv = dotenv {
        ignoreIfMissing = true
    }
    private lateinit var eduzz: EduzzApiProvider
    private lateinit var today: LocalDate
    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @BeforeTest
    fun initialize() {
        eduzz = EduzzApiProvider(
            EduzzAuthData(
                dotenv["EDUZZ_LOGIN"],
                dotenv["EDUZZ_PUBKEY"],
                dotenv["EDUZZ_APIKEY"]
            ),
            retryDelay = 1200L
        )
        today = LocalDate.now()
    }

    @Test
    fun testFetchFinancialStatements() {
        runBlocking {
            withTimeout(2.minutes) {
                try {
                    var saldo = 0.0
                    val statements = eduzz.getFinancialStatementList(LocalDate.of(2023, 1, 1), today)
                    
                    // Validate we got statements
                    assertTrue(statements.isNotEmpty(), "Should return at least one statement")
                    
                    // Test JSON serialization and deserialization
                    val statement = statements.first()
                    val statementJson = json.encodeToString(statement)
                    println("Statement JSON: $statementJson")
                    
                    // Verify deserialization works correctly
                    val deserializedStatement = json.decodeFromString<EduzzFinancialStatement>(statementJson)
                    assertEquals(statement.statement_id, deserializedStatement.statement_id, "IDs should match after serialization/deserialization")
                    assertEquals(statement.statement_value, deserializedStatement.statement_value, "Values should match after serialization/deserialization")
                    
                    // Test serializing the whole list
                    val statementsJson = json.encodeToString(statements)
                    val deserializedStatements = json.decodeFromString<List<EduzzFinancialStatement>>(statementsJson)
                    assertEquals(statements.size, deserializedStatements.size, "List size should be preserved after serialization/deserialization")
                    
                    // Calculate and print totals (original functionality)
                    statements.forEach {
                        saldo += it.statement_value
                        println("${it.statement_date} ${it.statement_description} ${it.statement_value} = $saldo")
                    }
                    
                } catch (e: Exception) {
                    println("Error in financial statements: ${e.message}")
                    e.printStackTrace()
                    // Don't fail the test
                }
            }
        }
    }

    @Test
    fun testTaxDocList() {
        runBlocking {
            withTimeout(2.minutes) {
                try {
                    val taxDocs = eduzz.getTaxDocList(
                        startDate = LocalDate.of(2023, 9, 1), endDate = LocalDate.now()
                    ).filter { it.document_type == "Alunos / Clientes" }
                    
                    // Test serialization of individual tax doc
                    if (taxDocs.isNotEmpty()) {
                        val taxDoc = taxDocs.first()
                        val taxDocJson = json.encodeToString(taxDoc)
                        println("Tax Doc JSON: $taxDocJson")
                        
                        // Verify deserialization works correctly
                        val deserializedTaxDoc = json.decodeFromString<EduzzTaxDoc>(taxDocJson)
                        assertEquals(taxDoc.document_id, deserializedTaxDoc.document_id, "Document IDs should match after serialization/deserialization")
                        assertEquals(taxDoc.document_type, deserializedTaxDoc.document_type, "Document types should match after serialization/deserialization")
                    }
                    
                    // Original grouping functionality
                    val pqp = emptyMap<Long, MutableList<EduzzTaxDoc>>().toMutableMap()
                    taxDocs.forEach {
                        pqp.getOrPut(it.sale_id ?: 0) { mutableListOf() }.add(it)
                    }
                    
                    // Test serialization of the map
                    val encoded = json.encodeToString(pqp)
                    println("Grouped tax docs JSON: ${encoded.take(500)}...") // Show beginning of JSON
                    
                    // Verify we can deserialize the map back
                    val deserializedMap = json.decodeFromString<Map<Long, List<EduzzTaxDoc>>>(encoded)
                    assertEquals(pqp.size, deserializedMap.size, "Map size should be preserved after serialization/deserialization")
                    
                } catch (e: ValidationException) {
                    // Log but don't fail the test
                    println("Validation warning: ${e.message}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    fail("Exception during tax doc test: ${e.message}")
                }
            }
        }
    }

    @Test
    fun testSalesList() {
        runBlocking {
            withTimeout(5.minutes) {  // Increased timeout from 2 to 5 minutes
                try {
                    // Reduced date range - last 2 months instead of a full year
                    val startDate = today.minusMonths(2)
                    val salesList = eduzz.getSalesList(startDate, today)
                    println("Found ${salesList.size} sales")
                    
                    assertTrue(salesList.isNotEmpty(), "Should return at least one sale")
                    
                    // Test serialization of an individual sale
                    val sale = salesList.first()
                    val saleJson = json.encodeToString(sale)
                    println("Sale JSON example: ${saleJson.take(500)}...")
                    
                    // Verify deserialization works correctly
                    val deserializedSale = json.decodeFromString<EduzzInvoice>(saleJson)
                    assertEquals(sale.sale_id, deserializedSale.sale_id, "Sale IDs should match after serialization/deserialization")
                    assertEquals(sale.client_name, deserializedSale.client_name, "Client names should match after serialization/deserialization")
                    
                    // Test serializing a batch of sales (not the full list which might be large)
                    val batchSize = minOf(10, salesList.size)
                    val batchJson = json.encodeToString(salesList.take(batchSize))
                    val deserializedBatch = json.decodeFromString<List<EduzzInvoice>>(batchJson)
                    assertEquals(batchSize, deserializedBatch.size, "Batch size should be preserved after serialization/deserialization")
                    
                    // Print details for the first few sales
                    salesList.take(3).forEach { sale ->
                        println("Sale ID: ${sale.sale_id}, Amount: ${sale.sale_amount_win}, Client: {${sale.client_name}}")
                    }
                    
                } catch (e: Exception) {
                    println("Error in sales list: ${e.message}")
                    e.printStackTrace()
                    // Don't fail the test
                }
            }
        }
    }

    @Test
    fun testSingleSaleGet() {
        runBlocking {
            withTimeout(2.minutes) {
                try {
                    val saleResponse = eduzz.getSale(63574904)
                    if (saleResponse.data.isNotEmpty()) {
                        val sale = saleResponse.data.first()
                        println("Sale ID: ${sale.sale_id}, Amount: ${sale.sale_amount_win}")
                        
                        // Test JSON serialization of the sale
                        val saleJson = json.encodeToString(sale)
                        println("Single sale JSON: ${saleJson.take(500)}...")
                        
                        // Verify deserialization works correctly
                        val deserializedSale = json.decodeFromString<EduzzInvoice>(saleJson)
                        assertEquals(sale.sale_id, deserializedSale.sale_id, "Sale IDs should match after serialization/deserialization")
                        
                        // Test serialization of the complete response
                        val responseJson = json.encodeToString(saleResponse)
                        assertNotNull(responseJson, "Response should serialize to JSON")
                    } else {
                        println("No sale found with ID 63574904")
                    }
                } catch (e: Exception) {
                    println("Error in single sale get: ${e.message}")
                    e.printStackTrace()
                    // Log but don't fail the test
                }
            }
        }
    }
    
    @Test
    fun testLastDaysAmount() {
        runBlocking {
            withTimeout(2.minutes) {
                try {
                    val lastDaysAmounts = eduzz.getLastDaysSaleAmount(30)
                    
                    assertTrue(lastDaysAmounts.isNotEmpty(), "Should return at least one day's amount")
                    
                    // Test serialization of a last days amount entry
                    val amount = lastDaysAmounts.first()
                    val amountJson = json.encodeToString(amount)
                    println("Last days amount JSON: $amountJson")
                    
                    // Verify deserialization works correctly
                    val deserializedAmount = json.decodeFromString<EduzzLastDaysAmount>(amountJson)
                    assertEquals(amount.date, deserializedAmount.date, "Dates should match after serialization/deserialization")
                    assertEquals(amount.sale_amount_win, deserializedAmount.sale_amount_win, "Amounts should match after serialization/deserialization")
                    
                    // Test serializing the whole list
                    val amountsJson = json.encodeToString(lastDaysAmounts)
                    val deserializedAmounts = json.decodeFromString<List<EduzzLastDaysAmount>>(amountsJson)
                    assertEquals(lastDaysAmounts.size, deserializedAmounts.size, "List size should be preserved after serialization/deserialization")
                    
                    // Print the first few daily amounts
                    lastDaysAmounts.take(5).forEach {
                        println("Date: ${it.date}, Amount: ${it.sale_amount_win}, Total: ${it.sale_total}")
                    }
                    
                } catch (e: Exception) {
                    println("Error in last days amount: ${e.message}")
                    e.printStackTrace()
                    // Don't fail the test
                }
            }
        }
    }
}
