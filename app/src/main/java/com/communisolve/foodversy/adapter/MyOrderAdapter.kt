package com.communisolve.foodversy.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.communisolve.foodversy.R
import com.communisolve.foodversy.callbacks.IOnOrderItemMenuClickListener
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.databinding.LayoutOrderItemBinding
import com.communisolve.foodversy.model.Order
import java.text.SimpleDateFormat
import java.util.*

class MyOrderAdapter(
    var context: Context, var ordersList: List<Order>,
    var iOnOrderItemMenuClickListener: IOnOrderItemMenuClickListener
) :
    RecyclerView.Adapter<MyOrderAdapter.ViewHolder>() {
    lateinit var binding: LayoutOrderItemBinding

    internal var calender: Calendar
    internal var simpleDateFormat: SimpleDateFormat

    init {
        calender = Calendar.getInstance()
        simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = LayoutOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(ordersList[position]!!.cartItemList!![0]!!.foodImage)
            .into(binding.imgOrder)

        calender.timeInMillis = ordersList[position].createDate
        val date = Date(ordersList[position].createDate)
        binding.txtOrderDate.text =
            StringBuilder(Common.getDateOfWeek(calender.get(Calendar.DAY_OF_WEEK)))
                .append(" ")
                .append(simpleDateFormat.format(date))

        // Toast.makeText(context, "${calender.get(Calendar.DAY_OF_WEEK)}", Toast.LENGTH_SHORT).show()


        binding.txtOrderNumber.text =
            StringBuilder("Order Number: ").append(ordersList[position].orderNumber)
        binding.txtOrderComment.text =
            StringBuilder("Comment: ").append(ordersList[position].comment)
        binding.txtOrderStatus.text =
            StringBuilder("Staus: ").append(Common.convertStatusToText(ordersList[position].orderStatus))

        binding!!.orderItemMenu.setOnClickListener {
            showPopupMenu(it,position,ordersList.get(position))
        }
    }

    override fun getItemCount(): Int = ordersList.size

    private fun showPopupMenu(view: View?, position: Int, orderModel: Order) {
        var popupmenu: androidx.appcompat.widget.PopupMenu = androidx.appcompat
            .widget.PopupMenu(view!!.context, view!!)
        popupmenu.inflate(R.menu.edit_order_menu)
        popupmenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_tracking_order -> {
                    iOnOrderItemMenuClickListener.onTrackingOrderClick(
                        position,
                        orderModel
                    )
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener false
                }
            }
        }
        popupmenu.show()
    }

}
