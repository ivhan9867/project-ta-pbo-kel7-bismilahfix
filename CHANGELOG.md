# ARCLIGHT CITY — CHANGELOG

Format: [Versi] - Tanggal
Kategori: Added | Fixed | Changed | Known Issues

---

## [v0.2.0] - 2026-04-27

### Fixed

**[CRITICAL] AI Turn Loop Stacking (CombatView.java)**
- Sebelumnya `handleCombatEvent()` membuat `Timeline` baru setiap kali event masuk
  tanpa guard, sehingga saat AI punya banyak turn berturut-turut, Timeline bisa
  menumpuk dan menyebabkan turn diproses dua kali lipat atau lebih
- Solusi: tambah field `aiTurnPending` sebagai boolean guard — Timeline baru hanya
  dibuat jika tidak ada yang sedang berjalan
- `startCombatLoop()` sekarang pakai delay 300ms sebelum turn pertama agar UI
  sempat ter-render sebelum proses dimulai

**[CRITICAL] DungeonManager tidak di-reset antar run (GameEngine.java)**
- `startDungeonRun()` sebelumnya memanggil `startDungeon()` pada DungeonManager
  yang sama, sehingga state dari run sebelumnya bisa bocor ke run baru
- Solusi: buat `DungeonManager` baru setiap kali `startDungeonRun()` dipanggil
  dan re-wire semua listener

**[BUG] `refreshCurrentRoomInfo()` kosong (DungeonMapView.java)**
- Method ini sebelumnya tidak melakukan apa-apa — body kosong `{ }`
- Sekarang: rebuild `currentRoomPanel` dan replace di parent VBox secara langsung
  menggunakan index lookup

**[BUG] `wireEngineListeners()` tidak handle banyak event type (DungeonMapView.java)**
- `LOOT_FOUND`, `REST`, `ROOM_CLEARED`, `ROOM_ENTERED`, `FLOOR_ENTERED` semua
  jatuh ke `default` yang hanya memanggil `refreshCurrentRoomInfo()` kosong
- Sekarang setiap event type punya handler yang tepat

**[BUG] Loot Room tidak tampilkan item ke player (DungeonMapView.java)**
- Sebelumnya loot hanya di-emit sebagai event tapi tidak di-resolve ke actual items
- Sekarang `LOOT_FOUND` event memanggil `LootManager.generateLoot()` dan langsung
  menambahkan item ke inventory player
- Popup konfirmasi tampil dengan daftar item yang didapat beserta rarity-nya

**[BUG] InventoryView — tombol EQUIP tidak ada (ViewsBundle.java)**
- Item equipment di inventory tidak bisa di-equip dari UI, hanya bisa di-upgrade
  dan dikalibrasi
- Sekarang ada tombol EQUIP di setiap equipment row
- Setelah equip/upgrade/kalibrasi, list di-refresh otomatis

**[BUG] Upgrade/Calibrate button tidak disabled saat kondisi tidak memenuhi (ViewsBundle.java)**
- Tombol upgrade masih aktif meski item sudah max level
- Tombol kalibrasi masih aktif meski tidak punya Calibration Kit
- Sekarang kedua tombol disable + style grey saat tidak bisa digunakan

### Changed

**Inventory item row — lebih informatif (ViewsBundle.java)**
- Sebelum: hanya tampil nama dan deskripsi generic
- Sekarang: tampil ringkasan 3 stat utama dengan format yang proper
  (persentase untuk stat seperti Crit Chance, flat untuk ATK/DEF)
- Consumable tampil stack count dan tombol USE langsung dari inventory
- Material tampil quantity

**DungeonMapView — instance fields untuk refresh (DungeonMapView.java)**
- Tambah `mapGridScrollPane` dan `currentRoomPanel` sebagai instance field
  agar bisa di-refresh tanpa rebuild seluruh screen
