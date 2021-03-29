package com.communisolve.foodversy.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.communisolve.foodversy.EventBus.UpdateItemInCart
import com.communisolve.foodversy.database.CartDataSource
import com.communisolve.foodversy.database.CartDatabase
import com.communisolve.foodversy.database.CartItem
import com.communisolve.foodversy.database.LocalCartDataSource
import com.communisolve.foodversy.databinding.LayoutCartItemBinding
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus

class MyCartAdapter(
    internal var context: Context,
    internal var cartItems: List<CartItem>
) : RecyclerView.Adapter<MyCartAdapter.ViewHolder>() {

    private val compositeDisposable: CompositeDisposable
    private val cartDataSource: CartDataSource

    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).CartDao())
    }

    private lateinit var binding: LayoutCartItemBinding

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = LayoutCartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentCartItem: CartItem = cartItems.get(position)

        Glide.with(context).load(currentCartItem.foodImage).into(binding.imgCart)


        binding.txtFoodName.text = StringBuilder(currentCartItem.foodName)
        binding.txtFoodPrice.text =
            StringBuilder("").append(currentCartItem.foodPrice + currentCartItem.foodExtraPrice)
                .toString()
        binding.numberButton.number = currentCartItem.foodQuantity.toString()

        binding.numberButton.setOnValueChangeListener { view, oldValue, newValue ->
            currentCartItem.foodQuantity = newValue
            EventBus.getDefault().postSticky(UpdateItemInCart(currentCartItem))
        }

    }

    override fun getItemCount(): Int = cartItems.size

}