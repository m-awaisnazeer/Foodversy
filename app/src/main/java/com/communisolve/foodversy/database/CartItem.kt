package com.communisolve.foodversy.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cart")
class CartItem(
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "foodId")
    var foodId:String="",

    @ColumnInfo(name = "foodName")
    var foodName:String="",

    @ColumnInfo(name = "foodImage")
    var foodImage:String="",

    @ColumnInfo(name = "foodPrice")
    var foodPrice:Double=0.0,

    @ColumnInfo(name = "foodQuantity")
    var foodQuantity:Int=0,

    @ColumnInfo(name = "foodAddon")
    var foodAddon:String="",

    @ColumnInfo(name = "foodSize")
    var foodSize:String="",

    @ColumnInfo(name = "userPhone")
    var userPhone:String="",

    @ColumnInfo(name = "foodExtraPrice")
    var foodExtraPrice:Double=0.0,

    @ColumnInfo(name = "uid")
    var uid:String="",
) {
}