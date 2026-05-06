# MYTHIC ITEM OBTAINED — CHANGELOG

> Urutan terbaru di atas. Format: `[vX.Y.Z] — tanggal`

---

## [v0.6.0] — 2026-05-07

### MAJOR — Combat Overhaul + System Polish

**Combat Visual Overhaul**
- Log pertempuran dihapus — diganti floating damage numbers yang kaya visual
- Floating damage: 8 tipe visual berbeda (crit gold+wobble, heal hijau, DOT coklat, skill gold, evade biru, block, status, defeated)
- Shadow/outline pada semua angka floating untuk keterbacaan
- Durasi dan rise berbeda per tipe: crit 1800ms/75px, skill 1600ms/65px, normal 1400ms/55px
- Enemy card: gradient background + left border tebal + glow merah saat giliran
- Ally card: gradient gold saat aktif
- Turn order bar: gradient merah + glow bawah
- Action panel: gradient naik + border gold halus
- battleArea: gradient 3 stop vertikal

**Fix Save/Load Item (Definitif)**
- Root cause: `getBonusStats()` return unmodifiable map → UnsupportedOperationException
- Fix: `Equipment.restoreBonusStats()` akses map internal langsung
- Fix: `setUpgradeLevelDirect()` — tidak trigger random bonus saat load
- `activeMercs` sekarang tersimpan dan di-restore → party langsung ready setelah load

**Map Reveal (Event Dungeon)**
- `revealAll()` di DungeonGridMap — reveal semua tiles sekaligus
- Toast hijau + system chat saat peta terbuka
- Room ditandai `visited = true` via `setVisited()`

**Lokalisasi Penuh Combat**
- DamageType: Physical→Fisik, Energy→Energi, True→Mutlak, Heal→Pulih
- CombatEvent: semua pesan bahasa Indonesia
- "No active effects" → "Tidak ada efek aktif"

---

## [v0.5.8] — 2026-05-06

### UI Polish & Animation Update

**Toast Notification — Single Line**
- Redesign dari VBox vertikal menjadi HBox horizontal satu baris
- Format: `● JUDUL │ isi pesan` — tidak pernah wrap/melebar
- Slide masuk dari kiri (180ms, ease-out), fade out otomatis 2.4 detik
- Ukuran terkontrol, tidak mengganggu gameplay

**CombatView — Tanpa Scroll di 720px**
- Hapus `ScrollPane` di battle area — konten langsung di `BorderPane.center`
- Enemy section, ally section, dan log dikompakkan
- Log area: 150px → 80px; turn bar: 36px → 28px
- Combat sekarang muat penuh di 720px tanpa scroll

**Animasi Baru (UIFactory)**
- `goldPulse(node, color)` — opacity loop untuk elemen penting Hub
- `flicker(node)` — kedip random, aktif di judul PERTEMPURAN saat boss
- `typewriter(label, text, ms)` — teks muncul karakter per karakter
- `scanlineEffect(panel)` — garis scan bergerak loop dari atas ke bawah
- Judul "MYTHIC ITEM OBTAINED" di Hub: animasi gold pulse aktif
- Judul combat saat boss: flicker effect

**Fix Floor Display**
- Hub tampil "Lantai 0" saat fresh start → sekarang "Lantai 1"

---

## [v0.5.7] — 2026-05-06

### Layout Adjustment untuk 1280×720

**Padding dikurangi di 5 view** (HubView, CharacterCreateView, ProfileView, CityView, ViewsBundle):
- `Insets(20,20,20,20)` → `Insets(8,12,8,12)`
- `Insets(16,16,16,16)` → `Insets(8,12,8,12)`
- Button padding: `14 20` → `10 16`

**HubView** — tambah `ScrollPane` di center, spacing antar section dikurangi

**CombatView** — log area 150px → 100px, action panel padding lebih kecil

---

## [v0.5.6] — 2026-05-06

### Floor Bug Fix + Resolusi 1280×720

**Bug Floor Skip (Critical Fix)**
- Root cause: `startDungeon` set `currentFloorNumber = savedFloor` lalu langsung panggil `advanceToNextFloor()` → floor selalu +1 setiap masuk dungeon
- Fix: cek `savedFloor <= 0` → fresh start (advance); `savedFloor > 0` → load floor langsung tanpa increment

