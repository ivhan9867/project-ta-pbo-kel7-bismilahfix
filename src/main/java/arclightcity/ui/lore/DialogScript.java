package arclightcity.ui.lore;

import java.util.*;
import static arclightcity.ui.lore.DialogBeat.*;

/**
 * Semua skrip dialog game, diorganisir per trigger ID.
 * Trigger ID dipanggil dari GameEngine / DungeonManager.
 */
public final class DialogScript {

    private DialogScript() {}

    private static final Map<String, List<DialogBeat>> SCRIPTS = new LinkedHashMap<>();

    static { buildAll(); }

    public static List<DialogBeat> get(String id) {
        return SCRIPTS.getOrDefault(id, Collections.emptyList());
    }

    public static boolean has(String id) { return SCRIPTS.containsKey(id); }

    // ══════════════════════════════════════════════════════════
    private static void buildAll() {
        opening();
        firstDungeon();
        firstShop();
        raksakalaMiniBoss();
        boss1Pre(); boss1Post();
        recruitGatotkaca(); recruitSrikandi(); recruitBima();
        boss2Pre(); boss2Post();
        theresaVoice1(); ranggaReveal();
        boss3Pre(); boss3Post();
        boss4Pre(); boss4Post();
        boss5Pre(); boss5Post();
        finalBossPre(); ending();
    }

    // ══════════════════════════════════════════════════════════
    // OPENING — berjalan saat new game dimulai
    // ══════════════════════════════════════════════════════════
    private static void opening() {
        SCRIPTS.put("OPENING", List.of(
            image("cut_opening_nusantara_freeze.png"),
            narration(bg("bg_pasar_malam_chaos.png"),
                "Nusantara selalu hangat.\nSelalu hidup.\nSampai ia datang."),
            image("cut_opening_theresa_silhouette.png"),
            narration(bg("bg_pasar_malam_chaos.png"),
                "Dan membawa kedinginan yang abadi."),

            // Kamar Asuna
            image("cut_asuna_room_night.png"),
            left(bg("bg_pasar_malam_chaos.png"), asuna(), "ASUNA",
                "Server maintenance jam segini...? Gila apa."),
            image("cut_pc_screen_glitch.png"),
            left(bg("bg_pasar_malam_chaos.png"), asuna(), "ASUNA",
                "Eh? Bug apa ini..."),
            image("cut_asuna_sucked_in.png"),
            image("cut_asuna_falling_pasar.png"),

            // Mendarat
            image("cut_asuna_landing_canopy.png"),
            left(bg("bg_pasar_malam_chaos.png"), asuna(), "ASUNA",
                "...Ini... di mana?! Ini bukan Jakarta!"),
            left(bg("bg_pasar_hidden_corner.png"), gm("kiageng"), "KI AGENG",
                "Kamu dari dimensi lain. Kukira masih bertahun-tahun lagi\nsebelum ini terjadi."),
            left(bg("bg_pasar_hidden_corner.png"), asuna(), "ASUNA",
                "HAH?! Di mana ini?! Siapa kamu?!"),
            left(bg("bg_pasar_hidden_corner.png"), gm("kiageng"), "KI AGENG",
                "Namaku Ki Ageng. Dan kamu baru saja tiba di Nusantara —\ntepat saat dia mulai membekukannya."),
            left(bg("bg_pasar_hidden_corner.png"), gm("kiageng"), "KI AGENG",
                "Theresa. Dia datang dari celah antardimensi tiga bulan lalu.\nDi mana pun dia melangkah, tanah membeku."),
            left(bg("bg_pasar_hidden_corner.png"), gm("kiageng"), "KI AGENG",
                "Lima Penjaga Agung kami sudah dua yang jatuh.\nJika semua lima — Nusantara tidak punya kehangatan lagi."),
            choose(bg("bg_pasar_hidden_corner.png"), asuna(), "ASUNA",
                "Dan aku bisa bantu karena...?",
                "Karena aku gamer dan sudah sering kalahkan dungeon?",
                "...Ini kayak plot isekai banget sih.",
                "Oke, apapun alasannya. Aku bantu."),
            left(bg("bg_pasar_hidden_corner.png"), gm("kiageng"), "KI AGENG",
                "Void Ice tidak bisa membekukan seseorang dari dimensi lain.\nKamu satu-satunya yang bisa mendekati para Penjaga."),
            left(bg("bg_pasar_hidden_corner.png"), asuna(), "ASUNA",
                "...Ini persis plot game isekai. Kecuali ini beneran."),
            left(bg("bg_pasar_hidden_corner.png"), asuna(), "ASUNA",
                "Oke. Tunjukkan dungeonnya.")
        ));
    }

