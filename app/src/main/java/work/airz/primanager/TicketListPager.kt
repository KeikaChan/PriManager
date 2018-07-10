package work.airz.primanager

import android.graphics.Bitmap
import android.util.Log
import work.airz.primanager.qr.QRUtil

abstract class TicketListPager(private val pageSize: Int = 20, private val ticketType: QRUtil.TicketType) {
    private var outlineData: List<TicketUtils.TicketOutlineFormat>
    private var page = 0

    init {
        if (pageSize <= 1) throw IllegalArgumentException("page size must be natural number.")
        outlineData = onDBOutline(ticketType)
    }


    /**
     * DBからデータを取ってくる
     */
    abstract fun onDBOutline(ticketType: QRUtil.TicketType): List<TicketUtils.TicketOutlineFormat>


    /**
     * 画像追加用のフォーマット。
     * 引数のリストにそのまま追加する
     */
    abstract fun onDBImage(rawData: String): Bitmap


    /**
     * ページ数取得
     * ページングしたアイテム数を取得する場合は
     * getPagedSizeを使うこと!!
     */
    fun getPageNum(): Int {
        return page
    }

    /**
     * ページ数のリセット
     */
    fun resetCursor() {
        page = 0
    }

    fun refreshOutline() {
        Log.d("refresh", "refresh called!!")
        outlineData = onDBOutline(ticketType)
    }

    fun next(): List<TicketUtils.TicketItemFormat>? {
        if (page * pageSize > outlineData.size) return null
        page++
        Log.d("increment Page", "page is ${page}")
        return getCurrentPage()
    }

    /**
     * 現在のページのデータ
     */
    fun getCurrentPage(): List<TicketUtils.TicketItemFormat>? {
        var pagedList = getPagedList(page)
        return if (pagedList.isNotEmpty()) pagedList else null
    }


    private fun getPageNum(pageNum: Int): List<TicketUtils.TicketItemFormat> {
        val startIndex = pageNum * pageSize
        val endIndex = if ((pageNum + 1) * pageSize > outlineData.size) outlineData.size - 1 else (pageNum + 1) * pageSize - 1
        return getList(startIndex, endIndex)
    }

    private fun getPagedList(pageNum: Int): List<TicketUtils.TicketItemFormat> {
        Log.d("getPagedList", "pagenum ${pageNum} outline size ${outlineData.size}")
        val startIndex = 0
        val endIndex = if ((pageNum + 1) * pageSize > outlineData.size) outlineData.size - 1 else (pageNum + 1) * pageSize - 1
        return getList(startIndex, endIndex)
    }

    fun getImage(position: Int): Bitmap {
        return onDBImage(outlineData[position].raw)
    }

    /**
     * 指定されたサイズのリストを取得する
     */
    private fun getList(startIndex: Int, endIndex: Int): List<TicketUtils.TicketItemFormat> {
        var resultList = mutableListOf<TicketUtils.TicketItemFormat>()
        for (index in startIndex..endIndex) {
            var target = outlineData[index]

            resultList.add(TicketUtils.TicketItemFormat(target.title, target.description, Bitmap.createScaledBitmap(onDBImage(target.raw), 75, 75, false), target.raw))
        }

        Log.d("getList", "start ${startIndex} to end ${endIndex} result size ${resultList.size.toString()}")
        return resultList.toList()
    }

    /**
     * ページング済みのアイテム数を返す。
     */
    fun getPagedItemSize(): Int {
        return if ((page + 1) * pageSize > outlineData.size) outlineData.size else (page + 1) * pageSize
    }

    fun getOutlineSize(): Int = outlineData.size

}