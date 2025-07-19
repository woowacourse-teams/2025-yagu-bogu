package com.yagubogu.presentation.home

import android.Manifest
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.yagubogu.R
import com.yagubogu.databinding.FragmentHomeBinding

@Suppress("ktlint:standard:backing-property-naming")
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val locationProvider by lazy { LocationProvider(requireContext()) }
    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            val granted = permissions.any { it.value }
            if (granted) {
                fetchLocationAndCheckIn()
            } else {
                showSnackbar(R.string.home_location_permission_denied_message)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupBindings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupBindings() {
        binding.btnCheckIn.setOnClickListener {
            if (locationProvider.isLocationPermissionGranted()) {
                fetchLocationAndCheckIn()
            } else {
                requestLocationPermissions()
            }
        }
    }

    private fun fetchLocationAndCheckIn() {
        locationProvider.fetchCurrentLocation(
            onSuccess = { location: Location ->
                val currentLatitude = location.latitude
                val currentLongitude = location.longitude
                Log.d("HomeFragment", "위도: $currentLatitude, 경도: $currentLongitude")
            },
            onFailure = { exception: Exception ->
                Log.e("HomeFragment", "위치 가져오기 실패", exception)
                showSnackbar(R.string.home_location_fetch_failed_message)
            },
        )
    }

    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }

    private fun showSnackbar(
        @StringRes message: Int,
    ) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).apply {
            setBackgroundTint(Color.DKGRAY)
            setTextColor(context.getColor(R.color.white))
            show()
        }
    }
}
