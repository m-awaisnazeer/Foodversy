package com.communisolve.foodversy.common

import com.communisolve.foodversy.model.CategoryModel
import com.communisolve.foodversy.model.FoodModel
import com.communisolve.foodversy.model.UserModel

object Common {
    lateinit var foodSelected: FoodModel
    lateinit var categorySelected: CategoryModel
    val CATEGORY_REF: String = "Category"
    val FULL_WIDTH_COLUMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int = 0
    val BEST_DEALS_REF: String = "BestDeals"
    val POPULAR_REF: String = "MostPopular"
    val USER_REFERENCE = "Users"
    var currentUser: UserModel? = null
}