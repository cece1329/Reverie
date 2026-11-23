# Reverie Music - Modern Android Audio Player

**Reverie Music** adalah aplikasi pemutar musik Android modern berbasis Jetpack Compose dan Media3 ExoPlayer dengan tampilan antarmuka yang estetik, responsif, dan kaya fitur.

---

## 🎧 Fitur Utama

- **Native Audio Player**: Pemutaran musik tingkat lanjut menggunakan Jetpack Media3 ExoPlayer dengan dukungan pemutaran latar belakang (*background playback*), lirik tersinkronisasi (*synced lyrics*), dan kontrol media notification.
- **Integrasi Audius REST API**: Mengakses ribuan lagu *trending* dan pencarian musik secara *real-time* langsung dari jaringan publik Audius tanpa iklan.
- **Upload Lagu MP3 Sendiri**: Pengguna dapat menambahkan file audio MP3 dari penyimpanan lokal perangkat secara langsung.
- **Koleksi & Playlist Kustom**: Membuat, mengedit, dan menghapus playlist buatan sendiri serta menyimpan lagu favorit.
- **Autentikasi & Profil**: Mendukung pendaftaran/masuk akun dengan Email & Kata Sandi serta integrasi **Google Sign-In**. Pengguna juga dapat memperbarui nama pengguna dan email.
- **Splash Screen & Visualizer Estetik**: Dilengkapi animasi loading screen serta visualizer *waveform* dan notasi musik yang mengapung di header beranda.
- **Mode Terang/Gelap & Bahasa**: Mendukung perpindahan tema Dark Mode / Light Mode dan bahasa (Indonesia & Inggris).

---

## 🛠️ Tech Stack & Library

- **Bahasa**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Audio Player**: Jetpack Media3 ExoPlayer (`androidx.media3:media3-exoplayer`, `media3-session`, `media3-ui`)
- **Image Loading**: Coil (`io.coil-kt:coil-compose`)
- **Networking**: OkHttp & Retrofit + Gson
- **Async & Reactive**: Kotlin Coroutines & Flow (`StateFlow`)
- **Autentikasi**: Google Play Services Auth (`play-services-auth`)

---

## 🚀 Cara Menjalankan Aplikasi

1. Clone repository ini:
   ```bash
   git clone https://github.com/cece1329/Reverie.git
   ```
2. Buka folder proyek di **Android Studio (Ladybug / Iguana atau yang lebih baru)**.
3. Sync proyek dengan Gradle Files.
4. Hubungkan perangkat fisik Android (versi Android 8.0 / API 26+) atau jalankan Emulator.
5. Tekan tombol **Run** (`Shift + F10`) di Android Studio.

---

## 🌐 API Reference

Aplikasi ini menggunakan **Audius REST API** publik:
- **Trending Tracks**: `GET https://discoveryprovider.audius.co/v1/tracks/trending`
- **Search Tracks**: `GET https://discoveryprovider.audius.co/v1/tracks/search?query={query}`
- **Stream Audio**: `GET https://discoveryprovider.audius.co/v1/tracks/{id}/stream`

---

## 📄 Lisensi & Kredit

Dipersembahkan oleh **Citra Maharani** • Reverie Music App v2.0