    // ══════════════════════════════════════════════════════════
    // EVENT TRIGGERS
    // ══════════════════════════════════════════════════════════
    private static void firstDungeon() {
        SCRIPTS.put("FIRST_DUNGEON", List.of(
            left(bg("bg_dungeon_entrance_pasar.png"), gm("kiageng"), "KI AGENG",
                "Makhluk di dalam bukan jahat dari asalnya."),
            left(bg("bg_dungeon_entrance_pasar.png"), gm("kiageng"), "KI AGENG",
                "Void Ice membekukan pikiran mereka — menyerang karena\nbingung, bukan pilihan. Ingat itu."),
            left(bg("bg_dungeon_entrance_pasar.png"), asuna(), "ASUNA",
                "...Noted. Masuk.")
        ));
    }

    private static void firstShop() {
        SCRIPTS.put("FIRST_SHOP", List.of(
            right(bg("bg_dungeon_entrance_pasar.png"), npc("portrait_pakwardi_normal"), "PAK WARDI",
                "Heh, muka baru! Dari dunia mana kamu?"),
            right(bg("bg_dungeon_entrance_pasar.png"), npc("portrait_pakwardi_normal"), "PAK WARDI",
                "Nggak penting sih, yang penting punya gold.\nDi sini aku jual segalanya."),
            right(bg("bg_dungeon_entrance_pasar.png"), npc("portrait_pakwardi_happy"), "PAK WARDI",
                "Obat, besi tua, bahkan informasi soal Theresa\nkalau kamu mau. Tapi yang terakhir mahal. Hehehe.")
        ));
    }

    // ══════════════════════════════════════════════════════════
    // RAKSASA KALA — MINI BOSS LT 1-9
    // ══════════════════════════════════════════════════════════
    private static void raksakalaMiniBoss() {
        SCRIPTS.put("RAKSAKALA_PRE", List.of(
            image("cut_raksakala_reveal.png"),
            left(bg("bg_dungeon_entrance_pasar.png"), gm("kiageng"), "KI AGENG",
                "Raksasa Kala. Penjaga batas dalam tradisi kita.\nBukan jahat — hanya terkena Void Ice."),
            left(bg("bg_dungeon_entrance_pasar.png"), asuna(), "ASUNA",
                "Berarti kalau kita kalahkan, dia bebas?"),
            left(bg("bg_dungeon_entrance_pasar.png"), gm("kiageng"), "KI AGENG",
                "Hentikan energinya — bukan hancurkan. Bedanya penting.")
        ));
    }

