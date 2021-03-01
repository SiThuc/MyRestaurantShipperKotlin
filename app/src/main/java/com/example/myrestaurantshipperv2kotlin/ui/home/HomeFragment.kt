package com.example.myrestaurantshipperv2kotlin.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myrestaurantshipperv2kotlin.R
import com.example.myrestaurantshipperv2kotlin.adapter.MyShipperOrderAdapter
import com.example.myrestaurantshipperv2kotlin.common.Common
import com.example.myrestaurantshipperv2kotlin.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding

    private lateinit var adapter: MyShipperOrderAdapter
    private var layoutAnimationController: LayoutAnimationController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        initViews()
        homeViewModel.getShipperOrderList(Common.currentShipper!!.phone!!).observe(viewLifecycleOwner, Observer {
            adapter = MyShipperOrderAdapter(requireContext(), it)
            binding.recyclerShipperOrder.adapter = adapter
            binding.recyclerShipperOrder.layoutAnimation = layoutAnimationController
        })
        return binding.root
    }

    private fun initViews() {
        binding.recyclerShipperOrder.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerShipperOrder.layoutManager = layoutManager
        binding.recyclerShipperOrder.addItemDecoration(DividerItemDecoration(requireContext(), layoutManager.orientation))

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_slide_from_left)

    }
}