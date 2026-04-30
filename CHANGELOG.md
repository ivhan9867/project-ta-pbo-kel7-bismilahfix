# ⚡ ARCLIGHT CITY — CHANGELOG

> Format: `[Versi] — Tanggal`
> Kategori: `Added` | `Fixed` | `Changed` | `Known Issues`
> Urutan: **terbaru di atas**, terlama di bawah.

---

## [v0.3.0] — 2026-04-30

### Fixed

**Hub bottom nav terpotong — definitif fix (HubView.java)**
- Bottom nav sekarang punya `setMinHeight(64)`, `setMaxHeight(64)`, `setPrefHeight(64)`
- Dengan fixed height eksplisit, bottom nav tidak bisa di-override JavaFX layout engine
- Padding dikurangi 8 → 6px untuk mengakomodasi fixed height

### Added

**Turn Order Bar (CombatView.java, TurnQueue.java)**
- Bar horizontal di bawah top bar combat — antrian giliran hingga 6 entity ke depan
- Warna per faction: Player=cyan, Mercenary=hijau, Enemy=merah
- Entity yang sedang giliran: border highlight + prefix "▶"
- HP mini bar per slot, separator "›" antar entity
- `TurnQueue.getUpcomingTurns(n)` — simulasi round berikutnya jika slot kurang

**Skill Selection Popup (CombatView.java)**
- Klik SKILL → popup berisi semua skill equipped (bukan auto-pick pertama)
- Setiap skill: nama, deskripsi, MP cost, status CD, badge AoE
- Skill ready = highlight cyan | Skill CD/MP kurang = disabled + abu
- Tooltip deskripsi saat hover, Cancel button untuk menutup

**Target Selection Mode (CombatView.java)**
- Klik ATTACK atau skill single-target → masuk target select mode
- Semua enemy card berubah border kuning glow
- Hover enemy → glow lebih terang sebagai visual feedback
- Cancel dengan tombol "✕ CANCEL" di action panel
- Skill AoE langsung eksekusi ke semua enemy (bypass target select)

**Combat Speed Control (CombatView.java)**
- Tombol `1×` `2×` `SKIP` di atas action buttons
- 1× = 500ms (normal) | 2× = 200ms (cepat) | SKIP = 50ms (hampir instant)
- Tetap tampil saat AI turn ("Processing..." state)

**SkillInfo Database (CombatView.java)**
- Record `SkillInfo(name, description, mpCost, isAoe)`
- 12 skill starter punya deskripsi, MP cost, dan AoE flag lengkap

### Changed

**refreshActionPanel — redesign**
- Target select mode: indicator menggantikan seluruh action panel saat aktif
- "Processing..." state inline dengan speed control di sampingnya

**handleCombatEvent**
- AI turn delay menggunakan `combatSpeedMs` (variable, bukan hardcoded 500ms)
- refreshTurnOrderBar() dipanggil setiap event

### Known Issues
- Floating damage numbers ditunda ke v0.3.1 — butuh Canvas overlay terpisah
- Level up screen ditunda ke v0.4 sesuai roadmap

---

## [v0.2.8.1] — 2026-04-30

### Fixed

**EventView — choices terpotong di bawah layar (ViewsBundle.java)**
- Root cause: `VBox card` + `UIFactory.spacer()` mendorong choices keluar layar
- Fix: hapus spacer, wrap konten dalam ScrollPane

**ShopView — `setVgrow(ALWAYS)` tidak perlu pada itemList**
- itemList sudah di dalam ScrollPane, `setVgrow` tambahan tidak diperlukan

**VictoryView dan GameOverView — konten bisa terpotong**
- Hapus `root.setAlignment(CENTER)` yang konflik dengan ScrollPane
- Wrap content dalam ScrollPane untuk safety

---

## [v0.2.8] — 2026-04-30

### Changed

**Dungeon Grid Map — Comprehensive Visual Overhaul (DungeonGridMap v3)**

| Aspek | Sebelum | Sesudah |
|-------|---------|---------|
| Tile size | 42×42px | 52×52px |
| Tile gap | 6px | 8px |
| Corner radius | 4px | 6px |
| Icon font | 15px | 18–20px |
| Background | Flat hitam | Dot grid halus 16px spacing |

