package work.airz.primanager

import android.support.v7.widget.RecyclerView
import android.view.View
import work.airz.primanager.qr.QRUtil

class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    interface IItemsList {
        fun onDelete(target: List<String>, ticketType: QRUtil.TicketType)
        fun onItemPager(ticketType: QRUtil.TicketType): TicketListPager
        fun onItemClick(view: View, position: Int, ticketType: QRUtil.TicketType)
    }
}
