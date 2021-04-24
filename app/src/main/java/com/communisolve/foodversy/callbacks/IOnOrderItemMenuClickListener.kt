package com.communisolve.foodversy.callbacks

import com.communisolve.foodversy.model.Order

interface IOnOrderItemMenuClickListener {
    fun onCancelOrderClick(pos:Int,orderModel:Order)
    fun onTrackingOrderClick(pos:Int,orderModel:Order)

}