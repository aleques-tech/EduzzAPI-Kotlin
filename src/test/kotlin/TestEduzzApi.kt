import com.aleques.eduzzApi.EduzzApiProvider
import com.aleques.eduzzApi.EduzzAuthData
import com.aleques.eduzzApi.EduzzTaxDoc
import com.aleques.eduzzApi.util.defaultEduzzApiHttpClientBuilder
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.logging.HttpLoggingInterceptor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.BeforeTest
import kotlin.test.Test


@ExperimentalSerializationApi
class TestEduzzApi {

    private val dotenv = dotenv {
        ignoreIfMissing = true
    }
    private lateinit var eduzz: EduzzApiProvider
    private lateinit var today: String

    @BeforeTest
    fun initialize() {
        eduzz = EduzzApiProvider(
            EduzzAuthData(
                dotenv["EDUZZ_LOGIN"], dotenv["EDUZZ_PUBKEY"], dotenv["EDUZZ_APIKEY"]
            ),
            defaultEduzzApiHttpClientBuilder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
        )
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val now = LocalDate.now()
        today = now.format(dateFormat)
    }

    @Test
    fun testFetchFinancialStatements() = runBlocking {
        var saldo = 0.0
        eduzz.getFinancialStatementList("2018-01-01", today).forEach {
            saldo += it.statement_value
            println("${it.statement_date} ${it.statement_description} ${it.statement_value} = $saldo")
        }

    }

    @Test
    fun testTaxDocList() =
        runBlocking {
            val taxDoc = eduzz.getTaxDocList(
                startDate = LocalDate.of(2023, 9, 1), endDate = LocalDate.now()
            ).filter { it.document_type == "Alunos / Clientes" }
            val pqp = emptyMap<Long, MutableList<EduzzTaxDoc>>().toMutableMap()
            taxDoc.forEach {
                pqp.getOrPut(it.sale_id!!) { mutableListOf() }.add(it)
            }
            val encoded = Json.encodeToString(pqp)
            println(encoded)
        }

    @Test
    fun testSalesList() = runBlocking {
        val salesList = eduzz.getSalesList("2023-09-01", today)

        val encoded = Json.encodeToString(salesList)
        println(encoded)
    }

    @Test
    fun testSingleSaleGet() = runBlocking {
        val sale = eduzz.getSale(63574904)
        println(sale)
    }
}

