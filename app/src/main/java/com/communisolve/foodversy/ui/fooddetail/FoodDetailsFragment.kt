package com.communisolve.foodversy.ui.fooddetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.communisolve.foodversy.R
import com.communisolve.foodversy.common.Common
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FoodDetailsFragment : Fragment() {

    //views
    private lateinit var collapsing_toolbar: CollapsingToolbarLayout
    private lateinit var img_food: ImageView
    private lateinit var btnCart: CounterFab
    private lateinit var btn_ratting: FloatingActionButton
    private lateinit var txt_food_name: TextView
    private lateinit var txt_food_price: TextView
    private lateinit var number_button: ElegantNumberButton
    private lateinit var ratingBar: RatingBar
    private lateinit var txt_food_description: TextView
    private lateinit var btnShowComment: MaterialButton


    private val viewModel: FoodDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var root = inflater.inflate(R.layout.food_details_fragment, container, false)
        (activity as AppCompatActivity).supportActionBar!!.title = Common.foodSelected.name
        initView(root)

        viewModel.getMutableLiveDataFood().observe(viewLifecycleOwner, Observer {
            it.apply {
                Glide.with(requireContext()).load(this.image).into(img_food)
                txt_food_description.setText(this.description)
                txt_food_price.setText(this.price.toString())
                txt_food_name.setText(this.name)

            }
        })
        return root
    }

    private fun initView(root: View?) {
        collapsing_toolbar = root!!.findViewById(R.id.collapsing_toolbar)
        img_food = root.findViewById(R.id.img_food)
        btnCart = root.findViewById(R.id.btnCart)
        btn_ratting = root.findViewById(R.id.btn_ratting)
        txt_food_name = root.findViewById(R.id.txt_food_name)
        txt_food_price = root.findViewById(R.id.txt_food_price)
        number_button = root.findViewById(R.id.number_button)
        ratingBar = root.findViewById(R.id.ratingBar)
        txt_food_description = root.findViewById(R.id.txt_food_description)
        btnShowComment = root.findViewById(R.id.btnShowComment)

    }


}