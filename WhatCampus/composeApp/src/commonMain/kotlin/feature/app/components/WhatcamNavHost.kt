package feature.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import core.common.extensions.collectUiEvent
import core.di.koinViewModel
import core.navigation.MainRoute
import feature.app.navigation.WhatcamNavigator
import feature.main.navigation.mainNavGraph
import feature.notice.navigation.noticeDetailNavGraph
import feature.noticeSearch.navigation.noticeSearchNavGraph
import feature.notificationArchive.navigation.notificationArchiveNavGraph
import feature.onboarding.navigation.onboardingNavGraph
import feature.profile.navigation.profileNavGraph
import feature.splash.navigation.splashNavGraph
import feature.university.UniversityViewModel
import feature.university.model.UniversityUiEvent
import feature.university.navigation.universityNavGraph
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinContext
import whatcampus.composeapp.generated.resources.Res
import whatcampus.composeapp.generated.resources.university_load_failed_message
import whatcampus.composeapp.generated.resources.user_save_success_message

@Composable
internal fun WhatcamNavHost(
    navigator: WhatcamNavigator,
    onShowSnackbar: (message: String, actionLabel: String?) -> Unit,
) {
    KoinContext {
        val universityViewModel = koinViewModel<UniversityViewModel>()

        universityViewModel.commonUiEvent.collectUiEvent()
        universityViewModel.uiEvent.collectUniversityUiEvent(
            onShowSnackbar = onShowSnackbar,
            navigator = navigator,
        )

        NavHost(
            navController = navigator.navController,
            startDestination = navigator.startDestination,
        ) {
            splashNavGraph { shouldOnboarding ->
                if (shouldOnboarding) {
                    navigator.navigateOnboarding(popUpTargetRoute = MainRoute.SplashRoute)
                } else {
                    navigator.navigateMain(popUpTargetRoute = MainRoute.SplashRoute)
                }
            }
            onboardingNavGraph(
                onboardingComplete = { navigator.navigateUniversitySelectivity() }
            )
            universityNavGraph(
                viewModel = universityViewModel,
                onClickBack = navigator::navigateUp,
                onClickUniversity = { navigator.navigateDepartmentSelectivity() },
                onClickDepartment = { navigator.navigateNoticeCategorySelectivity() },
                onClickSaveNoticeCategory = { navigator.navigateUniversityComplete() },
                onClickComplete = universityViewModel::saveUser,
            )
            mainNavGraph(
                onNoticeClick = navigator::navigateNoticeDetail,
                onClickNoticeSearch = navigator::navigateNoticeSearch,
                onClickNotificationArchive = navigator::navigateNotificationArchive,
                onClickProfile = navigator::navigateProfile,
            )
            noticeDetailNavGraph(
                onClickBack = navigator::navigateUp,
            )
            noticeSearchNavGraph(
                onClickBack = navigator::navigateUp,
                onClickNotice = navigator::navigateNoticeDetail
            )
            notificationArchiveNavGraph(
                onClickBack = navigator::navigateUp,
                onClickNewNoticeNotification = { newNoticeNotification ->
                    navigator.navigateNoticeDetail(notice = newNoticeNotification.notice)
                },
            )
            profileNavGraph(
                onClickBack = navigator::navigateUp,
                onClickNoticeCategory = navigator::navigateNoticeCategory,
                onClickNoticeCategorySave = { savedMessage, actionLabel ->
                    navigator.navigateUp()
                    onShowSnackbar(savedMessage, actionLabel)
                },
                onClickUniversityChange = { navigator.navigateOnboarding(popUpTargetRoute = MainRoute.HomeRoute) },
                onClickFaq = navigator::navigateFaq,
                onClickPrivacy = navigator::navigatePrivacy,
            )
        }
    }
}

@Composable
private fun SharedFlow<UniversityUiEvent>.collectUniversityUiEvent(
    onShowSnackbar: (message: String, actionLabel: String?) -> Unit,
    navigator: WhatcamNavigator,
) {
    val universityLoadFailedMessage = stringResource(Res.string.university_load_failed_message)
    val userSaveSuccessMessage = stringResource(Res.string.user_save_success_message)

    LaunchedEffect(this) {
        collectLatest { uiEvent ->
            when (uiEvent) {
                UniversityUiEvent.UNIVERSITY_LOAD_FAILED -> onShowSnackbar(
                    universityLoadFailedMessage,
                    null
                )

                UniversityUiEvent.USER_SAVE_SUCCESS -> {
                    onShowSnackbar(userSaveSuccessMessage, null)
                    navigator.navigateMain(popUpTargetRoute = MainRoute.OnboardingRoute)
                }
            }
        }
    }
}
