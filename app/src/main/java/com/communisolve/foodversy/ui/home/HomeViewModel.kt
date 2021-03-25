package com.communisolve.foodversy.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.communisolve.foodversy.callbacks.IBestDealLoadCallback
import com.communisolve.foodversy.callbacks.IPopularLoadCallback
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.model.BestDealsModel
import com.communisolve.foodversy.model.PopularCategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel(), IPopularLoadCallback, IBestDealLoadCallback {


    private  var popularListLiveData: MutableLiveData<List<PopularCategoryModel>>?=null
    private  var bestDealsListLiveData: MutableLiveData<List<BestDealsModel>>?=null
    private  var messageError: MutableLiveData<String>?=null
    private  var iPopularLoadCallback: IPopularLoadCallback
    private var iBestDealLoadCallback :IBestDealLoadCallback

    init {
        iPopularLoadCallback = this
        iBestDealLoadCallback = this
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

    val bestDealslist: LiveData<List<BestDealsModel>>
        get() {
            if (bestDealsListLiveData == null) {
                bestDealsListLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadBestDealsList()
            }
            return bestDealsListLiveData!!
        }

    private fun loadBestDealsList() {
        val templist = ArrayList<BestDealsModel>()
        val bestDealsRef = FirebaseDatabase.getInstance().getReference(Common.BEST_DEALS_REF)
        bestDealsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemsnapshot in snapshot.children) {
                    val model = itemsnapshot.getValue(BestDealsModel::class.java)
                    templist.add(model!!)
                }
                iBestDealLoadCallback.onBestDealLoadSuccess(templist)
            }

            override fun onCancelled(error: DatabaseError) {
                iBestDealLoadCallback.onBestDealLoadFailed(error.message)
            }

        })


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

    override fun onBestDealLoadSuccess(bestDealsModels: List<BestDealsModel>) {
bestDealsListLiveData!!.value = bestDealsModels
    }

    override fun onBestDealLoadFailed(message: String) {
        messageError!!.value = message
    }

}