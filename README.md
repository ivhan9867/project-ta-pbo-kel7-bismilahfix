<div align="center">

# ⬡ MYTHIC ITEM OBTAINED

**— Roguelite RPG Turn-Based · Tema Nusantara —**

[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-25-1E90FF?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Status](https://img.shields.io/badge/Status-Full%20Release-44AA44?style=for-the-badge)](/)
[![Version](https://img.shields.io/badge/Version-v1.0.0-8A2BE2?style=for-the-badge)](/)

---

> *"Asuna, mahasiswi IT dari Jakarta, tersedot ke Nusantara — dimensi paralel*
> *di mana mitologi Jawa, Bali, Sunda, dan Kalimantan menjadi kenyataan fisik.*
> *Bersama 7 Guildmate legendaris, ia harus membebaskan Lima Penjaga Agung*
> *dan menghadapi Theresa — entitas beku dari dimensi yang tak pernah hangat."*

---

</div>

## 🎮 Tentang Game

**Mythic Item Obtained** adalah game **Roguelite RPG Turn-Based** yang dibangun sepenuhnya dari nol menggunakan **Java 25 + JavaFX 25** tanpa game engine eksternal. Seluruh UI, sistem combat, dungeon, dialog, dan gacha diimplementasi secara programatik (tanpa FXML).

> 🏆 **Tugas Akhir — Pemrograman Berorientasi Objek (PBO)**
> Kelompok 7 · Semester 2 · 2026

---

## ✨ Fitur Utama

### ⚔️ Sistem Combat
- Turn-based dengan **Speed Bar** visual — urutan giliran otomatis berdasarkan stat SPEED
- 4 aksi per giliran: **Serang / Jurus / Item / Bertahan** + opsi Kabur
- **40+ skill** berbeda dengan efek: damage, heal, buff/debuff, CC (stun, freeze, dll.)
- **Floating damage text** + SFX per tipe serangan (Physical/Cyber/Energy)
- **On-hit effects**: BLEED/BURN/POISON dengan chance; nilai >100% = selalu trigger + bonus DoT
- **Artifact auto-trigger**: saat CD habis, efek aktif otomatis tanpa memakan giliran
- Speed control: 1× / 2× / SKIP
- **9 tipe status effect**: BLEED, BURN, STUN, FREEZE, WEAKEN, SHIELD, REGEN, VIRUS, CORRODE

### 🗺️ Dungeon Explorer
- Grid **12 kolom × N baris** dengan fog of war adaptif
- Fog of war: hanya **cleared rooms** yang permanent visible; current + neighbors dinamis
- **5 tema zona** per 10 lantai: Pasar Malam Gaib → Candi Terlarang → Hutan Angker → Goa Naga → Kahyangan Rusak
- **8 tipe room**: Enemy, Loot, Rest, Event, Shop, Trap, Boss, Empty
- Boss unik tiap 10 lantai dengan mechanic multi-phase
- Cutscene/dialog otomatis trigger saat memasuki **Boss Room** (bukan floor number)

### 👥 Sistem Guildmate
- **7 guildmate** dengan role berbeda: Tank, DPS, Healer, Assassin, Breaker, Controller, Support
- Sistem **Loyalty** — makin sering dipakai, makin kuat (compound scaling per role)
- AI combat cerdas: auto-pilih target dan skill berdasarkan situasi
- Dialog dinamis 50+ baris unik per guildmate sesuai konteks dungeon
- **1 slot artifact** per guildmate (role-filtered)

### ⬡ Sistem Artefak Gacha
- **20 jenis artefak** dari 7 role (UNIVERSAL, TANK, HEALER, DPS, SUPPORT, ASSASSIN, BREAKER)
- Gacha economy: 1× = 800 Gold / 1 Tiket · 10× = 7200 Gold / 9 Tiket
- **Pity system**: pull ke-80 dijamin LEGENDARY+
- Rates: MYTHIC 1% · LEGENDARY 4% · EPIC 12% · RARE 18% · UNCOMMON 25% · COMMON 40%
- **Artifact pocket** — storage terpisah dari tas utama, tidak makan slot, unlimited
- **Animasi gacha 3 fase** (±2.6 detik): portal muncul → spin 4 cincin + flash → slide reveal
- Slide reveal satu kartu per kartu (rarest first), klik/SPASI untuk skip
- **Auto-save setelah gacha** — mencegah reload dan gacha ulang

### 🎭 Sistem Lore & Dialog
- **Visual novel style**: portrait kiri, dialog bawah, pilihan kanan
- **21 script dialog** + 3 video cutscene (opening, mid, ending)
- 6 mood portrait Asuna, 2 mood per guildmate, 2 fase per boss
- Pilihan dialog interaktif dengan konsekuensi

### 💾 Sistem Inventory & Equipment
- **60-slot tas** + artifact pocket terpisah (unlimited)
- **2 artifact slot** player di KIRI dan KANAN grid equipment
- 8 slot equipment: Senjata, Baju, Helm, Sepatu, Cincin×2, Aksesori×2
- Tab filter: SEMUA · ARTEFAK · SENJATA · BAJU · HELM · SEPATU · CINCIN · AKSESORI · KONSUMABLE · MATERIAL
- Upgrade + Kalibrasi equipment di Workshop Kota
- Batch sell item di toko

### 🎵 Audio
- **15 BGM MP3**: menu, hub, 5 tema dungeon, combat, elite, 3 boss, 2 cutscene, shop
- **8 SFX WAV**: hit (physical/cyber/energy), critical, heal, miss, victory, gameover
- Fade in/out smooth antar BGM · BGM 55% volume · SFX 60% volume
- Boss BGM otomatis berdasarkan tipe boss (Semar, Theresa masing-masing punya BGM sendiri)

### 💿 Save System
- **3 slot save manual** + **1 auto-save**
- Auto-save: setelah setiap room cleared, setelah gacha, setelah battle
- Menyimpan: posisi lantai, inventory (termasuk artifact pocket), equipment, skill, gold, progress cerita

---

## 📊 Statistik Teknis

| Metrik | Nilai |
|--------|-------|
| Total file Java | 105 file |
| Total baris kode | ~26.500 baris |
| Abstract class | 5 (Entity, Enemy, Boss, Mercenary, Item) |
| Design patterns | 6 (Observer, Factory, Facade, MVC, Singleton, State Machine) |
| Total asset | 264 file |
| BGM tracks | 15 MP3 |
| SFX clips | 8 WAV |
| Artifact types | 20 jenis |
| Enemy types | 20+ jenis + 6 boss |
| Dialog scripts | 21 script |
| Ukuran build | ~367 MB |

---

## 🚀 Cara Menjalankan

### Prasyarat

| Komponen | Versi Minimum | Catatan |
|----------|--------------|---------|
| ☕ JDK | 21+ | Direkomendasikan JDK 25 |
| 🎨 JavaFX | Otomatis | Di-bundle via Maven |
| 📦 Maven | 3.8+ | Untuk build & run |

### Langkah

```bash
# 1. Clone repository
git clone https://github.com/ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX

# 2. Masuk ke direktori proyek
cd MythicItemObtained

# 3. Jalankan
mvn javafx:run
```

> **Catatan Windows**: Game menggunakan audio format MP3 dan WAV yang didukung JavaFX secara native di Windows. Tidak perlu instalasi codec tambahan.

---

## 🏗️ Arsitektur OOP

```
┌──────────────── PRESENTATION LAYER ────────────────────────┐
│  MainMenu │ Hub │ CombatView │ DungeonMap │ GachaView      │
│  Profile  │ MercenaryView │ CutsceneView │ SaveLoadView    │
└──────────────────────┬─────────────────────────────────────┘
                       │  SceneRouter (22 navigasi)
┌──────────────────────▼─────────────────────────────────────┐
│               GameEngine  (Facade)                         │
└──┬───────────┬──────────┬──────────┬───────────────────────┘
   │           │          │          │
 Combat    Dungeon    Inventory    Save · Gacha · Audio
 Manager   Manager   /Loot Mgr   Manager · System · Mgr
   │           │          │
┌──▼───────────▼──────────▼────────────────────────────────┐
│  Entity → Player / Mercenary(7) / Enemy(20+) / Boss(6)  │
│  Item   → Equipment / Artifact(20) / Consumable          │
└──────────────────────────────────────────────────────────┘
```

### Konsep OOP yang Diimplementasikan

| Konsep | Implementasi Utama |
|--------|-------------------|
| **Encapsulation** | `StatSheet` — 3 layer stat private, akses via `get(StatType)` |
| **Inheritance** | Hierarki `Entity → Player/Mercenary/Enemy/Boss`, `Item → Equipment/Artifact/...` |
| **Polymorphism** | `List<Entity>` dalam TurnQueue, `DamageCalculator.calculate(Entity, Entity)` |
| **Abstraction** | `GameEngine` Facade, 5 abstract class |
| **Observer** | `CombatManager` emit event ke `CombatView` via `Consumer<CombatEvent>` |
| **Factory** | `EntityFactory`, `LootManager`, `GachaSystem.generateArtifact()` |
| **State Machine** | `DungeonManager`: IDLE → EXPLORING → IN_COMBAT → FLOOR_COMPLETE |

---

## 🗺️ Peta Dungeon

| Lantai | Zona | Boss |
|--------|------|------|
| 1–10 | Pasar Malam Gaib | Batara Kala |
| 11–20 | Candi Terlarang | Nyi Roro Kidul |
| 21–30 | Hutan Angker | Rangda Agung |
| 31–40 | Goa Naga | Garuda Mahaguru |
| 41–50 | Kahyangan Rusak | Semar Pamungkas |
| 51+ | Void Dimension | Theresa (6 fase) |

---

## 👥 Tim Pengembang

**Kelompok 7 — Tugas Akhir PBO 2026**

Repository: [ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX](https://github.com/ivhan9867/PROJECT-TA-PBO-KEL7-BismilahFIX)

---

<div align="center">

*⬡ Mythic Item Obtained · v1.0.0 · Full Release*

*Built with Java 25 + JavaFX 25 · No external game engine*

</div>
