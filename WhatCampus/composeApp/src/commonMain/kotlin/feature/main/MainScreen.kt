package feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import core.model.Notice
import feature.bookmark.BookmarkScreen
import feature.campusmap.CampusMapScreen
import feature.main.components.MainBottomAppBar
import feature.main.navigation.MainRoute
import feature.main.navigation.rememberMainNavigator
import feature.notice.NoticeScreen

@Composable
internal fun MainScreen(
    modifier: Modifier = Modifier,
    onNoticeClick: (Notice) -> Unit,
) {
    val navigator = rememberMainNavigator()

    Scaffold(
        modifier = modifier,
        bottomBar = { MainBottomAppBar(navigator = navigator) },
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navigator.navController,
            startDestination = navigator.startDestination.route,
        ) {
            composable(MainRoute.NOTICE.route) {
                NoticeScreen(
                    onNoticeClick = onNoticeClick,
                )
            }
            composable(MainRoute.BOOKMARK.route) {
                BookmarkScreen(
                    onNoticeClick = onNoticeClick,
                )
            }
            composable(MainRoute.CHAT.route) {
//                ChatScreen()
            }
            composable(MainRoute.CAMPUS_MAP.route) {
//                CampusMapScreen()
            }
        }
    }
}
