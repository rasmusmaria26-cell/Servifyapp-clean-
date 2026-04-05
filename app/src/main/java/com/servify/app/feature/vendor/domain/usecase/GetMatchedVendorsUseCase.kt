package com.servify.app.feature.vendor.domain.usecase

import com.servify.app.feature.vendor.domain.Vendor
import com.servify.app.feature.vendor.data.VendorRepository
import javax.inject.Inject

class GetMatchedVendorsUseCase @Inject constructor(
    private val vendorRepository: VendorRepository
) {
    suspend operator fun invoke(
        serviceCategory: String
    ): Result<List<Vendor>> {
        return vendorRepository.getVendors(serviceCategory)
            .map { vendors ->
                vendors.sortedByDescending { it.rating }
            }
    }
}
