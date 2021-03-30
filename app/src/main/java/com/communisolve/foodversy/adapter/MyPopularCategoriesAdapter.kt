package com.communisolve.foodversy.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.communisolve.foodversy.EventBus.PopularFoodItemClick
import com.communisolve.foodversy.callbacks.IRecyclerItemClickLitner
import com.communisolve.foodversy.databinding.LayoutPopularCategoriesItemBinding
import com.communisolve.foodversy.model.PopularCategoryModel
import org.greenrobot.eventbus.EventBus

class MyPopularCategoriesAdapter(
    internal var context: Context,
    internal var popularCategoryModels: List<PopularCategoryModel>
) : RecyclerView.Adapter<MyPopularCategoriesAdapter.ViewHolder>() {
    lateinit var binding: LayoutPopularCategoriesItemBinding

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

        binding = LayoutPopularCategoriesItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(popularCategoryModels.get(position).image)
            .into(binding.categoryImage)
        binding.txtCategoryName.setText(popularCategoryModels.get(position).name)

        holder.setListner(object : IRecyclerItemClickLitner {
            override fun onItemClick(view: View, pos: Int) {
                EventBus.getDefault().postSticky(PopularFoodItemClick(popularCategoryModels[pos]))
            }

        })
    }

    override fun getItemCount(): Int {
        return popularCategoryModels.size
    }
}