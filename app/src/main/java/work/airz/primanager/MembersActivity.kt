package work.airz.primanager

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_members.*

class MembersActivity : AppCompatActivity(), IItemsList {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)
        setSupportActionBar(toolbar)

        supportFragmentManager.beginTransaction().replace(R.id.container, TicketListFragment()).commit()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
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

    override fun onItemClick(view: View, position: Int) {
        Toast.makeText(applicationContext, "position $position was tapped", Toast.LENGTH_SHORT).show()
    }

    override fun onItemList(): List<TicketUtils.TicketItemFormat> {
        val format = TicketUtils.TicketItemFormat("title1", "title2", BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
        return listOf(format)
    }

    override fun onDelete(positions: List<Int>) {
    }


}
