package com.example.myrestaurantshipperv2model

import com.example.myrestaurantshipperv2kotlin.model.CartItem

class OrderModel {
    var userId: String? = null
    var userName: String? = null
    var userPhone: String? = null
    var shippingAddress: String? = null
    var comment: String? = null
    var transactionId: String? = null
    var lat = 0.0
    var lng: Double = 0.0
    var totalPayment: Double = 0.0
    var finalPayment: Double = 0.0
    var cod = false
    var discount = 0
    var createDate: Long = 0
    var orderNumber: String? = null
    var orderStatus = 0
    var cartItemList: List<CartItem>? = null
    var key: String? = null
}
