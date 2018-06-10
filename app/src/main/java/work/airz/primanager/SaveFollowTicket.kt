package work.airz.primanager

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_save_follow_ticket.*
import work.airz.primanager.db.DBConstants
import work.airz.primanager.db.DBFormat
import work.airz.primanager.db.DBUtil
import work.airz.primanager.qr.QRUtil
import java.io.File

class SaveFollowTicket : AppCompatActivity(), View.OnClickListener {
    private lateinit var rawData: ByteArray
    private lateinit var ticketType: QRUtil.TicketType
    private lateinit var qrFormat: QRUtil.QRFormat

    private lateinit var dbUtil: DBUtil

    private lateinit var TEMP_URI: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_follow_ticket)
        save.setOnClickListener(this)
        destruction.setOnClickListener(this)
        continuation.setOnClickListener(this)
        display_qr.setOnClickListener(this)
        thumbnail.setOnClickListener(this)
        select_follow.setOnClickListener(this)

        TEMP_URI = FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.fileprovider", File(applicationContext.cacheDir.absolutePath, "temp.png"))

        dbUtil = DBUtil(applicationContext)

        rawData = intent.getByteArrayExtra(QRUtil.RAW) ?: return
        ticketType = intent.getSerializableExtra(QRUtil.TICKET_TYPE) as? QRUtil.TicketType ?: return
        qrFormat = intent.getSerializableExtra(QRUtil.QR_FORMAT) as? QRUtil.QRFormat ?: return
        arcade_series.setText(if (ticketType == QRUtil.TicketType.PRICHAN_FOLLOW) {
            DBConstants.PRICHAN
        } else {
            DBConstants.OTHERS
        })
        if (intent.getBooleanExtra(QRUtil.IS_DUPLICATE, false)) getStoredData()
    }

    private fun getStoredData() {
        val followTicket = dbUtil.getFollowTicket(QRUtil.byteToString(rawData))
        name.setText(followTicket.userName)
        follower_text.setText(followTicket.follower.toString()) //整数を渡してはいけない
        follow_text.setText(followTicket.follow.toString())
        date.setText(followTicket.date)
        coord.setText(followTicket.coordinate)
        arcade_series.setText(followTicket.arcade_series)
        thumbnail.setImageBitmap(followTicket.image)
        Toast.makeText(applicationContext, "データを読み込みました", Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.save -> {
                saveData()
                finish()
            }
            R.id.destruction -> {
                finish()
            }
            R.id.continuation -> {
                saveData()
                startActivity(Intent(this, QRActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP })
                finish()
            }
            R.id.display_qr -> {
                QRUtil.saveQRAlert(rawData, qrFormat, applicationContext)
            }
            R.id.select_follow -> {
                val userList = dbUtil.getUserList()
                val userListString = mutableListOf<String>()
                val userFollowList = mutableListOf<Boolean>()
                val targetId = QRUtil.getFollowUserID(rawData)
                userList.forEach { userListString.add(it.userName) }
                userList.forEach { userFollowList.add(dbUtil.isFollowed(it, targetId)) }
                AlertDialog.Builder(applicationContext).apply {
                    setTitle("どのアカウントでフォローする？")
                    setMultiChoiceItems(userListString.toTypedArray(), userFollowList.toBooleanArray(), { dialog, which, isChecked ->
                        userFollowList[which] = isChecked
                    })
                    setPositiveButton("保存", { dialog, id ->
                        userFollowList.withIndex().forEach {
                            if(it.value){
                                dbUtil.followUser(userList[it.index],DBFormat.UserFollow(targetId,"null","null","null")) //TODO:最後の保存時にまとめて追加するようにする
                            }
                        }
                        dialog.dismiss()
                    })
                }.show()
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
     * 分かっているデータが合ったら
     * TODO: 補助データ用
     */
    fun setData() {
    }

    /**
     * データ保存
     */
    private fun saveData() {
        val followTicket = DBFormat.FollowTicket(
                QRUtil.byteToString(rawData),
                QRUtil.getFollowUserID(rawData),
                name.text.toString(),
                date.text.toString(),
                follow_text.text.toString().toInt(),
                follower_text.text.toString().toInt(),
                coord.text.toString(),
                arcade_series.text.toString(),
                (thumbnail.drawable as BitmapDrawable).bitmap,
                memo.text.toString())
        dbUtil.addFollowTicketData(followTicket)
        Toast.makeText(applicationContext, "保存完了♪", Toast.LENGTH_LONG).show()
    }
}
