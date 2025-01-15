package feature.bookmark

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import core.common.extensions.collectAsStateMultiplatform
import core.common.util.logScreenEvent
import core.designsystem.components.WhatCamPullToRefreshContainer
import core.designsystem.components.dialog.WhatcamDialog
import core.designsystem.components.dialog.rememberDialogState
import core.di.koinViewModel
import core.model.Notice
import feature.bookmark.components.BookmarkList
import feature.bookmark.components.BookmarkTopBar
import feature.bookmark.components.EmptyBookmarkScreen
import feature.bookmark.model.BookmarkUiEvent
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import whatcampus.composeapp.generated.resources.Res
import whatcampus.composeapp.generated.resources.bookmark_delete_dialog_message
import whatcampus.composeapp.generated.resources.bookmark_delete_dialog_title
import whatcampus.composeapp.generated.resources.ic_alert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookmarkScreen(
    modifier: Modifier = Modifier,
    viewModel: BookmarkViewModel = koinViewModel(),
    onNoticeClick: (Notice) -> Unit,
) {
    logScreenEvent(screenName = "BookmarkScreen")

    val uiState by viewModel.uiState.collectAsStateMultiplatform()
    var isEditMode by rememberSaveable { mutableStateOf(false) }

    val dialogState = rememberDialogState()

    val refreshState = rememberPullToRefreshState()
    if (refreshState.isRefreshing) {
        viewModel.fetchBookmarkedNotices()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { uiEvent ->
            when (uiEvent) {
                BookmarkUiEvent.REFRESH_COMPLETE -> refreshState.endRefresh()
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(refreshState.nestedScrollConnection),
        topBar = {
            BookmarkTopBar(
                onClickEdit = { isEditMode = true },
                onClickCancel = {
                    isEditMode = false
                    viewModel.clearMarkedNoticesForDelete()
                },
                onClickSelectAll = {
                    if (uiState.isAllNoticesMarkedForDelete) {
                        viewModel.clearMarkedNoticesForDelete()
                    } else {
                        viewModel.markAllNoticesForDelete()
                    }
                },
                onClickUnbookmark = dialogState::showDialog,
                isEditMode = isEditMode,
                isAllSelected = uiState.isAllNoticesMarkedForDelete,
                isShowActions = !uiState.isEmptyBookmark,
            )
        },
    ) { paddingValues ->
        if (uiState.isEmptyBookmark) {
            EmptyBookmarkScreen(modifier = Modifier.padding(paddingValues))
        } else {
            BookmarkList(
                paddingValues = paddingValues,
                uiState = uiState,
                onClickNotice = onNoticeClick,
                onClickNoticeForDelete = viewModel::markBookmarkForDelete,
                isEditMode = isEditMode
            )
        }

        if (dialogState.isVisible.value) {
            WhatcamDialog(
                title = stringResource(Res.string.bookmark_delete_dialog_title),
                message = stringResource(Res.string.bookmark_delete_dialog_message),
                icon = painterResource(Res.drawable.ic_alert),
                onConfirmClick = {
                    isEditMode = false
                    viewModel.unbookmarkNotices()
                    dialogState.hideDialog()
                },
                onDismissClick = dialogState::hideDialog,
            )
        }

        WhatCamPullToRefreshContainer(
            modifier = Modifier.padding(paddingValues),
            refreshState = refreshState,
        )
    }
}
