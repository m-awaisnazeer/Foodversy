package com.communisolve.foodversy.callbacks

import com.communisolve.foodversy.model.CategoryModel

interface ICategoryCallBackListner {
    fun onCategoryLoadSuccess(categoryModels: List<CategoryModel>)
    fun onCategoryLoadFailed(message: String)
}
