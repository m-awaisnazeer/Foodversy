package com.communisolve.callbacks

import com.communisolve.foodversy.model.BestDealsModel
import com.communisolve.foodversy.model.PopularCategoryModel

interface IBestDealLoadCallback {
    fun onBestDealLoadSuccess(bestDealsModels: List<BestDealsModel>)
    fun onBestDealLoadFailed(message: String)
}