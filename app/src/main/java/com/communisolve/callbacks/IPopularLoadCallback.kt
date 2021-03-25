package com.communisolve.callbacks

import com.communisolve.foodversy.model.PopularCategoryModel

interface IPopularLoadCallback {

    fun onPopularLoadSuccess(popularCategoryModels: List<PopularCategoryModel>)
    fun onPopularLoadFailed(message: String)
}