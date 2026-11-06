/*
 * Moonlight Launcher — unofficial modified version of Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 * Modifications Copyright (C) 2026 frostkl4
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.moonlight.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.wardrobe.OfflineCapeException
import com.movtery.zalithlauncher.game.account.wardrobe.OfflineCapeSource
import com.movtery.zalithlauncher.game.account.wardrobe.fetchElyByCape
import com.movtery.zalithlauncher.game.account.wardrobe.fetchMinecraftCapesCape
import com.movtery.zalithlauncher.game.account.wardrobe.normalizeCapeImage
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Pending cape change of an offline account, applied when the skin dialog is confirmed. */
sealed interface OfflineCapeChange {
    data object None : OfflineCapeChange
    data object Remove : OfflineCapeChange
    class Set(val bytes: ByteArray) : OfflineCapeChange
}

/**
 * Lets an offline (non-premium) account pick a cape: from a PNG file, from Ely.by
 * or from MinecraftCapes. The result is only a pending change; the skin dialog saves it.
 */
@Composable
fun OfflineCapeDialog(
    account: Account,
    hasCape: Boolean,
    onResult: (OfflineCapeChange) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf(account.username) }
    var loading by remember { mutableStateOf<OfflineCapeSource?>(null) }
    var error by remember { mutableStateOf<Int?>(null) }

    fun load(source: OfflineCapeSource, block: suspend () -> ByteArray) {
        if (loading != null) return
        loading = source
        error = null
        scope.launch {
            try {
                val bytes = block()
                onResult(OfflineCapeChange.Set(bytes))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.warning("OfflineCapeDialog", "Could not get cape from $source", e)
                error = when ((e as? OfflineCapeException)?.reason) {
                    OfflineCapeException.Reason.NO_CAPE -> R.string.moonlight_offline_cape_error_no_cape
                    OfflineCapeException.Reason.PLAYER_NOT_FOUND -> R.string.moonlight_offline_cape_error_not_found
                    OfflineCapeException.Reason.INVALID_IMAGE -> R.string.moonlight_offline_cape_error_invalid
                    else -> R.string.moonlight_offline_cape_error_network
                }
            } finally {
                loading = null
            }
        }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            load(OfflineCapeSource.FILE) {
                withContext(Dispatchers.IO) {
                    val raw = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw OfflineCapeException(OfflineCapeException.Reason.INVALID_IMAGE, "Empty file")
                    normalizeCapeImage(raw)
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.moonlight_offline_cape_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.moonlight_offline_cape_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SourceButton(
                    text = stringResource(R.string.moonlight_offline_cape_pick_file),
                    loading = loading == OfflineCapeSource.FILE,
                    enabled = loading == null,
                    onClick = { filePicker.launch(arrayOf("image/png")) }
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = username,
                    onValueChange = { username = it.trim() },
                    singleLine = true,
                    label = { Text(text = stringResource(R.string.moonlight_offline_cape_username)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SourceButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.moonlight_offline_cape_from_elyby),
                        loading = loading == OfflineCapeSource.ELY_BY,
                        enabled = loading == null && username.isNotBlank(),
                        onClick = { load(OfflineCapeSource.ELY_BY) { fetchElyByCape(username) } }
                    )
                    SourceButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.moonlight_offline_cape_from_minecraftcapes),
                        loading = loading == OfflineCapeSource.MINECRAFT_CAPES,
                        enabled = loading == null && username.isNotBlank(),
                        onClick = { load(OfflineCapeSource.MINECRAFT_CAPES) { fetchMinecraftCapesCape(username) } }
                    )
                }

                error?.let { res ->
                    Text(
                        text = stringResource(res),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (hasCape) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = loading == null,
                        onClick = { onResult(OfflineCapeChange.Remove) }
                    ) {
                        Text(text = stringResource(R.string.moonlight_offline_cape_remove))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.generic_cancel))
            }
        }
    )
}

@Composable
private fun SourceButton(
    text: String,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    FilledTonalButton(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, maxLines = 1)
        }
    }
}
