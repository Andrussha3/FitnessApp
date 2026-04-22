package com.example.fitnesapp.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesapp.R
import com.example.fitnesapp.presentation.component.ImageBannerCard
import com.example.fitnesapp.presentation.component.SectionCard
import com.example.fitnesapp.presentation.component.StatTile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenExercises: () -> Unit,
    onOpenWorkouts: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartWorkout: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Image(
                    painter = painterResource(R.drawable.drawer_menu_banner),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
                NavigationDrawerItem(label = { Text("История тренировок") }, selected = false, onClick = { onOpenHistory() })
                NavigationDrawerItem(label = { Text("Мои тренировки") }, selected = false, onClick = { onOpenWorkouts() })
                NavigationDrawerItem(label = { Text("Мои упражнения") }, selected = false, onClick = { onOpenExercises() })
                NavigationDrawerItem(label = { Text("Мой прогресс") }, selected = false, onClick = { onOpenProgress() })
                NavigationDrawerItem(label = { Text("Мои заметки") }, selected = false, onClick = { onOpenNotes() })
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("FitnesApp") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onOpenExercises, containerColor = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = androidx.compose.ui.Modifier.size(28.dp))
                }
            }
        ) { padding ->
            Column(
                modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ImageBannerCard(
                    imageRes = R.drawable.theme_banner_sport,
                    title = "Премиальный темный фитнес",
                    subtitle = "Четкий тренировочный ритм, быстрый доступ и спортивный акцент"
                )
                SectionCard(title = "Привет, ${state.profileName}", subtitle = state.todayMessage) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatTile("Завершено", "${state.workoutsDone}")
                        StatTile("Активно", if (state.activeSession != null) "Да" else "Нет")
                    }
                }
                SectionCard(title = "Быстрый старт") {
                    Text(
                        text = if (state.activeSession != null) "Есть незавершенная тренировка. Продолжите ее или отмените на следующем экране."
                        else "Начните тренировку по плану на сегодня одним нажатием.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.material3.Button(
                        onClick = onStartWorkout,
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 18.dp)
                    ) {
                        Text("Начать тренировку")
                    }
                }
            }
        }
    }
}
