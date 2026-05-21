📋 CHANGELOG — Mythic Item Obtained
Semua perubahan signifikan dicatat di sini.
Format: [versi] (tanggal)

🔴 Critical Fix 🐛 Bug Fix ✨ Feature ⚡ Improvement 🎨 Visual 🔧 Refactor
[v1.0.0] — 2026-05-19 · FULL RELEASE 🎉

✨ Feature
Artifact Pocket — storage terpisah dari tas utama, tidak makan slot bag, unlimited kapasitas. Inventory.addItem(Artifact) selalu masuk pocket, tidak pernah ke bag
Artifact slot repositioning — Slot ARTEFAK 1 dan ARTEFAK 2 dipindah ke KIRI dan KANAN grid equipment (sesuai desain awal), bukan di bawah accessories
Auto-save setelah gacha — setiap pull berhasil trigger autoSave(), mencegah reload & gacha ulang (save-scumming)
Artifact pocket tersave — GameSaveState.savedArtifactPocket menyimpan [typeName, rarityName][], di-restore via ArtifactType.valueOf()

🐛 Bug Fix
Inventory.unequip(slot) tidak lagi cek bag full — item dari slot harus bisa dilepas kapanpun
maxBagSize dinaikkan dari 30 → 60
getArtifactPocket() getter ditambahkan ke Inventory
getLevel() dihapus dari save code (Artifact tidak punya field level)

🎨 Visual
Gacha animasi 3 fase (±2.6 detik total):
Fase 1 (0–0.8s): portal scale-in + background gelap
Fase 2 (0.8–2.2s): portal membesar 1.6× + 4 cincin berputar berlawanan + flash purple
Fase 3 (2.2–2.6s): fade out → slide reveal
4-ring portal dengan kecepatan rotasi berbeda (300/500/900/1400ms)
[v0.9.9] — 2026-05-19

✨ Feature
Gacha animasi loading — portal berputar cepat + teks "MEMANGGIL..." selama 1.2 detik sebelum reveal hasil
buildAnimPortal() — portal terpisah untuk animasi loading

🐛 Bug Fix
Artifact slot repositioning awal (sebelum fix final v1.0.0)
showArtifactBagPopup diubah ke private static dengan parameter SceneRouter router
[v0.9.8] — 2026-05-19# 📋 CHANGELOG — Mythic Item Obtained

> Semua perubahan signifikan dicatat di sini.
> Format: **[versi] (tanggal)**
> `🔴 Critical Fix` `🐛 Bug Fix` `✨ Feature` `⚡ Improvement` `🎨 Visual` `🔧 Refactor`

---

## [v1.0.0] — 2026-05-19 · FULL RELEASE 🎉

### ✨ Feature
- **Artifact Pocket** — storage terpisah dari tas utama, tidak makan slot bag, unlimited kapasitas. `Inventory.addItem(Artifact)` selalu masuk pocket, tidak pernah ke bag
- **Artifact slot repositioning** — Slot ARTEFAK 1 dan ARTEFAK 2 dipindah ke KIRI dan KANAN grid equipment (sesuai desain awal), bukan di bawah accessories
- **Auto-save setelah gacha** — setiap pull berhasil trigger `autoSave()`, mencegah reload & gacha ulang (save-scumming)
- **Artifact pocket tersave** — `GameSaveState.savedArtifactPocket` menyimpan `[typeName, rarityName][]`, di-restore via `ArtifactType.valueOf()`

### 🐛 Bug Fix
- `Inventory.unequip(slot)` tidak lagi cek bag full — item dari slot harus bisa dilepas kapanpun
- `maxBagSize` dinaikkan dari 30 → 60
- `getArtifactPocket()` getter ditambahkan ke `Inventory`
- `getLevel()` dihapus dari save code (Artifact tidak punya field level)

### 🎨 Visual
- **Gacha animasi 3 fase** (±2.6 detik total):
  - Fase 1 (0–0.8s): portal scale-in + background gelap
  - Fase 2 (0.8–2.2s): portal membesar 1.6× + 4 cincin berputar berlawanan + flash purple
  - Fase 3 (2.2–2.6s): fade out → slide reveal
- 4-ring portal dengan kecepatan rotasi berbeda (300/500/900/1400ms)

---

## [v0.9.9] — 2026-05-19

