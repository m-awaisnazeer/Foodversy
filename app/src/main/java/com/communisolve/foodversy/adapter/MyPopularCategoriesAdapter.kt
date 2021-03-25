package com.communisolve.foodversy.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.communisolve.foodversy.databinding.LayoutPopularCategoriesItemBinding
import com.communisolve.foodversy.model.PopularCategoryModel

class MyPopularCategoriesAdapter(
    internal var context: Context,
    internal var popularCategoryModels: List<PopularCategoryModel>
) : RecyclerView.Adapter<MyPopularCategoriesAdapter.ViewHolder>() {
    lateinit var binding: LayoutPopularCategoriesItemBinding

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

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
    }

    override fun getItemCount(): Int {
        return popularCategoryModels.size
    }
}