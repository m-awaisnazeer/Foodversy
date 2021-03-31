package com.communisolve.foodversy.callbacks

import com.communisolve.foodversy.model.CommentModel
import com.communisolve.foodversy.model.Order

interface ILoadTimeFromFirebaseCallback {
    fun onLoadTimeSuccess(order: Order,estimatedTimeMS:Long)
    fun onLoadTimeFailed(message:String)
}