Fitur baru:
- **Player breathing pulse** — 3 ring concentric yang bernapas (sinusoidal, 2.4 detik cycle)
- **Marching ants border** — reachable tile punya dashed border yang bergerak
- **Hover tooltip** — nama room muncul saat hover tile adjacent (mis. "Supply Cache")
- **Arrow indicator (→)** di tengah tile saat di-hover
- **Boss tile double ring** — outer ring 5px + 9px berlapis
- **Cleared tiles diagonal stroke** — bukan sekadar redup, ada garis diagonal overlay
- **Hidden tiles cross-hatch** — pattern subtle, bukan flat hitam
- Label "BOSS" berubah jadi "SLAIN" setelah boss dikalahkan
- Animasi di-pause saat player bergerak, di-resume setelah selesai

**DungeonMapView — map area lebih lega**
- gridScroll.setPrefHeight 230px → 290px

---

## [v0.2.7.1] — 2026-04-29

### Fixed

**Hub bottom nav terpotong setelah font naik (HubView.java)**
- Konten tengah (vitals + banner + 4 tombol) dibungkus ScrollPane
- Player bar atas dan bottom nav bawah tetap fixed, tidak ikut scroll

**Combat action panel terpotong (CombatView.java)**
- Hapus `UIFactory.spacer()` sebelum action panel
- Hapus `VBox.setVgrow(logScroll, ALWAYS)` — combat log fixed 130px

---

## [v0.2.7] — 2026-04-29

### Changed

**Comprehensive UI Pass — semua screen diperbesar**

| Elemen | Sebelum | Sesudah |
|--------|---------|---------|
| CSS root font | 13px | 14px |
| sectionTitle | 10px | 11px |
| vitalBar label | 10px | 11px |
| vitalBar height | 6px | 7px |
| Enemy card nama | 11px | 14px |
| Ally card lebar | 110px | 150px |
| Ally card nama | 9px | 12px |
| Skill slot size | 72×44px | 110×52px |
| Skill slot font | 8px | 11px |
| Action button lebar | 110px | 130px |
| Action button font | 11px | 13px |
| Combat log tinggi | 110px | 130px |
| Combat log font | 10px | 12px |
| CharCreate lore/bonus | 10px | 12px |

Semua screen: teks 9px→11px, 10px→12px, 8px→10px secara konsisten.

---

## [v0.2.6] — 2026-04-29

### Added

**Split Layout — Window 860×820px**
- Sebelumnya 420×820px. Sekarang: game area 560px (kiri) + chat panel 300px (kanan)
- Konstanta baru: `GAME_WIDTH=560`, `CHAT_WIDTH=300`
- Main Menu dan Char Create: full width. Screen lain: split layout

**MercChatPanel — panel chat mercenary (MercChatPanel.java)**
- Persistent di semua game screen (Hub, Dungeon, Combat, dll)
- Bubble chat dengan warna unik per merc:
  KiraVoss=ungu | TankRX9=cyan | SeraMend=hijau | Vector=merah |
  MagnusForge=orange | EchoNull=kuning | LyraBloom=pink
- Timestamp, fade-in animation, delay 800ms antar pesan, auto-scroll, max 40 pesan

**MercenaryDialogue — database 150+ dialog (MercenaryDialogue.java)**
- 7 mercenary × 17 trigger, masing-masing 3–5 variasi
- Kepribadian unik: KiraVoss=dingin | TankRX9=android formal | SeraMend=caring |
  Vector=sarkastis | MagnusForge=antusias | EchoNull=misterius | LyraBloom=spiritual
- Hanya 1–2 merc yang berbicara per trigger

**Chat triggers di semua screen**
- Hub: HUB_IDLE saat masuk, HUB_ENTER_DUNGEON saat enter dungeon
- Dungeon: trigger per room type yang dimasuki
- Combat: ENEMY_DIES, PLAYER_LOW_HP, VICTORY, DEFEAT

**CRAFT button → "Coming Soon" di chat panel**

### Changed

