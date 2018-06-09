package work.airz.primanager

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_save_follow_ticket.*
import work.airz.primanager.db.DBConstants
import work.airz.primanager.db.DBFormat
import work.airz.primanager.db.DBUtil
import work.airz.primanager.qr.QRUtil

class SaveFollowTicket : AppCompatActivity(), View.OnClickListener {
    private lateinit var rawData: ByteArray
    private lateinit var ticketType: QRUtil.TicketType
    private lateinit var qrFormat: QRUtil.QRFormat

    private lateinit var dbUtil: DBUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_follow_ticket)
        save.setOnClickListener(this)
        destruction.setOnClickListener(this)
        continuation.setOnClickListener(this)
        display_qr.setOnClickListener(this)

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
