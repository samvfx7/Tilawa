package com.example.data.source

import com.example.data.model.QuranAyah
import com.example.data.model.QuranSurah
import com.example.data.model.QuranWord
import com.example.data.model.RevelationType
import com.example.recognition.normalizer.ArabicNormalizer

object QuranDataSource {

    /**
     * Builds word tokens from an Ayah's Uthmani Arabic string.
     */
    private fun buildWords(surahNumber: Int, ayahNumber: Int, ayahText: String, startGlobalPos: Int): List<QuranWord> {
        val rawTokens = ayahText.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        return rawTokens.mapIndexed { index, token ->
            QuranWord(
                id = "${surahNumber}_${ayahNumber}_${index + 1}",
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                positionInAyah = index + 1,
                globalPosition = startGlobalPos + index,
                textUthmani = token,
                textNormalized = ArabicNormalizer.normalizeForRecognition(token)
            )
        }
    }

    /**
     * Complete Surahs with full authentic Uthmani text and word segmentation.
     */
    val SURAHS_WITH_TEXT: Map<Int, QuranSurah> by lazy {
        val map = mutableMapOf<Int, QuranSurah>()

        // 1. Surah Al-Fatihah (7 Ayahs)
        val fatihahAyahsText = listOf(
            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
            "الرَّحْمَٰنِ الرَّحِيمِ",
            "مَالِكِ يَوْمِ الدِّينِ",
            "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
            "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
            "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ"
        )
        map[1] = createSurahWithAyahs(
            number = 1,
            nameArabic = "الفَاتِحَة",
            nameEnglish = "Al-Fatihah",
            englishTranslation = "The Opening",
            revelationType = RevelationType.MECCAN,
            juzNumber = 1,
            ayahTexts = fatihahAyahsText
        )

        // 112. Surah Al-Ikhlas (4 Ayahs)
        val ikhlasAyahsText = listOf(
            "قُلْ هُوَ اللَّهُ أَحَدٌ",
            "اللَّهُ الصَّمَدُ",
            "لَمْ يَلِدْ وَلَمْ يُولَدْ",
            "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ"
        )
        map[112] = createSurahWithAyahs(
            number = 112,
            nameArabic = "الإِخْلَاص",
            nameEnglish = "Al-Ikhlas",
            englishTranslation = "The Sincerity",
            revelationType = RevelationType.MECCAN,
            juzNumber = 30,
            ayahTexts = ikhlasAyahsText
        )

        // 113. Surah Al-Falaq (5 Ayahs)
        val falaqAyahsText = listOf(
            "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ",
            "مِن شَرِّ مَا خَلَقَ",
            "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ",
            "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ",
            "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ"
        )
        map[113] = createSurahWithAyahs(
            number = 113,
            nameArabic = "الفَلَق",
            nameEnglish = "Al-Falaq",
            englishTranslation = "The Daybreak",
            revelationType = RevelationType.MECCAN,
            juzNumber = 30,
            ayahTexts = falaqAyahsText
        )

        // 114. Surah An-Nas (6 Ayahs)
        val nasAyahsText = listOf(
            "قُلْ أَعُوذُ بِرَبِّ النَّاسِ",
            "مَلِكِ النَّاسِ",
            "إِلَٰهِ النَّاسِ",
            "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ",
            "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ",
            "مِنَ الْجِنَّةِ وَالنَّاسِ"
        )
        map[114] = createSurahWithAyahs(
            number = 114,
            nameArabic = "النَّاس",
            nameEnglish = "An-Nas",
            englishTranslation = "Mankind",
            revelationType = RevelationType.MECCAN,
            juzNumber = 30,
            ayahTexts = nasAyahsText
        )

        // 108. Surah Al-Kawthar (3 Ayahs)
        val kawtharAyahsText = listOf(
            "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ",
            "فَصَلِّ لِرَبِّكَ وَانْحَرْ",
            "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ"
        )
        map[108] = createSurahWithAyahs(
            number = 108,
            nameArabic = "الكَوْثَر",
            nameEnglish = "Al-Kawthar",
            englishTranslation = "The Abundance",
            revelationType = RevelationType.MECCAN,
            juzNumber = 30,
            ayahTexts = kawtharAyahsText
        )

        // 110. Surah An-Nasr (3 Ayahs)
        val nasrAyahsText = listOf(
            "إِذَا جَاءَ نَصْرُ اللَّهِ وَالْفَتْحُ",
            "وَرَأَيْتَ النَّاسَ يَدْخُلُونَ فِي دِينِ اللَّهِ أَفْوَاجًا",
            "فَسَبِّحْ بِحَمْدِ رَبِّكَ وَاسْتَغْفِرْهُ إِنَّهُ كَانَ تَوَّابًا"
        )
        map[110] = createSurahWithAyahs(
            number = 110,
            nameArabic = "النَّصْر",
            nameEnglish = "An-Nasr",
            englishTranslation = "The Divine Help",
            revelationType = RevelationType.MEDINAN,
            juzNumber = 30,
            ayahTexts = nasrAyahsText
        )

        // 109. Surah Al-Kafirun (6 Ayahs)
        val kafirunAyahsText = listOf(
            "قُلْ يَا أَيُّهَا الْكَافِرُونَ",
            "لَا أَعْبُدُ مَا تَعْبُدُونَ",
            "وَلَا أَنتُمْ عَابِدُونَ مَا أَعْبُدُ",
            "وَلَا أَنَا عَابِدٌ مَّا عَبَدتُّمْ",
            "وَلَا أَنتُمْ عَابِدُونَ مَا أَعْبُدُ",
            "لَكُمْ دِينُكُمْ وَلِيَ دِينِ"
        )
        map[109] = createSurahWithAyahs(
            number = 109,
            nameArabic = "الكَافِرُون",
            nameEnglish = "Al-Kafirun",
            englishTranslation = "The Disbelievers",
            revelationType = RevelationType.MECCAN,
            juzNumber = 30,
            ayahTexts = kafirunAyahsText
        )

        // 107. Surah Al-Ma'un (7 Ayahs)
        val maunAyahsText = listOf(
            "أَرَأَيْتَ الَّذِي يُكَذِّبُ بِالدِّينِ",
            "فَذَٰلِكَ الَّذِي يَدُعُّ الْيَتِيمَ",
            "وَلَا يَحُضُّ عَلَىٰ طَعَامِ الْمِسْكِينِ",
            "فَوَيْلٌ لِّلْمُصَلِّينَ",
            "الَّذِينَ هُمْ عَن صَلَاتِهِمْ سَاهُونَ",
            "الَّذِينَ هُمْ يُرَاءُونَ",
            "وَيَمْنَعُونَ الْمَاعُونَ"
        )
        map[107] = createSurahWithAyahs(
            number = 107,
            nameArabic = "المَاعُون",
            nameEnglish = "Al-Ma'un",
            englishTranslation = "The Small Kindnesses",
            revelationType = RevelationType.MECCAN,
            juzNumber = 30,
            ayahTexts = maunAyahsText
        )

        // 106. Surah Quraysh (4 Ayahs)
        val qurayshAyahsText = listOf(
            "لِإِيلَافِ قُرَيْشٍ",
            "إِيلَافِهِمْ رِحْلَةَ الشِّتَاءِ وَالصَّيْفِ",
            "فَلْيَعْبُدُوا رَبَّ هَٰذَا الْبَيْتِ",
            "الَّذِي أَطْعَمَهُم مِّن جُوعٍ وَآمَنَهُم مِّنْ خَوْفٍ"
        )
        map[106] = createSurahWithAyahs(
            number = 106,
            nameArabic = "قُرَيْش",
            nameEnglish = "Quraysh",
            englishTranslation = "Quraysh",
            revelationType = RevelationType.MECCAN,
            juzNumber = 30,
            ayahTexts = qurayshAyahsText
        )

        // 105. Surah Al-Fil (5 Ayahs)
        val filAyahsText = listOf(
            "أَلَمْ تَرَ كَيْفَ فَعَلَ رَبُّكَ بِأَصْحَابِ الْفِيلِ",
            "أَلَمْ يَجْعَلْ كَيْدَهُمْ فِي تَضْلِيلٍ",
            "وَأَرْسَلَ عَلَيْهِمْ طَيْرًا أَبَابِيلَ",
            "تَرْمِيهِم بِحِجَارَةٍ مِّن سِجِّيلٍ",
            "فَجَعَلَهُمْ كَعَصْفٍ مَّأْكُولٍ"
        )
        map[105] = createSurahWithAyahs(
            number = 105,
            nameArabic = "الفِيل",
            nameEnglish = "Al-Fil",
            englishTranslation = "The Elephant",
            revelationType = RevelationType.MECCAN,
            juzNumber = 30,
            ayahTexts = filAyahsText
        )

        // 97. Surah Al-Qadr (5 Ayahs)
        val qadrAyahsText = listOf(
            "إِنَّا أَنزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ",
            "وَمَا أَدْرَاكَ مَا لَيْلَةُ الْقَدْرِ",
            "لَيْلَةُ الْقَدْرِ خَيْرٌ مِّنْ أَلْفِ شَهْرٍ",
            "تَنَزَّلُ الْمَلَائِكَةُ وَالرُّوحُ فِيهَا بِإِذْنِ رَبِّهِم مِّن كُلِّ أَمْرٍ",
            "سَلَامٌ هِيَ حَتَّىٰ مَطْلَعِ الْفَجْرِ"
        )
        map[97] = createSurahWithAyahs(
            number = 97,
            nameArabic = "القَدْر",
            nameEnglish = "Al-Qadr",
            englishTranslation = "The Power / Decree",
            revelationType = RevelationType.MECCAN,
            juzNumber = 30,
            ayahTexts = qadrAyahsText
        )

        // 94. Surah Ash-Sharh (8 Ayahs)
        val sharhAyahsText = listOf(
            "أَلَمْ نَشْرَحْ لَكَ صَدْرَكَ",
            "وَوَضَعْنَا عَنكَ وِزْرَكَ",
            "الَّذِي أَنقَضَ ظَهْرَكَ",
            "وَرَفَعْنَا لَكَ ذِكْرَكَ",
            "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا",
            "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            "فَإِذَا فَرَغْتَ فَانصَبْ",
            "وَإِلَىٰ رَبِّكَ فَارْغَب"
        )
        map[94] = createSurahWithAyahs(
            number = 94,
            nameArabic = "الشَّرْح",
            nameEnglish = "Ash-Sharh",
            englishTranslation = "The Relief",
            revelationType = RevelationType.MECCAN,
            juzNumber = 30,
            ayahTexts = sharhAyahsText
        )

        // 93. Surah Ad-Duha (11 Ayahs)
        val duhaAyahsText = listOf(
            "وَالضُّحَىٰ",
            "وَاللَّيْلِ إِذَا سَجَىٰ",
            "مَا وَدَّعَكَ رَبُّكَ وَمَا قَلَىٰ",
            "وَلَلْآخِرَةُ خَيْرٌ لَّكَ مِنَ الْأُولَىٰ",
            "وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ",
            "أَلَمْ يَجِدْكَ يَتِيمًا فَآوَىٰ",
            "وَوَجَدَكَ ضَالًّا فَهَدَىٰ",
            "وَوَجَدَكَ عَائِلًا فَأَغْنَىٰ",
            "فَأَمَّا الْيَتِيمَ فَلَا تَقْهَرْ",
            "وَأَمَّا السَّائِلَ فَلَا تَنْهَرْ",
            "وَأَمَّا بِنِعْمَةِ رَبِّكَ فَحَدِّثْ"
        )
        map[93] = createSurahWithAyahs(
            number = 93,
            nameArabic = "الضُّحَى",
            nameEnglish = "Ad-Duha",
            englishTranslation = "The Morning Hours",
            revelationType = RevelationType.MECCAN,
            juzNumber = 30,
            ayahTexts = duhaAyahsText
        )

        // 67. Surah Al-Mulk (First 5 Ayahs for practice)
        val mulkAyahsText = listOf(
            "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا وَهُوَ الْعَزِيزُ الْغَفُورُ",
            "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا مَّا تَرَىٰ فِي خَلْقِ الرَّحْمَٰنِ مِن تَفَاوُتٍ فَارْجِعِ الْبَصَرَ هَلْ تَرَىٰ مِن فُطُورٍ",
            "ثُمَّ ارْجِعِ الْبَصَرَ كَرَّتَيْنِ يَنقَلِبْ إِلَيْكَ الْبَصَرُ خَاسِئًا وَهُوَ حَسِيرٌ",
            "وَلَقَدْ زَيَّنَّا السَّمَاءَ الدُّنْيَا بِمَصَابِيحَ وَجَعَلْنَاهَا رُجُومًا لِّلشَّيَاطِينِ وَأَعْتَدْنَا لَهُمْ عَذَابَ السَّعِيرِ"
        )
        map[67] = createSurahWithAyahs(
            number = 67,
            nameArabic = "المُلْك",
            nameEnglish = "Al-Mulk",
            englishTranslation = "The Sovereignty",
            revelationType = RevelationType.MECCAN,
            juzNumber = 29,
            ayahTexts = mulkAyahsText
        )

        // 36. Surah Ya-Sin (First 6 Ayahs)
        val yasinAyahsText = listOf(
            "يس",
            "وَالْقُرْآنِ الْحَكِيمِ",
            "إِنَّكَ لَمِنَ الْمُرْسَلِينَ",
            "عَلَىٰ صِرَاطٍ مُّسْتَقِيمٍ",
            "تَنزِيلَ الْعَزِيزِ الرَّحِيمِ",
            "لِتُنذِرَ قَوْمًا مَّا أُنذِرَ آبَاؤُهُمْ فَهُمْ غَافِلُونَ"
        )
        map[36] = createSurahWithAyahs(
            number = 36,
            nameArabic = "يس",
            nameEnglish = "Ya-Sin",
            englishTranslation = "Ya-Sin",
            revelationType = RevelationType.MECCAN,
            juzNumber = 22,
            ayahTexts = yasinAyahsText
        )

        // 2. Surah Al-Baqarah (Ayat Al-Kursi 255)
        val baqarahAyatKursi = listOf(
            "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ وَلَا يَئُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ"
        )
        map[2] = createSurahWithAyahs(
            number = 2,
            nameArabic = "البَقَرَة (آية الكرسي)",
            nameEnglish = "Al-Baqarah (Ayat Al-Kursi)",
            englishTranslation = "The Throne Verse",
            revelationType = RevelationType.MEDINAN,
            juzNumber = 3,
            ayahTexts = baqarahAyatKursi
        )

        map
    }

