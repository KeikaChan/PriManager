package work.airz.primanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
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

    override fun onDelete(positions: List<Int>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onItemList(ticketType: QRUtil.TicketType): List<TicketUtils.TicketItemFormat> {
        val userList = mutableListOf<TicketUtils.TicketItemFormat>()
        return when (ticketType) {
            QRUtil.TicketType.PRICHAN_FOLLOW -> {
                dbUtil.getFollowTicketList().forEach {
                    Log.d("follow ticket", "${it.userName},${it.memo},${it.raw}")
                    userList.add(TicketUtils.TicketItemFormat(it.userName, it.memo, it.image, it.raw))
                }
                userList.toList()
            }
            QRUtil.TicketType.PRICHAN_COORD -> {
                dbUtil.getCoordTicketList().forEach {
                    Log.d("ticket format ", "${it.coordName},${it.memo},${it.raw}")
                    userList.add(TicketUtils.TicketItemFormat(it.coordName, it.memo, it.image, it.raw))
                }
                userList.toList()
            }
            else -> {
                userList.toList()
            }
        }
        return userList.toList()
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("nyaa", "nya")
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
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
