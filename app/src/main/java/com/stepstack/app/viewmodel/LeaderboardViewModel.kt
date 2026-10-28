package com.stepstack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepstack.app.data.DailyActivity
import com.stepstack.app.data.LeaderboardEntry
import com.stepstack.app.data.LeaderboardPeriod
import com.stepstack.app.data.LeaderboardRepository
import com.stepstack.app.data.LeaderboardScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeaderboardUiState(
    val period: LeaderboardPeriod = LeaderboardPeriod.TODAY,
    val scope: LeaderboardScope = LeaderboardScope.GLOBAL,
    val entries: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val me: LeaderboardEntry? get() = entries.firstOrNull { it.isMe }
}

class LeaderboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardUiState())
    val state: StateFlow<LeaderboardUiState> = _state.asStateFlow()

    private var history: List<DailyActivity> = emptyList()
    private var loadJob: Job? = null

    /** Publishes my latest numbers, then loads the board for the current period + scope. */
    fun refresh(latestHistory: List<DailyActivity> = history) {
        history = latestHistory
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Best effort: a failed publish shouldn't block viewing the board.
            LeaderboardRepository.publishMyStats(history)

            val s = _state.value
            val result = when (s.scope) {
                LeaderboardScope.GLOBAL -> LeaderboardRepository.fetchGlobal(s.period)
                LeaderboardScope.FRIENDS -> LeaderboardRepository.fetchFriends(s.period)
            }
            result
                .onSuccess { list ->
                    _state.update { it.copy(entries = list, isLoading = false, error = null) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(isLoading = false, error = e.message ?: "Couldn't load the leaderboard")
                    }
                }
        }
    }

    fun setPeriod(period: LeaderboardPeriod) {
        if (period == _state.value.period) return
        _state.update { it.copy(period = period, entries = emptyList()) }
        refresh()
    }

    fun setScope(scope: LeaderboardScope) {
        if (scope == _state.value.scope) return
        _state.update { it.copy(scope = scope, entries = emptyList()) }
        refresh()
    }

    /** My own value for the selected period, from local history (works even if I'm off the board). */
    fun myLocalValue(history: List<DailyActivity>): Int =
        LeaderboardRepository.myValue(_state.value.period, history)
}
