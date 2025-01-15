package feature.profile.subscreen.noticeCategory

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import core.common.extensions.collectAsStateMultiplatform
import core.common.extensions.collectUiEvent
import core.designsystem.components.NoticeCategoryList
import core.designsystem.components.WhatCamPullToRefreshContainer
import core.di.koinViewModel
import feature.profile.subscreen.noticeCategory.components.NoticeCategoryTopBar
import feature.profile.subscreen.noticeCategory.model.NoticeCategoryUiEvent
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import whatcampus.composeapp.generated.resources.Res
import whatcampus.composeapp.generated.resources.notice_category_saved_action_label
import whatcampus.composeapp.generated.resources.notice_category_saved_message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoticeCategoryScreen(
    modifier: Modifier = Modifier,
    viewModel: NoticeCategoryViewModel = koinViewModel(),
    onClickBack: () -> Unit,
    onClickSave: (savedMessage: String, actionLabel: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateMultiplatform()
    val savedMessage = stringResource(Res.string.notice_category_saved_message)
    val savedActionLabel = stringResource(Res.string.notice_category_saved_action_label)

    val refreshState = rememberPullToRefreshState()
    if (refreshState.isRefreshing) {
        viewModel.fetchNoticeCategories()
    }

    viewModel.commonUiEvent.collectUiEvent()
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { uiEvent ->
            when (uiEvent) {
                NoticeCategoryUiEvent.REFRESH_COMPLETE -> refreshState.endRefresh()
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(refreshState.nestedScrollConnection),
        topBar = {
            NoticeCategoryTopBar(
                onClickBack = onClickBack,
                onClickSave = {
                    viewModel.subscribeNoticeCategories()
                    onClickSave(savedMessage, savedActionLabel)
                },
            )
        }
    ) { paddingValues ->
        NoticeCategoryList(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 20.dp),
            noticeCategories = uiState.noticeCategories,
            subscribedNoticeCategories = uiState.subscribedNoticeCategories,
            onClickCategory = viewModel::toggleNoticeCategory,
        )

        WhatCamPullToRefreshContainer(
            modifier = Modifier.padding(paddingValues),
            refreshState = refreshState
        )
    }
}
