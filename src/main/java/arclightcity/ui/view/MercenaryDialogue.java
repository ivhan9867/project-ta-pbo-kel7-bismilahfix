package arclightcity.ui.view;

import arclightcity.entity.mercenary.MercenaryType;

import java.util.*;

/**
 * MercenaryDialogue — database dialog per mercenary dan trigger.
 *
 * Setiap merc punya kepribadian unik yang tercermin dalam dialog:
 *   KIRA_VOSS    — dingin, profesional, sedikit kata
 *   TANK_RX9     — formal, android, logis
 *   SERA_MEND    — hangat, caring, medis
 *   VECTOR       — sarkastis, arogan, overconfident
 *   MAGNUS_FORGE — kasar, blak-blakan, antusias
 *   ECHO_NULL    — misterius, teknis, cryptic
 *   LYRA_BLOOM   — positif, spiritual, poetic
 */
public class MercenaryDialogue {

    public enum Trigger {
        // Hub
        HUB_IDLE,
        HUB_ENTER_DUNGEON,

        // Dungeon
        DUNGEON_ENTER_FLOOR,
        DUNGEON_ENTER_ENEMY,
        DUNGEON_ENTER_BOSS,
        DUNGEON_ENTER_LOOT,
        DUNGEON_ENTER_REST,
        DUNGEON_ENTER_SHOP,
        DUNGEON_ENTER_TRAP,
        DUNGEON_BOSS_CLEARED,

        // Combat
        COMBAT_START,
        COMBAT_PLAYER_LOW_HP,
        COMBAT_MERC_ACTION,
        COMBAT_ENEMY_DIES,
        COMBAT_VICTORY,
        COMBAT_DEFEAT,
        COMBAT_BOSS_START,
    }

    private static final Random RNG = new Random();

    // ── Dialog Database ───────────────────────────────────────

    private static final Map<MercenaryType, Map<Trigger, List<String>>> DIALOGUES = new HashMap<>();

    static {
        // ── SRIKANDI — Pemanah Bayangan (dingin, presisi, sedikit kata) ──
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.HUB_IDLE,
            "...",
            "Waspada.",
            "Diam adalah senjata terbaik.",
            "Aku sudah pantau perimeter. Aman, untuk sekarang.",
            "Setiap detik tenang adalah persiapan untuk badai berikutnya."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.HUB_ENTER_DUNGEON,
            "Akhirnya.",
            "Bergerak.",
            "Tetap di belakangku.",
            "Target dikonfirmasi."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.DUNGEON_ENTER_ENEMY,
            "Kontak.",
            "Target terdeteksi.",
            "Musuh mendekat.",
            "Aku ambil posisi depan."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.DUNGEON_ENTER_BOSS,
            "Target prioritas. Jangan halangi bidikanku.",
            "Ini berbeda dari yang lain. Fokus.",
            "Aku pernah lihat apa yang makhluk ini bisa lakukan. Jangan lengah."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.DUNGEON_ENTER_LOOT,
            "Cek jebakan dulu.",
            "Jangan sentuh apapun sebelum aku bersihkan.",
            "Ambil cepat. Kita terlalu terbuka di sini."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.DUNGEON_ENTER_REST,
            "Dua menit. Tidak lebih.",
            "Istirahat. Aku jaga.",
            "Jangan terlalu nyaman."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.COMBAT_START,
            "Mulai.",
            "Menyerang.",
            "Ikuti tandaku.",
            "Panah sudah siap."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.COMBAT_PLAYER_LOW_HP,
            "Kau berdarah. Mundur.",
            "Tetap hidup. Aku tidak bisa membawa mayat.",
            "Jangan mati dulu. Kita belum selesai."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.COMBAT_ENEMY_DIES,
            "Target jatuh.",
            "Bersih.",
            "Berikutnya.",
            "Tepat sasaran."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.COMBAT_VICTORY,
            "Area bersih.",
            "Efisien.",
            "Begitulah caranya."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.COMBAT_DEFEAT,
            "Mundur. Sekarang.",
            "Kita perlu recalibrate.",
            "Kita meremehkan mereka."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.COMBAT_BOSS_START,
            "Tembakan penuh. Maju.",
            "Jangan beri dia kesempatan bernapas.",
            "Target prioritas. Semua yang kita punya."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.DUNGEON_BOSS_CLEARED,
            "Boss jatuh. Lanjut.",
            "Satu ancaman teratasi.",
            "Hmm. Lebih mudah dari yang kubayangkan."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.DUNGEON_ENTER_FLOOR,
            "Lantai baru. Tetap waspada.",
            "Lebih dalam, lebih berbahaya.",
            "Kita terus."
        );

