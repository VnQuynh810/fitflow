package io.github.vnquynh810.fitflow.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.vnquynh810.fitflow.feature.coach.ChatbotRoute
import io.github.vnquynh810.fitflow.feature.dashboard.DashboardRoute
import io.github.vnquynh810.fitflow.feature.library.LibraryRoute
import io.github.vnquynh810.fitflow.feature.onboarding.OnboardingRoute
import io.github.vnquynh810.fitflow.feature.onboarding.ProvisioningRoute
import io.github.vnquynh810.fitflow.feature.onboarding.WorkoutSetupRoute
import io.github.vnquynh810.fitflow.feature.planner.DayDetailRoute
import io.github.vnquynh810.fitflow.feature.planner.DaySummaryRoute
import io.github.vnquynh810.fitflow.feature.planner.EditPlanRoute
import io.github.vnquynh810.fitflow.feature.planner.PlannerRoute
import io.github.vnquynh810.fitflow.feature.planner.RestDayRoute
import io.github.vnquynh810.fitflow.feature.planner.SupplementaryDetailRoute
import io.github.vnquynh810.fitflow.feature.profile.ProfileRoute
import io.github.vnquynh810.fitflow.feature.profile.UpdateGoalRoute
import io.github.vnquynh810.fitflow.feature.profile.WorkoutHistoryRoute
import io.github.vnquynh810.fitflow.feature.session.StreakAchievedRoute
import io.github.vnquynh810.fitflow.feature.session.SupplementarySessionRoute
import io.github.vnquynh810.fitflow.feature.session.WorkoutCompletedRoute
import io.github.vnquynh810.fitflow.feature.session.WorkoutSessionRoute
import io.github.vnquynh810.fitflow.feature.settings.WorkoutSettingsRoute

