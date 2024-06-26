package com.coolnexttech.fireplayer.ui.home.topbar

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import com.coolnexttech.fireplayer.R
import com.coolnexttech.fireplayer.ui.components.BodyMediumText
import com.coolnexttech.fireplayer.ui.components.button.ActionIconButton
import com.coolnexttech.fireplayer.ui.home.HomeViewModel
import com.coolnexttech.fireplayer.ui.theme.AppColors
import com.coolnexttech.fireplayer.utils.extensions.getTopAppBarColor
import com.coolnexttech.fireplayer.utils.extensions.showToast
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBarOptions(
    context: Context,
    alphabeticalScrollerIconId: Int,
    searchPlaceholderId: Int,
    searchText: String,
    viewModel: HomeViewModel,
    toggleAlphabeticalScroller: () -> Unit,
    navigateToPlaylists: () -> Unit
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue(searchText)) }
    var showPlaceholder by remember { mutableStateOf(searchQuery.text.isEmpty()) }
    var debounceJob: Job? by remember { mutableStateOf(null) }
    var decorationBoxJob: Job? by remember { mutableStateOf(null) }
    val coroutineScope = rememberCoroutineScope()

    TopAppBar(
        colors = getTopAppBarColor(),
        title = {
            BasicTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    debounceJob?.cancel()
                    debounceJob = coroutineScope.launch {
                        delay(300)
                        viewModel.search(it.text)
                        showPlaceholder = it.text.isEmpty()
                    }

                    decorationBoxJob?.cancel()
                    decorationBoxJob = coroutineScope.launch {
                        showPlaceholder = it.text.isEmpty()
                    }
                },
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (showPlaceholder) {
                        BodyMediumText(id = searchPlaceholderId)
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .height(intrinsicSize = IntrinsicSize.Max)
                    .fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(AppColors.unhighlight),
                cursorBrush = SolidColor(AppColors.unhighlight)
            )
        },
        actions = {
            AnimatedVisibility(searchText.isNotEmpty()) {
                ActionIconButton(R.drawable.ic_cancel) {
                    viewModel.clearSearch()
                }
            }

            ActionIconButton(R.drawable.ic_reset) {
                viewModel.update()
                context.showToast(R.string.home_screen_update_button_description)
            }

            ActionIconButton(R.drawable.ic_playlists) {
                navigateToPlaylists()
            }

            ActionIconButton(alphabeticalScrollerIconId) {
                toggleAlphabeticalScroller()
            }
        }
    )
}
