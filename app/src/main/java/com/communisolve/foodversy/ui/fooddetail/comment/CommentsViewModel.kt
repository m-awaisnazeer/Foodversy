package com.communisolve.foodversy.ui.fooddetail.comment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.communisolve.foodversy.model.CommentModel

class CommentsViewModel : ViewModel() {

     var mutableLiveDataCommentList:MutableLiveData<List<CommentModel>>

    init {
        mutableLiveDataCommentList = MutableLiveData()
    }


    fun setCommentList(commentlist:List<CommentModel>){
        mutableLiveDataCommentList.value = commentlist
    }
}