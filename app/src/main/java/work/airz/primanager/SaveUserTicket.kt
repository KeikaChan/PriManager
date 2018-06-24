package work.airz.primanager

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_save_user_ticket.*
import work.airz.primanager.TicketUtils.SavePhoto
import work.airz.primanager.db.DBFormat
import work.airz.primanager.db.DBUtil
import work.airz.primanager.qr.QRUtil
import java.io.File

class SaveUserTicket : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {
    private lateinit var rawData: ByteArray
    private lateinit var qrFormat: QRUtil.QRFormat

    private lateinit var dbUtil: DBUtil

    private lateinit var TEMP_URI: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_user_ticket)
        save.setOnClickListener(this)
        display_qr.setOnClickListener(this)
        thumbnail.setOnClickListener(this)
        thumbnail.setOnLongClickListener(this)
        destruction.setOnClickListener(this)

        TEMP_URI = FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.fileprovider", File(applicationContext.cacheDir.absolutePath, "temp.png"))

        qrFormat = intent.getSerializableExtra(QRUtil.QR_FORMAT) as? QRUtil.QRFormat ?: return
        dbUtil = DBUtil(applicationContext)

        rawData = intent.getByteArrayExtra(QRUtil.RAW) ?: return
        if (intent.getBooleanExtra(QRUtil.IS_DUPLICATE, false)) getStoredData()
    }

    private fun getStoredData() {
        val user = dbUtil.getUser(QRUtil.byteToString(rawData)) ?: return
        name.setText(user.userName)
        card_id.setText(user.userCardId)
        thumbnail.setImageBitmap(user.image)
        display_qr.visibility = View.VISIBLE
        Toast.makeText(applicationContext, "データを読み込みました", Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.save -> {
                saveData()
                finish()
            }
            R.id.display_qr -> {
                QRUtil.saveQRAlert(rawData, qrFormat, this)
            }
            R.id.destruction -> {
                finish()
            }
            R.id.thumbnail -> {
                try {
                    startActivityForResult(SavePhoto.getCaptureIntent(TEMP_URI, applicationContext), SavePhoto.CAMERA_CAPTURE)
                } catch (e: ActivityNotFoundException) {
                    Log.e("image cropping", "crop not supported")
                }
            }
        }
    }

    override fun onLongClick(v: View): Boolean {
        when (v.id) {
            R.id.thumbnail -> {
                QRUtil.saveImageAlert((thumbnail.drawable as BitmapDrawable).bitmap, QRUtil.PRI_USER_FOLDER, this)
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            SavePhoto.CAMERA_CAPTURE -> {
                performCrop()
            }
            SavePhoto.CROP_PIC -> {
                thumbnail.setImageURI(TEMP_URI)
            }
        }
    }

    private fun performCrop() {
        try {
            startActivityForResult(SavePhoto.getCropIntent(TEMP_URI, applicationContext), SavePhoto.CROP_PIC)
        } catch (e: ActivityNotFoundException) {
            Log.e("image cropping", "this device doesn't support crop action")
        }
    }

    /**
     * データ保存
     */
    private fun saveData() {
        val user = DBFormat.User(
                QRUtil.byteToString(rawData),
                qrFormat,
                name.text.toString(),
                card_id.text.toString(),
                (thumbnail.drawable as BitmapDrawable).bitmap,
                date.text.toString(),
                memo.text.toString(),
                dbUtil.getUserHashString(QRUtil.byteToString(rawData)))
        dbUtil.addUser(user)
        PriQRPrefsManager(applicationContext).putIsUpdate(true)
        Toast.makeText(applicationContext, "保存完了♪", Toast.LENGTH_LONG).show()
    }
}
