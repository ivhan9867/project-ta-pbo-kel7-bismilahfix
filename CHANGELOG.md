# ⚡ ARCLIGHT CITY — CHANGELOG

> Format: `[Versi] — Tanggal`
> Kategori: `Added` | `Fixed` | `Changed` | `Known Issues`
>
> Urutan: **terbaru di atas**, terlama di bawah.

---

## [v0.2.6] — 2026-04-29

### Added

**Split Layout — Window diperlebar ke 860×820px**
- Sebelumnya 420×820px. Sekarang: game area 560px (kiri) + chat panel 300px (kanan)
- ArclightApp: tambah konstanta `GAME_WIDTH=560`, `CHAT_WIDTH=300`
- UIFactory.screenRoot() dan MainMenuView canvas di-scale menyesuaikan
- Main Menu dan Character Create tetap full width; screen lain pakai split layout

**MercChatPanel — panel chat mercenary di kanan layar (MercChatPanel.java)**
- Persistent di semua game screen (Hub, Dungeon, Combat, Inventory, dll)
- Bubble chat per merc dengan warna unik masing-masing:
  KiraVoss=ungu | TankRX9=cyan | SeraMend=hijau | Vector=merah |
  MagnusForge=orange | EchoNull=kuning | LyraBloom=pink
- Fitur: timestamp, fade-in animation, delay 800ms antar pesan, auto-scroll, max 40 pesan
- System messages untuk event penting (floor enter, shop, dll)

**MercenaryDialogue — database dialog 150+ baris (MercenaryDialogue.java)**
- 7 mercenary × 17 trigger, masing-masing 3–5 variasi dialog
- Kepribadian unik tiap merc:
  KiraVoss: dingin profesional | TankRX9: formal android |
  SeraMend: hangat caring | Vector: sarkastis |
  MagnusForge: antusias keras | EchoNull: misterius teknis | LyraBloom: spiritual poetic
- Hanya 1–2 merc yang berbicara per trigger (tidak semua serentak)
- Trigger aktif: HUB_IDLE, HUB_ENTER_DUNGEON, DUNGEON_ENTER_ENEMY/BOSS/LOOT/REST/TRAP,
  COMBAT_START, COMBAT_PLAYER_LOW_HP, COMBAT_ENEMY_DIES, COMBAT_VICTORY, COMBAT_DEFEAT

**Chat triggers terintegrasi di semua screen**
- HubView: HUB_IDLE saat masuk hub, HUB_ENTER_DUNGEON saat enter dungeon
- DungeonMapView: trigger otomatis berdasarkan room type yang dimasuki
- CombatView: COMBAT_ENEMY_DIES, COMBAT_PLAYER_LOW_HP (<30% HP),
  COMBAT_VICTORY / COMBAT_DEFEAT di akhir combat

**CRAFT button → notifikasi "Coming Soon" di chat panel**
- Sebelumnya: klik tidak ada respons
- Sekarang: tampilkan "CRAFT SYSTEM — Coming Soon" di MercChatPanel

### Changed

**SceneRouter — rewrite lengkap**
- Tambah field `public final MercChatPanel chatPanel`
- `setSceneWithChat()`: HBox berisi game area + chat panel
- `setSceneFullWidth()`: tanpa chat (Main Menu, Char Create)
- Helper baru: `emitChat(trigger)` dan `addSystemChat(message)`

---

## [v0.2.5] — 2026-04-29

### Fixed

**REST room tidak bisa dikunjungi lagi setelah cleared**
- Guard di `enterRoom()` memblok semua room cleared, termasuk REST
- Fix: guard dikecualikan untuk `Room.RoomType.REST`

**Skill slot selalu kosong saat combat**
- Player baru dibuat tanpa skill apapun di `equippedSkillIds`
- Fix: `createCharacter()` memanggil `giveStarterSkills()` — auto-unlock
  2 skill per background tanpa memerlukan skillPoints

### Added

