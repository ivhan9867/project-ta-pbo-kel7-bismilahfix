# ⚡ MYTHIC ITEM OBTAINED

![Version](https://img.shields.io/badge/version-v0.4.0-00E5FF?style=flat-square)
![Java](https://img.shields.io/badge/Java-25-orange?style=flat-square)
![JavaFX](https://img.shields.io/badge/JavaFX-25-blue?style=flat-square)
![Build](https://img.shields.io/badge/build-Maven-AA00FF?style=flat-square)
![Status](https://img.shields.io/badge/status-In%20Development-FFD600?style=flat-square)

> *"Bertahan hidup di kota yang tidak pernah tidur — dan tidak pernah memaafkan."*

**Mythic Item Obtained** adalah game RPG roguelite turn-based dengan GUI JavaFX murni (tanpa FXML).
Dibangun sebagai Tugas Akhir **Pemrograman Berorientasi Objek**.
Terinspirasi dari game mobile *Arclight City* oleh Dex App Studio.

Window **860×920px** — split layout: game area 560px (kiri) + mercenary chat 300px (kanan).

---

## ✅ Status Fitur (v0.3.7)

| Fitur | Status |
|-------|--------|
| Main Menu | ✅ |
| Create Character (6 background) | ✅ |
| Hub Screen | ✅ |
| Dungeon Grid Map 2D (fog of war, klik tile) | ✅ v0.2.3 |
| Room Preview di Hover Tooltip | ✅ v0.3.7 |
| Dungeon Map Visual Overhaul (pulse, marching ants) | ✅ v0.2.8 |
| Floor Transition Animation | ✅ v0.3.7 |
| Combat Turn-Based | ✅ |
| Turn Order Bar | ✅ v0.3.0 |
| Skill Selection Popup | ✅ v0.3.0 |
| Target Selection (klik enemy) | ✅ v0.3.0 |
| Combat Speed Control | ✅ v0.3.0 |
| Floating Damage Numbers | ✅ v0.3.7 |
| EXP & Gold diterapkan setelah combat | ✅ v0.3.5 |
| Level Up Notification | ✅ v0.3.5 |
| Loot Room → Inventory | ✅ v0.2.0 |
| REST Room Diminishing Heal | ✅ v0.2.5 |
| Inventory (EQUIP / UPGRADE / CALIBRATE / USE) | ✅ |
| Starter Skills per Background | ✅ v0.2.5 |
| Shop (beli item, harga deterministik) | ✅ v0.2.5 |
| Mercenary Chat Panel (150+ dialog) | ✅ v0.2.6 |
| Mercenary Hire di Hub | ✅ v0.3.6 |
| Profile Tab STATS (semua 26 stat) | ✅ v0.3.5 |
| Profile Tab EQUIPMENT | ✅ v0.3.5 |
| Profile Tab SKILLS (unlock/equip dari UI) | ✅ v0.3.5 |
| Status Effect Tooltip | ✅ v0.3.6 |
| Victory & Game Over Screen | ✅ |
| Event Room | ✅ |
| Save / Load (Java Serialization) | ✅ v0.3.4 |
| Tutorial / Onboarding | 🚧 v0.6 |
| Sound Effects | 🚧 TBD |
| Craft System | 🚧 TBD |
| Hidden Room | 🚧 v0.5 |
| Boss Kedua | 🚧 v0.5 |

---

## ✨ Fitur Utama

### ⚔️ Combat (v0.3)
- Turn-based berbasis SPEED stat
- **Turn Order Bar** — antrian 6 entity ke depan
- **Skill Selection Popup** — pilih skill, lihat deskripsi + MP cost
- **Target Selection** — klik enemy yang ingin diserang
- **Floating Damage Numbers** — angka melayang di atas enemy
- **Combat Speed Control** — 1× / 2× / SKIP
- 2 bar vital: HP + Shield | 3 tipe damage | 28 status effect

### 🗺️ Dungeon Grid Map (v0.2.8 + v0.3.7)
- Grid penuh COLS×ROWS, fog of war 3 state
- **Marching ants** border untuk reachable tile
- **Player breathing pulse** — 3 ring concentric sinusoidal
- **Room preview hover** — enemy names, REST heal remaining
- **Floor transition animation** — fade out/in dengan teks "DESCENDING TO FLOOR X"
- Boss wajib dikalahkan untuk DESCEND

### 💬 Mercenary Chat Panel (v0.2.6)
- Panel 300px persistent semua screen
- 7 mercenary, kepribadian unik, 150+ dialog
- Hire merc baru langsung dari menu Mercenary

### 💾 Save System (v0.3.4)
- Java Serialization — tidak butuh library eksternal
- 1 manual save + auto-save backup setiap turun floor
- File: `%APPDATA%\ArclightCity\` (Windows)

---

## 👤 Background Origin

| Background | Starter Skills |
|---|---|
| 🥊 Street Brawler | POWER_STRIKE + EXECUTE |
| 💻 Netrunner | DEEP_HACK + VIRUS_UPLOAD |
| 🪖 Veteran Soldier | IRON_SHIELD + SHOCKWAVE |
| ⚡ Energy Adept | ENERGY_DRAIN + BIO_IRRADIATE |
| 👻 Ghost Operative | PHANTOM_SHOT + SHADOW_STEP |
| 🔧 Techwright | EMP_BURST + FIELD_BARRIER |

---

## 🤝 Mercenary

| Merc | Role | Kepribadian | Hire Cost |
|---|---|---|---|
| Kira Voss | DPS Sniper | Dingin, profesional | 300g |
| Tank-RX9 | Tank | Formal, android | Starter (gratis) |
| Sera Mend | Support Medic | Hangat, caring | 350g |
| Vector | DPS Assassin | Sarkastis | 400g |
| Magnus Forge | DPS AoE | Antusias keras | 380g |
| Echo Null | CC Jammer | Misterius teknis | 420g |
| Lyra Bloom | Support Shaman | Spiritual poetic | 360g |

---

## 👾 Enemy

| Enemy | Tier | Mechanic |
|---|---|---|
| Street Thug | Standard | Stun |
| Neon Serpent | Standard | Bleed stack |
| Glitch Drone | Minion | MP Drain, Self Destruct |
| Iron Clad | **Elite** | 3 armor phase |
| Void Specter | **Elite** | Immune Physical |
| **Null King** | **BOSS** | 3 fase, Null Field |

---

## 🚀 Cara Run

```bash
git clone https://github.com/ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX.git
cd ArclightCity
mvn javafx:run
```

Requirements: JDK 25 (atau 17+), Maven 3.9+

---

## 🏗️ Arsitektur

```
Entity (abstract)
├── Player
├── Enemy → StreetThug, NeonSerpent, GlitchDrone, IronClad, VoidSpecter
│   └── Boss → NullKing
└── Mercenary → KiraVoss, TankRX9, SeraMend, Vector, MagnusForge, EchoNull, LyraBloom

Item (abstract)
├── Equipment → Weapon, Armor, Accessory
├── Consumable
└── Material
```

**Design Patterns:** Observer, Factory, Strategy, Template Method

---

## 📁 Package Structure

```
arclightcity/
├── engine/      → GameEngine (state machine)
├── entity/      → Player, Enemy, Mercenary, Stats, Status
├── combat/      → CombatManager, DamageCalculator, SkillExecutor, TurnQueue
├── dungeon/     → DungeonManager, ProceduralGenerator, Floor, Room
├── item/        → Item hierarchy, Inventory, LootManager
├── save/        → GameSaveState, SaveManager, GameStateConverter
└── ui/
    ├── ArclightApp    → Entry point (860×920px)
    ├── controller/    → SceneRouter + MercChatPanel global
    ├── util/UIFactory → Komponen reusable
    └── view/          → Semua screen views
```

---

## 🗺️ Roadmap

| Versi | Status |
|---|---|
| v0.1.0 – v0.2.0 | ✅ Foundation + bug fix |
| v0.2.1 – v0.2.8 | ✅ UI, map, chat, visual |
| v0.3.0 – v0.3.7 | ✅ Combat, save, hire, transition |
| v0.4.0 | 📋 Progression: stat up, craft |
| v0.5.0 | 📋 Content: boss baru, enemy baru, hidden room |
| v0.6.0 | 📋 QoL: tutorial, onboarding |

---

## 📝 Dokumentasi

Lihat [CHANGELOG.md](CHANGELOG.md) untuk detail tiap versi.

---

*Tugas Akhir — Pemrograman Berorientasi Objek*
