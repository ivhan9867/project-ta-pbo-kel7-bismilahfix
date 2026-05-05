# ⚡ ARCLIGHT CITY — CHANGELOG

> Format: `[Versi] — Tanggal`
> Urutan: **terbaru di atas**, terlama di bawah.

---

## [v0.3.7] — 2026-04-30

### Added

**Floating Damage Numbers (CombatView.java)**
- Canvas overlay transparan di atas enemy section
- Warna per tipe: merah=damage, kuning=crit, hijau=heal, orange=DOT
- Crit: font lebih besar + tanda seru "150!"
- Animasi: melayang ke atas 55px dalam 1.4 detik + fade out
- Canvas `mouseTransparent=true` — tidak halangi klik enemy

**Floor Transition Animation (DungeonMapView.java)**
- Overlay hitam fade in → teks "DESCENDING / FLOOR X" → engine.descend() → fade out
- Total durasi 1600ms, teks "SECTOR LOADED" hijau setelah descend selesai

**Room Preview di Hover Tooltip (DungeonGridMap.java)**
- Baris kedua di tooltip untuk ENEMY room: jumlah + nama enemy
- REST room: info heal sesuai restUseCount yang tersisa
- Tooltip height dan width dinamis sesuai konten

---

## [v0.3.6] — 2026-04-30

### Added

**Mercenary Hire System (ViewsBundle.java)**
- Tab HIRE di MercenaryView — daftar 7 merc dengan stats preview + harga
- Tombol HIRE disabled jika gold kurang, badge "ALREADY IN ROSTER" jika sudah punya
- Setelah hire: notifikasi chat panel

**Status Effect Tooltip (UIFactory.effectBadge)**
- Hover badge → tooltip: nama, deskripsi, sisa durasi dalam turn

### Changed

**MercenaryViewImpl — full rewrite ke BorderPane + tab ROSTER / HIRE**

---

## [v0.3.5] — 2026-04-30

### Fixed

**[CRITICAL] EXP dan Gold tidak diterapkan setelah combat**
- `setCombatEndListener` tidak memanggil `player.gainExp()` / `player.addGold()`
- Fix: keduanya sekarang dipanggil, level up emit `DungeonStateEvent.LEVEL_UP`

**Shop harga berubah tiap render**
- `Math.random()` → deterministik dari `item.getId().hashCode()`

**Combat log tidak di-clear saat combat baru**
- `startCombatLoop()` sekarang clear logContainer di awal

**GameOver retry tidak reset save**
- `SaveManager.deleteAllSaves()` dipanggil saat retry

### Added

**Level Up Notification** — alert + system chat setelah naik level

**Profile Tab EQUIPMENT** — slot equipped + item di bag dengan stats

**Profile Tab SKILLS** — equip/unequip/unlock langsung dari UI, skill points badge

**Player.addGold()** — method baru

**DungeonStateEvent.LEVEL_UP** — type baru di enum

**MercenaryDialogue — DUNGEON_ENTER_FLOOR trigger dipasang**

### Changed

**ProfileView — complete rewrite** ke BorderPane + 3 tab fungsional (STATS/EQUIPMENT/SKILLS)

**SceneRouter — tambah `showProfile(String tab)` overload**

---

## [v0.3.4] — 2026-04-30

### Added

**Save/Load System — Java Serialization (package arclightcity.save)**

