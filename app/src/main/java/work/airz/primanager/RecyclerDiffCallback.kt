package work.airz.primanager

import android.support.v7.util.DiffUtil

class RecyclerDiffCallback(private val old: List<TicketUtils.TicketItemFormat>, private val new: List<TicketUtils.TicketItemFormat>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = old[oldItemPosition].raw == new[newItemPosition].raw

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = old[oldItemPosition].raw == new[newItemPosition].raw &&
            old[oldItemPosition].title == new[newItemPosition].title &&
            old[oldItemPosition].description == new[newItemPosition].description &&
            old[oldItemPosition].thumbnail == new[newItemPosition].thumbnail

}