        // ── GATOT KACA — Ksatria Baja (formal, logis, android) ──────────
        addDialogues(MercenaryType.TANK_RX9, Trigger.HUB_IDLE,
            "Sistem normal. Siap siaga.",
            "Tidak ada ancaman terdeteksi.",
            "Sel energi 98%. Siap deploy.",
            "Protokol pemeliharaan selesai.",
            "Menghitung parameter tempur optimal."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.HUB_ENTER_DUNGEON,
            "Mengaktifkan subrutin tempur.",
            "Maju ke zona operasi.",
            "Perlindungan maksimum diaktifkan.",
            "Memimpin barisan."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.DUNGEON_ENTER_ENEMY,
            "Entitas musuh terdeteksi. Mengambil posisi defensif.",
            "Musuh di depan. Saya lindungi.",
            "Ancaman dikonfirmasi. Siap perang.",
            "Formasi tempur aktif."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.DUNGEON_ENTER_BOSS,
            "Ancaman level tinggi. Memaksimalkan output pertahanan.",
            "Entitas ini melebihi parameter normal. Berhati-hati.",
            "Boss terdeteksi. Mengalokasikan semua sumber daya untuk perlindungan."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.DUNGEON_ENTER_LOOT,
            "Area aman. Lanjutkan pengumpulan.",
            "Tidak ada ancaman tersembunyi terdeteksi.",
            "Saya jaga perimeter."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.DUNGEON_ENTER_REST,
            "Fase pemulihan dimulai. Efisiensi direkomendasikan.",
            "Memanfaatkan waktu untuk defrag internal.",
            "Istirahat produktif. 4 menit 37 detik optimal."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.COMBAT_START,
            "Mode tempur aktif.",
            "Mengeksekusi protokol penyerangan.",
            "Memulai sekuens pertempuran.",
            "Tameng diperkuat."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.COMBAT_PLAYER_LOW_HP,
            "HP kritis. Beralih ke mode perlindungan penuh.",
            "Kesehatan operator menurun. Mengambil alih garis terdepan.",
            "Perhatian: nyawa operator dalam bahaya."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.COMBAT_ENEMY_DIES,
            "Target dinetralkan.",
            "Ancaman tereliminasi.",
            "Satu musuh kurang.",
            "Proses eliminasi berhasil."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.COMBAT_VICTORY,
            "Semua ancaman tereliminasi. Misi sukses.",
            "Operasi selesai dengan kerusakan minimal.",
            "Efisiensi tempur: baik."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.COMBAT_DEFEAT,
            "Mundur strategis diinisiasi.",
            "Analisis kegagalan: musuh melebihi kalkulasi.",
            "Recalibrate dan coba lagi."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.COMBAT_BOSS_START,
            "Boss terdeteksi. Output pertahanan: MAKSIMUM.",
            "Entitas prioritas tinggi. Semua sistem tempur aktif.",
            "Ini akan menjadi pertempuran yang menguji batas sistem kami."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.DUNGEON_BOSS_CLEARED,
            "Boss dinetralkan. Database diperbarui.",
            "Ancaman level boss teratasi. Lanjutkan misi.",
            "Kalkulasi berhasil. Maju."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.DUNGEON_ENTER_FLOOR,
            "Memasuki lantai baru. Memperbarui peta ancaman.",
            "Sinkronisasi data lantai selesai.",
            "Siap menghadapi tantangan berikutnya."
        );