    private fun createSurahWithAyahs(
        number: Int,
        nameArabic: String,
        nameEnglish: String,
        englishTranslation: String,
        revelationType: RevelationType,
        juzNumber: Int,
        ayahTexts: List<String>
    ): QuranSurah {
        var globalPos = 1
        val ayahs = ayahTexts.mapIndexed { index, text ->
            val words = buildWords(number, index + 1, text, globalPos)
            globalPos += words.size
            QuranAyah(
                surahNumber = number,
                ayahNumber = index + 1,
                textUthmani = text,
                textNormalized = ArabicNormalizer.normalizeForRecognition(text),
                words = words
            )
        }
        return QuranSurah(
            number = number,
            nameArabic = nameArabic,
            nameEnglish = nameEnglish,
            englishTranslation = englishTranslation,
            totalAyahs = ayahs.size,
            revelationType = revelationType,
            juzNumber = juzNumber,
            ayahs = ayahs
        )
    }

    /**
     * All 114 Surahs catalog index.
     */
    val ALL_SURAHS_INDEX: List<QuranSurah> = listOf(
        QuranSurah(1, "الفَاتِحَة", "Al-Fatihah", "The Opening", 7, RevelationType.MECCAN, 1),
        QuranSurah(2, "البَقَرَة", "Al-Baqarah", "The Cow", 286, RevelationType.MEDINAN, 1),
        QuranSurah(3, "آلِ عِمْرَان", "Ali 'Imran", "Family of Imran", 200, RevelationType.MEDINAN, 3),
        QuranSurah(4, "النِّسَاء", "An-Nisa", "The Women", 176, RevelationType.MEDINAN, 4),
        QuranSurah(5, "المَائِدَة", "Al-Ma'idah", "The Table Spread", 120, RevelationType.MEDINAN, 6),
        QuranSurah(6, "الأَنْعَام", "Al-An'am", "The Cattle", 165, RevelationType.MECCAN, 7),
        QuranSurah(7, "الأَعْرَاف", "Al-A'raf", "The Heights", 206, RevelationType.MECCAN, 8),
        QuranSurah(8, "الأَنْفَال", "Al-Anfal", "The Spoils of War", 75, RevelationType.MEDINAN, 9),
        QuranSurah(9, "التَّوْبَة", "At-Tawbah", "The Repentance", 129, RevelationType.MEDINAN, 10),
        QuranSurah(10, "يُونُس", "Yunus", "Jonah", 109, RevelationType.MECCAN, 11),
        QuranSurah(11, "هُود", "Hud", "Hud", 123, RevelationType.MECCAN, 11),
        QuranSurah(12, "يُوسُف", "Yusuf", "Joseph", 111, RevelationType.MECCAN, 12),
        QuranSurah(13, "الرَّعْد", "Ar-Ra'd", "The Thunder", 43, RevelationType.MEDINAN, 13),
        QuranSurah(14, "إِبْرَاهِيم", "Ibrahim", "Abraham", 52, RevelationType.MECCAN, 13),
        QuranSurah(15, "الحِجْر", "Al-Hijr", "The Rocky Tract", 99, RevelationType.MECCAN, 14),
        QuranSurah(16, "النَّحْل", "An-Nahl", "The Bee", 128, RevelationType.MECCAN, 14),
        QuranSurah(17, "الإِسْرَاء", "Al-Isra", "The Night Journey", 111, RevelationType.MECCAN, 15),
        QuranSurah(18, "الكَهْف", "Al-Kahf", "The Cave", 110, RevelationType.MECCAN, 15),
        QuranSurah(19, "مَرْيَم", "Maryam", "Mary", 98, RevelationType.MECCAN, 16),
        QuranSurah(20, "طه", "Ta-Ha", "Ta-Ha", 135, RevelationType.MECCAN, 16),
        QuranSurah(21, "الأَنْبِيَاء", "Al-Anbiya", "The Prophets", 112, RevelationType.MECCAN, 17),
        QuranSurah(22, "الحَجّ", "Al-Hajj", "The Pilgrimage", 78, RevelationType.MEDINAN, 17),
        QuranSurah(23, "المُؤْمِنُون", "Al-Mu'minun", "The Believers", 118, RevelationType.MECCAN, 18),
        QuranSurah(24, "النُّور", "An-Nur", "The Light", 64, RevelationType.MEDINAN, 18),
        QuranSurah(25, "الفُرْقَان", "Al-Furqan", "The Criterion", 77, RevelationType.MECCAN, 18),
        QuranSurah(26, "الشُّعَرَاء", "Ash-Shu'ara", "The Poets", 227, RevelationType.MECCAN, 19),
        QuranSurah(27, "النَّمْل", "An-Naml", "The Ant", 93, RevelationType.MECCAN, 19),
        QuranSurah(28, "القَصَص", "Al-Qasas", "The Stories", 88, RevelationType.MECCAN, 20),
        QuranSurah(29, "العَنْكَبُوت", "Al-'Ankabut", "The Spider", 69, RevelationType.MECCAN, 20),
        QuranSurah(30, "الرُّوم", "Ar-Rum", "The Romans", 60, RevelationType.MECCAN, 21),
        QuranSurah(31, "لُقْمَان", "Luqman", "Luqman", 34, RevelationType.MECCAN, 21),
        QuranSurah(32, "السَّجْدَة", "As-Sajdah", "The Prostration", 30, RevelationType.MECCAN, 21),
        QuranSurah(33, "الأَحْزَاب", "Al-Ahzab", "The Combined Forces", 73, RevelationType.MEDINAN, 21),
        QuranSurah(34, "سَبَأ", "Saba", "Sheba", 54, RevelationType.MECCAN, 22),
        QuranSurah(35, "فَاطِر", "Fatir", "Originator", 45, RevelationType.MECCAN, 22),
        QuranSurah(36, "يس", "Ya-Sin", "Ya-Sin", 83, RevelationType.MECCAN, 22),
        QuranSurah(37, "الصَّافَّات", "As-Saffat", "Those who set the Ranks", 182, RevelationType.MECCAN, 23),
        QuranSurah(38, "ص", "Sad", "The Letter Sad", 88, RevelationType.MECCAN, 23),
        QuranSurah(39, "الزُّمَر", "Az-Zumar", "The Troops", 75, RevelationType.MECCAN, 23),
        QuranSurah(40, "غَافِر", "Ghafir", "The Forgiver", 85, RevelationType.MECCAN, 24),
        QuranSurah(41, "فُصِّلَت", "Fussilat", "Explained in Detail", 54, RevelationType.MECCAN, 24),
        QuranSurah(42, "الشُّورَى", "Ash-Shura", "The Consultation", 53, RevelationType.MECCAN, 25),
        QuranSurah(43, "الزُّخْرُف", "Az-Zukhruf", "The Ornaments of Gold", 89, RevelationType.MECCAN, 25),
        QuranSurah(44, "الدُّخَان", "Ad-Dukhan", "The Smoke", 59, RevelationType.MECCAN, 25),
        QuranSurah(45, "الجَاثِيَة", "Al-Jathiyah", "The Crouching", 37, RevelationType.MECCAN, 25),
        QuranSurah(46, "الأَحْقَاف", "Al-Ahqaf", "The Wind-Curved Sandhills", 35, RevelationType.MECCAN, 26),
        QuranSurah(47, "مُحَمَّد", "Muhammad", "Muhammad", 38, RevelationType.MEDINAN, 26),
        QuranSurah(48, "الفَتْح", "Al-Fath", "The Victory", 29, RevelationType.MEDINAN, 26),
        QuranSurah(49, "الحُجُرَات", "Al-Hujurat", "The Rooms", 18, RevelationType.MEDINAN, 26),
        QuranSurah(50, "ق", "Qaf", "The Letter Qaf", 45, RevelationType.MECCAN, 26),
        QuranSurah(51, "الذَّارِيَات", "Adh-Dhariyat", "The Winnowing Winds", 60, RevelationType.MECCAN, 26),
        QuranSurah(52, "الطُّور", "At-Tur", "The Mount", 49, RevelationType.MECCAN, 27),
        QuranSurah(53, "النَّجْم", "An-Najm", "The Star", 62, RevelationType.MECCAN, 27),
        QuranSurah(54, "القَمَر", "Al-Qamar", "The Moon", 55, RevelationType.MECCAN, 27),
        QuranSurah(55, "الرَّحْمَٰن", "Ar-Rahman", "The Beneficent", 78, RevelationType.MEDINAN, 27),
        QuranSurah(56, "الوَاقِعَة", "Al-Waqi'ah", "The Inevitable", 96, RevelationType.MECCAN, 27),
        QuranSurah(57, "الحَدِيد", "Al-Hadid", "The Iron", 29, RevelationType.MEDINAN, 27),
        QuranSurah(58, "المُجَادَلَة", "Al-Mujadila", "The Pleading Woman", 22, RevelationType.MEDINAN, 28),
        QuranSurah(59, "الحَشْر", "Al-Hashr", "The Exile", 24, RevelationType.MEDINAN, 28),
        QuranSurah(60, "المُمْتَحَنَة", "Al-Mumtahanah", "She that is to be examined", 13, RevelationType.MEDINAN, 28),
        QuranSurah(61, "الصَّفّ", "As-Saff", "The Ranks", 14, RevelationType.MEDINAN, 28),
        QuranSurah(62, "الجُمُعَة", "Al-Jumu'ah", "The Congregation", 11, RevelationType.MEDINAN, 28),
        QuranSurah(63, "المُنَافِقُون", "Al-Munafiqun", "The Hypocrites", 11, RevelationType.MEDINAN, 28),
        QuranSurah(64, "التَّغَابُن", "At-Taghabun", "The Mutual Disillusion", 18, RevelationType.MEDINAN, 28),
        QuranSurah(65, "الطَّلَاق", "At-Talaq", "The Divorce", 12, RevelationType.MEDINAN, 28),
        QuranSurah(66, "التَّحْرِيم", "At-Tahrim", "The Prohibition", 12, RevelationType.MEDINAN, 28),
        QuranSurah(67, "المُلْك", "Al-Mulk", "The Sovereignty", 30, RevelationType.MECCAN, 29),
        QuranSurah(68, "القَلَم", "Al-Qalam", "The Pen", 52, RevelationType.MECCAN, 29),
        QuranSurah(69, "الحَاقَّة", "Al-Haqqah", "The Reality", 52, RevelationType.MECCAN, 29),
        QuranSurah(70, "المَعَارِج", "Al-Ma'arij", "The Ascending Stairways", 44, RevelationType.MECCAN, 29),
        QuranSurah(71, "نُوح", "Nuh", "Noah", 28, RevelationType.MECCAN, 29),
        QuranSurah(72, "الجِنّ", "Al-Jinn", "The Jinn", 28, RevelationType.MECCAN, 29),
        QuranSurah(73, "المُزَّمِّل", "Al-Muzzammil", "The Enshrouded One", 20, RevelationType.MECCAN, 29),
        QuranSurah(74, "المُدَّثِّر", "Al-Muddaththir", "The Cloaked One", 56, RevelationType.MECCAN, 29),
        QuranSurah(75, "القِيَامَة", "Al-Qiyamah", "The Resurrection", 40, RevelationType.MECCAN, 29),
        QuranSurah(76, "الإِنْسَان", "Al-Insan", "Man", 31, RevelationType.MEDINAN, 29),
        QuranSurah(77, "المُرْسَلَات", "Al-Mursalat", "The Emissaries", 50, RevelationType.MECCAN, 29),
        QuranSurah(78, "النَّبَأ", "An-Naba", "The Tidings", 40, RevelationType.MECCAN, 30),
        QuranSurah(79, "النَّازِعَات", "An-Nazi'at", "Those who drag forth", 46, RevelationType.MECCAN, 30),
        QuranSurah(80, "عَبَسَ", "'Abasa", "He Frowned", 42, RevelationType.MECCAN, 30),
        QuranSurah(81, "التَّكْوِير", "At-Takwir", "The Overthrowing", 29, RevelationType.MECCAN, 30),
        QuranSurah(82, "الانْفِطَار", "Al-Infitar", "The Cleaving", 19, RevelationType.MECCAN, 30),
        QuranSurah(83, "المُطَفِّفِين", "Al-Mutaffifin", "The Defrauding", 36, RevelationType.MECCAN, 30),
        QuranSurah(84, "الانْشِقَاق", "Al-Inshiqaq", "The Splitting Open", 25, RevelationType.MECCAN, 30),
        QuranSurah(85, "البُرُوج", "Al-Buruj", "The Mansions of the Stars", 22, RevelationType.MECCAN, 30),
        QuranSurah(86, "الطَّارِق", "At-Tariq", "The Morning Star", 17, RevelationType.MECCAN, 30),
        QuranSurah(87, "الأَعْلَى", "Al-A'la", "The Most High", 19, RevelationType.MECCAN, 30),
        QuranSurah(88, "الغَاشِيَة", "Al-Ghashiyah", "The Overwhelming", 26, RevelationType.MECCAN, 30),
        QuranSurah(89, "الفَجْر", "Al-Fajr", "The Dawn", 30, RevelationType.MECCAN, 30),
        QuranSurah(90, "البَلَد", "Al-Balad", "The City", 20, RevelationType.MECCAN, 30),
        QuranSurah(91, "الشَّمْس", "Ash-Shams", "The Sun", 15, RevelationType.MECCAN, 30),
        QuranSurah(92, "اللَّيْل", "Al-Layl", "The Night", 21, RevelationType.MECCAN, 30),
        QuranSurah(93, "الضُّحَى", "Ad-Duha", "The Morning Hours", 11, RevelationType.MECCAN, 30),
        QuranSurah(94, "الشَّرْح", "Ash-Sharh", "The Relief", 8, RevelationType.MECCAN, 30),
        QuranSurah(95, "التِّين", "At-Tin", "The Fig", 8, RevelationType.MECCAN, 30),
        QuranSurah(96, "العَلَق", "Al-'Alaq", "The Clot", 19, RevelationType.MECCAN, 30),
        QuranSurah(97, "القَدْر", "Al-Qadr", "The Power", 5, RevelationType.MECCAN, 30),
        QuranSurah(98, "البَيِّنَة", "Al-Bayyinah", "The Clear Proof", 8, RevelationType.MEDINAN, 30),
        QuranSurah(99, "الزَّلْزَلَة", "Az-Zalzalah", "The Earthquake", 8, RevelationType.MEDINAN, 30),
        QuranSurah(100, "العَادِيَات", "Al-'Adiyat", "The Courser", 11, RevelationType.MECCAN, 30),
        QuranSurah(101, "القَارِعَة", "Al-Qari'ah", "The Calamity", 11, RevelationType.MECCAN, 30),
        QuranSurah(102, "التَّكَاثُر", "At-Takathur", "The Rivalry in World Increase", 8, RevelationType.MECCAN, 30),
        QuranSurah(103, "العَصْر", "Al-'Asr", "The Declining Day", 3, RevelationType.MECCAN, 30),
        QuranSurah(104, "الهُمَزَة", "Al-Humazah", "The Traducer", 9, RevelationType.MECCAN, 30),
        QuranSurah(105, "الفِيل", "Al-Fil", "The Elephant", 5, RevelationType.MECCAN, 30),
        QuranSurah(106, "قُرَيْش", "Quraysh", "Quraysh", 4, RevelationType.MECCAN, 30),
        QuranSurah(107, "المَاعُون", "Al-Ma'un", "The Small Kindnesses", 7, RevelationType.MECCAN, 30),
        QuranSurah(108, "الكَوْثَر", "Al-Kawthar", "The Abundance", 3, RevelationType.MECCAN, 30),
        QuranSurah(109, "الكَافِرُون", "Al-Kafirun", "The Disbelievers", 6, RevelationType.MECCAN, 30),
        QuranSurah(110, "النَّصْر", "An-Nasr", "The Divine Help", 3, RevelationType.MEDINAN, 30),
        QuranSurah(111, "المَسَد", "Al-Masad", "The Palm Fiber", 5, RevelationType.MECCAN, 30),
        QuranSurah(112, "الإِخْلَاص", "Al-Ikhlas", "The Sincerity", 4, RevelationType.MECCAN, 30),
        QuranSurah(113, "الفَلَق", "Al-Falaq", "The Daybreak", 5, RevelationType.MECCAN, 30),
        QuranSurah(114, "النَّاس", "An-Nas", "Mankind", 6, RevelationType.MECCAN, 30)
    )
}
