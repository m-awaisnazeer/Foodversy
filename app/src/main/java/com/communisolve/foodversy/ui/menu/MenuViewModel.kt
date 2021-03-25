package com.communisolve.foodversy.ui.menu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.communisolve.foodversy.callbacks.ICategoryCallBackListner
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.model.CategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MenuViewModel : ViewModel(), ICategoryCallBackListner {

    private var categoryListMutableLiveData: MutableLiveData<List<CategoryModel>>? = null
    private var messageError: MutableLiveData<String>? = null
    private val iCategoryCallBackListner: ICategoryCallBackListner


    init {
        iCategoryCallBackListner = this
    }

    override fun onCategoryLoadSuccess(categoryModels: List<CategoryModel>) {
        categoryListMutableLiveData!!.value = categoryModels
    }

    override fun onCategoryLoadFailed(message: String) {
        messageError!!.value = message
    }

    fun getError(): MutableLiveData<String> = messageError!!


    fun getCategoryList(): MutableLiveData<List<CategoryModel>> {
        if (categoryListMutableLiveData == null) {
            categoryListMutableLiveData = MutableLiveData()
            messageError = MutableLiveData()
            loadCategory()

        }
        return categoryListMutableLiveData!!
    }

    private fun loadCategory() {
        val templist = ArrayList<CategoryModel>()
        val categoryRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemsnapshot in snapshot.children) {
                    val model = itemsnapshot.getValue(CategoryModel::class.java)
                    templist.add(model!!)
                }
                iCategoryCallBackListner.onCategoryLoadSuccess(templist)
            }

            override fun onCancelled(error: DatabaseError) {
                iCategoryCallBackListner.onCategoryLoadFailed(error.message)
            }

        })

    }
}