@Composable
fun FitFlowNavHost(
    startDestination: AppRoute,
    onRequestStepPermission: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize(),
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() },
    ) {
        composable<AppRoute.Dashboard>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            DashboardRoute(
                onOpenChatbot = { navController.navigate(AppRoute.Chatbot) },
                onOpenProfile = { navController.navigate(AppRoute.Profile) },
                onOpenPlanner = {
                    navController.navigate(AppRoute.Planner) {
                        popUpTo(AppRoute.Dashboard) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onOpenSupplementary = { id ->
                    navController.navigate(AppRoute.SupplementaryDetail(id))
                },
                onOpenDaySummary = { dayNumber, epochDay ->
                    navController.navigate(AppRoute.DayWorkoutSummary(dayNumber, epochDay))
                },
                onOpenHistory = { navController.navigate(AppRoute.History) },
                onOpenChangeGoal = { navController.navigate(AppRoute.UpdateGoal) },
                onOpenLibrary = {
                    navController.navigate(AppRoute.Library) {
                        popUpTo(AppRoute.Dashboard) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }

        composable<AppRoute.Onboarding> { entry ->
            val route = entry.toRoute<AppRoute.Onboarding>()
            OnboardingRoute(
                isRecalibrate = route.isRecalibrate,
                onFinishedOnboarding = { navController.navigate(AppRoute.WorkoutSetup) },
                onFinishedRecalibrate = { navController.popBackStack() },
            )
        }

        composable<AppRoute.WorkoutSetup> {
            WorkoutSetupRoute(
                onSetupSelected = { goal ->
                    navController.navigate(AppRoute.Provisioning(goal.name)) {
                        popUpTo(AppRoute.WorkoutSetup) { inclusive = true }
                    }
                },
            )
        }

        composable<AppRoute.Provisioning> { entry ->
            val route = entry.toRoute<AppRoute.Provisioning>()
            ProvisioningRoute(
                goalName = route.goalName,
                onPlanReady = {
                    navController.navigate(AppRoute.Dashboard) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable<AppRoute.Planner> {
            PlannerRoute(
                onDayClick = { dayNumber, isRest ->
                    if (isRest) {
                        navController.navigate(AppRoute.RestDayDetail(dayNumber))
                    } else {
                        navController.navigate(AppRoute.DayDetail(dayNumber))
                    }
                },
                onOpenSettings = {
                    navController.navigate(AppRoute.WorkoutSettings(inSession = false))
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable<AppRoute.DayDetail> { entry ->
            val route = entry.toRoute<AppRoute.DayDetail>()
            DayDetailRoute(
                dayNumber = route.dayNumber,
                onBack = { navController.popBackStack() },
                onStartSession = { startIndex ->
                    navController.navigate(AppRoute.WorkoutSession(route.dayNumber, startIndex))
                },
                onEditPlan = { navController.navigate(AppRoute.EditPlan(route.dayNumber)) },
                onOpenSettings = {
                    navController.navigate(AppRoute.WorkoutSettings(inSession = false))
                },
            )
        }

        composable<AppRoute.RestDayDetail> { entry ->
            val route = entry.toRoute<AppRoute.RestDayDetail>()
            RestDayRoute(
                dayNumber = route.dayNumber,
                onBack = { navController.popBackStack() },
            )
        }

        composable<AppRoute.EditPlan> { entry ->
            val route = entry.toRoute<AppRoute.EditPlan>()
            EditPlanRoute(
                dayNumber = route.dayNumber,
                onBack = { navController.popBackStack() },
            )
        }

        composable<AppRoute.DayWorkoutSummary> { entry ->
            val route = entry.toRoute<AppRoute.DayWorkoutSummary>()
            DaySummaryRoute(
                dayNumber = route.dayNumber,
                epochDay = route.epochDay,
                onBack = {
                    val popped = navController.popBackStack(AppRoute.Dashboard, inclusive = false)
                    if (!popped) {
                        navController.navigate(AppRoute.Dashboard) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onOpenPlanner = {
                    navController.navigate(AppRoute.Planner) {
                        popUpTo(AppRoute.Dashboard) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onOpenWorkoutDay = { targetDay ->
                    navController.navigate(AppRoute.DayDetail(targetDay))
                },
            )
        }

        composable<AppRoute.WorkoutSession> { entry ->
            val route = entry.toRoute<AppRoute.WorkoutSession>()
            WorkoutSessionRoute(
                dayNumber = route.dayNumber,
                startIndex = route.startIndex,
                onBack = { navController.popBackStack() },
                onOpenSettings = {
                    navController.navigate(AppRoute.WorkoutSettings(inSession = true))
                },
                onSessionCompleted = { dayNum, activeSeconds, isNewStreak ->
                    if (isNewStreak) {
                        navController.navigate(AppRoute.StreakAchieved(dayNum, activeSeconds)) {
                            popUpTo(AppRoute.Dashboard) { saveState = true }
                        }
                    } else {
                        navController.navigate(
                            AppRoute.WorkoutCompleted(
                                dayNumber = dayNum,
                                activeSeconds = activeSeconds,
                            )
                        ) {
                            popUpTo(AppRoute.Dashboard) { saveState = true }
                        }
                    }
                },
            )
        }

        composable<AppRoute.StreakAchieved> { entry ->
            val route = entry.toRoute<AppRoute.StreakAchieved>()
            StreakAchievedRoute(
                onContinue = {
                    navController.navigate(
                        AppRoute.WorkoutCompleted(
                            dayNumber = route.dayNumber,
                            activeSeconds = route.activeSeconds,
                        )
                    ) {
                        popUpTo(AppRoute.Dashboard) { saveState = true }
                    }
                },
            )
        }

        composable<AppRoute.WorkoutCompleted> { entry ->
            val route = entry.toRoute<AppRoute.WorkoutCompleted>()
            WorkoutCompletedRoute(
                dayNumber = route.dayNumber.takeIf { it > 0 },
                supplementaryId = route.supplementaryId,
                activeSeconds = route.activeSeconds,
                onFinishToDashboard = {
                    navController.navigate(AppRoute.Dashboard) {
                        popUpTo(AppRoute.Dashboard) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable<AppRoute.SupplementaryDetail> { entry ->
            val route = entry.toRoute<AppRoute.SupplementaryDetail>()
            SupplementaryDetailRoute(
                workoutId = route.workoutId,
                onBack = { navController.popBackStack() },
                onStartSession = {
                    navController.navigate(AppRoute.SupplementarySession(route.workoutId))
                },
                onOpenSettings = {
                    navController.navigate(AppRoute.WorkoutSettings(inSession = false))
                },
            )
        }

        composable<AppRoute.SupplementarySession> { entry ->
            val route = entry.toRoute<AppRoute.SupplementarySession>()
            SupplementarySessionRoute(
                workoutId = route.workoutId,
                onBack = { navController.popBackStack() },
                onOpenSettings = {
                    navController.navigate(AppRoute.WorkoutSettings(inSession = true))
                },
                onSessionCompleted = { workoutId, activeSeconds ->
                    navController.navigate(
                        AppRoute.WorkoutCompleted(
                            supplementaryId = workoutId,
                            activeSeconds = activeSeconds,
                        )
                    ) {
                        popUpTo(AppRoute.Dashboard) { saveState = true }
                    }
                },
            )
        }

        composable<AppRoute.Library> {
            LibraryRoute(onBack = { navController.popBackStack() })
        }

        composable<AppRoute.Profile> {
            ProfileRoute(
                onBack = { navController.popBackStack() },
                onReCalibrate = {
                    navController.navigate(AppRoute.Onboarding(isRecalibrate = true))
                },
                onRequestStepPermission = onRequestStepPermission,
            )
        }

        composable<AppRoute.History> {
            WorkoutHistoryRoute(onBack = { navController.popBackStack() })
        }

        composable<AppRoute.UpdateGoal> {
            UpdateGoalRoute(
                onBack = { navController.popBackStack() },
                onGoalSelectedForProvisioning = { goal ->
                    navController.navigate(AppRoute.Provisioning(goal.name)) {
                        popUpTo(AppRoute.UpdateGoal) { inclusive = true }
                    }
                },
            )
        }

        composable<AppRoute.WorkoutSettings> { entry ->
            val route = entry.toRoute<AppRoute.WorkoutSettings>()
            WorkoutSettingsRoute(
                isInWorkoutSession = route.inSession,
                onBack = { navController.popBackStack() },
            )
        }

        composable<AppRoute.Chatbot> {
            ChatbotRoute(onBack = { navController.popBackStack() })
        }
    }
}