    // ══════════════════════════════════════════════════════════
    // BOSS 1 — BATARA KALA (LT 10)
    // ══════════════════════════════════════════════════════════
    private static void boss1Pre() {
        SCRIPTS.put("BOSS1_PRE", List.of(
            new DialogBeat(bg("bg_pasar_deep_chamber.png"), null, null, null, null, null, true, null),
            left(bg("bg_pasar_deep_chamber.png"), gm("kiageng"), "KI AGENG",
                "Pilar pertama. Batara Kala — Dewa Waktu.\nDia menjaga alur waktu Nusantara agar tidak kacau."),
            left(bg("bg_pasar_deep_chamber.png"), asuna(), "ASUNA",
                "Dan sekarang Theresa memakainya jadi penjaga dungeon."),
            left(bg("bg_pasar_deep_chamber.png"), gm("kiageng"), "KI AGENG",
                "Kalahkan — tapi jangan bunuh. Hentikan energinya,\nVoid Ice akan longgar sendiri."),
            image("cut_batarakala_lucid_moment.png"),
            right(bg("bg_pasar_deep_chamber.png"), bossA("batara_kala"), "BATARA KALA",
                "...Pergi... bahaya... tidak bisa... berhenti..."),
            left(bg("bg_pasar_deep_chamber.png"), asuna(), "ASUNA",
                "Dia masih ada di dalam sana."),
            left(bg("bg_pasar_deep_chamber.png"), gm("kiageng"), "KI AGENG",
                "Maka kita harus membebaskannya. Sekarang.")
        ));
    }

    private static void boss1Post() {
        SCRIPTS.put("BOSS1_POST", List.of(
            image("cut_batarakala_freed.png"),
            right(bg("bg_pasar_deep_chamber.png"), boss("batara_kala"), "BATARA KALA",
                "...Lama sekali. Terima kasih."),
            left(bg("bg_pasar_deep_chamber.png"), gm("kiageng"), "KI AGENG",
                "Red Essence Shard. Pecahan dari Red Blossom Katana.\nSatu dari lima."),
            left(bg("bg_pasar_deep_chamber.png"), asuna(), "ASUNA",
                "Katana? Ada hubungannya dengan Theresa?"),
            left(bg("bg_pasar_deep_chamber.png"), gm("kiageng"), "KI AGENG",
                "Satu-satunya senjata yang bisa menembus Void Ice-nya.\nKita perlu lima shard dari lima Penjaga.")
        ));
    }

    // ══════════════════════════════════════════════════════════
    // REKRUT GUILDMATE
    // ══════════════════════════════════════════════════════════
    private static void recruitGatotkaca() {
        SCRIPTS.put("RECRUIT_GATOTKACA", List.of(
            image("cut_gatotkaca_solo_fight.png"),
            right(bg("bg_candi_entrance_exterior.png"), gm("gatotkaca"), "GATOT KACA",
                "*(menghela nafas)* Terlalu banyak untuk sendiri ternyata."),
            choose(bg("bg_candi_entrance_exterior.png"), asuna(), "ASUNA",
                "Mau ikut?",
                "Kamu kuat. Tim butuh orang sepertimu.",
                "Mau ikut? Ini berbahaya sih.",
                "Berdua lebih aman dari sendiri."),
            right(bg("bg_candi_entrance_exterior.png"), gm("gatotkaca"), "GATOT KACA",
                "Kamu yang kalahkan Batara Kala? Sendiri?"),
            left(bg("bg_candi_entrance_exterior.png"), asuna(), "ASUNA",
                "Dengan Ki Ageng."),
            right(bg("bg_candi_entrance_exterior.png"), gm("gatotkaca"), "GATOT KACA",
                "...Boleh aku ikut?")
        ));
    }

    private static void recruitSrikandi() {
        SCRIPTS.put("RECRUIT_SRIKANDI", List.of(
            image("cut_srikandi_protecting_refugees.png"),
            right(bg("bg_candi_entrance_exterior.png"), gm("srikandi"), "SRIKANDI",
                "*(tanpa berbalik)* Aku sudah mengikuti pergerakanmu\nsejak Pasar Malam. Kamu efisien. Tidak banyak omong."),
            left(bg("bg_candi_entrance_exterior.png"), asuna(), "ASUNA",
                "...Terima kasih? Kamu mau bergabung?"),
            right(bg("bg_candi_entrance_exterior.png"), gm("srikandi"), "SRIKANDI",
                "Bukan. Aku mau pastikan Theresa kalah.\nKebetulan tujuan kita sama.")
        ));
    }