**Resolusi 1280×720**
- `SCREEN_WIDTH`: 860 → 1280
- `SCREEN_HEIGHT`: 920 → 720
- `GAME_WIDTH`: 560 → 940
- `CHAT_WIDTH`: 300 → 340
- Tile dungeon grid: 52px → 64px
- Layout landscape siap untuk cutscene video ending

**Toast Compact (awal)**
- Ukuran max 280px, slide dari kiri, font lebih kecil

---

## [v0.5.5f] — 2026-05-06

### Fix COMBAT_WIN → COMBAT_VICTORY

- `MercenaryDialogue.Trigger.COMBAT_WIN` tidak ada → diganti `COMBAT_VICTORY`

---

## [v0.5.5e] — 2026-05-06

### Fix Level Up Hilang + Boss Defeat + Map Reset

**Root Cause Level Up Hilang**
- `CombatView.addResultListener` override listener `DungeonManager` karena pakai single slot
- Fix: kembali ke list listener dengan cap 2, `DungeonManager.clearResultListeners()` sebelum tiap combat

**Boss Defeat Notification**
- Tambah `DungeonStateEvent.Type.BOSS_DEFEATED` dan `MYTHIC_CRAFT`
- Toast + system chat saat boss dikalahkan
- `bossDefeated()` factory method di `DungeonStateEvent`

**Map Reset saat Floor Baru**
- `FLOOR_ENTERED` sekarang panggil `router.showDungeonMap()` dengan delay 300ms

---

## [v0.5.5d] — 2026-05-06

### Fix Root Cause Notif Level Up Spam (Definitif)

**Root Cause Ditemukan**
- `CombatManager.addResultListener` menggunakan `ArrayList.add` bukan replace
- Setiap combat room memanggil `addResultListener` → list terus bertambah
- 10 room combat = 10 listener → 10 notifikasi level up per combat

**Fix**
- `combatResultListeners` (List) → `combatResultListener` (single Consumer)
- `addResultListener` sekarang replace, bukan add
- `addEventListener` dan `addBatchListener` juga: `clear()` sebelum `add()`
- `DungeonManager.clearResultListeners()` sebelum setup tiap combat baru

**Floor Start Fix**
- `startDungeon` baca `player.getDungeonDepth()` bukan hardcode 0

**Armor Subtype Loot**
- HELMET, BOOTS, RING bisa di-drop dari dungeon loot
- `generateArmor(rarity, ArmorType)` dengan stat spesifik per slot

**Aksesori Slot UI**
- Tambah baris aksesori (HBox wide) di bawah grid equipment inventory

---

## [v0.5.5c] — 2026-05-05

### Fix Notification Architecture (Level Up Definitif)

**Arsitektur baru:**
```
ArclightApp.start()
  → router.initEngineListeners()          ← SEKALI SELAMANYA
      → DungeonMapView.wireEngineListeners()
          → engine.setOnDungeonEvent(...)  ← satu closure, tidak pernah diganti

createCharacter() / createCharacterFromSave()
  → new DungeonManager()
  → wireDungeonListeners()               ← internal state machine
```

- `wireEngineListeners()` dihapus dari `build()` → dipanggil hanya dari `ArclightApp`
- `showToast()` dipindah ke `SceneRouter` (punya akses scene yang benar)
- `DungeonManager.resetState()` untuk new game tanpa hapus listener

---

## [v0.5.5b] — 2026-05-05

### Fix 10 Bug Sekaligus

| Bug | Fix |
|-----|-----|
| `getEquippedSkillIds` duplicate | Hapus definisi duplikat di Player |
| `dungeonEventListenerSet` not found | Hapus `setOnDungeonEvent` yang pakai field tidak ada |
| Save: HELMET/BOOTS/RING hilang saat load | Tambah 4 slot ke save dan restore |
| `forceEquipHelmet/Boots/Ring` not found | Tambah method di Inventory |
| Teks English di DungeonMap | Lokalisasi 6 string |
| Filter AKSESORI salah | Cek `instanceof Accessory` terpisah dari `Armor` |
| Filter label pakai internal name | Array `labels[]` terpisah untuk display |
| Klik item list tidak ada popup | Tambah `setOnMouseClicked` dengan `showItemDetailPopup` |
| Hub tidak refresh setelah kota | Simplifikasi showHub |
| Hover hilang dari item row | Re-add `setOnMouseEntered/Exited` |

