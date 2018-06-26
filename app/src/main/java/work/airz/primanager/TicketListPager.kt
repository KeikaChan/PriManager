package work.airz.primanager

import android.graphics.Bitmap
import android.util.Log
import work.airz.primanager.qr.QRUtil

abstract class TicketListPager(private val pageSize: Int = 25, private val ticketType: QRUtil.TicketType) {
    private var outlineData: List<TicketUtils.TicketOutlineFormat>

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

    fun refreshOutline() {
        Log.d("refresh", "refresh called!!")
        outlineData = onDBOutline(ticketType)
    }

    fun getPage(pageNum: Int): List<TicketUtils.TicketItemFormat> {
        val startIndex = pageNum * pageSize
        val endIndex = if ((pageNum + 1) * pageSize > outlineData.size) outlineData.size - 1 else (pageNum + 1) * pageSize - 1
        return getList(startIndex, endIndex)
    }

    fun getPagedList(pageNum: Int): List<TicketUtils.TicketItemFormat> {
        Log.d("getPagedList", "pagenum ${pageNum}")
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
        Log.d("getList", "start ${startIndex} to end ${endIndex}")
        var resultList = mutableListOf<TicketUtils.TicketItemFormat>()
        for (index in startIndex..endIndex) {
            var target = outlineData[index]

            resultList.add(TicketUtils.TicketItemFormat(target.title, target.description, onDBImage(target.raw), target.raw))
        }
        return resultList.toList()
    }

    fun getPagedSize(pageNum: Int): Int {
        return if ((pageNum + 1) * pageSize > outlineData.size) outlineData.size else (pageNum + 1) * pageSize
    }

    fun getOutlineSize(): Int = outlineData.size

}