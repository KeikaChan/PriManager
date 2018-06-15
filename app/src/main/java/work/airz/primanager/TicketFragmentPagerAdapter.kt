package work.airz.primanager

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import work.airz.primanager.qr.QRUtil

class TicketFragmentPagerAdapter(fragmentManager: FragmentManager, val context: Context) : FragmentPagerAdapter(fragmentManager) {

    val tabNames = context.resources.getStringArray(R.array.tabname)
    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        val fragment = TicketListFragment()

        when (position) {
            0 -> fragment.arguments = Bundle().apply { putSerializable(QRUtil.TICKET_TYPE, QRUtil.TicketType.PRICHAN_FOLLOW) }
            1 -> fragment.arguments = Bundle().apply { putSerializable(QRUtil.TICKET_TYPE, QRUtil.TicketType.PRICHAN_COORD) }
            else -> fragment.arguments = Bundle().apply { putSerializable(QRUtil.TICKET_TYPE, QRUtil.TicketType.PRICHAN_COORD) }
        }

        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabNames[position]

    }
}