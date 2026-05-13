# CHANGELOG — Mythic Item Obtained

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
