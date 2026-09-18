package com.example.eventsnowcielo.features.events.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    onEventClick: (String) -> Unit,
    onTicketsClick: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: EventListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartItemCount by viewModel.cartItemCount.collectAsState()
    val completedOrdersCount by viewModel.completedOrdersCount.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val appliedFilters by viewModel.appliedFilters.collectAsState()
    val filterDraft by viewModel.filterDraft.collectAsState()

    var showFilterBottomSheet by rememberSaveable { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    fun dismissFilterSheet() {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                showFilterBottomSheet = false
            }
        }
    }

    fun openFilterSheet() {
        viewModel.syncFilterDraftFromApplied()
        showFilterBottomSheet = true
    }

    if (showFilterBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterBottomSheet = false },
            sheetState = sheetState
        ) {
            EventFilterBottomSheet(
                filterState = filterDraft,
                categories = categories,
                onFilterStateChange = { update ->
                    viewModel.updateFilterDraft(update)
                },
                onClearFilters = {
                    viewModel.clearFilters()
                    dismissFilterSheet()
                },
                onApplyFilters = {
                    viewModel.applyDraftFilters()
                    dismissFilterSheet()
                }
            )
        }
    }

    Scaffold(
        topBar = {
            EventsTopSection(
                completedOrdersCount = completedOrdersCount,
                cartItemCount = cartItemCount,
                searchQuery = appliedFilters.query,
                onSearchQueryChange = { query ->
                    viewModel.updateSearchQuery(query)
                },
                onTicketsClick = onTicketsClick,
                onCartClick = onCartClick,
                onFilterClick = ::openFilterSheet
            )
        }
    ) { padding ->
        when (val state = uiState) {
            EventListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is EventListUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.events, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            onClick = { onEventClick(event.id) }
                        )
                    }
                }
            }

            EventListUiState.Empty,
            is EventListUiState.Error -> {
                EventUnavailableState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    message = when (state) {
                        EventListUiState.Empty -> "No events match your selected filters."
                        is EventListUiState.Error -> state.message
                        else -> ""
                    },
                    onRetry = viewModel::refreshEvents
                )
            }
        }
    }
}

@Composable
private fun EventUnavailableState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Try Again")
        }
    }
}