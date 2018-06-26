package work.airz.primanager

import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.ticket_item.view.*
import org.jetbrains.anko.forEachChild
import work.airz.primanager.qr.QRUtil


class RecyclarViewAdapter(val context: Context?, private val itemListener: RecyclerViewHolder.IItemsList, private var itemPager: TicketListPager, private val ticketType: QRUtil.TicketType) : RecyclerView.Adapter<RecyclerViewHolder>() {
    var recyclerView: RecyclerView? = null
    private var page = 0
    private var pagedList = itemPager.getPagedList(page)
    private var updateFinished = true


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val ticketItemView = layoutInflater.inflate(R.layout.ticket_item, parent, false)

        ticketItemView.setOnClickListener { view: View ->
            recyclerView?.let {
                itemListener.onItemClick(view, it.getChildAdapterPosition(view), ticketType)
            }
        }

        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var prevTotal = 0
            var isLoading = true

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d("onscrolled!", "called")
                val totalItemCount = recyclerView.adapter.itemCount
                val visibleItemCount = recyclerView.childCount
                val firstVisibleItem = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (isLoading && totalItemCount > prevTotal) {
                    isLoading = false
                    prevTotal = totalItemCount
                }
                if (!isLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 20) && totalItemCount < itemPager.getOutlineSize()) {
                    page++
                    Log.d("paged!", page.toString())
                    recyclerView.post { updateData() }
                }
            }
        })
        return RecyclerViewHolder(ticketItemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder?.itemView.apply {
            titleText.text = pagedList[position].title
            descriptionText.text = pagedList[position].description
            thumbnail.setImageBitmap(pagedList[position].thumbnail)
            raw_data.text = pagedList[position].raw
            getCheck.isChecked = false
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
        return itemPager.getPagedSize(page)
    }


    /**
     * チェックボックスのついている項目を削除します
     */
    fun deleteSelected() {
        AlertDialog.Builder(context!!).apply {
            setTitle("データの削除")
            setCancelable(false)
            setMessage("選択しているデータを削除します。よろしいですか？")
            setPositiveButton("はい") { _, _ ->
                val deleteSet = hashSetOf<String>()
                recyclerView!!.forEachChild {
                    if (it.getCheck.isChecked) {
                        deleteSet.add(it.raw_data.text.toString())
                    }
                }
                itemListener.onDelete(deleteSet.toList(), ticketType)
                val newList = itemPager.getPagedList(page).filter { deleteSet.add(it.raw) }
                Log.d("list sizes", "old:${itemPager.getPagedSize(page)}, new:${newList.size}")
                updateData()
            }
            setNegativeButton("いいえ", null)
        }.show()
    }

    fun updateData() {
        Log.d("update data", "Called!")
        UpDateAsync().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,this) //シリアルでasynctaskしてくれる
    }

    companion object {
        private class UpDateAsync : AsyncTask<RecyclarViewAdapter, Void, List<TicketUtils.TicketItemFormat>>() {
            var recyclerViewAdapter: RecyclarViewAdapter? = null

            override fun doInBackground(vararg params: RecyclarViewAdapter?): List<TicketUtils.TicketItemFormat>? {
                if (params.size != 1) return null
                recyclerViewAdapter = params[0]
                Log.d("fetched list", "called")
                return recyclerViewAdapter?.itemPager?.getPagedList(recyclerViewAdapter!!.page)
            }

            override fun onPostExecute(result: List<TicketUtils.TicketItemFormat>?) {
                super.onPostExecute(result)
                result ?: return
                Log.d("apply update list", "called")
                val oldList = recyclerViewAdapter!!.pagedList.toList()
                recyclerViewAdapter!!.pagedList = result
                DiffUtil.calculateDiff(RecyclerDiffCallback(oldList, result), false).dispatchUpdatesTo(recyclerViewAdapter)

            }
        }
    }

}