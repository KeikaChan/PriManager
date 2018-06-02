package work.airz.primanager

import android.Manifest
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.ImageView
import com.google.zxing.*
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import java.nio.charset.Charset
import java.util.*
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Environment
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import work.airz.primanager.db.DBConstants
import work.airz.primanager.db.DBUtil
import work.airz.primanager.qr.QRUtil
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat


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
    fun readQR() {
        qrReaderView = findViewById(R.id.decoratedBarcodeView)
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
                val qrBitmap = QRUtil.createQR(data, result.result.maskIndex, result.sourceData.isInverted, QRUtil.detectVersionM(result.rawBytes.size))
                val dbUtil = DBUtil(applicationContext)
                if (QRUtil.isPriChanFollowTicket(data)) {
                    Log.d("test","プリチャンチケット処理")
                    val raw = QRUtil.byteToString(data)
                    if(!dbUtil.isDuplicate(DBConstants.FOLLOW_TICKET_TABLE, raw)){
                        Toast.makeText(applicationContext, "QR被ってない", Toast.LENGTH_SHORT).show()
                        dbUtil.putFollowTicketData(DBUtil.FollowTicket(raw,"test","neko","1111",10000,111,"nekosan","prichan", BitmapFactory.decodeResource(resources,R.drawable.ic_qr),"test"))
                    }else{
                        saveAlert(dbUtil.getFollowTicketList().first().image,qrReaderView)
                        Toast.makeText(applicationContext, "QR被ってるよ！！", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Log.d("test","フォロチケじゃないっぽい")
                }
                Thread.sleep(1000)
//                qrReaderView.pause()
//                saveAlert(qrBitmap, qrReaderView)
            }
        })
        qrReaderView.resume()
    }


    fun saveAlert(qrImage: Bitmap, readerView: DecoratedBarcodeView) {

        val inflater = LayoutInflater.from(applicationContext)
        var dialogRoot = inflater.inflate(R.layout.save_dialog, null)

        var imageView = dialogRoot.findViewById<ImageView>(R.id.qrimage)
        imageView.scaleType = ImageView.ScaleType.FIT_XY
        imageView.adjustViewBounds = true
        imageView.setImageBitmap(qrImage)
        var editText = dialogRoot.findViewById<EditText>(R.id.filename)

        var builder = AlertDialog.Builder(this)
        builder.setView(dialogRoot)
        builder.setCancelable(false)
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, _ ->
            dialogInterface.dismiss()
            readerView.resume()
        })
        builder.setPositiveButton("Save", DialogInterface.OnClickListener { dialogInterface, _ ->
            val outDir = File(Environment.getExternalStorageDirectory().absolutePath, "priQR")
            if (!outDir.exists()) outDir.mkdirs()

            var outputName: String
            outputName = if (editText.text.toString() != "") {
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
                    qrImage.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            } else {
                FileOutputStream(File(outDir.absolutePath, "${outputName}.png")).use {
                    qrImage.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }
            readerView.resume()
        })
        builder.show()
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