        // ── NYAI RORO — Tabib Mistis (hangat, peduli, spiritual) ────────
        addDialogues(MercenaryType.SERA_MEND, Trigger.HUB_IDLE,
            "Bagaimana kondisimu? Terlihat lelah.",
            "Aku siapkan ramuan untuk perjalanan selanjutnya.",
            "Roh-roh di sini... gelisah. Hati-hati.",
            "Napas dalam. Kita punya waktu.",
            "Kurasakan aura kuat dari bawah. Kita harus waspada."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.HUB_ENTER_DUNGEON,
            "Aku ikut. Kau butuh penyembuh.",
            "Bawa jamu-jamanku. Mungkin kita perlu itu.",
            "Para leluhur membimbing langkah kita.",
            "Kita pergi bersama."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.DUNGEON_ENTER_ENEMY,
            "Ada yang mendekat. Bersiaplah.",
            "Energi gelap... mereka sudah di sini.",
            "Tetap tenang. Aku di sini.",
            "Lindungi dirimu. Aku siapkan mantra."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.DUNGEON_ENTER_BOSS,
            "Ini... bukan makhluk biasa. Kekuatannya luar biasa.",
            "Aku rasakan kekuatan kuno yang besar. Berhati-hatilah.",
            "Butuh semua kekuatan kita untuk ini. Bersama."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.DUNGEON_ENTER_LOOT,
            "Hati-hati, ada energi asing di sini.",
            "Biar aku purifikasi dulu sebelum kita ambil.",
            "Kabar baik! Tidak ada kutukan pada loot ini."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.DUNGEON_ENTER_REST,
            "Bagus. Istirahat. Biar aku periksa lukamu.",
            "Aku seduh jamu dari tanaman yang kutemukan.",
            "Gunakan waktu ini untuk memulihkan jiwa dan raga."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.COMBAT_START,
            "Aku siapkan mantra perlindungan!",
            "Bersama kita kuat.",
            "Jangan khawatir, aku ada di sini.",
            "Semangat, kita bisa!"
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.COMBAT_PLAYER_LOW_HP,
            "Kau terluka! Aku sembuhkan!",
            "Bertahan! Jamu ini akan membantumu!",
            "Jangan menyerah! Aku di sini bersamamu!"
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.COMBAT_ENEMY_DIES,
            "Semoga rohnya tenang.",
            "Terselesaikan dengan damai.",
            "Kita tidak punya pilihan lain.",
            "Satu ancaman berlalu."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.COMBAT_VICTORY,
            "Syukurlah! Kita baik-baik saja.",
            "Biar aku periksa semua luka.",
            "Pertarungan berat, tapi kita menang bersama."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.COMBAT_DEFEAT,
            "Tidak apa-apa. Mundur dulu. Aku rawat lukamu.",
            "Kadang mundur adalah pilihan terbaik.",
            "Kita akan lebih kuat setelah ini."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.COMBAT_BOSS_START,
            "Yang Maha Besar, lindungi kami!",
            "Ini saatnya kita buktikan kekuatan sejati kita!",
            "Aku kerahkan semua kemampuan penyembuhan!"
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.DUNGEON_BOSS_CLEARED,
            "Luar biasa! Kita berhasil!",
            "Boss jatuh! Aku bangga dengan kita semua.",
            "Istirahat sebentar, biar aku rawat luka-luka kita."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.DUNGEON_ENTER_FLOOR,
            "Lantai baru. Energinya berbeda di sini.",
            "Aku rasakan sesuatu yang kuno di lantai ini.",
            "Tetap bersama. Kita hadapi ini bersama."
        );

