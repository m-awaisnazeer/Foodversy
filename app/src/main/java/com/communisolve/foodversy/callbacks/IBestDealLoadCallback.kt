package com.communisolve.foodversy.callbacks

import com.communisolve.foodversy.model.BestDealsModel

interface IBestDealLoadCallback {
    fun onBestDealLoadSuccess(bestDealsModels: List<BestDealsModel>)
    fun onBestDealLoadFailed(message: String)
}