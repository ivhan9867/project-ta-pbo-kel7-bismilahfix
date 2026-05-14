# 📋 CHANGELOG — Mythic Item Obtained

> Semua perubahan signifikan dicatat di sini.
> Format: **[versi] (tanggal)** · `🔴 Critical` `🐛 Bug Fix` `✨ Feature` `⚡ Performance` `🎨 Visual` `🔧 Refactor`

---

## [v0.8.1a] — 2026-05-14

### 🐛 Bug Fix
- **`DungeonManager`** — Variable `maxHp` duplikat dalam `onFloorCompleted()` pada scope berbeda; rename ke `mercMaxHp` untuk merc loop

---

## [v0.8.1] — 2026-05-14

### 🔴 Bug Fix Kritis
- **Merc HP dipaksa ke 25% setiap ganti lantai** — `advanceFloor()` memanggil `restoreVitals(maxHp * 0.25)` yang mengoverride HP, bukan heal. Diganti jadi regen **+15% MAX_HP** tanpa override
- **Kalibrasi hasilkan stat +0.1/+0.2 untuk stat persentase** — `calibrate()` memakai formula absolut untuk stat seperti `LIFESTEAL`, `CRIT_CHANCE`, dll. Sekarang ada skala terpisah: stat % → `0.02–0.25`, stat absolut → `15–60`
- **Tema dungeon acak 20% chance** — Dihapus; tema kini 100% konsisten per range 10 lantai

### ✨ Feature
- **Dungeon grid 12 kolom** — Grid melebar dari `COLS=5` ke `COLS=12`, memanfaatkan 872px dari 940px game area (sebelumnya hanya 368px)
- **Player start tile acak** — Tiap masuk lantai baru, player muncul dari tepi grid acak (atas/bawah/kiri/kanan)
- **Ring & Aksesori slot picker** — Saat kedua slot penuh dan ingin equip item baru, muncul dialog pilih slot yang akan diganti
- **Workshop bengkel redesign** — Hapus sistem ULTRA +9/+10 dengan failure chance; tiap item card kini punya tombol `⬆ UPGRADE` + `◈ KALIBRASI` dalam satu tempat; biaya upgrade scale dari item tier
- **AssetManager fallback lengkap** — Tambah mapping eksplisit untuk semua sprite yang hilang: `BabiNgepet`, `BarongRusak`, `NeonSerpent`, `GlitchDrone`, `GarudaKorup`, `DetyaWesi`, `RangdaMerah` → tidak ada lagi spam warning

---

## [v0.8.0a] — 2026-05-14

### 🐛 Bug Fix
- **`LootManager`** — `floorTier` out of scope: `setItemTier()` dipanggil di dalam `generateWeapon()` padahal parameter milik `generateEquipment()`; dipindah ke method yang benar

---

## [v0.8.0] — 2026-05-14

### 🔴 Bug Fix Kritis
| Komponen | Root Cause | Fix |
|----------|------------|-----|
| Equipment stats tidak aktif | `recalcEquipStats()` membaca `player.equippedItems` (selalu kosong) | Ubah ke `inv.getAllEquipped()` |
| HP player 25% saat masuk combat | `MAX_HP` naik setelah recalc tapi `currentHp` tidak di-scale | Scale HP proporsional setelah recalc |
| recalcEquipStats tidak dipanggil di hub | — | Tambah panggilan di `returnToHub()` + `createCharacterFromSave()` |
| Merc MAX_HP reset setelah load | `MercData` tidak simpan stat level-up | Tambah `savedMaxHp/PhysAtk/CyberAtk/Speed` ke `MercData` |

### 🐛 Bug Fix UI
- **Inventory** — Item tidak diequip menampilkan tombol "LEPAS", seharusnya "PAKAI". `showItemDetailPopup` kini cek `inv.getAllEquipped().contains(eq)`.

### 🎨 Visual
- Ally sprite diperbesar: `4 member = 70px` · `3 = 80px` · `2 = 92px` · `1 = 105px`
- Floating damage/buff text muncul dekat entity yang terkena (bukan random seluruh layar)

### ✨ Feature
- **Item Tier System** — Equipment memiliki `itemTier` dari lantai saat drop. Tier 1 (lt.1-10) = max upgrade +10; Tier 2 (lt.11-20) = +20; dst.

---

## [v0.7.9] — 2026-05-14