    private static void recruitBima() {
        SCRIPTS.put("RECRUIT_BIMA", List.of(
            image("cut_bima_trapped_ruins.png"),
            right(bg("bg_candi_interior.png"), gm("bima"), "BIMA",
                "OI! Akhirnya ada yang lewat! Sudah tiga hari di sini!\nBisa tolong angkat ini sedikit?!"),
            narration(bg("bg_candi_interior.png"), "Party mengangkat reruntuhan..."),
            right(bg("bg_candi_interior.png"), gm("bima"), "BIMA",
                "Makasih. Theresa yang bikin ini jatuh.\nAku lagi nonton pas atapnya ambrol."),
            left(bg("bg_candi_interior.png"), gm("gatotkaca"), "GATOT KACA",
                "Kamu kenapa tidak hancurkan sendiri? Kamu Bima."),
            right(bg("bg_candi_interior.png"), gm("bima"), "BIMA",
                "...Nggak enak aja ngehancurin candi bersejarah.")
        ));
    }

    // ══════════════════════════════════════════════════════════
    // BOSS 2 — NYI RORO KIDUL (LT 20)
    // ══════════════════════════════════════════════════════════
    private static void boss2Pre() {
        SCRIPTS.put("BOSS2_PRE", List.of(
            new DialogBeat(bg("bg_candi_frozen_sea_chamber.png"), null, null, null, null, null, true, null),
            left(bg("bg_candi_frozen_sea_chamber.png"), gmA("nyairoro"), "NYAI RORO",
                "...Guru."),
            right(bg("bg_candi_frozen_sea_chamber.png"), bossA("nyi_roro"), "NYI RORO KIDUL",
                "...Siapa... yang berani masuk ke sini..."),
            left(bg("bg_candi_frozen_sea_chamber.png"), gmA("nyairoro"), "NYAI RORO",
                "*(mata berkaca-kaca)* Tidak, Guru.\nAku di sini untuk membawa Guru pulang."),
            image("cut_nyairoro_facing_guru.png"),
            right(bg("bg_candi_frozen_sea_chamber.png"), bossA("nyi_roro"), "NYI RORO KIDUL",
                "...Muridku... lari..."),
            left(bg("bg_candi_frozen_sea_chamber.png"), gmA("nyairoro"), "NYAI RORO",
                "Tidak.")
        ));
    }

    private static void boss2Post() {
        SCRIPTS.put("BOSS2_POST", List.of(
            image("cut_nyirorokidul_freed.png"),
            right(bg("bg_candi_frozen_sea_chamber.png"), boss("nyi_roro"), "NYI RORO KIDUL",
                "*(berbisik)* Muridku... jaga laut untuk kita...\njaga Nusantara..."),
            image("cut_nyairoro_kneeling.png"),
            left(bg("bg_candi_frozen_sea_chamber.png"), asuna(), "ASUNA",
                "Hei. Dia sudah bebas. Kamu berhasil."),
            left(bg("bg_candi_frozen_sea_chamber.png"), gmA("nyairoro"), "NYAI RORO",
                "*(diam, air mata jatuh)*"),
            left(bg("bg_candi_frozen_sea_chamber.png"), gm("kiageng"), "KI AGENG",
                "Red Essence Shard kedua. Dua dari lima.")
        ));
    }

    // ══════════════════════════════════════════════════════════
    // THERESA VOICE & RANGGA REVEAL
    // ══════════════════════════════════════════════════════════
    private static void theresaVoice1() {
        SCRIPTS.put("THERESA_VOICE_1", List.of(
            new DialogBeat(bg("bg_theresa_voice_overlay.png"), null, null, null, null, null, true, null),
            right(bg("bg_theresa_voice_overlay.png"), boss("theresa"), "THERESA",
                "Dua dari lima. Lebih cepat dari perkiraanku."),
            right(bg("bg_theresa_voice_overlay.png"), boss("theresa"), "THERESA",
                "Gadis dari dimensi lain... kamu tahu mengapa kamu\nbisa melakukan ini?"),
            right(bg("bg_theresa_voice_overlay.png"), boss("theresa"), "THERESA",
                "Karena kamu tidak punya koneksi dengan Nusantara.\nPulanglah. Biarkan proses ini selesai dengan damai."),
            left(bg("bg_theresa_voice_overlay.png"), asuna(), "ASUNA",
                "Damai buat siapa?"),
            narration(bg("bg_theresa_voice_overlay.png"),
                "Suara itu menghilang. Tidak ada jawaban.")
        ));
    }

