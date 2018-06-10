package work.airz.primanager

import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_ticket_list.view.*

/**
 *
 */

class TicketListFragment : Fragment(), RecyclerViewHolder.ItemClickListener {

    private var fragmentListener: RecyclerViewHolder.ItemClickListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_ticket_list, container, false)
        view.ticket_recyclerview.adapter = RecyclarViewAdapter(activity!!.applicationContext, this, listOf("cv", "test"))
        view.ticket_recyclerview.layoutManager = LinearLayoutManager(context)

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        if (activity !is RecyclerViewHolder.ItemClickListener) {
//            throw  UnsupportedOperationException(
//                    "Listener is not Implementation.");
//        } else {
//            fragmentListener = activity as RecyclerViewHolder.ItemClickListener
//        }
    }


    override fun onItemClick(view: View, position: Int) {
        Toast.makeText(context, "position $position was tapped", Toast.LENGTH_SHORT).show()
    }
}
