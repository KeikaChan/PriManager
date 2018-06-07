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
        val rawData = QRUtil.byteToString(data)

        val qrFormat = QRUtil.QRFormat(QRUtil.QRFormat().getStringToErrorCorrectionLevel(result.resultMetadata[ResultMetadataType.ERROR_CORRECTION_LEVEL] as String),
                result.result.maskIndex,
                result.sourceData.isInverted, QRUtil.detectVersionM(result.rawBytes.size))
        Log.d("qrformat", qrFormat.toString())

        when (QRUtil.detectQRFormat(data)) {
            QRUtil.QRType.PRICHAN_FOLLOW -> {
                val followUserID = QRUtil.getFollowUserID(data)
                val followedUsers = dbUtil.getUserList().filter { dbUtil.isFollowed(it, followUserID) }
                if (followedUsers.isNotEmpty()) followedAlert(rawData, qrFormat, followedUsers)
                else if (dbUtil.isDuplicate(DBConstants.FOLLOW_TICKET_TABLE, followUserID)) duplicateDataAlert(rawData, qrFormat, QRUtil.QRType.PRICHAN_FOLLOW)
                saveAlert(rawData, qrFormat, QRUtil.QRType.PRICHAN_FOLLOW)
            }
            QRUtil.QRType.PRICHAN_COORD -> {
                if (dbUtil.isDuplicate(DBConstants.COORD_TICKET_TABLE, rawData)) duplicateDataAlert(rawData, qrFormat, QRUtil.QRType.PRICHAN_COORD)
                saveAlert(rawData, qrFormat, QRUtil.QRType.PRICHAN_COORD)
            }
            QRUtil.QRType.OTHERS -> { //基本的にプリパラのトモチケは来ない前提で考える
                if (dbUtil.isDuplicate(DBConstants.COORD_TICKET_TABLE, rawData)) duplicateDataAlert(rawData, qrFormat, QRUtil.QRType.OTHERS)
                else nazoDataAlert(rawData, qrFormat, QRUtil.QRType.OTHERS)//謎データであることを告知する

            }
        }

    }

    private fun saveAlert(rawData: String, qrFormat: QRUtil.QRFormat, type: QRUtil.QRType) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("新データ")
        builder.setCancelable(false)
        builder.setMessage("まだ保存されていない物のようです。保存しますか？")
        builder.setPositiveButton("はい", { _, _ ->
            when (type) {
                QRUtil.QRType.PRICHAN_FOLLOW ->
                    startActivity(Intent(this, SaveFollowTicket::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP })
                QRUtil.QRType.PRICHAN_COORD ->
                    startActivity(Intent(this, SaveCoordTicket::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP })
                else ->
                    startActivity(Intent(this, SaveCoordTicket::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP })
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
    private fun nazoDataAlert(rawData: String, qrFormat: QRUtil.QRFormat, type: QRUtil.QRType) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("未知のデータ形式")
        builder.setCancelable(false)
        builder.setMessage("コーデ保存に飛びます。よろしいですか？")
        builder.setPositiveButton("はい", { _, _ ->
            //TODO: Intentでコーデに飛ぶ
            startActivity(Intent(this, SaveCoordTicket::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP })
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
    private fun duplicateDataAlert(rawData: String, qrFormat: QRUtil.QRFormat, type: QRUtil.QRType) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("既にデータが存在します")
        builder.setCancelable(false)
        builder.setMessage("データを編集しますか？")
        builder.setPositiveButton("はい", { _, _ ->
            //TODO: Intentでフォロー/コーデに飛ぶ
            qrReaderView.resume()
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
    private fun followedAlert(rawData: String, qrFormat: QRUtil.QRFormat, followdList: List<User>) {
        val strb = StringBuilder("以下のユーザにフォローされています。データを編集しますか？\n")
        followdList.forEach { strb.append("${it.userName}\n") }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("既にフォローされています")
        builder.setCancelable(false)
        builder.setMessage(strb.toString())
        builder.setPositiveButton("進む", { _, _ ->
            //TODO: Intentでフォローに飛ぶ
            qrReaderView.resume()
        })
        builder.setNegativeButton("戻る", { dialog, _ ->
            dialog.dismiss()
            qrReaderView.resume()
        })
        builder.show()
    }


//    fun saveAlert(qrImage: Bitmap, readerView: DecoratedBarcodeView) {
//
//        val inflater = LayoutInflater.from(applicationContext)
//        var dialogRoot = inflater.inflate(R.layout.save_dialog, null)
//
//        var imageView = dialogRoot.findViewById<ImageView>(R.id.qrimage)
//        imageView.scaleType = ImageView.ScaleType.FIT_XY
//        imageView.adjustViewBounds = true
//        imageView.setImageBitmap(qrImage)
//        var editText = dialogRoot.findViewById<EditText>(R.id.filename)
//
//        var builder = AlertDialog.Builder(this)
//        builder.setView(dialogRoot)
//        builder.setCancelable(false)
//        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, _ ->
//            dialogInterface.dismiss()
//            readerView.resume()
//        })
//        builder.setPositiveButton("Save", DialogInterface.OnClickListener { dialogInterface, _ ->
//            val outDir = File(Environment.getExternalStorageDirectory().absolutePath, "priQR")
//            if (!outDir.exists()) outDir.mkdirs()
//
//            var outputName: String
//            outputName = if (editText.text.toString() != "") {
//                editText.text.toString()
//            } else {
//                SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//            }
//            if (File(outDir.absolutePath, "${outputName}.png").exists()) {
//                var count = 1
//                while (File(outDir.absolutePath, "${outputName}-${count}.png").exists()) {
//                    count++
//                }
//                FileOutputStream(File(outDir.absolutePath, "${outputName}-${count}.png")).use {
//                    qrImage.compress(Bitmap.CompressFormat.PNG, 100, it)
//                }
//            } else {
//                FileOutputStream(File(outDir.absolutePath, "${outputName}.png")).use {
//                    qrImage.compress(Bitmap.CompressFormat.PNG, 100, it)
//                }
//            }
//            readerView.resume()
//        })
//        builder.show()
//    }

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
