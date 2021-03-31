package com.communisolve.foodversy.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.databinding.LayoutOrderItemBinding
import com.communisolve.foodversy.model.Order
import java.text.SimpleDateFormat
import java.util.*

class MyOrderAdapter(var context: Context, var ordersList: List<Order>) :
    RecyclerView.Adapter<MyOrderAdapter.ViewHolder>() {
    lateinit var binding: LayoutOrderItemBinding

    internal var calender:Calendar
    internal var simpleDateFormat:SimpleDateFormat

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
        /*
        calender.timeInMillis = ordersList[position].createDate
        val date = Date(ordersList[position].createDate)
        binding.txtOrderDate.text = StringBuilder(Common.getDateOfWeek(calender.get(Calendar.DAY_OF_WEEK)))
            .append(" ")
            .append(simpleDateFormat.format(date))
         */

        binding.txtOrderNumber.text = StringBuilder("Order Number: ").append(ordersList[position].orderNumber)
        binding.txtOrderComment.text = StringBuilder("Comment: ").append(ordersList[position].comment)
        binding.txtOrderStatus.text = StringBuilder("").append(Common.convertStatusToText(ordersList[position].orderStatus))

    }

    override fun getItemCount(): Int = ordersList.size

}
