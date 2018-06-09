package work.airz.primanager

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import com.google.zxing.*
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import kotlinx.android.synthetic.main.activity_qr.*
import kotlinx.android.synthetic.main.activity_save_follow_ticket.*
import work.airz.primanager.db.DBConstants
import work.airz.primanager.db.DBFormat.*
import work.airz.primanager.db.DBUtil
import work.airz.primanager.qr.QRUtil


class QRActivity : AppCompatActivity() {
    private lateinit var qrReaderView: DecoratedBarcodeView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)
        readQR()
    }

    /**
     * QRコードの読み取り部分の処理
     * 読み取って詳細データまで取得する
     * TODO: 読み取りデータの重複チェックをした後にユーザチェックとか色々入れる
     * TODO: DBとのデータ一致を確認する部分を作る
     */
    private fun readQR() {
        qrReaderView = decoratedBarcodeView
        qrReaderView.decodeContinuous(object : BarcodeCallback {
            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {

            }

            override fun barcodeResult(result: BarcodeResult?) {
                if (result == null || result.barcodeFormat != BarcodeFormat.QR_CODE) return

                val bytes = result.resultMetadata[ResultMetadataType.BYTE_SEGMENTS] as? List<*>
                val data = bytes?.get(0) as? ByteArray ?: return
                val strb = StringBuilder()
                data.forEach { strb.append(String.format("%02X ", it)) }
                Log.i("QR DUMP", strb.toString())

                Log.d("maskIndex", result.result.maskIndex.toString())
                Log.d("QRのサイズ", result.rawBytes.size.toString())

                analyzeQR(data, result)
            }
        })
        qrReaderView.resume()
    }

    fun analyzeQR(data: ByteArray, result: BarcodeResult) {
        qrReaderView.pause()

        val qrBitmap = QRUtil.createQR(data, result.result.maskIndex, result.sourceData.isInverted, QRUtil.detectVersionM(result.rawBytes.size))

        val dbUtil = DBUtil(applicationContext)
        val rawString = QRUtil.byteToString(data)

        val qrFormat = QRUtil.QRFormat(QRUtil.QRFormat().getStringToErrorCorrectionLevel(result.resultMetadata[ResultMetadataType.ERROR_CORRECTION_LEVEL] as String),
                result.result.maskIndex,
                result.sourceData.isInverted, QRUtil.detectVersionM(result.rawBytes.size))
        Log.d("qrformat", qrFormat.toString())

        when (QRUtil.detectQRFormat(data)) {
            QRUtil.QRType.PRICHAN_FOLLOW -> {
                val followUserID = QRUtil.getFollowUserID(data)
                val followedUsers = dbUtil.getUserList().filter { dbUtil.isFollowed(it, followUserID) }
                val isDuplicate = dbUtil.isDuplicate(DBConstants.FOLLOW_TICKET_TABLE, rawString)
                when {
                    followedUsers.isNotEmpty() -> followedAlert(data, qrFormat, followedUsers, QRUtil.QRType.PRICHAN_FOLLOW, isDuplicate)
                    isDuplicate -> duplicateDataAlert(data, qrFormat, QRUtil.QRType.PRICHAN_FOLLOW)
                    else -> saveAlert(data, qrFormat, QRUtil.QRType.PRICHAN_FOLLOW)
                }
            }
            QRUtil.QRType.PRICHAN_COORD -> {
                if (dbUtil.isDuplicate(DBConstants.COORD_TICKET_TABLE, rawString)) duplicateDataAlert(data, qrFormat, QRUtil.QRType.PRICHAN_COORD)
                saveAlert(data, qrFormat, QRUtil.QRType.PRICHAN_COORD)
            }
            QRUtil.QRType.OTHERS -> { //基本的にプリパラのトモチケは来ない前提で考える
                if (dbUtil.isDuplicate(DBConstants.COORD_TICKET_TABLE, rawString)) duplicateDataAlert(data, qrFormat, QRUtil.QRType.OTHERS)
                else nazoDataAlert(data, qrFormat, QRUtil.QRType.OTHERS)//謎データであることを告知する

            }
        }

    }

    private fun saveAlert(rawData: ByteArray, qrFormat: QRUtil.QRFormat, type: QRUtil.QRType) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("新データ")
        builder.setCancelable(false)
        builder.setMessage("まだ保存されていない物のようです。保存しますか？")
        builder.setPositiveButton("はい", { _, _ ->
            when (type) {
                QRUtil.QRType.PRICHAN_FOLLOW -> intentFollow(rawData, qrFormat, type, false)
                QRUtil.QRType.PRICHAN_COORD -> intentCoord(rawData, qrFormat, type, false)
                QRUtil.QRType.OTHERS -> intentCoord(rawData, qrFormat, type, false)
            }
            finish()
        })
        builder.setNegativeButton("いいえ", { dialog, _ ->
            dialog.dismiss()
            qrReaderView.resume()
        })
        builder.show()
    }

    /**
     * 謎データが来たときのアラート
     */
    private fun nazoDataAlert(rawData: ByteArray, qrFormat: QRUtil.QRFormat, type: QRUtil.QRType) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("未知のデータ形式")
        builder.setCancelable(false)
        builder.setMessage("コーデ保存に飛びます。よろしいですか？")
        builder.setPositiveButton("はい", { _, _ ->
            intentCoord(rawData, qrFormat, type, false)
            finish()
        })
        builder.setNegativeButton("いいえ", { dialog, _ ->
            dialog.dismiss()
            qrReaderView.resume()
        })
        builder.show()
    }

    /**
     * データ重複時のアラート
     */
    private fun duplicateDataAlert(rawData: ByteArray, qrFormat: QRUtil.QRFormat, type: QRUtil.QRType) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("既にデータが存在します")
        builder.setCancelable(false)
        builder.setMessage("データを編集しますか？")
        builder.setPositiveButton("はい", { _, _ ->
            when (type) {
                QRUtil.QRType.PRICHAN_FOLLOW -> intentFollow(rawData, qrFormat, type, true)
                QRUtil.QRType.PRICHAN_COORD -> intentCoord(rawData, qrFormat, type, true)
                QRUtil.QRType.OTHERS -> intentCoord(rawData, qrFormat, type, true)
            }
            finish()
        })
        builder.setNegativeButton("いいえ", { dialog, _ ->
            dialog.dismiss()
            qrReaderView.resume()
        })
        builder.show()
    }


    /**
     * フォロー済みのときのアラート
     */
    private fun followedAlert(rawData: ByteArray, qrFormat: QRUtil.QRFormat, followdList: List<User>, type: QRUtil.QRType, isDuplicate: Boolean) {
        val head = if (!isDuplicate) "このフォロチケは登録されていませんが、読み取ったユーザは以下のユーザでフォローしています。" else "以下のユーザでフォローしています。"
        val strb = StringBuilder("${head}編集しますか？\n")

        followdList.forEach { strb.append("${it.userName}\n") }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("既にフォローされています")
        builder.setCancelable(false)
        builder.setMessage(strb.toString())
        builder.setPositiveButton("進む", { _, _ ->
            intentFollow(rawData, qrFormat, type, isDuplicate)
            qrReaderView.resume()
        })
        builder.setNegativeButton("戻る", { dialog, _ ->
            dialog.dismiss()
            qrReaderView.resume()
        })
        builder.show()
    }

    /**
     * フォロー保存画面にジャンプ
     * @param rawData qrコードのデータ
     * @param qrFormat qrの形式
     * @param type QRのタイプ
     */
    private fun intentFollow(rawData: ByteArray, qrFormat: QRUtil.QRFormat, type: QRUtil.QRType, isDuplicate: Boolean) {
        startActivity(Intent(this, SaveFollowTicket::class.java).apply {
            putExtra(QRUtil.RAW, rawData)
            putExtra(QRUtil.TICKET_TYPE, type)
            putExtra(QRUtil.QR_FORMAT, qrFormat)
            putExtra(QRUtil.IS_DUPLICATE, isDuplicate)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
    }

    /**
     * コーデ保存画面にジャンプ
     * @param rawData qrコードのデータ
     * @param qrFormat qrの形式
     * @param type QRのタイプ
     */
    private fun intentCoord(rawData: ByteArray, qrFormat: QRUtil.QRFormat, type: QRUtil.QRType, isDuplicate: Boolean) {
        startActivity(Intent(this, SaveCoordTicket::class.java).apply {
            putExtra(QRUtil.RAW, rawData)
            putExtra(QRUtil.TICKET_TYPE, type)
            putExtra(QRUtil.QR_FORMAT, qrFormat)
            putExtra(QRUtil.IS_DUPLICATE, isDuplicate)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
    }

    override fun onResume() {
        super.onResume()
        qrReaderView.resume()
    }

    override fun onPause() {
        super.onPause()
        qrReaderView.resume()
    }

    override fun onStop() {
        super.onStop()
        qrReaderView.pause()
    }


}
