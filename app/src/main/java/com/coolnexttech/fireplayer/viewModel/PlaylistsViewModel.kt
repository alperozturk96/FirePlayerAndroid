package com.coolnexttech.fireplayer.viewModel

import android.content.Context
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

    fun initUserStorage(context: Context) {
        storage = UserStorage(context)
        readPlaylists()
    }

    fun initPlaylistViewMode(value: PlaylistViewMode) {
        _playlistViewMode.update {
            value
        }
    }

    private fun readPlaylists() {
        _playlists.update {
            storage?.readPlaylists() ?: hashMapOf()
        }
    }

    fun addPlaylist(title: String) {
        _playlists.update {
            it.add(title)
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