package com.communisolve.foodversy.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.communisolve.foodversy.EventBus.CategoryClick
import com.communisolve.foodversy.EventBus.FoodItemClick
import com.communisolve.foodversy.callbacks.IRecyclerItemClickLitner
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.databinding.LayoutCategoryItemBinding
import com.communisolve.foodversy.databinding.LayoutFoodItemBinding
import com.communisolve.foodversy.model.FoodModel
import org.greenrobot.eventbus.EventBus

class MyFoodListAdapter(
    internal var context: Context,
    internal var foodsList: List<FoodModel>

) : RecyclerView.Adapter<MyFoodListAdapter.ViewHolder>() {
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
        Glide.with(context).load(foodsList.get(position).image).into(binding!!.imgFoodList )
        binding!!.txtFoodName.setText(foodsList.get(position).name)
        binding!!.txtFoodPrice.setText(foodsList.get(position).price.toString())

        holder.setListner(object :IRecyclerItemClickLitner{
            override fun onItemClick(view: View, pos: Int) {
                Common.foodSelected = foodsList.get(pos)
                EventBus.getDefault().postSticky(FoodItemClick(true,foodsList.get(pos)))
            }

        })
    }


    override fun getItemCount(): Int {
        return foodsList.size
    }
}