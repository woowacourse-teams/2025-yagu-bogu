package com.yagubogu.presentation.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.yagubogu.R
import com.yagubogu.YaguBoguApplication
import com.yagubogu.databinding.FragmentHomeBinding
import com.yagubogu.presentation.MainActivity
import com.yagubogu.presentation.dialog.DefaultDialogFragment
import com.yagubogu.presentation.dialog.DefaultDialogUiModel
import com.yagubogu.presentation.home.model.CheckInUiEvent
import com.yagubogu.presentation.home.model.StadiumStatsUiModel
import com.yagubogu.presentation.home.ranking.VictoryFairyAdapter
import com.yagubogu.presentation.home.ranking.VictoryFairyRanking
import com.yagubogu.presentation.home.stadium.StadiumFanRateAdapter
import com.yagubogu.presentation.util.PermissionUtil
import com.yagubogu.presentation.util.ScrollToTop
import com.yagubogu.presentation.util.buildBalloon
import com.yagubogu.presentation.util.showSnackbar

@Suppress("ktlint:standard:backing-property-naming")
class HomeFragment :
    Fragment(),
    ScrollToTop {
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
            app.streamRepository,
        )
    }

    private val locationPermissionLauncher = createLocationPermissionLauncher()

    private val stadiumFanRateAdapter: StadiumFanRateAdapter by lazy { StadiumFanRateAdapter() }
    private val victoryFairyAdapter: VictoryFairyAdapter by lazy { VictoryFairyAdapter() }

    private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }

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
        setupFragmentResultListener()
        setupBalloons()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.fetchAll()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.startStreaming()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopStreaming()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun scrollToTop() {
        binding.nsvRoot.smoothScrollTo(0, 0)
    }

    private fun setupBindings() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.btnCheckIn.setOnClickListener {
            if (isLocationPermissionGranted()) {
                checkLocationSettingsThenShowDialog(requestLocationServices())
            } else {
                requestLocationPermissions()
            }
        }

        binding.rvStadiumFanRate.adapter = stadiumFanRateAdapter
        binding.rvVictoryFairy.adapter = victoryFairyAdapter

        binding.ivRefresh.setOnClickListener { view: View ->
//            viewModel.fetchStadiumStats()
            viewModel.updateRefreshTime()
            view
                .animate()
                .rotationBy(REFRESH_ANIMATION_ROTATION)
                .setDuration(REFRESH_ANIMATION_DURATION)
                .start()
            firebaseAnalytics.logEvent("fan_rate_refresh", null)
        }
    }

    private fun setupObservers() {
        viewModel.checkInUiEvent.observe(viewLifecycleOwner) { value: CheckInUiEvent ->
            val message: String =
                when (value) {
                    is CheckInUiEvent.Success ->
                        getString(R.string.home_check_in_success_message, value.stadium.fullName)

                    CheckInUiEvent.OutOfRange -> getString(R.string.home_check_in_out_of_range_message)
                    CheckInUiEvent.LocationFetchFailed -> getString(R.string.home_check_in_location_fetch_failed_message)
                    CheckInUiEvent.NetworkFailed -> getString(R.string.home_check_in_network_failed_message)
                }
            binding.root.showSnackbar(message, R.id.bnv_navigation)
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

        viewModel.isCheckInLoading.observe(viewLifecycleOwner) { value: Boolean ->
            (requireActivity() as MainActivity).setLoadingScreen(value)
        }
    }

    private fun setupFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(
            KEY_CHECK_IN_REQUEST_DIALOG,
            viewLifecycleOwner,
        ) { _, bundle ->
            val isConfirmed = bundle.getBoolean(DefaultDialogFragment.KEY_CONFIRM)
            if (isConfirmed) {
                viewModel.checkIn()
                firebaseAnalytics.logEvent("check_in", null)
            }
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
                isPermissionGranted -> checkLocationSettingsThenShowDialog(requestLocationServices())
                shouldShowRationale ->
                    binding.root.showSnackbar(
                        R.string.home_location_permission_denied_message,
                        R.id.bnv_navigation,
                    )

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

    private fun showCheckInConfirmDialog() {
        if (parentFragmentManager.findFragmentByTag(KEY_CHECK_IN_REQUEST_DIALOG) == null) {
            val dialogUiModel =
                DefaultDialogUiModel(
                    title = getString(R.string.home_check_in_confirm),
                    emoji = getString(R.string.home_check_in_stadium_emoji),
                    message = getString(R.string.home_check_in_caution),
                )
            val dialog =
                DefaultDialogFragment.newInstance(KEY_CHECK_IN_REQUEST_DIALOG, dialogUiModel)

            dialog.show(parentFragmentManager, KEY_CHECK_IN_REQUEST_DIALOG)
        }
    }

    private fun requestLocationServices(): Task<LocationSettingsResponse> {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0).build()

        val locationSettingsRequestBuilder =
            LocationSettingsRequest
                .Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)

        val settingsClient: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        return settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build())
    }

    private fun checkLocationSettingsThenShowDialog(task: Task<LocationSettingsResponse>) {
        task
            .addOnSuccessListener {
                // 위치 설정이 활성화된 경우 직관 인증 확인 다이얼로그 표시
                showCheckInConfirmDialog()
            }.addOnFailureListener { exception ->
                // 다이얼로그 띄워서 사용자가 GPS 켜도록 안내
                if (exception is ResolvableApiException) {
                    exception.startResolutionForResult(requireActivity(), RC_LOCATION_SETTINGS)
                } else {
                    binding.root.showSnackbar(
                        R.string.home_location_settings_disabled,
                        R.id.bnv_navigation,
                    )
                }
            }
    }

    private fun setupBalloons() {
        val stadiumStatsInfoBalloon =
            requireContext().buildBalloon(
                getString(R.string.home_stadium_stats_tooltip),
                viewLifecycleOwner,
            )
        binding.frameStadiumStatsTooltip.setOnClickListener {
            stadiumStatsInfoBalloon.showAlignBottom(binding.ivStadiumStatsTooltip)
            firebaseAnalytics.logEvent("tooltip_stadium_stats", null)
        }

        val victoryFairyInfoBalloon =
            requireContext().buildBalloon(
                getString(R.string.home_victory_fairy_tooltip),
                viewLifecycleOwner,
            )
        binding.frameVictoryFairyRankingTooltip.setOnClickListener {
            victoryFairyInfoBalloon.showAlignBottom(binding.ivVictoryFairyRankingTooltip)
            firebaseAnalytics.logEvent("tooltip_victory_fairy_ranking", null)
        }
    }

    companion object {
        private const val PACKAGE_SCHEME = "package"
        private const val KEY_CHECK_IN_REQUEST_DIALOG = "checkInRequest"
        private const val REFRESH_ANIMATION_ROTATION = 360f
        private const val REFRESH_ANIMATION_DURATION = 1000L
        private const val RC_LOCATION_SETTINGS = 1001
    }
}
