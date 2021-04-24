package com.communisolve.foodversy.model

class ShippingOrderModel(
    var shipperPhone:String="",
    var shipperName:String="",
    var currentLat:Double=0.0,
    var currentLng:Double=0.0,
    var orderModel: Order?=null,
    var isStartTrip:Boolean=false
) {

}
