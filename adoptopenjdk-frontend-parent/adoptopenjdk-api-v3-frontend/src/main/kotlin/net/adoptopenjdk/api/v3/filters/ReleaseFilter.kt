package net.adoptopenjdk.api.v3.filters

import net.adoptopenjdk.api.v3.config.Ecosystem
import net.adoptopenjdk.api.v3.models.JvmImpl
import net.adoptopenjdk.api.v3.models.Release
import net.adoptopenjdk.api.v3.models.ReleaseType
import net.adoptopenjdk.api.v3.models.Vendor
import net.adoptopenjdk.api.v3.models.Versions
import java.util.function.Predicate

class ReleaseFilter(
    private val releaseType: ReleaseType? = null,
    private val featureVersion: Int? = null,
    private val releaseName: String? = null,
    private val vendor: Vendor? = null,
    private val versionRange: VersionRangeFilter? = null,
    private val lts: Boolean? = null,
    private val jvm_impl: JvmImpl? = null
) : Predicate<Release> {
    override fun test(release: Release): Boolean {
        val ltsFilter = if (lts != null) {
            val isLts = Versions.ltsVersions.contains(release.version_data.major)
            lts == isLts
        } else {
            true
        }

        val vendorFilters = if (Ecosystem.CURRENT == Ecosystem.adoptopenjdk && vendor == Vendor.adoptopenjdk) {
            if (jvm_impl == JvmImpl.openj9) {
                // if the user is requesting an openj9 from adopt, also include IBMs
                (release.vendor == Vendor.adoptopenjdk || (release.vendor == Vendor.ibm || release.vendor == Vendor.ibm_ce))
            } else {
                // if we are in the adoptopenjdk api, and adoptopenjdk builds are requests, then also include adoptium builds
                (release.vendor == Vendor.adoptium || release.vendor == Vendor.adoptopenjdk)
            }
        } else {
            (vendor == null || release.vendor == vendor)
        }

        return (releaseType == null || release.release_type == releaseType) &&
            (featureVersion == null || release.version_data.major == featureVersion) &&
            (releaseName == null || release.release_name == releaseName) &&
            vendorFilters &&
            (versionRange == null || versionRange.test(release.version_data)) &&
            ltsFilter
    }
}
