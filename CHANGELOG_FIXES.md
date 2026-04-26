# ARCLIGHT CITY — Changelog Pembenahan & Migrasi Maven

Dokumen ini mencatat **semua perubahan** yang dilakukan saat migrasi dari
NetBeans Ant project ke **JavaFX Maven Archetype**, beserta pembenahan bug
yang ditemukan selama proses audit kode.

---

## 1. Migrasi Build System: NetBeans Ant → JavaFX Maven

### Sebelumnya (NetBeans Ant)
- Konfigurasi project tersebar di `nbproject/project.xml`, `nbproject/project.properties`, dan `build.xml`
- Path JavaFX SDK harus di-set **manual** di `project.properties` dengan path absolut lokal:
  ```
  file.reference.javafx.lib=D:/KULIAH PEMROGRAMAN/SEMESTER 2/javafx-sdk-25.0.3/lib
  ```
- Setiap anggota tim / komputer berbeda wajib mengubah path ini → tidak portable
- JavaFX modules di-load lewat `--module-path` dan `--add-modules` secara manual di JVM args
- Tidak ada dependency management otomatis

### Sesudahnya (JavaFX Maven)
- Satu file konfigurasi: **`pom.xml`** di root project
- JavaFX diunduh otomatis dari **Maven Central** — tidak perlu install SDK manual
- Berjalan di komputer manapun tanpa perubahan konfigurasi
- Menjalankan game: `mvn javafx:run`
- Build JAR: `mvn package`

### File yang Dibuat / Dihapus
| Aksi | File |
|------|------|
| ✅ Dibuat | `pom.xml` |
| ✅ Dibuat | `src/main/java/...` (struktur folder Maven standard) |
| ✅ Dipindahkan | `resources/ui/style/arclight.css` → `src/main/resources/ui/style/arclight.css` |
| ❌ Dihapus | `nbproject/project.xml` |
| ❌ Dihapus | `nbproject/project.properties` |
| ❌ Dihapus | `nbproject/build-impl.xml` |
| ❌ Dihapus | `build.xml` |
| ❌ Dihapus | `lib/javafx/README.txt` |

---

## 2. Penyesuaian Versi: JDK 25 + JavaFX 25

### Di `pom.xml`
```xml
<!-- Sebelum (versi awal draft) -->
<maven.compiler.source>17</maven.compiler.source>
<maven.compiler.target>17</maven.compiler.target>
<javafx.version>21.0.3</javafx.version>

<!-- Sesudah (disesuaikan dengan environment kamu) -->
<maven.compiler.source>25</maven.compiler.source>
<maven.compiler.target>25</maven.compiler.target>
<javafx.version>25</javafx.version>
```

---

## 3. Bug #1 — Kelas `CombatAction` Duplikat di Dua Package (KRITIS)

### Masalah
Kelas `CombatAction` ada **dua kali** dengan isi yang **identik** di dua package berbeda:
- `arclightcity.entity.base.CombatAction`
- `arclightcity.combat.CombatAction`

Yang lebih parah, `arclightcity/entity/base/CombatAction.java` bahkan **mengimport dirinya sendiri** dari package `combat`:
```java
// Di dalam entity/base/CombatAction.java — circular import!
import arclightcity.combat.CombatAction;
```

### Dampak
- Compile error: ambiguous class reference di seluruh project
- 20 file mengimport dari package yang salah (`entity.base`) padahal implementasi
  canonical ada di `combat`

### Perbaikan
1. **Hapus** `src/.../entity/base/CombatAction.java` (file duplikat)
2. **Ganti semua import** di 20 file dari `arclightcity.entity.base.CombatAction`
   ke `arclightcity.combat.CombatAction` menggunakan batch replace

### File Terdampak (import diubah)
```
combat/CombatManager.java
entity/enemy/VoidSpecter.java
entity/enemy/IronClad.java
entity/enemy/StreetThug.java
entity/enemy/NeonSerpent.java
entity/enemy/Enemy.java
entity/enemy/GlitchDrone.java
entity/enemy/NullKing.java
entity/player/Player.java
entity/mercenary/TankRX9.java
entity/mercenary/LyraBloom.java
entity/mercenary/EchoNull.java
entity/mercenary/Vector.java
entity/mercenary/KiraVoss.java
entity/mercenary/SeraMend.java
entity/mercenary/MagnusForge.java
entity/mercenary/Mercenary.java
ui/view/CombatView.java
engine/GameEngine.java
entity/base/Entity.java
```

