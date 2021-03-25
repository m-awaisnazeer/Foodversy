package com.communisolve.foodversy.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.databinding.LayoutCategoryItemBinding
import com.communisolve.foodversy.model.CategoryModel

class MyCategoriesAdapter(
    internal var context: Context,
    internal var CategoriesList: List<CategoryModel>

) : RecyclerView.Adapter<MyCategoriesAdapter.ViewHolder>() {
    var binding: LayoutCategoryItemBinding? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding =
            LayoutCategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(CategoriesList.get(position).image).into(binding!!.imgCategory)
        binding!!.txtCategoryName.setText(CategoriesList.get(position).name)
    }

    override fun getItemViewType(position: Int): Int {
        return if (CategoriesList.size ==1)
            Common.DEFAULT_COLUMN_COUNT
        else{
            if (CategoriesList.size %2 ==0){
                Common.DEFAULT_COLUMN_COUNT
            }else{
                if (position > 1 && position == CategoriesList.size-1) Common.FULL_WIDTH_COLUMN else Common.DEFAULT_COLUMN_COUNT
            }
        }

//        return super.getItemViewType(position)
    }
    override fun getItemCount(): Int {
        return CategoriesList.size
    }
}