- Tambah `refreshMapGrid()` dan `refreshNextRoomsPanel()` sebagai method terpisah

### Added

**Loot Popup — feedback visual saat dapat item (DungeonMapView.java)**
- Saat masuk LOOT room, popup muncul menampilkan daftar item yang didapat
  lengkap dengan rarity tag `[Common]`, `[Rare]`, dll
- Item langsung masuk inventory player

**Rest Room — notifikasi popup (DungeonMapView.java)**
- Saat masuk REST room, popup konfirmasi muncul sebelum lanjut
- Menampilkan pesan berapa HP dan MP yang dipulihkan

**`showInfoAlert()` helper (DungeonMapView.java)**
- Helper method baru untuk tampilkan Alert dengan style cyberpunk yang konsisten
- Digunakan oleh loot popup dan rest notification

### Known Issues

- Skill selection masih auto-pilih skill pertama yang ready, belum ada UI
  untuk memilih skill mana yang ingin digunakan
- Target selection masih auto-target enemy pertama yang hidup
- Shop screen masih placeholder "COMING SOON"
- Tidak ada animasi saat damage / healing di combat
- Boss phase transition tidak ada notifikasi visual khusus
- Save/Load system belum ada

---

## [v0.1.0] - 2026-04-26

### Release Awal

Game bisa dijalankan end-to-end:
- Main Menu → Create Character → Hub
- Enter Dungeon → Navigate rooms
- Combat (player vs enemy) → Victory/Defeat
- Mercenary screen
- Inventory screen (basic)
- Profile/Stats screen

Migrasi dari NetBeans Ant ke JavaFX Maven (lihat `CHANGELOG_FIXES.md`
untuk detail lengkap 16 bug yang diperbaiki selama migrasi).

**Cara run:**
```bash
cd ArclightCity
mvn javafx:run
```

---

## [v0.2.1] - 2026-04-27

### Fixed

**[BUG] Mercenary duplicate — muncul 2x TANK-RX9 di roster (GameEngine.java)**
- Root cause: `createCharacter()` tidak clear `ownedMercs` dan `activeMercs` sebelum
  menambah starter merc. Jika game over lalu retry, merc lama masih ada + merc baru
  ditambahkan lagi sehingga muncul duplikat
- Fix: tambah `ownedMercs.clear()` dan `activeMercs.clear()` di awal `createCharacter()`
- Bonus fix: starter TankRX9 sekarang otomatis masuk `activeMercs` sehingga langsung
  siap dibawa ke dungeon tanpa perlu ADD TO CREW manual

**[UI] Background putih di list item dan ScrollPane (ViewsBundle.java, CSS)**
- Item list sebelumnya tidak punya `background-color` eksplisit di state normal,
  menyebabkan JavaFX fallback ke background default (putih/abu)
- Fix item row: tambah `background-color: #050810` di style normal
- Fix ScrollPane: tambah `-fx-background: #050810` (JavaFX butuh property ini
  terpisah dari `-fx-background-color` untuk benar-benar remove white viewport)
- Fix global CSS: tambah `.scroll-pane .viewport { -fx-background-color: transparent }`

**[UI] Warna border equipment slot selalu hijau, tidak mengikuti rarity (ViewsBundle.java)**
- Sebelumnya semua slot yang terisi memiliki border hijau (`#00E67644`) terlepas
  dari rarity item yang diequip
- Fix: ambil `Equipment` object langsung (bukan hanya nama), lalu gunakan
  `UIFactory.rarityColor()` untuk menentukan warna border
- Common = abu, Uncommon = hijau, Rare = biru, Epic = ungu, Legendary = orange
- Tambahan: tampilkan upgrade level `+N` di bawah nama item jika level > 0
- Border left lebih tebal (3px) sebagai rarity indicator visual yang jelas

