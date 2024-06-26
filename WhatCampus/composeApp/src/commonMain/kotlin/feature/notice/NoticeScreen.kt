package feature.notice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import core.common.extensions.collectAsStateMultiplatform
import core.di.koinViewModel
import core.model.NoticeCategory
import feature.notice.components.NoticeCategoryBar
import feature.notice.components.NoticeList
import feature.notice.components.NoticeTopAppBar
import feature.notice.model.NoticeUiState

@Composable
fun NoticeScreen(
    noticeViewModel: NoticeViewModel = koinViewModel(),
) {
    val uiState by noticeViewModel.uiState.collectAsStateMultiplatform()

    when (uiState) {
        NoticeUiState.Loading -> {
            // TODO: 로딩 화면 구현
        }

        is NoticeUiState.Success -> NoticeScreen(
            uiState = uiState as NoticeUiState.Success,
            onClickCategory = { noticeViewModel.selectCategory(it) },
        )
    }
}

@Composable
private fun NoticeScreen(
    uiState: NoticeUiState.Success,
    onClickCategory: (NoticeCategory) -> Unit,
) {
    Scaffold(
        topBar = {
            NoticeTopAppBar(
                onClickSearch = {},
                onClickNotification = {},
                onClickProfile = {},
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            val noticeListScrollState = rememberLazyListState()

            val isAtTop by remember {
                derivedStateOf {
                    noticeListScrollState.firstVisibleItemIndex < 2
                }
            }

            NoticeList(
                listState = noticeListScrollState,
                notices = uiState.notices,
                onClickItem = {},
            )

            AnimatedVisibility(
                visible = isAtTop,
            ) {
                NoticeCategoryBar(
                    noticeCategories = uiState.noticeCategories,
                    selectedCategory = uiState.selectedCategory,
                    onClickCategory = onClickCategory,
                )
            }
        }
    }
}
