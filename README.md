# ⚡ ARCLIGHT CITY

<<<<<<< HEAD
![Version](https://img.shields.io/badge/version-v0.2.0-00E5FF?style=flat-square)
![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square)
![JavaFX](https://img.shields.io/badge/JavaFX-25-blue?style=flat-square)
![Build](https://img.shields.io/badge/build-Maven-AA00FF?style=flat-square)
![Status](https://img.shields.io/badge/status-In%20Development-FFD600?style=flat-square)

> *"Bertahan hidup di kota yang tidak pernah tidur — dan tidak pernah memaafkan."*

**Arclight City** adalah game RPG roguelite berbasis teks dengan GUI JavaFX.
Dibangun sebagai Tugas Akhir mata kuliah **Pemrograman Berorientasi Objek**,
terinspirasi dari game mobile *Arclight City* oleh Dex App Studio.
=======
> *"Bertahan hidup di kota yang tidak pernah tidur — dan tidak pernah memaafkan."*

**Arclight City** adalah game RPG berbasis teks dengan elemen roguelite yang dibangun menggunakan **Java + JavaFX**. Proyek ini merupakan tugas akhir mata kuliah **Pemrograman Berorientasi Objek**, terinspirasi dari game mobile *Arclight City* oleh Dex App Studio.
>>>>>>> de9e6d8b52d65ef51f84370e7418b006c7d423d5

---

## 🎮 Tentang Game

<<<<<<< HEAD
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
=======
Kamu adalah seorang runner di **Arclight City** — kota cyberpunk yang dikuasai korporasi megabesar, penuh dengan gang jalanan, entitas digital yang bocor ke dunia nyata, dan teknologi neon yang lebih berbahaya dari senjata apapun.

Tujuanmu: masuk ke **dungeon** bawah kota, bertarung melawan musuh yang makin kuat di setiap lantai, kumpulkan loot, upgrade equipment, dan buktikan bahwa kamu bisa bertahan lebih lama dari runner sebelumnya.

---

## ✨ Fitur Utama

### ⚔️ Combat System
- **Turn-based** berbasis kecepatan (SPEED stat) — entity lebih cepat bisa dapat giliran 2x sebelum yang lambat
- **3 tipe damage**: Physical, Cyber, Energy — masing-masing punya resistensi tersendiri
- **2 bar vital terpisah**: HP dan Shield — Shield diserap lebih dulu sebelum HP kena
- **28 status effect**: DOT (Burn, Bleed, Virus), CC (Stun, Freeze, Hack, Sleep), Buff/Debuff
- **Damage Multiplier** sebagai stat tersendiri — makin tinggi tier item, makin besar multiplier

### 🏰 Dungeon System
- **Procedural generation** — setiap run, setiap lantai berbeda
- **9 tipe room**: Enemy, Elite, Boss, Loot, Rest, Event, Shop, Trap, Empty
- **Branching path** — pilih jalur mana yang mau diambil
- **5 tema floor**: Neon Slum, Corporate HQ, Data Vault, Neon Wastes, Void Rift
- Makin dalam → enemy makin kuat, loot makin langka

### 👤 Character System
- **Classless skill system** — bebas pilih skill dari pool manapun
- **6 background origin** dengan bonus stat unik:
  - 🥊 Street Brawler — fisik tinggi, damage multiplier
  - 💻 Netrunner — cyber attack, skill power, CDR
  - 🪖 Veteran Soldier — tank, shield tinggi, block
  - ⚡ Energy Adept — energy attack, lifesteal, skill power
  - 👻 Ghost Operative — evasion, crit, armor pierce
  - 🔧 Techwright — shield regen, sync rate, mercenary buff

### 🤝 Mercenary System
- **7 mercenary** dengan role dan AI berbeda:
  - 🎯 **Kira Voss** (Ghost Sniper) — stealth + guaranteed crit dari bayang-bayang
  - 🤖 **Tank-RX9** (Combat Android) — tank terkuat, shield & taunt
  - 💉 **Sera Mend** (Field Medic) — healer + cleanse debuff
  - 🗡️ **Vector** (Cyber Assassin) — execute + hack musuh menyerang rekannya sendiri
  - 💣 **Magnus Forge** (Heavy Gunner) — AoE damage tertinggi, charge shot
  - 📡 **Echo Null** (Signal Jammer) — CC specialist, EMP, frequency lock
  - 🌸 **Lyra Bloom** (Neon Shaman) — AoE heal + resonance buff seluruh party
- **Loyalty system** — makin sering dipakai, makin kuat (hingga Soul Sync level 10)
- **Synergy bonuses** antar mercenary tertentu

### 🎒 Item & Progression
- **5 rarity tier**: Common → Uncommon → Rare → Epic → Legendary
- **Upgrade system** — naikan level item dengan material dungeon
- **Calibration system** — re-roll bonus stat item, dengan chance sukses berbeda per rarity
- **Procedural item generation** — weapon, armor, accessory di-generate secara acak dengan stat yang bervariasi

### 🎲 Event System
- **10+ jenis event** dengan pilihan yang berpengaruh:
  - 🔧 Calibration Terminal — kalibrasi gratis
  - 💜 Neon Fountain — pilih antara heal HP atau buff senjata
  - 💾 Data Cache — pilih EXP atau Gold
  - 📦 Mystery Box — risiko tinggi, reward tinggi
  - ☣️ Corrupted Cache — loot tapi mungkin kena Virus
  - ⚡ Electric Trap, ❄️ Cryo Trap, 🔥 Neon Burn, dan lainnya

---

## 👾 Daftar Enemy

| Enemy | Race | Tier | Mechanic Khusus |
|-------|------|------|-----------------|
| Street Thug | Human | Standard | Power Strike + Stun |
| Neon Serpent | Mutant | Standard | Stack Bleed, Coil Strike AoE |
| Glitch Drone | Android | Minion | Drain MP, Virus Upload, Self Destruct |
| Iron Clad | Cyborg | **Elite** | 3 armor phase, Shockwave AoE |
| Void Specter | Specter | **Elite** | Immune Physical, Phase Shift, Corrupt balik buff jadi debuff |
| **Null King** | Android | **BOSS** | 3 fase, Null Field (hapus semua buff party) |

---

## 🖥️ Screenshots

> *(Coming soon — project masih dalam pengembangan)*

```
╔══════════════════════════════╗
║      A R C L I G H T        ║
║      C  I  T  Y             ║
║  ─── CYBERPUNK RPG ───       ║
║                              ║
║   [ ENTER ARCLIGHT CITY ]   ║
║   [ CONTINUE ]              ║
╚══════════════════════════════╝
>>>>>>> de9e6d8b52d65ef51f84370e7418b006c7d423d5
```

---

<<<<<<< HEAD
## 🏗️ Arsitektur OOP

```
Entity (abstract)
├── Player
├── Enemy (abstract) → StreetThug, NeonSerpent, GlitchDrone,
│                      IronClad, VoidSpecter
│                      └── Boss (abstract) → NullKing
└── Mercenary (abstract) → KiraVoss, TankRX9, SeraMend,
                            Vector, MagnusForge, EchoNull, LyraBloom
=======
## 🏗️ Arsitektur & Konsep OOP

Project ini mengimplementasikan konsep OOP secara eksplisit:

### Inheritance
```
Entity (abstract)
├── Player
├── Enemy (abstract)
│   ├── StreetThug, NeonSerpent, GlitchDrone
│   ├── IronClad, VoidSpecter
│   └── Boss (abstract) → NullKing
└── Mercenary (abstract)
    └── KiraVoss, TankRX9, SeraMend, Vector, MagnusForge, EchoNull, LyraBloom
>>>>>>> de9e6d8b52d65ef51f84370e7418b006c7d423d5

Item (abstract)
├── Equipment → Weapon, Armor, Accessory
├── Consumable
└── Material
```

### Design Patterns
<<<<<<< HEAD

| Pattern | Implementasi |
|---|---|
| Observer | `CombatManager.addEventListener()`, `DungeonManager.setStateListener()` |
| Factory | `EntityFactory.createEnemy()`, `createMercenary()` |
| Strategy | AI behavior per enemy: `normalAction()`, `specialAction()`, `desperateAction()` |
| Template Method | `Enemy.decideAction()` template, subclass override |
=======
| Pattern | Implementasi |
|---------|-------------|
| **Observer** | `CombatManager.addEventListener()`, `DungeonManager.setStateListener()` |
| **Factory** | `EntityFactory.createEnemy()`, `EntityFactory.createMercenary()` |
| **Strategy** | AI behavior per enemy: `normalAction()`, `specialAction()`, `desperateAction()` |
| **Template Method** | `Enemy.decideAction()` sebagai template, subclass override aksi spesifik |

---

## 🛠️ Tech Stack

| Komponen | Teknologi |
|----------|-----------|
| Language | Java 17+ |
| UI Framework | JavaFX 17+ |
| Build Tool | Apache Ant (NetBeans) |
| IDE | NetBeans 17+ |

---

## 🚀 Cara Run

### Prerequisites
- JDK 17 atau 21
- JavaFX SDK 17+ ([download di sini](https://gluonhq.com/products/javafx/))
- NetBeans 17+

### Langkah Setup
1. Clone atau download project ini
2. Buka NetBeans → **File → Open Project** → pilih folder `ArclightCity_NetBeans`
3. Tambah JavaFX library: klik kanan project → **Properties → Libraries → Add Library**
4. Set VM Options di **Properties → Run**:
```
--module-path "/path/to/javafx-sdk/lib"
--add-modules javafx.controls,javafx.fxml,javafx.graphics
--add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED
```
5. Tekan **F6** untuk run

**Main class:** `arclightcity.ui.ArclightApp`

> Lihat `SETUP_GUIDE.md` untuk panduan lebih lengkap.
>>>>>>> de9e6d8b52d65ef51f84370e7418b006c7d423d5

---

## 📁 Struktur Project

```
<<<<<<< HEAD
src/main/java/arclightcity/
=======
src/arclightcity/
>>>>>>> de9e6d8b52d65ef51f84370e7418b006c7d423d5
├── engine/          # GameEngine — controller utama
├── entity/
│   ├── base/        # Entity, EntityType, CombatAction
│   ├── stats/       # StatType (26 stat), StatSheet, DamageType
<<<<<<< HEAD
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
=======
│   ├── status/      # 28 StatusEffectType
│   ├── player/      # Player + 6 PlayerBackground
│   ├── enemy/       # 5 enemy type + Boss system
│   └── mercenary/   # 7 mercenary dengan AI unik
├── combat/          # CombatManager, DamageCalculator, SkillExecutor, TurnQueue
├── dungeon/         # DungeonManager, ProceduralGenerator, DungeonEvent
├── item/            # Item system, Inventory, UpgradeSystem, LootManager
└── ui/
    ├── ArclightApp.java      # Entry point JavaFX
    ├── controller/           # SceneRouter
    ├── util/                 # UIFactory (komponen reusable)
    ├── view/                 # Semua screen (Hub, Combat, Dungeon, dll)
    └── style/arclight.css    # Cyberpunk dark theme
>>>>>>> de9e6d8b52d65ef51f84370e7418b006c7d423d5
```

---

<<<<<<< HEAD
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
=======
## 🗺️ Game Flow

```
Main Menu
    ↓
Create Character (nama + pilih background)
    ↓
Hub (stat, mercenary, inventory)
    ↓
Enter Dungeon → Dungeon Map
    ↓
Navigate Rooms → Enemy/Event/Loot/Rest
    ↓
Combat → Victory/Defeat
    ↓
Boss Room → Floor Complete
    ↓
Descend → Next Floor (makin susah)
    ↓
Return to Hub → Upgrade & Calibrate
```

---

## 📋 Roadmap

- [ ] Pisah `ItemSystem.java` ke file terpisah per class
- [ ] Save/Load system (Java Serialization)
- [ ] Skill registry yang proper (interface `Skill`)
- [ ] More enemy types per zone
- [ ] Sound effects
- [ ] Lebih banyak boss dengan mechanic unik
- [ ] Quest system
>>>>>>> de9e6d8b52d65ef51f84370e7418b006c7d423d5

---

## 👨‍💻 Author

<<<<<<< HEAD
Tugas Akhir — **Pemrograman Berorientasi Objek**

Terinspirasi dari: *Arclight City* by Dex App Studio
=======
Dibuat sebagai tugas akhir mata kuliah **Pemrograman Berorientasi Objek**.

Terinspirasi dari: **Arclight City** by Dex App Studio
>>>>>>> de9e6d8b52d65ef51f84370e7418b006c7d423d5

---

## 📄 License

<<<<<<< HEAD
Dibuat untuk keperluan akademik.
=======
Project ini dibuat untuk keperluan akademik.
>>>>>>> de9e6d8b52d65ef51f84370e7418b006c7d423d5