### ✨ Feature
- **Gacha animasi loading** — portal berputar cepat + teks "MEMANGGIL..." selama 1.2 detik sebelum reveal hasil
- `buildAnimPortal()` — portal terpisah untuk animasi loading

### 🐛 Bug Fix
- Artifact slot repositioning awal (sebelum fix final v1.0.0)
- `showArtifactBagPopup` diubah ke `private static` dengan parameter `SceneRouter router`

---

## [v0.9.8] — 2026-05-19

### 🐛 Bug Fix
- **LEPAS item tidak berfungsi** — root cause: `Inventory.unequip(slot)` gagal jika bag penuh (32/30). Item dari slot selalu bisa dilepas, tidak seharusnya cek bag capacity
- **Artifact dari gacha tidak masuk bag** — sama root cause: bag full → `addItem()` return false
- Artifact slot di `ViewsBundle` (PERBENDAHARAAN) mengambil dari bag bukan player slots
- `filters[]` array tidak sinkron dengan `labels[]` setelah tambah ARTEFAK tab

### ✨ Feature
- **Tab ARTEFAK** di inventory filter (ViewsBundle) — `filters: "ARTIFACT"` → tampilkan `instanceof Artifact`
- **`showArtifactBagPopup()`** — popup klik artifact dari bag: icon besar, info buff, CD, tombol → SLOT 1 / → SLOT 2
- **`showArtifactSlotInfo()`** — popup klik slot terisi: info + LEPAS ARTEFAK

---

## [v0.9.7] — 2026-05-18

### 🔴 Bug Fix Kritis
- **Lore trigger salah** — `isCurrentRoomBoss()` cek `floor number` (return true untuk SEMUA room di floor 10/20/30/40/50). Sekarang cek `room.getType() == RoomType.BOSS` — cutscene hanya trigger saat masuk Boss Room
- `double-brace initialization` pada `FadeTransition` / `ScaleTransition` (keduanya `final class`) — diganti ke variabel biasa

### ✨ Feature
- **Artifact icons di combat** — `buildArtifactIconRow()` di `CombatView`: icon 36×36 per artifact, CD indicator, glow saat SIAP
- **Gacha full-screen slide reveal** — pull → full-screen overlay → kartu muncul satu per satu (rarest first), auto-advance 1.8s, klik/SPASI skip, summary grid setelah semua
- **Merc artifact slot accessible** — `buildMercArtifactSlot()` sekarang ambil dari bag dengan role filter

---

## [v0.9.6] — 2026-05-18

### 🔴 Bug Fix Kritis
- **Map "dua pulau" bug** — root cause: `DungeonGridMap.initFog()` dan `refresh()` pakai `r.isVisited()` → visited-tapi-belum-cleared rooms ikut revealed walau player sudah jauh
  - **Fix**: keduanya diubah ke `r.isCleared()` — hanya cleared rooms yang masuk `visitedTiles` (permanent solid)
  - `syncPlayer()`: `visibleTiles.clear()` dan rebuild dari current + cleared rooms' neighbors → map selalu connected

### 🎵 Audio
- **BGM error fix** — JavaFX MediaPlayer Windows tidak support OGG format. Semua 15 file OGG dikonversi ke MP3 via FFmpeg. `AudioManager` constants diupdate ke `.mp3`

### 🐛 Bug Fix
- `DungeonManager` hub-return path: `setCurrentRoom(st)` sebelum `emit(floorEntered)` agar `syncPlayer()` baca posisi yang benar

---

## [v0.9.5] — 2026-05-18

### 🔴 Bug Fix Kritis
- **Map player selalu return ke room 0** — dua bug terpisah:
  1. `private enterRoom()` tidak memanggil `currentFloor.setCurrentRoom()` → `getCurrentRoomIndex()` selalu 0
  2. `advanceToNextFloor()` emit `floorEntered` SEBELUM `setCurrentRoom(startTile)` → map dibangun dengan posisi salah
  - **Fix**: `enterRoom()` sekarang selalu panggil `setCurrentRoom(roomIndex)`. Order diubah: `setCurrentRoom` → emit → `enterRoom`
- **Floor.setCurrentRoom(int)** ditambahkan — set posisi tanpa connectivity check (untuk inisialisasi)

