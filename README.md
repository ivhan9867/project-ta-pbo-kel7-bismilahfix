# ⚡ ARCLIGHT CITY

> *"Bertahan hidup di kota yang tidak pernah tidur — dan tidak pernah memaafkan."*

**Arclight City** adalah game RPG berbasis teks dengan elemen roguelite yang dibangun menggunakan **Java + JavaFX**. Proyek ini merupakan tugas akhir mata kuliah **Pemrograman Berorientasi Objek**, terinspirasi dari game mobile *Arclight City* oleh Dex App Studio.

---

## 🎮 Tentang Game

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
```

---

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

Item (abstract)
├── Equipment → Weapon, Armor, Accessory
├── Consumable
└── Material
```

### Design Patterns
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

---

## 📁 Struktur Project

```
src/arclightcity/
├── engine/          # GameEngine — controller utama
├── entity/
│   ├── base/        # Entity, EntityType, CombatAction
│   ├── stats/       # StatType (26 stat), StatSheet, DamageType
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
```

---

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

---

## 👨‍💻 Author

Dibuat sebagai tugas akhir mata kuliah **Pemrograman Berorientasi Objek**.

Terinspirasi dari: **Arclight City** by Dex App Studio

---

## 📄 License

Project ini dibuat untuk keperluan akademik.
