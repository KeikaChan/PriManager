package work.airz.primanager

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import kotlinx.android.synthetic.main.fragment_ticket_list.view.*
import work.airz.primanager.qr.QRUtil

/**
 * フォロチケ/コーデチケット/会員証のリストを表示するためのフラグメント
 * 特定のデータ形式でデータを受け取ってそれをリストにセット、また、
 */
class TicketListFragment : Fragment() {

    private lateinit var iTicketList: RecyclerViewHolder.IItemsList
    private lateinit var adapter: RecyclarViewAdapter
    private lateinit var ticketType: QRUtil.TicketType

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_ticket_list, container, false)
        ticketType = arguments!!.getSerializable(QRUtil.TICKET_TYPE) as? QRUtil.TicketType ?: return view
        view.ticket_recyclerview.layoutManager = LinearLayoutManager(context)
        adapter = RecyclarViewAdapter(context, iTicketList, iTicketList.onItemPager(ticketType), ticketType)
        view.ticket_recyclerview.adapter = adapter
        setHasOptionsMenu(true)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_ticket_list, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_action -> {
                adapter.deleteSelected()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        Log.d("update,",PriQRPrefsManager(context!!).getIsUpdate().toString())
        if (PriQRPrefsManager(context!!).getIsUpdate()) {
            adapter.updateData()
            PriQRPrefsManager(context!!).putIsUpdate(false)
        }
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
        if (activity !is RecyclerViewHolder.IItemsList) {
            throw  UnsupportedOperationException(
                    "Listener is not Implementation.") as Throwable
        } else {
            iTicketList = activity as RecyclerViewHolder.IItemsList
        }
    }

    override fun onDetach() {
        super.onDetach()
        val toolbar = activity!!.findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.menu.removeItem(R.id.delete_action)
    }


}