**REST Room Diminishing Heal System**
- REST bisa dikunjungi berkali-kali, heal berkurang per kunjungan:

  | Kunjungan | HP | MP |
  |-----------|----|----|
  | 1 | +35% | +50% |
  | 2 | +20% | +30% |
  | 3 | +10% | +15% |
  | 4+ | — | — |

- Merc ikut pulih proporsional (85% dari heal player)
- Room.java: tambah `restUseCount`, `getRestUseCount()`, `incrementRestUse()`

**Starter Skills per Background**

  | Background | Skill 1 | Skill 2 |
  |-----------|---------|---------|
  | Street Brawler | POWER_STRIKE | EXECUTE |
  | Netrunner | DEEP_HACK | VIRUS_UPLOAD |
  | Veteran Soldier | IRON_SHIELD | SHOCKWAVE |
  | Energy Adept | ENERGY_DRAIN | BIO_IRRADIATE |
  | Ghost Operative | PHANTOM_SHOT | SHADOW_STEP |
  | Techwright | EMP_BURST | FIELD_BARRIER |

- Player.java: tambah `forceUnlockSkill()` untuk bypass skillPoints requirement

**Shop Fungsional — Basic**
- Generate 4 item random berbasis floor level per kunjungan
- Harga per rarity:

  | Rarity | Harga |
  |--------|-------|
  | Common | 30–50g |
  | Uncommon | 80–120g |
  | Rare | 200–300g |
  | Epic | 500–700g |
  | Legendary | 1200–1500g |

- Tombol BUY disable + abu jika gold tidak cukup
- Item langsung masuk inventory setelah dibeli

### Known Issues
- Shop item tidak persistent — keluar-masuk shop = list baru (by design)
- Mercenary hire di hub belum ada — hanya TANK-RX9 sebagai starter

---

## [v0.2.4] — 2026-04-29

### Fixed

**[CRITICAL] Backtrack ke tile visited re-trigger event**

Root cause:
1. `enterRoom()` tidak punya guard untuk room cleared — handler selalu dipanggil
2. `Floor.moveToRoom()` hanya izinkan gerakan ke `nextRoomIndexes` (tidak symmetric)

Fix:
1. Guard di `enterRoom()`: jika `room.isCleared()`, emit `ROOM_ALREADY_CLEARED` dan return
2. `Floor.moveToRoom()` sekarang cek: A ada di nextRooms B **atau** B ada di nextRooms A

**ROOM_ALREADY_CLEARED tidak dihandle di wireEngineListeners**
- Sebelumnya: jatuh ke `default`
- Sekarang: punya handler eksplisit (refresh map + room info + next rooms panel)

### Changed

**buildCurrentRoomPanel — lebih informatif**
- Cleared room: judul abu + badge ✓ CLEARED
- Deskripsi berubah: "You've been here before. Nothing left to find."
- Progress bar visual untuk jumlah room dikunjungi
- Boss defeated indicator muncul jika boss sudah dikalahkan

---

## [v0.2.3.1] — 2026-04-29

### Fixed

**[CRITICAL] ProceduralGenerator.java — konten file duplikat setelah class closing brace**
- Root cause: `str_replace` hanya mengganti bagian awal file; konten lama (baris 245–479)
  tetap ada di bawah class closing brace
- Error compiler: `class, interface, enum, or record expected` di baris 479
- Fix: potong file di baris 244, buang semua setelahnya
- Verifikasi: brace balance 30:30 ✓

---

## [v0.2.3] — 2026-04-29

### Added

**Dungeon Full Grid Exploration — ProceduralGenerator overhaul**

| Floor | Grid | Total Tile |
|-------|------|-----------|
| 1–3 | 3×5 | 15 |
| 4–8 | 4×5 | 20 |
| 9–15 | 5×5 | 25 |
| 16+ | 6×5 | 30 |

- Semua tile berisi event random (bukan hanya jalur linear)
- Boss selalu di tengah baris terakhir — objective wajib untuk DESCEND
- Koneksi cardinal: atas/bawah/kiri/kanan (bukan diagonal)
- Tile visited bisa dikunjungi lagi (backtrack bebas)