    private static void ranggaReveal() {
        SCRIPTS.put("RANGGA_REVEAL", List.of(
            image("cut_rangga_emerges.png"),
            right(bg("bg_candi_interior.png"), gm("rangga"), "RANGGA",
                "Jangan dengarkan dia. Aku sudah pantau Theresa enam bulan.\nDia tidak pernah menawarkan sesuatu tanpa alasan."),
            left(bg("bg_candi_interior.png"), asuna(), "ASUNA",
                "Kamu... sudah di sini dari tadi?"),
            right(bg("bg_candi_interior.png"), gm("rangga"), "RANGGA",
                "Aku ikut sejak Pasar Malam. Kamu tidak sadar.")
        ));
    }

    // ══════════════════════════════════════════════════════════
    // BOSS 3 — RANGDA AGUNG (LT 30)
    // ══════════════════════════════════════════════════════════
    private static void boss3Pre() {
        SCRIPTS.put("BOSS3_PRE", List.of(
            new DialogBeat(bg("bg_hutan_angker_deep.png"), null, null, null, null, null, true, null),
            image("cut_dewisri_frontline.png"),
            left(bg("bg_hutan_angker_deep.png"), gmA("dewisri"), "DEWI SRI",
                "Rangda Agung bukan iblis. Dia penjaga batas\nantara dunia hidup dan dunia roh."),
            left(bg("bg_hutan_angker_deep.png"), gmA("dewisri"), "DEWI SRI",
                "Tanpa dia, arwah yang gelisah bisa masuk ke\ndunia kita kapan saja. Banyak nyawa bergantung ini."),
            left(bg("bg_hutan_angker_deep.png"), asuna(), "ASUNA",
                "Dan sekarang Theresa memakainya jadi penjaga dungeon."),
            left(bg("bg_hutan_angker_deep.png"), gmA("dewisri"), "DEWI SRI",
                "Membebaskannya berarti batas itu kembali terjaga.\nAyo.")
        ));
    }

    private static void boss3Post() {
        SCRIPTS.put("BOSS3_POST", List.of(
            image("cut_rangdaagung_freed.png"),
            left(bg("bg_hutan_angker_deep.png"), gm("kiageng"), "KI AGENG",
                "Red Essence Shard ketiga. Tiga dari lima."),
            left(bg("bg_hutan_angker_deep.png"), gm("dewisri"), "DEWI SRI",
                "Batas dunia roh sudah kembali terjaga.\nKita lanjut.")
        ));
    }

    // ══════════════════════════════════════════════════════════
    // BOSS 4 — GARUDA MAHAGURU (LT 40)
    // ══════════════════════════════════════════════════════════
    private static void boss4Pre() {
        SCRIPTS.put("BOSS4_PRE", List.of(
            new DialogBeat(bg("bg_goa_naga_deep.png"), null, null, null, null, null, true, null),
            image("cut_srikandi_garuda_moment.png"),
            left(bg("bg_goa_naga_deep.png"), gmA("srikandi"), "SRIKANDI",
                "Garuda adalah alasan aku jadi pemanah.\nKata ibuku — anak panah yang benar terbang setinggi Garuda."),
            left(bg("bg_goa_naga_deep.png"), gmA("srikandi"), "SRIKANDI",
                "Kalau dia sudah dibekukan...\nberarti langit pun sudah tidak bebas lagi."),
            left(bg("bg_goa_naga_deep.png"), asuna(), "ASUNA",
                "Maka kita kembalikan kebebasannya.")
        ));
    }

