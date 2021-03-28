package com.communisolve.foodversy.ui.foodlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.communisolve.foodversy.R
import com.communisolve.foodversy.adapter.MyFoodListAdapter
import com.communisolve.foodversy.common.Common

class FoodListFragment : Fragment() {

    lateinit var recycler_food_list: RecyclerView
    lateinit var adapter: MyFoodListAdapter
    private val viewModel: FoodListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var root = inflater.inflate(R.layout.food_list_fragment, container, false)
        recycler_food_list = root.findViewById(R.id.recycler_food_list)
        recycler_food_list.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            layoutAnimation =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)
        }

        (activity as AppCompatActivity).supportActionBar!!.title = Common.categorySelected.name

        viewModel.getMutavleFoodliveData().observe(viewLifecycleOwner, Observer {
            adapter = MyFoodListAdapter(requireContext(), it)
            recycler_food_list.adapter = adapter
        })

        return root
    }

    override fun onStop() {
        if (adapter !=null)
            adapter.onStop()
        super.onStop()
    }

}