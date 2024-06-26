package com.coolnexttech.fireplayer.ui.home

import android.app.Activity.RESULT_OK
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.coolnexttech.fireplayer.R
import com.coolnexttech.fireplayer.db.TrackEntity
import com.coolnexttech.fireplayer.model.SortOptions
import com.coolnexttech.fireplayer.player.AudioPlayer
import com.coolnexttech.fireplayer.ui.components.dialog.AddPlaylistAlertDialog
import com.coolnexttech.fireplayer.ui.components.view.SeekbarView
import com.coolnexttech.fireplayer.ui.home.bottomSheet.TrackActionsBottomSheet
import com.coolnexttech.fireplayer.ui.home.dialog.AddTrackToPlaylistDialog
import com.coolnexttech.fireplayer.ui.home.dialog.LoadingDialog
import com.coolnexttech.fireplayer.ui.home.dialog.SleepTimerAlertDialog
import com.coolnexttech.fireplayer.ui.home.dialog.SortOptionsAlertDialog
import com.coolnexttech.fireplayer.ui.home.topbar.HomeTopBar
import com.coolnexttech.fireplayer.ui.home.trackList.EmptyTrackList
import com.coolnexttech.fireplayer.ui.home.trackList.TrackList
import com.coolnexttech.fireplayer.ui.playlists.PlaylistsViewModel
import com.coolnexttech.fireplayer.utils.FolderAnalyzer
import com.coolnexttech.fireplayer.utils.ToastManager
import com.coolnexttech.fireplayer.utils.extensions.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@ExperimentalFoundationApi
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    playlistsViewModel: PlaylistsViewModel,
    audioPlayer: AudioPlayer,
    navigateToPlaylists: () -> Unit
) {
    val context = LocalContext.current

    val filteredTracks by viewModel.filteredTracks.collectAsState()
    val selectedTrack by viewModel.selectedTrack.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val showLoadingDialog by viewModel.showLoadingDialog.collectAsState()
    val playlists by playlistsViewModel.playlists.collectAsState()
    val isPlaylistSelected by viewModel.isPlaylistSelected.collectAsState()

    val sortOptions = remember { mutableStateOf(SortOptions.AToZ) }
    val showSortOptions = remember { mutableStateOf(false) }

    val selectedTrackForTrackAction = remember { mutableStateOf<TrackEntity?>(null) }
    val showTrackActionsBottomSheet = remember { mutableStateOf(false) }

    val showAddPlaylist = remember { mutableStateOf(false) }
    val showSleepTimerAlertDialog = remember { mutableStateOf(false) }
    val showAddTrackToPlaylistDialog = remember { mutableStateOf(false) }
    val showTrackPositionAlertDialog = remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val characterList = remember(filteredTracks) {
        filteredTracks.groupBy { it.title.first().uppercaseChar() }
            .mapValues { (_, tracks) -> filteredTracks.indexOf(tracks.first()) }
    }

    val contentResolver = context.contentResolver
    val deleteResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {
            if (it.resultCode == RESULT_OK) {
                selectedTrackForTrackAction.value?.let { track ->
                    if (track == selectedTrack) {
                        viewModel.playNextTrack()
                    }

                    viewModel.deleteTrack(track)
                    viewModel.search(searchText)
                    ToastManager.showDeleteSuccessMessage()
                }

            }
        }
    )

    BackHandler {
        if (isPlaylistSelected) {
            viewModel.init()
        }
    }

    if (showLoadingDialog) {
        LoadingDialog {
            viewModel.hideLoadingDialog()
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                viewModel,
                filteredTracks,
                characterList,
                coroutineScope,
                sortOptions.value,
                searchText,
                listState,
                showSortOptions = { showSortOptions.value = true },
                showSleepTimerAlertDialog = { showSleepTimerAlertDialog.value = true },
                navigateToPlaylists = {
                    navigateToPlaylists()
                }
            )
        }, bottomBar = {
            selectedTrack?.let { track ->
                val currentTime by audioPlayer.currentTime.collectAsState()
                val totalTime by audioPlayer.totalTime.collectAsState()
                val isPlaying by audioPlayer.isPlaying.collectAsState()

                SeekbarView(
                    track,
                    audioPlayer.isTotalTimeValid(),
                    currentTime,
                    totalTime,
                    isPlaying,
                    seekTo = { position ->
                        audioPlayer.seekTo(position)
                    },
                    updateCurrentTime = { time ->
                        audioPlayer.updateCurrentTime(time)
                    },
                    toggle = {
                        audioPlayer.toggle()
                    },
                    seekBackward = {
                        audioPlayer.seekBackward()
                    },
                    seekForward = {
                        audioPlayer.seekForward()
                    },
                    showTrackPositionAlertDialog = {
                        showTrackPositionAlertDialog.value = true
                    },
                    playPreviousTrack = {
                        viewModel.playPreviousTrack()
                    },
                    playNextTrack = {
                        viewModel.playNextTrack()
                    }
                )
            }
        }) {
        if (filteredTracks.isEmpty()) {
            EmptyTrackList(searchText)
        } else {
            TrackList(
                listState,
                it,
                filteredTracks,
                selectedTrack,
                action = { (index, track) ->
                    coroutineScope.launch(Dispatchers.Main) {
                        listState.animateScrollToItem(index)
                    }

                    viewModel.playTrack(track)
                },
                longPressAction = { track ->
                    selectedTrackForTrackAction.value = track
                    showTrackActionsBottomSheet.value = true
                }
            )

            if (showTrackActionsBottomSheet.value) {
                TrackActionsBottomSheet(
                    audioPlayer,
                    selectedTrackForTrackAction.value?.title ?: "",
                    showDeleteTrackAlertDialog = {
                        selectedTrackForTrackAction.value?.let { track ->
                            FolderAnalyzer.deleteTrack(track, contentResolver, deleteResultLauncher)
                        }
                    },
                    dismiss = {
                        showTrackActionsBottomSheet.value = false
                    },
                    showAddTrackToPlaylistDialog = {
                        if (playlists.isNotEmpty()) {
                            showAddTrackToPlaylistDialog.value = true
                        } else {
                            ToastManager.showEmptyPlaybackMessage()
                        }
                    }
                )
            }

            if (showSortOptions.value) {
                SortOptionsAlertDialog(
                    dismiss = { showSortOptions.value = false },
                    sort = { selectedSortOption ->
                        viewModel.sort(selectedSortOption)
                        showTrackActionsBottomSheet.value = false
                        sortOptions.value = selectedSortOption
                    }
                )
            }

            if (showAddTrackToPlaylistDialog.value) {
                AddTrackToPlaylistDialog(
                    it,
                    createNewPlaylist = {
                        showAddPlaylist.value = true
                    },
                    addToPlaylist = { playlist ->
                        selectedTrackForTrackAction.value?.let { track ->
                            playlistsViewModel.addTrackToPlaylist(
                                track,
                                playlist.id
                            )

                            context.run {
                                showToast(
                                    getString(
                                        R.string.playlist_screen_add,
                                        track.titleRepresentation(),
                                        playlist.title
                                    )
                                )
                            }
                        }
                    },
                    dismiss = {
                        showTrackActionsBottomSheet.value = false
                        showAddTrackToPlaylistDialog.value = false
                    }
                )
            }

            if (showAddPlaylist.value) {
                AddPlaylistAlertDialog(playlistsViewModel) {
                    showAddPlaylist.value = false
                }
            }

            if (showSleepTimerAlertDialog.value) {
                SleepTimerAlertDialog(
                    pause = {
                        audioPlayer.pause()
                    },
                    dismiss = {
                        showTrackActionsBottomSheet.value = false
                        showSleepTimerAlertDialog.value = false
                    }
                )
            }
        }
    }
}