**Fog of War 3 State**

| State | Tampilan |
|-------|---------|
| HIDDEN | Dot gelap kecil — tidak ada info |
| VISIBLE | Adjacent ke visited — icon redup |
| VISITED | Sangat redup + tanda ✓ jika cleared |
| CURRENT | Glow cyan |

**Koneksi Garis Cardinal H/V**
- Solid: koneksi visited–visited
- Putus-putus: koneksi ke tile visible
- Cyan: koneksi dari/ke tile current player

**DESCEND terkunci hingga Boss dikalahkan**
- `Floor.isBossDefeated()` method baru
- DESCEND button hanya muncul setelah boss cleared

**Map Legend** — icon + nama + warna tiap room type

### Changed

**DungeonGridMap — complete rewrite (v1 → v2)**
- Klik tile adjacent (cardinal) untuk bergerak
- Backtrack ke tile visited diperbolehkan
- Visual: fog of war, garis H/V, reachable dot indicator

---

## [v0.2.2] — 2026-04-28

### Fixed

**[CRITICAL] ConcurrentModificationException di Entity.tickEffects()**
- Root cause: `receiveDamage()` dipanggil di dalam loop `for (StatusEffect : activeEffects)`.
  Jika entity mati kena DOT, `activeEffects` bisa dimodifikasi saat iterasi berlangsung
- Fix: iterate over snapshot `new ArrayList<>(activeEffects)`,
  tambah `if (!alive) break` setelah DOT kill

**[WARNING] JavaFX native access warning di console**
- Tambah `--enable-native-access=javafx.graphics` ke pom.xml

### Added

**Dungeon Grid Map v1 — navigasi 2D interaktif (DungeonGridMap.java)**
- Canvas-based 2D grid, player icon ◈ bergerak dengan animasi ease in-out
- Fog of war dasar, highlight tile reachable, icon per room type
- Klik tile → animasi player → engine.moveToRoom() → event trigger

---

## [v0.2.1] — 2026-04-27

### Fixed

**[CRITICAL] Mercenary duplicate — muncul 2× TANK-RX9 di roster**
- Root cause: `createCharacter()` tidak clear `ownedMercs` dan `activeMercs`
  sebelum menambah starter merc; retry setelah game over → duplikasi
- Fix: tambah `.clear()` di awal `createCharacter()`
- Bonus: TankRX9 otomatis masuk active party tanpa ADD TO CREW manual

**Background putih di item list dan ScrollPane**
- Item row: eksplisit `background-color: #050810` di state normal
- ScrollPane: tambah `-fx-background: #050810`
  (JavaFX butuh property ini terpisah dari `-fx-background-color`)

**Warna border equipment slot selalu hijau (tidak mengikuti rarity item)**
- Fix: gunakan `UIFactory.rarityColor()` per item yang diequip
- Border left 3px sebagai rarity indicator visual
- Bonus: tampilkan upgrade level badge `+N`

**Teks title terlalu blur (ARCLIGHT, VICTORY, SYSTEM FAILURE)**
- DropShadow radius dikurangi: 20 → 6, spread: 0.7 → 0.3
- `glowPulse()` max radius: 15 → 6, durasi: 800ms → 1000ms

**Alert/Dialog popup masih style JavaFX default (putih)**
- Tambah CSS global `.dialog-pane` dengan style cyberpunk dark

### Changed

**Item row hover** — state normal eksplisit `#050810`, hover `#0C1220`

---

## [v0.2.0] — 2026-04-27

### Fixed

**[CRITICAL] AI Turn Loop Stacking (CombatView.java)**
- Tambah boolean guard `aiTurnPending` — Timeline tidak bisa menumpuk
- `startCombatLoop()` pakai delay 300ms agar UI render sebelum turn pertama

