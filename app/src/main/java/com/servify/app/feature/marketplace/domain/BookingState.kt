package com.servify.app.feature.marketplace.domain

/**
 * Represents the comprehensive state of a bidding process.
 * This is the abstraction layer exposed by the Repository to the ViewModel so the ViewModel
 * does not have to coordinate multiple primitive UI flows.
 */
sealed interface BookingState {
    data object Loading : BookingState
    
    data class ActiveBidding(
        val request: RepairRequest, 
        val quotes: List<Quote>,
        val remainingFormatted: String
    ) : BookingState
    
    data class AwaitingPayment(
        val request: RepairRequest, 
        val acceptedQuote: Quote
    ) : BookingState
    
    data class Error(val message: String) : BookingState
    
    data object Expired : BookingState
    
    data class Completed(val request: RepairRequest) : BookingState
}
