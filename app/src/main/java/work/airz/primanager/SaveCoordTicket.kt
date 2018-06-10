package work.airz.primanager

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_save_coord_ticket.*
import work.airz.primanager.db.DBConstants
import work.airz.primanager.db.DBFormat
import work.airz.primanager.db.DBUtil
import work.airz.primanager.qr.QRUtil
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class SaveCoordTicket : AppCompatActivity(), View.OnClickListener, SaveTicket {
    private lateinit var coordList: HashMap<String, CoordDetail>
    private lateinit var rawData: ByteArray
    private lateinit var ticketType: QRUtil.TicketType
    private lateinit var qrFormat: QRUtil.QRFormat

    private lateinit var dbUtil: DBUtil

    private lateinit var TEMP_URI: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_coord_ticket)
        save.setOnClickListener(this)
        destruction.setOnClickListener(this)
        continuation.setOnClickListener(this)
        get_data.setOnClickListener(this)
        display_qr.setOnClickListener(this)
        thumbnail.setOnClickListener(this)

        TEMP_URI = FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.fileprovider", File(applicationContext.cacheDir.absolutePath, "temp.png"))

        coordList = getPrichanCoordData()

        dbUtil = DBUtil(applicationContext)

        rawData = intent.getByteArrayExtra(QRUtil.RAW) ?: return
        ticketType = intent.getSerializableExtra(QRUtil.TICKET_TYPE) as? QRUtil.TicketType ?: return
        qrFormat = intent.getSerializableExtra(QRUtil.QR_FORMAT) as? QRUtil.QRFormat ?: return
        arcade_series.setText(if (ticketType == QRUtil.TicketType.PRICHAN_FOLLOW) {
            DBConstants.PRICHAN
        } else {
            DBConstants.OTHERS
        })
        if (ticketType == QRUtil.TicketType.OTHERS) display_qr.visibility = View.VISIBLE
        if (intent.getBooleanExtra(QRUtil.IS_DUPLICATE, false)) getStoredData()

    }

    override fun getStoredData() {
        val coordTicket = dbUtil.getCoordTicket(QRUtil.byteToString(rawData))
        thumbnail.setImageBitmap(coordTicket.image)
        rarity.setText(coordTicket.rarity)
        name.setText(coordTicket.coordName)
        id.setText(coordTicket.coordId)
        date.setText(coordTicket.date)
        color.setText(coordTicket.color)
        category.setText(coordTicket.category)
        genre.setText(coordTicket.genre)
        like.setText(coordTicket.like.toString())
        brand.setText(coordTicket.brand)
        arcade_series.setText(coordTicket.arcadeSeries)
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
            R.id.get_data -> {
                id.setText(id.text.toString().toUpperCase())
                val detail = coordList[id.text.toString()]
                if (detail != null) {
                    setImage(detail.imageUrl, thumbnail)
                    name.setText(detail.name)
                    rarity.setText(detail.rarity)
                    color.setText(detail.color)
                    category.setText(detail.category)
                    genre.setText(detail.genre)
                    like.setText(detail.like)
                    brand.setText(detail.brand)
                    arcade_series.setText("プリチャン")

                }
            }
            R.id.thumbnail -> {
                try {
                    startActivityForResult(SavePhoto.getCaptureIntent(TEMP_URI, applicationContext), SavePhoto.CAMERA_CAPTURE)
                } catch (e: ActivityNotFoundException) {
                    Log.e("image cropping", "crop not supported")
                }
            }
            R.id.display_qr -> {
                QRUtil.saveQRAlert(rawData, qrFormat, this)
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

    private fun setImage(url: String, thumbnail: ImageView) {
        ImageAsyncTask().execute(url, thumbnail)
    }

    override fun saveData() {
        id.setText(id.text.toString().toUpperCase())
        val coordTicket = DBFormat.CoordTicket(
                QRUtil.byteToString(rawData),
                id.text.toString(),
                name.text.toString(),
                rarity.text.toString(),
                brand.text.toString(),
                color.text.toString(),
                category.text.toString(),
                genre.text.toString(),
                if (like.text.toString().isEmpty()) 0 else like.text.toString().toInt(),
                arcade_series.text.toString(),
                date.text.toString(),
                (thumbnail.drawable as BitmapDrawable).bitmap,
                memo.text.toString())
        dbUtil.addCoordTicketData(coordTicket)
    }

    private fun getPrichanCoordData(): HashMap<String, CoordDetail> {
        val coordHash = HashMap<String, CoordDetail>()
        resources.openRawResource(R.raw.prichan).bufferedReader().use {
            it.readLines().withIndex().forEach {
                if (it.index == 0) return@forEach
                coordHash[it.value.split(",")[0]] = CoordDetail(it.value)
            }
        }
        return coordHash
    }

    private class CoordDetail(csvdata: String) {
        val name: String
        val imageUrl: String
        val category: String
        val color: String
        val brand: String
        val genre: String
        val rarity: String
        val like: String
        val note: String

        init {
            val splitData = csvdata.split(",")
            name = splitData[1]
            imageUrl = splitData[2]
            category = splitData[3]
            color = splitData[4]
            brand = splitData[5]
            genre = splitData[6]
            rarity = splitData[7]
            like = splitData[8]
            note = splitData[9]
        }
    }


    companion object {
        open class ImageAsyncTask : AsyncTask<Any, Void, Bitmap>() {
            var imageView: ImageView? = null
            override fun doInBackground(vararg params: Any): Bitmap? {
                val url = URL(params[0] as? String ?: return null)
                imageView = params[1] as? ImageView ?: return null
                val urlConnection = url.openConnection()  as? HttpURLConnection ?: return null
                urlConnection.readTimeout = 5000
                urlConnection.connectTimeout = 7000
                urlConnection.requestMethod = "GET"
                urlConnection.doInput = true
                urlConnection.connect()
                return BitmapFactory.decodeStream(urlConnection.inputStream)
            }

            override fun onPostExecute(result: Bitmap?) {
                super.onPostExecute(result)
                result ?: return
                imageView!!.setImageBitmap(result)
            }
        }
    }


}
