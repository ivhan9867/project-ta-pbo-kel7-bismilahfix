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
