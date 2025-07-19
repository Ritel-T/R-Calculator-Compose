package com.ritel.calculator.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ritel.calculator.ui.scientific.ScientificScreen
import com.ritel.calculator.ui.simple.SimpleScreen
import com.ritel.calculator.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorApp()
        }
    }
}

private val animationSpec = tween<IntOffset>()

fun slideInAnimation(direction: AnimatedContentTransitionScope.SlideDirection): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) {
    return {
        slideIntoContainer(towards = direction, animationSpec = animationSpec)
    }
}

fun slideOutAnimation(direction: AnimatedContentTransitionScope.SlideDirection): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) {
    return {
        slideOutOfContainer(towards = direction, animationSpec = animationSpec)
    }
}

@Composable
fun CalculatorApp() {
    CalculatorTheme {
        val navController = rememberNavController()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = { BottomBar(navController) }) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Simple.route,
                Modifier.padding(innerPadding)
            ) {
                composable(
                    route = Screen.Simple.route,
                    enterTransition = slideInAnimation(AnimatedContentTransitionScope.SlideDirection.Right),
                    exitTransition = slideOutAnimation(AnimatedContentTransitionScope.SlideDirection.Left),
                    popEnterTransition = slideInAnimation(AnimatedContentTransitionScope.SlideDirection.Right),
                    popExitTransition = slideOutAnimation(AnimatedContentTransitionScope.SlideDirection.Left)
                ) {
                    SimpleScreen()
                }

                composable(
                    route = Screen.Scientific.route,
                    enterTransition = slideInAnimation(AnimatedContentTransitionScope.SlideDirection.Left),
                    exitTransition = slideOutAnimation(AnimatedContentTransitionScope.SlideDirection.Right),
                    popEnterTransition = slideInAnimation(AnimatedContentTransitionScope.SlideDirection.Left),
                    popExitTransition = slideOutAnimation(AnimatedContentTransitionScope.SlideDirection.Right)
                ) {
                    ScientificScreen()
                }
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val items = listOf(Screen.Simple, Screen.Scientific)

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorAppPreview() {
    CalculatorApp()
}
