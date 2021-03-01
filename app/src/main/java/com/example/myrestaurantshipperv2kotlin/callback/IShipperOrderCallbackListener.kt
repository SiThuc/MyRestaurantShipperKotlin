package com.example.myrestaurantshipperv2kotlin.callback

import com.example.myrestaurantshipperv2kotlin.model.ShipperOrderModel

interface IShipperOrderCallbackListener {
    fun onShippingOrderLoadSuccess(shippingOrderModelList: List<ShipperOrderModel>)
    fun onShippingOrderLoadFailed(message: String)
}