package com.citra.reverie.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    INDONESIAN("id", "Indonesia", "ID"),
    ENGLISH("en", "English", "EN")
}

object LanguageManager {
    var currentLanguage by mutableStateOf(AppLanguage.INDONESIAN)

    fun toggleLanguage() {
        currentLanguage = if (currentLanguage == AppLanguage.INDONESIAN) AppLanguage.ENGLISH else AppLanguage.INDONESIAN
    }

    fun setLanguage(lang: AppLanguage) {
        currentLanguage = lang
    }
}

object AppStrings {
    private val translations = mapOf(
        // Navigation
        "nav_home" to mapOf(AppLanguage.INDONESIAN to "Beranda", AppLanguage.ENGLISH to "Home"),
        "nav_search" to mapOf(AppLanguage.INDONESIAN to "Cari", AppLanguage.ENGLISH to "Search"),
        "nav_library" to mapOf(AppLanguage.INDONESIAN to "Koleksi", AppLanguage.ENGLISH to "Library"),
        "nav_profile" to mapOf(AppLanguage.INDONESIAN to "Akun", AppLanguage.ENGLISH to "Account"),

        // Greetings
        "greeting_morning" to mapOf(AppLanguage.INDONESIAN to "Selamat Pagi", AppLanguage.ENGLISH to "Good Morning"),
        "greeting_afternoon" to mapOf(AppLanguage.INDONESIAN to "Selamat Siang", AppLanguage.ENGLISH to "Good Afternoon"),
        "greeting_evening" to mapOf(AppLanguage.INDONESIAN to "Selamat Sore", AppLanguage.ENGLISH to "Good Evening"),
        "greeting_night" to mapOf(AppLanguage.INDONESIAN to "Selamat Malam", AppLanguage.ENGLISH to "Good Night"),
        "app_tagline" to mapOf(AppLanguage.INDONESIAN to "Musik Pilihan Tanpa Batas", AppLanguage.ENGLISH to "Unlimited Music Experience"),
        "loading_api" to mapOf(AppLanguage.INDONESIAN to "Memuat lagu terpopuler...", AppLanguage.ENGLISH to "Loading top songs..."),

        // Filter Genres
        "genre_all" to mapOf(AppLanguage.INDONESIAN to "Semua", AppLanguage.ENGLISH to "All"),
        "genre_rock" to mapOf(AppLanguage.INDONESIAN to "Rock", AppLanguage.ENGLISH to "Rock"),
        "genre_pop" to mapOf(AppLanguage.INDONESIAN to "Pop", AppLanguage.ENGLISH to "Pop"),
        "genre_jazz" to mapOf(AppLanguage.INDONESIAN to "Jazz", AppLanguage.ENGLISH to "Jazz"),
        "genre_indie" to mapOf(AppLanguage.INDONESIAN to "Indie", AppLanguage.ENGLISH to "Indie"),
        "genre_electronic" to mapOf(AppLanguage.INDONESIAN to "Elektronik", AppLanguage.ENGLISH to "Electronic"),
        "genre_rnb" to mapOf(AppLanguage.INDONESIAN to "R&B", AppLanguage.ENGLISH to "R&B"),

        // Sections
        "section_playlists" to mapOf(AppLanguage.INDONESIAN to "Playlist Reverie", AppLanguage.ENGLISH to "Reverie Playlists"),
        "section_popular_artists" to mapOf(AppLanguage.INDONESIAN to "Artis Populer", AppLanguage.ENGLISH to "Popular Artists"),
        "section_top_songs" to mapOf(AppLanguage.INDONESIAN to "Lagu Pilihan Terpopuler", AppLanguage.ENGLISH to "Top Featured Songs"),
        "songs_count" to mapOf(AppLanguage.INDONESIAN to "lagu", AppLanguage.ENGLISH to "songs"),
        "no_songs_genre" to mapOf(AppLanguage.INDONESIAN to "Tidak ada lagu untuk genre ini", AppLanguage.ENGLISH to "No songs found for this genre"),

        // Now Playing & Lyrics
        "now_playing" to mapOf(AppLanguage.INDONESIAN to "SEDANG DIPUTAR", AppLanguage.ENGLISH to "NOW PLAYING"),
        "queue" to mapOf(AppLanguage.INDONESIAN to "ANTREAN", AppLanguage.ENGLISH to "QUEUE"),
        "lyrics" to mapOf(AppLanguage.INDONESIAN to "LIRIK", AppLanguage.ENGLISH to "LYRICS"),
        "lyrics_synced" to mapOf(AppLanguage.INDONESIAN to "Lirik Sinkron Otomatis (Ketuk baris untuk lompat)", AppLanguage.ENGLISH to "Synced Lyrics (Tap line to seek)"),
        "no_lyrics" to mapOf(AppLanguage.INDONESIAN to "Lirik belum tersedia untuk lagu ini", AppLanguage.ENGLISH to "Lyrics not available for this song"),
        "play" to mapOf(AppLanguage.INDONESIAN to "Putar", AppLanguage.ENGLISH to "Play"),
        "pause" to mapOf(AppLanguage.INDONESIAN to "Jeda", AppLanguage.ENGLISH to "Pause"),
        "next" to mapOf(AppLanguage.INDONESIAN to "Berikutnya", AppLanguage.ENGLISH to "Next"),
        "previous" to mapOf(AppLanguage.INDONESIAN to "Sebelumnya", AppLanguage.ENGLISH to "Previous"),
        "shuffle" to mapOf(AppLanguage.INDONESIAN to "Acak", AppLanguage.ENGLISH to "Shuffle"),
        "repeat" to mapOf(AppLanguage.INDONESIAN to "Ulangi", AppLanguage.ENGLISH to "Repeat"),
        "pip_mode" to mapOf(AppLanguage.INDONESIAN to "Mode Melayang (PiP)", AppLanguage.ENGLISH to "Floating Mode (PiP)"),

        // Library & Playlists
        "my_library" to mapOf(AppLanguage.INDONESIAN to "Koleksi Musik Anda", AppLanguage.ENGLISH to "Your Music Library"),
        "tab_favorites" to mapOf(AppLanguage.INDONESIAN to "Favorit", AppLanguage.ENGLISH to "Favorites"),
        "tab_my_songs" to mapOf(AppLanguage.INDONESIAN to "Lagu Saya", AppLanguage.ENGLISH to "My Songs"),
        "tab_history" to mapOf(AppLanguage.INDONESIAN to "Riwayat", AppLanguage.ENGLISH to "History"),
        "tab_custom_playlists" to mapOf(AppLanguage.INDONESIAN to "Playlist Saya", AppLanguage.ENGLISH to "My Playlists"),
        "play_all" to mapOf(AppLanguage.INDONESIAN to "Putar Semua", AppLanguage.ENGLISH to "Play All"),
        "empty_favorites" to mapOf(AppLanguage.INDONESIAN to "Belum ada lagu favorit tersimpan", AppLanguage.ENGLISH to "No favorite songs saved yet"),
        "empty_my_songs" to mapOf(AppLanguage.INDONESIAN to "Belum ada lagu ditambahkan. Tekan '+ Lagu' untuk menambahkan dari HP Anda!", AppLanguage.ENGLISH to "No custom songs added yet. Tap '+ Song' to add music from your phone!"),
        "empty_history" to mapOf(AppLanguage.INDONESIAN to "Belum ada riwayat lagu yang diputar", AppLanguage.ENGLISH to "No playback history yet"),
        "empty_playlists" to mapOf(AppLanguage.INDONESIAN to "Belum ada playlist buatanmu. Yuk buat sekarang!", AppLanguage.ENGLISH to "No playlists yet. Create your first playlist!"),
        "btn_create_playlist" to mapOf(AppLanguage.INDONESIAN to "+ Buat Playlist Baru", AppLanguage.ENGLISH to "+ Create New Playlist"),
        "dialog_playlist_title" to mapOf(AppLanguage.INDONESIAN to "Buat Playlist Baru", AppLanguage.ENGLISH to "Create New Playlist"),
        "dialog_playlist_name" to mapOf(AppLanguage.INDONESIAN to "Nama Playlist", AppLanguage.ENGLISH to "Playlist Name"),
        "dialog_playlist_desc" to mapOf(AppLanguage.INDONESIAN to "Deskripsi (opsional)", AppLanguage.ENGLISH to "Description (optional)"),
        "btn_cancel" to mapOf(AppLanguage.INDONESIAN to "Batal", AppLanguage.ENGLISH to "Cancel"),
        "btn_save" to mapOf(AppLanguage.INDONESIAN to "Simpan", AppLanguage.ENGLISH to "Save"),
        "btn_shuffle" to mapOf(AppLanguage.INDONESIAN to "Acak", AppLanguage.ENGLISH to "Shuffle"),
        "add_to_playlist" to mapOf(AppLanguage.INDONESIAN to "Tambah ke Playlist", AppLanguage.ENGLISH to "Add to Playlist"),
        "delete_playlist" to mapOf(AppLanguage.INDONESIAN to "Hapus Playlist", AppLanguage.ENGLISH to "Delete Playlist"),
        "delete_playlist_confirm" to mapOf(AppLanguage.INDONESIAN to "Apakah Anda yakin ingin menghapus playlist ini?", AppLanguage.ENGLISH to "Are you sure you want to delete this playlist?"),
        "clear_history" to mapOf(AppLanguage.INDONESIAN to "Hapus Riwayat", AppLanguage.ENGLISH to "Clear History"),
        "back" to mapOf(AppLanguage.INDONESIAN to "Kembali", AppLanguage.ENGLISH to "Back"),
        "delete_confirm" to mapOf(AppLanguage.INDONESIAN to "Hapus", AppLanguage.ENGLISH to "Delete"),

        // Login & Profile
        "account_title" to mapOf(AppLanguage.INDONESIAN to "Akun & Pengaturan", AppLanguage.ENGLISH to "Account & Settings"),
        "login_title" to mapOf(AppLanguage.INDONESIAN to "Masuk ke Reverie", AppLanguage.ENGLISH to "Login to Reverie"),
        "register_title" to mapOf(AppLanguage.INDONESIAN to "Daftar Akun Baru", AppLanguage.ENGLISH to "Register New Account"),
        "username_label" to mapOf(AppLanguage.INDONESIAN to "Nama Pengguna", AppLanguage.ENGLISH to "Username"),
        "email_label" to mapOf(AppLanguage.INDONESIAN to "Alamat Email", AppLanguage.ENGLISH to "Email Address"),
        "password_label" to mapOf(AppLanguage.INDONESIAN to "Kata Sandi", AppLanguage.ENGLISH to "Password"),
        "btn_login" to mapOf(AppLanguage.INDONESIAN to "Masuk", AppLanguage.ENGLISH to "Login"),
        "btn_register" to mapOf(AppLanguage.INDONESIAN to "Daftar Akun", AppLanguage.ENGLISH to "Create Account"),
        "btn_logout" to mapOf(AppLanguage.INDONESIAN to "Keluar Akun", AppLanguage.ENGLISH to "Logout"),
        "already_have_account" to mapOf(AppLanguage.INDONESIAN to "Sudah punya akun? Masuk di sini", AppLanguage.ENGLISH to "Already have an account? Login here"),
        "dont_have_account" to mapOf(AppLanguage.INDONESIAN to "Belum punya akun? Daftar di sini", AppLanguage.ENGLISH to "Don't have an account? Register here"),
        "membership_status" to mapOf(AppLanguage.INDONESIAN to "Status: Member Premium Reverie", AppLanguage.ENGLISH to "Status: Reverie Premium Member"),
        "playlists_created" to mapOf(AppLanguage.INDONESIAN to "Playlist Dibuat", AppLanguage.ENGLISH to "Playlists Created"),
        "songs_liked" to mapOf(AppLanguage.INDONESIAN to "Lagu Disukai", AppLanguage.ENGLISH to "Liked Songs"),
        "edit_profile" to mapOf(AppLanguage.INDONESIAN to "Edit Profil", AppLanguage.ENGLISH to "Edit Profile"),
        "save_profile" to mapOf(AppLanguage.INDONESIAN to "Simpan Profil", AppLanguage.ENGLISH to "Save Profile"),
        "google_signin" to mapOf(AppLanguage.INDONESIAN to "Masuk dengan Google", AppLanguage.ENGLISH to "Sign in with Google"),

        // Theme & Language Settings
        "theme_settings" to mapOf(AppLanguage.INDONESIAN to "Pengaturan Tampilan & Bahasa", AppLanguage.ENGLISH to "Display & Language Settings"),
        "switch_to_light" to mapOf(AppLanguage.INDONESIAN to "Ganti ke Mode Terang (Light Mode)", AppLanguage.ENGLISH to "Switch to Light Mode"),
        "switch_to_dark" to mapOf(AppLanguage.INDONESIAN to "Ganti ke Mode Gelap (Dark Mode)", AppLanguage.ENGLISH to "Switch to Dark Mode"),
        "language_setting" to mapOf(AppLanguage.INDONESIAN to "Pilihan Bahasa", AppLanguage.ENGLISH to "Language Selection"),
        "service_status" to mapOf(AppLanguage.INDONESIAN to "Status Layanan & Fitur", AppLanguage.ENGLISH to "Service Status & Features"),
        "bg_playback_status" to mapOf(AppLanguage.INDONESIAN to "Pemutaran Latar Belakang (Layar Mati / Di atas Aplikasi Lain)", AppLanguage.ENGLISH to "Background Playback (Screen Off / Picture-in-Picture)"),
        "bg_playback_desc" to mapOf(AppLanguage.INDONESIAN to "Aktif via Foreground MediaSession Service", AppLanguage.ENGLISH to "Active via Foreground MediaSession Service"),

        // Search & Add Song
        "search_title" to mapOf(AppLanguage.INDONESIAN to "Cari Musik", AppLanguage.ENGLISH to "Search Music"),
        "search_placeholder" to mapOf(AppLanguage.INDONESIAN to "Lagu, artis, atau genre...", AppLanguage.ENGLISH to "Songs, artists, or genres..."),
        "search_categories" to mapOf(AppLanguage.INDONESIAN to "Jelajahi Genre & Kategori", AppLanguage.ENGLISH to "Explore Genres & Categories"),
        "search_results" to mapOf(AppLanguage.INDONESIAN to "Hasil Pencarian", AppLanguage.ENGLISH to "Search Results"),
        "searching_songs" to mapOf(AppLanguage.INDONESIAN to "Mencari lagu...", AppLanguage.ENGLISH to "Searching songs..."),
        "no_results" to mapOf(AppLanguage.INDONESIAN to "Tidak ada lagu ditemukan", AppLanguage.ENGLISH to "No songs found"),
        "search_action" to mapOf(AppLanguage.INDONESIAN to "Cari", AppLanguage.ENGLISH to "Search"),
        "add_custom_track" to mapOf(AppLanguage.INDONESIAN to "Tambah Lagu Sendiri", AppLanguage.ENGLISH to "Add Your Own Song"),
        "add_track_btn" to mapOf(AppLanguage.INDONESIAN to "+ Lagu", AppLanguage.ENGLISH to "+ Song"),
        "add_song_device" to mapOf(AppLanguage.INDONESIAN to "Tambah Lagu dari HP", AppLanguage.ENGLISH to "Add Song from Device"),
        "choose_mp3_file" to mapOf(AppLanguage.INDONESIAN to "Pilih File MP3 dari HP", AppLanguage.ENGLISH to "Choose MP3 File from Device"),
        "selected_prefix" to mapOf(AppLanguage.INDONESIAN to "Terpilih:", AppLanguage.ENGLISH to "Selected:"),
        "song_title_label" to mapOf(AppLanguage.INDONESIAN to "Judul Lagu", AppLanguage.ENGLISH to "Song Title"),
        "artist_name_label" to mapOf(AppLanguage.INDONESIAN to "Nama Artis", AppLanguage.ENGLISH to "Artist Name"),
        "cover_url_label" to mapOf(AppLanguage.INDONESIAN to "URL Gambar Cover (opsional)", AppLanguage.ENGLISH to "Cover Image URL (optional)"),
        "error_pick_file" to mapOf(AppLanguage.INDONESIAN to "Pilih file MP3 dari HP Anda terlebih dahulu", AppLanguage.ENGLISH to "Please choose an MP3 file from your device first"),
        "btn_add_song_confirm" to mapOf(AppLanguage.INDONESIAN to "Tambah Lagu", AppLanguage.ENGLISH to "Add Song"),
        "btn_add_song_short" to mapOf(AppLanguage.INDONESIAN to "Lagu", AppLanguage.ENGLISH to "Song"),
        "btn_add_playlist_short" to mapOf(AppLanguage.INDONESIAN to "Playlist", AppLanguage.ENGLISH to "Playlist"),
        "close" to mapOf(AppLanguage.INDONESIAN to "Tutup", AppLanguage.ENGLISH to "Close"),
        "no_playlists_notice" to mapOf(AppLanguage.INDONESIAN to "Belum ada playlist. Buat playlist baru terlebih dahulu di menu Koleksi.", AppLanguage.ENGLISH to "No playlists yet. Create a new playlist first in Library."),
        "all_fields_required" to mapOf(AppLanguage.INDONESIAN to "Semua kolom wajib diisi", AppLanguage.ENGLISH to "All fields are required"),
        "divider_or" to mapOf(AppLanguage.INDONESIAN to "  atau  ", AppLanguage.ENGLISH to "  or  "),
        "reverie_collection_subtitle" to mapOf(AppLanguage.INDONESIAN to "Koleksi Musik Pribadi", AppLanguage.ENGLISH to "Personal Music Collection"),
        "btn_add_music" to mapOf(AppLanguage.INDONESIAN to "+ Lagu", AppLanguage.ENGLISH to "+ Song")
    )

    fun get(key: String, lang: AppLanguage = LanguageManager.currentLanguage): String {
        return translations[key]?.get(lang) ?: key
    }
}