**SceneRouter — rewrite**
- `setSceneWithChat()`: game + chat panel sebagai HBox
- `setSceneFullWidth()`: full width (Main Menu, Char Create)
- Helper: `emitChat(trigger)` dan `addSystemChat(message)`

---

## [v0.2.5] — 2026-04-29

### Fixed

**REST room tidak bisa dikunjungi lagi setelah cleared**
- Guard `enterRoom()` dikecualikan untuk `Room.RoomType.REST`

**Skill slot selalu kosong saat combat**
- `createCharacter()` sekarang panggil `giveStarterSkills()` — auto-unlock 2 skill per background

### Added

**REST Room Diminishing Heal System**

| Kunjungan | HP Pulih | MP Pulih |
|-----------|----------|----------|
| 1 | +35% | +50% |
| 2 | +20% | +30% |
| 3 | +10% | +15% |
| 4+ | — | — |

Room.java: tambah `restUseCount`, `getRestUseCount()`, `incrementRestUse()`

**Starter Skills per Background**

| Background | Skill 1 | Skill 2 |
|-----------|---------|---------|
| Street Brawler | POWER_STRIKE | EXECUTE |
| Netrunner | DEEP_HACK | VIRUS_UPLOAD |
| Veteran Soldier | IRON_SHIELD | SHOCKWAVE |
| Energy Adept | ENERGY_DRAIN | BIO_IRRADIATE |
| Ghost Operative | PHANTOM_SHOT | SHADOW_STEP |
| Techwright | EMP_BURST | FIELD_BARRIER |

**Shop Fungsional — Basic**

| Rarity | Harga |
|--------|-------|
| Common | 30–50g |
| Uncommon | 80–120g |
| Rare | 200–300g |
| Epic | 500–700g |
| Legendary | 1200–1500g |

Tombol BUY disable jika gold kurang. Item langsung masuk inventory setelah beli.

---

## [v0.2.4] — 2026-04-29

### Fixed

**[CRITICAL] Backtrack ke tile visited re-trigger event**

Root cause:
1. `enterRoom()` tidak punya guard → handler selalu dipanggil ulang
2. `Floor.moveToRoom()` hanya izinkan gerakan ke `nextRoomIndexes` (tidak symmetric)

Fix:
1. Guard: `room.isCleared()` → emit `ROOM_ALREADY_CLEARED` dan return
2. `Floor.moveToRoom()`: cek A→B **atau** B→A (symmetric)

**ROOM_ALREADY_CLEARED tidak dihandle di wireEngineListeners**
- Sekarang punya handler eksplisit

### Changed

**buildCurrentRoomPanel — lebih informatif**
- Cleared room: badge ✓ CLEARED + deskripsi "You've been here before"
- Progress bar visual untuk room dikunjungi
- Boss defeated indicator

---

## [v0.2.3.1] — 2026-04-29

### Fixed

**[CRITICAL] ProceduralGenerator.java — konten file duplikat**
- Root cause: `str_replace` hanya ganti bagian awal, konten lama (baris 245–479) tetap ada
- Error: `class, interface, enum, or record expected` di baris 479
- Fix: potong file di baris 244, brace balance 30:30 ✓

---

## [v0.2.3] — 2026-04-29

### Added

**Dungeon Full Grid Exploration — ProceduralGenerator overhaul**

| Floor | Grid | Tile |
|-------|------|------|
| 1–3 | 3×5 | 15 |
| 4–8 | 4×5 | 20 |
| 9–15 | 5×5 | 25 |
| 16+ | 6×5 | 30 |

- Semua tile berisi event random
- Boss di tengah baris terakhir — wajib dikalahkan untuk DESCEND
- Koneksi cardinal: atas/bawah/kiri/kanan

**Fog of War 3 State**

| State | Tampilan |
|-------|---------|
| HIDDEN | Dot gelap — tidak ada info |
| VISIBLE | Adjacent ke visited — icon redup |
| VISITED | Sangat redup + ✓ jika cleared |
| CURRENT | Glow cyan |

**Koneksi Garis Cardinal H/V**
- Solid: visited–visited
- Putus-putus: ke tile visible
- Cyan: dari/ke current player

**DESCEND terkunci hingga Boss dikalahkan**
- `Floor.isBossDefeated()` method baru