File baru:
- `GameSaveState.java` — snapshot: PlayerData, MercData, ItemData, ProgressData
- `SaveManager.java` — IO ke disk: `save_manual.dat` + `save_auto.dat` di `%APPDATA%\ArclightCity\`
- `GameStateConverter.java` — konversi GameEngine ↔ GameSaveState

GameEngine: `saveGame()`, `autoSave()`, `loadGame()`, `hasSave()`, `getSaveSummary()`

Auto-save setiap `engine.descend()` dipanggil.

UI: CONTINUE button aktif di Main Menu jika ada save, SAVE GAME button di Hub (kuning), hasil muncul di chat panel.

Player: `setLevelDirect()`, `setExpDirect()`, `setGold()`, `setHpDirect()`, `setMpDirect()`, `setShieldDirect()`

Inventory: `forceEquipWeapon/Armor/Accessory1/2()` untuk restore

---

## [v0.3.3] — 2026-04-30

### Fixed

**[CRITICAL] Layout menyeluruh — semua konten tidak lagi tenggelam**

Root cause: `UIFactory.screenRoot()` set `setMinSize(GAME_WIDTH, SCREEN_HEIGHT)` → VBox dipaksa overflow.

Fix:
1. `UIFactory.screenRoot()` — hapus `setMinSize`, tambah `setMaxHeight`
2. `UIFactory.screenRootBorder()` — helper baru return `BorderPane`
3. `CombatView` → BorderPane: top=header+turnbar, center=scrollable, bottom=actionPanel
4. `DungeonMapView` → BorderPane: top=header+vitals, center=map+info, bottom=roomActions
5. `wireEngineListeners()` dipindah ke `build()`

---

## [v0.3.2] — 2026-04-30

### Fixed

**Enemy card kosong saat target select mode**
- `return card` dipanggil sebelum konten ditambahkan
- Fix: tambah label "▶ SELECT TARGET" + nameRow + bars sebelum return

**Hub bottom nav tenggelam — definitif fix**
- Root `VBox` → `BorderPane`: `setTop()` player bar, `setCenter()` ScrollPane, `setBottom()` nav
- `BorderPane.bottom` dijamin selalu terlihat oleh JavaFX layout engine

---

## [v0.3.1] — 2026-04-30

### Fixed

**Combat screen terpotong — window terlalu pendek**
- Window 820px → 920px (+100px vertikal)

**Turn AI terlalu cepat**
- Default `combatSpeedMs` 500ms → 1200ms
- Speed buttons: `1×=1200ms`, `2×=500ms`, `SKIP=50ms`

### Changed

**Combat layout compact** — padding/spacing dikurangi, log 130 → 150px, turn bar fixed 36px

---

## [v0.3.0] — 2026-04-30

### Fixed

**Hub bottom nav terpotong**
- `buildBottomNav()` sekarang `setMinHeight(64)`, `setMaxHeight(64)`, `setPrefHeight(64)`

### Added

**Turn Order Bar** — antrian giliran 6 entity dengan HP bar, warna per faction

**Skill Selection Popup** — klik SKILL → popup daftar skill dengan deskripsi + CD

**Target Selection Mode** — klik ATTACK/skill → enemy card border kuning glow, klik untuk target

**Combat Speed Control** — tombol 1× / 2× / SKIP di atas action buttons

**SkillInfo Database** — 12 skill dengan nama, deskripsi, MP cost, AoE flag

### Changed

**`refreshActionPanel`** — target select mode menggantikan seluruh panel saat aktif

**`handleCombatEvent`** — AI delay pakai `combatSpeedMs`, refresh turn bar setiap event

**`TurnQueue.getUpcomingTurns(n)`** — method baru, simulasi round berikutnya jika slot kurang

---

## [v0.2.8.1] — 2026-04-30

### Fixed

**EventView choices terpotong** — hapus spacer(), wrap dalam ScrollPane

**ShopView VBox.setVgrow** tidak perlu pada itemList yang sudah di ScrollPane

**VictoryView & GameOverView** — wrap content dalam ScrollPane untuk safety

---

## [v0.2.8] — 2026-04-30

### Changed

**Dungeon Grid Map — Visual Overhaul v3 (DungeonGridMap.java)**

| Aspek | Sebelum | Sesudah |
|-------|---------|---------|
| Tile size | 42×42px | 52×52px |
| Background | Flat | Dot grid 16px spacing |
| Player | Statis | Breathing pulse 3 ring |
| Reachable border | Statis | Marching ants bergerak |
| Hover | Nama saja | Nama + info room |
| Cleared tiles | Redup | Diagonal stroke + desaturated |
| Boss tile | Border merah | Double outer ring |
| Hidden tiles | Flat hitam | Cross-hatch pattern |

---

## [v0.2.7.1] — 2026-04-29

### Fixed

**Hub bottom nav terpotong** — ScrollPane wrap area tengah, player bar + nav fixed

**Combat action panel terpotong** — hapus spacer() + VBox.setVgrow(logScroll)

---

## [v0.2.7] — 2026-04-29

### Changed

**Comprehensive UI Font Pass**

| Elemen | Sebelum | Sesudah |
|--------|---------|---------|
| CSS root | 13px | 14px |
| Enemy card nama | 11px | 14px |
| Ally card lebar | 110px | 150px |
| Skill slot | 72×44px | 110×52px |
| Action button | 110px wide | 130px wide |
| Combat log | 110px tall | 130px tall |
| CharCreate lore | 10px | 12px |

---

## [v0.2.6] — 2026-04-29

### Added

**Split Layout — 860×820px** — game area 560px (kiri) + chat panel 300px (kanan)

**MercChatPanel** — panel chat 300px persistent semua screen, bubble per merc dengan warna unik

**MercenaryDialogue** — 150+ dialog × 7 merc × 17 trigger, kepribadian unik tiap merc

**Chat triggers** — HUB_IDLE, HUB_ENTER_DUNGEON, room type triggers, COMBAT_VICTORY/DEFEAT

**CRAFT button → "Coming Soon" di chat**

---

## [v0.2.5] — 2026-04-29

### Fixed

**REST room tidak bisa dikunjungi lagi** — guard `enterRoom()` dikecualikan untuk REST

**Skill slot selalu kosong** — `createCharacter()` memanggil `giveStarterSkills()`

### Added

**REST Diminishing Heal** — visit 1: +35%/+50%, visit 2: +20%/+30%, visit 3: +10%/+15%, visit 4+: nihil

**Starter Skills per Background** — 2 skill langsung unlock + equip sesuai background

**Shop Fungsional** — 4 item random, harga per rarity, BUY disable jika kurang gold

---

## [v0.2.4] — 2026-04-29

### Fixed

**[CRITICAL] Backtrack re-trigger event** — guard cleared + koneksi symmetric `Floor.moveToRoom()`

---

## [v0.2.3.1] — 2026-04-29

### Fixed

**[CRITICAL] ProceduralGenerator duplikat** — konten file di bawah class closing brace, brace 30:30 ✓

---

## [v0.2.3] — 2026-04-29

### Added

**Full Grid Exploration** — COLS×ROWS tile, boss di tengah baris terakhir, cardinal connections

**Fog of War 3 State** — Hidden / Visible / Visited

**DESCEND terkunci** hingga boss dikalahkan

**Map Legend**

---

## [v0.2.2] — 2026-04-28

### Fixed

**[CRITICAL] ConcurrentModificationException** — iterate snapshot `activeEffects`

### Added

**Dungeon Grid Map v1** — Canvas 2D, animasi ease in-out, klik tile

---

## [v0.2.1] — 2026-04-27

### Fixed

**[CRITICAL] Mercenary duplicate** — `clear()` sebelum tambah starter merc

**Background putih ScrollPane** — `-fx-background: #050810`

