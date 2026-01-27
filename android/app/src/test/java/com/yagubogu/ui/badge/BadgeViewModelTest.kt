package com.yagubogu.ui.badge

import co.touchlab.kermit.Logger
import com.yagubogu.fixture.BADGE_ID_0_ACQUIRED_FIXTURE
import com.yagubogu.fixture.BADGE_ID_1_ACQUIRED_FIXTURE
import com.yagubogu.fixture.MemberFakeRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class BadgeViewModelTest :
    StringSpec({
        lateinit var memberRepository: MemberFakeRepository
        lateinit var badgeViewModel: BadgeViewModel
        val testLogger = Logger

        beforeTest { Dispatchers.setMain(StandardTestDispatcher()) }
        afterTest { Dispatchers.resetMain() }

        "초기 배지 조회 요청이 성공하면 BadgeUiState가 Success 상태이다" {
            runTest {
                // given & when
                memberRepository = MemberFakeRepository(isFailureMode = false)
                badgeViewModel = BadgeViewModel(memberRepository, testLogger)

                advanceUntilIdle()

                // then
                badgeViewModel.badgeUiState.value.shouldBeTypeOf<BadgeUiState.Success>()
            }
        }

        "초기 배지 조회 요청이 실패하면 BadgeUiState가 Loading 상태이다" {
            runTest {
                // given & when
                memberRepository = MemberFakeRepository(isFailureMode = true)
                badgeViewModel = BadgeViewModel(memberRepository, testLogger)

                advanceUntilIdle()

                // then
                badgeViewModel.badgeUiState.value shouldBe BadgeUiState.Loading
            }
        }

        "대표 배지 설정 요청이 성공하면 representativeBadge가 갱신된다" {
            runTest {
                // given - 초기 대표 배지 0번으로 설정 요청
                memberRepository =
                    MemberFakeRepository(
                        isFailureMode = false,
                        badgeList =
                            listOf(BADGE_ID_0_ACQUIRED_FIXTURE, BADGE_ID_1_ACQUIRED_FIXTURE),
                    )
                badgeViewModel = BadgeViewModel(memberRepository, testLogger)
                badgeViewModel.updateRepresentativeBadge(0)

                // when - 대표 배지 1번으로 설정 요청
                badgeViewModel.updateRepresentativeBadge(1)

                advanceUntilIdle()

                // then
                badgeViewModel.badgeUiState.value
                    .shouldBeTypeOf<BadgeUiState.Success>()
                    .representativeBadge
                    ?.id shouldBe 1
            }
        }

        "대표 배지 설정 요청이 실패하면 representativeBadge가 갱신되지 않는다" {
            runTest {
                // given - 초기 대표 배지 로딩 성공, 0번으로 설정
                memberRepository =
                    MemberFakeRepository(
                        isFailureMode = false,
                        badgeList =
                            listOf(BADGE_ID_0_ACQUIRED_FIXTURE, BADGE_ID_1_ACQUIRED_FIXTURE),
                    )
                badgeViewModel = BadgeViewModel(memberRepository, testLogger)
                advanceUntilIdle()

                // when - 대표 배지 1번으로 설정 요청, 실패
                memberRepository.isFailureMode = true
                badgeViewModel.updateRepresentativeBadge(1)
                advanceUntilIdle()

                // then
                badgeViewModel.badgeUiState.value
                    .shouldBeTypeOf<BadgeUiState.Success>()
                    .representativeBadge
                    ?.id shouldBe 0
            }
        }
    })
