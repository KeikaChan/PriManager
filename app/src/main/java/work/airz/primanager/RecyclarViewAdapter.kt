package work.airz.primanager

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.ticket_item.view.*
import work.airz.primanager.qr.QRUtil


class RecyclarViewAdapter(val context: Context, private val itemClickListener: IItemsList, private var itemList: List<TicketUtils.TicketItemFormat>, val ticketType: QRUtil.TicketType) : RecyclerView.Adapter<RecyclerViewHolder>() {
    var recyclerView: RecyclerView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val mView = layoutInflater.inflate(R.layout.ticket_item, parent, false)

        mView.setOnClickListener { view: View ->
            recyclerView?.let {
                itemClickListener.onItemClick(view, it.getChildAdapterPosition(view), ticketType)
            }
        }

        return RecyclerViewHolder(mView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder?.let {
            it.itemView.titleText.text = itemList[position].title
            it.itemView.descriptionText.text = itemList[position].description
            it.itemView.thumbnail.setImageBitmap(itemList[position].thumbnail)
            it.itemView.raw_data.text = itemList[position].raw
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

    fun changeList(newItemList: List<TicketUtils.TicketItemFormat>) {
        itemList = newItemList
        notifyDataSetChanged()
    }


}