package com.communisolve.foodversy.common

import com.communisolve.foodversy.model.*
import java.math.RoundingMode
import java.text.DecimalFormat

object Common {
    fun formatPrice(price: Double): String {
        if (price != 0.toDouble()) {
            val df = DecimalFormat("#,##0.00")
            df.roundingMode = RoundingMode.HALF_UP
            val finalPrice = StringBuilder(df.format(price)).toString()
            return finalPrice.replace(".", ",")
        } else return "0,00"
    }

    val COMMENT_REF: String = "Comments"
    lateinit var foodSelected: FoodModel
    lateinit var categorySelected: CategoryModel
    val CATEGORY_REF: String = "Category"
    val FULL_WIDTH_COLUMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int = 0
    val BEST_DEALS_REF: String = "BestDeals"
    val POPULAR_REF: String = "MostPopular"
    val USER_REFERENCE = "Users"
    var currentUser: UserModel? = null

    var userSelectedAddon: List<AddOnModel>? = null
    var userSelectedSize: SizeModel? = null
}