**Border equipment slot selalu hijau** — gunakan `rarityColor()` per item

**Title terlalu blur** — DropShadow radius 20 → 6

---

## [v0.2.0] — 2026-04-27

### Fixed

**[CRITICAL] AI Turn Loop Stacking** — guard `aiTurnPending`

**[CRITICAL] DungeonManager tidak reset** — `startDungeonRun()` buat DungeonManager baru

**Loot Room tidak generate item** — `LOOT_FOUND` → `LootManager.generateLoot()`

### Added

**Loot Popup**, **Rest Popup**, **`showInfoAlert()` helper**

---

## [v0.1.0] — 2026-04-26

### Release Awal

Game end-to-end setelah migrasi NetBeans Ant → Maven.

```bash
cd ArclightCity
mvn javafx:run
```

---

## [v0.4.0] — 2026-05-01

### Changed — MAJOR: Konversi Tema Nusantara + Judul Baru

**Judul Game: "Arclight City" → "Mythic Item Obtained"**
- `ArclightApp.java`: stage title diubah
- `pom.xml`: artifactId dan name diubah
- `MainMenuView.java`: teks judul diubah

**Tema UI: Cyberpunk Cyan → Nusantara Gold/Merah/Batik**
- `arclight.css`: full rewrite — palette baru:
  BG `#0A0604` (hitam hangat), gold utama `#C8860A`, gold terang `#FFB830`,
  merah bata `#CC2200`, hijau tua `#44AA44`, teks kertas tua `#EDE0C8`
