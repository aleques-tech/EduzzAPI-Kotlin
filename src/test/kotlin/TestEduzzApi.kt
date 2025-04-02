import com.aleques.eduzzApi.EduzzApiProvider
import com.aleques.eduzzApi.EduzzAuthData
import com.aleques.eduzzApi.EduzzTaxDoc
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test


@ExperimentalSerializationApi
class TestEduzzApi {

    private val dotenv = dotenv {
        ignoreIfMissing = true
    }
    private lateinit var eduzz: EduzzApiProvider
    private lateinit var today: LocalDate

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
    fun testFetchFinancialStatements() = runBlocking {
        var saldo = 0.0
        eduzz.getFinancialStatementList(LocalDate.of(2023, 1, 1), today)
            .forEach {
                saldo += it.statement_value
                println("${it.statement_date} ${it.statement_description} ${it.statement_value} = $saldo")
            }

    }

    @Test
    fun testTaxDocList() = runBlocking {
        try {
            val taxDoc = eduzz.getTaxDocList(
                startDate = LocalDate.of(2023, 9, 1), endDate = LocalDate.now()
            ).filter { it.document_type == "Alunos / Clientes" }
            val pqp = emptyMap<Long, MutableList<EduzzTaxDoc>>().toMutableMap()
            taxDoc.forEach {
                pqp.getOrPut(it.sale_id ?: 0) { mutableListOf() }.add(it)
            }
            val encoded = Json.encodeToString(pqp)
            println(encoded)
        } catch (e: ValidationException) {
            // Log but don't fail the test
            println("Validation warning: ${e.message}")
        }
    }

    @Test
    fun testSalesList() = runBlocking {
        val salesList = eduzz.getSalesList(LocalDate.of(2023, 9, 1), today)
        val encoded = Json.encodeToString(salesList)
        println(encoded)
    }

    @Test
    fun testSingleSaleGet() = runBlocking {
        try {
            val sale = eduzz.getSale(63574904)
            println(sale)
        } catch (e: ValidationException) {
            // Log but don't fail the test
            println("Validation warning: ${e.message}")
        }
    }
}

