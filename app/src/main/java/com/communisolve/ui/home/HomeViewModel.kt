package com.communisolve.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.communisolve.callbacks.IPopularLoadCallback
import com.communisolve.common.Common
import com.communisolve.foodversy.model.PopularCategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel(), IPopularLoadCallback {


    private  var popularListLiveData: MutableLiveData<List<PopularCategoryModel>>?=null
    private  var messageError: MutableLiveData<String>?=null
    private lateinit var iPopularLoadCallback: IPopularLoadCallback

    init {
        iPopularLoadCallback = this
    }

    val popularlist: LiveData<List<PopularCategoryModel>>
        get() {
            if (popularListLiveData == null) {
                popularListLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadPopularList()
            }
            return popularListLiveData!!
        }

    private fun loadPopularList() {
        val templist = ArrayList<PopularCategoryModel>()
        val popularRef = FirebaseDatabase.getInstance().getReference(Common.POPULAR_REF)
        popularRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemsnapshot in snapshot.children) {
                    val model = itemsnapshot.getValue(PopularCategoryModel::class.java)
                    templist.add(model!!)
                }
                iPopularLoadCallback.onPopularLoadSuccess(templist)
            }

            override fun onCancelled(error: DatabaseError) {
                iPopularLoadCallback.onPopularLoadFailed(error.message)
            }

        })
    }

    override fun onPopularLoadSuccess(popularCategoryModels: List<PopularCategoryModel>) {
        popularListLiveData!!.value = popularCategoryModels
    }

    override fun onPopularLoadFailed(message: String) {
        messageError!!.value = message
    }

}