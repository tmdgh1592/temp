package feature.university.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import core.common.extensions.navigateSingleTop
import core.navigation.Route
import feature.university.UniversitySelectivityScreen

fun NavController.navigateUniversitySelectivity() {
    navigateSingleTop(Route.UniversitySelectivityRoute.route)
}

fun NavGraphBuilder.universityNavGraph() {
    composable(Route.UniversitySelectivityRoute.route) {
        UniversitySelectivityScreen()
    }
}
