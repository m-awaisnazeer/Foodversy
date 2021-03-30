package com.communisolve.foodversy

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.andremion.counterfab.CounterFab
import com.communisolve.foodversy.EventBus.*
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.database.CartDataSource
import com.communisolve.foodversy.database.CartDatabase
import com.communisolve.foodversy.database.LocalCartDataSource
import com.communisolve.foodversy.model.CategoryModel
import com.communisolve.foodversy.model.FoodModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var cartDataSource: CartDataSource
    private lateinit var fab: CounterFab
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        dialog = SpotsDialog.Builder().setCancelable(false).setContext(this).build()

        fab = findViewById(R.id.counter_fab)
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(this).CartDao())
        counterCartItem()
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_menu, R.id.nav_cart
            ), drawerLayout
        )

        fab.setOnClickListener { view ->
            navController.navigate(R.id.nav_cart)
        }

        val headerView = navView.getHeaderView(0)
        var txt_user = headerView.findViewById<TextView>(R.id.txt_user)
        txt_user.setText("Welcome, ${Common.currentUser!!.name}")
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        counterCartItem()

        navView.menu.findItem(R.id.nav_signout).setOnMenuItemClickListener {
            FirebaseAuth.getInstance().signOut()
            Common.foodSelected = null
            Common.categorySelected = null
            Common.currentUser = null
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return@setOnMenuItemClickListener true
        }
    }

    override fun onResume() {
        counterCartItem()
        super.onResume()
        counterCartItem()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCategorySelected(event: CategoryClick) {
        if (event.isClicked) {

            findNavController(R.id.nav_host_fragment).navigate(R.id.nav_foodListFragment)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onFoodSelected(event: FoodItemClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment).navigate(R.id.nav_foodDetailsFragment)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCounterCartEvent(event: CounterCartEvent) {
        if (event.isSuccess) {
            counterCartItem()
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCartFabHide(event: HideFabCart) {
        if (event.isHide) {
            fab.hide()
        } else {
            fab.show()
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onPopularFoodItemClick(event: PopularFoodItemClick) {
        if (event.popularCategoryModel != null) {
            dialog!!.show()
            FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF).child(event.popularCategoryModel.menu_id)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            Common.categorySelected = snapshot.getValue(CategoryModel::class.java)
                            //load food
                            FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
                                .child(event.popularCategoryModel.menu_id)
                                .child("foods")
                                .orderByChild("id")
                                .equalTo(event.popularCategoryModel.food_id)
                                .limitToLast(1)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            for (foodSnapshot in snapshot.children){
                                                Common.foodSelected = foodSnapshot.getValue(FoodModel::class.java)
                                            }
                                            navController.navigate(R.id.nav_foodDetailsFragment)
                                            dialog!!.dismiss()
                                        } else {
                                            dialog!!.dismiss()
                                            Toast.makeText(
                                                this@HomeActivity,
                                                "Item doesn't exists",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        dialog!!.dismiss()
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "${error.message}",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }

                                })
                        } else {
                            dialog!!.dismiss()
                            Toast.makeText(
                                this@HomeActivity,
                                "Item doesn't exists",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        dialog!!.dismiss()
                        Toast.makeText(this@HomeActivity, "${error.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                })
        } else {

        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onBestDealsItemClick(event: BestDealsItemClick) {
        if (event.bestDealsModel != null) {
            dialog!!.show()
            FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF).child(event.bestDealsModel!!.menu_id)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            dialog!!.dismiss()
                            Common.categorySelected = snapshot.getValue(CategoryModel::class.java)
                            //load food
                            FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
                                .child(event.bestDealsModel!!.menu_id)
                                .child("foods")
                                .orderByChild("id")
                                .equalTo(event.bestDealsModel!!.food_id)
                                .limitToLast(1)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            for (foodSnapshot in snapshot.children){
                                                Common.foodSelected = foodSnapshot.getValue(FoodModel::class.java)
                                            }
                                            navController.navigate(R.id.nav_foodDetailsFragment)
                                            dialog!!.dismiss()

                                        } else {
                                            dialog!!.dismiss()
                                            Toast.makeText(
                                                this@HomeActivity,
                                                "Item doesn't exists",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        dialog!!.dismiss()
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "${error.message}",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }

                                })
                        } else {
                            dialog!!.dismiss()
                            Toast.makeText(
                                this@HomeActivity,
                                "Item doesn't exists",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        dialog!!.dismiss()
                        Toast.makeText(this@HomeActivity, "${error.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                })
        } else {

        }
    }


    private fun counterCartItem() {
        cartDataSource.countItemInCart(Common.currentUser!!.uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Int> {
                override fun onSubscribe(d: Disposable) {


                }

                override fun onSuccess(t: Int) {
                    fab.count = t
                }

                override fun onError(e: Throwable) {
                    if (e.message!!.contains("empty")) {
                        // Toast.makeText(this@HomeActivity, "Empty Cart", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@HomeActivity, "${e.message}", Toast.LENGTH_SHORT).show()

                    }
                }

            })
    }
}