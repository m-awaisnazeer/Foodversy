package com.communisolve.foodversy.model

class Transaction(
    var id:String="",
    var status:String="",
    var type:String="",
    var currencyIsoCode:String="",
    var amount:String="",
    var merchantAccountId:String="",
    var subMerchantAccountId:String="",
    var masterMerchantAccountId:String="",
    var orderId:String="",
    var createAt:String="",
    var updateAt:String=""
) {
}