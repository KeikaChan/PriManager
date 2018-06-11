package work.airz.primanager

import android.view.View

interface IItemsList {
    fun onDelete(positions: List<Int>)
    fun onItemList(): List<TicketUtils.TicketItemFormat>
    fun onItemClick(view: View, position: Int)
}