    private static void boss4Post() {
        SCRIPTS.put("BOSS4_POST", List.of(
            image("cut_garudamahaguru_freed.png"),
            image("cut_garuda_feather_srikandi.png"),
            left(bg("bg_goa_naga_deep.png"), gmA("srikandi"), "SRIKANDI",
                "*(menangkap bulu emas, mata berkaca)*"),
            left(bg("bg_goa_naga_deep.png"), gm("kiageng"), "KI AGENG",
                "Red Essence Shard keempat. Empat dari lima.")
        ));
    }

    // ══════════════════════════════════════════════════════════
    // BOSS 5 — SEMAR PAMUNGKAS (LT 50)
    // ══════════════════════════════════════════════════════════
    private static void boss5Pre() {
        SCRIPTS.put("BOSS5_PRE", List.of(
            new DialogBeat(bg("bg_kahyangan_ruins.png"), null, null, null, null, null, true, null),
            left(bg("bg_kahyangan_ruins.png"), gm("bima"), "BIMA",
                "*(suara bergetar)* Kakek Semar... sering diceritain.\nKatanya dia nggak bisa dikuasai siapa pun."),
            left(bg("bg_kahyangan_ruins.png"), gm("kiageng"), "KI AGENG",
                "Theresa tidak menguasainya."),
            left(bg("bg_kahyangan_ruins.png"), gm("kiageng"), "KI AGENG",
                "Semar membiarkan dirinya terinfeksi — supaya yang lain\ntidak ikut jatuh. Dia menahan Void Ice dari menyebar."),
            image("cut_party_shocked_semar.png"),
            left(bg("bg_kahyangan_ruins.png"), asuna(), "ASUNA",
                "Jadi dia... mengorbankan dirinya sendiri?"),
            left(bg("bg_kahyangan_ruins.png"), gm("kiageng"), "KI AGENG",
                "Dan sekarang giliran kita membebaskannya\ndari pengorbanan itu.")
        ));
    }

    private static void boss5Post() {
        SCRIPTS.put("BOSS5_POST", List.of(
            image("cut_semar_final_smile.png"),
            right(bg("bg_kahyangan_ruins.png"), boss("semar"), "SEMAR PAMUNGKAS",
                "...Sudah lama menunggu."),
            image("cut_five_shards_merge.png"),
            left(bg("bg_kahyangan_ruins.png"), gm("kiageng"), "KI AGENG",
                "Lima Red Essence Shard — semuanya terkumpul."),
            narration(bg("bg_kahyangan_ruins.png"),
                "Kelima shard melayang dan bersatu.\nCahaya merah-emas meledak dari pusatnya."),
            left(bg("bg_kahyangan_ruins.png"), gm("rangga"), "RANGGA",
                "Red Blossom Katana. Satu-satunya yang bisa\nmenembus Void Ice Theresa."),
            left(bg("bg_kahyangan_ruins.png"), asuna(), "ASUNA",
                "Oke. Sekarang kita akhiri ini.")
        ));
    }

