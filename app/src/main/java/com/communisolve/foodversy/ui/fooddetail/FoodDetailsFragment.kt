package com.communisolve.foodversy.ui.fooddetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.communisolve.foodversy.R
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.model.CommentModel
import com.communisolve.foodversy.model.FoodModel
import com.communisolve.foodversy.ui.fooddetail.comment.CommentsFragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import dmax.dialog.SpotsDialog


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

    private var waitingDialog: android.app.AlertDialog? = null

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

                ratingBar.rating = this.ratingValue.toFloat()
            }
        })
        return root
    }

    private fun initView(root: View?) {

        waitingDialog =
            SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()


        collapsing_toolbar = root!!.findViewById(R.id.collapsing_toolbar)
        img_food = root.findViewById(R.id.img_food)
        btnCart = root.findViewById(R.id.btnCart)
        btn_ratting = root.findViewById(R.id.btn_ratting)
        txt_food_name = root.findViewById(R.id.txt_food_name)
        txt_food_price = root.findViewById(R.id.txt_food_price)
        number_button = root.findViewById(R.id.number_button)
        ratingBar = root.findViewById(R.id.rating_bar)
        txt_food_description = root.findViewById(R.id.txt_food_description)
        btnShowComment = root.findViewById(R.id.btnShowComment)

        btn_ratting.setOnClickListener {
            showDialogRating()
        }

        btnShowComment.setOnClickListener {
            val commentFragment = CommentsFragment.getInstance()
            commentFragment.show(requireActivity().supportFragmentManager,"CommentsFragment")
        }

        viewModel.getMutableLiveDataComment().observe(viewLifecycleOwner, Observer {
            submitRatingtoFirebase(it)
        })
    }

    private fun submitRatingtoFirebase(commentModel: CommentModel?) {
        waitingDialog!!.show()

        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
            .child(Common.foodSelected.id)
            .push().setValue(commentModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    addRatingToFood(commentModel!!.ratingValue.toDouble())
                } else {
                    waitingDialog!!.dismiss()
                }
            }.addOnFailureListener {

            }

    }

    private fun addRatingToFood(ratingValue: Double) {


        FirebaseDatabase.getInstance()
            .getReference(Common.CATEGORY_REF)  // select category
            .child(Common.categorySelected.menu_id) // selected menu in category
            .child("foods") // selected foods array
            .child(Common.foodSelected.key) // select key
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val foodModel = snapshot.getValue(FoodModel::class.java)
                        foodModel!!.key = Common.foodSelected.key
                        //Apply Rating

                        val sumRating = foodModel.ratingValue + ratingValue
                        val ratingCount = foodModel.ratingCount + 1
                        val result = sumRating / ratingCount

                        val updateData = HashMap<String, Any>()
                        updateData["ratingValue"] = result
                        updateData["ratingCount"] = ratingCount

                        //update data in variable
                        foodModel.ratingCount = ratingCount
                        foodModel.ratingValue = result

                        snapshot.ref
                            .updateChildren(updateData)
                            .addOnCompleteListener { task ->
                                waitingDialog!!.dismiss()
                                if (task.isSuccessful) {
                                    Common.foodSelected = foodModel
                                    viewModel.setFoodModel(foodModel)


                                    Toast.makeText(
                                        requireContext(),
                                        "Thank You",
                                        Toast.LENGTH_SHORT
                                    ).show()


                                }
                            }.addOnFailureListener {
                                Toast.makeText(
                                    requireContext(),
                                    "${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(requireContext(), "not exist", Toast.LENGTH_SHORT).show()
                        waitingDialog!!.dismiss()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    waitingDialog!!.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "${error.message.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

    private fun showDialogRating() {
        var builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Rating Food")
        builder.setMessage("Please fill information")

        var itemView = LayoutInflater.from(context).inflate(R.layout.layout_rating_comment, null)

        val ratingBar = itemView.findViewById<RatingBar>(R.id.rating_bar)
        val edt_comment = itemView.findViewById<EditText>(R.id.edt_comment)

        builder.setView(itemView)

        builder.setNegativeButton("CANCEL") { dialog, i -> dialog.dismiss() }
        builder.setPositiveButton("OK") { dialog, i ->
            val comment = CommentModel()
            comment.name = Common.currentUser!!.name
            comment.uid = Common.currentUser!!.uid
            comment.comment = edt_comment.text.toString()
            comment.ratingValue = ratingBar.rating

            val serverTimeStamp = HashMap<String, Any>()
            serverTimeStamp["timeStamp"] = ServerValue.TIMESTAMP
            comment.commentTimeStamp = serverTimeStamp

            viewModel.setCommentModel(comment)
        }

        val dialog = builder.create()
        dialog.show()

    }


}