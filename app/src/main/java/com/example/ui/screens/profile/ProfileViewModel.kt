package com.example.ui.screens.profile

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.di.AppContainer
import com.example.domain.model.GuestSession
import com.example.domain.model.UserProfile
import com.example.domain.provider.MusicCatalogProvider
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.ListeningHistoryRepository
import com.example.domain.repository.ProfileRepository
import com.example.ui.theme.MuseAmber
import com.example.ui.theme.MuseCoral
import com.example.ui.theme.MuseCyan
import com.example.ui.theme.MuseEmerald
import com.example.ui.theme.MuseIndigo
import com.example.ui.theme.MuseViolet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class GenreStat(
    val name: String,
    val percentage: Int,
    val fraction: Float,
    val color: Color
)

sealed interface SonicSignatureUiState {
    object Loading : SonicSignatureUiState
    object Empty : SonicSignatureUiState
    data class Success(val genres: List<GenreStat>, val totalTracksListened: Int) : SonicSignatureUiState
    data class Error(val message: String) : SonicSignatureUiState
}

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val guestSession: GuestSession? = null,
    val userEmail: String? = null,
    val userId: String = "",
    val isLoadingProfile: Boolean = true,
    val profileErrorMessage: String? = null,
    val sonicSignatureState: SonicSignatureUiState = SonicSignatureUiState.Loading
)

class ProfileViewModel(
    private val authRepository: AuthRepository = AppContainer.authRepository,
    private val profileRepository: ProfileRepository = AppContainer.profileRepository,
    private val listeningHistoryRepository: ListeningHistoryRepository = AppContainer.listeningHistoryRepository,
    private val musicCatalogProvider: MusicCatalogProvider = AppContainer.musicCatalogProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val genrePalette = listOf(
        MuseViolet,
        MuseIndigo,
        MuseCyan,
        MuseAmber,
        MuseCoral,
        MuseEmerald
    )

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val guest = authRepository.getGuestSession()
            val userId = authRepository.getCurrentSession() ?: ""
            val email = authRepository.getCurrentUserEmail()

            _uiState.value = _uiState.value.copy(
                guestSession = guest,
                userId = userId,
                userEmail = email,
                isLoadingProfile = userId.isNotEmpty(),
                sonicSignatureState = SonicSignatureUiState.Loading
            )

            // Load UserProfile if authenticated
            if (userId.isNotEmpty()) {
                try {
                    val profile = profileRepository.getProfile(userId)
                    _uiState.value = _uiState.value.copy(
                        userProfile = profile,
                        isLoadingProfile = false
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        profileErrorMessage = e.message,
                        isLoadingProfile = false
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoadingProfile = false)
            }

            // Load Sonic Signature
            loadSonicSignature()
        }
    }

    fun loadSonicSignature() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(sonicSignatureState = SonicSignatureUiState.Loading)
            try {
                val history = listeningHistoryRepository.getHistory()
                if (history.isEmpty()) {
                    _uiState.value = _uiState.value.copy(sonicSignatureState = SonicSignatureUiState.Empty)
                    return@launch
                }

                val genreCounts = mutableMapOf<String, Int>()
                for (entry in history) {
                    val track = musicCatalogProvider.getTrack(entry.trackId)
                    if (track != null && track.genres.isNotEmpty()) {
                        for (genre in track.genres) {
                            if (genre.isNotBlank()) {
                                genreCounts[genre] = (genreCounts[genre] ?: 0) + 1
                            }
                        }
                    }
                }

                if (genreCounts.isEmpty()) {
                    _uiState.value = _uiState.value.copy(sonicSignatureState = SonicSignatureUiState.Empty)
                    return@launch
                }

                val sortedGenres = genreCounts.entries.sortedByDescending { it.value }.take(4)
                val totalCount = sortedGenres.sumOf { it.value }

                if (totalCount == 0) {
                    _uiState.value = _uiState.value.copy(sonicSignatureState = SonicSignatureUiState.Empty)
                    return@launch
                }

                val genreStats = sortedGenres.mapIndexed { index, entry ->
                    val frac = entry.value.toFloat() / totalCount
                    val pct = (frac * 100f).roundToInt()
                    GenreStat(
                        name = entry.key,
                        percentage = pct,
                        fraction = frac,
                        color = genrePalette[index % genrePalette.size]
                    )
                }

                _uiState.value = _uiState.value.copy(
                    sonicSignatureState = SonicSignatureUiState.Success(
                        genres = genreStats,
                        totalTracksListened = history.size
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    sonicSignatureState = SonicSignatureUiState.Error(
                        message = e.message ?: "Failed to load listening history"
                    )
                )
            }
        }
    }

    fun updateProfile(updatedProfile: UserProfile, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                profileRepository.updateProfile(updatedProfile)
                _uiState.value = _uiState.value.copy(userProfile = updatedProfile)
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(profileErrorMessage = e.message)
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                onSignedOut()
            } catch (e: Exception) {
                println("ProfileViewModel: Error signing out: ${e.message}")
            }
        }
    }
}
