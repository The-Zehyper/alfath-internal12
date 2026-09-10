# 07 — Riwayat gelombang UI (kilau)

Catatan ringkas tiap gelombang penyempurnaan UI: apa yang diubah, di berkas
mana, dan keputusan desain yang diambil. Gelombang 1–7 sudah tercatat tersebar
di `04` (tema), `05` (fitur), dan `06` (porting); berkas ini meneruskan untuk
gelombang berikutnya agar mudah dirunut balik oleh sesi mendatang.

## Gelombang 8 — "Now Playing & antrean berkilau" (2026-09-08, commit ...)

Tidak ada dependensi baru, tidak ada blur baru, tidak ada string yang
dihilangkan. Semua animasi tetap lewat helper `Motion.kt`.

| Perubahan | Berkas | Keputusan desain |
|---|---|---|
| Pratinjau "Berikutnya" (up to 2 lagu) di Now Playing, ketuk = lompat ke indeks antrean | `ui/screens/NowPlayingScreen.kt` (+ `np_up_next` di 6 strings.xml) | Meniru pola Metrolist/Meld tanpa membuka lembar antrean; kartu inset membulat + hairline; tidak digambar sama sekali saat tidak ada lagu tersisa (nol biaya). Durasi kanan pakai `formatMs`; ikon putar memakai aksen sampul |
| Baris antrean & riwayat menjadi inset membulat ala daftar grup iOS; baris aktif memakai aksen sampul (alpha 0.14), yang lain `NanzMusifySurface` 0.45 | `ui/screens/NowPlayingScreen.kt` | Daftar `LazyColumn` tetap satu arah, tanpa blur per item; tinggi baris tetap konstan supaya scroll mulus; kontenPadding + padding baris diatur agar tidak menambah tinggi tak terduga |
| Baris lirik aktif mendapat pita lembut aksen (alpha 0.10, membulat) yang muncul/memudar mengikuti pergantian baris | `ui/components/LyricsSheet.kt` | Mempertegas baris yang sedang dinyanyikan di atas sampul terang tanpa menaikkan kontras teks; alpha dijaga rendah agar tidak bersaing dengan teks aksen |
| String baru `np_up_next` (+ terjemahan) | 6 berkas `strings.xml` | Paritas 340 string di semua locale dipertahankan; tanpa emoji/unicode dekoratif |

Aturan yang dijaga: tidak menyentuh `yt/`, `player/`, `download/`; tidak ada
`Color(0x…)` baru di layar; radius baru memakai token `NanzMusifyRadius`; tidak ada
animasi per item list panjang selain yang sudah ada (pita lirik hanya satu item
aktif, sama seperti alpha/skala baris lirik yang sudah ada sebelumnya).

### Gelombang 8b — tombol lirik terapung (2026-09-08)

- Kanvas sampul ⇄ lirik kini punya tombol bulat terapung di pojok kanan atas
  (ikon Lirik, latar hitam 38%, aksen sampul saat mode lirik aktif) sehingga
  perpindahan mode bisa ditemukan, bukan hanya ketukan tersembunyi pada gambar.
  Posisi pojok atas dipilih karena saat mode lirik area itu adalah ruang kosong
  auto-scroll — tidak pernah menutupi baris teks. `NowPlayingScreen.kt`,
  tanpa string baru.

### Gelombang 9 — "Tiru Meld": audit bug pencarian, artis, motion blur (2026-09-08)

Audit menyeluruh terhadap keluhan pemilik proyek dengan membandingkan perilaku
NanzMusify vs Meld/Metrolist (`SearchSummaryPage.kt`, `ArtistScreen.kt`,
`HomeScreen.kt`, `YouTube.kt` upstream dibaca langsung). Perbaikan:

**Motion blur transisi "tidak bekerja" — AKAR MASALAH di `MotionBlur.kt`:**
- Watchdog `IDLE_RELEASE_NS` 110 ms mematikan pulse transisi layar lebih cepat
  daripada durasi transisi (~260–350 ms), jadi blur hilang sebelum perpindahan
  selesai. Dinaikkan ke 360 ms; pulse di `MainActivity` tidak dilepas manual
  lagi — watchdog yang meluruhkan (mengikuti durasi transisi).

**Search: tab SEMUA tidak menampilkan semua rak (pola ringkasan Meld):**
- Rak artis & album kini selalu tampil saat hasilnya ada (tidak hanya saat
  filternya dipilih). Rak lagu disembunyikan saat filter ARTIS/ALBUM.
- Filter ARTIS & ALBUM memakai jalur YouTube Music `musicSearch` tanpa params
  (ringkasan semua jenis) lalu layar memilih raknya — sebelumnya ARTIS memakai
  extractor yang tidak mengerti entitas artis.
- Rak playlist hasil YouTube Music (jalur WEB_REMIX) kini dirender di tab
  SEMUA/LAGU/VIDEO lewat `musicPlaylists` (sebelumnya hanya di filter
  PLAYLIST); playlist hasil extractor tetap untuk PLAYLIST/PODCAST. Kunci
  LazyColumn diberi awalan `pl:` agar tidak bentrok antar sumber.

**Artis: bio & channel tidak muncul (pola `ArtistScreen` Meld):**
- Bio artis/playlist ada di rak `musicDescriptionShelfRenderer` (Tentang/About)
  yang sebelumnya diabaikan pengurai → kini disuntikkan ke
  `BrowseHeader.description` sehingga hero menampilkannya.
- Kartu artis Home kini avatar BULAT (`CircleShape`), bukan kotak membulat;
  avatar halaman artis bulat penuh (route `browse/UC…` → `isArtist`).
- Subtitle baris artis yang merupakan dirinya sendiri (endpoint kanal UC…)
  diisi baris jumlah pengikut/pendengar, bukan nama artis duplikat
  (deteksi kolom berangka & kata pengikut).

**Thumbnail artis placeholder di Home (charts):**
- `fetchChartsTopArtists` hanya membaca `thumbnail.musicThumbnailRenderer`;
  charts memakai bentuk kotak-terpotong `croppedSquareThumbnailRenderer` untuk
  sebagian kanal → ditambahkan fallback. `listThumb`/`twoRowThumb` browse ikut
  mendapat fallback bentuk yang sama.
- `enrichArtistAvatars` (isi yang kosong) kini mencoba thumbnail kepala halaman
  artis InnerTube dulu (jalur yang sama dengan halaman artis, cache 6 jam),
  baru NewPipe `channelProfile`; berurutan untuk membatasi semburan jaringan.
- Pembersihan import `async/awaitAll/coroutineScope` yang tidak terpakai.

Berkas: `MotionBlur.kt`, `MainActivity.kt`, `SearchScreen.kt`, `ViewModels.kt`,
`BrowseModel.kt`, `YouTubeRepository.kt`, `BrowseScreen.kt`, `HomeScreen.kt`.
Tanpa string baru; tanpa sentuhan `player/`, `download/`.

