package com.example.myrestaurantshipperv2kotlin.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myrestaurantshipperv2kotlin.ShippingActivity
import com.example.myrestaurantshipperv2kotlin.common.Common
import com.example.myrestaurantshipperv2kotlin.databinding.LayoutShipperOrderBinding
import com.example.myrestaurantshipperv2kotlin.model.ShipperOrderModel
import com.example.myrestaurantshipperv2kotlin.model.ShipperUserModel
import com.google.gson.Gson
import io.paperdb.Paper
import java.lang.StringBuilder
import java.text.SimpleDateFormat

class MyShipperOrderAdapter(
        var context: Context,
        var shipperOrderList: List<ShipperOrderModel>
): RecyclerView.Adapter<MyShipperOrderAdapter.MyViewHolder>() {
    private val simpleDateFormat: SimpleDateFormat

    init {
        Paper.init(context)
        simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    }
    lateinit var binding: LayoutShipperOrderBinding

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        binding = LayoutShipperOrderBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        with(holder){
            val shipperOrder = shipperOrderList[position]
            Glide.with(context).load(shipperOrder.orderModel!!.cartItemList!![0].foodImage).into(binding.imgFood)
            binding.txtDate.text = StringBuilder(simpleDateFormat.format(shipperOrder.orderModel.createDate))
            Common.setSpanStringColor("Order No.:", shipperOrder.orderModel.key!!, binding.txtOrderNumber, Color.parseColor("#BA454A"))
            Common.setSpanStringColor("Address:", shipperOrder.orderModel.shippingAddress!!, binding.txtOrderAddress, Color.parseColor("#BA454A"))
            Common.setSpanStringColor("Payment:", shipperOrder.orderModel.transactionId!!, binding.txtOrderPayment, Color.parseColor("#BA454A"))


            //Disable button if already start trip
            //Event
            binding.btnShipNow.setOnClickListener {
                //Write Data
                Paper.book().write(Common.SHIPPING_DATA, Gson().toJson(shipperOrder))

                //Start activity
                context.startActivity(Intent(context, ShippingActivity::class.java))
            }

        }
    }

    override fun getItemCount(): Int = shipperOrderList.size
}