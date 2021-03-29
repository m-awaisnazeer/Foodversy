package com.communisolve.foodversy.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.communisolve.foodversy.EventBus.CounterCartEvent
import com.communisolve.foodversy.EventBus.FoodItemClick
import com.communisolve.foodversy.callbacks.IRecyclerItemClickLitner
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.database.CartDataSource
import com.communisolve.foodversy.database.CartDatabase
import com.communisolve.foodversy.database.CartItem
import com.communisolve.foodversy.database.LocalCartDataSource
import com.communisolve.foodversy.databinding.LayoutFoodItemBinding
import com.communisolve.foodversy.model.FoodModel
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

class MyFoodListAdapter(
    internal var context: Context,
    internal var foodsList: List<FoodModel>

) : RecyclerView.Adapter<MyFoodListAdapter.ViewHolder>() {

    private val compositeDisposable: CompositeDisposable
    private val cartDataSource: CartDataSource

    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).CartDao())
    }

    var binding: LayoutFoodItemBinding? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var listner: IRecyclerItemClickLitner? = null

        fun setListner(listner: IRecyclerItemClickLitner) {
            this.listner = listner
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            listner!!.onItemClick(view!!, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding =
            LayoutFoodItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentFoodModel = foodsList.get(position)
        Glide.with(context).load(foodsList.get(position).image).into(binding!!.imgFoodList)
        binding!!.txtFoodName.setText(foodsList.get(position).name)
        binding!!.txtFoodPrice.setText(foodsList.get(position).price.toString())

        holder.setListner(object : IRecyclerItemClickLitner {
            override fun onItemClick(view: View, pos: Int) {
                Common.foodSelected = foodsList.get(pos)
                Common.foodSelected.key = pos.toString()
                EventBus.getDefault().postSticky(FoodItemClick(true, foodsList.get(pos)))
            }

        })

        binding!!.imgQuickCart.setOnClickListener {

            val cartItem: CartItem = CartItem(
                currentFoodModel.id,
                currentFoodModel.name,
                currentFoodModel.image,
                currentFoodModel.price.toDouble(),
                1,
                "Default",
                "Default",
                Common.currentUser!!.phone,
                0.0,
                Common.currentUser!!.uid
            )

            cartDataSource.getItemWithAllOptionsInCart(
                Common.currentUser!!.uid,
                cartItem.foodId,
                cartItem.foodSize,
                cartItem.foodAddon
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<CartItem> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(cartItemFromDB: CartItem) {
                        if (cartItemFromDB.equals(cartItem)) {

                            //if item already in database, just update

                            cartItemFromDB.foodExtraPrice = cartItem.foodExtraPrice
                            cartItemFromDB.foodAddon = cartItem.foodAddon
                            cartItemFromDB.foodSize = cartItem.foodSize
                            cartItemFromDB.foodQuantity = cartItem.foodQuantity

                            cartDataSource.updateCart(cartItemFromDB)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : SingleObserver<Int> {
                                    override fun onSubscribe(d: Disposable) {


                                    }

                                    override fun onSuccess(t: Int) {
                                        Toast.makeText(
                                            context,
                                            "Update Cart Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        EventBus.getDefault().postSticky(CounterCartEvent(true))
                                    }

                                    override fun onError(e: Throwable) {
                                        Toast.makeText(
                                            context,
                                            "[Update Cart Error]: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                })
                        }
                        else
                        {
                            //if item not aviable in database , just insert
                            compositeDisposable.add(
                                cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Toast.makeText(
                                            context,
                                            "Add to Cart Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        EventBus.getDefault().postSticky(CounterCartEvent(true))
                                    }, {
                                        Toast.makeText(
                                            context,
                                            "Error ${it.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    })
                            )
                        }
                    }

                    override fun onError(e: Throwable) {
                        if (e.message!!.contains("empty")) {
                            compositeDisposable.add(
                                cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Toast.makeText(
                                            context,
                                            "Add to Cart Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        EventBus.getDefault().postSticky(CounterCartEvent(true))
                                    }, {
                                        Toast.makeText(
                                            context,
                                            "Error ${it.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    })
                            )
                        } else
                            Toast.makeText(context, "Error ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }

    fun onStop() {
        compositeDisposable.clear()
    }

    override fun getItemCount(): Int {
        return foodsList.size
    }
}