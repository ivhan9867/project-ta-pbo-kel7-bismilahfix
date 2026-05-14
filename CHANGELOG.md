# CHANGELOG — Mythic Item Obtained

## v0.7.9 (2026-05-14)
### Bug Fix — Kritis
- Fixed: Equipment stats (lifesteal, burn/bleed/poison on-hit, dll.) TIDAK PERNAH masuk ke StatSheet
  - Root cause: `Player.recalcEquipStats()` tidak ada — `equipment` layer di StatSheet selalu 0
  - Fix: Tambah `Player.recalcEquipStats(Inventory)`, dipanggil sebelum tiap combat di DungeonManager
  - Impact: Semua on-hit weapon effects kini benar-benar aktif; lifesteal kini berfungsi

### Bug Fix — Party Battle
- Fixed: Guildmate ke-3 hilang dari combat — `CombatManager` hardcode max 2 merc, diubah ke 3
- Fixed: Party bar tidak muat 4 slot (player + 3 merc) — slot dilebarkan ke 4, height lebih compact

### Bug Fix — Dungeon Floor Naming
- Fixed: Tema area berganti tiap 5 lantai, seharusnya tiap 10
  - 1-10: Pasar Malam Gaib | 11-20: Candi Terlarang | 21-30: Hutan Angker
  - 31-40: Goa Naga | 41+: Kahyangan Rusak

### Improvement — Item Stat Scaling
- Rarity kini memiliki stat minimum yang berbeda (bukan hanya ceiling)
  - Common weapon ATK: 14-29 | Legendary weapon ATK: 46-91 (tidak lagi overlap)
  - Helmet HP: Common 15-45 → Legendary 75-145
  - Calibrate bonus juga scale dari rarity minimum

## v0.7.8b (2026-05-14)
### Bug Fix
- Fixed: method `buildSellRow(Item, int)` hilang saat refactor — deklarasi method ditambahkan kembali

## v0.7.8a (2026-05-14)
### Bug Fix
- Fixed: `buildSellRow(Item, Integer)` — `priceOf.apply()` returns boxed `Integer`, ditambah explicit cast `(int)` agar match signature primitive

## v0.7.8 (2026-05-14)
### Performance Fix
- Fixed: Progressive lag — animasi `DungeonGridMap` (pulseAnim + marchAnim INDEFINITE) tidak pernah di-stop saat navigasi; setelah 10x kunjungan dungeon map = 20+ timer berjalan sekaligus
  - Fix: `DungeonMapView.stopAnimations()` + `SceneRouter` memanggil stop sebelum replace view
- Fixed: `CombatView.combatLoop` / `floatLoop` dapat bocor jika combat di-replace; `stopAll()` kini dipanggil SceneRouter
- Perintah `giveMaterialBonus()` diextract ke helper agar tidak duplikat antara jual satuan dan batch

### New Feature — Penadah Barang: Batch Sell
- Filter bar rarity: SEMUA | Common | Uncommon | Rare | Epic | Legendary
- Filter tipe: ⚔ Equip | ◈ Material
- Tombol **JUAL SEKALIGUS** — jual semua item yang lolos filter sekaligus
- Preview otomatis: jumlah item + total ⚙ gold sebelum konfirmasi
- Filter bar sticky di atas, item list scroll sendiri di bawah

## v0.7.4c (2026-05-13)
### Bug Fix
- Fixed: "Coba lagi" setelah gugur — karakter langsung mati kembali saat masuk dungeon
  - Root cause: `alive = false` di Entity tidak di-reset saat HP dipulihkan
  - Fix: `setHpDirect()` sekarang reset `alive = true` jika HP > 0
  - Berlaku untuk Player dan Mercenary (override di keduanya)
- Fixed: Tulisan GUGUR di layar game over blur/tidak terbaca — effect dihapus
- Fixed: Material (Scrap, Chip, Crystal) tidak tersave — disimpan sebagai ItemData terpisah
- Fixed: Upgrade item dari popup "item not found" — `findById()` sekarang cari di equipped slots
- Fixed: `ItemRarity` → `Item.Rarity`, `SCRAP/CHIP/CRYSTAL` → `SCRAP_METAL/CYBER_CHIP/NEON_CRYSTAL`

## v0.7.4b (2026-05-13)
### New Features
- Inventory: Tombol ⬆ UPGRADE dan ◈ KALIBRASI langsung di popup equipped item
- Inventory: Tombol + di samping "BAG x/x" untuk expand kapasitas (max 100 slot, bayar gold)
- Upgrade popup: Tampilkan biaya material yang dibutuhkan, disabled jika tidak cukup
- Kalibrasi popup: Tampilkan jumlah Cal Kit, disabled jika 0

## v0.7.4 (2026-05-13)
### Balance & Gameplay
- Guildmate ATK naik ~60% (TankRX9 55, Srikandi 80, Nyai Roro 40+60, Bima 70+70, Rangga 90, Ki Ageng 45+85, Dewi Sri 35+80)
- Basic attack multiplier ×1.5 untuk semua entitas
- Buff duration ×3 (2 turn → 6, 3 turn → 8-9) karena turn dihitung per entitas
- MP regen +8/turn untuk guildmate, +5/turn untuk player
- Skill guildmate cost dikurangi 30%
- Setelah menang lantai: MP + Shield PENUH, HP +30%
- Jual item memberikan material bonus sesuai rarity item

