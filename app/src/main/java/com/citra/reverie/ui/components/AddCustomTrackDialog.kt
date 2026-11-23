package com.citra.reverie.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AddCustomTrackDialog
 * Dialog memilih file MP3 dari perangkat atau memasukkan link audio/YouTube
 */
@Composable
fun AddCustomTrackDialog(
    onDismiss: () -> Unit,
    onAddTrack: (title: String, artist: String, coverUrl: String, songUrl: String) -> Unit,
    onAddLocalTrack: (title: String, artist: String, coverUrl: String, fileUri: String) -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    var artistInput by remember { mutableStateOf("") }
    var coverUrlInput by remember { mutableStateOf("") }
    var songUrlInput by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Launcher untuk memilih file MP3 dari HP
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            val name = it.lastPathSegment?.substringAfterLast("/")?.substringBeforeLast(".") ?: "Lagu Lokal"
            selectedFileName = name
            if (titleInput.isBlank()) titleInput = name
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tambah Lagu Sendiri",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Tombol Pilih MP3 dari HP
                OutlinedButton(
                    onClick = { audioPickerLauncher.launch("audio/*") },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedFileName.isNotBlank()) "Terpilih: $selectedFileName" else "Pilih File MP3 dari HP",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "atau isi manual di bawah ini:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it; errorMessage = "" },
                    label = { Text("Judul Lagu") },
                    leadingIcon = { Icon(Icons.Filled.MusicNote, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = artistInput,
                    onValueChange = { artistInput = it; errorMessage = "" },
                    label = { Text("Nama Artis") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = coverUrlInput,
                    onValueChange = { coverUrlInput = it },
                    label = { Text("URL Gambar Cover (opsional)") },
                    leadingIcon = { Icon(Icons.Filled.Image, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (selectedFileUri == null) {
                    OutlinedTextField(
                        value = songUrlInput,
                        onValueChange = { songUrlInput = it; errorMessage = "" },
                        label = { Text("Link YouTube / Stream URL") },
                        leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
                        singleLine = true,
                        placeholder = { Text("https://www.youtube.com/watch?v=...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedFileUri != null) {
                        onAddLocalTrack(
                            titleInput.ifBlank { selectedFileName },
                            artistInput.ifBlank { "Perangkat Saya" },
                            coverUrlInput.trim(),
                            selectedFileUri.toString()
                        )
                    } else {
                        if (titleInput.isBlank() || artistInput.isBlank() || songUrlInput.isBlank()) {
                            errorMessage = "Pilih file MP3 atau isi Judul, Artis, & Link"
                            return@Button
                        }
                        onAddTrack(
                            titleInput.trim(),
                            artistInput.trim(),
                            coverUrlInput.trim(),
                            songUrlInput.trim()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Tambah Lagu", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
