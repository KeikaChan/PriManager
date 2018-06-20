package work.airz.primanager

import android.support.v7.widget.RecyclerView
import android.view.View
import work.airz.primanager.qr.QRUtil

class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    interface IItemsList {
        fun onDelete(target: List<String>)
        fun onItemList(ticketType: QRUtil.TicketType): List<TicketUtils.TicketItemFormat>
        fun onItemClick(view: View, position: Int, ticketType: QRUtil.TicketType)
    }
}
