# MYTHIC ITEM OBTAINED — CHANGELOG

> Urutan: **terbaru di atas**, terlama di bawah.

---

## [v0.5.0] — 2026-05-05

### MAJOR — Lore & Identity Overhaul

**Protagonis Tetap: ASUNA**
- Hapus sistem class selection (6 background → 1 karakter tetap)
- `PlayerBackground` hanya punya satu nilai: `ASUNA`
- CharacterCreateView sekarang menampilkan intro lore: cerita, preview stat, input nama panggilan
- Stat awal Asuna: Slash-type swordswoman — ATK fisik 22, Crit 18%, Speed 16

**Satu Mythic Item: ✦ Red Blossom Katana**
- Hapus 10 Mythic weapon random
- Hanya satu senjata Mythic: **Red Blossom Katana**
- Stat: ATK 120, Crit 40%, Crit DMG 220%, Pierce 50%, Lifesteal 20%, DMG+35%
- Ditempa dari 5 Serpihan Red Essence (bukan 3 Pecahan Mitik)

**Sistem Red Essence Shard (5 Shard)**
- Setiap boss di Floor 10/20/30/40/50 drop 1 Serpihan Red Essence
- Kumpulkan 5 → auto-craft Red Blossom Katana
- Counter Hub: /3 → /5, label "PECAHAN MITIK" → "SERPIHAN RED ESSENCE"

**Boss per 10 Floor**
- F10: Batara Kala | F20: Nyi Roro Kidul | F30: Rangda Agung
- F40: Garuda Mahaguru | F50: Semar Pamungkas | F51: THERESA (Final Boss)

**Theresa — Final Boss (Floor 51)**
- HP 5000, Shield 800, 6 fase, dialog unik per fase
- Fase 1: meremehkan Asuna → Fase 3: kaget lihat Red Blossom → Fase 5: Ice Age Ultima
- `Theresa.java` — class baru extends Boss

**Weapon Types → Semua Pedang**
- BLADE/GUN/CYBER_TOOL/ENERGY_EMITTER/HEAVY dihapus
- Diganti: KATANA/ODACHI/WAKIZASHI/KERIS_SWORD/GOLOK_RUNE/KUJANG_BLADE/FLAME_KATANA/SHADOW_BLADE/DIVINE_SWORD

---

## [v0.4.5] — 2026-05-02

### Polish Menyeluruh Semua View

**EventView — Total Redesign**
- BorderPane, header dinamis per kategori (POSITIVE/NEGATIVE/CHOICE/NEUTRAL)
- Warna unik: hijau jade / merah api / gold / abu
- Tombol pilihan hover sesuai warna kategori
- "CHOOSE YOUR ACTION" → "PILIH TINDAKANMU"

**ShopView — Total Redesign**
- Header "PEDAGANG GAIB" dengan kutipan Bahasa Indonesia
- BorderPane layout, "TINGGALKAN PASAR"

**ProfileView — Lokalisasi + Polish**
- Tab: STATS/EQUIPMENT/SKILLS → STATISTIK/PERLENGKAPAN/JURUS
- Semua 26 label stat dalam Bahasa Indonesia
- Tombol EQUIP/UNEQUIP/UNLOCK → PASANG/LEPAS/BUKA

**DungeonMapView + InventoryView**
- BorderPane layout, header konsisten, semua warna diupdate ke palette baru

**Color Token Update (semua view)**
- #050810→#0A0604 | #0C1220→#150E08 | #1C2E44→#3A2810
- #00E5FF→#C8860A | #FFD600→#FFB830 | #FF1744→#CC3300

---

## [v0.4.4] — 2026-05-02

### GUI Total Overhaul

**MainMenuView — Total Redesign**
- Judul MYTHIC (52px) + ITEM OBTAINED (28px) dengan double gold glow + pulse animation
- Ornamen batik, tombol MULAI PETUALANGAN + LANJUTKAN + KELUAR

**CharacterCreateView (lama) — Redesign**
- Background card dengan ikon emoji + lore + starter skills
- Card hover gold effect

**HubView — Total Redesign**
- Identity bar: avatar circle + nama + badge + gold + fragment counter
- District banner dinamis, Navigation grid, Crew preview, Quick bar bawah