---

## [v0.5.5] — 2026-05-05

### Tab JURUS + Skill Tree Redesign

**Tab JURUS ProfileView (Clean)**
- Hapus semua unlock lama dengan poin
- SP Banner + tombol "🌳 POHON JURUS ▶"
- Daftar Jurus Aktif (dengan tombol LEPAS)
- Daftar Jurus Terbuka Tidak Aktif (dengan tombol AKTIFKAN)

**SkillTreeView Redesign**
- Circle lebih besar (radius 28), pulse animation untuk node terbuka
- Garis konektor berwarna per cabang (merah/gold/ungu)
- Equip dot di pojok circle jika skill aktif
- Tooltip lengkap dengan instruksi klik
- Klik toggle equip/unequip langsung refresh

**MercChat Fix**
- Hapus cek `getScene() == null` yang terlalu ketat
- Try-catch dengan retry 500ms jika panel belum siap

---

## [v0.5.4] — 2026-05-05

### Bug Fix Batch

**Notif Level Up Spam** — `startDungeonRun` tidak lagi buat `DungeonManager` baru
**Equip langsung update** — `router.showInventory()` setelah equip
**Filter tab Nusantara** — SEMUA/SENJATA/BAJU/HELM/SEPATU/CINCIN/AKSESORI/KONSUMABLE/MATERIAL
**Filter AKSESORI fix** — match `instanceof Accessory` sebelum `instanceof Armor`
**Loot HELM/BOOTS/RING** — `generateEquipment` roll 6 slot termasuk armor subtype

---

## [v0.5.3] — 2026-05-05

### Equipment 8 Slot + Item Detail Popup + Save Fix

**Equipment Slots Lengkap (8 Slot)**
- ⚔ Senjata, 🛡 Baju Besi, 👑 Helm, 👢 Sepatu, 💍 Cincin 1, 💍 Cincin 2, ◈ Aksesori 1, ◈ Aksesori 2
- Layout 3 kolom visual di inventory
- `Armor.ArmorType`: tambah HELMET, BOOTS, RING

**Item Detail Popup**
- Klik equipment slot → popup dengan semua stat, bar kalibrasi/upgrade, tombol LEPAS

**Equip Langsung Update**
- Klik LEPAS → view refresh tanpa perlu keluar

**Save/Load Floor Fix**
- `GameStateConverter.restoreFromSave()` set `dungeonManager.setCurrentFloorNumber()`
- `DungeonManager.setCurrentFloorNumber()` ditambahkan

**Notification Redesign**
- Level up: toast overlay slide-down (ganti Alert dialog)
- Toast via `SceneRouter.showToast()`

**Lokalisasi DungeonMap**
- 6 string English tersisa dilokalkan ke Bahasa Indonesia

---

## [v0.5.2] — 2026-05-05

### GuildMate Chat Fix + Skill Sync

**GuildMate Chat — Fix Timing**
- `emitChatDelayed(trigger, ms)` — `Timeline` delay + double `Platform.runLater`
- Trigger di setiap navigasi: Hub (700ms), DungeonMap (600ms), Combat (1000ms)
- `addMercMessage` defensive retry dengan try-catch

**Skill Names Sync**
- `getSkillInfo()` cover 16 skill termasuk baru dari SkillTree
- SOVEREIGN_STRIKE, NULL_FIELD, NULL_PROTOCOL, DATA_FRAGMENTATION

**Victory Screen Loot**
- Tampilkan nama item (bukan hanya "1 item")

**Persona Dialog Timing**
- Delay sebelum combat mulai: 800ms → 2500ms saat boss

---

## [v0.5.1b] — 2026-05-05

### Fix 3 Error PersonaDialogBox

- `Runnable` dari `java.util.function` → `java.lang` (tidak perlu import)
- `TranslateTransition` dan `FadeTransition` adalah final class — tidak bisa anonymous subclass `{{}}`

---

