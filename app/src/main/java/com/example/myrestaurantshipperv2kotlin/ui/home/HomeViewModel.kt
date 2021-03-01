package com.example.myrestaurantshipperv2kotlin.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myrestaurantshipperv2kotlin.callback.IShipperOrderCallbackListener
import com.example.myrestaurantshipperv2kotlin.common.Common
import com.example.myrestaurantshipperv2kotlin.model.ShipperOrderModel
import com.example.myrestaurantshipperv2kotlin.model.ShipperUserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeViewModel : ViewModel(), IShipperOrderCallbackListener {
    private var shipperOrderMutableList:MutableLiveData<List<ShipperOrderModel>> = MutableLiveData()
    private var messageError: MutableLiveData<String> = MutableLiveData()
    private val listener: IShipperOrderCallbackListener = this

    fun getMessageError(): MutableLiveData<String>{
        return messageError
    }

    fun getShipperOrderList(shipperPhone: String): MutableLiveData<List<ShipperOrderModel>>{
        loadOrderByShipper(shipperPhone)
        return shipperOrderMutableList
    }

    private fun loadOrderByShipper(shipperPhone: String) {
        val tempList: MutableList<ShipperOrderModel> = ArrayList()
        val orderRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPING_ORDER_REF)
            .orderByChild("shipperPhone")
            .equalTo(shipperPhone)
        orderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {
                    val shipperOrder = item.getValue(ShipperOrderModel::class.java)
                    if (shipperOrder != null) {
                        tempList.add(shipperOrder)
                    }
                }
                listener.onShippingOrderLoadSuccess(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                listener.onShippingOrderLoadFailed(error.message)
            }
        })

    }

    override fun onShippingOrderLoadSuccess(shippingOrderModelList: List<ShipperOrderModel>) {
        shipperOrderMutableList.value = shippingOrderModelList
    }

    override fun onShippingOrderLoadFailed(message: String) {
        messageError.value = message
    }
}