<div align="center">

# ⚔️ MYTHIC ITEM OBTAINED

**— Isekai RPG Dungeon Crawler · Tema Nusantara —**

[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-25-1E90FF?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Status](https://img.shields.io/badge/Status-In%20Development-FFB830?style=for-the-badge)]()
[![Version](https://img.shields.io/badge/Version-v0.8.1a-8A2BE2?style=for-the-badge)]()

---

> *"Asuna, seorang gamer dari dunia modern, tersesat ke dimensi Nusantara yang kacau.*
> *Dungeon penuh makhluk mistis, artefak kuno, dan kutukan leluhur menantinya.*
> *Satu-satunya jalan pulang kumpulkan **Mythic Item** dan taklukkan Kahyangan Rusak."*

---

</div>

## 🎮 Tentang Game

**Mythic Item Obtained** adalah game **RPG Dungeon Crawler berbasis giliran (turn-based)** yang dibangun sepenuhnya dengan **Java + JavaFX** tanpa engine eksternal. Terinspirasi dari estetika dark-fantasy dan mitologi Indonesia, game ini menggabungkan sistem combat yang dalam, item procedural, dan enam jenis dungeon dengan tema Nusantara.

> 🏆 **Proyek Akhir — Pemrograman Berorientasi Objek (OOP)**
> Universitas Negeri Surabaya · Kelompok 7 · Semester 2

---

## 🚀 Cara Menjalankan

### Prasyarat
| Komponen | Versi | Catatan |
|----------|-------|---------|
| ☕ JDK | 25+ | OpenJDK atau Oracle JDK |
| 🎨 JavaFX | 25 | Otomatis via Maven |
| 📦 Maven | 3.8+ | Untuk build & run |

```bash
# Clone repositori
git clone https://github.com/ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX

# Masuk ke direktori
cd MythicItemObtained

# Jalankan game
mvn javafx:run
```

> 💡 **Tips PC Spek Rendah:** JVM args sudah dikonfigurasi di `pom.xml` (`-Xmx256m`, `-XX:+UseSerialGC`) untuk menghemat RAM.

---

## ✨ Fitur Utama

<table>
<tr>
<td width="50%">

### ⚔️ Combat System
- Turn-based dengan **speed bar** visual
- 4 aksi: **Serang / Jurus / Item / Bertahan**
- **Floating damage numbers** dekat entity
- Status effects: Bleed, Burn, Poison, Freeze, Stun, Taunt, Regen…
- Speed control: `1×` `2×` `SKIP`
- Flee mechanic dengan tombol KABUR

</td>
<td width="50%">

### 🎒 Item & Equipment
- **5 rarity tier:** Common → Legendary (+ Mythic)
- **8 slot equipment:** Senjata, Baju, Helm, Sepatu, 2× Cincin, 2× Aksesori
- **Upgrade system** berbasis tier lantai drop (cap: Tier×10)
- **Kalibrasi** dengan Cal Kit — reroll bonus stat
- **Batch sell** di penadah dengan filter rarity/tipe

</td>
</tr>
<tr>
<td>

### 🗺️ Dungeon Procedural
- Grid **12×N tile** yang penuh area horizontal
- Player start dari **tepi acak** tiap lantai
- 5 tema unik per 10 lantai
- Event room: pertempuran, loot, toko, istirahat

</td>
<td>

### 💾 Save System
- **Auto-save** per lantai
- **Manual save** kapan saja
- Lokasi: `%APPDATA%\MythicItemObtained\`
- Retry setelah gugur: restore HP, progress tetap

</td>
</tr>
</table>

---

## 🗺️ Peta Dungeon

| Lantai | Zona | Suasana |
|--------|------|---------|
| 🟡 1–10 | **Pasar Malam Gaib** | Pasar mistis bawah tanah — bau kemenyan dan daging bakar |
| 🟠 11–20 | **Candi Terlarang** | Candi kuno tersegel, penuh jebakan dan kutukan leluhur |
| 🟢 21–30 | **Hutan Angker** | Hutan belantara Kalimantan, dijaga roh-roh purba yang gelisah |
| 🔴 31–40 | **Goa Naga** | Gua bawah gunung berapi tempat naga purba bersemayam |
| 🟣 41+ | **Kahyangan Rusak** | Langit para dewa yang runtuh — berbahaya tapi penuh artefak sakti |

---

## 👥 Guildmate

Rekrut hingga **3 guildmate** sekaligus dari 7 karakter berbeda, masing-masing dengan AI, role, dan skill unik.

| Karakter | Role | Skill Khas | Buff | CC |
|----------|------|------------|------|----|
| 🛡️ **Gatot Kaca** | Tank | Iron Shield | FORTIFY (DEF+) | TAUNT |
| 🏹 **Srikandi** | DPS | Phantom Shot | FOCUS (CRIT+) | EXPOSE |
| 💚 **Nyai Roro** | Healer | Triage Heal | REGEN + BARRIER | SLOW |
| 💥 **Bima** | Breaker | Overload Shot | EMPOWERED (ATK+) | STUN |
| 🗡️ **Rangga** | Assassin | Execute | STEALTH | BLEED |
| ⚡ **Ki Ageng** | Controller | EMP Burst | SYNC (SPD+) | FREEZE |
| 🌸 **Dewi Sri** | Support | Neon Bloom | REGEN + BARRIER | WEAKEN |

> **AI Logic:** Buff tim → CC jika perlu → Serang. Healer prioritaskan ally kritis.

---

## 👹 Bestiary

<details>
<summary><b>🔓 Klik untuk lihat semua musuh (30+ jenis)</b></summary>

### Pasar Malam Gaib (1–10)
| Musuh | Tipe | Ancaman |
|-------|------|---------|
| Tuyul Pencuri | Cepat | Steal gold |
| Wewe Gombel | CC | Confuse |
| Pocong Listrik | Magic | AOE stun |
| Banaspati | DoT | Burn lingkungan |
| Babi Ngepet | Tank | Resistansi tinggi |

### Candi Terlarang (11–20)
| Musuh | Tipe | Ancaman |
|-------|------|---------|
| Rangda Merah | Elite | Multi-hit |
| Barong Rusak | Elite | Counterstrike |
| Leyak Api | Mage | Burn + Poison |
| Garuda Korup | Flyer | High evasion |
| Detya Wesi | Armored | Thorn reflect |

### Boss (Per 10 Lantai)
| Lantai | Boss | Kemampuan |
|--------|------|-----------|
| 10 | Genderuwo Mekanik | AOE Slam + Shield |
| 20 | Raksasa Kala | Time Warp |
| 30 | **Rangda Agung** | Multi-phase |

</details>

---

## 🎯 Status Effects

| Ikon | Kategori | Efek |
|------|----------|------|
| 🟢 | **Buff** | FORTIFY, REGEN, EMPOWERED, FOCUS, SYNC, BARRIER |
| 🔴 | **Debuff** | WEAKEN, SHRED, EXPOSE, CURSE |
| 🟠 | **DoT** | BLEED, BURN, POISON / VIRUS |
| 🔵 | **CC** | FREEZE, STUN, SLOW, TAUNT, SILENCE |

---

## 💎 Sistem Item

### Rarity Tier
```
⚪ COMMON   →   🟢 UNCOMMON   →   🔵 RARE   →   🟣 EPIC   →   🟡 LEGENDARY   →   🔴 MYTHIC
```

### On-Hit Effects (dari Equipment)
| Efek | Stat | Keterangan |
|------|------|------------|
| 🩸 Bleed on Hit | `BLEED_ON_HIT` | % chance pendarahan tiap serang |
| 🔥 Burn on Hit | `BURN_ON_HIT` | % chance bakar musuh |
| ☠️ Poison on Hit | `POISON_ON_HIT` | % chance racun |
| 💉 Lifesteal | `LIFESTEAL` | % damage kembali jadi HP |
| 🛡️ Thorn | `THORN` | % damage dikembalikan ke penyerang |

### Material Crafting
| Material | Sumber | Kegunaan |
|----------|--------|---------|
| ⚙️ Scrap Metal | Jual Common/Uncommon | Upgrade level rendah |
| 💡 Cyber Chip | Jual Uncommon/Rare | Upgrade level menengah |
| 💎 Neon Crystal | Jual Rare/Epic | Upgrade level tinggi |
| 🔮 Cal Kit | Jual Epic/Legendary | Kalibrasi stat bonus |

---

## 🏗️ Arsitektur

```
src/main/java/arclightcity/
│
├── 🎮 engine/
│   └── GameEngine.java          ← Pusat state & lifecycle game
│
├── 👤 entity/
│   ├── player/Player.java       ← Karakter utama, stat, skill
│   ├── mercenary/               ← 7 guildmate dengan AI unik
│   ├── enemy/                   ← 30+ jenis musuh + boss
│   └── stats/StatSheet.java     ← Multi-layer stat (base/equip/buff)
│
├── ⚔️ combat/
│   ├── CombatManager.java       ← Orchestrator giliran
│   ├── DamageCalculator.java    ← Formula damage + crit + resistansi
│   ├── SkillExecutor.java       ← Eksekusi skill & efek
│   └── TurnQueue.java           ← Antrian giliran berbasis SPEED
│
├── 🗺️ dungeon/
│   ├── DungeonManager.java      ← State machine dungeon
│   ├── ProceduralGenerator.java ← Generator lantai 12×N
│   ├── Floor.java               ← Model lantai & tema
│   └── Room.java                ← Tile dungeon (combat/loot/event/shop)
│
├── 🎒 item/
│   ├── Item.java / Equipment.java / Weapon.java / Armor.java
│   ├── Inventory.java           ← 8 slot equip + bag 100 slot
│   ├── LootManager.java         ← Procedural item generation
│   ├── UpgradeSystem.java       ← Upgrade berbasis tier lantai
│   └── CalibrationSystem.java   ← Reroll bonus stat
│
├── 💾 save/
│   ├── SaveManager.java         ← I/O ke disk
│   ├── GameSaveState.java       ← Serializable snapshot
│   └── GameStateConverter.java  ← Engine ↔ SaveState
│
└── 🖥️ ui/
    ├── ArclightApp.java         ← Entry point (1280×720px)
    ├── controller/SceneRouter   ← Navigasi antar layar
    ├── util/UIFactory           ← Design system & komponen
    ├── util/AssetManager        ← 173+ asset (sprite/portrait/bg)
    └── view/                    ← CombatView, DungeonMapView, dll.
```

---

## 🎨 UI & Asset

| Kategori | Resolusi | Lokasi |
|----------|----------|--------|
| Background | 940×620px | `resources/assets/backgrounds/` |
| Sprite Enemy | 180×180px | `resources/assets/sprites/enemy/` |
| Sprite Guildmate | 180×180px | `resources/assets/sprites/guildmate/` |
| Portrait | Flexible | `resources/assets/portraits/` |
| Icon | 64×64px | `resources/assets/icons/` |

---

## 📚 Konsep OOP yang Diterapkan

| Konsep | Implementasi |
|--------|-------------|
| **Encapsulation** | Private fields + getter/setter di semua entity (`Player`, `Enemy`, `Item`) |
| **Inheritance** | `Entity → Player / Mercenary / Enemy` · `Item → Equipment → Weapon / Armor` |
| **Polymorphism** | `List<Item>` menampung `Equipment` + `Material` · Override `takeDamage()` tiap subclass |
| **Abstraction** | Abstract class `Entity`, `Item` · `GameEngine` sebagai fasad sistem |
| **Observer Pattern** | `CombatManager` listener system — UI tidak perlu tahu internal combat |
| **MVC** | `GameEngine` (Model) · `*View` (View) · `SceneRouter` (Controller) |

---

## 👨‍💻 Tim Pengembang

**Proyek Akhir — Pemrograman Berorientasi Objek**
Kelompok 7 — Semester 2

| Anggota | Kontribusi Utama |
|---------|-----------------|
| *(nama anggota 1)* | Game Engine, Combat System |
| *(nama anggota 2)* | UI/UX, View Layer |
| *(nama anggota 3)* | Dungeon System, Procedural Generation |
| *(nama anggota 4)* | Item System, Save/Load |

🔗 **Repository:** [github.com/ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX](https://github.com/ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX)

---

<div align="center">

*Built with using Java 25 + JavaFX 25 · No external game engine · Pure OOP*

</div>