    // ══════════════════════════════════════════════════════════
    // FINAL BOSS — THERESA (LT 51+)
    // ══════════════════════════════════════════════════════════
    private static void finalBossPre() {
        SCRIPTS.put("FINAL_BOSS_PRE", List.of(
            image("cut_void_portal_opens.png"),
            left(bg("bg_void_dimension.png"), gm("rangga"), "RANGGA",
                "Ini bukan Nusantara lagi. Ini dimensinya."),
            left(bg("bg_void_dimension.png"), gm("kiageng"), "KI AGENG",
                "Void Ice yang menginfeksi para Penjaga — ini sumbernya.\nKita harus masuk."),
            image("cut_asuna_theresa_faceoff.png"),
            right(bg("bg_void_dimension.png"), boss("theresa"), "THERESA",
                "Gadis lemah dari dunia lain...\nberani menantangku?"),
            choose(bg("bg_void_dimension.png"), asuna(), "ASUNA",
                "Kamu takut sendiri. Itu kenapa kamu mau Nusantara ikut beku.",
                "Kedinginan itu bukan kedamaian. Itu kesepian.",
                "Kamu takut sendiri. Itu yang sebenarnya.",
                "Aku mengerti. Tapi bukan begitu caranya."),
            right(bg("bg_void_dimension.png"), bossA("theresa"), "THERESA",
                "Tutup mulutmu. Kau tidak tahu apa-apa."),
            left(bg("bg_void_dimension.png"), asuna(), "ASUNA",
                "Di dimensimu — tidak ada siapa-siapa, kan.\nKedinginan yang kamu bilang damai itu bukan damai.\nItu kesepian yang sudah lama banget."),
            right(bg("bg_void_dimension.png"), bossA("theresa"), "THERESA",
                "Sekarang — tunjukkan kekuatanmu!")
        ));
    }

    // ══════════════════════════════════════════════════════════
    // ENDING — setelah Theresa dikalahkan
    // Urutan: video nusantara_restored → scene theresa_defeated → dialog
    // ══════════════════════════════════════════════════════════
    private static void ending() {
        SCRIPTS.put("ENDING", List.of(
            // Video Nusantara dipulihkan DULU
            video("cut_nusantara_restored_ending.mp4"),

            // Video self-made animation (file: legacy_snow_ending.mp4)
            // Kamu perlu copy file ini ke: resources/assets/lore/video/legacy_snow_ending.mp4
            video("legacy_snow_ending.mp4"),

            // Baru scene Theresa defeated
            image("cut_theresa_defeated.png"),
            left(bg("bg_void_dimension.png"), asuna(), "ASUNA",
                "Kamu takut sendirian. Itu saja."),
            image("cut_asuna_theresa_intimate.png"),
            right(bg("bg_void_dimension.png"), boss("theresa"), "THERESA",
                "...Kenapa hangat?"),
            left(bg("bg_void_dimension.png"), asuna(), "ASUNA",
                "Karena kamu baru saja kalah dari seseorang yang\npeduli sama tempat ini. Dan kamu merasakan itu."),
            right(bg("bg_void_dimension.png"), boss("theresa"), "THERESA",
                "...Aku tidak tahu harus pergi ke mana."),
            left(bg("bg_void_dimension.png"), asuna(), "ASUNA",
                "Itu urusan nanti."),

            // Kembali ke Nusantara
            image("cut_party_laughing.png"),
            left(bg("bg_kahyangan_ruins.png"), gm("kiageng"), "KI AGENG",
                "Energimu memudar. Portal akan terbuka tidak lama lagi."),
            left(bg("bg_kahyangan_ruins.png"), asuna(), "ASUNA",
                "...Sudah?"),
            left(bg("bg_kahyangan_ruins.png"), gm("gatotkaca"), "GATOT KACA",
                "Memangnya mau tinggal di sini selamanya?"),
            left(bg("bg_kahyangan_ruins.png"), asuna(), "ASUNA",
                "Tidak. Tapi... aku belum selesai UAS."),
            narration(bg("bg_kahyangan_ruins.png"),
                "Seluruh party tertawa — untuk pertama kalinya,\ntawa yang benar-benar lepas."),

            // Portal
            image("cut_return_portal.png"),
            left(bg("bg_kahyangan_ruins.png"), gm("gatotkaca"), "GATOT KACA",
                "Kalau Nusantara memanggilmu lagi —\nkamu akan tahu jalannya."),
            right(bg("bg_void_dimension.png"), boss("theresa"), "THERESA",
                "...Mungkin aku coba lihat apa itu hangat."),
            narration(bg("bg_kahyangan_ruins.png"),
                "Asuna mengangguk. Melangkah masuk.\nPortal menutup."),

            // Video penutup Theresa berjalan ke cahaya
            video("cut_theresa_walks_to_warmth.mp4")
        ));
    }
}