**VictoryView + GameOverView — Redesign**
- Reward panel gold, boss kill indicator
- GameOver: 3 tombol (COBA LAGI / KEMBALI KE MARKAS / MENU UTAMA)
- "Kembali ke Markas" → Hub existing (bukan main menu)

**Lokalisasi penuh Bahasa Indonesia**
- ENTER DUNGEON→MASUK DUNGEON, ATTACK→SERANG, SKILL→JURUS, FLEE→KABUR, dll

---

## [v0.4.3] — 2026-05-02

### Bug Fix + Dialog Nusantara

**MercChatPanel — Root Cause Fix (definitive)**
- `merc.getName()` → `merc.getMercenaryType().displayName` (nama Nusantara)
- Duplikat `static {}` block dihapus
- Orphan javadoc comment diperbaiki

**MercenaryDialogue — Total Rewrite (659 baris)**
- 7 merc × 15 trigger = 100+ dialog unik dalam Bahasa Indonesia
- Kepribadian masing-masing: Srikandi (dingin), Gatot Kaca (formal), Nyai Roro (hangat),
  Rangga (sarkastis), Bima (antusias), Ki Ageng (bijak), Dewi Sri (spiritual)

**CSS Overhaul v2.0 — "Nusantara Dark Gold"**
- 666 baris CSS komprehensif
- Gradient progress bars, slim scrollbar, hover effects
- Class baru: `.item-card-mythic`, `.boss-alert`, `.victory-glow`, `.mythic-glow`
- `.rarity-mythic` dengan double dropshadow

---

## [v0.4.2] — 2026-05-01

### Bug Fix Critical

**Gold & EXP double-apply setelah combat**
- CombatManager sudah apply, GameEngine juga apply → double
- Fix: GameEngine hanya handle loot drop + notifikasi

**gameArea width tidak reset setelah showFullWidth**
- Main menu full-width, tapi showWithChat tidak reset → chat panel terpush
- Fix: showWithChat reset gameArea ke GAME_WIDTH setiap kali

**Boss baru tidak implement onPhaseTransition()**
- 4 boss baru compile error karena abstract method tidak diimplementasi
- Fix: tambah `@Override protected void onPhaseTransition(int, int) {}`

**CombatResult.levelsGained field baru**
- `getLevelsGained()` + `setLevelsGained()` — untuk notifikasi level up tanpa re-calculate

---

## [v0.4.1] — 2026-05-01

### Content Expansion

**15 Enemy Baru (total 20 enemy + 5 boss)**

Standard: Tuyul Pencuri, Wewe Gombel, Pocong Listrik, Banaspati, Babi Ngepet
Elite: Rangda Merah, Barong Rusak, Leyak Api, Garuda Korup, Detya Wesi
Boss: Nyi Roro Kidul (F8), Rangda Agung (F12), Garuda Mahaguru (F16), Semar Pamungkas (F20)

**EnemyRace baru: SPIRIT, GIANT, DEMON, DIVINE**

**Tier MYTHIC (Item.Rarity ke-6)**
- Warna #FF6B00, multiplier 5.0×, 25 upgrade slots
- Tidak bisa drop dari loot biasa

**Encounter generation per floor range**
- Floor 1-6: standard enemies | 7-17: elite | per milestone: boss

---

## [v0.4.0] — 2026-05-01

### Konversi Tema Nusantara

**Judul: "Arclight City" → "Mythic Item Obtained"**

**Tema UI: Cyberpunk → Nusantara Dark Gold**
- Palette: tinta hitam #0A0604, gold batik #C8860A/#FFB830, merah bata #CC3300

**Player Background → Asal Usul Nusantara**
- Pendekar Betawi, Dukun Digital, Prajurit Majapahit, Pawang Neon, Mata-mata Demak, Empu Modern

**Mercenary → Tokoh Nusantara**
- Srikandi, Gatot Kaca, Nyai Roro, Rangga, Bima, Ki Ageng, Dewi Sri

**Enemy → Mitologi Nusantara**
- Leak Pengembara, Naga Basuki, Genderuwo Mekanik, Raksasa Kala, Kuntilanak Abadi, Batara Kala