**[UI] Teks ARCLIGHT, VICTORY, SYSTEM FAILURE terlalu blur — tidak terbaca (ViewsBundle, MainMenuView, UIFactory)**
- DropShadow radius 20 dengan spread 0.7 menyebabkan glow sangat besar yang
  mengaburkan teks terutama di background gelap
- Fix: kurangi radius dari 20 → 6, spread dari 0.7 → 0.3 untuk semua title besar
- `glowPulse()` animation max radius dari 15 → 6, durasi 800ms → 1000ms
  agar animasi lebih smooth dan tidak jitterish

**[UI] Alert/Dialog popup masih style JavaFX default (putih) (arclight.css)**
- Tambah CSS global untuk `.dialog-pane` dan semua child-nya:
  background gelap, border cyan, button style cyberpunk
- Cancel button di confirmation dialog pakai warna merah

### Changed

**Item row hover — lebih konsisten (ViewsBundle.java)**
- State normal: `#050810` (dark) → hover: `#0C1220` (slightly lighter)
- Sebelumnya state normal tidak punya background eksplisit

### Known Issues

- Dungeon map masih linear scroll, belum grid 2D yang bisa dinavigasi (target v0.3)
- Skill selection masih auto-pick skill pertama ready (target v0.3)
- Target selection masih auto-target enemy pertama hidup (target v0.3)
- Shop masih placeholder (target v0.5)

---

## [v0.2.2] - 2026-04-28

### Fixed

**[CRITICAL] ConcurrentModificationException di Entity.tickEffects() (Entity.java)**
- Exception terjadi saat DOT effect (Burn, Bleed, Virus, dll) diterapkan dalam combat
- Root cause: `receiveDamage()` dipanggil di dalam loop `for (StatusEffect : activeEffects)`.
  Jika entity mati kena DOT dan `onDeath()` memodifikasi `activeEffects`, iterator
  menjadi invalid → ConcurrentModificationException
- Fix:
  1. Buat snapshot `List<StatusEffect> snapshot = new ArrayList<>(activeEffects)`
     dan iterate over snapshot, bukan list asli
  2. Tambah guard `if (!activeEffects.contains(effect)) continue` untuk skip
     effect yang dihapus di tengah iterasi
  3. Tambah `if (!alive) break` untuk stop DOT processing saat entity sudah mati

**[WARNING] JavaFX native access warning di console (pom.xml)**
- Warning: "A restricted method in java.lang.System has been called"
- Fix: tambah `--enable-native-access=javafx.graphics` ke javafx-maven-plugin options

### Added

**[FEATURE] Dungeon Grid Map — navigasi 2D interaktif (DungeonGridMap.java)**
- File baru: `DungeonGridMap.java` — Canvas-based 2D grid dungeon map
- Menggantikan tampilan linear scroll sebelumnya
- Fitur grid map:
  - Grid COLS×N (5 kolom) yang di-generate otomatis dari Floor rooms
  - Player icon (◈) bergerak smooth antar tile dengan animasi ease in-out 250ms
  - **Fog of war** — tile belum dikunjungi tampil gelap dengan icon "?"
  - **Highlight reachable** — tile yang bisa dituju memiliki glow sesuai room type
  - Icon per room type: ◆=enemy, ☠=boss, ☆=loot, ♥=rest, ?=event, $=shop, !=trap
  - Connection lines antar tile yang terhubung (reachable = cyan glow)
  - Cleared rooms ditampilkan redup + checkmark ✓ di pojok kanan
  - Room index ditampilkan kecil di pojok kiri atas tile
  - Upgrade badge dan warna border tile mengikuti room type color
  - Klik tile → player bergerak → engine.moveToRoom() dipanggil → event trigger

**[FEATURE] Dungeon map navigasi via klik tile (DungeonMapView.java)**
- Integrasi DungeonGridMap ke DungeonMapView
- Panel bawah berubah: tidak lagi berisi tombol room, tapi
  legend badge room type yang reachable + hint "Click tile to move"