        // ── RANGGA — Pembunuh Bayaran (sarkastis, dingin, humor gelap) ──
        addDialogues(MercenaryType.VECTOR, Trigger.HUB_IDLE,
            "Membosankan.",
            "Berapa lama lagi kita diam di sini?",
            "Aku lebih suka di lapangan.",
            "Kalau ada yang perlu dibunuh, bilang aja.",
            "Bayaranku jalan terus meski kau tidak melakukan apa-apa."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.HUB_ENTER_DUNGEON,
            "Nah, akhirnya ada kegiatan.",
            "Bayaran ekstra untuk hari ini.",
            "Sudah siap? Aku sudah dari tadi.",
            "Makin dalam makin seru."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.DUNGEON_ENTER_ENEMY,
            "Mangsa terdeteksi.",
            "Oh, selamat datang. Aku sudah tunggu kalian.",
            "Lumayan. Buat pemanasan.",
            "Ini mudah. Hampir terlalu mudah."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.DUNGEON_ENTER_BOSS,
            "Ini yang menarik. Akhirnya lawan yang layak.",
            "Besar ya. Biasanya yang besar lebih lambat.",
            "Bayaran ekstra untuk ini. Kita deal?"
        );
        addDialogues(MercenaryType.VECTOR, Trigger.DUNGEON_ENTER_LOOT,
            "Cepat ambil. Siapa tahu ada yang mengawasi.",
            "Loot terbaik biasanya sudah diambil orang lain. Semoga tidak.",
            "Aku jaga pintu. Ambil yang berharga."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.DUNGEON_ENTER_REST,
            "Lima menit. Tidak lebih.",
            "Aku tidak tidur di dungeon. Terlalu berbahaya.",
            "Istirahat cepat, lanjut lagi."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.COMBAT_START,
            "Ini gilaku.",
            "Aku percepat prosesnya.",
            "Jangan halangi bidikanku.",
            "Habisi mereka."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.COMBAT_PLAYER_LOW_HP,
            "Eh, jangan mati dulu. Aku belum dibayar penuh.",
            "Kau terlihat mengerikan. Lebih buruk dari biasanya.",
            "Minum sesuatu! Apa gunanya bawa tabib kalau tidak dipakai?"
        );
        addDialogues(MercenaryType.VECTOR, Trigger.COMBAT_ENEMY_DIES,
            "Mudah.",
            "Berikutnya.",
            "Terlalu lambat.",
            "Sesuai ekspektasi."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.COMBAT_VICTORY,
            "Seperti yang kuduga.",
            "Terlalu mudah.",
            "Mana lagi?"
        );
        addDialogues(MercenaryType.VECTOR, Trigger.COMBAT_DEFEAT,
            "Hari yang buruk. Langka, tapi terjadi.",
            "Kita mundur. Bukan kalah, strategi.",
            "Ini tidak dihitung dalam rekam jejakku."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.COMBAT_BOSS_START,
            "Nah, ini baru namanya tantangan.",
            "Kalau kita menang, bonus besar ya?",
            "Habisi dulu, nego belakangan."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.DUNGEON_BOSS_CLEARED,
            "Itu baru kerja nyata.",
            "Bagus juga. Tidak seburuk yang kukira.",
            "Boss jatuh. Transfer bayaranku."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.DUNGEON_ENTER_FLOOR,
            "Lantai baru, ancaman baru. Siap.",
            "Makin dalam, makin bagus.",
            "Aku suka adrenalinnya."
        );