### 🔴 Bug Fix Kritis
- **Equipment stats tidak pernah aktif** (lifesteal, burn on-hit, bleed on-hit, dll.)
  - Root cause: `Player.recalcEquipStats()` tidak ada — `equipment` layer di `StatSheet` selalu 0
  - Fix: Tambah method `recalcEquipStats(Inventory)`, dipanggil sebelum setiap combat

### 🐛 Bug Fix Party Battle
- Guildmate ke-3 hilang dari combat — `CombatManager` hardcode max 2 merc → diubah ke 3
- Party bar tidak muat 4 slot (player + 3 merc) — dilebarkan ke 4 slot, height lebih compact

### 🐛 Bug Fix Dungeon
- Tema area berganti tiap 5 lantai → diubah ke setiap 10 lantai

### ⚡ Improvement
- Stat minimum per rarity kini berbeda (bukan hanya ceiling yang berbeda):
  - Common weapon ATK: `14–29` · Legendary: `46–91` (tidak overlap lagi)
  - Calibrate bonus juga scale dari rarity minimum

---

## [v0.7.8b] — 2026-05-14
### 🐛 Bug Fix
- Method `buildSellRow(Item, int)` hilang saat refactor — ditambahkan kembali

## [v0.7.8a] — 2026-05-14
### 🐛 Bug Fix
- `buildSellRow` menerima `Integer` (boxed) padahal signature `int` (primitive) — tambah explicit cast `(int)`

---

## [v0.7.8] — 2026-05-14

### ⚡ Performance Fix — Memory Leak
- **`DungeonGridMap`** `pulseAnim` + `marchAnim` (INDEFINITE) tidak pernah di-stop saat navigasi
  - Efek: setiap kunjungan dungeon map = +2 timer aktif. Setelah 10x kunjungan = 20 timer berjalan sekaligus
  - Fix: `DungeonMapView.stopAnimations()` + `SceneRouter` memanggil stop sebelum replace view
- **`CombatView`** `combatLoop/floatLoop` bocor saat replace view → `stopAll()` kini dipanggil `SceneRouter`

### ✨ Feature — Penadah Barang Batch Sell
- Filter bar rarity: `SEMUA` · `Common` · `Uncommon` · `Rare` · `Epic` · `Legendary`
- Filter tipe: `⚔ Equip` · `◈ Material`
- Tombol **JUAL SEKALIGUS** — preview total gold sebelum eksekusi
- Filter bar sticky di atas, item list scroll sendiri di bawah

### 🔧 Refactor
- `giveMaterialBonus()` diextract ke helper agar tidak duplikat antara jual satuan dan batch

---

## [v0.7.4c] — 2026-05-13

### 🐛 Bug Fix
- "Coba lagi" setelah gugur → karakter langsung mati kembali
  - Root cause: `alive = false` di `Entity` tidak di-reset saat HP dipulihkan
  - Fix: `setHpDirect()` kini reset `alive = true` jika HP > 0
- Tulisan GUGUR di layar game over blur — effect dihapus
- Material (Scrap, Chip, Crystal) tidak tersave — disimpan sebagai `ItemData` terpisah

---

## [v0.7.0] — 2026-05-12

### ✨ Feature Besar
- **Sistem Mercenary lengkap** — Rekrut, upgrade loyalty, aktifkan hingga 3 sekaligus
- **Merc Chat Panel** — Dialog dinamis berdasarkan kondisi dungeon
- **Profile & Equipment UI** — Tampilan stat lengkap + slot equip visual

---

## [v0.6.0] — 2026-05-10

> 🔄 **Rename:** `Arclight City` → `Mythic Item Obtained`

### ✨ Feature
- Save/Load system — Java Serialization ke `%APPDATA%`
- Dungeon grid map visual redesign
- Combat turn order bar + skill popup + speed control

---

## [v0.3.x – v0.5.x] — 2026-05-01 s/d 05-09

<details>
<summary>Lihat history versi lama (Arclight City)</summary>

### v0.5.x
- Item calibration system
- Workshop bengkel awal
- Boss fight pertama (Genderuwo Mekanik)

### v0.4.x
- Status effect system (Bleed, Burn, Poison, Freeze, Stun)
- Enemy AI behavior by type
- Floating damage numbers

### v0.3.x
- Combat system dasar
- Skill tree player
- Procedural loot generation

</details>

---

<div align="center">

*Semua versi menggunakan semantic versioning `v[major].[minor].[patch][hotfix]`*

</div>
