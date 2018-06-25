package work.airz.primanager

import android.graphics.Bitmap

abstract class TicketListPager(private val pageSize: Int) {
    init {
        if (pageSize <= 1) throw IllegalArgumentException("page size must be natural number.")
    }

    private var outlineData: List<TicketUtils.TicketItemFormat>? = null

    /**
     * DBからデータを取ってくる
     */
    abstract fun onDBOutline(): List<TicketUtils.TicketItemFormat>


    /**
     * 画像追加用のフォーマット。
     * 引数のリストにそのまま追加する
     */
    abstract fun onDBImage(rawData: String): Bitmap

    fun setOutline() {
        outlineData = onDBOutline()
    }

    fun getPage(pageNum: Int): List<TicketUtils.TicketItemFormat> {
        val startIndex = pageNum * pageSize
        val endIndex = if ((pageNum + 1) * pageSize > outlineData!!.size) outlineData!!.size - 1 else (pageNum + 1) * pageSize - 1
        var resultList = mutableListOf<TicketUtils.TicketItemFormat>()
        for (index in startIndex..endIndex) {
            var target = outlineData!![index]

            resultList.add(TicketUtils.TicketItemFormat(target.title, target.description, onDBImage(target.raw), target.raw))
        }
        return resultList.toList()
    }

}