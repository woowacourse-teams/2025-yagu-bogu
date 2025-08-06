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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.yagubogu.R
import com.yagubogu.YaguBoguApplication
import com.yagubogu.databinding.FragmentHomeBinding
import com.yagubogu.presentation.home.model.CheckInUiEvent
import com.yagubogu.presentation.home.model.HomeUiModel
import com.yagubogu.presentation.home.model.StadiumStatsUiModel
import com.yagubogu.presentation.home.ranking.VictoryFairyAdapter
import com.yagubogu.presentation.home.ranking.VictoryFairyItem
import com.yagubogu.presentation.util.PermissionUtil
import java.time.LocalDate

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

    private var isTeamOccupancyChartExpanded: Boolean = false

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
        binding.btnCheckIn.setOnClickListener {
            if (isLocationPermissionGranted()) {
                viewModel.checkIn()
            } else {
                requestLocationPermissions()
            }
        }

        binding.ivRefresh.setOnClickListener { view: View ->
//            val today = LocalDate.now()
            val today = LocalDate.of(2025, 7, 25) // TODO: LocalDate.now()로 변경
            viewModel.fetchStadiumStats(today)
            view
                .animate()
                .rotationBy(360f)
                .setDuration(1000L)
                .start()
        }

        binding.constraintShowMore.setOnClickListener {
            isTeamOccupancyChartExpanded = !isTeamOccupancyChartExpanded
            when (isTeamOccupancyChartExpanded) {
                true -> {
                    binding.tvShowMore.text = getString(R.string.home_show_less)
                    binding.ivArrow.setImageResource(R.drawable.ic_arrow_up)
                }

                false -> {
                    binding.tvShowMore.text = getString(R.string.home_show_more)
                    binding.ivArrow.setImageResource(R.drawable.ic_arrow_down)
                }
            }
        }

        val victoryFairyAdapter = VictoryFairyAdapter()
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.rvVictoryFairy.apply {
            adapter = victoryFairyAdapter
            layoutManager = linearLayoutManager
        }
        victoryFairyAdapter.submitList(DUMMY_VICTORY_FAIRY_ITEMS)
        binding.layoutMyVictoryFairy.victoryFairyItem = DUMMY_MY_VICTORY_FAIRY
    }

    private fun setupObservers() {
        viewModel.homeUiModel.observe(viewLifecycleOwner) { value: HomeUiModel ->
            binding.homeUiModel = value
        }

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
            binding.stadiumStatsUiModel = value
            binding.layoutTeamOccupancy.teamOccupancyRates = value.stadiumOccupancyRates.first()
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
        private val DUMMY_MY_VICTORY_FAIRY: VictoryFairyItem =
            VictoryFairyItem(
                rank = 1,
                profileImageUrl = "",
                nickname = "이포르",
                teamName = "KIA",
                winRate = 100.0,
            )

        private val DUMMY_VICTORY_FAIRY_ITEMS: List<VictoryFairyItem> =
            listOf(
                DUMMY_MY_VICTORY_FAIRY,
                VictoryFairyItem(
                    rank = 2,
                    profileImageUrl = "",
                    nickname = "닉네임",
                    teamName = "삼성",
                    winRate = 87.2,
                ),
                VictoryFairyItem(
                    rank = 3,
                    profileImageUrl = "",
                    nickname = "닉네임",
                    teamName = "롯데",
                    winRate = 75.0,
                ),
                VictoryFairyItem(
                    rank = 4,
                    profileImageUrl = "",
                    nickname = "닉네임",
                    teamName = "두산",
                    winRate = 66.7,
                ),
                VictoryFairyItem(
                    rank = 982,
                    profileImageUrl = "",
                    nickname = "닉네임",
                    teamName = "한화",
                    winRate = 32.5,
                ),
            )
    }
}