- `UIFactory.java`: semua konstanta warna dikonversi ke palette Nusantara

**Player Background → Asal Usul Nusantara (display name)**
- Street Brawler → **Pendekar Betawi**
- Netrunner → **Dukun Digital**
- Veteran Soldier → **Prajurit Majapahit**
- Energy Adept → **Pawang Neon**
- Ghost Operative → **Mata-mata Demak**
- Techwright → **Empu Modern**

**Mercenary → Tokoh Nusantara (display name)**
- Kira Voss → **Srikandi** (Pemanah Bayangan)
- Tank-RX9 → **Gatot Kaca** (Ksatria Baja)
- Sera Mend → **Nyai Roro** (Tabib Mistis)
- Vector → **Rangga** (Pembunuh Bayaran)
- Magnus Forge → **Bima** (Petarung Agung)
- Echo Null → **Ki Ageng** (Dukun Tua)
- Lyra Bloom → **Dewi Sri** (Penjaga Keseimbangan)

**Enemy → Makhluk Mitologi Nusantara**
- Street Thug → **Leak Pengembara**
- Neon Serpent → **Naga Basuki**
- Glitch Drone → **Genderuwo Mekanik**
- Iron Clad → **Raksasa Kala**
- Void Specter → **Kuntilanak Abadi**
- Null King → **Batara Kala**

**Floor Themes → Lokasi Nusantara**
- Neon Slum → **Pasar Malam Gaib**
- Corporate HQ → **Candi Terlarang**
- Data Vault → **Hutan Angker**
- Neon Wastes → **Goa Naga**
- Void Rift → **Kahyangan Rusak**

**MercChatPanel**
- Header "CREW COMMS" → "BISIK KAWULA"
- "Waiting for crew to speak" → "Menunggu kawula berbicara"
- Warna bubble merc disesuaikan tema batik

**Skill display names di CombatView**
- POWER STRIKE → PUKULAN HARIMAU
- PHANTOM SHOT → PANAH BAYANGAN
- SHADOW STEP → LANGKAH GAIB
- DEEP HACK → SANTET DIGITAL
- EXECUTE → TEBASAN

### Fixed (poin 1-10 dari audit)

**Poin 2: Loyalty mercenary naik setelah combat victory**
- `activeMercs.forEach(m -> m.completeMission())` dipanggil di `setCombatEndListener`

**Poin 3: Hub district name dinamis berdasarkan floor depth**
- Floor 0-3: Pasar Malam Gaib | 4-6: Candi Terlarang | 7-10: Hutan Angker
- 11-15: Goa Naga | 16+: Kahyangan Rusak

---

## [v0.4.1] — 2026-05-01

### Added

**[CONTENT] 15 Enemy Baru (total 20 enemy + 5 boss)**

Standard enemies baru (Floor 1-6):
- **Tuyul Pencuri** — SPIRIT, steal gold saat hit, evasion 20%
- **Wewe Gombel** — SPIRIT, steal buff dari player, medium threat
- **Pocong Listrik** — SPIRIT, shock + AoE shockwave saat desperate
- **Banaspati** — SPIRIT, burn DOT AoE setiap giliran
- **Babi Ngepet** — BEAST, HP tinggi 150, gold reward 80 (tertinggi standard)

Elite enemies baru (Floor 5-17):
- **Rangda Merah** — DEMON, Physical DEF 50 (praktis kebal fisik)
- **Barong Rusak** — BEAST, toggle ATK/DEF mode setiap turn
- **Leyak Api** — SPIRIT, AoE burn setiap giliran, evasion 15%
- **Garuda Korup** — BEAST, evasion 25%, crit tinggi, dive AoE
- **Detya Wesi** — GIANT, armor stacking +8 DEF per turn, HP 280

