package com.communisolve.foodversy.ui.fooddetail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.communisolve.foodversy.EventBus.CounterCartEvent
import com.communisolve.foodversy.EventBus.MenuItemBack
import com.communisolve.foodversy.R
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.database.CartDataSource
import com.communisolve.foodversy.database.CartDatabase
import com.communisolve.foodversy.database.CartItem
import com.communisolve.foodversy.database.LocalCartDataSource
import com.communisolve.foodversy.model.CommentModel
import com.communisolve.foodversy.model.FoodModel
import com.communisolve.foodversy.ui.fooddetail.comment.CommentsFragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus


class FoodDetailsFragment : Fragment(R.layout.food_details_fragment), TextWatcher {

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

    //Size Layout
    private lateinit var rdi_group_size: RadioGroup
    private lateinit var img_add_addon: ImageView
    private lateinit var chip_group_user_selected_addon: ChipGroup

    //AddOn Layout
    private var chip_group_addon: ChipGroup? = null
    private var edt_search_addOn: EditText? = null

    //Dialog
    private lateinit var addOnBottomSheetDialog: BottomSheetDialog
    private var waitingDialog: android.app.AlertDialog? = null

    private val viewModel: FoodDetailsViewModel by viewModels()

    //Database
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var cartDataSource: CartDataSource


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var root = inflater.inflate(R.layout.food_details_fragment, container, false)
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).CartDao())

        (activity as AppCompatActivity).supportActionBar!!.title = Common.foodSelected!!.name
        initView(root)

        viewModel.getMutableLiveDataFood().observe(viewLifecycleOwner, Observer {
            it.apply {
                Glide.with(requireContext()).load(this.image).into(img_food)
                txt_food_description.setText(this.description)
                txt_food_price.setText(this.price.toString())
                txt_food_name.setText(this.name)
                ratingBar.rating = this.ratingValue.toFloat()/it.ratingCount

                for (sizeModel in it.size) {
                    val radioButton = RadioButton(context)
                    radioButton.setOnCheckedChangeListener { buttonView, isChecked ->
                        if (isChecked)
                            Common.foodSelected!!.userSelectedSize = sizeModel
                        calculateTotalPrice()
                    }

                    val params = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f
                    )
                    radioButton.layoutParams = params
                    radioButton.text = sizeModel.name
                    radioButton.tag = sizeModel.price

                    rdi_group_size.addView(radioButton)

                    //Default first radio button select
                    if (rdi_group_size.childCount > 0) {
                        val radioButton = rdi_group_size.getChildAt(0) as RadioButton
                        radioButton.isChecked = true
                    }
                }
            }
        })

        number_button.setOnValueChangeListener { view, oldValue, newValue ->
            if (oldValue != newValue) {
                calculateTotalPrice()
            }
        }
        return root
    }

    private fun calculateTotalPrice() {
        var totalPrice = Common.foodSelected!!.price.toDouble()
        var displayPrice = 0.0

        //Addon

        if (Common.foodSelected!!.userSelectedAddon != null && Common.foodSelected!!.userSelectedAddon!!.size > 0) {
            for (addOnModel in Common.foodSelected!!.userSelectedAddon!!)
                totalPrice += addOnModel.price.toDouble()
        }

        //size
        totalPrice += Common.foodSelected!!.userSelectedSize!!.price.toDouble()

        displayPrice = totalPrice * number_button.number.toInt()
        displayPrice = Math.round(displayPrice * 100.0) / 100.0

        txt_food_price.text = StringBuilder("").append(Common.formatPrice(displayPrice))
    }

    private fun initView(root: View?) {

        addOnBottomSheetDialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        val layout_user_selected_addon = layoutInflater.inflate(R.layout.layout_addon_display, null)
        chip_group_addon = layout_user_selected_addon.findViewById(R.id.chip_group_addon)
        edt_search_addOn = layout_user_selected_addon.findViewById(R.id.edt_search_addOn)

        addOnBottomSheetDialog.setContentView(layout_user_selected_addon)

        addOnBottomSheetDialog.setOnDismissListener { dialogInterface ->
            displayUserSelectedAddon()
            calculateTotalPrice()
        }

        waitingDialog =
            SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()
        rdi_group_size = root!!.findViewById(R.id.rdi_group_size)
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
        img_add_addon = root.findViewById(R.id.img_add_addon)
        chip_group_user_selected_addon = root.findViewById(R.id.chip_group_user_selected_addon)

        //Event
        img_add_addon.setOnClickListener {
            if (Common.foodSelected!!.addon != null) {
                displayAllAddOn()
                addOnBottomSheetDialog.show()
            }
        }
        btn_ratting.setOnClickListener {
            showDialogRating()
        }

        btnShowComment.setOnClickListener {
            val commentFragment = CommentsFragment.getInstance()
            commentFragment.show(requireActivity().supportFragmentManager, "CommentsFragment")
        }

        viewModel.getMutableLiveDataComment().observe(viewLifecycleOwner, Observer {
            submitRatingtoFirebase(it)
        })

        btnCart.setOnClickListener {
            val cartItem: CartItem = CartItem()

            cartItem.apply {
                this.foodId = Common.foodSelected!!.id
                this.foodName = Common.foodSelected!!.name
                this.foodImage = Common.foodSelected!!.image
                this.foodPrice = Common.foodSelected!!.price.toDouble()
                this.foodQuantity = number_button.number.toInt()

                if (Common.foodSelected!!.userSelectedAddon != null) {
                    this.foodAddon = Gson().toJson(
                        Common.foodSelected!!.userSelectedAddon
                    )
                } else
                    this.foodAddon = "Default"

                if (Common.foodSelected!!.userSelectedSize != null) {
                    this.foodSize = Gson().toJson(
                        Common.foodSelected!!.userSelectedSize
                    )
                } else
                    this.foodSize = "Default"

                this.userPhone = Common.currentUser!!.phone
                this.foodExtraPrice = Common.calculateExtraPrice(
                    Common.foodSelected!!.userSelectedSize,
                    Common.foodSelected!!.userSelectedAddon
                )
                this.uid = Common.currentUser!!.uid
            }

            cartDataSource.getItemWithAllOptionsInCart(
                Common.currentUser!!.uid,
                cartItem.foodId,
                cartItem.foodSize,
                cartItem.foodAddon
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<CartItem> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(cartItemFromDB: CartItem) {
                        if (cartItemFromDB.equals(cartItem)) {

                            //if item already in database, just update

                            cartItemFromDB.foodExtraPrice = cartItem.foodExtraPrice
                            cartItemFromDB.foodAddon = cartItem.foodAddon
                            cartItemFromDB.foodSize = cartItem.foodSize
                            cartItemFromDB.foodQuantity = cartItem.foodQuantity

                            cartDataSource.updateCart(cartItemFromDB)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : SingleObserver<Int> {
                                    override fun onSubscribe(d: Disposable) {


                                    }

                                    override fun onSuccess(t: Int) {
                                        Toast.makeText(
                                            context,
                                            "Update Cart Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        EventBus.getDefault().postSticky(CounterCartEvent(true))
                                    }

                                    override fun onError(e: Throwable) {
                                        Toast.makeText(
                                            context,
                                            "[Update Cart Error]: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                })
                        } else {
                            //if item not aviable in database , just insert
                            compositeDisposable.add(
                                cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Toast.makeText(
                                            context,
                                            "Add to Cart Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        EventBus.getDefault().postSticky(CounterCartEvent(true))
                                    }, {
                                        Toast.makeText(
                                            context,
                                            "Error ${it.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    })
                            )
                        }
                    }

                    override fun onError(e: Throwable) {
                        if (e.message!!.contains("empty")) {
                            compositeDisposable.add(
                                cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Toast.makeText(
                                            context,
                                            "Add to Cart Success",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        EventBus.getDefault().postSticky(CounterCartEvent(true))
                                    }, {
                                        Toast.makeText(
                                            context,
                                            "Error ${it.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    })
                            )
                        } else
                            Toast.makeText(context, "Error ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }

    private fun displayAllAddOn() {
        if (Common.foodSelected!!.addon!!.size > 0) {
            chip_group_addon!!.clearCheck()
            chip_group_addon!!.removeAllViews()

            edt_search_addOn!!.addTextChangedListener(this)

            for (addOnModel in Common.foodSelected!!.addon!!) {
                val chip = layoutInflater.inflate(R.layout.layout_chip, null, false) as Chip
                chip.text = StringBuilder(addOnModel.name).append("(+$").append(addOnModel.price)
                    .append(")").toString()
                chip.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        if (Common.foodSelected!!.userSelectedAddon == null)
                            Common.foodSelected!!.userSelectedAddon = ArrayList()
                        Common.foodSelected!!.userSelectedAddon!!.add(addOnModel)
                    }
                }
                chip_group_addon!!.addView(chip)

            }

        }
    }

    private fun displayUserSelectedAddon() {
        if (Common.foodSelected!!.userSelectedAddon != null && Common.foodSelected!!.userSelectedAddon!!.size > 0) {
            chip_group_user_selected_addon.removeAllViews()

            for (addonModel in Common.foodSelected!!.userSelectedAddon!!) {
                val chip =
                    layoutInflater.inflate(R.layout.layout_chip_with_delete, null, false) as Chip
                chip.text = StringBuilder(addonModel.name).append("(+$").append(addonModel.price)
                    .append(")").toString()
                chip.isClickable = false
                chip.setOnCloseIconClickListener { view ->
                    chip_group_user_selected_addon.removeView(view)
                    Common.foodSelected!!.userSelectedAddon!!.remove(addonModel)
                    calculateTotalPrice()
                }
                chip_group_user_selected_addon.addView(chip)
            }
        } else  {
            chip_group_user_selected_addon.removeAllViews()
        }
    }

    private fun submitRatingtoFirebase(commentModel: CommentModel?) {
        waitingDialog!!.show()

        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
            .child(Common.foodSelected!!.id)
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
            .child(Common.categorySelected!!.menu_id) // selected menu in category
            .child("foods") // selected foods array
            .child(Common.foodSelected!!.key) // select key
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val foodModel = snapshot.getValue(FoodModel::class.java)
                        foodModel!!.key = Common.foodSelected!!.key
                        //Apply Rating

                        val sumRating = foodModel.ratingValue + ratingValue
                        val ratingCount = foodModel.ratingCount + 1

                        val updateData = HashMap<String, Any>()
                        updateData["ratingValue"] = sumRating
                        updateData["ratingCount"] = ratingCount

                        //update data in variable
                        foodModel.ratingCount = ratingCount
                        foodModel.ratingValue = sumRating

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

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {


    }

    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
        chip_group_addon!!.clearCheck()
        chip_group_addon!!.removeAllViews()

        for (addOnModel in Common.foodSelected!!.addon!!) {
            if (addOnModel!!.name.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                val chip = layoutInflater.inflate(R.layout.layout_chip, null, false) as Chip
                chip.text = StringBuilder(addOnModel.name).append("(+$").append(addOnModel.price)
                    .append(")").toString()
                chip.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        if (Common.foodSelected!!.userSelectedAddon == null)
                            Common.foodSelected!!.userSelectedAddon = ArrayList()
                        Common.foodSelected!!.userSelectedAddon!!.add(addOnModel)
                    }
                }
                chip_group_addon!!.addView(chip)
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
    }


    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        super.onDestroy()
    }
}