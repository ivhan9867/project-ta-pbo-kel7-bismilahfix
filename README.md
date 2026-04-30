# ⚡ ARCLIGHT CITY

![Version](https://img.shields.io/badge/version-v0.3.4-00E5FF?style=flat-square)
![Java](https://img.shields.io/badge/Java-25-orange?style=flat-square)
![JavaFX](https://img.shields.io/badge/JavaFX-25-blue?style=flat-square)
![Build](https://img.shields.io/badge/build-Maven-AA00FF?style=flat-square)
![Status](https://img.shields.io/badge/status-In%20Development-FFD600?style=flat-square)

> *"Bertahan hidup di kota yang tidak pernah tidur — dan tidak pernah memaafkan."*

**Arclight City** adalah game RPG roguelite turn-based dengan GUI JavaFX murni (tanpa FXML).
Dibangun sebagai Tugas Akhir mata kuliah **Pemrograman Berorientasi Objek**,
terinspirasi dari game mobile *Arclight City* oleh Dex App Studio.

Window **860×820px** — split layout: game area 560px (kiri) + mercenary chat panel 300px (kanan).

---

## 🎮 Tentang Game

Kamu adalah seorang runner di **Arclight City** — kota cyberpunk yang dikuasai korporasi megabesar.
Masuki dungeon, jelajahi grid map yang penuh event random, lawan musuh makin kuat,
kumpulkan loot, upgrade equipment, dan manage tim mercenary-mu.

Semua UI dibangun secara programmatic (view-as-code, bukan FXML).

---

## ✅ Status Fitur (v0.3.0)

| Fitur | Status |
|-------|--------|
| Main Menu | ✅ Jalan |
| Create Character (6 background) | ✅ Jalan |
| Hub Screen | ✅ Jalan |
| Dungeon Grid Map 2D (fog of war, klik tile) | ✅ v0.2.3 |
| Full Grid Exploration (backtrack bebas) | ✅ v0.2.3 |
| Dungeon Map Visual Overhaul (pulse, marching ants) | ✅ v0.2.8 |
| Combat Turn-Based | ✅ Jalan |
| Turn Order Bar di Combat | ✅ v0.3.0 |
| Skill Selection Popup | ✅ v0.3.0 |
| Target Selection (klik enemy) | ✅ v0.3.0 |
| Combat Speed Control (1× 2× SKIP) | ✅ v0.3.0 |
| Loot Room → Item masuk Inventory | ✅ v0.2.0 |
| REST Room Diminishing Heal | ✅ v0.2.5 |
| Inventory (EQUIP / UPGRADE / CALIBRATE / USE) | ✅ v0.2.0 |
| Starter Skills per Background | ✅ v0.2.5 |
| Mercenary Management | ✅ Jalan |
| Shop Basic (beli item) | ✅ v0.2.5 |
| Mercenary Chat Panel | ✅ v0.2.6 |
| Profile / Stat Sheet | ✅ Jalan |
| Victory & Game Over Screen | ✅ Jalan |
| Event Room (pilihan + efek) | ✅ Jalan |
| Floating Damage Numbers | 🚧 v0.3.1 |
| Level Up Screen | 🚧 v0.4 |
| Save / Load | 🚧 v0.6 |
| Mercenary Hire di Hub | 🚧 v0.6 |
| Craft System | 🚧 TBD |
| Sound Effects | 🚧 TBD |

---

## ✨ Fitur Utama

### ⚔️ Combat (v0.3)
- Turn-based berbasis SPEED stat
- **Turn Order Bar** — antrian giliran 6 entity ke depan dengan HP mini bar
- **Skill Selection Popup** — pilih skill dari daftar, lihat deskripsi + MP cost
- **Target Selection** — klik enemy yang ingin diserang (bukan auto-target)
- **Combat Speed Control** — tombol 1× / 2× / SKIP untuk kontrol kecepatan AI
- 2 bar vital: HP (merah) dan Shield (ungu) — shield diserap sebelum HP
- 3 tipe damage: Physical, Cyber, Energy
- 28 status effect: DOT, CC, Buff, Debuff

### 🗺️ Dungeon Grid Map (v0.2.8)
- Grid penuh COLS×ROWS — semua tile berisi event random
- **Fog of war 3 state**: Hidden → Visible → Visited
- **Marching ants border** — reachable tile punya dashed border bergerak
- **Player breathing pulse** — animasi 3 ring concentric sinusoidal
- **Hover tooltip** — nama room muncul saat hover tile adjacent
- Koneksi garis H/V cardinal
- Boss wajib dikalahkan untuk DESCEND ke lantai berikutnya
- Backtrack ke tile visited bebas (event tidak re-trigger)

### 💬 Mercenary Chat Panel (v0.2.6)
- Panel 300px di kanan layar, persistent semua screen
- 7 mercenary dengan dialog kepribadian berbeda
- Auto-trigger: masuk room, combat start, low HP, victory, dll

---

## 👤 Background Origin

| Background | Keunggulan | Starter Skills |
|---|---|---|
| 🥊 Street Brawler | Physical ATK, HP, Crit | POWER_STRIKE + EXECUTE |
| 💻 Netrunner | Cyber ATK, MP, Skill Power | DEEP_HACK + VIRUS_UPLOAD |
| 🪖 Veteran Soldier | DEF, HP, Shield, Block | IRON_SHIELD + SHOCKWAVE |
| ⚡ Energy Adept | Energy ATK, Lifesteal | ENERGY_DRAIN + BIO_IRRADIATE |
| 👻 Ghost Operative | Evasion, Speed, Crit | PHANTOM_SHOT + SHADOW_STEP |
| 🔧 Techwright | Shield Regen, Sync Rate | EMP_BURST + FIELD_BARRIER |

---

## 🤝 Mercenary

| Merc | Role | Kepribadian |
|---|---|---|
| Kira Voss | DPS Sniper | Dingin, profesional, sedikit kata |
| Tank-RX9 | Tank | Formal, android, logis |
| Sera Mend | Support Medic | Hangat, caring, protektif |
| Vector | DPS Assassin | Sarkastis, arogan, overconfident |
| Magnus Forge | DPS AoE | Kasar, antusias, keras |
| Echo Null | CC Jammer | Misterius, teknis, cryptic |
| Lyra Bloom | Support Shaman | Positif, spiritual, poetic |

---

## 👾 Enemy

| Enemy | Tier | Mechanic |
|---|---|---|
| Street Thug | Standard | Stun |
| Neon Serpent | Standard | Bleed stack |
| Glitch Drone | Minion | MP Drain, Self Destruct |
| Iron Clad | **Elite** | 3 armor phase |
| Void Specter | **Elite** | Immune Physical, Phase Shift |
| **Null King** | **BOSS** | 3 fase, Null Field |

---

## 🚀 Cara Run

### Prerequisites
- JDK 25 (atau 17+)
- Maven 3.9+
- JavaFX dihandle otomatis oleh Maven

### Run
```bash
git clone https://github.com/ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX.git
cd ArclightCity
mvn javafx:run
```

---

## 🏗️ Arsitektur OOP

### Hierarchy
```
Entity (abstract)
├── Player
├── Enemy (abstract)
│   ├── StreetThug, NeonSerpent, GlitchDrone
│   ├── IronClad, VoidSpecter
│   └── Boss (abstract) → NullKing
└── Mercenary (abstract)
    └── KiraVoss, TankRX9, SeraMend,
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
| Factory | `EntityFactory.createEnemy()`, `EntityFactory.createMercenary()` |
| Strategy | AI behavior per enemy: `normalAction()`, `specialAction()`, `desperateAction()` |
| Template Method | `Enemy.decideAction()` sebagai template, subclass override |

---

## 📁 Struktur Project

```
src/main/java/arclightcity/
├── engine/           → GameEngine — state machine & controller utama
├── entity/
│   ├── base/         → Entity, CombatAction, EntityType
│   ├── stats/        → StatType (26 stat), DamageType
│   ├── status/       → StatusEffectType (28), StatusEffect
│   ├── player/       → Player + 6 PlayerBackground
│   ├── enemy/        → 5 enemy + Boss
│   └── mercenary/    → 7 mercenary
├── combat/           → CombatManager, DamageCalculator,
│                       SkillExecutor, TurnQueue
├── dungeon/          → DungeonManager, ProceduralGenerator,
│                       DungeonEvent, Floor, Room
├── item/             → Item, Equipment, Weapon, Armor, Accessory,
│                       Consumable, Material, Inventory,
│                       LootManager, UpgradeSystem, CalibrationSystem
└── ui/
    ├── ArclightApp.java         → Entry point (860×820px)
    ├── controller/SceneRouter   → Navigasi + MercChatPanel
    ├── util/UIFactory           → Komponen reusable
    └── view/
        ├── MainMenuView
        ├── CharacterCreateView
        ├── HubView
        ├── DungeonMapView       → Layout dungeon screen
        ├── DungeonGridMap       → Canvas 2D grid interaktif
        ├── CombatView           → Combat screen + v0.3 features
        ├── ProfileView
        ├── ViewsBundle          → Inventory, Mercenary, Event,
        │                          Shop, Victory, GameOver
        ├── MercChatPanel        → Chat panel 300px
        └── MercenaryDialogue    → Database dialog

src/main/resources/arclightcity/ui/style/
└── arclight.css                 → Cyberpunk dark theme
```

---

## 🗺️ Roadmap

| Versi | Fokus | Status |
|---|---|---|
| v0.1.0 | Foundation — game bisa jalan | ✅ Done |
| v0.2.0 | Bug fix krusial: loot, inventory, AI loop | ✅ Done |
| v0.2.1 | UI patch: warna, scroll, glow, merc bug | ✅ Done |
| v0.2.2 | ConcurrentModification fix, Grid Map v1 | ✅ Done |
| v0.2.3 | Full grid exploration, fog of war | ✅ Done |
| v0.2.4 | Backtrack bug fix | ✅ Done |
| v0.2.5 | REST heal, starter skills, shop basic | ✅ Done |
| v0.2.6 | Split layout 860px, Merc Chat Panel | ✅ Done |
| v0.2.7 | Comprehensive UI font pass | ✅ Done |
| v0.2.8 | Dungeon map visual overhaul | ✅ Done |
| v0.3.0 | Combat: turn bar, skill UI, target select | ✅ Done |
| v0.3.1 | Window +100px height, combat speed fix, layout compact | ✅ Done |
| v0.4.0 | Level up screen, skill unlock UI | 📋 Planned |
| v0.5.0 | Content: enemy baru, boss kedua, hidden room | 📋 Planned |
| v0.6.0 | QoL: save/load, merc hire, tutorial | 📋 Planned |

---

## 📝 Dokumentasi

Lihat [CHANGELOG.md](CHANGELOG.md) untuk detail perubahan tiap versi.

---

## 👨‍💻 Author

Tugas Akhir — **Pemrograman Berorientasi Objek**
Terinspirasi dari: *Arclight City* by Dex App Studio

---

## 📄 License

Dibuat untuk keperluan akademik.