**Map Legend** — icon + nama + warna tiap room type

### Changed

**DungeonGridMap — complete rewrite v1 → v2**
- Klik tile adjacent (cardinal) untuk bergerak
- Backtrack ke tile visited diperbolehkan
- Visual: fog of war, garis H/V, reachable dot

---

## [v0.2.2] — 2026-04-28

### Fixed

**[CRITICAL] ConcurrentModificationException di Entity.tickEffects()**
- Root cause: `receiveDamage()` dipanggil di dalam loop `activeEffects`
- Fix: iterate snapshot `new ArrayList<>(activeEffects)`, tambah `if (!alive) break`

**[WARNING] JavaFX native access warning**
- Tambah `--enable-native-access=javafx.graphics` ke pom.xml

### Added

**Dungeon Grid Map v1 — navigasi 2D (DungeonGridMap.java)**
- Canvas-based 2D grid, player ◈ bergerak dengan animasi ease in-out
- Fog of war dasar, klik tile → moveToRoom()

---

## [v0.2.1] — 2026-04-27

### Fixed

**[CRITICAL] Mercenary duplicate — muncul 2× TANK-RX9**
- Root cause: `createCharacter()` tidak clear list sebelum tambah starter merc
- Fix: `ownedMercs.clear()` + `activeMercs.clear()` di awal `createCharacter()`
- Bonus: TankRX9 otomatis masuk active party

**Background putih di item list dan ScrollPane**
- Item row: eksplisit `background-color: #050810`
- ScrollPane: tambah `-fx-background: #050810`

**Warna border equipment slot selalu hijau**
- Fix: gunakan `UIFactory.rarityColor()` per item yang diequip
- Border left 3px sebagai rarity indicator

**Teks title terlalu blur (ARCLIGHT, VICTORY, SYSTEM FAILURE)**
- DropShadow radius: 20 → 6, spread: 0.7 → 0.3

**Alert/Dialog masih style default JavaFX (putih)**
- Tambah CSS global `.dialog-pane` dark cyberpunk style

---

## [v0.2.0] — 2026-04-27

### Fixed

**[CRITICAL] AI Turn Loop Stacking (CombatView.java)**
- Guard `aiTurnPending` — Timeline tidak menumpuk
- `startCombatLoop()` delay 300ms sebelum turn pertama

**[CRITICAL] DungeonManager tidak di-reset antar run**
- `startDungeonRun()` buat `DungeonManager` baru setiap kali

**`refreshCurrentRoomInfo()` kosong** — body `{}` tidak melakukan apa-apa

**`wireEngineListeners()` tidak handle LOOT_FOUND, REST, ROOM_CLEARED**

**Loot Room tidak generate item ke inventory**
- `LOOT_FOUND` → `LootManager.generateLoot()` → push ke inventory

**InventoryView — tidak ada tombol EQUIP**

**Upgrade/Calibrate tidak disabled saat kondisi tidak memenuhi**

### Added

**Loot Popup** — daftar item + rarity tag saat masuk LOOT room
**Rest Room notifikasi popup**
**`showInfoAlert()` helper** — style cyberpunk konsisten

### Changed

**Inventory item row** — ringkasan 3 stat utama, USE button untuk consumable

### Known Issues
- Skill selection auto-pick skill pertama
- Target selection auto-target enemy pertama
- Shop placeholder "COMING SOON"

---

## [v0.1.0] — 2026-04-26

### Release Awal

Game bisa dijalankan end-to-end setelah migrasi NetBeans Ant → Maven.

Flow yang berjalan:
- Main Menu → Create Character → Hub
- Enter Dungeon → Navigate rooms
- Combat → Victory / Defeat
- Mercenary, Inventory, Profile screen

```bash
cd ArclightCity
mvn javafx:run
```

---

## [v0.3.1] — 2026-04-30

### Fixed

**[BUG] Combat screen terpotong — semua elemen tidak muat di 820px vertikal**
- Root cause: setelah v0.2.7 font naik + v0.3.0 tambah turn order bar,
  total tinggi combat (TopBar+TurnBar+Log+Enemy+Ally+Status+Action) melebihi 820px
