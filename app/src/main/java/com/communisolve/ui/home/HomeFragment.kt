package com.communisolve.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.communisolve.adapter.MyBestDealsAdapter
import com.communisolve.adapter.MyPopularCategoriesAdapter
import com.communisolve.foodversy.R
import com.communisolve.foodversy.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val homeViewModel: HomeViewModel by viewModels()
    private var binding: FragmentHomeBinding? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        if (context != null) {
            homeViewModel.popularlist.observe(viewLifecycleOwner, Observer {
                binding!!.recyclerPopular.apply {
                    setHasFixedSize(true)
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = MyPopularCategoriesAdapter(requireContext(), it)
                }
            })

            homeViewModel.bestDealslist.observe(viewLifecycleOwner, Observer {
                binding!!.viewpager.apply {
                    adapter = MyBestDealsAdapter(requireContext(), it, false)
                }
            })
        }
    }


    override fun onResume() {
        super.onResume()
        binding!!.viewpager.resumeAutoScroll()
    }

    override fun onPause() {
        binding!!.viewpager.pauseAutoScroll()
        super.onPause()
    }
}