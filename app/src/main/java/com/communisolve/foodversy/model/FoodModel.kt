package com.communisolve.foodversy.model

class FoodModel(
    var name: String="",
    var image:String="",
    var id:String="",
    var description:String="",
    var price:Int=0,
    var addon:List<AddOnModel>?=ArrayList<AddOnModel>(),
    var size:List<SizeModel> = ArrayList<SizeModel>()
) {

}
