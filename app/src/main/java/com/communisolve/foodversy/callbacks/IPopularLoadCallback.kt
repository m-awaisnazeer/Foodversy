package com.communisolve.foodversy.callbacks

import com.communisolve.foodversy.model.PopularCategoryModel

interface IPopularLoadCallback {

    fun onPopularLoadSuccess(popularCategoryModels: List<PopularCategoryModel>)
    fun onPopularLoadFailed(message: String)
}