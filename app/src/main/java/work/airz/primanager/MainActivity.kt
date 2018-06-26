package work.airz.primanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.ticket_item.view.*
import work.airz.primanager.db.DBUtil
import work.airz.primanager.qr.QRUtil

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, RecyclerViewHolder.IItemsList {
    private val REQUEST_PERMISSION = 1000

    private lateinit var dbUtil: DBUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbUtil = DBUtil(applicationContext)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            startActivity(Intent(this, QRActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        // 権限取得
        while (!checkPermission()) {
            Log.d("permission", "not granted")
            Thread.sleep(2000)
        }
        Log.d("permission", "granted")

        val itemFragmentPagerAdapter = TicketFragmentPagerAdapter(supportFragmentManager, applicationContext)
        main_view_pager.apply {
            offscreenPageLimit = 2
            adapter = itemFragmentPagerAdapter
        }
        main_tab_layout.setupWithViewPager(main_view_pager)
    }

    override fun onDelete(target: List<String>, ticketType: QRUtil.TicketType) {
        target.forEach { Log.d("delete target", it) }
        when (ticketType) {
            QRUtil.TicketType.PRICHAN_FOLLOW -> {
                target.forEach { dbUtil.removeFollowTicketData(dbUtil.getFollowTicket(it)) }
                //TODO: ユーザのフォロー解除
            }
            QRUtil.TicketType.PRICHAN_COORD -> {
                target.forEach { dbUtil.removeCoordTicketData(dbUtil.getCoordTicket(it)) }
            }
        }
    }


    override fun onItemPager(ticketType: QRUtil.TicketType): TicketListPager {
        return object : TicketListPager(ticketType = ticketType) {
            override fun onDBImage(rawData: String): Bitmap {
                Log.d("getDBImage","called")
                return when (ticketType) {
                    QRUtil.TicketType.PRICHAN_FOLLOW -> {
                        dbUtil.getFollowTicket(rawData).image
                    }
                    QRUtil.TicketType.PRICHAN_COORD -> {
                        dbUtil.getCoordTicket(rawData).image

                    }
                    else -> {
                        return BitmapFactory.decodeResource(resources, R.drawable.ic_qr)
                    }
                }
            }

            override fun onDBOutline(ticketType: QRUtil.TicketType): List<TicketUtils.TicketOutlineFormat> {
                Log.d("getOutline","called")
                return when (ticketType) {
                    QRUtil.TicketType.PRICHAN_FOLLOW -> {
                        dbUtil.getFollowTicketOutlines().toList()
                    }
                    QRUtil.TicketType.PRICHAN_COORD -> {
                        dbUtil.getCoordTicketOutlines().toList()

                    }
                    else -> {
                        return mutableListOf<TicketUtils.TicketOutlineFormat>()
                    }
                }
            }
        }
    }

    override fun onItemClick(view: View, position: Int, ticketType: QRUtil.TicketType) {
        when (ticketType) {
            QRUtil.TicketType.PRICHAN_FOLLOW -> {
                startActivity(Intent(this, SaveFollowTicket::class.java).apply {
                    putExtra(QRUtil.RAW, QRUtil.stringToByte(view.raw_data.text.toString()))
                    putExtra(QRUtil.QR_FORMAT, dbUtil.getFollowTicket(view.raw_data.text.toString())!!.qrFormat)
                    putExtra(QRUtil.TICKET_TYPE, ticketType)
                    putExtra(QRUtil.IS_DUPLICATE, true)
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
            }
            QRUtil.TicketType.PRICHAN_COORD -> {
                startActivity(Intent(this, SaveCoordTicket::class.java).apply {
                    putExtra(QRUtil.RAW, QRUtil.stringToByte(view.raw_data.text.toString()))
                    putExtra(QRUtil.QR_FORMAT, dbUtil.getCoordTicket(view.raw_data.text.toString())!!.qrFormat)
                    putExtra(QRUtil.TICKET_TYPE, ticketType)
                    putExtra(QRUtil.IS_DUPLICATE, true)
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                startActivity(Intent(this, MembersActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP })
            }
            R.id.nav_settings -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun checkPermission(): Boolean {
        // 既に許可している
        return if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            requestStoragePermission()
            false
        }// 拒否していた場合
    }


    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.INTERNET), REQUEST_PERMISSION)
    }

}
