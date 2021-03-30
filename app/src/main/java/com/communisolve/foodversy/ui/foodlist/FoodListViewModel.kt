package com.communisolve.foodversy.ui.foodlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.model.FoodModel

class FoodListViewModel : ViewModel() {

    private var mutablefoodListData: MutableLiveData<List<FoodModel>>? = null

    fun getMutavleFoodliveData(): MutableLiveData<List<FoodModel>> {
        if (mutablefoodListData == null) {
            mutablefoodListData = MutableLiveData()
            mutablefoodListData!!.value = Common.categorySelected!!.foods

        }
        return mutablefoodListData!!
    }

}