- Fix: naikkan window dari **820px → 920px** (+100px vertikal)
- Cascade fix: `UIFactory.screenRoot()` dan `MercChatPanel` otomatis ikut
  karena keduanya reference `ArclightApp.SCREEN_HEIGHT`

**[BUG] Turn AI terlalu cepat — player tidak sempat baca log**
- Default `combatSpeedMs` 500ms terlalu pendek; action AI berganti
  sebelum player sempat membaca apa yang terjadi
- Fix: default naik dari **500ms → 1200ms** (nyaman dibaca)
- Speed button diupdate: `1×=1200ms`, `2×=500ms`, `SKIP=50ms`

**[BUG] Hub bottom nav masih bisa terpotong**
- Perbaikan residual dari v0.2.7.1 — dengan window 100px lebih tinggi,
  scrollable area hub mendapat ruang lebih sehingga bottom nav lebih mudah terlihat

### Changed

**Combat layout — lebih compact tanpa kehilangan readability**
- Enemy card spacing: 6 → 4, padding atas/bawah: 10 → 8px
- Ally card spacing: 6 → 4, padding atas/bawah: 10 → 8px
- Section padding (enemy/ally): 8 → 6px atas
- Action panel padding: 10 → 8px atas
- Combat log tinggi: 130 → 150px (memanfaatkan +100px window)
- Turn order bar: fixed height 36px, padding dikurangi 6→5px

**ArclightApp.SCREEN_HEIGHT: 820 → 920px**

---

## [v0.3.2] — 2026-04-30

### Fixed

**[BUG] Enemy card kosong saat target select mode (CombatView.java)**
- Root cause: blok target select mode memanggil `return card` SEBELUM
  `nameRow` dan `bars` ditambahkan ke card
  → card yang dikembalikan benar-benar kosong (hanya border kuning)
- Fix: tambahkan konten (label "▶ SELECT TARGET" + nameRow + bars)
  ke card SEBELUM return, sehingga nama musuh dan HP tetap terlihat
  saat player dalam mode pilih target

**[BUG] Hub bottom nav selalu tenggelam — definitif fix (HubView.java)**
- Root cause semua fix sebelumnya: menggunakan `VBox` sebagai root dengan
  `Priority.ALWAYS` pada ScrollPane. Masalahnya adalah VBox tidak menjamin
  child terakhir (`buildBottomNav()`) tetap visible jika total content
  melebihi window height — JavaFX bisa mengalokasikan space lebih ke
  ScrollPane dan mendorong nav keluar batas
- Fix definitif: ganti root dari `VBox` → `BorderPane`
  - `BorderPane.setTop()` → player bar (fixed)
  - `BorderPane.setCenter()` → ScrollPane dengan vitals + banner + nav buttons
  - `BorderPane.setBottom()` → bottom nav (SELALU fixed di bawah, tidak bisa tenggelam)
  - `BorderPane` oleh desainnya menjamin `bottom` selalu di posisi paling bawah
    dengan ukuran preferred-nya, center mengambil sisa space

### Changed

**Combat speed label di buildSpeedControl() lebih informatif**
- SKIP sebelumnya = 0ms (bisa infinite loop), sekarang minimum 50ms

---

## [v0.3.3] — 2026-04-30

### Fixed

**[CRITICAL] Penyesuaian layout menyeluruh — semua konten tidak lagi tenggelam**

Root cause akhir yang ditemukan:
- `UIFactory.screenRoot()` set `setMinSize(GAME_WIDTH, SCREEN_HEIGHT)` memaksa
  VBox root selalu minimal 920px — akibatnya total konten SELALU melebihi
  batas layar dan action panel/bottom nav tidak pernah terlihat
- Semua fix sebelumnya (setMaxHeight, ScrollPane, setMinHeight pada nav)
  tidak efektif karena root VBox sendiri dipaksa overflow

Fix menyeluruh:
1. `UIFactory.screenRoot()` — hapus `setMinSize`, tambah `setMaxHeight(SCREEN_HEIGHT)`
   agar VBox tidak bisa melebihi window
2. `UIFactory.screenRootBorder()` — helper baru untuk views yang butuh fixed bottom:
   BorderPane dengan `setPrefSize` dan `setMaxSize` sesuai window