### 🎵 Audio
- **BGM lengkap di SceneRouter**: showCity → BGM_SHOP, showVictory → sfxVictory + BGM_HUB, showGameOver → sfxGameOver + stopBgm, playCutscene → BGM_CUTSCENE_OP/END
- **Boss BGM otomatis** — `engine.getCombatBgm()` deteksi floor/boss type → Semar, Theresa masing-masing BGM sendiri

---

## [v0.9.4] — 2026-05-18

### 🎵 Audio System (AudioManager.java)
- BGM 55% volume dengan fade-in/out smooth (thread background)
- SFX 60% volume via `AudioClip` fire-and-forget
- 15 file BGM: menu, hub, shop, dungeon×5, combat, elite, boss×2, theresa, cutscene×2
- 8 file SFX: hit physical/cyber/energy, critical, heal, miss, victory, gameover
- `AudioManager.bgmForFloor(int)` → auto-pilih tema per 10 lantai
- Wired ke SceneRouter: showMainMenu, showHub, showDungeonMap, showCombat, showGacha
- SFX di CombatManager: hit type, critical (via `calc.isCritical`, `calc.isMissed`)

### 🐛 Bug Fix
- `calc.missed` → `calc.isMissed` (field name yang benar di `DamageCalcResult`)

---

## [v0.9.3] — 2026-05-17

### 🔴 Bug Fix Kritis
- **Guildmate tetap mati setelah return hub** — `Entity.setHpDirect()` tidak reset `alive` flag. Ditambahkan `setHpDirect()` dengan `this.alive = this.currentHp > 0` dan `revive(hpPct)` yang paksa `alive = true`
- `GameEngine.returnToHub()` sekarang panggil `m.revive(0.30)` bukan `setHpDirect()`

### ⚡ Improvement
- `recalcEquipStats()` dipanggil di 3 tempat: `startDungeonRun()`, `returnToHub()`, `GameStateConverter.restoreFromSave()` — fix equipment stat tidak teraplikasi setelah load save lama
- BLEED/BURN/POISON on-hit: chance capped `Math.min(chance, 1.0)`, excess ×multiplier = bonus DoT
- `GameStateConverter.restoreFromSave()` tambah `recalcEquipStats()` setelah restore

### 🎨 Visual
- **Main menu fix** — `BorderPane.setBottom()` guarantee posisi tombol di bawah logo (tidak overlap)
- **20 artifact icons** di-import ke `assets/icons/artifact/`
- `AssetManager.artifactIcon(ArtifactType)` — load icon berdasarkan enum name

---

## [v0.9.2] — 2026-05-17

### 🔴 Bug Fix Kritis
- `Entity.revive(double)` tidak ditemukan — spacing mismatch pada string replacement. Ditambahkan manual dengan `str_replace`
- `playerArtifacts` dan `mercArtifacts` fields tidak terdeklare di `CombatManager` (pattern tidak match)

### ✨ Feature
- **Artifact slot UI di ProfileView** — 2 slot 140×140, empty = kotak `+`, filled = icon artifact + CD
- Klik empty → `showArtifactPickPopup()` (filter dari bag)
- Klik filled → `showArtifactInfoPopup()` (icon besar + buff desc + LEPAS)
- **Artifact auto-trigger** — `CombatManager.tickAllArtifactCooldowns()` + `autoTriggerReadyArtifacts()` setelah setiap giliran, tanpa memakan turn
- `GameEngine.syncArtifactsToCombat()` — pass artifact ke CombatManager saat showCombat

---

## [v0.9.1] — 2026-05-17

### ✨ Feature (Gacha System)
- `GachaSystem.java` — pull logic: pity 80, rates MYTHIC/LEGENDARY/EPIC/RARE/UNCOMMON/COMMON
- `Artifact.java` — extends Item, CD system (`tickCooldown`, `activate`, `isReady`), scaling per rarity
- `ArtifactType.java` — 20 jenis dengan mode, role, baseValue, baseCooldown
- `ArtifactRole.java` — UNIVERSAL, TANK, HEALER, DPS, SUPPORT, ASSASSIN, BREAKER
- `GachaView.java` — Altar Artefak dengan portal animasi + kartu hasil
- `Inventory` fields: `artifactSlot1`, `artifactSlot2`, `equipArtifactToSlot()`, `unequipArtifact()`
- `Mercenary.equippedArtifact` field + `equipArtifact()`, `unequipArtifact()`
- `Item.ItemType.ARTIFACT`, `Material.MaterialType.GACHA_TICKET`

---

