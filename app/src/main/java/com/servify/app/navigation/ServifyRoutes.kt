package com.servify.app.navigation

import android.net.Uri

/**
 * Single source of truth for all navigation route strings.
 * Never use raw string literals for navigation routes elsewhere in the codebase.
 */
object ServifyRoutes {

    // Auth flow
    const val SPLASH         = "splash"
    const val LOGIN          = "login"
    const val SIGNUP         = "signup"

    // Role-selection landing (after login)
    const val HOME           = "home/{role}"
    fun home(role: String) = "home/$role"

    // Customer screens
    const val CUSTOMER_GRAPH      = "customer_graph"
    const val CUSTOMER_HOME       = "customer_home"
    const val CUSTOMER_ORDERS     = "customer_orders"
    const val CUSTOMER_REPAIRS    = "customer_repairs"
    const val CUSTOMER_PROFILE    = "customer_profile"
    
    const val CREATE_BOOKING      = "create_booking?category={category}"
    fun createBooking(category: String? = null) = 
        if (category != null) "create_booking?category=$category" else "create_booking"
    const val BOOKING_DETAIL      = "booking_detail/{bookingId}"
    const val POST_REPAIR_REQUEST = "post_repair_request?category={category}"
    fun postRepairRequest(category: String? = null) = 
        if (category != null) "post_repair_request?category=$category" else "post_repair_request"
    const val QUOTES              = "quotes/{requestId}"
    const val ACTIVE_REPAIR       = "active_repair/{requestId}"
    fun bookingDetail(bookingId: String)      = "booking_detail/$bookingId"
    fun quotes(requestId: String)             = "quotes/$requestId"
    fun activeRepair(requestId: String)       = "active_repair/$requestId"

    // Vendor screens
    const val REPAIR_FEED   = "repair_feed"
    const val SUBMIT_QUOTE  = "submit_quote"
    const val VENDOR_BOOKING_DETAIL = "vendor_booking_detail/{bookingId}"
    fun vendorBookingDetail(bookingId: String) = "vendor_booking_detail/$bookingId"
    
    const val LOCATION_MAP    = "location_map/{title}/{lat}/{lng}"
    fun locationMap(title: String, lat: Double, lng: Double) = "location_map/$title/$lat/$lng"

    // Payment screen
    const val PAYMENT = "payment/{amount}/{description}/{vendorName}/{bookingId}"
    fun payment(amount: Long, description: String, vendorName: String, bookingId: String = "") =
        "payment/$amount/${Uri.encode(description)}/${Uri.encode(vendorName)}/${Uri.encode(bookingId)}"
}
