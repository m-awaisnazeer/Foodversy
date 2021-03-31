package com.communisolve.foodversy.common

import com.communisolve.foodversy.model.*
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.random.Random

object Common {
    fun formatPrice(price: Double): String {
        if (price != 0.toDouble()) {
            val df = DecimalFormat("#,##0.00")
            df.roundingMode = RoundingMode.HALF_UP
            val finalPrice = StringBuilder(df.format(price)).toString()
            return finalPrice.replace(".", ",")
        } else return "0,00"
    }

    fun calculateExtraPrice(
        userSelectedSize: SizeModel?,
        userSelectedAddon: MutableList<AddOnModel>?
    ): Double {
        var result: Double = 0.0
        if (userSelectedSize == null && userSelectedAddon == null) {
            return 0.0
        } else if (userSelectedSize == null) {
            for (addonModel in userSelectedAddon!!) {
                result += addonModel.price.toDouble()
            }
            return result
        } else if (userSelectedAddon == null) {
            result = userSelectedSize.price.toDouble()
            return result
        } else {
            result = userSelectedSize.price.toDouble()
            for (addonModel in userSelectedAddon!!)
                result += addonModel.price.toDouble()
            return result
        }
    }

    fun createOrderNumber(): String {
        return StringBuilder()
            .append(System.currentTimeMillis())
            .append(Math.abs(Random.nextInt()))
            .toString()
    }

    fun getDateOfWeek(i: Int): String {
        when(i){
            1-> return "Monday"
            2-> return "Tuesday"
            3-> return "Wednesday"
            4-> return "Thursday"
            5-> return "Friday"
            6-> return "Saturday"
            7-> return "Sunday"
            else -> return "Unk"
        }
    }

    fun convertStatusToText(orderStatus: Int): String {
        when(orderStatus){
            0-> return "Placed"
            1-> return "Shipping"
            2-> return "Shipped"
            -1 -> return "Cancelled"
            else -> return "Unk"
        }
    }

    val ORDER_REF: String="Orders"
    val COMMENT_REF: String = "Comments"
     var foodSelected: FoodModel?=null
     var categorySelected: CategoryModel?=null
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