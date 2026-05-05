# ⚔ MYTHIC ITEM OBTAINED

![Version](https://img.shields.io/badge/version-v0.5.0-FFB830?style=flat-square)
![Java](https://img.shields.io/badge/Java-25-orange?style=flat-square)
![JavaFX](https://img.shields.io/badge/JavaFX-25-blue?style=flat-square)
![Build](https://img.shields.io/badge/build-Maven-AA00FF?style=flat-square)
![Status](https://img.shields.io/badge/status-Alpha-CC3300?style=flat-square)

> *"Kumpulkan 5 Serpihan Red Essence. Tempa Red Blossom Katana. Kalahkan Theresa."*

**Mythic Item Obtained** adalah RPG roguelite turn-based bergaya Nusantara dengan GUI JavaFX murni.
Dibuat sebagai Tugas Akhir **Pemrograman Berorientasi Objek 2026**.

---

## 📖 Cerita

**Asuna** — seorang gamer perempuan dari dunia modern, gemar bermain game hack-and-slash dengan katana sebagai senjata andalan.

Suatu malam ia tertidur di depan layar game yang masih menyala. Ketika bangun, ia sudah berada di **Nusantara** — benua kuno yang indah namun kini terancam.

Ras **Demon Lord** dipimpin **Theresa** sedang mengubah tanah hijau Nusantara menjadi dataran es hampa. Asuna harus berjuang bersama para kawula untuk mengumpulkan **5 Serpihan Red Essence** dari 5 boss, menempa **Red Blossom Katana**, dan mengakhiri ancaman Theresa di Floor 51.

---

## 🎮 Gameplay

### Core Loop
```
Markas → Masuk Dungeon → Jelajah Floor → Lawan Musuh → Ambil Loot
       ↓                                               ↓
    Upgrade Gear ←─────────── Kembali ──────────────── Boss Drop Shard
       ↓
    (5 Shard) → Tempa Red Blossom Katana → Lawan Theresa (F51)
```

### Sistem Red Essence Shard
| Floor | Boss | Shard |
|-------|------|-------|
| F10 | Batara Kala | ✦ Serpihan 1/5 |
| F20 | Nyi Roro Kidul | ✦ Serpihan 2/5 |
| F30 | Rangda Agung | ✦ Serpihan 3/5 |
| F40 | Garuda Mahaguru | ✦ Serpihan 4/5 |
| F50 | Semar Pamungkas | ✦ Serpihan 5/5 |
| **F51** | **THERESA** | **⚔ Final Battle** |

### Kawula (Mercenary)
| Nama | Peran | Kepribadian |
|------|-------|-------------|
| Srikandi | Pemanah Bayangan | Dingin, sedikit kata |
| Gatot Kaca | Ksatria Baja | Formal, android |
| Nyai Roro | Tabib Mistis | Hangat, peduli |
| Rangga | Pembunuh Bayaran | Sarkastis, humor gelap |
| Bima | Petarung Agung | Antusias, keras |
| Ki Ageng | Dukun Tua | Bijak, prophetic |
| Dewi Sri | Penjaga Keseimbangan | Spiritual, puitis |

---

## ✅ Status Fitur (v0.5.0)

| Fitur | Status |
|-------|--------|
| Protagonis Asuna (fixed character) | ✅ v0.5.0 |
| Red Blossom Katana (1 Mythic item) | ✅ v0.5.0 |
| Red Essence Shard system (5 shard) | ✅ v0.5.0 |
| Boss per 10 floor (F10-F50) | ✅ v0.5.0 |
| Theresa — Final Boss (F51) | ✅ v0.5.0 |
| Weapon types → semua pedang | ✅ v0.5.0 |
| GUI Total Nusantara Dark Gold | ✅ v0.4.4-v0.4.5 |
| Lokalisasi penuh Bahasa Indonesia | ✅ v0.4.4 |
| MercChatPanel fungsional | ✅ v0.3.7d |
| 20 enemy + 5 boss Nusantara | ✅ v0.4.1 |
| Combat (turn order, skill, floating damage) | ✅ v0.3.x |
| Save/Load System | ✅ v0.3.4 |
| Dungeon Grid Map (fog of war, animasi) | ✅ v0.2.x |
| Dialog kawula 100+ (Bahasa Indonesia) | ✅ v0.4.3 |
| Floor transition animation | ✅ v0.3.7 |
| Lore/dialog boss per fase | 🚧 In progress |
| Portrait karakter/kawula | 🚧 Asset pending |
| Sound effects | 🚧 TBD |

---

## 🚀 Cara Run

```bash
git clone https://github.com/ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX.git
cd ArclightCity
mvn javafx:run
```

**Requirements:** JDK 25+, Maven 3.9+

---

## 🏗 Arsitektur

```
arclightcity/
├── engine/      → GameEngine (state machine)
├── entity/      → Player(Asuna), Enemy(20+), Mercenary(7), Boss(6)
├── combat/      → CombatManager, SkillExecutor, TurnQueue
├── dungeon/     → DungeonManager, ProceduralGenerator, Floor, Room
├── item/        → Item hierarchy, Inventory, LootManager (Red Blossom Katana)
├── save/        → GameSaveState, SaveManager, GameStateConverter
└── ui/
    ├── ArclightApp       → Entry point (860×920px)
    ├── controller/       → SceneRouter (persistent single scene)
    ├── util/UIFactory    → Komponen reusable, palette Nusantara Dark Gold
    └── view/             → Semua screen (Bahasa Indonesia penuh)
```

**Design Patterns:** Observer, Factory, Strategy, Template Method, State Machine

---

## 📝 Dokumentasi

Lihat [CHANGELOG.md](CHANGELOG.md) untuk riwayat versi lengkap.

---

*Tugas Akhir — Pemrograman Berorientasi Objek | 2026*
