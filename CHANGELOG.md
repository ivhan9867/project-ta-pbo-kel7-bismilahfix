# ARCLIGHT CITY — CHANGELOG

Format: 0.2 - 27/04/2026

Kategori: Added | Fixed | Changed | Known Issues

---

## [v0.2.0] - 2026-04-27

### Fixed

**[CRITICAL] AI Turn Loop Stacking (CombatView.java)**
- Sebelumnya `handleCombatEvent()` membuat `Timeline` baru setiap kali event masuk
  tanpa guard, sehingga saat AI punya banyak turn berturut-turut, Timeline bisa
  menumpuk dan menyebabkan turn diproses dua kali lipat atau lebih
- Solusi: tambah field `aiTurnPending` sebagai boolean guard — Timeline baru hanya
  dibuat jika tidak ada yang sedang berjalan
- `startCombatLoop()` sekarang pakai delay 300ms sebelum turn pertama agar UI
  sempat ter-render sebelum proses dimulai

**[CRITICAL] DungeonManager tidak di-reset antar run (GameEngine.java)**
- `startDungeonRun()` sebelumnya memanggil `startDungeon()` pada DungeonManager
  yang sama, sehingga state dari run sebelumnya bisa bocor ke run baru
- Solusi: buat `DungeonManager` baru setiap kali `startDungeonRun()` dipanggil
  dan re-wire semua listener

**[BUG] `refreshCurrentRoomInfo()` kosong (DungeonMapView.java)**
- Method ini sebelumnya tidak melakukan apa-apa — body kosong `{ }`
- Sekarang: rebuild `currentRoomPanel` dan replace di parent VBox secara langsung
  menggunakan index lookup

**[BUG] `wireEngineListeners()` tidak handle banyak event type (DungeonMapView.java)**
- `LOOT_FOUND`, `REST`, `ROOM_CLEARED`, `ROOM_ENTERED`, `FLOOR_ENTERED` semua
  jatuh ke `default` yang hanya memanggil `refreshCurrentRoomInfo()` kosong
- Sekarang setiap event type punya handler yang tepat

**[BUG] Loot Room tidak tampilkan item ke player (DungeonMapView.java)**
- Sebelumnya loot hanya di-emit sebagai event tapi tidak di-resolve ke actual items
- Sekarang `LOOT_FOUND` event memanggil `LootManager.generateLoot()` dan langsung
  menambahkan item ke inventory player
- Popup konfirmasi tampil dengan daftar item yang didapat beserta rarity-nya

**[BUG] InventoryView — tombol EQUIP tidak ada (ViewsBundle.java)**
- Item equipment di inventory tidak bisa di-equip dari UI, hanya bisa di-upgrade
  dan dikalibrasi
- Sekarang ada tombol EQUIP di setiap equipment row
- Setelah equip/upgrade/kalibrasi, list di-refresh otomatis

**[BUG] Upgrade/Calibrate button tidak disabled saat kondisi tidak memenuhi (ViewsBundle.java)**
- Tombol upgrade masih aktif meski item sudah max level
- Tombol kalibrasi masih aktif meski tidak punya Calibration Kit
- Sekarang kedua tombol disable + style grey saat tidak bisa digunakan

### Changed

**Inventory item row — lebih informatif (ViewsBundle.java)**
- Sebelum: hanya tampil nama dan deskripsi generic
- Sekarang: tampil ringkasan 3 stat utama dengan format yang proper
  (persentase untuk stat seperti Crit Chance, flat untuk ATK/DEF)
- Consumable tampil stack count dan tombol USE langsung dari inventory
- Material tampil quantity

**DungeonMapView — instance fields untuk refresh (DungeonMapView.java)**
- Tambah `mapGridScrollPane` dan `currentRoomPanel` sebagai instance field
  agar bisa di-refresh tanpa rebuild seluruh screen
- Tambah `refreshMapGrid()` dan `refreshNextRoomsPanel()` sebagai method terpisah

### Added

**Loot Popup — feedback visual saat dapat item (DungeonMapView.java)**
- Saat masuk LOOT room, popup muncul menampilkan daftar item yang didapat
  lengkap dengan rarity tag `[Common]`, `[Rare]`, dll
- Item langsung masuk inventory player

**Rest Room — notifikasi popup (DungeonMapView.java)**
- Saat masuk REST room, popup konfirmasi muncul sebelum lanjut
- Menampilkan pesan berapa HP dan MP yang dipulihkan

**`showInfoAlert()` helper (DungeonMapView.java)**
- Helper method baru untuk tampilkan Alert dengan style cyberpunk yang konsisten
- Digunakan oleh loot popup dan rest notification

### Known Issues

- Skill selection masih auto-pilih skill pertama yang ready, belum ada UI
  untuk memilih skill mana yang ingin digunakan
- Target selection masih auto-target enemy pertama yang hidup
- Shop screen masih placeholder "COMING SOON"
- Tidak ada animasi saat damage / healing di combat
- Boss phase transition tidak ada notifikasi visual khusus
- Save/Load system belum ada

---

## [v0.1.0] - 2026-04-26

### Release Awal

Game bisa dijalankan end-to-end:
- Main Menu → Create Character → Hub
- Enter Dungeon → Navigate rooms
- Combat (player vs enemy) → Victory/Defeat
- Mercenary screen
- Inventory screen (basic)
- Profile/Stats screen

Migrasi dari NetBeans Ant ke JavaFX Maven (lihat `CHANGELOG_FIXES.md`
untuk detail lengkap 16 bug yang diperbaiki selama migrasi).

**Cara run:**
```bash
cd ArclightCity
mvn javafx:run
```
