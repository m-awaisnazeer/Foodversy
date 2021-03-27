package com.communisolve.foodversy.database

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class LocalCartDataSource(private var cartDao: CartDao) : CartDataSource {
    override fun getAllCart(uid: String): Flowable<List<CartItem>> {
        return cartDao.getAllCart(uid)
    }

    override fun countItemInCart(uid: String): Single<Int> {
        return cartDao.countItemInCart(uid)
    }

    override fun sumPrice(uid: String): Single<Long> {
        return cartDao.sumPrice(uid)
    }

    override fun getItemCart(foodId: String, uid: String): Single<CartItem> {
        return cartDao.getItemCart(foodId, uid)
    }

    override fun insertOrReplaceAll(vararg cartItem: CartItem): Completable {
        return cartDao.insertOrReplaceAll(*cartItem)
    }

    override fun updateCart(cartItem: CartItem): Single<Int> {
        return cartDao.updateCart(cartItem)
    }

    override fun deleteCart(cartItem: CartItem): Single<Int> {
return cartDao.deleteCart(cartItem)   }

    override fun cleanCart(uid: String): Single<Int> {
return cartDao.cleanCart(uid)   }


}