package work.airz.primanager

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.ticket_item.view.*
import org.jetbrains.anko.forEachChild
import work.airz.primanager.db.DBUtil
import work.airz.primanager.qr.QRUtil


class RecyclarViewAdapter(val context: Context?, private val itemListener: RecyclerViewHolder.IItemsList, private var itemList: List<TicketUtils.TicketItemFormat>, private val ticketType: QRUtil.TicketType) : RecyclerView.Adapter<RecyclerViewHolder>() {
    var recyclerView: RecyclerView? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val ticketItemView = layoutInflater.inflate(R.layout.ticket_item, parent, false)

        ticketItemView.setOnClickListener { view: View ->
            recyclerView?.let {
                itemListener.onItemClick(view, it.getChildAdapterPosition(view), ticketType)
            }
        }

        return RecyclerViewHolder(ticketItemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder?.let {
            it.itemView.titleText.text = itemList[position].title
            it.itemView.descriptionText.text = itemList[position].description
            it.itemView.thumbnail.setImageBitmap(itemList[position].thumbnail)
            it.itemView.raw_data.text = itemList[position].raw
            it.itemView.getCheck.isChecked = false
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null

    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun removeSelected() {
        val deleteList= mutableListOf<String>()
        recyclerView!!.forEachChild {
            if (it.getCheck.isChecked) {
               deleteList.add(it.raw_data.toString())
            }
        }
        itemListener.onDelete(deleteList)
    }

    fun updateData(newList: List<TicketUtils.TicketItemFormat>) {
        Log.d("datasize ", "old ${itemList.size}   new ${newList.size}")
        DiffUtil.calculateDiff(RecyclerDiffCallback(itemList, newList), true).dispatchUpdatesTo(this)
        itemList = newList
    }


}