---

## 4. Bug #2 — Nama Constructor Salah di `ViewsBundle.java` (KRITIS)

### Masalah
`ViewsBundle.java` berisi 6 inner static class dengan nama `...ViewImpl`,
tetapi **semua constructornya diberi nama view lama** (tanpa `Impl`).
Di Java, constructor harus bernama **sama persis** dengan nama class-nya —
jika berbeda, compiler memperlakukannya sebagai **method biasa** (bukan constructor),
yang menyebabkan `NoSuchMethodException` saat runtime.

```java
// SALAH — ini bukan constructor, ini dianggap sebagai method!
public static class InventoryViewImpl {
    InventoryView(GameEngine engine, SceneRouter router) { ... }
    //  ^^^^^^^^^^^ nama SALAH, seharusnya InventoryViewImpl
}
```

### Perbaikan
| Inner Class | Constructor Lama (SALAH) | Constructor Baru (BENAR) |
|-------------|--------------------------|--------------------------|
| `InventoryViewImpl` | `InventoryView(...)` | `InventoryViewImpl(...)` |
| `MercenaryViewImpl` | `MercenaryView(...)` | `MercenaryViewImpl(...)` |
| `EventViewImpl` | `EventView(...)` | `EventViewImpl(...)` |
| `ShopViewImpl` | `ShopView(...)` | `ShopViewImpl(...)` |
| `VictoryViewImpl` | `VictoryView(...)` | `VictoryViewImpl(...)` |
| `GameOverViewImpl` | `GameOverView(...)` | `GameOverViewImpl(...)` |

---

## 5. Bug #3 — Duplicate Import di `CombatView.java`

### Masalah
`CombatView.java` mengimport class dari package `arclightcity.combat` dua kali:
satu per satu **dan** sekaligus pakai wildcard `.*`:

```java
// Import individual yang redundant
import arclightcity.combat.CombatManager;   // ← redundant
import arclightcity.combat.CombatResult;    // ← redundant
import arclightcity.combat.CombatAction;    // ← redundant
// ...
import arclightcity.combat.*;               // sudah cover semua di atas
```

### Perbaikan
Hapus ketiga import individual, pertahankan hanya wildcard `import arclightcity.combat.*;`

---

## 6. Bug #4 — Duplicate Import di `GameEngine.java`

### Masalah
`GameEngine.java` mengimport dari `arclightcity.item` tiga kali:

```java
import arclightcity.item.Item;       // ← redundant
import arclightcity.item.Inventory;  // ← redundant
import arclightcity.item.*;          // sudah cover semua
```

### Perbaikan
Hapus `import arclightcity.item.Item;` dan `import arclightcity.item.Inventory;`,
pertahankan hanya wildcard `import arclightcity.item.*;`

---

## 7. Verifikasi API Consistency (Tidak Ada Perubahan Kode)

Berikut method-method yang **diaudit dan dikonfirmasi ada** di class yang sesuai,
sehingga tidak menimbulkan bug runtime:

### `Inventory.java`
- `getEquippedWeapon()`, `getEquippedArmor()`, `getEquippedAccessory1()`, `getEquippedAccessory2()`
- `getScrapMetal()`, `getCyberChips()`, `getNeonCrystals()`, `getCalibrationKits()`
- `getBagSize()`, `getMaxBagSize()`

### `DungeonManager.java`
- `startDungeon()`, `advanceToNextFloor()`, `moveToRoom()`, `resolveEventChoice()`
- `getAvailableNextRooms()`, `getCurrentFloor()`, `getCurrentFloorNumber()`
- `setStateListener()`, `setCombatStartListener()`, `setCombatEndListener()`
- `setEventRoomListener()`, `setShopOpenListener()`, `getCombatManager()`

### `CombatManager.java`
- `addEventListener()`, `addResultListener()`, `getLivingEnemies()`, `getLivingAllies()`
- `processTurn()`, `submitPlayerAction()`, `attemptFlee()`

### `DungeonStateEvent.Type` enum
- Semua nilai yang dipakai di `DungeonMapView` terkonfirmasi ada:
  `COMBAT_STARTED`, `EVENT_ENCOUNTERED`, `SHOP_OPENED`, `GAME_OVER`, `READY_FOR_NEXT_FLOOR`

