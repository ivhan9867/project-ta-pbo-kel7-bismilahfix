# ⚡ ARCLIGHT CITY

![Version](https://img.shields.io/badge/version-v0.2.0-00E5FF?style=flat-square)
![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square)
![JavaFX](https://img.shields.io/badge/JavaFX-25-blue?style=flat-square)
![Build](https://img.shields.io/badge/build-Maven-AA00FF?style=flat-square)
![Status](https://img.shields.io/badge/status-In%20Development-FFD600?style=flat-square)

> *"Bertahan hidup di kota yang tidak pernah tidur — dan tidak pernah memaafkan."*

**Arclight City** adalah game RPG roguelite berbasis teks dengan GUI JavaFX.
Dibangun sebagai Tugas Akhir mata kuliah **Pemrograman Berorientasi Objek**,
terinspirasi dari game mobile *Arclight City* oleh Dex App Studio.

---

## 🎮 Tentang Game

Kamu adalah seorang runner di **Arclight City** — kota cyberpunk yang dikuasai
korporasi megabesar. Masuki dungeon bawah kota, lawan musuh yang makin kuat
di setiap lantai, kumpulkan loot, upgrade equipment, dan manage mercenary-mu.

---

## ✅ Status Fitur (v0.2.0)

| Fitur | Status |
|-------|--------|
| Main Menu | ✅ Jalan |
| Create Character (6 background) | ✅ Jalan |
| Hub Screen | ✅ Jalan |
| Dungeon Map + Navigasi Room | ✅ Jalan |
| Combat Turn-Based | ✅ Jalan |
| Loot Room → Item masuk Inventory | ✅ Fixed v0.2 |
| Rest Room → HP/MP restore | ✅ Fixed v0.2 |
| Inventory (EQUIP/UPGRADE/CALIBRATE/USE) | ✅ Fixed v0.2 |
| Mercenary Management | ✅ Jalan |
| Profile / Stat Sheet | ✅ Jalan |
| Victory & Game Over Screen | ✅ Jalan |
| Event Room (choices) | ✅ Jalan |
| Shop Screen | 🚧 Placeholder |
| Skill Selection UI | 🚧 Coming v0.3 |
| Save / Load | 🚧 Coming v0.6 |
| Sound Effects | 🚧 Coming |

---

## ✨ Fitur Combat

- **Turn-based** berbasis SPEED stat — entity lebih cepat giliran duluan
- **2 bar vital**: HP (merah) + Shield (ungu) — shield diserap sebelum HP
- **3 tipe damage**: Physical / Cyber / Energy — masing-masing punya resistensi
- **28 status effect**: DOT, CC, Buff, Debuff
- **Damage Multiplier** sebagai stat tersendiri dari equipment

---

## 👤 Background Origin (6 pilihan)

| Background | Keunggulan |
|---|---|
| 🥊 Street Brawler | Physical ATK, HP, Crit, DMG Mult |
| 💻 Netrunner | Cyber ATK, MP, Skill Power, CDR |
| 🪖 Veteran Soldier | DEF, HP, Shield, Block Chance |
| ⚡ Energy Adept | Energy ATK, Lifesteal, Skill Power |
| 👻 Ghost Operative | Evasion, Speed, Crit, Armor Pierce |
| 🔧 Techwright | Shield Regen, Sync Rate, Merc Buff |

---

## 🤝 Mercenary (7 tersedia)

| Merc | Role | Keunikan |
|---|---|---|
| Kira Voss | DPS | Stealth + guaranteed crit |
| Tank-RX9 | Tank | Shield tertinggi, Taunt, Counter |
| Sera Mend | Support | Heal + Cleanse debuff |
| Vector | DPS | Execute + Hack musuh |
| Magnus Forge | DPS AoE | Charge shot, Incendiary |
| Echo Null | CC | EMP, Frequency Lock, Signal Jam |
| Lyra Bloom | Support | AoE Heal + Resonance buff |

---

## 👾 Enemy yang Ada

| Enemy | Race | Tier | Mechanic |
|---|---|---|---|
| Street Thug | Human | Standard | Stun |
| Neon Serpent | Mutant | Standard | Bleed stack |
| Glitch Drone | Android | Minion | MP Drain, Self Destruct |
| Iron Clad | Cyborg | **Elite** | 3 armor phase |
| Void Specter | Specter | **Elite** | Immune Physical, Phase Shift |
| **Null King** | Android | **BOSS** | 3 fase, Null Field |

---

## 🚀 Cara Run

### Prerequisites
- JDK 17 atau 21+
- Maven 3.9+
- JavaFX tidak perlu download terpisah — sudah dihandle Maven

### Run langsung
```bash
git clone https://github.com/USERNAME/ArclightCity.git
cd ArclightCity
mvn javafx:run
```

---

## 🏗️ Arsitektur OOP

```
Entity (abstract)
├── Player
├── Enemy (abstract) → StreetThug, NeonSerpent, GlitchDrone,
│                      IronClad, VoidSpecter
│                      └── Boss (abstract) → NullKing
└── Mercenary (abstract) → KiraVoss, TankRX9, SeraMend,
                            Vector, MagnusForge, EchoNull, LyraBloom

Item (abstract)
├── Equipment → Weapon, Armor, Accessory
├── Consumable
└── Material
```

### Design Patterns

| Pattern | Implementasi |
|---|---|
| Observer | `CombatManager.addEventListener()`, `DungeonManager.setStateListener()` |
| Factory | `EntityFactory.createEnemy()`, `createMercenary()` |
| Strategy | AI behavior per enemy: `normalAction()`, `specialAction()`, `desperateAction()` |
| Template Method | `Enemy.decideAction()` template, subclass override |

---

## 📁 Struktur Project

```
src/main/java/arclightcity/
├── engine/          # GameEngine — controller utama
├── entity/
│   ├── base/        # Entity, EntityType, CombatAction
│   ├── stats/       # StatType (26 stat), StatSheet, DamageType
│   ├── status/      # 28 StatusEffectType + StatusEffect
│   ├── player/      # Player + 6 PlayerBackground
│   ├── enemy/       # 5 enemy + Boss system
│   └── mercenary/   # 7 mercenary dengan AI unik
├── combat/          # CombatManager, DamageCalculator, SkillExecutor, TurnQueue
├── dungeon/         # DungeonManager, ProceduralGenerator, DungeonEvent
├── item/            # Item, Equipment, Weapon, Armor, Accessory,
│                    # Consumable, Material, Inventory,
│                    # UpgradeSystem, CalibrationSystem, LootManager
└── ui/
    ├── ArclightApp.java        # Entry point JavaFX
    ├── controller/SceneRouter  # Navigasi antar screen
    ├── util/UIFactory          # Komponen UI reusable
    └── view/                   # Semua screen
src/main/resources/
└── arclightcity/ui/style/arclight.css   # Cyberpunk dark theme
```

---

## 🗺️ Roadmap

| Versi | Fokus | Status |
|---|---|---|
| v0.1.0 | Foundation — game bisa jalan end-to-end | ✅ Done |
| v0.2.0 | Bug fix krusial: loot, inventory, AI loop | ✅ Done |
| v0.3.0 | Combat polish: skill UI, target select, damage anim | 🔜 Next |
| v0.4.0 | Progression: level up UI, skill unlock screen | 📋 Planned |
| v0.5.0 | Content: enemy baru, boss kedua, shop fungsional | 📋 Planned |
| v0.6.0 | QoL: save/load, tutorial, mercenary hire | 📋 Planned |

---

## 📝 Changelog

Lihat [CHANGELOG.md](CHANGELOG.md) untuk detail perubahan tiap versi.

---

## 👨‍💻 Author

Tugas Akhir — **Pemrograman Berorientasi Objek**

Terinspirasi dari: *Arclight City* by Dex App Studio

---

## 📄 License

Dibuat untuk keperluan akademik.
