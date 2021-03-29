package com.communisolve.foodversy.ui.cart

import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.*
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
        super.onStop()
    }


    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().postSticky(HideFabCart(true))
        calculateTotalPrice()

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
                        StringBuilder("Total: ").append(Common.formatPrice(price))
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