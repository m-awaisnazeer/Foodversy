package com.communisolve.foodversy.callbacks

import android.view.View

interface IRecyclerItemClickLitner {
    fun onItemClick(view: View, pos: Int)
}