Boss baru (milestone floors):
- **Nyi Roro Kidul** — Floor 8, DIVINE, AoE Energy + debuff kurse (HP 800)
- **Rangda Agung** — Floor 12, DEMON, Null Field + AoE curse (HP 1100)
- **Garuda Mahaguru** — Floor 16, DIVINE, 4 fase, evasion 30%, crit 25% (HP 1600)
- **Semar Pamungkas** — Floor 20, DIVINE, FINAL BOSS, HP 3000, random skill setiap turn,
  HP regen 20/turn, guaranteed Mythic Fragment ×2

**[SYSTEM] EnemyRace baru — SPIRIT, GIANT, DEMON, DIVINE**

**[SYSTEM] Enemy helper methods baru (Enemy.java)**
- `getHighestAtkTarget()` — target dengan ATK tertinggi
- `getFastestTarget()` — target dengan SPEED tertinggi

**[SYSTEM] Encounter generation per floor range**
- Floor 1-3: Leak Pengembara, Tuyul Pencuri, Naga Basuki
- Floor 4-6: Wewe Gombel, Banaspati, Pocong Listrik
- Floor 7-10: Babi Ngepet, Barong Rusak, Leyak Api
- Floor 11-15: Garuda Korup, Detya Wesi, Rangda Merah
- Floor 16+: Garuda Korup, Detya Wesi (hard), Garuda Mahaguru

**[SYSTEM] Boss per floor milestone**
- Floor ≤5: Batara Kala | ≤8: Nyi Roro Kidul | ≤12: Rangda Agung
- ≤16: Garuda Mahaguru | ≥20: Semar Pamungkas

**[FEATURE] Tier MYTHIC — Rarity ke-6 (Item.java)**
- Warna: `#FF6B00` (oranye api)
- Stat multiplier: 5.0× (vs Legendary 3.0×)
- Upgrade slots: 25 (vs Legendary 15)
- **TIDAK bisa drop dari loot biasa** — hanya dari boss/craft/trade

**[FEATURE] Mythic Weapon System (LootManager.java)**
- 10 Mythic weapon unik dengan stat jauh di atas Legendary
- Keris Naga Raja, Cakra Wisnu, Tombak Inti Bumi, Kujang Bintang,
  Golok Roh Purba, Trisula Samudra, Panah Angin Sakti, Cemeti Kilat,
  Mandau Dayak Agung, Pedang Surya
- `generateMythicDrop()` — pilih random 1 dari 10
- `generateMythicFragment()` — material untuk craft

**[FEATURE] Mythic Fragment Craft System (GameEngine.java)**
- Setiap boss dikalahkan → guaranteed 1 Mythic Fragment masuk inventory
- Kumpulkan 3 Fragment → auto-craft 1 Mythic weapon random

**[FEATURE] Material.MaterialType.MYTHIC_FRAGMENT**

**[CONTENT] Nama weapon dikonversi ke Nusantara (LootManager.java)**
- Physical: Keris Pamor, Golok Siluman, Tombak Rajawali, Kujang Sakti, Mandau Dayak
- Cyber: Santet Kristal, Rajah Perusak, Ilmu Hitam Runcing, Keris Cyber, Tombak Roh Data
- Energy: Cakra Neon, Panah Petir, Trisula Energi, Lembing Surya, Cahaya Kahyangan

---

## [v0.3.7d] — 2026-05-01

### Fixed — Critical

**[BUG ROOT CAUSE] MercChatPanel tidak pernah tertrigger sejak versi pertama (SceneRouter.java)**

Root cause ditemukan setelah audit mendalam:

Setiap kali navigasi antar screen, `setSceneWithChat()` membuat `new HBox()` baru dan `new Scene()` baru, lalu menambahkan `chatPanel` ke HBox tersebut.

Di JavaFX, sebuah Node hanya boleh memiliki satu parent. Ketika `chatPanel` ditambahkan ke HBox baru, JavaFX **otomatis melepaskan `chatPanel` dari parent lamanya**. Akibatnya:
- `messageContainer` (VBox internal chatPanel) kehilangan parent-nya
- Semua pesan yang dikirim via `addMercMessage()` diterima tapi tidak pernah ter-render ke screen yang tampil
- Panel terlihat kosong atau hanya menampilkan welcome message awal