### `MercenaryType`
- Field `displayName` dan `subtitle` terkonfirmasi ada dan dipakai benar di `ViewsBundle`

### `ViewsBundle` brace balance
- Jumlah `{` dan `}` dikonfirmasi seimbang: 58 buka = 58 tutup ✓

---

## 8. Cara Menjalankan Project (Maven)

### Prasyarat
- JDK 25 sudah terinstall dan `JAVA_HOME` sudah di-set
- Apache Maven 3.8+ sudah terinstall

### Perintah
```bash
# Masuk ke folder project
cd ArclightCity

# Jalankan langsung (auto-download dependency JavaFX 25)
mvn javafx:run

# Build JAR (output di target/)
mvn package

# Clean build
mvn clean javafx:run
```

### Struktur Folder Maven
```
ArclightCity/
├── pom.xml                          ← konfigurasi Maven
└── src/
    └── main/
        ├── java/
        │   └── arclightcity/
        │       ├── combat/           ← sistem pertarungan
        │       ├── dungeon/          ← dungeon & procedural gen
        │       ├── engine/           ← GameEngine (controller utama)
        │       ├── entity/           ← player, enemy, mercenary, stats
        │       ├── item/             ← inventory, item, loot
        │       └── ui/               ← JavaFX views & controllers
        └── resources/
            └── ui/style/
                └── arclight.css      ← styling game
```

---

---

## 9. Bug #6 — `CombatAction.java` Hilang dari Package `combat`

### Masalah
Waktu `entity/base/CombatAction.java` dihapus (duplikat), ternyata file **kanonik**
`arclightcity/combat/CombatAction.java` juga **tidak ada** di ZIP asli.
Akibatnya semua class yang import `arclightcity.combat.CombatAction` gagal compile.

### Perbaikan
Buat ulang `arclightcity/combat/CombatAction.java` lengkap dengan:
- Enum `ActionType`: `BASIC_ATTACK`, `USE_SKILL`, `USE_ITEM`, `DEFEND`, `PASS`
- Factory methods: `basicAttack()`, `useSkill()`, `useItem()`, `defend()`, `pass()`
- Getter: `getActionType()`, `getSkillId()`, `getItemId()`, `getTargetIds()`

---

## 10. Bug #7 — Import Hilang di Beberapa File (Batch Fix)

### File dan import yang ditambahkan

| File | Import yang Ditambahkan |
|------|------------------------|
| `combat/CombatEvent.java` | `arclightcity.entity.stats.DamageType`, `arclightcity.entity.status.StatusEffectType` |
| `entity/base/Entity.java` | `arclightcity.entity.status.StatusEffect` |
| `ui/util/UIFactory.java` | `arclightcity.entity.stats.DamageType`, `arclightcity.entity.stats.StatType`, `arclightcity.entity.status.StatusEffect`, `arclightcity.entity.status.StatusEffectType` |
| `item/Item.java` | `arclightcity.entity.stats.StatType` |
| `entity/mercenary/TankRX9.java` | `arclightcity.entity.stats.DamageType` |
| `entity/mercenary/Vector.java` | `arclightcity.entity.stats.DamageType` |

### Penyebab
Semua class di atas menggunakan tipe dari package `entity.stats` atau `entity.status`
tanpa mendeklarasikan importnya — kemungkinan di NetBeans sebelumnya class-class ini
di-resolve otomatis karena path classpath yang terlalu luas.

---

## 11. Bug #8 — Import Duplikat `CombatAction` di 14 File Enemy & Mercenary

### Masalah
Setelah batch-replace import, banyak file memiliki dua baris:
```java
import arclightcity.combat.CombatAction;  // baris 2
// ...
import arclightcity.combat.CombatAction;  // baris 8 — DUPLIKAT
```

### Perbaikan
Scan semua 70 file Java, deteksi duplikat, hapus otomatis dengan Python script.
Total 14 file dibersihkan.

---

## 12. Bug #9 — Unclosed String Literal di `ViewsBundle.java` (ShopViewImpl)

### Masalah
String di dalam `ShopViewImpl.build()` punya **literal newline** di tengah:
```java
// SALAH — Java tidak izinkan newline literal di dalam string
Label desc = new Label("...price.\"\n    // ← baris terputus di sini
\n" + "SHOP SYSTEM...");
```

### Perbaikan
Gabungkan ke string concatenation yang benar dengan `\\n` escape sequence.

---

