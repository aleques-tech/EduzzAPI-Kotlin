package com.aleques.eduzzApi

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

@file:JvmName("EduzzSerializers")

package com.aleques.eduzzApi

object EduzzAmericanFmtDateSerializer : KSerializer<LocalDate> {

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
