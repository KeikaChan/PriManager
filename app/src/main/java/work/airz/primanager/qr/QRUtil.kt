package work.airz.primanager.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.nio.charset.Charset
import java.util.*

class QRUtil {

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
     * @param maskindex mask index (it can get BarcodeResult.result.maskIndex)
     * @param isInverted PriChan/PriPara format
     * @param version version of QR Code
     */
    fun createQR(data: ByteArray, maskindex: Int, isInverted: Boolean, version: Int): Bitmap {
        var hints = EnumMap<EncodeHintType, Object>(EncodeHintType::class.java)
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M as Object
        hints[EncodeHintType.MASK_INDEX] = maskindex as Object
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
     * when Pri☆Chan (1st release) QR, QR header is 0x50A203
     * 第一弾時点ではフォロチケ/コーデのQRのヘッダがすべて0x50A203から始まる
     * プリパラはヘッダもランダム
     * また，データサイズは26か122。これはプリパラと同様。
     */
    fun isPriChanQR(data: ByteArray): Boolean {
        return isPriChanFollowTicket(data) && isPriChanCodeTicket(data)
    }

    /**
     * フォロチケ判別
     */
    fun isPriChanFollowTicket(data: ByteArray): Boolean {
        if (!data.isNotEmpty()) return false
        return data[0].toInt() == 0x50
                && data[1].toInt() == 0xA2
                && data[2].toInt() == 0x03
                && data.size == 122
    }

    /**
     * コーデチケット判別
     * 会員証も同様
     */
    fun isPriChanCodeTicket(data: ByteArray): Boolean {
        if (!data.isNotEmpty()) return false
        return data[0].toInt() == 0x50
                && data[1].toInt() == 0xA2
                && data[2].toInt() == 0x03
                && data.size == 26
    }

    /**
     * フォロチケのユーザIDを返す
     * 会員証とはバイナリデータが異なるので注意!!
     *  詳細は解析が必要
     */
    fun getFollowUserID(data: ByteArray): String? {
        if (data.size != 122 || !isPriChanFollowTicket(data)) return null
        val userId = StringBuilder()
        for (index in 105..120) {
            userId.append(String.format("%02X ", data[index]))
        }
        return userId.toString()
    }
}