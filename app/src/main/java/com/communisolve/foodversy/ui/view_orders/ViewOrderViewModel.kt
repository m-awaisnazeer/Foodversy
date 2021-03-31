package com.communisolve.foodversy.ui.view_orders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.FtsOptions
import com.communisolve.foodversy.model.Order

class ViewOrderViewModel: ViewModel() {
    val mutableLiveDataOrderList:MutableLiveData<List<Order>>

    init {
        mutableLiveDataOrderList = MutableLiveData()
    }

    fun setMutableLiveDataOrderList(orders:List<Order>)
    {
        mutableLiveDataOrderList.value = orders
    }
}