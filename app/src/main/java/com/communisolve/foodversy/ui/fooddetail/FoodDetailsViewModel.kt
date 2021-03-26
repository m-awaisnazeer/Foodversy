package com.communisolve.foodversy.ui.fooddetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.communisolve.foodversy.model.FoodModel

class FoodDetailsViewModel : ViewModel() {
    private var mutableLiveDataFood: MutableLiveData<FoodModel>? = null

    fun getMutableLiveDataFood(): MutableLiveData<FoodModel> {
        if (mutableLiveDataFood == null) {
            mutableLiveDataFood = MutableLiveData()
            mutableLiveDataFood!!.value = com.communisolve.foodversy.common.Common.foodSelected
        }
        return mutableLiveDataFood!!
    }

}