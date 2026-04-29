# ⚡ ARCLIGHT CITY

![Version](https://img.shields.io/badge/version-v0.2.7-00E5FF?style=flat-square)
![Java](https://img.shields.io/badge/Java-25-orange?style=flat-square)
![JavaFX](https://img.shields.io/badge/JavaFX-25-blue?style=flat-square)
![Build](https://img.shields.io/badge/build-Maven-AA00FF?style=flat-square)
![Status](https://img.shields.io/badge/status-In%20Development-FFD600?style=flat-square)

> *"Bertahan hidup di kota yang tidak pernah tidur — dan tidak pernah memaafkan."*

**Arclight City** adalah game RPG roguelite turn-based dengan GUI JavaFX murni (tanpa FXML).
Dibangun sebagai Tugas Akhir mata kuliah **Pemrograman Berorientasi Objek**,
terinspirasi dari game mobile *Arclight City* oleh Dex App Studio.

---

## 🎮 Tentang Game

Kamu adalah seorang runner di **Arclight City** — kota cyberpunk yang dikuasai
korporasi megabesar. Masuki dungeon bawah kota, jelajahi grid map yang penuh event,
lawan musuh makin kuat di setiap lantai, kumpulkan loot, upgrade equipment,
dan manage tim mercenary-mu.

Semua screen dibangun secara programmatic (view-as-code, bukan FXML).
Window 860×820px — split layout: game area kiri + mercenary chat panel kanan.

---

## ✅ Status Fitur (v0.2.6)

| Fitur | Status |
|-------|--------|
| Main Menu | ✅ Jalan |
| Create Character (6 background) | ✅ Jalan |
| Hub Screen | ✅ Jalan |
| Dungeon Grid Map 2D (klik tile, fog of war) | ✅ v0.2.3 |
| Full Grid Exploration (backtrack bebas) | ✅ v0.2.3 |
| Combat Turn-Based | ✅ Jalan |
| Loot Room → Item masuk Inventory | ✅ v0.2.0 |
| REST Room Diminishing Heal | ✅ v0.2.5 |
| Inventory (EQUIP / UPGRADE / CALIBRATE / USE) | ✅ v0.2.0 |
| Starter Skills per Background | ✅ v0.2.5 |
| Mercenary Management | ✅ Jalan |
| Shop — Basic (beli item) | ✅ v0.2.5 |
| Mercenary Chat Panel | ✅ v0.2.6 |
| Profile / Stat Sheet | ✅ Jalan |
| Victory & Game Over Screen | ✅ Jalan |
| Event Room (pilihan + efek) | ✅ Jalan |
| Skill Selection UI (pilih skill di combat) | 🚧 v0.3 |
| Target Selection (klik enemy) | 🚧 v0.3 |
| Save / Load | 🚧 v0.6 |
| Sound Effects | 🚧 TBD |
| Mercenary Hire di Hub | 🚧 v0.6 |
| Craft System | 🚧 TBD |

---

## ✨ Fitur Utama

### ⚔️ Combat
- Turn-based berbasis SPEED stat — entity lebih cepat giliran duluan
- Dua bar vital terpisah: **HP** (merah) dan **Shield** (ungu) — shield diserap sebelum HP
- 3 tipe damage: Physical, Cyber, Energy — masing-masing punya resistensi
- 28 status effect: DOT, CC, Buff, Debuff
- Damage Multiplier sebagai stat tersendiri dari equipment

### 🗺️ Dungeon Grid Map
- Grid penuh COLS×ROWS — semua tile berisi event random
- **Fog of war 3 state**: Hidden (dot gelap) → Visible (redup) → Visited (sangat redup)
- Koneksi garis **horizontal dan vertikal** antar tile yang terhubung
- Player bebas **backtrack** ke tile yang sudah dikunjungi
- Boss di tengah baris terakhir — harus dikalahkan untuk DESCEND ke lantai berikutnya

### 💬 Mercenary Chat Panel
- Panel chat 300px di sisi kanan layar, persistent di semua screen
- 7 mercenary dengan kepribadian dialog yang berbeda-beda
- Dialog otomatis trigger berdasarkan event: masuk room, combat, low HP, victory, dll
- Bubble chat per merc dengan warna unik

---

## 👤 Background Origin

| Background | Keunggulan | Starter Skills |
|---|---|---|
| 🥊 Street Brawler | Physical ATK, HP, Crit | POWER_STRIKE + EXECUTE |
| 💻 Netrunner | Cyber ATK, MP, Skill Power, CDR | DEEP_HACK + VIRUS_UPLOAD |
| 🪖 Veteran Soldier | DEF, HP, Shield, Block | IRON_SHIELD + SHOCKWAVE |
| ⚡ Energy Adept | Energy ATK, Lifesteal, Skill Power | ENERGY_DRAIN + BIO_IRRADIATE |
| 👻 Ghost Operative | Evasion, Speed, Crit, Armor Pierce | PHANTOM_SHOT + SHADOW_STEP |
| 🔧 Techwright | Shield Regen, Sync Rate | EMP_BURST + FIELD_BARRIER |

---

## 🤝 Mercenary

| Merc | Role | Kepribadian Dialog |
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

| Enemy | Tier | Mechanic Khusus |
|---|---|---|
| Street Thug | Standard | Stun |
| Neon Serpent | Standard | Bleed stack |
| Glitch Drone | Minion | MP Drain, Self Destruct |
| Iron Clad | **Elite** | 3 armor phase, Shockwave AoE |
| Void Specter | **Elite** | Immune Physical, Phase Shift |
| **Null King** | **BOSS** | 3 fase, Null Field (hapus semua buff) |

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
    └── KiraVoss, TankRX9, SeraMend, Vector, MagnusForge, EchoNull, LyraBloom

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
│   ├── base/         → Entity (abstract), EntityType, CombatAction
│   ├── stats/        → StatType (26 stat), StatSheet, DamageType
│   ├── status/       → StatusEffectType (28 effect), StatusEffect
│   ├── player/       → Player + 6 PlayerBackground
│   ├── enemy/        → 5 enemy type + Boss system
│   └── mercenary/    → 7 mercenary dengan AI unik
├── combat/           → CombatManager, DamageCalculator, SkillExecutor, TurnQueue
├── dungeon/          → DungeonManager, ProceduralGenerator, DungeonEvent, Floor, Room
├── item/             → Item, Equipment, Weapon, Armor, Accessory,
│                       Consumable, Material, Inventory, LootManager,
│                       UpgradeSystem, CalibrationSystem
└── ui/
    ├── ArclightApp.java         → Entry point JavaFX (860×820px)
    ├── controller/SceneRouter   → Navigasi + MercChatPanel global
    ├── util/UIFactory           → Komponen UI reusable
    └── view/
        ├── MainMenuView         → Main menu (full width)
        ├── CharacterCreateView  → Pilih background + nama
        ├── HubView              → Hub utama
        ├── DungeonMapView       → Grid dungeon map
        ├── DungeonGridMap       → Canvas 2D grid interaktif
        ├── CombatView           → Screen combat
        ├── ProfileView          → Stat sheet lengkap
        ├── ViewsBundle          → Inventory, Mercenary, Event,
        │                          Shop, Victory, GameOver views
        ├── MercChatPanel        → Panel chat kanan layar
        └── MercenaryDialogue    → Database dialog per merc × trigger

src/main/resources/arclightcity/ui/style/
└── arclight.css                 → Cyberpunk dark theme
```

---

## 🗺️ Roadmap

| Versi | Fokus | Status |
|---|---|---|
| v0.1.0 | Foundation — game bisa jalan end-to-end | ✅ Done |
| v0.2.0 | Bug fix krusial: loot, inventory, AI turn loop | ✅ Done |
| v0.2.1 | UI/Visual patch: warna, scroll, glow, merc bug | ✅ Done |
| v0.2.2 | ConcurrentModification fix, Dungeon Grid Map v1 | ✅ Done |
| v0.2.3 | Full grid exploration, fog of war, cardinal lines | ✅ Done |
| v0.2.4 | Backtrack bug fix | ✅ Done |
| v0.2.5 | REST diminishing heal, starter skills, shop basic | ✅ Done |
| v0.2.6 | Split layout 860px, Mercenary Chat Panel | ✅ Done |
| v0.3.0 | Combat polish: skill UI, target select, damage anim | 🔜 Next |
| v0.4.0 | Progression: level up UI, skill unlock screen | 📋 Planned |
| v0.5.0 | Content: enemy baru, boss kedua, shop expand | 📋 Planned |
| v0.6.0 | QoL: save/load, mercenary hire, tutorial | 📋 Planned |
| Post v0.6 | Craft system, sound effects | 💡 Idea |

---

## 📝 Dokumentasi

- [CHANGELOG.md](CHANGELOG.md) — detail perubahan tiap versi

---

## 👨‍💻 Author

Tugas Akhir — **Pemrograman Berorientasi Objek**
Terinspirasi dari: *Arclight City* by Dex App Studio

---

## 📄 License

Dibuat untuk keperluan akademik.
