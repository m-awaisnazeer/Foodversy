package com.communisolve.foodversy.ui.cart

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.database.CartDataSource
import com.communisolve.foodversy.database.CartDatabase
import com.communisolve.foodversy.database.CartItem
import com.communisolve.foodversy.database.LocalCartDataSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CartViewModel : ViewModel() {
    private val compositeDisposable: CompositeDisposable
    private var cartDataSource: CartDataSource?=null
    private var mutableLiveDataCartItem: MutableLiveData<List<CartItem>>? = null

    init {
        compositeDisposable = CompositeDisposable()
    }

    fun initCartDatabase(context: Context){
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).CartDao())
    }

    fun getMutableLivwDataCartItem(): MutableLiveData<List<CartItem>> {
        if (mutableLiveDataCartItem == null)
            mutableLiveDataCartItem = MutableLiveData()
        getCartItems()
        return mutableLiveDataCartItem!!
    }

    private fun getCartItems() {
        compositeDisposable.addAll(
            cartDataSource!!.getAllCart(Common.currentUser!!.uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ cartItems ->
                    mutableLiveDataCartItem!!.value = cartItems

                }, {
                    mutableLiveDataCartItem!!.value = null
                })
        )
    }

    fun onStop(){
        compositeDisposable.clear()
    }
}