- Floor complete: tombol DESCEND muncul otomatis saat semua room cleared

### Changed

**DungeonMapView — refactor (DungeonMapView.java)**
- Hapus `buildMapGrid()` dan `buildRoomNode()` (diganti DungeonGridMap)
- Hapus `buildRoomButton()` (navigasi sekarang via klik tile)
- `refreshMapGrid()` sekarang hanya panggil `dungeonGridMap.refresh()`
- Field `mapGridScrollPane` diganti `dungeonGridMap: DungeonGridMap`

### Known Issues
- Dungeon grid belum support branching path yang kompleks —
  saat ini koneksi antar room mengikuti `getNextRoomIndexes()` dari Room
- Animasi player masih linear (ease in-out sudah ada, tapi path hanya garis lurus)
- Skill selection masih auto-pick (target v0.3)
- Shop masih placeholder (target v0.5)

---

## [v0.2.3] - 2026-04-29

### Added

**[FEATURE] Dungeon Full Grid Exploration — ProceduralGenerator overhaul (ProceduralGenerator.java)**
- Floor sekarang di-generate sebagai grid COLS×ROWS yang **penuh** — tidak ada tile kosong
- Jumlah tile per floor: F1-3 = 15 tile (3×5), F4-8 = 20 tile (4×5),
  F9-15 = 25 tile (5×5), F16+ = 30 tile (6×5)
- Semua tile berisi event random (ENEMY, LOOT, REST, EVENT, SHOP, TRAP, ELITE)
- Boss selalu ada 1 di tengah baris terakhir — harus dikalahkan untuk DESCEND
- Distribusi tile terjamin: minimal N LOOT, REST, EVENT, SHOP, TRAP per floor
- Koneksi cardinal: setiap tile terhubung ke tile atas/bawah/kiri/kanan
- Tile yang sudah dikunjungi bisa dikunjungi lagi (backtrack bebas)

**[FEATURE] Fog of War 3 State (DungeonGridMap.java)**
- HIDDEN  → tile tidak terlihat, hanya dot gelap kecil
- VISIBLE → tile adjacent ke visited: icon kelihatan tapi redup (belum dikunjungi)
- VISITED → tile sudah dikunjungi: icon sangat redup + tanda ✓ jika cleared
- CURRENT → tile player sekarang: glow cyan

**[FEATURE] Koneksi Garis Cardinal H/V (DungeonGridMap.java)**
- Garis penghubung antar tile hanya horizontal dan vertikal (tidak diagonal)
- Garis solid untuk koneksi antar visited tiles
- Garis putus-putus untuk koneksi ke visible (belum dikunjungi)
- Garis cyan highlight untuk koneksi dari/ke tile current player

**[FEATURE] Boss sebagai Objective — DESCEND terkunci (Floor.java, DungeonMapView.java)**
- Method baru `Floor.isBossDefeated()` — cek apakah boss room sudah di-clear
- DESCEND button hanya muncul setelah boss dikalahkan
- Sebelum boss mati: panel bawah tampilkan reminder "☠ Defeat the BOSS to unlock"

**[FEATURE] Map Legend (DungeonMapView.java)**
- Legend row di bawah map: icon dan nama setiap room type dengan warna

### Changed

**ProceduralGenerator — complete rewrite**
- Sebelum: linear path dengan percabangan (6-16 rooms total)
- Sekarang: full grid dengan koneksi cardinal (15-30 tiles)
- `buildConnections()` → `buildCardinalConnections()` yang generate 4-arah
- `getRoomCount()` → `getGridRows()` untuk kontrol ukuran grid
- `getBossIndex()` dan `getTotalTiles()` ditambah sebagai public utility

**DungeonGridMap — full rewrite (v1 → v2)**
- Navigation: klik tile adjacent (cardinal only)
- Backtrack: bisa kembali ke tile yang sudah dikunjungi
- Visual: fog of war, garis H/V, reachable dot indicator

