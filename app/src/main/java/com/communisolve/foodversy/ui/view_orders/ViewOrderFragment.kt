package com.communisolve.foodversy.ui.view_orders

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.communisolve.foodversy.EventBus.MenuItemBack
import com.communisolve.foodversy.R
import com.communisolve.foodversy.adapter.MyOrderAdapter
import com.communisolve.foodversy.callbacks.ILoadOrderCallbaclListner
import com.communisolve.foodversy.callbacks.IOnOrderItemMenuClickListener
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.model.Order
import com.communisolve.foodversy.model.ShippingOrderModel
import com.communisolve.foodversy.ui.TrackingOrderActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus


class ViewOrderFragment : Fragment(), ILoadOrderCallbaclListner, IOnOrderItemMenuClickListener {

    private val viewModel: ViewOrderViewModel by viewModels()

    internal lateinit var dialog: AlertDialog
    internal var recycler_order: RecyclerView? = null

    internal lateinit var listner: ILoadOrderCallbaclListner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var root = inflater.inflate(R.layout.fragment_view_order, container, false)
        initViews(root)
        loadOrderFromFirebase()

        viewModel.mutableLiveDataOrderList.observe(viewLifecycleOwner, Observer {
            val adapter = MyOrderAdapter(requireContext(), it.asReversed(), this)
            recycler_order!!.adapter = adapter
        })
        return root
    }

    private fun loadOrderFromFirebase() {
        dialog.show()
        val orderList = ArrayList<Order>()
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
            .orderByChild("userId")
            .equalTo(Common.currentUser!!.uid)
            .limitToLast(100)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ordersnapshot in snapshot.children) {
                        val order = ordersnapshot.getValue(Order::class.java)
                        order!!.orderNumber = ordersnapshot!!.key!!
                        orderList.add(order)
                    }
                    listner.onLoadOrdersSuccess(orderList)

                }

                override fun onCancelled(error: DatabaseError) {
                    listner.onLoadOrdersFailed(error.message)

                }

            })


    }

    private fun initViews(root: View?) {
        listner = this

        dialog = SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()

        recycler_order = root!!.findViewById<RecyclerView>(R.id.recycler_order)

        recycler_order.apply {
            this!!.setHasFixedSize(true)
            var mLayoutManager = LinearLayoutManager(requireContext())
            this!!.layoutManager = mLayoutManager
            this!!.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    mLayoutManager.orientation
                )
            )
        }
    }

    override fun onLoadOrdersSuccess(orderList: List<Order>) {
        //implement late
        dialog.dismiss()

        viewModel.setMutableLiveDataOrderList(orderList)


    }

    override fun onLoadOrdersFailed(message: String) {
        dialog.dismiss()
        Toast.makeText(requireContext(), "${message}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }

    override fun onCancelOrderClick(pos: Int, orderModel: Order) {

    }

    override fun onTrackingOrderClick(pos: Int, orderModel: Order) {
        //Fetch From Firebase
        FirebaseDatabase.getInstance().getReference(Common.SHIPPING_ORDER_REF) //
            .child(orderModel.orderNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {

                        Common.currentShippingOrder =
                            snapshot.getValue(ShippingOrderModel::class.java)
                        if (Common.currentShippingOrder!!.currentLat != -1.0 &&
                            Common.currentShippingOrder!!.currentLng != -1.0
                        ) {
                            startActivity(Intent(requireContext(), TrackingOrderActivity::class.java))

                        }else{
                            Toast.makeText(
                                requireContext(),
                                "your order has not been shipped, please wait",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "you just place your order, please wait for it shipping",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
                }

            })

    }
}