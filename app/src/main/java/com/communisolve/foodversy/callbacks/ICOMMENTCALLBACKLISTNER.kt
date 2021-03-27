package com.communisolve.foodversy.callbacks

import com.communisolve.foodversy.model.CommentModel

interface ICommentCallbackListner {
    fun onCommentLoadSuccess(commentsList:List<CommentModel>)
    fun onCommentLoadFailed(message:String)
}
