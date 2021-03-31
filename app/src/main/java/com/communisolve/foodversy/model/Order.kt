package com.communisolve.foodversy.model

import com.communisolve.foodversy.database.CartItem

class Order(
    var userId: String = "",
    var userName: String = "",
    var userPhone: String = "",
    var shippingAddress: String = "",
    var comment: String = "",
    var transactionId: String = "",
    var lat: Double = 0.toDouble(),
    var lng: Double = 0.toDouble(),
    var totalPayment: Double = 0.toDouble(),
    var finalPayment: Double = 0.toDouble(),
    var isCod: Boolean = false,
    var discount: Int = 0,
    var cartItemList: List<CartItem>? = null,
    var orderNumber: String = "",
    var orderStatus:Int=0
) {
}