## [v0.5.1] — 2026-05-05

### 6 Fitur Besar Sekaligus

**Kota (4 Area)**
- 🏪 Toko Senjata Pak Empu
- ⚗️ Kedai Jamu Mbah Jamu
- 🔨 Bengkel Empu (upgrade +9/+10 dengan Kristal Ultra, kalibrasi premium)
- 💰 Penadah Barang (jual item 40% harga)
- Item baru: Kalibrator (⚙300) dan Kristal Ultra Enhance (⚙800)
- Tombol 🏙 MASUK KOTA di Hub

**Skill Tree — Pohon Jurus**
- 3 cabang × 4 depth: Serangan (merah), Mobilitas (gold), Pertahanan (ungu)
- Syarat: unlock skill sebelumnya + level minimum + SP
- Akses dari tab JURUS via "🌳 POHON JURUS ▶"

**Dialog Persona-style**
- `PersonaDialogBox` — portrait + nama + typewriter effect
- Auto-muncul saat boss encounter
- Dialog khusus Theresa per fase (1, 3, 5)

**GuildMate Redesign**
- Chat bubble: portrait circle + nama di bawah + bubble di kanan
- Party max: 2 → 3 orang

**Hub Navigation**
- Tombol 🏙 MASUK KOTA ditambahkan

---

## [v0.5.0] — 2026-05-05

### Major Lore & Identity Overhaul

**Protagonis Tetap: ASUNA**
- Hapus class selection (6 background → 1 karakter)
- `PlayerBackground` hanya `ASUNA`
- CharacterCreateView: intro lore, preview stat, input nama panggilan

**Satu Mythic: ✦ Red Blossom Katana**
- Hapus 10 Mythic weapon random
- Ditempa dari 5 Serpihan Red Essence (boss F10/20/30/40/50)

**Boss per 10 Floor + Theresa**
- F10: Batara Kala | F20: Nyi Roro Kidul | F30: Rangda Agung
- F40: Garuda Mahaguru | F50: Semar Pamungkas | F51: THERESA

**Weapon Types → Semua Pedang**
- BLADE/GUN/CYBER_TOOL/ENERGY_EMITTER/HEAVY dihapus
- Diganti: KATANA/ODACHI/WAKIZASHI/KERIS_SWORD/GOLOK_RUNE/KUJANG_BLADE/FLAME_KATANA/SHADOW_BLADE/DIVINE_SWORD

---

## [v0.4.5] — 2026-05-02

**EventView, ShopView, ProfileView, DungeonMapView, InventoryView** — Total redesign Nusantara Dark Gold

---

## [v0.4.4] — 2026-05-02

**GUI Total Overhaul** — MainMenu, CharacterCreate, Hub, Victory, GameOver, Combat semua redesign

---

## [v0.4.3] — 2026-05-02

**MercenaryDialogue** — 100+ dialog unik Bahasa Indonesia, 7 karakter kepribadian berbeda

---

## [v0.4.2] — 2026-05-01

**Bug Fix** — Gold/EXP double-apply, gameArea width reset, Boss `onPhaseTransition()`

---

## [v0.4.1] — 2026-05-01

**15 Enemy Baru** — total 20 enemy + 5 boss, EnemyRace baru, tier MYTHIC

---

## [v0.4.0] — 2026-05-01

**Konversi Tema** — Arclight City → Mythic Item Obtained, Cyberpunk → Nusantara Dark Gold

---

## [v0.3.7d] — 2026-05-01

**Single Persistent Scene Architecture** — fix MercChatPanel root cause

---

## [v0.3.7] — 2026-04-30

**Floating Damage Numbers, Floor Transition Animation, Room Preview Tooltip**

---

## [v0.3.4] — 2026-04-30

**Save/Load System** — Java Serialization, auto-save tiap turun floor

---

## [v0.3.3] — 2026-04-30

**Layout Fix** — `UIFactory.screenRootBorder()`, hapus `setMinSize()` yang menyebabkan overflow

---

## [v0.3.0] — 2026-04-30

**Turn Order Bar, Skill Popup, Target Select, Combat Speed Control**

---

## [v0.1.0] — 2026-04-26

**Release awal** — migrasi NetBeans Ant → Maven
