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

    @BeforeTest
    fun initialize() {
        eduzz = EduzzApiProvider(
            EduzzAuthData(
                dotenv["EDUZZ_LOGIN"], dotenv["EDUZZ_PUBKEY"], dotenv["EDUZZ_APIKEY"]
            )
        )
    }

    @Test
    fun testFetch() {
        runBlocking {
            val fin = eduzz.getFinancialStatement("2020-10-01", "2020-12-08")
            fin.forEach {
                println(Json.encodeToString(it))
            }
        }
    }

    @Test
    fun testTaxDocList() {
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
//            println(pqp)
        }
    }
}
