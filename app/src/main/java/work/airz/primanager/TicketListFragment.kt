package work.airz.primanager

import android.content.Context
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_ticket_list.view.*

/**
 * フォロチケ/コーデチケット/会員証のリストを表示するためのフラグメント
 * 特定のデータ形式でデータを受け取ってそれをリストにセット、また、
 */
class TicketListFragment : Fragment() {

    private var iItemsList: IItemsList? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_ticket_list, container, false)
        view.ticket_recyclerview.adapter = RecyclarViewAdapter(activity!!.applicationContext, iItemsList, iItemsList!!.onItemList())
        view.ticket_recyclerview.layoutManager = LinearLayoutManager(context)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (activity !is IItemsList) {
            throw  UnsupportedOperationException(
                    "Listener is not Implementation.")
        } else {
            iItemsList = activity as IItemsList
        }
    }

}
