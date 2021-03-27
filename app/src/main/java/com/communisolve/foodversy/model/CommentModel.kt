package com.communisolve.foodversy.model

class CommentModel(
    var ratingValue: Float = 0.toFloat(),
    var comment: String? = "",
    var name: String? = "",
    var uid: String? = null,
    var commentTimeStamp: Map<String, Any>? = null
) {

}