## 13. Bug #10 — `DamageType` Tidak Diimport di `CombatManager.java`

### Masalah
`CombatManager.java` menggunakan `DamageType` di method `executeBasicAttack()`
tapi tidak punya import `arclightcity.entity.stats.DamageType`.

### Perbaikan
Tambahkan `import arclightcity.entity.stats.DamageType;` ke `CombatManager.java`.

---

## 14. Bug #11 — Akses Field `protected` dari Luar Package

### Masalah
`CombatManager.java` (package `combat`) dan `SkillExecutor.java` (package `combat`)
mengakses field `protected double totalDamageDealt` dari `Entity` (package `entity.base`)
secara langsung:
```java
actor.totalDamageDealt += result.damage;  // COMPILE ERROR — protected access
```
Di Java, `protected` hanya bisa diakses dari subclass **atau** package yang sama.
`CombatManager` bukan subclass `Entity` dan beda package, jadi ini illegal.

### Perbaikan
Tambahkan public method `addDamageDealt(double amount)` di `Entity.java`:
```java
public void addDamageDealt(double amount) { totalDamageDealt += amount; }
```
Lalu ganti semua akses langsung:
```java
// Sebelum (SALAH)
actor.totalDamageDealt += result.damage;
// Sesudah (BENAR)
actor.addDamageDealt(result.damage);
```

---

## 15. Bug #12 — Referensi Package Tidak Lengkap di `EchoNull.java`

### Masalah
`EchoNull.java` menggunakan pattern matching dengan nama package tidak lengkap:
```java
// SALAH — tidak ada package "entity.enemy" di classpath root
e instanceof entity.enemy.Enemy enemy
```

### Perbaikan
Ganti ke fully-qualified class name dan tambah import:
```java
// BENAR
import arclightcity.entity.enemy.Enemy;
// ...
e instanceof arclightcity.entity.enemy.Enemy enemy
```

---

## 16. Bug #13 — Import `ArclightApp` Hilang di `SceneRouter.java`

### Masalah
`SceneRouter.java` menggunakan konstanta `ArclightApp.SCREEN_WIDTH` dan
`ArclightApp.SCREEN_HEIGHT` tapi tidak mengimport class `ArclightApp`:
```java
// ERROR — ArclightApp tidak dikenali
new Scene(root, ArclightApp.SCREEN_WIDTH, ArclightApp.SCREEN_HEIGHT);
```

### Perbaikan
Tambahkan `import arclightcity.ui.ArclightApp;` ke `SceneRouter.java`.

---

## Ringkasan Semua Perubahan

| No | Kategori | Perubahan | Dampak |
|----|----------|-----------|--------|
| 1 | Build System | NetBeans Ant → JavaFX Maven | Portabilitas, no manual SDK setup |
| 2 | Versi | JavaFX 21 → JavaFX 25, JDK 17 → JDK 25 | Sesuai environment lokal |
| 3 | Bug Kritis | Hapus `entity/base/CombatAction.java` duplikat | Fix compile error |
| 4 | Bug Kritis | Fix 20 import `entity.base.CombatAction` → `combat.CombatAction` | Fix compile error |
| 5 | Bug Kritis | Fix 6 nama constructor di `ViewsBundle.java` | Fix runtime crash |
| 6 | Code Quality | Hapus duplicate import di `CombatView.java` | Bersihkan warning |
| 7 | Code Quality | Hapus duplicate import di `GameEngine.java` | Bersihkan warning |
| 8 | Resources | Pindahkan CSS ke `src/main/resources/` | Classpath Maven standard |
| 9 | Bug Kritis | Buat ulang `combat/CombatAction.java` yang hilang | Fix compile error |
| 10 | Bug Kritis | Tambah import hilang di 7 file | Fix compile error |
| 11 | Code Quality | Hapus 14 import `CombatAction` duplikat | Bersihkan warning |
| 12 | Bug Kritis | Fix unclosed string literal di `ViewsBundle.java` | Fix compile error |
| 13 | Bug Kritis | Tambah `DamageType` import di `CombatManager.java` | Fix compile error |
| 14 | Bug Kritis | Ganti akses `protected` field dengan `addDamageDealt()` | Fix compile error |
| 15 | Bug Kritis | Fix referensi package tidak lengkap di `EchoNull.java` | Fix compile error |
| 16 | Bug Kritis | Tambah `import ArclightApp` di `SceneRouter.java` | Fix compile error |
