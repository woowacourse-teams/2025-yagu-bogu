package com.yagubogu.presentation.home

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.yagubogu.R
import com.yagubogu.YaguBoguApplication
import com.yagubogu.databinding.FragmentHomeBinding
import com.yagubogu.presentation.home.model.CheckInUiEvent
import com.yagubogu.presentation.home.model.StadiumStatsUiModel
import com.yagubogu.presentation.home.ranking.VictoryFairyAdapter
import com.yagubogu.presentation.home.ranking.VictoryFairyRanking
import com.yagubogu.presentation.home.stadium.StadiumFanRateAdapter
import com.yagubogu.presentation.util.PermissionUtil

@Suppress("ktlint:standard:backing-property-naming")
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        val app = requireActivity().application as YaguBoguApplication
        HomeViewModelFactory(
            app.memberRepository,
            app.checkInsRepository,
            app.statsRepository,
            app.locationRepository,
            app.stadiumRepository,
        )
    }

    private val locationPermissionLauncher = createLocationPermissionLauncher()

    private val stadiumFanRateAdapter: StadiumFanRateAdapter by lazy { StadiumFanRateAdapter() }
    private val victoryFairyAdapter: VictoryFairyAdapter by lazy { VictoryFairyAdapter() }

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
        setupObservers()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.fetchAll()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupBindings() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.btnCheckIn.setOnClickListener {
            if (isLocationPermissionGranted()) {
                viewModel.checkIn()
            } else {
                requestLocationPermissions()
            }
        }

        binding.rvStadiumFanRate.adapter = stadiumFanRateAdapter
        binding.rvVictoryFairy.adapter = victoryFairyAdapter

        binding.ivRefresh.setOnClickListener { view: View ->
            viewModel.fetchStadiumStats()
            view
                .animate()
                .rotationBy(REFRESH_ANIMATION_ROTATION)
                .setDuration(REFRESH_ANIMATION_DURATION)
                .start()
        }
    }

    private fun setupObservers() {
        viewModel.checkInUiEvent.observe(viewLifecycleOwner) { value: CheckInUiEvent ->
            showSnackbar(
                when (value) {
                    is CheckInUiEvent.CheckInSuccess -> R.string.home_check_in_success_message
                    CheckInUiEvent.CheckInFailure -> R.string.home_check_in_failure_message
                    CheckInUiEvent.LocationFetchFailed -> R.string.home_location_fetch_failed_message
                },
            )
        }

        viewModel.stadiumStatsUiModel.observe(viewLifecycleOwner) { value: StadiumStatsUiModel ->
            stadiumFanRateAdapter.submitList(value.stadiumFanRates)
        }

        viewModel.victoryFairyRanking.observe(viewLifecycleOwner) { value: VictoryFairyRanking ->
            victoryFairyAdapter.submitList(value.topRankings)
            binding.layoutMyVictoryFairy.victoryFairyItem =
                value.myRanking.copy(
                    nickname =
                        getString(
                            R.string.home_victory_fairy_my_nickname,
                            value.myRanking.nickname,
                        ),
                )
        }
    }

    private fun createLocationPermissionLauncher(): ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isPermissionGranted = permissions.any { it.value }
            val shouldShowRationale =
                permissions.keys.any { permission: String ->
                    PermissionUtil.shouldShowRationale(requireActivity(), permission)
                }
            when {
                isPermissionGranted -> viewModel.checkIn()
                shouldShowRationale -> showSnackbar(R.string.home_location_permission_denied_message)
                else -> showPermissionDeniedDialog()
            }
        }

    private fun isLocationPermissionGranted(): Boolean {
        val isFineLocationPermissionGranted =
            PermissionUtil.isGranted(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        val isCoarseLocationPermissionGranted =
            PermissionUtil.isGranted(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        return isFineLocationPermissionGranted || isCoarseLocationPermissionGranted
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
            setAnchorView(R.id.bnv_navigation)
            show()
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog
            .Builder(requireContext())
            .setTitle(R.string.permission_dialog_location_title)
            .setMessage(R.string.permission_dialog_location_description)
            .setPositiveButton(R.string.permission_dialog_open_settings) { _, _ ->
                openAppSettings()
            }.setNegativeButton(R.string.all_cancel, null)
            .setCancelable(false)
            .show()
    }

    private fun openAppSettings() {
        val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts(PACKAGE_SCHEME, requireContext().packageName, null)
            }
        startActivity(intent)
    }

    companion object {
        private const val PACKAGE_SCHEME = "package"
        private const val REFRESH_ANIMATION_ROTATION = 360f
        private const val REFRESH_ANIMATION_DURATION = 1000L
    }
}
