package com.coolnexttech.fireplayer.viewModel

import androidx.lifecycle.ViewModel
import com.coolnexttech.fireplayer.extensions.add
import com.coolnexttech.fireplayer.model.PlaylistViewMode
import com.coolnexttech.fireplayer.model.Playlists
import com.coolnexttech.fireplayer.util.UserStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class PlaylistsViewModel: ViewModel() {

    private val _playlistViewMode = MutableStateFlow(PlaylistViewMode.Add)
    val playlistViewMode: StateFlow<PlaylistViewMode> = _playlistViewMode

    private val _playlists = MutableStateFlow<Playlists>(hashMapOf())
    val playlists: StateFlow<Playlists> = _playlists

    private var storage: UserStorage? = null

    init {
        readPlaylists()
    }

    fun initPlaylistViewMode(playlistViewMode: PlaylistViewMode) {
        _playlistViewMode.update {
            playlistViewMode
        }
    }

    private fun readPlaylists() {
        _playlists.update {
            UserStorage.readPlaylists()
        }
    }

    fun addPlaylist(title: String) {
        _playlists.update {
            it.add(title)
            storage?.savePlaylists(it)
            it
        }
    }

    fun addTrackToPlaylist(trackTitle: String, playlistTitle: String) {
        _playlists.update {
            it[playlistTitle]?.add(trackTitle)
            storage?.savePlaylists(it)
            it
        }
    }

    fun removePlaylist(title: String) {
        _playlists.update {
            it.remove(title)
            storage?.savePlaylists(it)
            it
        }
    }
}