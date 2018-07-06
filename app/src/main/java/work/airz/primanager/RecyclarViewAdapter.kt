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
    private var pagedList = itemPager.next()
    private var currentSize = itemPager.getPagedItemSize()


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
//                Log.d("onscrolled!", "called")
                val totalItemCount = recyclerView.adapter.itemCount
                val visibleItemCount = recyclerView.childCount
                val firstVisibleItem = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (isLoading && totalItemCount > prevTotal) {
                    isLoading = false
                    prevTotal = totalItemCount
                }
                if (!isLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 20) && totalItemCount < itemPager.getOutlineSize()) {
                    Log.d("paged!", itemPager.getPageNum().toString())
                    recyclerView.post { updateData(isIncrementPage = true) }
                }
            }
        })
        return RecyclerViewHolder(ticketItemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder?.itemView.apply {
            titleText.text = pagedList!![position].title
            descriptionText.text = pagedList!![position].description
            thumbnail.setImageBitmap(pagedList!![position].thumbnail)
            raw_data.text = pagedList!![position].raw
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
//        Log.d("getItemCount", currentSize.toString())
        return currentSize
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
                recyclerView!!.layoutManager.scrollToPosition(0)
//                itemPager.resetCursor()
                itemListener.onDelete(deleteSet.toList(), ticketType)
                updateData(true)
            }
            setNegativeButton("いいえ", null)
        }.show()
    }

    fun updateData(isFullUpdate: Boolean = false, isIncrementPage: Boolean = false) {
        Log.d("update data", "Called!")
        if (isFullUpdate) itemPager.refreshOutline()
        UpDateAsync().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, this, isIncrementPage) //シリアルでasynctaskしてくれる
    }

    companion object {
        private class UpDateAsync : AsyncTask<Any, Void, List<TicketUtils.TicketItemFormat>>() {
            var recyclerViewAdapter: RecyclarViewAdapter? = null
            var isIncrementPage = false

            override fun doInBackground(vararg params: Any?): List<TicketUtils.TicketItemFormat>? {
                if (params.size != 2) return null
                recyclerViewAdapter = params[0] as? RecyclarViewAdapter ?: return null
                isIncrementPage = params[1] as? Boolean ?: return null
                Log.d("is increment page", isIncrementPage.toString())
                Log.d("fetched list", "called")
                var itemPager = recyclerViewAdapter?.itemPager!!

                return if (isIncrementPage) itemPager.next() else itemPager.getCurrentPage()
            }

            override fun onPostExecute(result: List<TicketUtils.TicketItemFormat>?) {
                super.onPostExecute(result)
                result ?: return
                Log.d("apply update list", "called")
                var oldList = recyclerViewAdapter!!.pagedList!!.toList() //tolistでコピー
                recyclerViewAdapter!!.pagedList = result
                DiffUtil.calculateDiff(RecyclerDiffCallback(oldList, result), false).dispatchUpdatesTo(recyclerViewAdapter)
                recyclerViewAdapter!!.currentSize = recyclerViewAdapter?.itemPager!!.getPagedItemSize() //ページ数を遅延更新にしないとバインドでコケる
            }
        }
    }

}