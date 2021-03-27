package com.communisolve.foodversy.ui.fooddetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.communisolve.foodversy.model.CommentModel
import com.communisolve.foodversy.model.FoodModel

class FoodDetailsViewModel : ViewModel() {
    private var mutableLiveDataFood: MutableLiveData<FoodModel>? = null
    private var mutableLiveDataComment: MutableLiveData<CommentModel>? = null

    init {
        mutableLiveDataComment = MutableLiveData()
    }


    fun getMutableLiveDataFood(): MutableLiveData<FoodModel> {
        if (mutableLiveDataFood == null) {
            mutableLiveDataFood = MutableLiveData()
            mutableLiveDataFood!!.value = com.communisolve.foodversy.common.Common.foodSelected
        }
        return mutableLiveDataFood!!
    }

    fun getMutableLiveDataComment(): MutableLiveData<CommentModel> {
        if (mutableLiveDataComment == null) {
            mutableLiveDataComment = MutableLiveData()
        }
        return mutableLiveDataComment!!
    }

    fun setCommentModel(comment: CommentModel) {
        if (mutableLiveDataComment!=null){
            mutableLiveDataComment!!.value = comment
        }

    }

    fun setFoodModel(foodModel: FoodModel) {
        if (mutableLiveDataFood !=null){
            mutableLiveDataFood!!.value = foodModel
        }
    }

}