**DungeonMapView — refreshNextRooms redesign**
- Panel bawah sekarang tidak lagi daftar tombol room
- Menampilkan: hint navigasi + boss reminder + legend
- DESCEND button muncul kondisional (setelah boss defeated)

### Known Issues
- Backtrack ke tile visited re-trigger event (akan difix: cek `room.isCleared()` sebelum trigger)
- Animasi player masih garis lurus (path-finding A* untuk v0.3)

---

## [v0.2.3.1] - 2026-04-29

### Fixed

**[CRITICAL] ProceduralGenerator.java — konten file duplikat setelah class closing brace**
- Root cause: `str_replace` sebelumnya hanya mengganti bagian awal file (javadoc + class header)
  tapi tidak menghapus konten lama di bawahnya, sehingga file berisi dua versi kode:
  versi baru (baris 1-244) + sisa versi lama (baris 245-479)
- Error: `class, interface, enum, or record expected` di baris 479 karena ada kode
  di luar class block (setelah `}` closing)
- Error kedua: `compact source file should not have package declaration` — Java 25
  mendeteksi file tidak valid sebagai class file biasa
- Fix: potong file di baris 244 (tepat setelah `}` class closing), buang semua
  konten setelahnya
- Verifikasi: brace balance sekarang 30:30 (seimbang)

---

## [v0.2.4] - 2026-04-29

### Fixed

**[CRITICAL] Backtrack ke tile visited re-trigger event (DungeonManager.java)**
- Root cause 1: `enterRoom()` tidak punya guard untuk room yang sudah cleared —
  setiap kali player masuk room manapun, handler dipanggil ulang
- Root cause 2: `Floor.moveToRoom()` hanya izinkan gerakan ke `nextRoomIndexes`
  dari current room, tidak mengizinkan backtrack ke tile sebelumnya yang sudah
  dikunjungi (karena koneksi cardinal bersifat symmetric tapi tidak bidirectional
  di getNextRoomIndexes)
- Fix 1: tambah early-return guard di `enterRoom()` — jika `room.isCleared()`,
  langsung emit `ROOM_ALREADY_CLEARED` dan return tanpa trigger event apapun
- Fix 2: `Floor.moveToRoom()` sekarang izinkan backtrack — cek BAIK apakah
  roomIndex ada di `current.getNextRoomIndexes()` MAUPUN apakah current ada
  di `targetRoom.getNextRoomIndexes()` (symmetric check)

**[BUG] handleRestRoom dan handleEmptyRoom tidak punya isCleared guard (DungeonManager.java)**
- Sebelumnya: REST room masuk lagi → HP/MP dipulihkan lagi (exploit/bug)
- Fix tidak diperlukan lagi karena sudah ditangani oleh guard di `enterRoom()` —
  jika room cleared, tidak akan sampai ke handler manapun

**[UI] ROOM_ALREADY_CLEARED tidak dihandle di wireEngineListeners (DungeonMapView.java)**
- Event ini sebelumnya jatuh ke `default` yang memanggil `refreshCurrentRoomInfo()`
- Sekarang: punya handler eksplisit yang refresh map + room info + next rooms panel

### Changed

**buildCurrentRoomPanel — lebih informatif (DungeonMapView.java)**
- Cleared room tampil berbeda: judul menjadi abu, ada badge ✓ CLEARED di samping
- Deskripsi berubah menjadi "You've been here before. Nothing left to find."
- Progress bar visual (tidak hanya teks) untuk jumlah room yang sudah dikunjungi
- Boss defeated indicator muncul di panel jika boss sudah dikalahkan

### Known Issues
- REST room tidak bisa digunakan lagi setelah cleared (by design — anti-exploit)
  Akan dipertimbangkan untuk dikembalikan sebagai "partial heal" di v0.3
- Skill selection masih auto-pick (target v0.3)
- Shop masih placeholder (target v0.5)