## [v0.9.0] — 2026-05-17

### 🔴 Bug Fix Kritis
- **Map fog of war "dua pulau" versi 1** — `initFog()` reveal room 0 saat floor pertama dibuat sebelum player diposisikan. `advanceToNextFloor()` sekarang pre-set `currentFloor.setCurrentRoom(startTile)` sebelum emit
- **Consumable +0 HP display** — ANTIDOTE type (effectValue=0) sekarang tampil "Cleanse Debuff", bukan "+0 HP"

### ✨ Feature
- `HubView` ditambahkan tombol **⬡ ALTAR ARTEFAK** menuju GachaView
- `DungeonManager.isCurrentRoomBoss()` awal (masih floor-based, diperbaiki di v0.9.7)

---

## [v0.8.9a] — 2026-05-17

### 🐛 Bug Fix
- `CombatManager.setArtifacts()` — `playerArtifacts` dan `mercArtifacts` tidak terdeklare (fixed placement di class body)
- `Inventory.equipArtifactToSlot(slot, art)` ditambahkan

---

## [v0.8.9] — 2026-05-16

### ✨ Feature
- **Dialog system P5-style** (CutsceneView.java) — portrait kiri, dialog bawah, pilihan kanan
  - `AnchorPane.setBottomAnchor(dialogPanel, 0.0)` — guaranteed bottom positioning
  - `fadeBlack.setMouseTransparent(true)` — KRITIS agar QTE bisa di-klik
  - `e.consume()` di choice cells — cegah event bubble
- **21 dialog script** di `DialogScript.java`
- `DialogBeat.java` — record: bg, portrait, speaker, text, choices, title card
- Trigger cutscene otomatis saat `FLOOR_ENTERED` pada milestone floor

---

## [v0.8.8] — 2026-05-16

