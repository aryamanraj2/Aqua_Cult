package com.parth.aquasense.presentation.screens.tanklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parth.aquasense.domain.model.Tank
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.parth.aquasense.presentation.components.TankCard

/**
 * Tank List Screen - Shows list of all tanks
 *
 * Features:
 * - Loading indicator while fetching
 * - Scrollable list of tanks
 * - Empty state when no tanks
 * - Error state with retry button
 * - Pull-to-refresh
 * - FAB to add new tank
 *
 * @param onNavigateToTankDetail Navigate to tank details screen
 * @param onNavigateToTankForm Navigate to add tank screen
 * @param viewModel ViewModel (injected by Hilt)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TankListScreen(
    onNavigateToTankDetail: (String) -> Unit,
    onNavigateToTankForm: () -> Unit,
    viewModel: TankListViewModel = hiltViewModel()
) {
    // Collect UI state
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Pull-to-refresh state
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tanks", fontWeight = FontWeight.Bold, fontSize = 35.sp) },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToTankForm,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Tank"
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            state = pullRefreshState,
            isRefreshing = uiState is TankListUiState.Loading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            when (val state = uiState) {
                is TankListUiState.Loading -> {
                    LoadingState()
                }

                is TankListUiState.Success -> {
                    TankListContent(
                        tanks = state.tanks,
                        onTankClick = onNavigateToTankDetail,
                        onAddTank = onNavigateToTankForm
                    )
                }

                is TankListUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadTanks() }
                    )
                }
            }
        }
    }
}

/**
 * Loading State - Centered circular progress indicator
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Tank List Content - Search Bar + Scrollable list of tanks
 */
@Composable
private fun TankListContent(
    tanks: List<Tank>,
    onTankClick: (String) -> Unit,
    onAddTank: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val filteredTanks = remember(tanks, searchQuery) {
        if (searchQuery.isBlank()) {
            tanks
        } else {
            tanks.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.location?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { focusManager.clearFocus() }
        )

        if (filteredTanks.isEmpty() && searchQuery.isNotBlank()) {
            // Empty search results
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tanks found matching \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else if (filteredTanks.isEmpty()) {
            // No tanks at all
            EmptyState(onAddTank = onAddTank)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, 
                    end = 16.dp, 
                    top = 16.dp, 
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = filteredTanks,
                    key = { tank -> tank.id }
                ) { tank ->
                    TankCard(
                        tank = tank,
                        onClick = { onTankClick(tank.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search tanks...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        shape = RoundedCornerShape(50),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = com.parth.aquasense.ui.theme.AquaBlue,
            unfocusedBorderColor = com.parth.aquasense.ui.theme.AquaBlue.copy(alpha = 0.3f)
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        textStyle = MaterialTheme.typography.bodyLarge
    )
}

/**
 * Empty State - No tanks message with add button
 */
@Composable
private fun EmptyState(
    onAddTank: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No Tanks Yet",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add your first tank to get started tracking your aquaculture",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        FilledTonalButton(onClick = onAddTank) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Your First Tank")
        }
    }
}

/**
 * Error State - Error message with retry button
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Oops! Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        FilledTonalButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}
