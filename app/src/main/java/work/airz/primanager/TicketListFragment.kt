package work.airz.primanager

import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_ticket_list.view.*
import work.airz.primanager.qr.QRUtil

/**
 * フォロチケ/コーデチケット/会員証のリストを表示するためのフラグメント
 * 特定のデータ形式でデータを受け取ってそれをリストにセット、また、
 */
class TicketListFragment : Fragment() {

    private lateinit var iTicketList: IItemsList
    private lateinit var adapter: RecyclarViewAdapter
    private lateinit var ticketType: QRUtil.TicketType

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_ticket_list, container, false)
        ticketType = if (arguments != null) {
            arguments!!.getSerializable(QRUtil.TICKET_TYPE) as? QRUtil.TicketType ?: return view
        } else {
            Log.d("qrtype","QRType is NULL!!!!!!")
            QRUtil.TicketType.PRICHAN_MEMBERS
        }
        view.ticket_recyclerview.layoutManager = LinearLayoutManager(context)
        adapter = RecyclarViewAdapter(context, iTicketList, iTicketList.onItemList(ticketType), ticketType)
        view.ticket_recyclerview.adapter = adapter
        return view
    }

    override fun onResume() {
        super.onResume()
        adapter.updateData(iTicketList.onItemList(ticketType))
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        attach()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        attach()
    }

    private fun attach() {
        if (activity !is IItemsList) {
            throw  UnsupportedOperationException(
                    "Listener is not Implementation.")
        } else {
            iTicketList = activity as IItemsList
        }
    }

}
