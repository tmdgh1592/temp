package feature.notice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import core.common.util.defaultDateFormatter
import core.common.util.format
import core.designsystem.icons.filled.Bookmark
import core.designsystem.theme.Graphite
import core.designsystem.theme.Gray
import core.designsystem.theme.Mint01
import core.designsystem.theme.PaleGray
import core.designsystem.theme.WhatcamTheme
import core.designsystem.theme.White
import core.model.Notice
import feature.notice.model.NoticeWithBookmark
import org.jetbrains.compose.resources.stringResource
import whatcampus.composeapp.generated.resources.Res
import whatcampus.composeapp.generated.resources.bookmark_title

private val horizontalPadding = 12.dp

@Composable
internal fun NoticeList(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    noticesWithBookmark: List<NoticeWithBookmark>,
    onClickItem: (Notice) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 56.dp),
        state = listState,
    ) {
        itemsIndexed(
            items = noticesWithBookmark,
            key = { _, notice -> notice.id },
        ) { index, noticeWithBookmark ->
            NoticeItem(
                noticeWithBookmark = noticeWithBookmark,
                onClick = { onClickItem(noticeWithBookmark.notice) },
            )

            if (index < noticesWithBookmark.size - 1) {
                NoticeDivider()
            }
        }
    }
}

@Composable
private fun NoticeItem(
    modifier: Modifier = Modifier,
    noticeWithBookmark: NoticeWithBookmark,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 16.dp)
            .heightIn(min = 48.dp, max = 80.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = noticeWithBookmark.title,
                style = WhatcamTheme.typography.bodyLargeR,
                color = Graphite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.size(16.dp))

            Text(
                text = noticeWithBookmark.datetime.format(formatter = defaultDateFormatter),
                style = WhatcamTheme.typography.bodyMediumR,
                color = Gray,
            )
        }

        if (noticeWithBookmark.isBookmarked) {
            Spacer(modifier = Modifier.size(12.dp))

            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = stringResource(Res.string.bookmark_title),
                tint = White,
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        color = Mint01,
                        shape = CircleShape,
                    )
                    .padding(4.dp),
            )
        }
    }
}

@Composable
private fun NoticeDivider(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = horizontalPadding),
        thickness = 2.dp,
        color = PaleGray,
    )
}
