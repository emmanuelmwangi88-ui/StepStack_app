package com.stepstack.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepstack.app.data.ChallengeRepository
import com.stepstack.app.data.ChallengeSnapshot
import com.stepstack.app.data.DailyActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChallengeUiState(
    val snapshot: ChallengeSnapshot? = null,
    val isLoading: Boolean = true,
    val isBusy: Boolean = false,
    val error: String? = null,
    val deleted: Boolean = false
)

class ChallengeViewModel : ViewModel() {

    private val _state = MutableStateFlow(ChallengeUiState())
    val state: StateFlow<ChallengeUiState> = _state.asStateFlow()

    /**
     * Loads the challenge. If I've accepted, my progress is computed from [history] and
     * published first, then the board is reloaded so it includes my fresh numbers.
     */
    fun load(challengeId: String, history: List<DailyActivity>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = it.snapshot == null, error = null) }

            var result = ChallengeRepository.load(challengeId)
            val first = result.getOrNull()
            if (first != null && first.iAmJoined) {
                ChallengeRepository.publishMyProgress(first.challenge, history)
                result = ChallengeRepository.load(challengeId)
            }

            result
                .onSuccess { snap ->
                    _state.update { it.copy(snapshot = snap, isLoading = false, error = null) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(isLoading = false, error = e.message ?: "Couldn't load this challenge")
                    }
                }
        }
    }

    fun join(history: List<DailyActivity>) {
        val snap = _state.value.snapshot ?: return
        runAction(snap, history) { ChallengeRepository.join(snap.challenge, wasInvited = snap.iAmInvited) }
    }

    fun decline(history: List<DailyActivity>) {
        val snap = _state.value.snapshot ?: return
        runAction(snap, history) { ChallengeRepository.decline(snap.challenge.id) }
    }

    fun delete() {
        val snap = _state.value.snapshot ?: return
        if (!snap.isCreator) return
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, error = null) }
            ChallengeRepository.delete(snap.challenge.id)
                .onSuccess { _state.update { it.copy(isBusy = false, deleted = true) } }
                .onFailure { e ->
                    _state.update { it.copy(isBusy = false, error = e.message ?: "Couldn't delete the challenge") }
                }
        }
    }

    fun dismissError() = _state.update { it.copy(error = null) }

    private fun runAction(
        snap: ChallengeSnapshot,
        history: List<DailyActivity>,
        action: suspend () -> Result<Unit>
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, error = null) }
            action()
                .onSuccess {
                    _state.update { it.copy(isBusy = false) }
                    load(snap.challenge.id, history)
                }
                .onFailure { e ->
                    _state.update { it.copy(isBusy = false, error = e.message ?: "Something went wrong") }
                }
        }
    }
}
