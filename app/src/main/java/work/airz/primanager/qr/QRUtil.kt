package work.airz.primanager.qr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import work.airz.primanager.R
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*


class QRUtil {
    companion object {
        const val RAW = "raw"
        const val TICKET_TYPE = "ticket_type"
        const val QR_FORMAT = "qr_format"
        const val IS_DUPLICATE = "is_duplicate"

        const val PRI_QR_FOLDER = "PriQR"
        const val PRI_COORD_FOLDER = "PriCoord"
        const val PRI_FOLLOW_FOLDER = "PriFollow"
        const val PRI_USER_FOLDER = "PriUser"

        /**
         * This function is only support when "error correction level is M and also size is 14 ~ 213"
         * プリパラのQRコードはエラーレベルがMでサイズもあまり大きくないのでこれくらいにしています。
         * 将来的にはライブラリみたいにしたほうが良いかも
         * @param size rawByte size of qr code
         */
        fun detectVersionM(size: Int): Int {
            //rawdata -2のサイズがQRコードのバージョン表Mの部分と一致する
            return when (size) {
                14 + 2 -> 1
                26 + 2 -> 2
                42 + 2 -> 3
                62 + 2 -> 4
                84 + 2 -> 5
                106 + 2 -> 6
                122 + 2 -> 7
                152 + 2 -> 8
                180 + 2 -> 9
                213 + 2 -> 10
                else -> 40
            }
        }

        /**
         * QRコードを作成します
         * @param data qr data
         * @param maskIndex mask index (it can get BarcodeResult.result.maskIndex)
         * @param isInverted PriChan/PriPara format
         * @param version version of QR Code
         */
        fun createQR(data: ByteArray, maskIndex: Int, isInverted: Boolean, version: Int): Bitmap {
            var hints = EnumMap<EncodeHintType, Object>(EncodeHintType::class.java)
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M as Object
            hints[EncodeHintType.MASK_INDEX] = maskIndex as Object
            hints[EncodeHintType.QR_VERSION] = version as Object

            val image = MultiFormatWriter().encode(String(data, Charset.forName("ISO-8859-1")), BarcodeFormat.QR_CODE, 256, 256, hints)

            val width = image.width
            val height = image.height
            var bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    when (isInverted) {
                        true -> bmp.setPixel(x, y,
                                if (image.get(x, y)) {
                                    Color.WHITE
                                } else {
                                    Color.BLACK
                                })
                        false -> bmp.setPixel(x, y,
                                if (image.get(x, y)) {
                                    Color.BLACK
                                } else {
                                    Color.WHITE
                                })
                    }
                }
            }
            return bmp
        }

        /**
         * QRコードのフォーマット形式を調べる
         * 追加で解析情報がわかったらここで処理を付け足す
         * @param data QRコードデータ
         * @return QRコードの形式
         */
        fun detectQRFormat(data: ByteArray): TicketType {
            return when {
                isPriChanFollowTicket(data) -> TicketType.PRICHAN_FOLLOW
                isPriChanCodeTicket(data) -> TicketType.PRICHAN_COORD
                else -> TicketType.OTHERS
            }
        }

        /**
         * when Pri☆Chan (1st release) QR, QR header is 0x50A203
         * 第一弾時点ではフォロチケ/コーデのQRのヘッダがすべて0x50A203から始まる
         * プリパラはヘッダもランダムなので別途解析が必要。khromはプリパラ住民じゃなかったのでわからん。
         * また，データサイズは26か122。これはプリパラと同様。
         */
        private fun isPriChanQR(data: ByteArray): Boolean {
            return isPriChanFollowTicket(data) && isPriChanCodeTicket(data)
        }

        /**
         * フォロチケ判別
         * @param data QRコードデータ
         * @return true:フォロチケ false:その他
         */
        private fun isPriChanFollowTicket(data: ByteArray): Boolean {
            if (!data.isNotEmpty()) return false
            return isPriChanHeader(data)
                    && data.size == 122
        }

        /**
         * コーデチケット判別
         * 会員証も同様
         * @param data QRコードデータ
         * @return true:コーデチケット false:その他
         */
        private fun isPriChanCodeTicket(data: ByteArray): Boolean {
            if (!data.isNotEmpty()) return false
            return isPriChanHeader(data)
                    && data.size == 26
        }

        /**
         * プリちゃん形式のヘッダになっているか
         * @param data QRコードデータ
         * @return true:プリチャン形式 false:それ以外
         */
        private fun isPriChanHeader(data: ByteArray): Boolean {
            val strb = StringBuilder()
            for (index in 0..2) {
                strb.append(String.format("%02X", data[index]))
            }
            return strb.toString() == "50A203"
        }


        /**
         * フォロチケのユーザIDを返す
         * 会員証とはバイナリデータが異なるので注意!!
         *  詳細は解析が必要
         */
        fun getFollowUserID(data: ByteArray): String {
            if (data.size != 122 || !isPriChanFollowTicket(data)) throw  IllegalArgumentException("data format is not correct.")
            val userId = StringBuilder()
            for (index in 105..120) {
                userId.append(String.format("%02X", data[index]))
            }
            return userId.toString()
        }

        fun byteToString(data: ByteArray): String {
            val strb = StringBuilder()
            data.forEach { strb.append(String.format("%02X", it)) }
            return strb.toString()
        }

        fun stringToByte(data: String): ByteArray {
            val chars = data.toCharArray()
            val len = chars.size
            if (len and 1 != 0) {
                return byteArrayOf()
            } else {
                val out = ByteArray(len shr 1)
                var i = 0
                var j = 0
                while (j < len) {
                    var f = Character.digit(chars[j], 16) shl 4
                    j++
                    f = f or Character.digit(chars[j], 16)
                    j++
                    out[i] = (f and 255).toByte()
                    i++
                }
                return out
            }
        }

        /**
         * qrコード保存用
         * @param data qr data
         * @param qrFormat qr format
         * @param context app context
         */
        fun saveQRAlert(data: ByteArray, qrFormat: QRUtil.QRFormat, context: Context) {
            val qrBitmap = QRUtil.createQR(data, qrFormat.maskIndex, qrFormat.isInverted, qrFormat.version)
            saveImageAlert(qrBitmap, PRI_QR_FOLDER, context)
        }


        /**
         * 保存用画像を表示して保存処理をする
         * @param imageBitmap 保存する画像
         * @param context app context
         */
        fun saveImageAlert(imageBitmap: Bitmap, childFolderName: String, context: Context) {

            val inflater = LayoutInflater.from(context)
            var dialogRoot = inflater.inflate(R.layout.save_dialog, null)

            var imageView = dialogRoot.findViewById<ImageView>(R.id.qrimage)
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            imageView.adjustViewBounds = true
            imageView.setImageBitmap(imageBitmap)
            var editText = dialogRoot.findViewById<EditText>(R.id.filename)

            var builder = AlertDialog.Builder(context)
            builder.setView(dialogRoot)
            builder.setCancelable(false)
            builder.setNegativeButton(context.resources.getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            builder.setPositiveButton(context.resources.getString(R.string.save)) { _, _ ->
                val outDir = File(Environment.getExternalStorageDirectory().absolutePath, childFolderName)
                if (!outDir.exists()) outDir.mkdirs()

                var outputName: String = if (editText.text.toString() != "") {
                    editText.text.toString()
                } else {
                    SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                }
                if (File(outDir.absolutePath, "${outputName}.png").exists()) {
                    var count = 1
                    while (File(outDir.absolutePath, "${outputName}-${count}.png").exists()) {
                        count++
                    }
                    FileOutputStream(File(outDir.absolutePath, "${outputName}-${count}.png")).use {
                        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    Toast.makeText(context, "save to ${File(outDir.absolutePath, "${outputName}-${count}.png").absolutePath}", Toast.LENGTH_LONG)
                } else {
                    FileOutputStream(File(outDir.absolutePath, "${outputName}.png")).use {
                        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    Toast.makeText(context, "save to ${File(outDir.absolutePath, "${outputName}.png").absolutePath}", Toast.LENGTH_LONG)

                }
            }
            builder.show()
        }
    }


    class QRFormat(val errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M, val maskIndex: Int = 1, val isInverted: Boolean = false, val version: Int = 2) : Serializable {
        companion object {
            /**
             * String形式で送られてきたデータのパース
             * @param qrformat qrコードのフォーマットデータ
             * @return 整形済みデータ
             */
            fun parseString(qrformat: String): QRFormat {
                val split = qrformat.split(",")
                return QRFormat(getStringToErrorCorrectionLevel(split[0]), split[1].toInt(), split[2].toBoolean(), split[3].toInt())

            }

            /**
             * Emumのエラーレベルを文字列に変換
             * @param errorCorrectionLevel エラーレベル
             * @return Stringに変換したデータ
             */
            fun getErrorCorrectionString(errorCorrectionLevel: ErrorCorrectionLevel): String {
                return when (errorCorrectionLevel) {
                    ErrorCorrectionLevel.M -> "M"
                    ErrorCorrectionLevel.L -> "L"
                    ErrorCorrectionLevel.H -> "H"
                    ErrorCorrectionLevel.Q -> "Q"
                    else -> "M"
                }
            }

            /**
             * 文字列からエラーレベルを起こす
             * @param errorCorrectionString エラーレベル
             * @return エラーレベルのenum
             */
            fun getStringToErrorCorrectionLevel(errorCorrectionString: String): ErrorCorrectionLevel {
                return when (errorCorrectionString) {
                    "M" -> ErrorCorrectionLevel.M
                    "L" -> ErrorCorrectionLevel.L
                    "H" -> ErrorCorrectionLevel.H
                    "Q" -> ErrorCorrectionLevel.Q
                    else -> ErrorCorrectionLevel.M
                }
            }

        }


        /**
         * データ形式のテキスト化
         */
        override fun toString(): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append("${getErrorCorrectionString(errorCorrectionLevel)},")
            stringBuilder.append("${maskIndex},")
            stringBuilder.append("${isInverted},")
            stringBuilder.append("${version},")
            return stringBuilder.toString()
        }

    }

    enum class TicketType : Serializable {
        PRICHAN_FOLLOW, PRICHAN_COORD, PRICHAN_MEMBERS, OTHERS //OTHERS にはプリパラの他、映画特典のプリチャンのチケットも含むよ。現状ではここまでしかわからない。

    }
}