Fix definitif — **arsitektur Single Persistent Scene**:
- `persistentLayout` (HBox): dibuat SEKALI di constructor, tidak pernah diganti
- `gameArea` (StackPane): hanya isinya yang diganti saat navigasi, bukan container-nya
- `chatPanel`: selalu ada di `persistentLayout`, tidak pernah dipindahkan
- Scene dibuat SEKALI di constructor SceneRouter, `stage.setScene()` tidak pernah dipanggil lagi
- `showWithChat()`: ganti `gameArea.getChildren().setAll(content)` — bukan buat scene baru
- `showFullWidth()`: sembunyikan chatPanel via `setVisible(false)` untuk main menu
- Semua `emitChat()` dan `addSystemChat()` dibungkus `Platform.runLater()` agar eksekusi setelah JavaFX render selesai

### Fixed — Poin 1-8

**[POIN 1] Inkonsistensi tema Nusantara — consumable names**
- Health Pack → **Jamu Kunyit** (heal HP)
- MP Injector → **Tirta Mahkota** (restore MP)
- Antidote → **Daun Suruh Sakti** (cleanse DOT)
- Stim Pack → **Sesajen Kekuatan** (buff item)

**[POIN 1] Inkonsistensi tema Nusantara — armor & accessory names**
- Armor: Baju Zirah Majapahit, Tameng Naga, Kain Batik Pelindung, Rompi Rajah, Zirah Gaib, Perisai Garuda, Baju Besi Empu
- Accessory: Gelang Rajah, Kalung Garuda, Cincin Semar, Jimat Naga, Amulet Kahyangan, Gelang Kala, Cincin Pamor, Keris Mini

**[POIN 1] DungeonEvent narratives dikonversi ke Nusantara**
- "Calibration Terminal / MegaCorp" → "Altar Kalibrasi kuno"
- "Neon Fountain / cairan neon" → "Sumber Mata Air Gaib"
- "Data Cache / modul memori" → "Gulungan Ilmu Kuno / rontal"
- "Electric Trap" → "Jebakan Petir Raksasa / rajah"
- "Security Alarm / korporat" → "Gong Peringatan / pasukan gaib"
- "Neon Burn / pipa energi neon" → "Api Banaspati"
- "Mystery Container" → "Peti Misterius"
- "Wandering Merchant" → "Pedagang Gaib"
- "Corrupted Cache / virus digital" → "Harta Terkutuk / kutukan"

**[POIN 2] Skill descriptions dikonversi ke Nusantara (CombatView.SkillInfo)**
- Power Strike → Pukulan Harimau
- Execute → Tebasan Pamungkas
- Deep Hack → Santet Digital
- Virus Upload → Upload Santet
- Phantom Shot → Panah Bayangan
- Shadow Step → Langkah Gaib
- Iron Shield → Tameng Baja
- Shockwave → Gempa Bumi
- Energy Drain → Serap Tenaga
- Bio Irradiate → Racun Semesta
- EMP Burst → Ledakan Petir
- Field Barrier → Rajah Pelindung

**[POIN 5] Mythic visual eksklusif di inventory (ViewsBundle.java)**
- Item Mythic: nama dengan glow oranye `dropshadow(gaussian, #FF6B00, 8, 0.5, 0, 0)`
- Badge `✦ MYTHIC ✦` dengan glow menggantikan `[Mythic]` biasa

**[POIN 6] Mythic Fragment counter di Hub (HubView.java)**
- Counter `✦ X/3` di header bar player
- Oranye jika ada fragment, redup jika belum
- Tooltip: info cara craft + status fragment saat ini

**[POIN 7] Semar Pamungkas balance (SemarPamungkas.java)**
- HP regen: 20/turn → 8/turn (masih challenge tapi tidak impossible)
- Shield regen: 15/turn → 5/turn

---

## [v0.4.2] — 2026-05-01

### Fixed — Critical

**[BUG] Gold & EXP double-apply setelah combat**

Root cause: `CombatManager.buildVictoryResult()` sudah memanggil `player.gainExp()` dan `player.gainGold()` secara langsung. Tapi `GameEngine.setCombatEndListener` juga memanggil `player.gainExp()` dan `player.addGold()` lagi dari `result.getTotalExpGained/GoldGained()`.

