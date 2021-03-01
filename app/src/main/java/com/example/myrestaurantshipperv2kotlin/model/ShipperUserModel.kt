package com.example.myrestaurantshipperv2kotlin.model

class ShipperUserModel() {
    var uid: String? = null
    var name: String? = null
    var phone: String? = null
    var isActive: Boolean = false

    constructor(uid: String, name: String, phone: String, isActive: Boolean) : this() {
        this.uid = uid
        this.name = name
        this.phone = phone
        this.isActive = isActive

    }
}