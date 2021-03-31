package com.communisolve.foodversy.callbacks

import com.communisolve.foodversy.model.Order

interface ILoadOrderCallbaclListner {
    fun onLoadOrdersSuccess(orderList:List<Order>)
    fun onLoadOrdersFailed(message:String)
}
