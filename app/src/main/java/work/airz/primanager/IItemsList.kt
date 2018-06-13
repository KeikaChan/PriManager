package work.airz.primanager

import android.view.View
import work.airz.primanager.qr.QRUtil

interface IItemsList {
    fun onDelete(positions: List<Int>)
    fun onItemList(ticketType: QRUtil.TicketType): List<TicketUtils.TicketItemFormat>
    fun onItemClick(view: View, position: Int,ticketType: QRUtil.TicketType)
}
