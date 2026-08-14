<div align="center">

  # 🏃‍♂️ PelariKalcer

  **Aplikasi Running Tracker & Leaderboard Komunitas dengan Tampilan Modern**

  [![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![Supabase](https://img.shields.io/badge/Supabase-Database-3FCF8E?style=for-the-badge&logo=supabase&logoColor=white)](https://supabase.com/)
  [![Android Studio](https://img.shields.io/badge/Android%20Studio-IDE-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white)](https://developer.android.com/studio)

  <p align="center">
    <a href="#-tentang-proyek">Tentang</a> •
    <a href="#-fitur-utama">Fitur</a> •
    <a href="#-tampilan-aplikasi">Demo</a> •
    <a href="#-tech-stack">Tech Stack</a> •
    <a href="#-struktur-folder-proyek">Struktur</a> •
    <a href="#-cara-menjalankan-getting-started">Cara Run</a>
  </p>

</div>

---

## 📌 Tentang Proyek

**PelariKalcer** adalah aplikasi Android berbasis **Jetpack Compose** dan **Supabase** yang dirancang untuk komunitas pelari modern. Aplikasi ini memudahkan pengguna untuk melacak performa lari, memantau posisi di leaderboard, serta terhubung dengan sesama teman pelari secara *real-time*.

---

## ✨ Fitur Utama

- 🏆 **Leaderboard Komunitas**: Pantau posisi dan persaingan jarak lari antar pengguna secara fleksibel dan *real-time*.
- 👥 **Add Friends System**: Cari dan tambahkan teman sesama pelari untuk saling memantau statistik aktivitas.
- 📱 **Modern Compose UI**: Antarmuka bersih, responsif, dan lancar menggunakan Jetpack Compose.
- ⚡ **Backend Real-Time**: Integrasi langsung ke Supabase untuk manajemen pengguna dan penyimpanan data aktivitas.

---

## 📱 Tampilan Aplikasi

<div align="center">
  <!-- Ganti URL gambar di bawah dengan screenshot/GIF asli aplikasi PelariKalcer -->
  <img src="https://via.placeholder.com/260x540.png?text=Main+Screen" width="30%" />
  <img src="https://via.placeholder.com/260x540.png?text=Leaderboard+Screen" width="30%" />
  <img src="https://via.placeholder.com/260x540.png?text=Add+Friends+Screen" width="30%" />
</div>

---

## 🛠️ Tech Stack

<div align="center">
  <a href="https://skillicons.dev">
    <img src="https://skillicons.dev/icons?i=kotlin,androidstudio,supabase,gradle,figma,git" />
  </a>
</div>

<br>

* **Language**: Kotlin
* **UI Framework**: Jetpack Compose & Material 3
* **Backend / Database**: Supabase (PostgreSQL, Realtime, Auth)
* **Architecture**: MVVM / Clean Architecture
* **Asynchronous**: Kotlin Coroutines & Flow

---

## 📁 Struktur Folder Proyek

<details>
<summary>🔍 Klik untuk melihat struktur folder</summary>

```text
app/src/main/java/com/example/pelarikalcer/
├── data/
│   ├── remote/        # SupabaseClient & API Service
│   └── repository/    # Data repositories
├── ui/
│   ├── navigation/    # App Navigation Graph
│   └── screens/
│       ├── leaderboard/  # Leaderboard Screen & Components
│       ├── main/         # Main Dashboard
│       └── friends/      # Add Friends & Friend List
└── util/              # Helper & Constants