### ✨ Feature
- **Save/Load system** — Java Serialization, simpan ke `%APPDATA%\MythicItemObtained\`
- 3 slot manual (`save_manual_1/2/3.dat`) + 1 auto (`save_auto.dat`)
- `GameSaveState implements Serializable` — menyimpan player, merc, inventory, progress
- `GameStateConverter.toSaveState()` + `restoreFromSave()`
- `SaveLoadView.java` — UI 3 slot dengan preview info
- Auto-save setelah setiap room cleared

---

## [v0.8.7] — 2026-05-16

### ⚡ Improvement
- **Mercenary dialog system** — 50+ baris dialog dinamis per guildmate
- `MercChatPanel.java` — panel kanan dengan chat bubble per guildmate
- `MercenaryDialogue.java` — 50+ dialog contextual per situasi
- `GuildmateStatus` tracking — online, exploring, resting

---

## [v0.8.6] — 2026-05-16

### ✨ Feature
- **CombatView** — turn order bar visual, sprite battle, floating damage text
- Speed control: 1× / 2× / SKIP
- Status effect badges di entity card
- Skill popup dengan icon + deskripsi + cooldown
- `buildEnemyCard()` — sprite + HP bar + status effects

---

## [v0.8.5] — 2026-05-16

### ✨ Feature
- **Dungeon grid map** — canvas 12×N tile, tile icons per RoomType
- Fog of war awal: `visitedTiles` + `visibleTiles` set
- Player indicator (cyan circle) bergerak saat explore
- Tile click handler → `engine.enterRoom(roomIndex)`
- Room legend di footer

---

## [v0.8.4] — 2026-05-14

### 🐛 Bug Fix
- `DungeonManager.enterRoom()` — room tidak ter-mark visited jika langsung di-enter tanpa click
- `Floor.moveToRoom()` — connectivity check terlalu ketat, blok inisialisasi start tile

### ⚡ Improvement
- `ProceduralGenerator` — path generator lebih reliable, boss room selalu terhubung

---

## [v0.8.3a] — 2026-05-14

### 🐛 Bug Fix
- Asset audio OGG tidak ter-bundle di Maven resources — tambah resource config
- `AssetManager.load()` fallback ke `getClassLoader()` jika `getResource()` null

---

## [v0.8.3] — 2026-05-14

### ✨ Feature
- Lore cutscene assets: 33 background art, 13 lore dialog bg
- Video cutscene support via JavaFX `MediaView`
- `DungeonStateEvent` — event system untuk DungeonManager → DungeonMapView

---

## [v0.8.2] — 2026-05-14

### ✨ Feature
- **DungeonManager.java** — state machine IDLE → EXPLORING → IN_COMBAT → FLOOR_COMPLETE
- `ProceduralGenerator.java` — floor generation 12×N grid, 5 tema per range 10 lantai
- `Floor.java` + `Room.java` — model data lantai dan room
- `DungeonEvent.java` — event types: combat, loot, shop, rest, trap
- `CombatResult.java` — WIN, LOSE, FLEE dengan loot + XP data

---

## [v0.8.1a] — 2026-05-14

### 🐛 Bug Fix
- `DungeonManager` — variable `maxHp` duplikat dalam `onFloorCompleted()` pada scope berbeda; rename ke `mercMaxHp`

---

## [v0.8.1] — 2026-05-14

### 🔴 Bug Fix Kritis
- **Merc HP dipaksa ke 25% setiap ganti lantai** — `advanceFloor()` memanggil `restoreVitals(maxHp * 0.25)` yang override HP. Diganti jadi regen +15% MAX_HP
- **Kalibrasi hasilkan stat kecil untuk stat persentase** — `calibrate()` pakai formula absolut untuk LIFESTEAL, CRIT_CHANCE. Sekarang ada skala terpisah: stat % → 0.02–0.25, stat absolut → 15–60
- **Tema dungeon acak 20% chance** — dihapus; tema 100% konsisten per range lantai

### ✨ Feature
- **Dungeon grid 12 kolom** — dari COLS=5 ke COLS=12
- Player start tile acak dari edge grid
- Ring & Aksesori slot picker saat kedua slot penuh
- Workshop redesign — tombol `⬆ UPGRADE` + `◈ KALIBRASI` per item card
- `AssetManager` fallback mapping lengkap untuk semua sprite

---

## [v0.8.0a] — 2026-05-14

### 🐛 Bug Fix
- `LootManager` — `floorTier` out of scope dalam `generateWeapon()`

---

## [v0.8.0] — 2026-05-14

### 🔴 Bug Fix Kritis
- CRIT_DMG_MULT → CRIT_DAMAGE (StatType rename)
- CDR → COOLDOWN_REDUCE (StatType rename)
- DMG_MULT → DAMAGE_MULT (StatType rename)
- `cm.getEnemies()` → `cm.getAllEnemies()` (method rename)

### 🔧 Refactor
- `UIFactory.screenRoot()` tidak lagi panggil `setMinSize()` — fix overflow/sinking bug
- `UIFactory.screenRootBorder()` ditambahkan untuk split-layout views
- `stage.setHeight()` dihapus, ganti ke `stage.sizeToScene()`

---

## [v0.7.9] — 2026-05-14

### ✨ Feature
- CombatManager — observer pattern, `addEventListener(Consumer<CombatEvent>)`
- `DamageCalculator.java` — formula lengkap: DEF reduction, armor pierce, crit, min damage
- `SkillExecutor.java` — 40+ skill execution
- `TurnQueue.java` — priority queue berdasarkan SPEED stat
- `CombatEvent.java` — 15+ event types

---

> Untuk history lengkap sebelum v0.7.9, lihat git log.
>
> *Mythic Item Obtained · v1.0.0 · Full Release · Kelompok 7 · BismilahFIX*

🐛 Bug Fix
LEPAS item tidak berfungsi — root cause: Inventory.unequip(slot) gagal jika bag penuh (32/30). Item dari slot selalu bisa dilepas, tidak seharusnya cek bag capacity
Artifact dari gacha tidak masuk bag — sama root cause: bag full → addItem() return false
Artifact slot di ViewsBundle (PERBENDAHARAAN) mengambil dari bag bukan player slots
filters[] array tidak sinkron dengan labels[] setelah tambah ARTEFAK tab
✨ Feature
Tab ARTEFAK di inventory filter (ViewsBundle) — filters: "ARTIFACT" → tampilkan instanceof Artifact
showArtifactBagPopup() — popup klik artifact dari bag: icon besar, info buff, CD, tombol → SLOT 1 / → SLOT 2
showArtifactSlotInfo() — popup klik slot terisi: info + LEPAS ARTEFAK
[v0.9.7] — 2026-05-18
🔴 Bug Fix Kritis
Lore trigger salah — isCurrentRoomBoss() cek floor number (return true untuk SEMUA room di floor 10/20/30/40/50). Sekarang cek room.getType() == RoomType.BOSS — cutscene hanya trigger saat masuk Boss Room
double-brace initialization pada FadeTransition / ScaleTransition (keduanya final class) — diganti ke variabel biasa
✨ Feature
Artifact icons di combat — buildArtifactIconRow() di CombatView: icon 36×36 per artifact, CD indicator, glow saat SIAP
Gacha full-screen slide reveal — pull → full-screen overlay → kartu muncul satu per satu (rarest first), auto-advance 1.8s, klik/SPASI skip, summary grid setelah semua
Merc artifact slot accessible — buildMercArtifactSlot() sekarang ambil dari bag dengan role filter
[v0.9.6] — 2026-05-18
🔴 Bug Fix Kritis
Map "dua pulau" bug — root cause: DungeonGridMap.initFog() dan refresh() pakai r.isVisited() → visited-tapi-belum-cleared rooms ikut revealed walau player sudah jauh
Fix: keduanya diubah ke r.isCleared() — hanya cleared rooms yang masuk visitedTiles (permanent solid)
syncPlayer(): visibleTiles.clear() dan rebuild dari current + cleared rooms' neighbors → map selalu connected
🎵 Audio
BGM error fix — JavaFX MediaPlayer Windows tidak support OGG format. Semua 15 file OGG dikonversi ke MP3 via FFmpeg. AudioManager constants diupdate ke .mp3
🐛 Bug Fix
DungeonManager hub-return path: setCurrentRoom(st) sebelum emit(floorEntered) agar syncPlayer() baca posisi yang benar
[v0.9.5] — 2026-05-18
🔴 Bug Fix Kritis
Map player selalu return ke room 0 — dua bug terpisah:
private enterRoom() tidak memanggil currentFloor.setCurrentRoom() → getCurrentRoomIndex() selalu 0
advanceToNextFloor() emit floorEntered SEBELUM setCurrentRoom(startTile) → map dibangun dengan posisi salah
Fix: enterRoom() sekarang selalu panggil setCurrentRoom(roomIndex). Order diubah: setCurrentRoom → emit → enterRoom
Floor.setCurrentRoom(int) ditambahkan — set posisi tanpa connectivity check (untuk inisialisasi)
🎵 Audio
BGM lengkap di SceneRouter: showCity → BGM_SHOP, showVictory → sfxVictory + BGM_HUB, showGameOver → sfxGameOver + stopBgm, playCutscene → BGM_CUTSCENE_OP/END
Boss BGM otomatis — engine.getCombatBgm() deteksi floor/boss type → Semar, Theresa masing-masing BGM sendiri
[v0.9.4] — 2026-05-18
🎵 Audio System (AudioManager.java)
BGM 55% volume dengan fade-in/out smooth (thread background)
SFX 60% volume via AudioClip fire-and-forget
15 file BGM: menu, hub, shop, dungeon×5, combat, elite, boss×2, theresa, cutscene×2
8 file SFX: hit physical/cyber/energy, critical, heal, miss, victory, gameover
AudioManager.bgmForFloor(int) → auto-pilih tema per 10 lantai
Wired ke SceneRouter: showMainMenu, showHub, showDungeonMap, showCombat, showGacha
SFX di CombatManager: hit type, critical (via calc.isCritical, calc.isMissed)
🐛 Bug Fix
calc.missed → calc.isMissed (field name yang benar di DamageCalcResult)
[v0.9.3] — 2026-05-17
🔴 Bug Fix Kritis
Guildmate tetap mati setelah return hub — Entity.setHpDirect() tidak reset alive flag. Ditambahkan setHpDirect() dengan this.alive = this.currentHp > 0 dan revive(hpPct) yang paksa alive = true
GameEngine.returnToHub() sekarang panggil m.revive(0.30) bukan setHpDirect()
⚡ Improvement
recalcEquipStats() dipanggil di 3 tempat: startDungeonRun(), returnToHub(), GameStateConverter.restoreFromSave() — fix equipment stat tidak teraplikasi setelah load save lama
BLEED/BURN/POISON on-hit: chance capped Math.min(chance, 1.0), excess ×multiplier = bonus DoT
GameStateConverter.restoreFromSave() tambah recalcEquipStats() setelah restore
🎨 Visual
Main menu fix — BorderPane.setBottom() guarantee posisi tombol di bawah logo (tidak overlap)
20 artifact icons di-import ke assets/icons/artifact/
AssetManager.artifactIcon(ArtifactType) — load icon berdasarkan enum name
[v0.9.2] — 2026-05-17
🔴 Bug Fix Kritis
Entity.revive(double) tidak ditemukan — spacing mismatch pada string replacement. Ditambahkan manual dengan str_replace
playerArtifacts dan mercArtifacts fields tidak terdeklare di CombatManager (pattern tidak match)
✨ Feature
Artifact slot UI di ProfileView — 2 slot 140×140, empty = kotak +, filled = icon artifact + CD
Klik empty → showArtifactPickPopup() (filter dari bag)
Klik filled → showArtifactInfoPopup() (icon besar + buff desc + LEPAS)
Artifact auto-trigger — CombatManager.tickAllArtifactCooldowns() + autoTriggerReadyArtifacts() setelah setiap giliran, tanpa memakan turn
GameEngine.syncArtifactsToCombat() — pass artifact ke CombatManager saat showCombat
[v0.9.1] — 2026-05-17
✨ Feature (Gacha System)
GachaSystem.java — pull logic: pity 80, rates MYTHIC/LEGENDARY/EPIC/RARE/UNCOMMON/COMMON
Artifact.java — extends Item, CD system (tickCooldown, activate, isReady), scaling per rarity
ArtifactType.java — 20 jenis dengan mode, role, baseValue, baseCooldown
ArtifactRole.java — UNIVERSAL, TANK, HEALER, DPS, SUPPORT, ASSASSIN, BREAKER
GachaView.java — Altar Artefak dengan portal animasi + kartu hasil
Inventory fields: artifactSlot1, artifactSlot2, equipArtifactToSlot(), unequipArtifact()
Mercenary.equippedArtifact field + equipArtifact(), unequipArtifact()
Item.ItemType.ARTIFACT, Material.MaterialType.GACHA_TICKET
[v0.9.0] — 2026-05-17
🔴 Bug Fix Kritis
Map fog of war "dua pulau" versi 1 — initFog() reveal room 0 saat floor pertama dibuat sebelum player diposisikan. advanceToNextFloor() sekarang pre-set currentFloor.setCurrentRoom(startTile) sebelum emit
Consumable +0 HP display — ANTIDOTE type (effectValue=0) sekarang tampil "Cleanse Debuff", bukan "+0 HP"
✨ Feature
HubView ditambahkan tombol ⬡ ALTAR ARTEFAK menuju GachaView
DungeonManager.isCurrentRoomBoss() awal (masih floor-based, diperbaiki di v0.9.7)
[v0.8.9a] — 2026-05-17
🐛 Bug Fix
CombatManager.setArtifacts() — playerArtifacts dan mercArtifacts tidak terdeklare (fixed placement di class body)
Inventory.equipArtifactToSlot(slot, art) ditambahkan
[v0.8.9] — 2026-05-16
✨ Feature
Dialog system P5-style (CutsceneView.java) — portrait kiri, dialog bawah, pilihan kanan
AnchorPane.setBottomAnchor(dialogPanel, 0.0) — guaranteed bottom positioning
fadeBlack.setMouseTransparent(true) — KRITIS agar QTE bisa di-klik
e.consume() di choice cells — cegah event bubble
21 dialog script di DialogScript.java
DialogBeat.java — record: bg, portrait, speaker, text, choices, title card
Trigger cutscene otomatis saat FLOOR_ENTERED pada milestone floor
[v0.8.8] — 2026-05-16
✨ Feature
Save/Load system — Java Serialization, simpan ke %APPDATA%\MythicItemObtained\
3 slot manual (save_manual_1/2/3.dat) + 1 auto (save_auto.dat)
GameSaveState implements Serializable — menyimpan player, merc, inventory, progress
GameStateConverter.toSaveState() + restoreFromSave()
SaveLoadView.java — UI 3 slot dengan preview info
Auto-save setelah setiap room cleared
[v0.8.7] — 2026-05-16
⚡ Improvement
Mercenary dialog system — 50+ baris dialog dinamis per guildmate
MercChatPanel.java — panel kanan dengan chat bubble per guildmate
MercenaryDialogue.java — 50+ dialog contextual per situasi
GuildmateStatus tracking — online, exploring, resting
[v0.8.6] — 2026-05-16
✨ Feature
CombatView — turn order bar visual, sprite battle, floating damage text
Speed control: 1× / 2× / SKIP
Status effect badges di entity card
Skill popup dengan icon + deskripsi + cooldown
buildEnemyCard() — sprite + HP bar + status effects
[v0.8.5] — 2026-05-16
✨ Feature
Dungeon grid map — canvas 12×N tile, tile icons per RoomType
Fog of war awal: visitedTiles + visibleTiles set
Player indicator (cyan circle) bergerak saat explore
Tile click handler → engine.enterRoom(roomIndex)
Room legend di footer
[v0.8.4] — 2026-05-14
🐛 Bug Fix
DungeonManager.enterRoom() — room tidak ter-mark visited jika langsung di-enter tanpa click
Floor.moveToRoom() — connectivity check terlalu ketat, blok inisialisasi start tile
⚡ Improvement
ProceduralGenerator — path generator lebih reliable, boss room selalu terhubung
[v0.8.3a] — 2026-05-14
🐛 Bug Fix
Asset audio OGG tidak ter-bundle di Maven resources — tambah resource config
AssetManager.load() fallback ke getClassLoader() jika getResource() null
[v0.8.3] — 2026-05-14
✨ Feature
Lore cutscene assets: 33 background art, 13 lore dialog bg
Video cutscene support via JavaFX MediaView
DungeonStateEvent — event system untuk DungeonManager → DungeonMapView
[v0.8.2] — 2026-05-14
✨ Feature
DungeonManager.java — state machine IDLE → EXPLORING → IN_COMBAT → FLOOR_COMPLETE
ProceduralGenerator.java — floor generation 12×N grid, 5 tema per range 10 lantai
Floor.java + Room.java — model data lantai dan room
DungeonEvent.java — event types: combat, loot, shop, rest, trap
CombatResult.java — WIN, LOSE, FLEE dengan loot + XP data
[v0.8.1a] — 2026-05-14
🐛 Bug Fix
DungeonManager — variable maxHp duplikat dalam onFloorCompleted() pada scope berbeda; rename ke mercMaxHp
[v0.8.1] — 2026-05-14
🔴 Bug Fix Kritis
Merc HP dipaksa ke 25% setiap ganti lantai — advanceFloor() memanggil restoreVitals(maxHp * 0.25) yang override HP. Diganti jadi regen +15% MAX_HP
Kalibrasi hasilkan stat kecil untuk stat persentase — calibrate() pakai formula absolut untuk LIFESTEAL, CRIT_CHANCE. Sekarang ada skala terpisah: stat % → 0.02–0.25, stat absolut → 15–60
Tema dungeon acak 20% chance — dihapus; tema 100% konsisten per range lantai
✨ Feature
Dungeon grid 12 kolom — dari COLS=5 ke COLS=12
Player start tile acak dari edge grid
Ring & Aksesori slot picker saat kedua slot penuh
Workshop redesign — tombol ⬆ UPGRADE + ◈ KALIBRASI per item card
AssetManager fallback mapping lengkap untuk semua sprite
[v0.8.0a] — 2026-05-14
🐛 Bug Fix
LootManager — floorTier out of scope dalam generateWeapon()
[v0.8.0] — 2026-05-14
🔴 Bug Fix Kritis
CRIT_DMG_MULT → CRIT_DAMAGE (StatType rename)
CDR → COOLDOWN_REDUCE (StatType rename)
DMG_MULT → DAMAGE_MULT (StatType rename)
cm.getEnemies() → cm.getAllEnemies() (method rename)
🔧 Refactor
UIFactory.screenRoot() tidak lagi panggil setMinSize() — fix overflow/sinking bug
UIFactory.screenRootBorder() ditambahkan untuk split-layout views
stage.setHeight() dihapus, ganti ke stage.sizeToScene()
[v0.7.9] — 2026-05-14
✨ Feature
CombatManager — observer pattern, addEventListener(Consumer<CombatEvent>)
DamageCalculator.java — formula lengkap: DEF reduction, armor pierce, crit, min damage
SkillExecutor.java — 40+ skill execution
TurnQueue.java — priority queue berdasarkan SPEED stat
CombatEvent.java — 15+ event types
Untuk history lengkap sebelum v0.7.9, lihat git log.
Mythic Item Obtained · v1.0.0 · Full Release · Kelompok 7 · BismilahFIX