3. `CombatView` → dikonversi ke `BorderPane`:
   - `setTop()`: TopBar + TurnOrderBar (fixed)
   - `setCenter()`: ScrollPane berisi CombatLog + EnemySection + AllySection + StatusPanel
   - `setBottom()`: ActionPanel (SELALU terlihat, tidak bisa tenggelam)
4. `DungeonMapView` → dikonversi ke `BorderPane`:
   - `setTop()`: Header + VitalsBar (fixed)
   - `setCenter()`: ScrollPane berisi GridMap + CurrentRoomPanel
   - `setBottom()`: RoomListContainer/DESCEND button (SELALU terlihat)
5. `wireEngineListeners()` dipindah dari constructor → dipanggil di `build()`
   agar field sudah ter-inisialisasi sebelum listener dipasang

Prinsip yang diterapkan secara konsisten:
- Header/TopBar = `BorderPane.top` → fixed
- Scrollable content = `BorderPane.center` → dapat semua sisa space
- Action/Nav/DESCEND = `BorderPane.bottom` → fixed, tidak bisa tenggelam

---

## [v0.3.4] — 2026-04-30

### Added

**[FEATURE] Save/Load System — Java Serialization (package arclightcity.save)**

Tiga file baru di package `arclightcity.save`:

**GameSaveState.java** — Data class serializable yang merupakan snapshot game:
- `PlayerData`: nama, background, level, exp, gold, HP/MP/Shield, skill IDs, depth
- `MercData`: merc type, loyalty, HP/MP/Shield, isActive
- `ItemData`: semua item termasuk stats, upgrade level, calibration, equipped slot
- `ProgressData`: floor terdalam, combat stats, playtime, timestamp save
- `saveId`: "MANUAL" atau "AUTO"
- `serialVersionUID = 20260430L` untuk compatibility check

**SaveManager.java** — Mengelola IO ke disk:
- `saveManual(state)` → simpan ke `save_manual.dat`
- `saveAuto(state)` → simpan ke `save_auto.dat`
- `loadLatest()` → load yang paling baru (manual vs auto berdasarkan timestamp)
- `hasSave()` → cek ketersediaan save untuk Main Menu
- `getSaveSummary()` → info singkat "Nama LV.X Floor Y [timestamp]"
- `deleteAllSaves()` → hapus semua save
- File lokasi: `%APPDATA%\ArclightCity\` (Windows) / `~/.arclight/` (Linux/Mac)
- Auto-backup file lama ke `.bak` sebelum overwrite

**GameStateConverter.java** — Konversi antara GameEngine ↔ GameSaveState:
- `toSaveState(engine, isAuto)` → snapshot engine ke save data
- `restoreFromSave(engine, save)` → rebuild engine dari save

**GameEngine.java — save/load integration:**
- `saveGame()` → manual save + log ke chat panel
- `autoSave()` → auto-save (dipanggil otomatis saat descend)
- `loadGame()` → load terbaru dan restore state
- `hasSave()`, `getSaveSummary()` → untuk Main Menu
- `createCharacterFromSave()` → restore player tanpa starter items
- `clearMercsForLoad()`, `addOwnedMercForLoad()`, `addActiveMercForLoad()` → helpers

**UI Integration:**
- Main Menu: tombol CONTINUE sekarang aktif jika ada save,
  tampilkan summary save di bawah tombol
- Hub: tombol SAVE GAME baru (kuning) — hasil save muncul di Merc Chat
- Auto-save: setiap kali `engine.descend()` dipanggil

**Player.java — setter untuk restore:**
- `setLevelDirect()`, `setExpDirect()`, `setGold()`, `setHpDirect()`,
  `setMpDirect()`, `setShieldDirect()`, `setSkillPointsDirect()`

**Inventory.java — forceEquip untuk restore:**
- `forceEquipWeapon()`, `forceEquipArmor()`,
  `forceEquipAccessory1()`, `forceEquipAccessory2()`

### Known Issues
- `getMaterials()` di Inventory mungkin perlu di-cek apakah sudah ada
- GameStateConverter belum handle semua edge case Equipment reconstruct
  (calibration re-apply tidak 100% deterministic karena RNG)
