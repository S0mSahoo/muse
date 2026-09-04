package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.di.AppContainer
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.DailyMix
import com.example.domain.model.Playlist
import com.example.domain.model.Track
import com.example.domain.model.User
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.MusicRepository
import com.example.domain.repository.PlaybackRepository
import com.example.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val user: User = User(),
    val greeting: String = "Good evening",
    val subtitle: String = "Here's something you'll love.",
    val selectedFilter: String = "all",
    val featuredPlaylist: Playlist? = null,
    val dailyMixes: List<DailyMix> = emptyList(),
    val madeForYouPlaylists: List<Playlist> = emptyList(),
    val recentlyPlayedTracks: List<Track> = emptyList(),
    val freshDiscoveries: List<Track> = emptyList(),
    val recommendedArtists: List<Artist> = emptyList(),
    val recommendedAlbums: List<Album> = emptyList(),
    val becauseYouListenedArtist: Artist? = null,
    val becauseYouListenedTracks: List<Track> = emptyList(),
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val musicRepository: MusicRepository = AppContainer.musicRepository,
    private val playbackRepository: PlaybackRepository = AppContainer.playbackRepository,
    private val authRepository: AuthRepository = AppContainer.authRepository,
    private val profileRepository: ProfileRepository = AppContainer.profileRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("all")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val greeting = calculateGreeting()

    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = authRepository.getCurrentSession()
            if (userId != null) {
                val profile = profileRepository.getProfile(userId)
                if (profile != null) {
                    _user.value = User(
                        id = profile.id,
                        name = profile.displayName ?: "",
                        handle = profile.username ?: "",
                        avatarUrl = profile.avatarUrl ?: ""
                    )
                }
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        _user,
        musicRepository.getFeaturedPlaylist(),
        musicRepository.getDailyMixes(),
        musicRepository.getMadeForYouPlaylists(),
        musicRepository.getRecentlyPlayedTracks(),
        musicRepository.getFreshDiscoveries(),
        musicRepository.getRecommendedArtists(),
        musicRepository.getRecommendedAlbums(),
        musicRepository.getBecauseYouListenedTo(),
        _selectedFilter
    ) { params ->
        val user = params[0] as User
        val featured = params[1] as Playlist
        @Suppress("UNCHECKED_CAST")
        val dailyMixes = params[2] as List<DailyMix>
        @Suppress("UNCHECKED_CAST")
        val madeForYou = params[3] as List<Playlist>
        @Suppress("UNCHECKED_CAST")
        val recentTracks = params[4] as List<Track>
        @Suppress("UNCHECKED_CAST")
        val freshTracks = params[5] as List<Track>
        @Suppress("UNCHECKED_CAST")
        val artists = params[6] as List<Artist>
        @Suppress("UNCHECKED_CAST")
        val albums = params[7] as List<Album>
        @Suppress("UNCHECKED_CAST")
        val becausePair = params[8] as Pair<Artist, List<Track>>
        val filter = params[9] as String

        val personalizedGreeting = if (user.name.isNotEmpty()) "$greeting, ${user.name}" else greeting
        val personalizedSubtitle = when (greeting) {
            "Good morning" -> "Energize your morning focus."
            "Good afternoon" -> "Your afternoon soundscape is ready."
            else -> "Here's something you'll love."
        }

        HomeUiState(
            user = user,
            greeting = personalizedGreeting,
            subtitle = personalizedSubtitle,
            selectedFilter = filter,
            featuredPlaylist = featured,
            dailyMixes = dailyMixes,
            madeForYouPlaylists = madeForYou,
            recentlyPlayedTracks = recentTracks,
            freshDiscoveries = freshTracks,
            recommendedArtists = artists,
            recommendedAlbums = albums,
            becauseYouListenedArtist = becausePair.first,
            becauseYouListenedTracks = becausePair.second,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(greeting = calculateGreeting())
    )

    fun onFilterSelected(filterId: String) {
        _selectedFilter.value = if (_selectedFilter.value == filterId) "all" else filterId
    }

    fun playTrack(track: Track, queue: List<Track> = emptyList()) {
        val effectiveQueue = if (queue.isNotEmpty()) queue else listOf(track)
        playbackRepository.playTrack(track, effectiveQueue)
    }

    fun playPlaylist(playlist: Playlist) {
        playbackRepository.playPlaylist(playlist)
    }

    fun playDailyMix(dailyMix: DailyMix) {
        val playlist = Playlist(
            id = dailyMix.id,
            title = dailyMix.title,
            description = dailyMix.description,
            artworkUrl = dailyMix.artworkUrl,
            curator = "MUSE Daily",
            tracks = dailyMix.tracks
        )
        playbackRepository.playPlaylist(playlist)
    }

    fun toggleLike(trackId: String) {
        viewModelScope.launch {
            musicRepository.toggleTrackLike(trackId)
            playbackRepository.toggleLike(trackId)
        }
    }

    companion object {
        fun calculateGreeting(): String {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when (hour) {
                in 5..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                else -> "Good evening"
            }
        }
    }
}