Akibatnya: setiap kali menang combat, player mendapat EXP dan Gold **dua kali lipat** dari seharusnya.

Fix: Hapus pemanggilan `gainExp()` dan `addGold()` dari `GameEngine.setCombatEndListener`. Engine sekarang hanya handle: loot drop, level up notification, dan Mythic fragment. EXP dan Gold tetap diapply oleh CombatManager (satu-satunya tempat yang benar).

**[BUG] gameArea width tidak di-reset setelah showFullWidth (SceneRouter.java)**

`showFullWidth()` mengubah `gameArea.setPrefWidth(SCREEN_WIDTH)` untuk tampilkan main menu/char create full width, tapi `showWithChat()` tidak me-reset width kembali ke `GAME_WIDTH`. Akibatnya, saat player masuk Hub setelah char create, game area masih 860px dan chat panel ter-push keluar layar.

Fix: `showWithChat()` sekarang reset `gameArea.setPrefSize(GAME_WIDTH, SCREEN_HEIGHT)` dan `setMaxWidth/setMinWidth(GAME_WIDTH)` sebelum set konten.

**[BUG] Boss baru tidak implement `onPhaseTransition()` (abstract method di Boss.java)**

Keempat boss baru (NyiRoroKidul, RangdaAgung, GarudaMahaguru, SemarPamungkas) tidak mengimplementasikan `onPhaseTransition(int, int)` yang abstract di `Boss.java`. Ini akan menyebabkan compile error.

Fix: Tambahkan `@Override protected void onPhaseTransition(int fromPhase, int toPhase) {}` ke semua boss baru.

### Changed

**CombatResult — tambah `levelsGained` field**
- `levelsGained` field baru (mutable, default 0)
- `getLevelsGained()` dan `setLevelsGained(int)` getter/setter baru
- `CombatManager.buildVictoryResult()` sekarang set `result.setLevelsGained(n)` setelah `player.gainExp()`
- `GameEngine` menggunakan `result.getLevelsGained()` untuk trigger level up notification

**DungeonStateEvent — tambah `mythicCraft()` factory**
- Notification khusus saat 3 Mythic Fragment berhasil di-craft
- Pakai Type.LEVEL_UP untuk tampil sebagai notifikasi di DungeonMapView

---

## [v0.4.4] — 2026-05-02

### Changed — GUI Total Overhaul

**MainMenuView — Total Redesign**
- Judul besar MYTHIC (52px) + ITEM OBTAINED (28px) dengan double gold glow animasi pulse
- Ornamen batik: garis vertikal kiri/kanan gradient, diamond di pojok
- Tagline "✦ MASUKI DUNIA GAIB NUSANTARA ✦"
- Tombol MULAI PETUALANGAN (gold filled) + LANJUTKAN (dengan save info)
- Tombol KELUAR dengan hover merah

**CharacterCreateView — Total Redesign**
- Header dengan tombol KEMBALI + ornamen gold
- Input nama pendekar dark parchment style
- Background card: ikon emoji + nama Nusantara + lore + starter skills
- Card yang dipilih: border gold + glow + badge ✦
- Bottom bar fixed: tampilkan asal usul + tombol MULAI PETUALANGAN

**HubView — Total Redesign**
- Identity bar: avatar circle + nama gold glow + badge level/background/depth + gold + fragment counter
- Vitals section: EXP bar gradient + HP/Shield/MP compact
- District banner: nama lokasi dinamis berdasarkan depth
- Mythic Fragment progress bar dengan dot indicators (muncul jika punya fragment)
- Navigation grid: MASUK DUNGEON (primary full-width) + 2×2 grid untuk kawula/item/profil/simpan
  - Setiap tombol punya subtitle deskripsi
  - Hover effect: icon + label berubah warna gold
- Crew preview: daftar kawula aktif dengan vitals bars mini
- Quick bar bottom: 4 ikon navigasi cepat dengan hover gold

**VictoryView — Total Redesign**
- Header "✦ KEMENANGAN ✦" dengan double gold glow
- Notifikasi boss kill + fragment info
- Reward panel border gold dengan semua reward
- Tombol LANJUT JELAJAH + KEMBALI KE MARKAS

