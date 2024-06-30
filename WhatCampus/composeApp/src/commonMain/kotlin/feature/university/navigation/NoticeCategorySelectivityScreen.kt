package feature.university.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.common.extensions.collectAsStateMultiplatform
import core.designsystem.components.NoticeCategoryList
import core.designsystem.theme.Graphite
import core.designsystem.theme.Mint01
import core.designsystem.theme.WhatcamTheme
import core.designsystem.theme.White
import core.model.NoticeCategory
import feature.university.UniversityViewModel
import org.jetbrains.compose.resources.stringResource
import whatcampus.composeapp.generated.resources.Res
import whatcampus.composeapp.generated.resources.department_title
import whatcampus.composeapp.generated.resources.notice_category_selectivity_desc
import whatcampus.composeapp.generated.resources.notice_category_selectivity_title
import whatcampus.composeapp.generated.resources.onboarding_start

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoticeCategorySelectivityScreen(
    modifier: Modifier = Modifier,
    viewModel: UniversityViewModel,
    onClickSave: () -> Unit,
    onClickBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateMultiplatform()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.department_title),
                            tint = Graphite,
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(Res.string.notice_category_selectivity_title),
                style = WhatcamTheme.typography.headlineMediumM,
                color = Mint01,
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            Text(
                text = stringResource(Res.string.notice_category_selectivity_desc),
                style = WhatcamTheme.typography.bodyLargeR,
                color = Graphite,
            )

            Spacer(modifier = Modifier.padding(top = 40.dp))

            NoticeCategoryList(
                noticeCategories = uiState.noticeCategories,
                selectedNoticeCategories = uiState.selectedNoticeCategories,
                onClickCategory = viewModel::toggleNoticeCategory,
                onClickSave = onClickSave,
            )
        }
    }
}

@Composable
private fun NoticeCategoryList(
    noticeCategories: List<NoticeCategory>,
    selectedNoticeCategories: Set<NoticeCategory>,
    onClickCategory: (NoticeCategory) -> Unit,
    onClickSave: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        NoticeCategoryList(
            modifier = Modifier.fillMaxSize(),
            noticeCategories = noticeCategories,
            subscribedNoticeCategories = selectedNoticeCategories,
            onClickCategory = onClickCategory,
            isShowDescription = false,
        )

        Button(
            onClick = onClickSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .align(Alignment.BottomCenter),
        ) {
            Text(
                text = stringResource(Res.string.onboarding_start),
                style = WhatcamTheme.typography.bodyLargeB,
                color = White,
            )
        }
    }
}