**[CRITICAL] DungeonManager tidak di-reset antar run**
- `startDungeonRun()` sekarang buat `DungeonManager` baru setiap kali dipanggil

**`refreshCurrentRoomInfo()` kosong** — body `{ }` tidak melakukan apa-apa

**`wireEngineListeners()` tidak handle LOOT_FOUND, REST, ROOM_CLEARED**
- Semua jatuh ke `default` yang memanggil method kosong

**Loot Room tidak generate item ke inventory**
- `LOOT_FOUND` sekarang memanggil `LootManager.generateLoot()` → push ke inventory
- Popup muncul dengan daftar item + rarity tag

**InventoryView — tidak ada tombol EQUIP**

**Upgrade/Calibrate tidak disabled saat kondisi tidak memenuhi**

### Added

**Loot Popup** — daftar item dengan rarity tag saat masuk LOOT room
**Rest Room notifikasi popup** — konfirmasi sebelum lanjut
**`showInfoAlert()` helper** — Alert dengan style cyberpunk konsisten

### Changed

**Inventory item row** — ringkasan 3 stat utama, USE button untuk consumable, stack count

### Known Issues
- Skill selection auto-pick skill pertama ready
- Target selection auto-target enemy pertama
- Shop masih placeholder "COMING SOON"
- Tidak ada animasi damage/heal di combat

---

## [v0.1.0] — 2026-04-26

### Release Awal

Game bisa dijalankan end-to-end setelah migrasi dari NetBeans Ant ke Maven.

**Flow yang berjalan:**
- Main Menu → Create Character → Hub
- Enter Dungeon → Navigate rooms (linear scroll)
- Combat (player vs enemy) → Victory / Defeat
- Mercenary screen (basic)
- Inventory screen (basic)
- Profile / Stats screen

**Cara run:**
```bash
cd ArclightCity
mvn javafx:run
```

---

## [v0.2.7] — 2026-04-29

### Changed

**[UI] Comprehensive UI Pass — semua screen diperbesar dan dirapikan**

Global:
- CSS `.root` font-size naik 13px → 14px
- `UIFactory.sectionTitle()` lebih terang: 10px → 11px, #5A6A80 → #8899AA
- `UIFactory.vitalBar()` label 10px → 11px, bar height 6px → 7px

Combat screen:
- Enemy card nama 11px → 14px, padding lebih lega, border left 3px
- Ally card lebar 110px → 150px, nama 9px → 12px, tampilkan role badge
- Skill slots 72×44px → 110×52px, font 8px → 11px, hover glow
- Action buttons lebar 110 → 130px, font 11px → 13px, hover dropshadow
- Combat log tinggi 110 → 130px, entry font 10px → 12px

Semua screen:
- Teks 9px → 11px, teks 10px → 12px, teks 8px → 10px (secara konsisten)
- MercChatPanel: dialog text sedikit lebih besar untuk readability

---

## [v0.2.7.1] — 2026-04-29

### Fixed

**[BUG] Hub screen — bottom nav terpotong tidak terlihat (HubView.java)**
- Root cause: setelah font diperbesar di v0.2.7, konten Hub (player bar + vitals +
  district banner + 4 nav button) melebihi tinggi 820px sehingga bottom nav
  terdorong keluar layar
- Fix: wrap vitals + banner + nav buttons dalam ScrollPane dengan VBox.setVgrow(ALWAYS)
  sehingga area tengah bisa discroll, sementara bottom nav tetap fixed di bawah
- Player bar (atas) dan bottom nav (bawah) tidak ikut discroll

**[BUG] Combat — action panel bisa terpotong di layar (CombatView.java)**
- Root cause: UIFactory.spacer() dengan Priority.ALWAYS mendorong action panel
  keluar layar ketika konten di atas (log + enemy + ally + status) terlalu besar
- Fix 1: hapus UIFactory.spacer() sebelum action panel
- Fix 2: hapus VBox.setVgrow(logScroll, ALWAYS) — combat log sekarang fixed
  130px, tidak bisa grow tak terbatas
