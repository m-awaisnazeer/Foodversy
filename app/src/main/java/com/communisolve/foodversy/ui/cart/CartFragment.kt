package com.communisolve.foodversy.ui.cart

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.view.*
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.communisolve.foodversy.EventBus.CounterCartEvent
import com.communisolve.foodversy.EventBus.HideFabCart
import com.communisolve.foodversy.EventBus.UpdateItemInCart
import com.communisolve.foodversy.R
import com.communisolve.foodversy.adapter.MyCartAdapter
import com.communisolve.foodversy.callbacks.IMyButtonCallback
import com.communisolve.foodversy.callbacks.IOnCartItemMenuClickListner
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.common.MySwipeHelper
import com.communisolve.foodversy.database.CartDataSource
import com.communisolve.foodversy.database.CartDatabase
import com.communisolve.foodversy.database.CartItem
import com.communisolve.foodversy.database.LocalCartDataSource
import com.google.android.gms.location.*
import com.google.android.material.button.MaterialButton
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CartFragment : Fragment(), IOnCartItemMenuClickListner {

    //views
    private lateinit var recycler_cart: RecyclerView
    private lateinit var txt_empty_cart: TextView
    private lateinit var txt_total_price: TextView
    private lateinit var btn_place_order: MaterialButton
    private lateinit var group_place_order: CardView
    private var recyclerViewState: Parcelable? = null
    private lateinit var adapter: MyCartAdapter
    private val cartViewModel: CartViewModel by viewModels()


    private lateinit var locationRequest:LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation:Location


    //Database
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var cartDataSource: CartDataSource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_cart, container, false)
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).CartDao())

        initViews(root)
        initLocation()
        cartViewModel.initCartDatabase(requireContext())
        calculateTotalPrice()
        EventBus.getDefault().postSticky(HideFabCart(true))
        cartViewModel.getMutableLivwDataCartItem().observe(viewLifecycleOwner, Observer {
            if (it == null || it.isEmpty()) {
                recycler_cart.visibility = View.GONE
                group_place_order.visibility = View.GONE
                txt_empty_cart.visibility = View.VISIBLE
            } else {
                recycler_cart.visibility = View.VISIBLE
                group_place_order.visibility = View.VISIBLE
                txt_empty_cart.visibility = View.GONE
                adapter = MyCartAdapter(requireContext(),this, it,)
                recycler_cart.adapter = adapter
            }
        })
        return root
    }

    @SuppressLint("MissingPermission")
    private fun initLocation() {
        buildLocationRequest()
        buildLocationCallback()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
    }

    private fun buildLocationCallback() {
        locationCallback = object :LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                currentLocation = locationResult.lastLocation
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(5000)
        locationRequest.setFastestInterval(3000)
        locationRequest.setSmallestDisplacement(10f)

    }

    @SuppressLint("MissingPermission")
    private fun initViews(root: View?) {
        setHasOptionsMenu(true) //if we not add it, menu will never be inflace
        txt_empty_cart = root!!.findViewById(R.id.txt_empty_cart)
        txt_total_price = root.findViewById(R.id.txt_total_price)
        btn_place_order = root.findViewById(R.id.btn_place_order)
        group_place_order = root.findViewById(R.id.group_place_order)
        recycler_cart = root.findViewById(R.id.recycler_cart)
        recycler_cart.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext())
        recycler_cart.layoutManager = layoutManager
        recycler_cart.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                layoutManager.orientation
            )
        )


        btn_place_order.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("One more step!")

            val view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_place_order,null)
            val edt_address = view.findViewById(R.id.edt_address) as EditText
            val edt_comment = view.findViewById(R.id.edt_comment) as EditText
            //val txt_address = view.findViewById<TextView>(R.id.txt_address_detail)
            val txt_address_detail = view.findViewById<TextView>(R.id.txt_address_detail)
            val rdi_home_address = view.findViewById(R.id.rdi_home_address) as RadioButton
            val rdi_other_address = view.findViewById(R.id.rdi_other_address) as RadioButton
            val rdi_ship_this_address = view.findViewById(R.id.rdi_ship_this_address) as RadioButton
            val rdi_cod = view.findViewById(R.id.rdi_cod) as RadioButton
            val rdi_online = view.findViewById(R.id.rdi_online) as RadioButton

            edt_address.setText("${Common.currentUser!!.address}")

            rdi_home_address.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){
                    edt_address.setText("${Common.currentUser!!.address}")
                    txt_address_detail.visibility = View.GONE

                }
            }

            rdi_other_address.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){
                    edt_address.setText("")
                    //edt_address.setHint("Enter Your Address")
                    txt_address_detail.visibility = View.GONE

                }
            }

            rdi_ship_this_address.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){
                    fusedLocationProviderClient.lastLocation
                        .addOnCompleteListener { task->
                            val cooridate = StringBuilder()
                                .append(task.result!!.latitude)
                                .append("/")
                                .append(task.result.longitude)
                                .toString()

                            edt_address.setText(cooridate)
                            txt_address_detail.visibility = View.VISIBLE
                            txt_address_detail.setText("Implement late with Google API")
                        }.addOnFailureListener { e->
                            txt_address_detail.visibility = View.GONE
                            Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            builder.setView(view)
            builder.setNegativeButton("NO",{dialog,_-> dialog.dismiss()})
            builder.setPositiveButton("YES") { dialog, _ ->
                Toast.makeText(requireContext(), "Implement Late", Toast.LENGTH_SHORT).show()
            }

            val dialog = builder.create()
            dialog.show()

        }
        /*
           val swipe = object :MySwipeHelper(requireContext(),recycler_cart,200)
        {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(MyButton(context!!,
                    "Delete",
                    30.toString(),
                    0,
                    Color.parseColor("#FF3C30"),
                    object : IMyButtonCallback {
                        override fun onClick(pos: Int) {
                            Toast.makeText(context, "Delete Item", Toast.LENGTH_SHORT).show()
                        }

                    }
                ))
            }

        }
         */
    }

    override fun onStop() {
        EventBus.getDefault().postSticky(HideFabCart(false))
        EventBus.getDefault().postSticky(CounterCartEvent(true))
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        cartViewModel.onStop()
        compositeDisposable.clear()
        if (fusedLocationProviderClient !=null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }


    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        EventBus.getDefault().postSticky(HideFabCart(true))
        calculateTotalPrice()
        if (fusedLocationProviderClient !=null)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,
                Looper.getMainLooper())

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateItemInCart(event: UpdateItemInCart) {
        recyclerViewState = recycler_cart.layoutManager!!.onSaveInstanceState()

        cartDataSource.updateCart(event.currentCartItem)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Int> {
                override fun onSubscribe(d: Disposable) {


                }

                override fun onSuccess(t: Int) {
                    calculateTotalPrice()
                    recycler_cart!!.layoutManager!!.onRestoreInstanceState(recyclerViewState)
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "[UPDATE CART] ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

    private fun calculateTotalPrice() {
        cartDataSource.sumPrice(Common.currentUser!!.uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Double> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(price: Double) {
                    txt_total_price.text =
                        StringBuilder("Total: $").append(Common.formatPrice(price))
                }

                override fun onError(e: Throwable) {
                    if (e.message!!.contains("empty"))
                    {

                    }else{
                        Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
                    }                }

            })
    }

    override fun onDeleteselected(position: Int, deletedCartItem: CartItem) {
        cartDataSource.deleteCart(deletedCartItem)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Int> {
                override fun onSubscribe(d: Disposable) {


                }

                override fun onSuccess(t: Int) {
                    Toast.makeText(
                        requireContext(),
                        "${deletedCartItem.foodName} Deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    //adapter.notifyItemRemoved(position)
                    EventBus.getDefault().postSticky(CounterCartEvent(true))
                    calculateTotalPrice()
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
                }

            })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_settings).setVisible(false)
        super.onPrepareOptionsMenu(menu)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.cart_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_clear_cart){

            cartDataSource.cleanCart(Common.currentUser!!.uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(t: Int) {
                        calculateTotalPrice()
                        group_place_order.visibility = View.GONE
                        EventBus.getDefault().postSticky(CounterCartEvent(true))
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
                    }

                })
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}