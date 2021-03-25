package com.communisolve.foodversy.ui.menu

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.communisolve.foodversy.R
import com.communisolve.foodversy.adapter.MyCategoriesAdapter
import com.communisolve.foodversy.common.SpacesItemDecoration
import dmax.dialog.SpotsDialog

class MenuFragment : Fragment() {

    private lateinit var recycler_menu: RecyclerView
    private val menuViewModel: MenuViewModel by viewModels()
    private lateinit var dialog: AlertDialog
    private var mAdapter: MyCategoriesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_categories, container, false)
        recycler_menu = root.findViewById(R.id.recycler_menu)
        initViews()
        menuViewModel.getCategoryList().observe(viewLifecycleOwner, Observer {
            dialog.dismiss()
            mAdapter = MyCategoriesAdapter(requireContext(), it)
            recycler_menu.adapter = mAdapter
            recycler_menu.layoutAnimation =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)

        })
        menuViewModel.getError().observe(viewLifecycleOwner, Observer {
            dialog.dismiss()
            Toast.makeText(context, "${it}", Toast.LENGTH_SHORT).show()
        })
        return root
    }

    private fun initViews() {
        dialog = SpotsDialog.Builder().setContext(context)
            .setCancelable(false).build()
        dialog.show()
        val mlayoutManager = GridLayoutManager(context, 2)
        mlayoutManager.orientation = RecyclerView.VERTICAL
        mlayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (mAdapter != null) {
                    when (mAdapter!!.getItemViewType(position)) {
                        com.communisolve.foodversy.common.Common.DEFAULT_COLUMN_COUNT -> 1
                        com.communisolve.foodversy.common.Common.FULL_WIDTH_COLUMN -> 2
                        else -> -1
                    }
                } else {
                    -1
                }
            }

        }
        recycler_menu.apply {
            setHasFixedSize(true)
            layoutManager = mlayoutManager
            addItemDecoration(SpacesItemDecoration(8))
        }
    }

}