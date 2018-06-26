package work.airz.primanager

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_members.*
import kotlinx.android.synthetic.main.ticket_item.view.*
import work.airz.primanager.db.DBUtil
import work.airz.primanager.qr.QRUtil

class MembersActivity : AppCompatActivity(), RecyclerViewHolder.IItemsList {
    private lateinit var dbUtil: DBUtil
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbUtil = DBUtil(applicationContext)
        setContentView(R.layout.activity_members)
        setSupportActionBar(toolbar)

        val fragment = TicketListFragment()
        fragment.arguments = Bundle().apply { putSerializable(QRUtil.TICKET_TYPE, QRUtil.TicketType.PRICHAN_MEMBERS) }
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()

        fab.setOnClickListener {
            startActivity(Intent(this, QRActivity::class.java).apply {
                putExtra(QRUtil.TICKET_TYPE, QRUtil.TicketType.PRICHAN_MEMBERS)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    override fun onItemClick(view: View, position: Int, ticketType: QRUtil.TicketType) {
        startActivity(Intent(this, SaveUserTicket::class.java).apply {
            putExtra(QRUtil.RAW, QRUtil.stringToByte(view.raw_data.text.toString()))
            putExtra(QRUtil.QR_FORMAT, dbUtil.getUser(view.raw_data.text.toString())!!.qrFormat)
            putExtra(QRUtil.IS_DUPLICATE, true)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
    }

    override fun onItemPager(ticketType: QRUtil.TicketType): TicketListPager {
        return object : TicketListPager(ticketType = ticketType) {
            override fun onDBImage(rawData: String): Bitmap {
                return dbUtil.getUser(rawData)!!.image
            }

            override fun onDBOutline(ticketType: QRUtil.TicketType): List<TicketUtils.TicketOutlineFormat> {
                val userList = mutableListOf<TicketUtils.TicketOutlineFormat>()
                dbUtil.getUserList().forEach {
                    userList.add(TicketUtils.TicketOutlineFormat(it.userName, it.userCardId, it.raw))
                }
                return userList.toList()

            }

        }
//        val userList = mutableListOf<TicketUtils.TicketItemFormat>()
//        dbUtil.getUserList().forEach {
//            userList.add(TicketUtils.TicketItemFormat(it.userName, it.userCardId, it.image, it.raw))
//        }
//        return userList.toList()
    }

    override fun onDelete(target: List<String>, ticketType: QRUtil.TicketType) {
        target.forEach { Log.d("delete target", it) }
        when (ticketType) {
            QRUtil.TicketType.PRICHAN_MEMBERS -> {
                target.forEach { dbUtil.removeUser(dbUtil.getUser(it)!!) }
            }
        }
    }


}