**GameOverView — Total Redesign + Fix**
- Judul "◆ GUGUR ◆" merah dengan animasi flicker
- Kutipan Nusantara random
- 3 tombol: COBA LAGI | KEMBALI KE MARKAS | MENU UTAMA
- "Kembali ke Markas" → bukan ke main menu, tapi ke hub existing

**Lokalisasi penuh Bahasa Indonesia**
- HubView: ENTER DUNGEON → MASUK DUNGEON, MERCENARY → KAWULA, dll
- DungeonMapView: FLOOR → LANTAI, DESCEND → TURUN, BOSS DEFEATED → BOSS DIKALAHKAN
- CombatView: ENEMIES → LAWAN, ALLIES → SEKUTU, ATTACK → SERANG, SKILL → JURUS, FLEE → KABUR, DEFEND → BERTAHAN
- ViewsBundle: HIRE → REKRUT, ROSTER → REGU, BUY → BELI, EQUIP → PAKAI, dll

---

## [v0.4.5] — 2026-05-02

### Changed — Polish Menyeluruh Semua View

**DungeonMapView**
- Header diganti: custom HBox dengan tombol KEMBALI, judul lantai gold glow, counter emas
- Semua warna lama (cyberpunk) diganti ke palette Nusantara Dark Gold
- GridScroll border: #1C2E44 → #3A2810
- Bottom bar: #0C1220 → #0F0A06

**InventoryView (ViewsBundle)**
- Layout: VBox (overflow) → BorderPane (fixed)
- Custom header dengan tombol KEMBALI + judul "PERBENDAHARAAN" + gold counter
- Item list di center ScrollPane, tidak overflow

**ShopView (ViewsBundle)**
- Layout: VBox → BorderPane
- Custom header + Merchant info panel bergaya Nusantara
- "WANDERING MERCHANT" → "PEDAGANG GAIB"
- "I have just what you need..." → kutipan Bahasa Indonesia
- "LEAVE SHOP" → "TINGGALKAN PASAR"
- "FOR SALE" → "BARANG DAGANGAN"

**EventView (ViewsBundle) — Total Redesign**
- Layout: VBox → BorderPane
- Header dinamis per kategori event:
  - POSITIVE (✦ KEBERUNTUNGAN) → hijau jade
  - NEGATIVE (☠ BAHAYA) → merah api
  - CHOICE (◈ PILIHAN GAIB) → gold batik
  - NEUTRAL (— PERISTIWA) → abu redup
- Judul event dengan double dropshadow warna kategori
- Narrative panel parchment style dengan border kiri 3px warna kategori
- Tombol pilihan: hover berubah warna sesuai kategori + glow
- "CHOOSE YOUR ACTION" → "PILIH TINDAKANMU"
- "CONTINUE" → "LANJUTKAN PERJALANAN"

**ProfileView — Lokalisasi + Polish**
- Header custom Nusantara (konsisten dengan view lain)
- Tab bar: STATS/EQUIPMENT/SKILLS → STATISTIK/PERLENGKAPAN/JURUS
- Semua label stat dilokalkan: "Physical ATK" → "ATK Fisik", dll
- Section headers: CHARACTER/VITALS/OFFENSE/DEFENSE/UTILITY → KARAKTER/VITAL/SERANGAN/PERTAHANAN/UTILITAS
- Tombol EQUIP/UNEQUIP/UNLOCK → PASANG/LEPAS/BUKA
- Skill status "EQUIPPED/UNLOCKED/READY" → "AKTIF/TERBUKA/SIAP"

**MercChatPanel**
- Header: "BISIK KAWULA" → "✦ BISIK KAWULA"
- Placeholder: "Menunggu kawula berbicara" → "Kawula menunggu di sudut gelap..."

**Semua View — Color Token Update**
- #050810 → #0A0604 (bg deep)
- #080D18 → #0F0A06 (bg panel)
- #0C1220 → #150E08 (panel)
- #1C2E44 → #3A2810 (border)
- #5A6A80 → #6A5840 (text dim)
- #00E5FF → #C8860A (accent)
- #FFD600 → #FFB830 (highlight)
- #FF1744 → #CC3300 (danger)
- #00E676 → #2D7A45 (success)