**Floor Themes → Lokasi Nusantara**
- Pasar Malam Gaib, Candi Terlarang, Hutan Angker, Goa Naga, Kahyangan Rusak

---

## [v0.3.7d] — 2026-05-01

### MercChatPanel Root Cause Fix

**Root cause chat tidak pernah muncul:**
Setiap navigasi membuat `new Scene()` dan `new HBox()` baru → chatPanel dipindahkan
→ JavaFX detach dari parent lama → messageContainer kehilangan parent → pesan tidak render.

**Fix: Single Persistent Scene Architecture (SceneRouter.java)**
- `persistentLayout` (HBox) dibuat SEKALI, tidak pernah diganti
- `gameArea` (StackPane) hanya isinya yang diganti saat navigasi
- `chatPanel` selalu di `persistentLayout`, tidak pernah dipindahkan
- Scene dibuat SEKALI di constructor, `stage.setScene()` tidak pernah dipanggil lagi

---

## [v0.3.7] — 2026-04-30

### Fitur Baru

**Floating Damage Numbers (CombatView)**
- Canvas overlay transparan di atas enemy section
- Animasi melayang ke atas 55px, 1.4 detik, fade out
- Warna: merah=damage, kuning=crit, hijau=heal, orange=DOT

**Floor Transition Animation (DungeonMapView)**
- Overlay hitam fade in/out total 1.6 detik
- Teks "TURUN / LANTAI X" + "SEKTOR DIMUAT" setelah selesai

**Room Preview di Hover Tooltip (DungeonGridMap)**
- ENEMY room: jumlah + nama enemy
- REST room: info heal sesuai restUseCount

---

## [v0.3.6] — 2026-04-30

**Mercenary Hire System** — Tab REKRUT, 7 merc dengan stats preview + harga
**Status Effect Tooltip** — hover badge → nama, deskripsi, sisa durasi

---

## [v0.3.5] — 2026-04-30

**EXP & Gold diterapkan setelah combat** (fix double-apply pertama)
**Level Up Notification** — alert + system chat
**Profile Tab PERLENGKAPAN + JURUS** — fungsional
**Shop harga fix** — deterministik dari item ID hash
**Combat log clear** — bersih setiap combat baru
**GameOver retry reset** — deleteAllSaves() saat retry

---

## [v0.3.4] — 2026-04-30

**Save/Load System — Java Serialization**
- `GameSaveState`, `SaveManager`, `GameStateConverter`
- 1 manual save + auto-save backup setiap turun floor
- File: `%APPDATA%\ArclightCity\`
- CONTINUE button aktif di Main Menu jika ada save

---

## [v0.3.3] — 2026-04-30

**[CRITICAL] Layout fix menyeluruh**
- Root cause: `UIFactory.screenRoot()` set `setMinSize` → VBox overflow
- Fix: `UIFactory.screenRootBorder()` — BorderPane untuk semua view utama
- CombatView + DungeonMapView dikonversi ke BorderPane

---

## [v0.3.2] — 2026-04-30

**Enemy card kosong saat target select** — return card sebelum konten ditambahkan
**Hub bottom nav tenggelam** — VBox → BorderPane definitif fix

---

## [v0.3.1] — 2026-04-30

Window 820→920px, combat speed default 1200ms

---

## [v0.3.0] — 2026-04-30

**Turn Order Bar, Skill Selection Popup, Target Selection Mode, Combat Speed Control**

---

## [v0.2.x] — 2026-04-28/29

v0.2.8: DungeonGridMap v3 (52px tiles, pulse, marching ants)
v0.2.6: Split layout 860px, MercChatPanel, MercenaryDialogue 150+ dialog
v0.2.5: REST diminishing heal, starter skills per background, shop basic
v0.2.3: Full grid exploration, fog of war, boss-gated DESCEND
v0.2.2: DungeonGridMap v1, fix ConcurrentModificationException
v0.2.1: Fix mercenary duplicate, background putih ScrollPane
v0.2.0: Fix AI Turn Loop, fix DungeonManager reset, Loot/Rest popup

---

## [v0.1.0] — 2026-04-26

Release awal setelah migrasi NetBeans Ant → Maven.
