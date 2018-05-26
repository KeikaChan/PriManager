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
}