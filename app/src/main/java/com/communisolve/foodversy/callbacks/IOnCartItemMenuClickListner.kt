package com.communisolve.foodversy.callbacks

import com.communisolve.foodversy.database.CartItem

interface IOnCartItemMenuClickListner {
    fun onDeleteselected(position: Int,deletedCartItem: CartItem)

}