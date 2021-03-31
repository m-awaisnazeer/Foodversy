package com.communisolve.foodversy.remote

import com.communisolve.foodversy.model.BraintreeToken
import com.communisolve.foodversy.model.BraintreeTransaction
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.GET

interface ApiService {
    @GET("token")
    fun getToken():Observable<BraintreeToken>

    @GET("checkout")
    fun submitAPIPayment(@Field("amount") amount:Double,
    @Field("payment_method_nonce") nonce:String):Observable<BraintreeTransaction>
}