        // ── BIMA — Petarung Agung (antusias, keras, langsung) ───────────
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.HUB_IDLE,
            "KAPAN KITA BERANGKAT?!",
            "Ototku sudah gatal pengen beraksi!",
            "Musuh mana yang mau kuremukkan hari ini?",
            "Bima siap! Selalu siap!",
            "Latihan fisik selesai. Siap perang!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.HUB_ENTER_DUNGEON,
            "AKHIRNYA! MAJU!",
            "Bima di depan! Ikuti!",
            "Siapkan otot, saatnya beraksi!",
            "Dungeon tidak akan tahu apa yang menghantam mereka!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.DUNGEON_ENTER_ENEMY,
            "MUSUH! BAGUS!",
            "Ayo, tunjukkan kekuatanmu!",
            "Bima tidak sabar!",
            "Ini hari yang baik untuk bertarung!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.DUNGEON_ENTER_BOSS,
            "BOSS! INI YANG BIMA TUNGGU!",
            "Semakin besar, semakin SERU!",
            "Bima akan remukkan si besar ini!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.DUNGEON_ENTER_LOOT,
            "Ada harta! Bagikan rata!",
            "Hasil kerja keras kita! Ambil semua!",
            "Bima suka loot! Terutama yang berat!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.DUNGEON_ENTER_REST,
            "Istirahat? Oke, tapi sebentar saja!",
            "Bima masih kuat! Tapi oke lah, lima menit.",
            "Pakai waktu ini untuk makan! Bima lapar!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.COMBAT_START,
            "SERANG!",
            "HAJAR!",
            "BIMA MAJU!",
            "TIDAK ADA YANG LOLOS!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.COMBAT_PLAYER_LOW_HP,
            "HEY! Kau baik-baik saja?! BIMA LINDUNGI!",
            "Mundur! Bima hadapi mereka!",
            "Jangan mati! Bima butuh teman yang kuat!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.COMBAT_ENEMY_DIES,
            "YA! JATUH!",
            "BIMA KUAT!",
            "HAJAR LAGI!",
            "SATU LAGI!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.COMBAT_VICTORY,
            "MENANG! BIMA MENANG!",
            "TIDAK ADA YANG BISA HENTIKAN BIMA!",
            "Pertarungan bagus! Bima puas!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.COMBAT_DEFEAT,
            "Tidak... Bima tidak terima!",
            "Mundur dulu! Bima masih bisa!",
            "Kita akan balas ini!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.COMBAT_BOSS_START,
            "BOSS BESAR! INI TANTANGAN SESUNGGUHNYA!",
            "BIMA TIDAK TAKUT! MAJU!",
            "KALI INI BIMA KERAHKAN SEMUA KEKUATAN!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.DUNGEON_BOSS_CLEARED,
            "BOSS JATUH! BIMA MENANG!",
            "TIDAK ADA YANG BISA KALAHKAN BIMA DAN KAWAN-KAWAN!",
            "Pertarungan LUAR BIASA! Bima bangga!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.DUNGEON_ENTER_FLOOR,
            "LANTAI BARU! MUSUH BARU! BIMA SIAP!",
            "Makin dalam makin seru! Bima suka!",
            "Kita terus! Tidak ada yang bisa hentikan kita!"
        );

        // ── KI AGENG — Dukun Tua (misterius, bijak, prophetic) ──────────
        addDialogues(MercenaryType.ECHO_NULL, Trigger.HUB_IDLE,
            "Angin berbicara... ada yang datang.",
            "Diam itu bukan kekosongan. Diam adalah mendengarkan.",
            "Aku membaca pertanda. Perjalanan sulit menanti.",
            "Api lilin ini bergetar. Roh di sekitar kita gelisah.",
            "Mantraku sudah siap. Tinggal menunggu waktu yang tepat."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.HUB_ENTER_DUNGEON,
            "Bintang menunjuk ke dalam. Kita ikuti.",
            "Ilmu gaibku akan berguna di kedalaman itu.",
            "Hati-hati dengan apa yang ada di sana. Aku sudah lihat bayangannya.",
            "Kita pergi. Alam semesta menunggu."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.DUNGEON_ENTER_ENEMY,
            "Mereka datang. Aku sudah tahu.",
            "Aura gelap mendekat. Bersiaplah.",
            "Musuh ini... ada kutukan pada mereka.",
            "Tangan di atas. Mantra sudah siap."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.DUNGEON_ENTER_BOSS,
            "Ini yang aku lihat dalam meditasiku. Waktunya tiba.",
            "Kekuatan kuno yang luar biasa. Hormati, lalu taklukkan.",
            "Jangan terburu-buru. Entitas ini punya pola. Aku sudah pelajari."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.DUNGEON_ENTER_LOOT,
            "Harta ini menyimpan cerita panjang.",
            "Ada energi tersisa pada benda-benda ini. Hati-hati.",
            "Ambil dengan hormat. Setiap benda punya roh."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.DUNGEON_ENTER_REST,
            "Bagus. Waktu untuk meditasi singkat.",
            "Alam semesta memberi kita jeda. Manfaatkan.",
            "Dengarkan suara ruangan ini. Ada cerita di dalamnya."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.COMBAT_START,
            "Mantra perlindungan aktif.",
            "Aku siapkan ilmu penghalang.",
            "Kita akan menang. Aku sudah lihat.",
            "Ikuti naluri. Jangan pikir berlebihan."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.COMBAT_PLAYER_LOW_HP,
            "Nyawamu dalam bahaya. Fokus pada napas.",
            "Jangan panik. Panik adalah musuh terbesar.",
            "Aku kirim energi penyembuh padamu. Bertahanlah."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.COMBAT_ENEMY_DIES,
            "Takdirnya terpenuhi.",
            "Roh mereka kembali ke alam asalnya.",
            "Selesai.",
            "Seperti yang tertulis."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.COMBAT_VICTORY,
            "Seperti yang sudah aku lihat.",
            "Alam semesta berpihak pada kita hari ini.",
            "Kemenangan ini bukan kebetulan."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.COMBAT_DEFEAT,
            "Ini pelajaran. Bukan akhir.",
            "Bahkan naga pun pernah terluka. Bangkit lagi.",
            "Aku hitung ulang. Ada yang aku lewatkan."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.COMBAT_BOSS_START,
            "Ini makhluk yang ada dalam ramalanku. Bersiaplah.",
            "Kekuatannya luar biasa. Tapi kita punya sesuatu yang tidak dimilikinya.",
            "Mantra terkuatku untuk ini."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.DUNGEON_BOSS_CLEARED,
            "Takdir telah digenapi.",
            "Kekuatan kuno ini telah takluk. Lanjutkan.",
            "Ini yang aku lihat dalam mimpi. Kita berhasil."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.DUNGEON_ENTER_FLOOR,
            "Lantai baru. Roh di sini berbeda karakternya.",
            "Aku rasakan perubahan energi. Waspada.",
            "Setiap lantai menyimpan misteri. Kita baca bersama."
        );

        // ── DEWI SRI — Penjaga Keseimbangan (spiritual, puitis, tenang) ─
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.HUB_IDLE,
            "Keseimbangan... selalu ada keseimbangan.",
            "Bunga-bunga di atas tanah tidak tahu betapa dalamnya akar mereka.",
            "Aku merasakan getaran alam di sini. Sesuatu akan datang.",
            "Apakah kau sudah makan? Tubuh adalah rumah jiwa.",
            "Air mengalir ke bawah. Kita harus ikut arah alam."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.HUB_ENTER_DUNGEON,
            "Bumi di bawah kita penuh dengan cerita. Mari kita tulis satu lagi.",
            "Pergi bersama, pulang bersama.",
            "Alam semesta merestui perjalanan kita.",
            "Aku bawa berkat panen untuk kita semua."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.DUNGEON_ENTER_ENEMY,
            "Makhluk ini pun punya tempatnya... tapi bukan di sini.",
            "Keseimbangan terganggu. Kita kembalikan.",
            "Maaf, tapi kita harus melewatimu.",
            "Pertarungan ini perlu. Untuk keseimbangan."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.DUNGEON_ENTER_BOSS,
            "Kekuatan sebesar ini... pasti ada alasan mengapa ia ada di sini.",
            "Ini bukan sekadar pertarungan. Ini pemulihan keseimbangan.",
            "Alam mengirim kita untuk menyelesaikan ini. Percaya."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.DUNGEON_ENTER_LOOT,
            "Bumi memberikan hadiahnya. Terima dengan syukur.",
            "Ambil yang perlu, tinggalkan yang tidak.",
            "Setiap item ini punya kisahnya. Kita lanjutkan kisah itu."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.DUNGEON_ENTER_REST,
            "Tanah ini menyembuhkan. Biarkan dia bekerja.",
            "Istirahat adalah bagian dari perjalanan, bukan kelemahan.",
            "Aku nyanyikan mantra pemulihan untuk kita semua."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.COMBAT_START,
            "Keseimbangan menuntut tindakan!",
            "Untuk alam dan kedamaian!",
            "Mari kita selesaikan ini.",
            "Kekuatan bumi bersamaku!"
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.COMBAT_PLAYER_LOW_HP,
            "Kau terluka! Biarkan energi bumi menyembuhkanmu!",
            "Bertahan! Alam semesta belum selesai denganmu!",
            "Jangan menyerah! Masih banyak yang menunggumu di luar sana!"
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.COMBAT_ENEMY_DIES,
            "Semoga rohnya menemukan kedamaian.",
            "Selesai dengan hormat.",
            "Keseimbangan dipulihkan.",
            "Begitulah alam bekerja."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.COMBAT_VICTORY,
            "Keseimbangan dipulihkan. Kita berhasil.",
            "Alam semesta tersenyum pada kita hari ini.",
            "Setiap kemenangan adalah hadiah. Syukuri."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.COMBAT_DEFEAT,
            "Tidak apa-apa. Setiap jatuh adalah pelajaran untuk tumbuh.",
            "Alam mengajarkan kita hari ini. Dengarkan.",
            "Kita mundur, bukan menyerah. Ada bedanya."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.COMBAT_BOSS_START,
            "Kekuatan ini luar biasa... tapi keseimbangan lebih kuat.",
            "Alam bersama kita. Jangan takut.",
            "Ini momen yang sudah ditakdirkan. Berikan segalanya!"
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.DUNGEON_BOSS_CLEARED,
            "Keseimbangan telah dipulihkan. Indah.",
            "Kita lakukan yang harus dilakukan. Terima kasih semua.",
            "Alam berterima kasih atas pengorbanan kita."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.DUNGEON_ENTER_FLOOR,
            "Lantai baru. Cerita baru. Aku siap.",
            "Energi di sini berbeda... tapi bukan tidak bersahabat.",
            "Kita terus maju. Alam menuntun langkah kita."
        );
    }

    /**
     * Ambil dialog random dari mercenary untuk trigger tertentu.
     * Return null jika tidak ada dialog untuk kombinasi ini.
     */
    public static String getDialogue(MercenaryType type, Trigger trigger) {
        Map<Trigger, List<String>> byType = DIALOGUES.get(type);
        if (byType == null) return null;
        List<String> lines = byType.get(trigger);
        if (lines == null || lines.isEmpty()) return null;
        return lines.get(RNG.nextInt(lines.size()));
    }

    /**
     * Ambil dialog dari semua merc aktif untuk trigger tertentu.
     * Hanya 1-2 merc yang akan berbicara (acak, tidak semua sekaligus).
     */
    public static List<ChatMessage> getGroupDialogue(
            List<arclightcity.entity.mercenary.Mercenary> mercs, Trigger trigger) {
        List<ChatMessage> result = new ArrayList<>();
        if (mercs == null || mercs.isEmpty()) return result;

        // Shuffle agar tidak selalu merc pertama yang ngomong duluan
        List<arclightcity.entity.mercenary.Mercenary> shuffled = new ArrayList<>(mercs);
        Collections.shuffle(shuffled, RNG);

        // Ambil dialog dari 1-2 merc (tidak semua ngobrol setiap saat)
        int maxSpeakers = Math.min(shuffled.size(), 1 + RNG.nextInt(2));

        for (int i = 0; i < maxSpeakers; i++) {
            arclightcity.entity.mercenary.Mercenary merc = shuffled.get(i);
            String line = getDialogue(merc.getMercenaryType(), trigger);
            if (line != null) {
                result.add(new ChatMessage(
                        merc.getMercenaryType(),
                        merc.getMercenaryType().displayName, // pakai displayName Nusantara
                        line
                ));
            }
        }

        return result;
    }

    // ── Helper ────────────────────────────────────────────────

    private static void addDialogues(MercenaryType type, Trigger trigger, String... lines) {
        DIALOGUES.computeIfAbsent(type, k -> new EnumMap<>(Trigger.class))
                 .computeIfAbsent(trigger, k -> new ArrayList<>())
                 .addAll(Arrays.asList(lines));
    }

    // ── ChatMessage record ─────────────────────────────────────

    public record ChatMessage(
            MercenaryType mercType,
            String        mercName,
            String        text
    ) {}
}
