<div align="center">

# ⚔ MYTHIC ITEM OBTAINED

**RPG Roguelite Dungeon Crawler — Nusantara Isekai**

![Version](https://img.shields.io/badge/versi-v0.5.8-FFB830?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-25-0096C7?style=for-the-badge)
![Status](https://img.shields.io/badge/status-Alpha-CC3300?style=for-the-badge)

> *"Kumpulkan 5 Serpihan Red Essence. Tempa Red Blossom Katana. Kalahkan Theresa."*

</div>

---

## 📖 Lore

**Asuna** adalah seorang gamer perempuan dari dunia modern yang gemar bermain game hack-and-slash. Suatu malam, kelelahan bermain, ia tertidur di depan layar yang masih menyala.

Ketika bangun, ia sudah berada di **Nusantara** — benua kuno yang kini terancam oleh ras **Demon Lord** pimpinan **Theresa**, yang ingin mengubah tanah hijau Nusantara menjadi dataran es hampa.

Asuna harus berjuang bersama para guildmate, mengumpulkan **5 Serpihan Red Essence** dari 5 boss untuk menempa **✦ Red Blossom Katana** — satu-satunya senjata yang dapat melukai Theresa.

---

## 🎮 Cara Main

```
Markas → Masuk Dungeon → Jelajah Floor → Lawan Musuh → Ambil Loot
                                                              ↓
                         Kembali ke Markas ← Kalahkan Boss → Dapat Serpihan
                              ↓
                         (5 Serpihan) → Red Blossom Katana → Theresa (Floor 51)
```

### Boss Utama & Serpihan Red Essence

| Floor | Boss | Serpihan |
|-------|------|:--------:|
| 10 | ☠ Batara Kala | ✦ 1/5 |
| 20 | ☠ Nyi Roro Kidul | ✦ 2/5 |
| 30 | ☠ Rangda Agung | ✦ 3/5 |
| 40 | ☠ Garuda Mahaguru | ✦ 4/5 |
| 50 | ☠ Semar Pamungkas | ✦ 5/5 |
| **51** | **💀 THERESA** | **⚔ Final** |

---

## ✅ Status Fitur (v0.5.8)

### Combat & Dungeon
| Fitur | Status |
|-------|:------:|
| Turn-based combat (giliran, skill, floating damage) | ✅ |
| Speed control (1×, 2×, skip) | ✅ |
| 20 enemy + 6 boss Nusantara | ✅ |
| Theresa — Final Boss 6 fase | ✅ |
| Dungeon grid map + fog of war | ✅ |
| Floor transition animation | ✅ |
| Boss defeat notification + toast | ✅ |

### Karakter & Equipment
| Fitur | Status |
|-------|:------:|
| Protagonis Asuna (fixed character) | ✅ |
| 8 slot equipment (Senjata/Baju/Helm/Sepatu/Cincin×2/Aksesori×2) | ✅ |
| Item detail popup (klik slot) | ✅ |
| Filter inventory (9 kategori Bahasa Indonesia) | ✅ |
| Upgrade biasa max +8 (gold) | ✅ |
| Ultra Enhance +9/+10 (Kristal, bisa gagal) | ✅ |
| Kalibrasi premium (Kalibrator) | ✅ |

### Skill System
| Fitur | Status |
|-------|:------:|
| Pohon Jurus — 3 cabang × 4 depth | ✅ |
| Unlock via Skill Point | ✅ |
| Equip/unequip langsung dari tree | ✅ |
| Tab JURUS di profil (Aktif + Terbuka) | ✅ |

### Kota & Economy
| Fitur | Status |
|-------|:------:|
| 🏪 Toko Senjata Pak Empu | ✅ |
| ⚗️ Kedai Jamu Mbah Jamu | ✅ |
| 🔨 Bengkel Empu (upgrade + kalibrasi) | ✅ |
| 💰 Penadah Barang (jual item) | ✅ |

### Guildmate
| Fitur | Status |
|-------|:------:|
| 7 guildmate unik | ✅ |
| Party max 3 orang | ✅ |
| Chat panel dengan portrait | ✅ |
| Dialog 100+ Bahasa Indonesia | ✅ |

### Save & UI
| Fitur | Status |
|-------|:------:|
| Save/Load Java Serialization | ✅ |
| Auto-save tiap turun floor | ✅ |
| GUI Nusantara Dark Gold | ✅ |
| Resolusi 1280×720 | ✅ |
| Dialog Persona-style saat boss | ✅ |
| Animasi: goldPulse, flicker, typewriter | ✅ |

### Pending
| Fitur | Status |
|-------|:------:|
| Lore intro Asuna (cutscene text) | 🚧 |
| Asset gambar (portrait, background) | 🚧 |
| Dialog boss per fase (Theresa) | 🚧 |
| Sound effects & BGM | 🚧 |
| Cutscene video ending | 🚧 |

---

## 👥 Guildmate

| Nama | Peran | Kepribadian |
|------|-------|-------------|
| Srikandi | Pemanah Bayangan | Dingin, sedikit kata |
| Gatot Kaca | Ksatria Baja | Formal, tegas |
| Nyai Roro | Tabib Mistis | Hangat, peduli |
| Rangga | Pembunuh Bayaran | Sarkastis, humor gelap |
| Bima | Petarung Agung | Antusias, keras |
| Ki Ageng | Dukun Tua | Bijak, prophetic |
| Dewi Sri | Penjaga Keseimbangan | Spiritual, puitis |

---

## 🚀 Cara Menjalankan

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
├── engine/          → GameEngine (state machine, event dispatch)
├── entity/          → Player(Asuna), Enemy(20+), Mercenary(7), Boss(6)
├── combat/          → CombatManager, SkillExecutor, TurnQueue
├── dungeon/         → DungeonManager, ProceduralGenerator, Floor, Room
├── item/            → Item hierarchy, Inventory (8 slot), LootManager
├── save/            → GameSaveState, SaveManager, GameStateConverter
└── ui/
    ├── ArclightApp.java        → Entry point (1280×720)
    ├── controller/SceneRouter  → Single persistent scene architecture
    ├── util/UIFactory          → Komponen reusable + animasi
    └── view/                   → Semua screen (Bahasa Indonesia penuh)
```

**Design Patterns yang digunakan:**
Observer, Factory, Strategy, Template Method, State Machine, Single Persistent Scene

---

## 🎨 Palette Nusantara Dark Gold

| Nama | Hex | Digunakan untuk |
|------|-----|-----------------|
| Background | `#0A0604` | Latar utama |
| Panel | `#1A1008` | Card, panel |
| Gold Primer | `#C8860A` | Border aktif, ikon |
| Gold Sekunder | `#FFB830` | Teks penting, title |
| Merah Api | `#CC3300` | Danger, combat |
| Jade | `#2D7A45` | Sukses, heal |
| Amethyst | `#7755BB` | Skill defense |
| Mythic Orange | `#FF6B00` | Item mythic |
| Teks Utama | `#EDE0C8` | Konten teks |

---

## 📝 Tim Pengembang

> Tugas Akhir — Pemrograman Berorientasi Objek  
> Semester 2 | 2026  
> Kelompok 7 — BismilahFIX

---

*Lihat [CHANGELOG.md](CHANGELOG.md) untuk riwayat versi lengkap.*
