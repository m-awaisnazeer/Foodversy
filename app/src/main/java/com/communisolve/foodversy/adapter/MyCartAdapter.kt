package com.communisolve.foodversy.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.communisolve.foodversy.EventBus.UpdateItemInCart
import com.communisolve.foodversy.R
import com.communisolve.foodversy.callbacks.IOnCartItemMenuClickListner
import com.communisolve.foodversy.database.CartDataSource
import com.communisolve.foodversy.database.CartDatabase
import com.communisolve.foodversy.database.CartItem
import com.communisolve.foodversy.database.LocalCartDataSource
import com.communisolve.foodversy.databinding.LayoutCartItemBinding
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus

class MyCartAdapter(
    internal var context: Context,
    internal var iOnCartItemMenuClickListner: IOnCartItemMenuClickListner,
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

        binding.cartItemMenu.setOnClickListener {
            showPopupMenu(it,position,currentCartItem)
        }

    }

    private fun showPopupMenu(view: View?, position: Int, currentCartItem: CartItem) {
        var popupmenu: androidx.appcompat.widget.PopupMenu = androidx.appcompat
            .widget.PopupMenu(view!!.context, view!!)
        popupmenu.inflate(R.menu.cart_item_pop_up_menu)
        popupmenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete -> {
                    iOnCartItemMenuClickListner.onDeleteselected(position,currentCartItem)
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener false
                }
            }
        }
        popupmenu.show()
    }


    override fun getItemCount(): Int = cartItems.size

}

