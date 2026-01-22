package gg.ninjagaming.lobbyswitcher.ping

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.Charset
import java.util.logging.Level
import java.util.logging.Logger

class ServerPing {
    private var host: InetSocketAddress? = null
    private val timeout = 2000
    fun setAddress(host: InetSocketAddress) {
        this.host = host
    }

    @Suppress("unused")
    @Throws(IOException::class)
    fun fetchData(): DefaultResponse {
        var socket: Socket? = null
        var outputStream: OutputStream? = null
        var dataOut: DataOutputStream? = null
        var inputStream: InputStream? = null
        var dataIn: DataInputStream? = null

        try {
            socket = Socket()

            socket.soTimeout = timeout
            socket.connect(host, timeout)

            outputStream = socket.getOutputStream()
            dataOut = DataOutputStream(outputStream)

            inputStream = socket.getInputStream()
            dataIn = DataInputStream(inputStream)

            // HANDSHAKE >
            val bOut = ByteArrayOutputStream()
            val handshake = DataOutputStream(bOut)
            bOut.write(0x00) // packet id
            writeVarInt(handshake, 4) // protocol version
            writeVarInt(handshake, host!!.hostString.length)
            handshake.writeBytes(host!!.hostString)
            handshake.writeShort(host!!.port)
            writeVarInt(handshake, 1) // target state 1

            writeVarInt(dataOut, bOut.toByteArray().size)
            dataOut.write(bOut.toByteArray())

            // < HANDSHAKE
            writeVarInt(dataOut, byteArrayOf(0x00).size)
            dataOut.write(byteArrayOf(0x00))

            // >
            val size = readVarInt(dataIn)
            val packetId = readVarInt(dataIn)

            if (packetId != 0x00) {
                throw IOException("Invalid packetId")
            }

            val stringLength = readVarInt(dataIn)

            if (stringLength < 1) {
                throw IOException("Invalid string length.")
            }

            val responseData = ByteArray(stringLength)
            dataIn.readFully(responseData)
            val jsonString = String(responseData, Charset.forName("utf-8"))

            var jsonObject = JsonObject()
            val parser = JsonParser()
            try {
                jsonObject = parser.parse(jsonString) as JsonObject
            } catch (ex: ParseException) {
                Logger.getLogger(ServerPing::class.java.getName()).log(Level.SEVERE, null, ex)
            }
            val jsonVersion = jsonObject.get("version") as JsonObject
            val version = jsonVersion.get("name").asString
            val response: DefaultResponse = DefaultResponse()

            populateResponseData(version, response, jsonString)

            return response
        } finally {
            runCatching { dataIn?.close() }
            runCatching { inputStream?.close() }
            runCatching { dataOut?.close() }
            runCatching { outputStream?.close() }
            runCatching { socket?.close() }
        }
    }

    fun populateResponseData(version: String, response: DefaultResponse, jsonString: String) {
        when {
            version.contains("1.9") -> {
                val responseData: StatusResponseV1x9 =
                    gson.fromJson<StatusResponseV1x9>(jsonString, StatusResponseV1x9::class.java)
                response.description = responseData.description!!.text.toString()
                response.favicon = responseData.favicon
                response.players = responseData.players!!.online
                response.maxPlayers = responseData.players.max
                response.time = responseData.time
                response.protocol = responseData.version!!.protocol
                response.version = responseData.version.name
            }

            version.contains("1.10")
                    or version.contains("1.11")
                    or version.contains("1.12") -> {
                val responseData: StatusResponseV1x10 =
                    gson.fromJson<StatusResponseV1x10>(jsonString, StatusResponseV1x10::class.java)
                response.description = responseData.description!!.text.toString()
                response.players = responseData.players!!.online
                response.maxPlayers = responseData.players.max
                response.time = responseData.time
                response.protocol = responseData.version!!.protocol
                response.version = responseData.version.name
            }

            version.contains("1.13")
                    or version.contains("1.14")
                    or version.contains("1.15") -> {
                val responseData: StatusResponseV1x13 =
                    gson.fromJson<StatusResponseV1x13>(jsonString, StatusResponseV1x13::class.java)
                response.description = responseData.description!!.text.toString()
                response.players = responseData.players!!.online
                response.maxPlayers = responseData.players.max
                response.time = -1
                response.protocol = responseData.version!!.protocol
                response.version = responseData.version.name
            }

            else -> {
                val statusResponse: StatusResponse =
                    gson.fromJson<StatusResponse>(jsonString, StatusResponse::class.java)
                response.description = statusResponse.description.toString()
                response.favicon = statusResponse.favicon
                response.players = statusResponse.players!!.online
                response.maxPlayers = statusResponse.players!!.max
                response.time = statusResponse.time
                response.protocol = statusResponse.version!!.protocol
                response.version = statusResponse.version!!.name
            }
        }
    }

    class DefaultResponse {
        var description: String = ""
        var version: String? = null
        var protocol: String? = null
        var favicon: String? = null
        var players: Int = 0
        var maxPlayers: Int = 0
        var time: Int = 0
    }

    class StatusResponse {
        var description: String? = null
        var players: Players? = null
        var version: Version? = null
        var favicon: String? = null
        var time: Int = 0

        class Players {
            var max: Int = 0
            var online: Int = 0
            var sample: MutableList<Player?>? = null
        }

        class Player {
            var name: String? = null
            var id: String? = null
        }

        class Version {
            var name: String? = null
            var protocol: String? = null
        }
    }

    class StatusResponseV1x9 {
        val players: Players? = null
        val version: Version? = null
        val favicon: String? = null
        val description: Description? = null
        var time: Int = 0

        class Description {
            val text: String? = null
        }

        inner class Players {
            val max: Int = 0
            val online: Int = 0
            val sample: MutableList<Player?>? = null
        }

        class Player {
            val name: String? = null
            val id: String? = null
        }

        class Version {
            val name: String? = null
            val protocol: String? = null
        }
    }

    class StatusResponseV1x10 {
        val players: Players? = null
        val version: Version? = null
        val description: Description? = null
        var time: Int = 0

        class Description {
            val text: String? = null
        }

        class Players {
            val max: Int = 0
            val online: Int = 0
        }

        class Version {
            val name: String? = null
            val protocol: String? = null
        }
    }

    class StatusResponseV1x13 {
        val description: Description? = null
        val players: Players? = null
        val version: Version? = null

        class Description {
            val text: String? = null
        }

        class Players {
            val max: Int = 0
            val online: Int = 0
        }

        class Version {
            val name: String? = null
            val protocol: String? = null
        }
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

    @Throws(IOException::class)
    fun writeVarInt(out: DataOutputStream, paramInt: Int) {
        var paramInt = paramInt
        while (true) {
            if ((paramInt and -0x80) == 0) {
                out.write(paramInt)
                return
            }

            out.write(paramInt and 0x7F or 0x80)
            paramInt = paramInt ushr 7
        }
    }

    companion object {
        private val gson = Gson()
    }
}
