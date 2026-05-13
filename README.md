# Mythic Item Obtained v0.7.4c
**Isekai RPG Dungeon Crawler — Tema Nusantara**

> *"Asuna, seorang gamer dari dunia modern, tersesat ke dunia fantasi Nusantara. Bertahan di dungeon yang penuh makhluk mistis, kumpulkan item mythic, dan temukan jalan pulang."*

---

## 🚀 Cara Menjalankan
```bash
mvn javafx:run
```
**Requirements:** Java 25, JavaFX 25, Maven

---

## 🎮 Gameplay Overview

### Combat (Turn-Based)
- **Layout:** Ally di kiri scene, Enemy di kanan
- **Aksi:** Klik ally → pilih aksi (Serang/Jurus/Item/Bertahan) → klik target enemy
- **Speed:** 1× / 2× / SKIP
- **Flee:** Tombol KABUR di header, langsung berlaku saat berhasil

### Guildmate (7 karakter)
| Nama Asli | Role | Buff Team | CC | Attack |
|-----------|------|-----------|-----|--------|
| Gatot Kaca | Tank | FORTIFY (DEF+) | TAUNT | IRON_SHIELD |
| Srikandi | DPS | FOCUS (CRIT+) | EXPOSE | PHANTOM_SHOT |
| Nyai Roro | Healer | REGEN+BARRIER | SLOW | TRIAGE_HEAL |
| Bima | Breaker | EMPOWERED (ATK+) | STUN | OVERLOAD_SHOT |
| Rangga | Assassin | STEALTH | BLEED | EXECUTE |
| Ki Ageng | Controller | SYNC (SPD+) | FREEZE | EMP_BURST |
| Dewi Sri | Support | REGEN+BARRIER | WEAKEN | NEON_BLOOM |

**AI Logic:** Buff tim dulu → CC jika perlu → Serang. Healer prioritaskan ally kritis.

### Dungeon
| Floor | Lokasi |
|-------|--------|
| 1-10 | Pasar Malam Gaib |
| 11-20 | Candi Terlarang |
| 21-30 | Hutan Angker |
| 31-40 | Goa Naga |
| 41-50 | Kahyangan Rusak |
| 51+ | Jurang Abadi |

### Item & Upgrade
- **Material:** Scrap Metal, Cyber Chip, Neon Crystal, Cal Kit
- **Upgrade:** Klik item equipped → ⬆ UPGRADE (biaya ditampilkan, disabled jika tidak cukup)
- **Kalibrasi:** Klik item equipped → ◈ KALIBRASI (butuh Cal Kit)
- **Tas:** Klik + di samping "BAG x/x" untuk expand (max 100 slot)
- **Jual:** Item memberikan gold + material bonus sesuai rarity

### Status Effects
- 🟢 **Hijau** = Buff (FORTIFY, REGEN, EMPOWERED, FOCUS, SYNC...)
- 🔴 **Merah** = Debuff (WEAKEN, SHRED, EXPOSE...)
- 🟠 **Oranye** = DOT (BLEED, BURN, VIRUS...)
- 🔵 **Biru** = CC (FREEZE, STUN, SLOW, TAUNT...)

---

## 💾 Save System
- **3 slot manual** + **auto-save** per lantai
- Lokasi: `%APPDATA%\MythicItemObtained\`
- Retry setelah gugur: restore 50% HP, item & progress tetap

---

## 🏗️ Arsitektur

```
src/main/java/arclightcity/
├── engine/         → GameEngine, GameState
├── entity/         → Player, Mercenary (7), Enemy (20+), Boss (6+)
├── combat/         → CombatManager, DamageCalculator, SkillExecutor, TurnQueue
├── dungeon/        → DungeonManager, ProceduralGenerator, Floor, Room
├── item/           → Item hierarchy, Inventory, UpgradeSystem
├── save/           → SaveManager, GameSaveState, GameStateConverter
└── ui/
    ├── ArclightApp.java        → Entry point (1280×720px)
    ├── controller/SceneRouter  → Navigasi antar screen
    ├── util/UIFactory          → Palette & komponen UI
    ├── util/AssetManager       → 172+ asset (sprites/portraits/bg/icons)
    └── view/                   → CombatView, DungeonMapView, HubView, dll
```

---

## 📦 Asset
- **Background:** 940×620px di `resources/assets/backgrounds/`
- **Sprites:** 180×180px (idle/attack/hit) di `resources/assets/sprites/`
- **Portraits:** di `resources/assets/portraits/`
- **Icons:** 64×64px di `resources/assets/icons/`

---

## 👥 Tim
**Proyek Akhir OOP — Kelompok 7**
Repository: [GitHub](https://github.com/ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX)
