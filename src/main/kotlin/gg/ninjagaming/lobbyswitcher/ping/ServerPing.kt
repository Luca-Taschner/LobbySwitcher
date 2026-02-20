package gg.ninjagaming.lobbyswitcher.ping

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.logging.Level
import java.util.logging.Logger

class ServerPing {
    private var host: InetSocketAddress? = null
    private val timeout = 2000

    fun setAddress(host: InetSocketAddress) {
        this.host = host
    }

    fun fetchData(): DefaultResponse {
        val address = host ?: return DefaultResponse()

        return try {
            pingStatus(address, timeout)
        } catch (_: Throwable) {
            DefaultResponse()
        }
    }

    private fun pingStatus(address: InetSocketAddress, timeoutMs: Int): DefaultResponse {
        Socket().use { socket ->
            socket.soTimeout = timeoutMs
            socket.tcpNoDelay = true
            socket.connect(address, timeoutMs)

            DataOutputStream(socket.getOutputStream()).use { out ->
                DataInputStream(socket.getInputStream()).use { input ->

                    // 1) Handshake (state = STATUS)
                    val handshake = ByteArrayOutput().apply {
                        writeVarInt(0x00)
                        writeVarInt(0)
                        writeString(address.hostString)
                        writeUnsignedShort(address.port)
                        writeVarInt(1)
                    }.toByteArray()

                    writeFramedPacket(out, handshake)

                    // 2) Status Request
                    val statusRequest = ByteArrayOutput().apply {
                        writeVarInt(0x00) // packet id
                    }.toByteArray()

                    writeFramedPacket(out, statusRequest)

                    // 3) Read Status Response
                    val packetLength = readVarInt(input)
                    if (packetLength <= 0) return DefaultResponse()

                    val packetId = readVarInt(input)
                    if (packetId != 0x00) return DefaultResponse()

                    val jsonString = readString(input)

                    return parseAndPopulate(jsonString)
                }
            }
        }
    }

    private fun writeFramedPacket(out: DataOutputStream, payload: ByteArray) {
        val frame = ByteArrayOutput().apply {
            writeVarInt(payload.size)
            writeBytes(payload)
        }.toByteArray()

        out.write(frame)
        out.flush()
    }

    private fun parseAndPopulate(jsonString: String): DefaultResponse {
        val jsonObject: JsonObject = try {
            val element = JsonParser.parseString(jsonString)
            if (!element.isJsonObject) {
                Logger.getLogger(ServerPing::class.java.name)
                    .log(Level.SEVERE, "JSON response is not an object: {0}", element.toString())
                return DefaultResponse()
            }
            element.asJsonObject
        } catch (ex: JsonParseException) {
            Logger.getLogger(ServerPing::class.java.name).log(Level.SEVERE, "Invalid JSON response", ex)
            return DefaultResponse()
        }

        return DefaultResponse().also { response ->
            populateResponseData(response, jsonObject)
        }
    }

    private fun populateResponseData(response: DefaultResponse, root: JsonObject) {
        response.description = root.get("description")
            ?.let { parseDescription(it) }
            .orEmpty()

        response.favicon = root.get("favicon")?.takeIf { it.isJsonPrimitive }?.asString

        val playersObj = root.getAsJsonObject("players")
        response.players = playersObj?.get("online")?.asIntOrNull() ?: 0
        response.maxPlayers = playersObj?.get("max")?.asIntOrNull() ?: 0

        val versionObj = root.getAsJsonObject("version")
        response.version = versionObj?.get("name")?.takeIf { it.isJsonPrimitive }?.asString
        response.protocol = versionObj?.get("protocol")?.asStringOrNumberToString()

        response.time = root.get("time")?.asIntOrNull() ?: -1
    }

    private fun parseDescription(element: JsonElement): String? {
        return when {
            element.isJsonPrimitive -> element.asString
            element.isJsonObject -> {
                val obj = element.asJsonObject
                obj.get("text")?.takeIf { it.isJsonPrimitive }?.asString
                    ?: obj.toString()
            }
            else -> null
        }
    }

    private fun JsonElement.asIntOrNull(): Int? =
        runCatching { if (isJsonPrimitive) asInt else null }.getOrNull()

    private fun JsonElement.asStringOrNumberToString(): String? =
        runCatching {
            if (!isJsonPrimitive) return@runCatching null
            val p = asJsonPrimitive
            when {
                p.isString -> p.asString
                p.isNumber -> p.asNumber.toString()
                else -> null
            }
        }.getOrNull()

    class DefaultResponse {
        var description: String = ""
        var version: String? = null
        var protocol: String? = null
        var favicon: String? = null
        var players: Int = 0
        var maxPlayers: Int = 0
        var time: Int = 0
    }

    @Throws(IOException::class)
    fun readVarInt(`in`: DataInputStream): Int {
        var i = 0
        var j = 0
        while (true) {
            val k = `in`.readByte().toInt()
            i = i or ((k and 0x7F) shl j++ * 7)
            if (j > 5) throw RuntimeException("VarInt too big")
            if ((k and 0x80) != 128) break
        }
        return i
    }

    private fun readString(input: DataInputStream): String {
        val len = readVarInt(input)
        if (len !in 0..32767) throw IOException("Invalid string length: $len")
        val bytes = ByteArray(len)
        input.readFully(bytes)
        return String(bytes, StandardCharsets.UTF_8)
    }

    private class ByteArrayOutput {
        private val byteStream = java.io.ByteArrayOutputStream()
        private val out = DataOutputStream(byteStream)

        fun writeVarInt(value: Int) {
            var v = value
            while (true) {
                if ((v and -0x80) == 0) {
                    out.writeByte(v)
                    return
                }
                out.writeByte(v and 0x7F or 0x80)
                v = v ushr 7
            }
        }

        fun writeString(s: String) {
            val bytes = s.toByteArray(StandardCharsets.UTF_8)
            writeVarInt(bytes.size)
            out.write(bytes)
        }

        fun writeUnsignedShort(port: Int) {
            out.writeShort(port and 0xFFFF)
        }

        fun writeBytes(bytes: ByteArray) {
            out.write(bytes)
        }

        fun toByteArray(): ByteArray = byteStream.toByteArray()
    }
}