package com.parth.aquasense.presentation.screens.marketplace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parth.aquasense.domain.model.Product
import com.parth.aquasense.presentation.components.MarketplaceGridItem
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import java.util.Locale

/**
 * Marketplace screen showing all products with category filtering.
 *
 * Features:
 * - Product list with ProductCard components
 * - Category filter chips
 * - Pull-to-refresh
 * - Navigate to product detail
 * - Navigate to cart
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Marketplace",
                        fontWeight = FontWeight.Bold,
                        fontSize = 35.sp
                    ) 
                },
                actions = {
                    IconButton(onClick = onNavigateToCart) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = (uiState as? MarketplaceUiState.Success)?.isRefreshing ?: false,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            when (val state = uiState) {
                is MarketplaceUiState.Loading -> LoadingState()
                is MarketplaceUiState.Success -> {
                    if (state.products.isEmpty()) {
                        EmptyState()
                    } else {
                        MarketplaceContent(
                            products = state.products,
                            categories = state.categories,
                            selectedCategory = state.selectedCategory,
                            onProductClick = onNavigateToProductDetail,
                            onCategoryChange = { viewModel.filterByCategory(it) }
                        )
                    }
                }
                is MarketplaceUiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadProducts() }
                )
            }
        }
    }
}


@Composable
private fun MarketplaceContent(
    products: List<Product>,
    categories: List<String>,
    selectedCategory: String?,
    onProductClick: (String) -> Unit,
    onCategoryChange: (String?) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) {
            products
        } else {
            products.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { focusManager.clearFocus() }
        )

        // Category filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                MarketplaceFilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategoryChange(null) },
                    label = "All",
                    icon = Icons.Default.GridView
                )
            }
            items(categories) { category ->
                MarketplaceFilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategoryChange(category) },
                    label = category.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    },
                    icon = getCategoryIcon(category)
                )
            }
        }


        // Product list
        if (filteredProducts.isEmpty() && searchQuery.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No products found matching \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 100.dp // Accommodate floating navigation pill
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    MarketplaceGridItem(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        modifier = Modifier.animateItem()
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
        placeholder = { Text("Search products...") },
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

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingBag,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = "No products available",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "Check back later for new products",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

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
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        FilledTonalButton(
            onClick = onRetry,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun MarketplaceFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { 
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 6.dp)
            ) 
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        shape = RoundedCornerShape(50),
        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
            selectedContainerColor = com.parth.aquasense.ui.theme.AquaBlue,
            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
            selectedLeadingIconColor = androidx.compose.ui.graphics.Color.White,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.onSurface
        ),
        border = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) com.parth.aquasense.ui.theme.AquaBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            borderWidth = 1.dp
        ),
        modifier = Modifier.height(48.dp)
    )
}

private fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        category.contains("feed", ignoreCase = true) || category.contains("food", ignoreCase = true) -> Icons.Default.Restaurant
        category.contains("medicine", ignoreCase = true) || category.contains("treatment", ignoreCase = true) -> Icons.Default.MedicalServices
        category.contains("equipment", ignoreCase = true) || category.contains("device", ignoreCase = true) -> Icons.Default.Settings
        category.contains("iot", ignoreCase = true) || category.contains("sensor", ignoreCase = true) -> Icons.Default.Sensors
        else -> Icons.Default.Category
    }
}
