# ⚡ ARCLIGHT CITY

![Version](https://img.shields.io/badge/version-v0.2.2-00E5FF?style=flat-square)
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
korporasi megabesar. Masuki dungeon bawah kota, lawan musuh yang makin kuat
di setiap lantai, kumpulkan loot, upgrade equipment, dan manage tim mercenary-mu.

Game ini sepenuhnya dimainkan lewat UI JavaFX — tidak ada command line input.
Semua screen dibangun secara programmatic (view-as-code), bukan dengan file FXML.

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
| Rest Room → HP/MP restore + notifikasi | ✅ Fixed v0.2 |
| Inventory (EQUIP / UPGRADE / CALIBRATE / USE) | ✅ Fixed v0.2 |
| Mercenary Management | ✅ Jalan |
| Profile / Stat Sheet | ✅ Jalan |
| Victory & Game Over Screen | ✅ Jalan |
| Event Room (pilihan + efek) | ✅ Jalan |
| Shop Screen | 🚧 Placeholder |
| Skill Selection UI | 🚧 Coming v0.3 |
| Save / Load | 🚧 Coming v0.6 |
| Sound Effects | 🚧 Coming |

---

## 📸 Screenshots (v0.2.0)

| Screen | Preview |
|--------|---------|
| Main Menu | Animasi title glowing cyan + grid background perspektif |
| Create Character | Card selector 6 background dengan bonus preview |
| Dungeon Map | Room grid dengan icon type + next room navigation |
| Combat | Enemy/ally cards dengan HP+Shield bars + combat log |
| Loot Found | Popup item yang didapat dengan rarity tag |
| Victory | Layar victory dengan reward EXP + Gold |
| Mercenary | Card stats + HP/Shield/MP bars per merc |

> *Screenshot aktual akan ditambahkan di update berikutnya*



- **Turn-based** berbasis stat **SPEED** — entity lebih cepat giliran duluan
- **2 bar vital**: HP (merah) + Shield (ungu) — Shield diserap terlebih dahulu sebelum HP
- **3 tipe damage**: Physical / Cyber / Energy — masing-masing punya resistensi berbeda
- **27 status effect**: DOT, CC, Buff, Debuff (lihat `StatusEffectType.java`)
- **28 stat unik** per entity melalui `StatSheet` (lihat `StatType.java`)
- **Damage Multiplier** sebagai stat tersendiri yang dipengaruhi equipment
- AI enemy punya 3 mode aksi: `normalAction()`, `specialAction()`, `desperateAction()`

---

## 👤 Background Origin (6 pilihan)

| Background | Keunggulan Stat Awal |
|---|---|
| 🥊 Street Brawler | Physical ATK, HP, Crit Chance, DMG Multiplier |
| 💻 Netrunner | Cyber ATK, MP, Skill Power, Cooldown Reduction |
| 🪖 Veteran Soldier | DEF, HP, Shield, Block Chance |
| ⚡ Energy Adept | Energy ATK, Lifesteal, Skill Power |
| 👻 Ghost Operative | Evasion, Speed, Crit Chance, Armor Pierce |
| 🔧 Techwright | Shield Regen, Sync Rate, Mercenary Buff |

---

## 🤝 Mercenary (7 tersedia)

| Merc | Role | Keunikan |
|---|---|---|
| Kira Voss | DPS Sniper | Stealth + guaranteed crit shot |
| Tank-RX9 | Tank | Shield tertinggi, Taunt, Counter |
| Sera Mend | Support Healer | Heal + Cleanse debuff party |
| Vector | DPS Assassin | Execute low-HP enemy + Hack |
| Magnus Forge | DPS AoE | Charge shot, Incendiary rounds |
| Echo Null | CC Specialist | EMP burst, Frequency Lock, Signal Jam |
| Lyra Bloom | Support Buffer | AoE Heal + Resonance buff party |

---

## 👾 Daftar Enemy

| Enemy | Race | Tier | Mechanic Unik |
|---|---|---|---|
| Street Thug | Human | Standard | Stun |
| Neon Serpent | Mutant | Standard | Bleed stack |
| Glitch Drone | Android | Minion | MP Drain, Self Destruct |
| Iron Clad | Cyborg | **Elite** | 3-fase armor (Heavy → Damaged → Core) |
| Void Specter | Specter | **Elite** | Kebal Physical, Phase Shift |
| **Null King** | Android | **BOSS** | 3 fase bertahap, Null Field, sistem HP shield |

---

## 🚀 Cara Run

### Prasyarat
- **JDK 25** (sesuai versi project)
- **Maven 3.9+** — download di https://maven.apache.org/download.cgi
  lalu tambahkan ke PATH, atau gunakan Maven bawaan NetBeans
- JavaFX **tidak perlu diinstall manual** — diunduh otomatis oleh Maven

### Menjalankan Game
```bash
cd ArclightCity
mvn javafx:run
```

### Membuka di NetBeans
1. `File` → `Open Project` → pilih folder `ArclightCity` (yang ada `pom.xml`)
2. NetBeans mendeteksi otomatis sebagai Maven project
3. Klik kanan project → **Run** atau tekan **F6**

### Build JAR
```bash
mvn package
# Output: target/ArclightCity-1.0-SNAPSHOT.jar
```

> **Catatan:** Pertama kali build akan mengunduh dependency JavaFX 25 dari
> Maven Central (~beberapa menit tergantung koneksi). Build berikutnya lebih cepat.

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

### Design Patterns yang Digunakan

| Pattern | Lokasi Implementasi |
|---|---|
| **Observer** | `CombatManager.addEventListener()`, `DungeonManager.setStateListener()` — event combat & dungeon disebarkan ke listener UI |
| **Factory** | `EntityFactory.createEnemy()`, `createMercenary()`, `createPlayer()` — satu titik pembuatan entity |
| **Strategy** | AI behavior per enemy: `normalAction()`, `specialAction()`, `desperateAction()` — polymorphism penuh |
| **Template Method** | `Enemy.decideAction()` mendefinisikan alur, subclass override `normalAction()` dll |
| **Builder** | `CombatEvent.Builder` dan `DungeonStateEvent.Builder` — konstruksi event yang kompleks secara fluent |

---

## 📁 Struktur Project

```
ArclightCity/
├── pom.xml                              ← konfigurasi Maven (JavaFX 25, JDK 25)
├── CHANGELOG.md                         ← log perubahan fitur per versi
├── CHANGELOG_FIXES.md                   ← log bug fixing teknis (migrasi Maven)
├── README.md                            ← file ini
└── src/
    └── main/
        ├── java/arclightcity/
        │   ├── engine/
        │   │   └── GameEngine.java       ← controller utama, state machine
        │   ├── entity/
        │   │   ├── base/                 ← Entity (abstract), EntityType
        │   │   ├── stats/                ← StatType (28 stat), StatSheet, DamageType
        │   │   ├── status/               ← StatusEffectType (27 efek), StatusEffect
        │   │   ├── player/               ← Player + 6 PlayerBackground
        │   │   ├── enemy/                ← Enemy, Boss, 5 implementasi enemy
        │   │   └── mercenary/            ← Mercenary, MercenaryType, 7 implementasi
        │   ├── combat/
        │   │   ├── CombatAction.java     ← representasi aksi per turn
        │   │   ├── CombatManager.java    ← engine pertarungan, turn queue
        │   │   ├── CombatEvent.java      ← event system combat (Builder pattern)
        │   │   ├── CombatResult.java     ← hasil akhir pertarungan
        │   │   ├── DamageCalculator.java ← kalkulasi damage, resistensi, crit
        │   │   ├── SkillExecutor.java    ← eksekusi skill & status effect
        │   │   └── TurnQueue.java        ← antrian giliran berbasis SPEED
        │   ├── dungeon/
        │   │   ├── DungeonManager.java   ← alur dungeon, floor, room transitions
        │   │   ├── ProceduralGenerator.java ← generasi floor & room secara prosedural
        │   │   ├── Floor.java / Room.java   ← model data dungeon
        │   │   ├── DungeonEvent.java     ← event room (pilihan & efek)
        │   │   └── DungeonStateEvent.java ← event state mesin dungeon (Builder)
        │   ├── item/
        │   │   ├── Item.java             ← base class item (abstract)
        │   │   ├── Equipment.java        ← base equipment + stat modifier
        │   │   ├── Weapon / Armor / Accessory.java
        │   │   ├── Consumable.java       ← potion, booster
        │   │   ├── Material.java         ← bahan upgrade
        │   │   ├── Inventory.java        ← bag + slot equipment player
        │   │   ├── LootManager.java      ← generasi loot berdasarkan floor
        │   │   ├── UpgradeSystem.java    ← sistem upgrade item
        │   │   └── CalibrationSystem.java ← sistem kalibrasi equipment
        │   └── ui/
        │       ├── ArclightApp.java      ← entry point JavaFX (extends Application)
        │       ├── controller/
        │       │   └── SceneRouter.java  ← navigasi & transisi antar screen
        │       ├── util/
        │       │   └── UIFactory.java    ← komponen UI reusable (button, panel, dll)
        │       └── view/                 ← semua screen game (pure JavaFX, tanpa FXML)
        │           ├── MainMenuView.java
        │           ├── CharacterCreateView.java
        │           ├── HubView.java
        │           ├── DungeonMapView.java
        │           ├── CombatView.java
        │           ├── ProfileView.java
        │           ├── ViewsBundle.java  ← Inventory, Mercenary, Shop, Event, Victory, GameOver
        │           ├── InventoryView.java / MercenaryView.java / ...
        │           └── (dan lainnya)
        └── resources/
            └── ui/style/
                └── arclight.css         ← cyberpunk dark theme
```

---

## 🗺️ Roadmap

| Versi | Fokus | Status |
|---|---|---|
| v0.1.0 | Foundation — migrasi Maven, game bisa jalan end-to-end | ✅ Done |
| v0.2.0 | Bug fix krusial: loot, inventory, AI loop, rest room | ✅ Done |
| v0.3.0 | Combat polish: skill selection UI, target selection, damage animation | 🔜 Next |
| v0.4.0 | Progression: level up UI, skill unlock screen, stat growth | 📋 Planned |
| v0.5.0 | Content: enemy baru, boss kedua, shop fungsional | 📋 Planned |
| v0.6.0 | QoL: save/load system, tutorial, mercenary hire di hub | 📋 Planned |

---

## 📝 Dokumentasi

| File | Isi |
|---|---|
| [CHANGELOG.md](CHANGELOG.md) | Log perubahan fitur per versi game |
| [CHANGELOG_FIXES.md](CHANGELOG_FIXES.md) | Log teknis: 17 bug yang diperbaiki selama migrasi NetBeans Ant → JavaFX Maven |

---

## 👨‍💻 Author

Tugas Akhir — **Pemrograman Berorientasi Objek**, Semester 2

Terinspirasi dari: *Arclight City* by Dex App Studio

---

## 🔧 Git Workflow (untuk kontributor)

### Push pertama kali
```bash
git init
git remote add origin https://github.com/USERNAME/ArclightCity.git
git add .
git commit -m "feat: initial commit v0.1.0"
git push -u origin main
```

### Update versi baru
```bash
git add .
git commit -m "feat: v0.2.0 - loot system, inventory fix, AI turn guard"
git push origin main
```

### Kalau muncul merge conflict di README
```bash
# Jangan langsung push kalau ada conflict marker <<<<<<< HEAD
git pull origin main --rebase
# Buka file yang conflict, hapus semua marker:
# <<<<<<< HEAD  → hapus
# =======       → hapus
# >>>>>>> hash  → hapus
# Sisakan hanya versi yang benar
git add README.md
git rebase --continue
git push origin main
```

---

## 📄 License

Dibuat untuk keperluan akademik.
