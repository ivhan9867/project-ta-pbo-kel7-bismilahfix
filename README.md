<div align="center">

# ⬡ MYTHIC ITEM OBTAINED

### ✦ Roguelite RPG Turn-Based · Nusantara Dark Fantasy ✦

[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-25-1E90FF?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Version](https://img.shields.io/badge/Version-v1.0.4-8A2BE2?style=for-the-badge)](/)
[![Status](https://img.shields.io/badge/Status-Full%20Release-44AA44?style=for-the-badge)](/)
[![Platform](https://img.shields.io/badge/Platform-Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white)](/)

---

*Tugas Akhir Pemrograman Berorientasi Objek 2026 · Kelompok 7 · BismilahFIX*

</div>

---

## 📖 Daftar Isi

| # | Bagian | Isi |
|---|--------|-----|
| 1 | [🎮 Tentang Game](#-tentang-game) | Overview, genre, lore, tech stack |
| 2 | [✨ Fitur Lengkap](#-fitur-lengkap) | Combat, dungeon, gacha, lore, UI |
| 3 | [📊 Statistik Proyek](#-statistik-proyek) | Angka-angka impressive |
| 4 | [🏗️ Arsitektur OOP](#️-arsitektur-oop) | Class hierarchy, patterns |
| 5 | [📋 CHANGELOG LENGKAP](#-changelog-lengkap) | v0.7.9 → v1.0.4, detail tiap versi |
| 6 | [🚀 Cara Menjalankan](#-cara-menjalankan) | Build, run, package EXE |
| 7 | [🗺️ Peta Dunia & Lore](#️-peta-dunia--lore) | Timeline, karakter, zona |
| 8 | [👥 Tim](#-tim) | Kontribusi per anggota |

---

## 🎮 Tentang Game

> *"Asuna, mahasiswi IT dari Jakarta, tersedot ke Nusantara — dimensi paralel di mana mitologi Jawa, Bali, Sunda, dan Kalimantan menjadi kenyataan fisik. Bersama 7 Guildmate legendaris, ia harus membebaskan Lima Penjaga Agung dan menghadapi Theresa — entitas beku dari dimensi yang tak pernah hangat."*

**Mythic Item Obtained** adalah game **Roguelite RPG Turn-Based** yang dibangun **dari nol** menggunakan Java 25 + JavaFX 25, **tanpa game engine eksternal**. Seluruh sistem combat, dungeon, dialog, gacha, save/load diimplementasi secara programatik murni sebagai Tugas Akhir Pemrograman Berorientasi Objek.

```
Genre    : Roguelite RPG Turn-Based
Tema     : Dark Fantasy Nusantara
Platform : Windows Desktop (Standalone EXE / Maven Run)
Stack    : Java 25 + JavaFX 25 + Maven 3.8+
UI Mode  : Programmatic (tanpa FXML)
```

---

## ✨ Fitur Lengkap

### ⚔️ Sistem Combat
```
✓ Turn-based dengan Speed Bar visual
✓ 4 aksi: Serang / Jurus / Item / Bertahan + Kabur
✓ 40+ skill dengan efek damage, heal, buff/debuff, CC
✓ Floating damage text berwarna per tipe serangan:
  - Physical  → 🟠 Orange (#FF6633)
  - Cyber     → 🔵 Electric Blue (#44BBFF)  
  - Energy    → 🟣 Purple (#CC66FF)
  - True      → ⚪ White (#FFFFFF)
  - CRIT      → 🟡 Gold (#FFD700) + label "⚡ CRIT!" terpisah
  - DoT Bleed → 🔴 Dark Red (#CC2200)
  - DoT Burn  → 🟠 Orange Fire (#FF6600)
  - DoT Poison→ 🟢 Toxic Green (#88CC00)
✓ On-hit effects: BLEED/BURN/POISON (>100% = bonus DoT)
✓ Artifact auto-trigger setiap turn (no manual input)
✓ Speed control: 1× / 2× / SKIP
✓ 9 tipe status effect: BLEED, BURN, STUN, FREEZE, 
                         WEAKEN, SHIELD, REGEN, VIRUS, CORRODE
✓ Tooltip status effect tidak menghilang (30 detik)
```

### 🗺️ Dungeon System
```
✓ Grid 12 kolom × N baris procedural
✓ Fog of war adaptif (hanya cleared rooms solid)
✓ 5 tema zona per 10 lantai dengan BGM berbeda
✓ 8 tipe room: Enemy, Loot, Rest, Event, Shop, Trap, Boss, Empty
✓ Boss unik tiap 10 lantai dengan multi-phase mechanic
✓ Endless mode setelah floor 51 — boss random tiap 10 lantai
✓ MAP REVEAL event membuka semua tile sekaligus
✓ Level-up notification slide saat naik level
```

### 👥 Guildmate System
```
✓ 7 guildmate dengan role unik:
  - TankRX9     (Tank)     — HP 5.000, shield & taunt
  - SeraMend    (Healer)   — HP 2.400, mass heal & revive
  - KiraVoss    (DPS)      — HP 2.200, compound crit scaling
  - MagnusForge (Bruiser)  — HP 3.200, armor break
  - EchoNull    (Support)  — HP 2.200, CC & debuff
  - LyraBloom   (Support)  — HP 2.500, barrier & buff
  - Vector      (Assassin) — HP 1.800, execute & stealth
✓ Sistem Loyalty — naik level = HP +200/level (compound)
✓ AI combat: pilih skill & target berdasarkan situasi
✓ Dialog dinamis 50+ baris per guildmate
✓ 1 slot artifact per guildmate (role-filtered)
```

### ⬡ Sistem Artifact & Gacha
```
✓ 20 jenis artifact dari 7 role
✓ Gacha economy:
  - 1× Pull  = 800 Gold / 1 Tiket
  - 10× Pull = 7.200 Gold / 9 Tiket
  - Pity 80  = garansi LEGENDARY+
✓ Drop rates: MYTHIC 1% · LEGENDARY 4% · EPIC 12%
               RARE 18% · UNCOMMON 25% · COMMON 40%
✓ Animasi gacha 3 fase (±2.6 detik):
  Fase 1: Portal muncul + background gelap
  Fase 2: 4 cincin spin + portal membesar 1.6× + flash purple
  Fase 3: Slide reveal per kartu (rarest first)
✓ Artifact pocket — storage TERPISAH dari bag (unlimited)
✓ Auto-save setelah setiap pull (anti save-scumming)
✓ Pasar Artefak di Kota — jual per rarity:
  COMMON 80-180 · UNCOMMON 200-350 · RARE 400-700
  EPIC 800-1.400 · LEGENDARY 2.000-4.000 · MYTHIC 6.000-10.000
```

### 📖 Lore & Dialog System
```
✓ Visual novel style: portrait kiri, dialog bawah, pilihan kanan
✓ 21 script dialog + 3 video cutscene
✓ 7 Act dengan lock system:
  - ACT 0     : Malam Kejadian (selalu terbuka)
  - ACT 1     : Pasar Malam Gaib (unlock: kalahkan Batara Kala)
  - ACT 2     : Candi Terlarang (unlock: kalahkan Nyi Roro Kidul)
  - ACT 3A    : Hutan Angker (unlock: kalahkan Rangda Agung)
  - ACT 3B    : Goa Naga (unlock: kalahkan Garuda Mahaguru)
  - ACT 3C    : Kahyangan Rusak (unlock: kalahkan Semar Pamungkas)
  - ACT FINAL : Void Dimension (unlock: kalahkan Theresa)
✓ ARSIP LORE — galeri replay seluruh cutscene dari hub
✓ Notifikasi "📖 Arsip Lore Terbuka" 2 detik setelah boss kalah
✓ Theresa 6 fase — tidak bisa mati sebelum fase 6
✓ Save playedCutscenes — cutscene tidak terulang saat reload
✓ Replay ENDING tersedia di hub setelah floor 52+
```

### 💾 Inventory & Equipment
```
✓ 60-slot bag + artifact pocket terpisah (unlimited)
✓ Slot artifact player: KIRI dan KANAN grid equipment
✓ 8 slot equipment dengan variasi stat luas:
  - Helm    : HP, DEF, Crit, Evasion, HP Regen, Damage Mult
  - Sepatu  : Speed, Evasion, Damage Mult, CDR, Accuracy
  - Cincin  : 5 build (Crit / HP-Shield / Lifesteal / DPS / Utility)
  - Baju    : HP, Shield, DEF tri-type, Thorn, Tenacity
✓ Tab filter: SEMUA·ARTEFAK·SENJATA·BAJU·HELM·SEPATU·
              CINCIN·AKSESORI·KONSUMABLE·MATERIAL
✓ Consumable heal: COMMON ~750 · RARE ~1.050 · LEGENDARY ~1.500 HP
```

### 🎵 Audio System
```
✓ 15 BGM MP3: menu, hub, shop, 5 zona dungeon, 
              combat, elite, 2 boss, Theresa, 2 cutscene
✓ 8 SFX WAV: hit physical/cyber/energy, critical, 
             heal, miss, victory, gameover
✓ Fade in/out smooth antar BGM
✓ Boss BGM otomatis berdasarkan tipe boss
✓ BGM 55% · SFX 60% volume
```

### 💿 Save System
```
✓ 3 slot manual + 1 auto-save
✓ Auto-save: setelah room cleared, setelah gacha, setelah battle
✓ Menyimpan: inventory, artifact pocket, played cutscenes,
             guildmate state, dungeon progress, gold, level
✓ Merc revive 30% HP saat return hub + auto-save revived state
✓ Load save tidak trigger cutscene yang sudah terlewat
```

---

## 📊 Statistik Proyek

<div align="center">

| Metrik | Nilai |
|--------|-------|
| 📁 Total file Java | **107 file** |
| 📝 Total baris kode | **~27.000 baris** |
| 🎨 Total file asset | **264 file** |
| 📦 Ukuran build | **~367 MB** |
| ⚗️ Abstract class | **5** (Entity, Enemy, Boss, Mercenary, Item) |
| 🔧 Design patterns | **6** (Observer, Factory, Facade, MVC, Singleton, State Machine) |
| 🎵 BGM tracks | **15 MP3** |
| 🔊 SFX clips | **8 WAV** |
| ⬡ Artifact types | **20 jenis** |
| 👾 Enemy types | **20+ musuh + 6 boss** |
| 💬 Dialog scripts | **21 script** |
| 🗡️ Skills | **40+ skill** |
| 🏰 Boss phases | **Max 6 fase (Theresa)** |
| 🗺️ Lantai | **∞ (Endless setelah floor 51)** |

</div>

---

## 🏗️ Arsitektur OOP

### Layer Diagram
```
┌──────────────────────── PRESENTATION LAYER ────────────────────────────┐
│  MainMenu · HubView · CombatView · DungeonGridMap · GachaView          │
│  ProfileView · MercenaryView · CutsceneView · LoreArchiveView          │
│  SaveLoadView · ShopView · WorkshopView · CityView · EventView         │
└───────────────────────────────┬────────────────────────────────────────┘
                                │  SceneRouter (Controller, 22 show*())
┌───────────────────────────────▼────────────────────────────────────────┐
│                      GameEngine  ◄── Facade Pattern                    │
└──┬────────────┬────────────┬────────────┬──────────────────────────────┘
   │            │            │            │
CombatMgr  DungeonMgr  Inventory    SaveMgr · GachaSystem · AudioMgr
   │            │         /Loot          
   │            │                        
┌──▼────────────▼──────────────────────────────────────────────────────┐
│  Entity (abstract)                                                    │
│  ├── Player                                                           │
│  ├── Mercenary (abstract) → 7 guildmate class                        │
│  └── Enemy (abstract) → Boss (abstract) → 6 Boss, 20+ Enemy biasa   │
│                                                                       │
│  Item (abstract)                                                      │
│  ├── Equipment → Weapon / Armor / Accessory                          │
│  ├── Artifact  (pocket terpisah, unlimited)                          │
│  ├── Consumable · Material                                            │
└──────────────────────────────────────────────────────────────────────┘
```

### Implementasi 4 Pilar OOP

| Pilar | Implementasi | Contoh Konkret |
|-------|-------------|----------------|
| **Encapsulation** | StatSheet 3-layer (base/equip/buff) | `stat.get(StatType.PHYSICAL_ATK)` — internal tersembunyi |
| **Inheritance** | Entity → Player/Mercenary/Enemy/Boss | KiraVoss override `onLoyaltyLevelUp()` compound scaling |
| **Polymorphism** | `List<Entity>` dalam TurnQueue | Player, Merc, Enemy semua masuk PriorityQueue<Entity> |
| **Abstraction** | GameEngine Facade, 5 abstract class | UI hanya panggil `engine.submitCombatAction()` |

### Design Patterns

| Pattern | Class | Kegunaan |
|---------|-------|---------|
| **Observer** | CombatManager → CombatView | Emit 15+ event via `Consumer<CombatEvent>` |
| **Factory** | EntityFactory, LootManager | `generateBoss(floor)`, `generateEquipment(rarity)` |
| **Facade** | GameEngine | 1 entry point ke 8 sistem backend |
| **MVC** | SceneRouter/View/Engine | 22 View, 22 `show*()` methods |
| **Singleton** | AudioManager | `AudioManager.get()` — 1 instance |
| **State Machine** | DungeonManager | IDLE→EXPLORING→IN_COMBAT→FLOOR_COMPLETE |

---

## 📋 CHANGELOG LENGKAP

> Format: 🔴 Critical Fix · 🐛 Bug Fix · ✨ Feature · ⚡ Improvement · 🎨 Visual · 🔧 Refactor

---

### [v1.0.4] — 2026-05-21 · Lore Archive + EXE Build

#### ✨ Feature
- **Arsip Lore** — galeri 7 act dari hub (tombol 📖 ARSIP LORE di nav grid)
  - Lock system: act terbuka setelah menyelesaikan arc boss-nya
  - Replay semua dialog + cutscene per act dengan tombol ▶ PUTAR
  - VIDEO CUTSCENE section hanya muncul jika ada video tersedia
  - Urutan ACT FINAL: FINAL_BOSS_PRE → Video → ENDING (kehangatan pertama paling akhir)
  - Notifikasi "📖 Arsip Lore Terbuka" 2 detik setelah boss dikalahkan
- **Endless Mode** — floor 52+ dengan boss random tiap 10 lantai (tanpa Theresa)
  - Floor 60: Nyi Roro Kidul · Floor 70: Rangda · Floor 80: Garuda · Floor 90: Semar · Floor 100: Batara Kala
- **Replay Ending** — tombol di hub setelah floor 52+
- **EXE Build** — jpackage bundling dengan icon game

#### 🔴 Critical Fix
- **Lore trigger salah** — cutscene Theresa muncul saat bunuh kroco di floor 51
  - Fix: cek `instanceof Boss` pada enemy yang dilawan, bukan `isBossRoom()` (yang bisa return true untuk room biasa)
  - `resolveBossCutsceneKey()` diubah ke range-based (floor 1-10, 11-20, dst) bukan exact number
- **Theresa mati terlalu cepat** — tidak ada HP floor per phase
  - Fix: override `receiveDamage()` di Theresa.java — HP tidak bisa turun di bawah threshold per fase (85%→65%→40%→20%→10%→0%)

#### 🐛 Bug Fix
- Merc mati setelah kembali ke hub: `returnToHub()` sekarang revive `activeMercs` + `getOwnedMercs()` + auto-save setelah revive
- Tombol act di Arsip Lore tidak bisa diklik (rebuild seluruh view tiap klik → selectedActId reset) — sekarang update panel kanan in-place via `detailPane.setContent()`
- `SceneRouter.showVideo()` salah constructor CutsceneView — fix pakai `DialogScript.get()` + signature benar
- Hapus tombol `...` sisa (LIHAT KEMBALI ENDING) dari hub
- Duplicate `maven-shade-plugin` di pom.xml dihapus
- `app-image` enum fix ke `APP_IMAGE` untuk jpackage plugin
- `--win-menu`/`--win-shortcut` dihapus (tidak kompatibel dengan app-image)
- `Launcher.java` ditambahkan — fix JavaFX silent crash saat run dari fat JAR/EXE

---

### [v1.0.3] — 2026-05-20 · Pasar Artefak + Memory Fix

#### ✨ Feature
- **Pasar Artefak** di Kota — area baru (⬡ icon) untuk jual artifact dari pocket
  - Harga per rarity, konsisten berdasarkan artifact ID hash
  - Tombol JUAL SEMUA (muncul jika ≥2 artifact)
  - Tombol JUAL individual per artifact
- **Boss EVASION cap 20%** — boss tidak bisa unhittable akibat scaling
- **Min hit chance 15%** (dari 5%) — player selalu punya chance hit

#### 🔴 Critical Fix
- **Boss ATK 89.999 one-shot semua** di floor 50
  - `stats.scaleBase(bossScale)` scale semua stat termasuk ATK — floor 50 bossScale=7.86×
  - Fix: pisah scaling ATK (+6%/floor) vs HP, hard cap ATK = `400 + floor×16`
  - Floor 50 max ATK/stat: 1.200 (bukan 39.300+)
- **Boss EVASION 75-193%** di floor 30-51 (RangdaAgung base 0.15 × 5.06 = 75.9%)
  - Fix: cap EVASION boss maks 20% setelah scaling

#### ⚡ Improvement
- **Image caching** di AssetManager — `ConcurrentHashMap` untuk semua image load
- **CombatView.cleanup()** — stop semua Timeline saat pindah scene
- **SceneRouter** track `activeCombatView` → memanggil cleanup saat ke hub/menu
- **pom.xml**: `-Xmx512m`, `G1GC`, `prism.maxvram=256m`, `javafx.animation.fullspeed=false`
- `removeArtifactFromPocket()` ditambahkan ke Inventory

---

### [v1.0.2] — 2026-05-20 · Boss & Combat Fixes

#### 🔴 Critical Fix
- **Boss 0 damage (FINAL fix)** — 3-layer shield masalah:
  1. ✓ MAX_SHIELD stat di-zero (sudah dari v1.0.1)
  2. ✓ SHIELD_REGEN stat di-zero (baru)
  3. ✓ **`this.currentShield = 0`** — field yang di-set dari MAX_SHIELD di constructor sebelum scaleToFloor — INI ROOT CAUSE UTAMA

#### 🎨 Visual
- **Floating damage redesign** — per-type colors, CRIT split label, DoT colors
  - "⚡ CRIT!" label terpisah di atas angka damage
  - Physical/Cyber/Energy/True masing-masing warna unik
  - Fade-in 25% pertama, fade-out 75% sisanya
  - Outline shadow 1.5px, glow ring untuk CRIT
  - 20ms render loop (dari 30ms)

---

### [v1.0.1] — 2026-05-19 · Bug Patch

#### 🔴 Critical Fix  
- **Boss 0 damage** — DEF cap 233 + SHIELD = 0 + currentShield = 0 (sebagian)
- **Boss EVASION overflow** — scaled ke 75-193% → fix cap 20%
- **Min hit chance** 5% → 15%
- **Lore trigger di kroco** — COMBAT_STARTED check `isBossRoom()` (masih bug, fix final di v1.0.4)

#### ✨ Feature
- **Endless mode skeleton** — ProceduralGenerator.endlessBossForFloor()
- **Replay ending button** di HubView (floor ≥52)
- **Theresa 6 phase** — `receiveDamage()` override dengan HP floor per phase

#### 🐛 Bug Fix  
- `activeCombatView` field not found → ditambahkan ke SceneRouter
- `speedTimer/turnTimer` tidak ada → cleanup() fix pakai combatLoop/floatLoop
- `portraitAsuna("normal")` → `portraitAsuna()` (no-arg)
- `equipArtifact()` duplikat → dihapus

---

### [v1.0.0] — 2026-05-19 · FULL RELEASE 🎉

#### ✨ Feature
- **Artifact Pocket** — storage artifact terpisah dari bag, tidak makan slot, unlimited
  - `Inventory.addItem(Artifact)` → selalu ke pocket
  - Tersave di `GameSaveState.savedArtifactPocket` sebagai `[typeName, rarityName][]`
- **Artifact slot repositioning** — KIRI dan KANAN grid equipment (bukan di bawah accessories)
- **Auto-save setelah gacha** — setiap pull trigger `autoSave()`, mencegah save-scumming
- **`getArtifactPocket()`** getter ditambahkan ke Inventory

#### 🐛 Bug Fix
- `equipArtifactToSlot()` tidak hapus dari pocket → artifact duplikat muncul di list
- Cross-slot duplicate: equip ke slot 1 = artifact yang sama di slot 2 → dikosongkan dulu

#### 🎨 Visual
- **Gacha animasi 3 fase** (±2.6 detik):
  - Fase 1 (0-0.8s): portal scale-in + background gelap
  - Fase 2 (0.8-2.2s): 4 cincin spin berlawanan + portal 1.6× + flash purple
  - Fase 3 (2.2-2.6s): fade out → slide reveal

---

### [v0.9.9] — 2026-05-19 · Pre-Release Polish

#### 🎨 Visual
- Gacha loading animation: portal berputar + teks "MEMANGGIL..." selama 1.2 detik

#### 🐛 Bug Fix
- Artifact slot awal repositioning (iterasi pertama, belum final)

---

### [v0.9.8] — 2026-05-19 · Inventory & Artifact UI

#### 🔴 Critical Fix
- **LEPAS item tidak berfungsi** — `Inventory.unequip(slot)` gagal jika bag penuh
  - Item dari slot tidak seharusnya cek bag capacity
- **Artifact dari gacha tidak masuk bag** — bug sama: bag full → `addItem()` return false

#### ✨ Feature
- **Tab ARTEFAK** di inventory filter — `instanceof Artifact` check
- **`showArtifactBagPopup()`** — popup klik artifact: icon, info, → SLOT 1/2
- **`showArtifactSlotInfo()`** — popup slot terisi: info + LEPAS ARTEFAK

---

### [v0.9.7] — 2026-05-18 · Lore Trigger Fix

#### 🔴 Critical Fix
- **Lore trigger v1** — `isCurrentRoomBoss()` cek floor number bukan room type
  - Fix: `room.getType() == RoomType.BOSS`
- `double-brace initialization` pada `final class` → error → variabel biasa

#### ✨ Feature
- **Artifact icons di combat** — row icon 36×36 per artifact, CD indicator
- **Gacha full-screen slide reveal** — per kartu dengan auto-advance 1.8s + skip
- **Merc artifact slot** dengan role filter

---

### [v0.9.6] — 2026-05-18 · Fog of War + Audio Fix

#### 🔴 Critical Fix
- **Map "dua pulau" bug** — `initFog()` pakai `isVisited()` bukan `isCleared()`
  - Fix: hanya cleared rooms masuk visitedTiles (permanent solid)
  - `syncPlayer()`: rebuild visibleTiles fresh tiap move
- **BGM error Windows** — JavaFX tidak support OGG di Windows
  - Fix: semua 15 file BGM dikonvert ke MP3

#### 🐛 Bug Fix
- `DungeonManager` hub-return: `setCurrentRoom()` sebelum emit `floorEntered`

---

### [v0.9.5] — 2026-05-18 · Map & Audio Wiring

#### 🔴 Critical Fix
- **Player selalu return ke room 0** — dua bug:
  1. `enterRoom()` tidak memanggil `setCurrentRoom()`
  2. `advanceToNextFloor()` emit SEBELUM `setCurrentRoom()` (terbalik)

#### 🎵 Audio
- BGM lengkap di semua `show*()` SceneRouter
- Boss BGM otomatis: `engine.getCombatBgm()` detect boss type

---

### [v0.9.4] — 2026-05-18 · Audio System

#### ✨ Feature
- **AudioManager** — Singleton, BGM 55% + SFX 60%
- 15 BGM MP3 + 8 SFX WAV semua terpasang
- `bgmForFloor(int)` — auto-pilih tema per 10 lantai

---

### [v0.9.3] — 2026-05-17 · Entity & Combat Fix

#### 🔴 Critical Fix
- **Guildmate tetap mati setelah hub** — `Entity.setHpDirect()` tidak reset `alive`
  - Fix: `setHpDirect()` + `alive = currentHp > 0`, `revive()` paksa `alive = true`
- **recalcEquipStats()** dipanggil di 3 tempat: startDungeonRun, returnToHub, restoreFromSave

#### 🎨 Visual
- **Main menu layout fix** — tombol di bawah logo (BorderPane.setBottom)
- 20 artifact icons di-import ke `assets/icons/artifact/`

---

### [v0.9.0–v0.9.2] — 2026-05-17 · Artifact System Launch

#### ✨ Feature (Gacha System)
- `GachaSystem.java` — pity 80, 6 rarity, rates
- `Artifact.java` — CD system, scaling per rarity
- `ArtifactType.java` — 20 jenis, 7 role
- `GachaView.java` — Altar Artefak
- `Inventory` artifact slots: equipArtifactToSlot(), unequipArtifact()
- `CombatManager` artifact auto-trigger setiap advanceTurn()

---

### [v0.8.9] — 2026-05-16 · Dialog System

#### ✨ Feature
- **CutsceneView** — Visual novel P5-style
  - `fadeBlack.setMouseTransparent(true)` — KRITIS agar pilihan bisa diklik
  - Portrait kiri, dialog bawah, pilihan kanan
- **21 dialog script** di `DialogScript.java`

---

### [v0.8.8] — 2026-05-16 · Save System

#### ✨ Feature
- **Java Serialization Save** — `%APPDATA%\MythicItemObtained\`
- 3 slot manual + 1 auto (`save_auto.dat`)
- `GameSaveState implements Serializable`
- `GameStateConverter.toSaveState()` + `restoreFromSave()`
- `SaveLoadView.java` — UI 3 slot dengan preview

---

### [v0.8.0–v0.8.7] — 2026-05-14-16 · Foundation Systems

#### ✨ Features
- DungeonManager state machine + ProceduralGenerator 12-kolom
- CombatView dengan turn order bar, sprites, floating damage, speed control
- Mercenary dialog system (50+ baris per guildmate)
- Workshop upgrade + kalibrasi

#### 🔴 Critical Fixes
- StatType renames: `CRIT_DMG_MULT→CRIT_DAMAGE`, `CDR→COOLDOWN_REDUCE`, `DMG_MULT→DAMAGE_MULT`
- `UIFactory.screenRoot()` hapus `setMinSize()` — fix overflow/sinking bug
- Boss SHIELD=0, EVASION awal fix

---

### [v0.7.9] — 2026-05-14 · Combat Engine Foundation

#### ✨ Feature
- `CombatManager` + Observer pattern (`Consumer<CombatEvent>`)
- `DamageCalculator` — formula DEF reduction, armor pierce, crit, min damage
- `SkillExecutor` — 40+ skill
- `TurnQueue` — PriorityQueue<Entity> by SPEED
- `CombatEvent` — 15+ event types

---

## 🚀 Cara Menjalankan

### Dev Mode (mvn)
```bash
git clone https://github.com/ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX
cd MythicItemObtained
mvn javafx:run
```

### Build EXE Portable
```bash
# Step 1: Build fat JAR
mvn clean package -DskipTests

# Step 2: Buat folder portable
mvn jpackage:jpackage

# Hasil: target/installer/MythicItemObtained/
# → Klik MythicItemObtained.exe langsung main!
```

**Prasyarat build:** JDK 25+ · Maven 3.8+
**Prasyarat EXE:** WiX Toolset 3.x (https://github.com/wixtoolset/wix3/releases)

---

## 🗺️ Peta Dunia & Lore

### Zona Dungeon

| Lantai | Zona | Boss | Red Shard |
|--------|------|------|-----------|
| 1–10 | 🕯️ Pasar Malam Gaib | Batara Kala | #1 |
| 11–20 | 🏛️ Candi Terlarang | Nyi Roro Kidul | #2 |
| 21–30 | 🌳 Hutan Angker | Rangda Agung | #3 |
| 31–40 | 🐉 Goa Naga | Garuda Mahaguru | #4 |
| 41–50 | ☁️ Kahyangan Rusak | Semar Pamungkas | #5 |
| 51 | ❄️ Void Dimension | Theresa (6 fase) | Red Blossom Katana |
| 52+ | 🔄 Endless Mode | Boss Random | Gacha Ticket |

### Boss Scaling (v1.0.4)
```
HP    = max(baseHP × 2.5, 10.000 + floor × 600)
ATK   = base × (1 + floor × 0.06) → max (400 + floor × 16)
DEF   = 0 (zero — boss kuat dari HP, bukan DEF)
SHIELD= 0 (currentShield = 0 juga!)
EVASION = max(baseEvasion × scale, 0.20)  ← cap 20%
```

### 7 Guildmate
| Nama | Alias | Role | HP | Weapon |
|------|-------|------|----|--------|
| Srikandi | Kira Voss | Pemanah Bayangan | 2.200 | Compound Crit |
| Gatot Kaca | Tank-RX9 | Ksatria Baja | 5.000 | Shield + Taunt |
| Nyai Roro | Sera Mend | Tabib Mistis | 2.400 | Mass Heal |
| Rangga | Vector | Pembunuh Bayaran | 1.800 | Execute |
| Bima | Magnus Forge | Petarung Agung | 3.200 | Armor Break |
| Ki Ageng | Echo Null | Dukun Tua | 2.200 | CC + Debuff |
| Dewi Sri | Lyra Bloom | Penjaga Keseimbangan | 2.500 | Barrier + Buff |

---

## 👥 Tim

**Kelompok 7 — Tugas Akhir PBO 2026 · BismilahFIX**

🔗 Repository: [ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX](https://github.com/ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX)

---

<div align="center">

```
⬡  Mythic Item Obtained  ·  v1.0.4  ·  Full Release
Built with Java 25 + JavaFX 25  ·  No External Game Engine
Tugas Akhir PBO 2026  ·  Kelompok 7  ·  BismilahFIX
```

*"Setiap detik tenang adalah persiapan untuk badai berikutnya."*
— Srikandi / Kira Voss

</div>
```

---

## ✨ Fitur Lengkap

### ⚔️ Sistem Combat
```
✓ Turn-based dengan Speed Bar visual
✓ 4 aksi: Serang / Jurus / Item / Bertahan + Kabur
✓ 40+ skill dengan efek damage, heal, buff/debuff, CC
✓ Floating damage text berwarna per tipe serangan:
  - Physical  → 🟠 Orange (#FF6633)
  - Cyber     → 🔵 Electric Blue (#44BBFF)  
  - Energy    → 🟣 Purple (#CC66FF)
  - True      → ⚪ White (#FFFFFF)
  - CRIT      → 🟡 Gold (#FFD700) + label "⚡ CRIT!" terpisah
  - DoT Bleed → 🔴 Dark Red (#CC2200)
  - DoT Burn  → 🟠 Orange Fire (#FF6600)
  - DoT Poison→ 🟢 Toxic Green (#88CC00)
✓ On-hit effects: BLEED/BURN/POISON (>100% = bonus DoT)
✓ Artifact auto-trigger setiap turn (no manual input)
✓ Speed control: 1× / 2× / SKIP
✓ 9 tipe status effect: BLEED, BURN, STUN, FREEZE, 
                         WEAKEN, SHIELD, REGEN, VIRUS, CORRODE
✓ Tooltip status effect tidak menghilang (30 detik)
```

### 🗺️ Dungeon System
```
✓ Grid 12 kolom × N baris procedural
✓ Fog of war adaptif (hanya cleared rooms solid)
✓ 5 tema zona per 10 lantai dengan BGM berbeda
✓ 8 tipe room: Enemy, Loot, Rest, Event, Shop, Trap, Boss, Empty
✓ Boss unik tiap 10 lantai dengan multi-phase mechanic
✓ Endless mode setelah floor 51 — boss random tiap 10 lantai
✓ MAP REVEAL event membuka semua tile sekaligus
✓ Level-up notification slide saat naik level
```

### 👥 Guildmate System
```
✓ 7 guildmate dengan role unik:
  - TankRX9     (Tank)     — HP 5.000, shield & taunt
  - SeraMend    (Healer)   — HP 2.400, mass heal & revive
  - KiraVoss    (DPS)      — HP 2.200, compound crit scaling
  - MagnusForge (Bruiser)  — HP 3.200, armor break
  - EchoNull    (Support)  — HP 2.200, CC & debuff
  - LyraBloom   (Support)  — HP 2.500, barrier & buff
  - Vector      (Assassin) — HP 1.800, execute & stealth
✓ Sistem Loyalty — naik level = HP +200/level (compound)
✓ AI combat: pilih skill & target berdasarkan situasi
✓ Dialog dinamis 50+ baris per guildmate
✓ 1 slot artifact per guildmate (role-filtered)
```

### ⬡ Sistem Artifact & Gacha
```
✓ 20 jenis artifact dari 7 role
✓ Gacha economy:
  - 1× Pull  = 800 Gold / 1 Tiket
  - 10× Pull = 7.200 Gold / 9 Tiket
  - Pity 80  = garansi LEGENDARY+
✓ Drop rates: MYTHIC 1% · LEGENDARY 4% · EPIC 12%
               RARE 18% · UNCOMMON 25% · COMMON 40%
✓ Animasi gacha 3 fase (±2.6 detik):
  Fase 1: Portal muncul + background gelap
  Fase 2: 4 cincin spin + portal membesar 1.6× + flash purple
  Fase 3: Slide reveal per kartu (rarest first)
✓ Artifact pocket — storage TERPISAH dari bag (unlimited)
✓ Auto-save setelah setiap pull (anti save-scumming)
✓ Pasar Artefak di Kota — jual per rarity:
  COMMON 80-180 · UNCOMMON 200-350 · RARE 400-700
  EPIC 800-1.400 · LEGENDARY 2.000-4.000 · MYTHIC 6.000-10.000
```

### 📖 Lore & Dialog System
```
✓ Visual novel style: portrait kiri, dialog bawah, pilihan kanan
✓ 21 script dialog + 3 video cutscene
✓ 7 Act dengan lock system:
  - ACT 0     : Malam Kejadian (selalu terbuka)
  - ACT 1     : Pasar Malam Gaib (unlock: kalahkan Batara Kala)
  - ACT 2     : Candi Terlarang (unlock: kalahkan Nyi Roro Kidul)
  - ACT 3A    : Hutan Angker (unlock: kalahkan Rangda Agung)
  - ACT 3B    : Goa Naga (unlock: kalahkan Garuda Mahaguru)
  - ACT 3C    : Kahyangan Rusak (unlock: kalahkan Semar Pamungkas)
  - ACT FINAL : Void Dimension (unlock: kalahkan Theresa)
✓ ARSIP LORE — galeri replay seluruh cutscene dari hub
✓ Notifikasi "📖 Arsip Lore Terbuka" 2 detik setelah boss kalah
✓ Theresa 6 fase — tidak bisa mati sebelum fase 6
✓ Save playedCutscenes — cutscene tidak terulang saat reload
✓ Replay ENDING tersedia di hub setelah floor 52+
```

### 💾 Inventory & Equipment
```
✓ 60-slot bag + artifact pocket terpisah (unlimited)
✓ Slot artifact player: KIRI dan KANAN grid equipment
✓ 8 slot equipment dengan variasi stat luas:
  - Helm    : HP, DEF, Crit, Evasion, HP Regen, Damage Mult
  - Sepatu  : Speed, Evasion, Damage Mult, CDR, Accuracy
  - Cincin  : 5 build (Crit / HP-Shield / Lifesteal / DPS / Utility)
  - Baju    : HP, Shield, DEF tri-type, Thorn, Tenacity
✓ Tab filter: SEMUA·ARTEFAK·SENJATA·BAJU·HELM·SEPATU·
              CINCIN·AKSESORI·KONSUMABLE·MATERIAL
✓ Consumable heal: COMMON ~750 · RARE ~1.050 · LEGENDARY ~1.500 HP
```

### 🎵 Audio System
```
✓ 15 BGM MP3: menu, hub, shop, 5 zona dungeon, 
              combat, elite, 2 boss, Theresa, 2 cutscene
✓ 8 SFX WAV: hit physical/cyber/energy, critical, 
             heal, miss, victory, gameover
✓ Fade in/out smooth antar BGM
✓ Boss BGM otomatis berdasarkan tipe boss
✓ BGM 55% · SFX 60% volume
```

### 💿 Save System
```
✓ 3 slot manual + 1 auto-save
✓ Auto-save: setelah room cleared, setelah gacha, setelah battle
✓ Menyimpan: inventory, artifact pocket, played cutscenes,
             guildmate state, dungeon progress, gold, level
✓ Merc revive 30% HP saat return hub + auto-save revived state
✓ Load save tidak trigger cutscene yang sudah terlewat
```

---

## 📊 Statistik Proyek

<div align="center">

| Metrik | Nilai |
|--------|-------|
| 📁 Total file Java | **107 file** |
| 📝 Total baris kode | **~27.000 baris** |
| 🎨 Total file asset | **264 file** |
| 📦 Ukuran build | **~367 MB** |
| ⚗️ Abstract class | **5** (Entity, Enemy, Boss, Mercenary, Item) |
| 🔧 Design patterns | **6** (Observer, Factory, Facade, MVC, Singleton, State Machine) |
| 🎵 BGM tracks | **15 MP3** |
| 🔊 SFX clips | **8 WAV** |
| ⬡ Artifact types | **20 jenis** |
| 👾 Enemy types | **20+ musuh + 6 boss** |
| 💬 Dialog scripts | **21 script** |
| 🗡️ Skills | **40+ skill** |
| 🏰 Boss phases | **Max 6 fase (Theresa)** |
| 🗺️ Lantai | **∞ (Endless setelah floor 51)** |

</div>

---

## 🏗️ Arsitektur OOP

### Layer Diagram
```
┌──────────────────────── PRESENTATION LAYER ────────────────────────────┐
│  MainMenu · HubView · CombatView · DungeonGridMap · GachaView          │
│  ProfileView · MercenaryView · CutsceneView · LoreArchiveView          │
│  SaveLoadView · ShopView · WorkshopView · CityView · EventView         │
└───────────────────────────────┬────────────────────────────────────────┘
                                │  SceneRouter (Controller, 22 show*())
┌───────────────────────────────▼────────────────────────────────────────┐
│                      GameEngine  ◄── Facade Pattern                    │
└──┬────────────┬────────────┬────────────┬──────────────────────────────┘
   │            │            │            │
CombatMgr  DungeonMgr  Inventory    SaveMgr · GachaSystem · AudioMgr
   │            │         /Loot          
   │            │                        
┌──▼────────────▼──────────────────────────────────────────────────────┐
│  Entity (abstract)                                                    │
│  ├── Player                                                           │
│  ├── Mercenary (abstract) → 7 guildmate class                        │
│  └── Enemy (abstract) → Boss (abstract) → 6 Boss, 20+ Enemy biasa   │
│                                                                       │
│  Item (abstract)                                                      │
│  ├── Equipment → Weapon / Armor / Accessory                          │
│  ├── Artifact  (pocket terpisah, unlimited)                          │
│  ├── Consumable · Material                                            │
└──────────────────────────────────────────────────────────────────────┘
```

### Implementasi 4 Pilar OOP

| Pilar | Implementasi | Contoh Konkret |
|-------|-------------|----------------|
| **Encapsulation** | StatSheet 3-layer (base/equip/buff) | `stat.get(StatType.PHYSICAL_ATK)` — internal tersembunyi |
| **Inheritance** | Entity → Player/Mercenary/Enemy/Boss | KiraVoss override `onLoyaltyLevelUp()` compound scaling |
| **Polymorphism** | `List<Entity>` dalam TurnQueue | Player, Merc, Enemy semua masuk PriorityQueue<Entity> |
| **Abstraction** | GameEngine Facade, 5 abstract class | UI hanya panggil `engine.submitCombatAction()` |

### Design Patterns

| Pattern | Class | Kegunaan |
|---------|-------|---------|
| **Observer** | CombatManager → CombatView | Emit 15+ event via `Consumer<CombatEvent>` |
| **Factory** | EntityFactory, LootManager | `generateBoss(floor)`, `generateEquipment(rarity)` |
| **Facade** | GameEngine | 1 entry point ke 8 sistem backend |
| **MVC** | SceneRouter/View/Engine | 22 View, 22 `show*()` methods |
| **Singleton** | AudioManager | `AudioManager.get()` — 1 instance |
| **State Machine** | DungeonManager | IDLE→EXPLORING→IN_COMBAT→FLOOR_COMPLETE |

---

## 📋 CHANGELOG LENGKAP

> Format: 🔴 Critical Fix · 🐛 Bug Fix · ✨ Feature · ⚡ Improvement · 🎨 Visual · 🔧 Refactor

---

### [v1.0.4] — 2026-05-21 · Lore Archive + EXE Build

#### ✨ Feature
- **Arsip Lore** — galeri 7 act dari hub (tombol 📖 ARSIP LORE di nav grid)
  - Lock system: act terbuka setelah menyelesaikan arc boss-nya
  - Replay semua dialog + cutscene per act dengan tombol ▶ PUTAR
  - VIDEO CUTSCENE section hanya muncul jika ada video tersedia
  - Urutan ACT FINAL: FINAL_BOSS_PRE → Video → ENDING (kehangatan pertama paling akhir)
  - Notifikasi "📖 Arsip Lore Terbuka" 2 detik setelah boss dikalahkan
- **Endless Mode** — floor 52+ dengan boss random tiap 10 lantai (tanpa Theresa)
  - Floor 60: Nyi Roro Kidul · Floor 70: Rangda · Floor 80: Garuda · Floor 90: Semar · Floor 100: Batara Kala
- **Replay Ending** — tombol di hub setelah floor 52+
- **EXE Build** — jpackage bundling dengan icon game

#### 🔴 Critical Fix
- **Lore trigger salah** — cutscene Theresa muncul saat bunuh kroco di floor 51
  - Fix: cek `instanceof Boss` pada enemy yang dilawan, bukan `isBossRoom()` (yang bisa return true untuk room biasa)
  - `resolveBossCutsceneKey()` diubah ke range-based (floor 1-10, 11-20, dst) bukan exact number
- **Theresa mati terlalu cepat** — tidak ada HP floor per phase
  - Fix: override `receiveDamage()` di Theresa.java — HP tidak bisa turun di bawah threshold per fase (85%→65%→40%→20%→10%→0%)

#### 🐛 Bug Fix
- Merc mati setelah kembali ke hub: `returnToHub()` sekarang revive `activeMercs` + `getOwnedMercs()` + auto-save setelah revive
- Tombol act di Arsip Lore tidak bisa diklik (rebuild seluruh view tiap klik → selectedActId reset) — sekarang update panel kanan in-place via `detailPane.setContent()`
- `SceneRouter.showVideo()` salah constructor CutsceneView — fix pakai `DialogScript.get()` + signature benar
- Hapus tombol `...` sisa (LIHAT KEMBALI ENDING) dari hub
- Duplicate `maven-shade-plugin` di pom.xml dihapus
- `app-image` enum fix ke `APP_IMAGE` untuk jpackage plugin
- `--win-menu`/`--win-shortcut` dihapus (tidak kompatibel dengan app-image)
- `Launcher.java` ditambahkan — fix JavaFX silent crash saat run dari fat JAR/EXE

---

### [v1.0.3] — 2026-05-20 · Pasar Artefak + Memory Fix

#### ✨ Feature
- **Pasar Artefak** di Kota — area baru (⬡ icon) untuk jual artifact dari pocket
  - Harga per rarity, konsisten berdasarkan artifact ID hash
  - Tombol JUAL SEMUA (muncul jika ≥2 artifact)
  - Tombol JUAL individual per artifact
- **Boss EVASION cap 20%** — boss tidak bisa unhittable akibat scaling
- **Min hit chance 15%** (dari 5%) — player selalu punya chance hit

#### 🔴 Critical Fix
- **Boss ATK 89.999 one-shot semua** di floor 50
  - `stats.scaleBase(bossScale)` scale semua stat termasuk ATK — floor 50 bossScale=7.86×
  - Fix: pisah scaling ATK (+6%/floor) vs HP, hard cap ATK = `400 + floor×16`
  - Floor 50 max ATK/stat: 1.200 (bukan 39.300+)
- **Boss EVASION 75-193%** di floor 30-51 (RangdaAgung base 0.15 × 5.06 = 75.9%)
  - Fix: cap EVASION boss maks 20% setelah scaling

#### ⚡ Improvement
- **Image caching** di AssetManager — `ConcurrentHashMap` untuk semua image load
- **CombatView.cleanup()** — stop semua Timeline saat pindah scene
- **SceneRouter** track `activeCombatView` → memanggil cleanup saat ke hub/menu
- **pom.xml**: `-Xmx512m`, `G1GC`, `prism.maxvram=256m`, `javafx.animation.fullspeed=false`
- `removeArtifactFromPocket()` ditambahkan ke Inventory

---

### [v1.0.2] — 2026-05-20 · Boss & Combat Fixes

#### 🔴 Critical Fix
- **Boss 0 damage (FINAL fix)** — 3-layer shield masalah:
  1. ✓ MAX_SHIELD stat di-zero (sudah dari v1.0.1)
  2. ✓ SHIELD_REGEN stat di-zero (baru)
  3. ✓ **`this.currentShield = 0`** — field yang di-set dari MAX_SHIELD di constructor sebelum scaleToFloor — INI ROOT CAUSE UTAMA

#### 🎨 Visual
- **Floating damage redesign** — per-type colors, CRIT split label, DoT colors
  - "⚡ CRIT!" label terpisah di atas angka damage
  - Physical/Cyber/Energy/True masing-masing warna unik
  - Fade-in 25% pertama, fade-out 75% sisanya
  - Outline shadow 1.5px, glow ring untuk CRIT
  - 20ms render loop (dari 30ms)

---

### [v1.0.1] — 2026-05-19 · Bug Patch

#### 🔴 Critical Fix  
- **Boss 0 damage** — DEF cap 233 + SHIELD = 0 + currentShield = 0 (sebagian)
- **Boss EVASION overflow** — scaled ke 75-193% → fix cap 20%
- **Min hit chance** 5% → 15%
- **Lore trigger di kroco** — COMBAT_STARTED check `isBossRoom()` (masih bug, fix final di v1.0.4)

#### ✨ Feature
- **Endless mode skeleton** — ProceduralGenerator.endlessBossForFloor()
- **Replay ending button** di HubView (floor ≥52)
- **Theresa 6 phase** — `receiveDamage()` override dengan HP floor per phase

#### 🐛 Bug Fix  
- `activeCombatView` field not found → ditambahkan ke SceneRouter
- `speedTimer/turnTimer` tidak ada → cleanup() fix pakai combatLoop/floatLoop
- `portraitAsuna("normal")` → `portraitAsuna()` (no-arg)
- `equipArtifact()` duplikat → dihapus

---

### [v1.0.0] — 2026-05-19 · FULL RELEASE 🎉

#### ✨ Feature
- **Artifact Pocket** — storage artifact terpisah dari bag, tidak makan slot, unlimited
  - `Inventory.addItem(Artifact)` → selalu ke pocket
  - Tersave di `GameSaveState.savedArtifactPocket` sebagai `[typeName, rarityName][]`
- **Artifact slot repositioning** — KIRI dan KANAN grid equipment (bukan di bawah accessories)
- **Auto-save setelah gacha** — setiap pull trigger `autoSave()`, mencegah save-scumming
- **`getArtifactPocket()`** getter ditambahkan ke Inventory

#### 🐛 Bug Fix
- `equipArtifactToSlot()` tidak hapus dari pocket → artifact duplikat muncul di list
- Cross-slot duplicate: equip ke slot 1 = artifact yang sama di slot 2 → dikosongkan dulu

#### 🎨 Visual
- **Gacha animasi 3 fase** (±2.6 detik):
  - Fase 1 (0-0.8s): portal scale-in + background gelap
  - Fase 2 (0.8-2.2s): 4 cincin spin berlawanan + portal 1.6× + flash purple
  - Fase 3 (2.2-2.6s): fade out → slide reveal

---

### [v0.9.9] — 2026-05-19 · Pre-Release Polish

#### 🎨 Visual
- Gacha loading animation: portal berputar + teks "MEMANGGIL..." selama 1.2 detik

#### 🐛 Bug Fix
- Artifact slot awal repositioning (iterasi pertama, belum final)

---

### [v0.9.8] — 2026-05-19 · Inventory & Artifact UI

#### 🔴 Critical Fix
- **LEPAS item tidak berfungsi** — `Inventory.unequip(slot)` gagal jika bag penuh
  - Item dari slot tidak seharusnya cek bag capacity
- **Artifact dari gacha tidak masuk bag** — bug sama: bag full → `addItem()` return false

#### ✨ Feature
- **Tab ARTEFAK** di inventory filter — `instanceof Artifact` check
- **`showArtifactBagPopup()`** — popup klik artifact: icon, info, → SLOT 1/2
- **`showArtifactSlotInfo()`** — popup slot terisi: info + LEPAS ARTEFAK

---

### [v0.9.7] — 2026-05-18 · Lore Trigger Fix

#### 🔴 Critical Fix
- **Lore trigger v1** — `isCurrentRoomBoss()` cek floor number bukan room type
  - Fix: `room.getType() == RoomType.BOSS`
- `double-brace initialization` pada `final class` → error → variabel biasa

#### ✨ Feature
- **Artifact icons di combat** — row icon 36×36 per artifact, CD indicator
- **Gacha full-screen slide reveal** — per kartu dengan auto-advance 1.8s + skip
- **Merc artifact slot** dengan role filter

---

### [v0.9.6] — 2026-05-18 · Fog of War + Audio Fix

#### 🔴 Critical Fix
- **Map "dua pulau" bug** — `initFog()` pakai `isVisited()` bukan `isCleared()`
  - Fix: hanya cleared rooms masuk visitedTiles (permanent solid)
  - `syncPlayer()`: rebuild visibleTiles fresh tiap move
- **BGM error Windows** — JavaFX tidak support OGG di Windows
  - Fix: semua 15 file BGM dikonvert ke MP3

#### 🐛 Bug Fix
- `DungeonManager` hub-return: `setCurrentRoom()` sebelum emit `floorEntered`

---

### [v0.9.5] — 2026-05-18 · Map & Audio Wiring

#### 🔴 Critical Fix
- **Player selalu return ke room 0** — dua bug:
  1. `enterRoom()` tidak memanggil `setCurrentRoom()`
  2. `advanceToNextFloor()` emit SEBELUM `setCurrentRoom()` (terbalik)

#### 🎵 Audio
- BGM lengkap di semua `show*()` SceneRouter
- Boss BGM otomatis: `engine.getCombatBgm()` detect boss type

---

### [v0.9.4] — 2026-05-18 · Audio System

#### ✨ Feature
- **AudioManager** — Singleton, BGM 55% + SFX 60%
- 15 BGM MP3 + 8 SFX WAV semua terpasang
- `bgmForFloor(int)` — auto-pilih tema per 10 lantai

---

### [v0.9.3] — 2026-05-17 · Entity & Combat Fix

#### 🔴 Critical Fix
- **Guildmate tetap mati setelah hub** — `Entity.setHpDirect()` tidak reset `alive`
  - Fix: `setHpDirect()` + `alive = currentHp > 0`, `revive()` paksa `alive = true`
- **recalcEquipStats()** dipanggil di 3 tempat: startDungeonRun, returnToHub, restoreFromSave

#### 🎨 Visual
- **Main menu layout fix** — tombol di bawah logo (BorderPane.setBottom)
- 20 artifact icons di-import ke `assets/icons/artifact/`

---

### [v0.9.0–v0.9.2] — 2026-05-17 · Artifact System Launch

#### ✨ Feature (Gacha System)
- `GachaSystem.java` — pity 80, 6 rarity, rates
- `Artifact.java` — CD system, scaling per rarity
- `ArtifactType.java` — 20 jenis, 7 role
- `GachaView.java` — Altar Artefak
- `Inventory` artifact slots: equipArtifactToSlot(), unequipArtifact()
- `CombatManager` artifact auto-trigger setiap advanceTurn()

---

### [v0.8.9] — 2026-05-16 · Dialog System

#### ✨ Feature
- **CutsceneView** — Visual novel P5-style
  - `fadeBlack.setMouseTransparent(true)` — KRITIS agar pilihan bisa diklik
  - Portrait kiri, dialog bawah, pilihan kanan
- **21 dialog script** di `DialogScript.java`

---

### [v0.8.8] — 2026-05-16 · Save System

#### ✨ Feature
- **Java Serialization Save** — `%APPDATA%\MythicItemObtained\`
- 3 slot manual + 1 auto (`save_auto.dat`)
- `GameSaveState implements Serializable`
- `GameStateConverter.toSaveState()` + `restoreFromSave()`
- `SaveLoadView.java` — UI 3 slot dengan preview

---

### [v0.8.0–v0.8.7] — 2026-05-14-16 · Foundation Systems

#### ✨ Features
- DungeonManager state machine + ProceduralGenerator 12-kolom
- CombatView dengan turn order bar, sprites, floating damage, speed control
- Mercenary dialog system (50+ baris per guildmate)
- Workshop upgrade + kalibrasi

#### 🔴 Critical Fixes
- StatType renames: `CRIT_DMG_MULT→CRIT_DAMAGE`, `CDR→COOLDOWN_REDUCE`, `DMG_MULT→DAMAGE_MULT`
- `UIFactory.screenRoot()` hapus `setMinSize()` — fix overflow/sinking bug
- Boss SHIELD=0, EVASION awal fix

---

### [v0.7.9] — 2026-05-14 · Combat Engine Foundation

#### ✨ Feature
- `CombatManager` + Observer pattern (`Consumer<CombatEvent>`)
- `DamageCalculator` — formula DEF reduction, armor pierce, crit, min damage
- `SkillExecutor` — 40+ skill
- `TurnQueue` — PriorityQueue<Entity> by SPEED
- `CombatEvent` — 15+ event types

---

## 🚀 Cara Menjalankan

### Dev Mode (mvn)
```bash
git clone https://github.com/ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX
cd MythicItemObtained
mvn javafx:run
```

### Build EXE Portable
```bash
# Step 1: Build fat JAR
mvn clean package -DskipTests

# Step 2: Buat folder portable
mvn jpackage:jpackage

# Hasil: target/installer/MythicItemObtained/
# → Klik MythicItemObtained.exe langsung main!
```

**Prasyarat build:** JDK 25+ · Maven 3.8+
**Prasyarat EXE:** WiX Toolset 3.x (https://github.com/wixtoolset/wix3/releases)

---

## 🗺️ Peta Dunia & Lore

### Zona Dungeon

| Lantai | Zona | Boss | Red Shard |
|--------|------|------|-----------|
| 1–10 | 🕯️ Pasar Malam Gaib | Batara Kala | #1 |
| 11–20 | 🏛️ Candi Terlarang | Nyi Roro Kidul | #2 |
| 21–30 | 🌳 Hutan Angker | Rangda Agung | #3 |
| 31–40 | 🐉 Goa Naga | Garuda Mahaguru | #4 |
| 41–50 | ☁️ Kahyangan Rusak | Semar Pamungkas | #5 |
| 51 | ❄️ Void Dimension | Theresa (6 fase) | Red Blossom Katana |
| 52+ | 🔄 Endless Mode | Boss Random | Gacha Ticket |

### Boss Scaling (v1.0.4)
```
HP    = max(baseHP × 2.5, 10.000 + floor × 600)
ATK   = base × (1 + floor × 0.06) → max (400 + floor × 16)
DEF   = 0 (zero — boss kuat dari HP, bukan DEF)
SHIELD= 0 (currentShield = 0 juga!)
EVASION = max(baseEvasion × scale, 0.20)  ← cap 20%
```

### 7 Guildmate
| Nama | Alias | Role | HP | Weapon |
|------|-------|------|----|--------|
| Srikandi | Kira Voss | Pemanah Bayangan | 2.200 | Compound Crit |
| Gatot Kaca | Tank-RX9 | Ksatria Baja | 5.000 | Shield + Taunt |
| Nyai Roro | Sera Mend | Tabib Mistis | 2.400 | Mass Heal |
| Rangga | Vector | Pembunuh Bayaran | 1.800 | Execute |
| Bima | Magnus Forge | Petarung Agung | 3.200 | Armor Break |
| Ki Ageng | Echo Null | Dukun Tua | 2.200 | CC + Debuff |
| Dewi Sri | Lyra Bloom | Penjaga Keseimbangan | 2.500 | Barrier + Buff |

---

## 👥 Tim

**Kelompok 7 — Tugas Akhir PBO 2026 · BismilahFIX**

🔗 Repository: [ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX](https://github.com/ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX)

---

<div align="center">

```
⬡  Mythic Item Obtained  ·  v1.0.4  ·  Full Release
Built with Java 25 + JavaFX 25  ·  No External Game Engine
Tugas Akhir PBO 2026  ·  Kelompok 7  ·  BismilahFIX
```

*"Setiap detik tenang adalah persiapan untuk badai berikutnya."*
— Srikandi / Kira Voss

</div>