### Bug Fix
- Nama lokasi konsisten: F1-10 Pasar, F11-20 Candi, F21-30 Hutan, F31-40 Goa, F41-50 Kahyangan
- Portrait semua guildmate (lyra bloom, sera mend, dll) sudah terpetakan dengan benar

## v0.7.3e (2026-05-13)
### Bug Fix  
- SkillExecutor: hapus semua duplicate case label
- SkillExecutor: fix `void cannot convert to List<CombatEvent>` dengan `{ ...; yield events; }`
- DamageCalculator: fix urutan parameter `calculate(caster, t, baseDmg, DamageType, 0)`
- UIFactory: `getDurationTurns()` → `getRemainingTurns()`

## v0.7.3c (2026-05-13)
### New Features — Guildmate AI Redesign (Buff-First Priority)
- **Gatot Kaca (Tank)**: FORTIFY (DEF +) → TAUNT → IRON_SHIELD
- **Srikandi (DPS)**: FOCUS (CRIT +) → EXPOSE → PHANTOM_SHOT
- **Nyai Roro (Healer)**: Heal kritis → REGEN+BARRIER → SLOW → TRIAGE_HEAL
- **Bima (Breaker)**: EMPOWERED (ATK +) → STUN → OVERLOAD_SHOT
- **Rangga (Assassin)**: STEALTH → BLEED → EXECUTE
- **Ki Ageng (Controller)**: SYNC (SPD +) → FREEZE → EMP_BURST
- **Dewi Sri (Support)**: Heal kritis → REGEN+BARRIER → WEAKEN → NEON_BLOOM
- NeonBloom: Jika HP penuh, heal dialihkan ke Shield
- Status effect icon warna: hijau=buff ✦, merah=debuff ✗, oranye=DOT ☠, biru=CC ❄

### New Features — Party Bar
- Height adaptif: 2 member = 100px, 3 member = 82px
- Portrait, font, dan bar menyesuaikan jumlah member

## v0.7.2d (2026-05-13)
### Bug Fix
- Level up notifikasi muncul kembali — `showDungeonMap()` sekarang selalu `wireEngineListeners()`
- DamageCalculator SHRED: flat -20% (bukan tergantung attacker level)

## v0.7.2 (2026-05-13)
### Bug Fix
- USE_ITEM: heal benar-benar diapply ke player (sebelumnya hanya logging)
- `alive` flag di Entity tidak direset saat HP dipulihkan → mati terus (partial fix)
- Retry setelah mati: restore 50% HP, tidak load save lama
- backHub: restore 25% HP dan revive guildmate
- Enemy kroco scaling: HP +12%/lantai, ATK +3%/lantai, DEF tidak naik
- SHRED dari enemy dikurangi: kroco -20%, boss -35%

## v0.7.1 (2026-05-13)
### Combat UI Overhaul (Final)
- Layout: **Ally KIRI** (VBox), **Enemy KANAN** (Pane absolute positioning)
- Enemy ditempatkan di 6 slot absolut (depan/belakang/kiri/kanan) berdasarkan ID hash
- Sprite pose animation: poseState map → attack/hit/idle per entitas
- Action flow: klik ally → overlay → pilih aksi → klik target enemy
- Targeting mode: enemy di-highlight saat player memilih SERANG/JURUS

## v0.7.0 (2026-05-12)
### Combat UI
- Layout Bravely Second style: sprite scene full, party bar horizontal di bawah
- Party bar: info only (tidak ada tombol action di party bar)
- Action overlay muncul di tengah scene saat klik ally
- Flee fix: `attemptFlee()` langsung trigger `processTurn()`

## v0.6.7 (2026-05-12)
### Fix
- Sprite pose animation (attack/hit/idle) via poseState map
- DungeonMapView background terpasang
- Enemy positioning dengan hash-based slot randomization

## v0.6.6 (2026-05-11)
### Combat Layout
- Enemy zone kiri (HBox), Ally zone kanan (VBox)
- Action menu: klik ally → overlay pilihan
- Skill submenu per guildmate dengan deskripsi dan MP cost

## v0.6.3 - v0.6.5
### Fixes
- Bar HP/SHD/MP menggunakan HBox (bukan StackPane) agar resize benar
- Pulse animation pada current actor
- Floating damage text (attack/crit/heal/efek)

## v0.5.0 - v0.6.0
- Save/Load system (3 slot manual + auto-save)
- Sistem kalibrasi item
- Equipment slot picker saat slot penuh
- Skill popup dari battle scene

## v0.1.0 - v0.4.0
- Core combat system (turn-based, speed-based queue)
- Entity hierarchy: Player, Mercenary, Enemy, Boss
- Inventory system, Equipment, Consumable, Material
- Dungeon procedural generator
- City shops (Senjata, Jamu, Bengkel Empu, Penadah)
- Guildmate hire & loyalty system
- JavaFX UI framework
