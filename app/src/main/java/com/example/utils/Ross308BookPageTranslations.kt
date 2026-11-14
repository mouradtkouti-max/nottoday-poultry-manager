package com.example.utils

import com.example.BookSubCardModel
import com.example.models.EncyclopediaPosterSpec
import com.example.models.InfographicAnatomyLabel
import com.example.models.InfographicComparisonPair
import com.example.models.InfographicPerformanceMetric
import com.example.models.InfographicSymptomCard
import com.example.models.InfographicTimelineStep

object Ross308BookPageTranslations {

    val uiKeys: Map<String, Map<String, String>> = mapOf(
        "book_encyclopedia_ribbon_title" to mapOf(
            "ar" to "📖 موسوعة تربية دجاج اللحم",
            "en" to "📖 Broiler Farming Encyclopedia",
            "fr" to "📖 Encyclopédie d'Élevage du Poulet de Chair",
            "es" to "📖 Enciclopedia de Cría de Pollos de Engorde",
            "pt" to "📖 Enciclopédia de Criação de Frangos de Corte",
            "tr" to "📖 Etlik Piliç Yetiştiriciliği Ansiklopedisi",
            "id" to "📖 Ensiklopedia Budidaya Ayam Broiler",
            "hi" to "📖 ब्रॉयलर पालन विश्वकोश",
            "ur" to "📖 برائلر مرغی بانی کا انسائیکلوپیڈیا",
            "sw" to "📖 Ensaiklopidia ya Ufugaji wa Kuku wa Nyama",
            "ru" to "📖 Энциклопедия выращивания бройлеров",
            "zh" to "📖 肉鸡养殖图文百科全书",
            "de" to "📖 Broiler-Aufzucht-Enzyklopädie",
            "it" to "📖 Enciclopedia dell'Allevamento dei Broiler"
        ),
        "book_lesson_number_fmt" to mapOf(
            "ar" to "رقم الدرس {num}",
            "en" to "Lesson No. {num}",
            "fr" to "Leçon N° {num}",
            "es" to "Lección N.° {num}",
            "pt" to "Lição N.º {num}",
            "tr" to "Ders No {num}",
            "id" to "Pelajaran No. {num}",
            "hi" to "पाठ संख्या {num}",
            "ur" to "سبق نمبر {num}",
            "sw" to "Somo Na. {num}",
            "ru" to "Урок № {num}",
            "zh" to "第 {num} 课",
            "de" to "Lektion Nr. {num}",
            "it" to "Lezione N. {num}"
        ),
        "book_page_footer_fmt" to mapOf(
            "ar" to "صفحة {page} من {total}",
            "en" to "Page {page} of {total}",
            "fr" to "Page {page} sur {total}",
            "es" to "Página {page} de {total}",
            "pt" to "Página {page} de {total}",
            "tr" to "Sayfa {page} / {total}",
            "id" to "Halaman {page} dari {total}",
            "hi" to "पृष्ठ {page} / {total}",
            "ur" to "صفحہ {page} از {total}",
            "sw" to "Ukurasa {page} kati ya {total}",
            "ru" to "Страница {page} из {total}",
            "zh" to "第 {page} / {total} 页",
            "de" to "Seite {page} von {total}",
            "it" to "Pagina {page} di {total}"
        ),
        "book_chapter_1_short" to mapOf(
            "ar" to "1. الرعاية 🐣",
            "en" to "1. Care 🐣",
            "fr" to "1. Élevage 🐣",
            "es" to "1. Manejo 🐣",
            "pt" to "1. Manejo 🐣",
            "tr" to "1. Bakım 🐣",
            "id" to "1. Perawatan 🐣",
            "hi" to "1. देखभाल 🐣",
            "ur" to "1. نگہداشت 🐣",
            "sw" to "1. Utunzaji 🐣",
            "ru" to "1. Уход 🐣",
            "zh" to "1. 饲养管理 🐣",
            "de" to "1. Pflege 🐣",
            "it" to "1. Gestione 🐣"
        ),
        "book_chapter_2_short" to mapOf(
            "ar" to "2. التحصينات 💉",
            "en" to "2. Vaccines 💉",
            "fr" to "2. Vaccins 💉",
            "es" to "2. Vacunas 💉",
            "pt" to "2. Vacinas 💉",
            "tr" to "2. Aşılar 💉",
            "id" to "2. Vaksinasi 💉",
            "hi" to "2. टीकाकरण 💉",
            "ur" to "2. ویکسینیشن 💉",
            "sw" to "2. Chanjo 💉",
            "ru" to "2. Вакцины 💉",
            "zh" to "2. 免疫程序 💉",
            "de" to "2. Impfung 💉",
            "it" to "2. Vaccini 💉"
        ),
        "book_chapter_3_short" to mapOf(
            "ar" to "3. الأمراض 🩺",
            "en" to "3. Diseases 🩺",
            "fr" to "3. Maladies 🩺",
            "es" to "3. Enfermedades 🩺",
            "pt" to "3. Doenças 🩺",
            "tr" to "3. Hastalıklar 🩺",
            "id" to "3. Penyakit 🩺",
            "hi" to "3. रोग 🩺",
            "ur" to "3. امراض 🩺",
            "sw" to "3. Magonjwa 🩺",
            "ru" to "3. Болезни 🩺",
            "zh" to "3. 疾病防治 🩺",
            "de" to "3. Krankheiten 🩺",
            "it" to "3. Malattie 🩺"
        ),
        "book_chapter_4_short" to mapOf(
            "ar" to "4. العلف والأوزان 🌾",
            "en" to "4. Feed & Weight 🌾",
            "fr" to "4. Aliment & Poids 🌾",
            "es" to "4. Pienso y Peso 🌾",
            "pt" to "4. Ração e Peso 🌾",
            "tr" to "4. Yem ve Ağırlık 🌾",
            "id" to "4. Pakan & Bobot 🌾",
            "hi" to "4. दाना व वजन 🌾",
            "ur" to "4. خوراک اور وزن 🌾",
            "sw" to "4. Chakula & Uzito 🌾",
            "ru" to "4. Корм и вес 🌾",
            "zh" to "4. 饲料与体重 🌾",
            "de" to "4. Futter & Gewicht 🌾",
            "it" to "4. Mangime e Peso 🌾"
        ),
        "book_branch_0_title" to mapOf(
            "ar" to "1. برنامج الرعاية 🐣",
            "en" to "1. Brooding & Care Program 🐣",
            "fr" to "1. Programme d'Élevage & Soins 🐣",
            "es" to "1. Programa de Cría y Manejo 🐣",
            "pt" to "1. Programa de Manejo Inicial 🐣",
            "tr" to "1. Bakım ve İklimlendirme 🐣",
            "id" to "1. Program Pemeliharaan 🐣",
            "hi" to "1. ब्रूडिंग और देखभाल कार्यक्रम 🐣",
            "ur" to "1. نگہداشت اور بروڈنگ پروگرام 🐣",
            "sw" to "1. Mpango wa Utunzaji 🐣",
            "ru" to "1. Программа ухода и микроклимата 🐣",
            "zh" to "1. 饲养与环控方案 🐣",
            "de" to "1. Aufzucht- & Pflegeprogramm 🐣",
            "it" to "1. Programma di Allevamento 🐣"
        ),
        "book_branch_0_subtitle" to mapOf(
            "ar" to "(الحرارة، التهوية، ومراحل النمو)",
            "en" to "(Heat, ventilation & growth stages)",
            "fr" to "(Chaleur, ventilation et croissance)",
            "es" to "(Calor, ventilación y etapas)",
            "pt" to "(Calor, ventilação e crescimento)",
            "tr" to "(Isı, havalandırma ve büyüme)",
            "id" to "(Suhu, ventilasi & fase tumbuh)",
            "hi" to "(तापमान, वेंटिलेशन और विकास)",
            "ur" to "(حرارت، ہوا داری اور نشوونما)",
            "sw" to "(Joto, hewa na hatua za ukuaji)",
            "ru" to "(Температура, вентиляция и рост)",
            "zh" to "(温度、通风与生长阶段)",
            "de" to "(Wärme, Lüftung & Wachstum)",
            "it" to "(Calore, ventilazione e crescita)"
        ),
        "book_branch_1_title" to mapOf(
            "ar" to "2. برنامج التحصينات 💉",
            "en" to "2. Vaccination Program 💉",
            "fr" to "2. Programme de Vaccination 💉",
            "es" to "2. Programa de Vacunación 💉",
            "pt" to "2. Programa de Vacinação 💉",
            "tr" to "2. Aşılama Programı 💉",
            "id" to "2. Program Vaksinasi 💉",
            "hi" to "2. टीकाकरण कार्यक्रम 💉",
            "ur" to "2. ویکسینیشن پروگرام 💉",
            "sw" to "2. Mpango wa Chanjo 💉",
            "ru" to "2. Программа вакцинации 💉",
            "zh" to "2. 免疫接种方案 💉",
            "de" to "2. Impfprogramm 💉",
            "it" to "2. Programma Vaccinale 💉"
        ),
        "book_branch_1_subtitle" to mapOf(
            "ar" to "(جدول اللقاحات والفيتامينات)",
            "en" to "(Vaccines, electrolytes & vitamins)",
            "fr" to "(Calendrier vaccinal et vitamines)",
            "es" to "(Calendario de vacunas y vitaminas)",
            "pt" to "(Calendário de vacinas e vitaminas)",
            "tr" to "(Aşı takvimi ve vitaminler)",
            "id" to "(Jadwal vaksin & vitamin)",
            "hi" to "(टीकाकरण तालिका और विटामिन)",
            "ur" to "(ویکسین شیڈول اور وٹامنز)",
            "sw" to "(Ratiba ya chanjo na vitamini)",
            "ru" to "(График вакцин и витаминов)",
            "zh" to "(疫苗日历与维生素方案)",
            "de" to "(Impfplan & Vitamine)",
            "it" to "(Calendario vaccini e vitamine)"
        ),
        "book_branch_2_title" to mapOf(
            "ar" to "3. الأمراض والوقاية 🩺",
            "en" to "3. Diseases & Biosecurity 🩺",
            "fr" to "3. Maladies & Biosécurité 🩺",
            "es" to "3. Enfermedades y Prevención 🩺",
            "pt" to "3. Doenças e Biossegurança 🩺",
            "tr" to "3. Hastalıklar ve Korunma 🩺",
            "id" to "3. Penyakit & Pencegahan 🩺",
            "hi" to "3. रोग और जैव सुरक्षा 🩺",
            "ur" to "3. امراض اور بائیو سیکیورٹی 🩺",
            "sw" to "3. Magonjwa na Kinga 🩺",
            "ru" to "3. Болезни и биозащита 🩺",
            "zh" to "3. 疾病诊断与生物安全 🩺",
            "de" to "3. Krankheiten & Biosicherheit 🩺",
            "it" to "3. Malattie e Biosicurezza 🩺"
        ),
        "book_branch_2_subtitle" to mapOf(
            "ar" to "(الأعراض والأمن الحيوي)",
            "en" to "(Symptoms, anatomy & biosecurity)",
            "fr" to "(Symptômes et biosécurité)",
            "es" to "(Síntomas y bioseguridad)",
            "pt" to "(Sintomas e biossegurança)",
            "tr" to "(Belirtiler ve biyogüvenlik)",
            "id" to "(Gejala klinis & biosekuriti)",
            "hi" to "(लक्षण और बायोसिक्योरिटी)",
            "ur" to "(علامات اور احتیاطی تدابیر)",
            "sw" to "(Dalili na usalama wa viumbe)",
            "ru" to "(Симптомы и профилактика)",
            "zh" to "(临床症状与防疫措施)",
            "de" to "(Symptome & Prävention)",
            "it" to "(Sintomi e prevenzione)"
        ),
        "book_branch_3_title" to mapOf(
            "ar" to "4. برنامج العلف والأوزان 🌾",
            "en" to "4. Feed & Weight Program 🌾",
            "fr" to "4. Programme Aliment & Poids 🌾",
            "es" to "4. Programa de Pienso y Peso 🌾",
            "pt" to "4. Programa de Ração e Peso 🌾",
            "tr" to "4. Yem ve Ağırlık Programı 🌾",
            "id" to "4. Program Pakan & Bobot 🌾",
            "hi" to "4. दाना और वजन कार्यक्रम 🌾",
            "ur" to "4. خوراک اور وزن کا پروگرام 🌾",
            "sw" to "4. Mpango wa Chakula na Uzito 🌾",
            "ru" to "4. Программа кормления и веса 🌾",
            "zh" to "4. 饲喂阶段与体重曲线 🌾",
            "de" to "4. Futter- & Gewichtsprogramm 🌾",
            "it" to "4. Programma Mangime e Peso 🌾"
        ),
        "book_branch_3_subtitle" to mapOf(
            "ar" to "(مراحل التغذية والتحويل)",
            "en" to "(Feeding phases, FCR & water curve)",
            "fr" to "(Phases d'aliment et indice IC)",
            "es" to "(Fases de nutrición y conversión)",
            "pt" to "(Fases de nutrição e conversão)",
            "tr" to "(Besleme evreleri ve FCR)",
            "id" to "(Fase pakan & konversi FCR)",
            "hi" to "(पोषण चरण और FCR रूपांतरण)",
            "ur" to "(غذائی مراحل اور ایف سی آر)",
            "sw" to "(Hatua za lishe na uwiano FCR)",
            "ru" to "(Фазы откорма и конверсия FCR)",
            "zh" to "(饲喂阶段与料肉比 FCR)",
            "de" to "(Futterphasen & FCR)",
            "it" to "(Fasi nutrizionali e FCR)"
        ),
        "book_branch_badge_4_lessons" to mapOf(
            "ar" to "📖 كتاب مستقل: الدروس 01–04",
            "en" to "📖 Book: Lessons 01–04",
            "fr" to "📖 Livre : Leçons 01–04",
            "es" to "📖 Libro: Lecciones 01–04",
            "pt" to "📖 Livro: Lições 01–04",
            "tr" to "📖 Kitap: Dersler 01–04",
            "id" to "📖 Buku: Pelajaran 01–04",
            "hi" to "📖 पुस्तक: पाठ 01–04",
            "ur" to "📖 مستقل کتاب: اسباق 01–04",
            "sw" to "📖 Kitabu: Masomo 01–04",
            "ru" to "📖 Книга: Уроки 01–04",
            "zh" to "📖 独立分册：第 01–04 课",
            "de" to "📖 Buch: Lektionen 01–04",
            "it" to "📖 Libro: Lezioni 01–04"
        ),
        "book_chick_crop_yolk" to mapOf(
            "ar" to "الحوصلة وكيس المح",
            "en" to "Crop & Yolk Sac",
            "fr" to "Jabot & Sac vitellin",
            "es" to "Buche y Saco vitelino",
            "pt" to "Papo e Saco vitelino",
            "tr" to "Kursak ve Sarı Kesesi",
            "id" to "Tembolok & Kantung Kuning",
            "hi" to "क्रॉप और जर्दी थैली",
            "ur" to "پوٹا اور زردی کی تھیلی",
            "sw" to "Finye na Mfuko wa Kiini",
            "ru" to "Зоб и желточный мешок",
            "zh" to "嗉囊与卵黄囊",
            "de" to "Kropf & Dottersack",
            "it" to "Gozzo e Sacco vitellino"
        ),
        "book_btn_prev_lesson" to mapOf(
            "ar" to "⬅ الدرس السابق",
            "en" to "⬅ Previous Lesson",
            "fr" to "⬅ Leçon précédente",
            "es" to "⬅ Lección anterior",
            "pt" to "⬅ Lição anterior",
            "tr" to "⬅ Önceki Ders",
            "id" to "⬅ Pelajaran Sebelumnya",
            "hi" to "⬅ पिछला पाठ",
            "ur" to "⬅ پچھلا سبق",
            "sw" to "⬅ Somo Lililopita",
            "ru" to "⬅ Предыдущий урок",
            "zh" to "⬅ 上一课",
            "de" to "⬅ Vorherige Lektion",
            "it" to "⬅ Lezione precedente"
        ),
        "book_btn_zoom_page" to mapOf(
            "ar" to "🔍 تكبير الصفحة",
            "en" to "🔍 Zoom Page",
            "fr" to "🔍 Agrandir la page",
            "es" to "🔍 Ampliar página",
            "pt" to "🔍 Ampliar página",
            "tr" to "🔍 Sayfayı Büyüt",
            "id" to "🔍 Perbesar Halaman",
            "hi" to "🔍 पृष्ठ बड़ा करें",
            "ur" to "🔍 صفحہ بڑا کریں",
            "sw" to "🔍 Kuza Ukurasa",
            "ru" to "🔍 Увеличить страницу",
            "zh" to "🔍 放大书页",
            "de" to "🔍 Seite vergrößern",
            "it" to "🔍 Ingrandisci pagina"
        ),
        "book_btn_next_lesson" to mapOf(
            "ar" to "الدرس التالي ➔",
            "en" to "Next Lesson ➔",
            "fr" to "Leçon suivante ➔",
            "es" to "Siguiente lección ➔",
            "pt" to "Próxima lição ➔",
            "tr" to "Sonraki Ders ➔",
            "id" to "Pelajaran Berikutnya ➔",
            "hi" to "अगला पाठ ➔",
            "ur" to "اگلا سبق ➔",
            "sw" to "Somo Lijalo ➔",
            "ru" to "Следующий урок ➔",
            "zh" to "下一课 ➔",
            "de" to "Nächste Lektion ➔",
            "it" to "Lezione successiva ➔"
        ),
        "book_ribbon_core_idea" to mapOf(
            "ar" to "🎯 الفكرة الأساسية",
            "en" to "🎯 Core Idea",
            "fr" to "🎯 Idée principale",
            "es" to "🎯 Idea principal",
            "pt" to "🎯 Ideia principal",
            "tr" to "🎯 Temel Fikir",
            "id" to "🎯 Ide Utama",
            "hi" to "🎯 मुख्य विचार",
            "ur" to "🎯 بنیادی نقطہ",
            "sw" to "🎯 Wazo Kuu",
            "ru" to "🎯 Основная идея",
            "zh" to "🎯 核心要点",
            "de" to "🎯 Kernidee",
            "it" to "🎯 Idea principale"
        ),
        "book_ribbon_why_important" to mapOf(
            "ar" to "❓ لماذا هو مهم؟",
            "en" to "❓ Why Is It Important?",
            "fr" to "❓ Pourquoi est-ce important ?",
            "es" to "❓ ¿Por qué es importante?",
            "pt" to "❓ Por que é importante?",
            "tr" to "❓ Neden Önemlidir?",
            "id" to "❓ Mengapa Penting?",
            "hi" to "❓ यह क्यों महत्वपूर्ण है?",
            "ur" to "❓ یہ کیوں اہم ہے؟",
            "sw" to "❓ Kwa Nini Ni Muhimu?",
            "ru" to "❓ Почему это важно?",
            "zh" to "❓ 为什么至关重要？",
            "de" to "❓ Warum ist das wichtig?",
            "it" to "❓ Perché è importante?"
        ),
        "book_ribbon_how_to_prepare" to mapOf(
            "ar" to "⚙️ كيف أجهّز العنبر؟",
            "en" to "⚙️ How to Prepare the Barn?",
            "fr" to "⚙️ Comment préparer le bâtiment ?",
            "es" to "⚙️ ¿Cómo preparar el galpón?",
            "pt" to "⚙️ Como preparar o galpão?",
            "tr" to "⚙️ Kümesi Nasıl Hazırlarım?",
            "id" to "⚙️ Cara Menyiapkan Kandang?",
            "hi" to "⚙️ शेड कैसे तैयार करें?",
            "ur" to "⚙️ شیڈ کیسے تیار کریں؟",
            "sw" to "⚙️ Jinsi ya Kuandaa Banda?",
            "ru" to "⚙️ Как подготовить птичник?",
            "zh" to "⚙️ 实操步骤指南",
            "de" to "⚙️ Wie bereite ich den Stall vor?",
            "it" to "⚙️ Come preparare il capannone?"
        ),
        "book_ribbon_avoid_mistake" to mapOf(
            "ar" to "❌ تجنّب هذا الخطأ",
            "en" to "❌ Avoid This Mistake",
            "fr" to "❌ Évitez cette erreur",
            "es" to "❌ Evite este error",
            "pt" to "❌ Evite este erro",
            "tr" to "❌ Bu Hatadan Kaçının",
            "id" to "❌ Hindari Kesalahan Ini",
            "hi" to "❌ इस गलती से बचें",
            "ur" to "❌ اس غلطی سے بچیں",
            "sw" to "❌ Epuka Kosa Hili",
            "ru" to "❌ Избегайте этой ошибки",
            "zh" to "❌ 避免此错误",
            "de" to "❌ Diesen Fehler vermeiden",
            "it" to "❌ Evita questo errore"
        ),
        "book_ribbon_chick_anatomy" to mapOf(
            "ar" to "🐣 رسم تشريحي للصوص",
            "en" to "🐣 Chick Internal Anatomy",
            "fr" to "🐣 Anatomie interne du poussin",
            "es" to "🐣 Anatomía interna del pollito",
            "pt" to "🐣 Anatomia interna do pintinho",
            "tr" to "🐣 Civciv İç Anatomisi",
            "id" to "🐣 Anatomi Internal Anak Ayam",
            "hi" to "🐣 चूजे की आंतरिक संरचना",
            "ur" to "🐣 چوزے کی اندرونی تشریح",
            "sw" to "🐣 Anatomia ya Kifaranga",
            "ru" to "🐣 Анатомия цыпленка",
            "zh" to "🐣 雏鸡内部解剖图",
            "de" to "🐣 Küken-Anatomie",
            "it" to "🐣 Anatomia del pulcino"
        ),
        "book_ribbon_readiness_signs" to mapOf(
            "ar" to "✅ علامات الجاهزية",
            "en" to "✅ Signs of Readiness",
            "fr" to "✅ Signes de préparation",
            "es" to "✅ Señales de preparación",
            "pt" to "✅ Sinais de prontidão",
            "tr" to "✅ Hazırlık İşaretleri",
            "id" to "✅ Tanda Kesiapan",
            "hi" to "✅ तैयारी के संकेत",
            "ur" to "✅ تیاری کی علامات",
            "sw" to "✅ Dalili za Utayari",
            "ru" to "✅ Признаки готовности",
            "zh" to "✅ 达标特征",
            "de" to "✅ Zeichen der Bereitschaft",
            "it" to "✅ Segni di prontezza"
        ),
        "book_ribbon_disease_def" to mapOf(
            "ar" to "ℹ️ تعريف المرض",
            "en" to "ℹ️ Disease Definition",
            "fr" to "ℹ️ Définition de la maladie",
            "es" to "ℹ️ Definición de la enfermedad",
            "pt" to "ℹ️ Definição da doença",
            "tr" to "ℹ️ Hastalık Tanımı",
            "id" to "ℹ️ Definisi Penyakit",
            "hi" to "ℹ️ रोग की परिभाषा",
            "ur" to "ℹ️ مرض کی تعریف",
            "sw" to "ℹ️ Ufafanuzi wa Ugonjwa",
            "ru" to "ℹ️ Определение болезни",
            "zh" to "ℹ️ 疾病定义",
            "de" to "ℹ️ Krankheitsdefinition",
            "it" to "ℹ️ Definizione della malattia"
        ),
        "book_ribbon_quick_facts" to mapOf(
            "ar" to "معلومات سريعة",
            "en" to "Quick Facts",
            "fr" to "Infos rapides",
            "es" to "Datos rápidos",
            "pt" to "Dados rápidos",
            "tr" to "Hızlı Bilgiler",
            "id" to "Info Cepat",
            "hi" to "त्वरित जानकारी",
            "ur" to "فوری معلومات",
            "sw" to "Taarifa Fupi",
            "ru" to "Краткие сведения",
            "zh" to "速览指标",
            "de" to "Kurzinfos",
            "it" to "Dati rapidi"
        ),
        "book_ribbon_infection_sites" to mapOf(
            "ar" to "مواقع الإصابة",
            "en" to "Infection Sites",
            "fr" to "Sites d'infection",
            "es" to "Sitios de infección",
            "pt" to "Locais de infecção",
            "tr" to "Enfeksiyon Bölgeleri",
            "id" to "Lokasi Infeksi",
            "hi" to "संक्रमण के स्थान",
            "ur" to "متاثرہ اعضا",
            "sw" to "Maeneo ya Maambukizi",
            "ru" to "Очаги поражения",
            "zh" to "感染部位解剖",
            "de" to "Infektionsstellen",
            "it" to "Siti di infezione"
        ),
        "book_ribbon_main_symptoms" to mapOf(
            "ar" to "🩺 الأعراض الرئيسية",
            "en" to "🩺 Main Symptoms",
            "fr" to "🩺 Symptômes principaux",
            "es" to "🩺 Síntomas principales",
            "pt" to "🩺 Sintomas principais",
            "tr" to "🩺 Başlıca Belirtiler",
            "id" to "🩺 Gejala Utama",
            "hi" to "🩺 मुख्य लक्षण",
            "ur" to "🩺 اہم علامات",
            "sw" to "🩺 Dalili Kuu",
            "ru" to "🩺 Основные симптомы",
            "zh" to "🩺 主要临床症状",
            "de" to "🩺 Hauptsymptome",
            "it" to "🩺 Sintomi principali"
        ),
        "book_ribbon_treatment" to mapOf(
            "ar" to "➕ العلاج والتعامل",
            "en" to "➕ Treatment & Care",
            "fr" to "➕ Traitement et conduite",
            "es" to "➕ Tratamiento y manejo",
            "pt" to "➕ Tratamento e manejo",
            "tr" to "➕ Tedavi ve Müdahale",
            "id" to "➕ Pengobatan & Penanganan",
            "hi" to "➕ उपचार और प्रबंधन",
            "ur" to "➕ علاج اور نگہداشت",
            "sw" to "➕ Matibabu na Utunzaji",
            "ru" to "➕ Лечение и уход",
            "zh" to "➕ 治疗与护理",
            "de" to "➕ Behandlung & Pflege",
            "it" to "➕ Trattamento e gestione"
        ),
        "book_ribbon_performance_impact" to mapOf(
            "ar" to "📊 التأثير على الأداء",
            "en" to "📊 Performance Impact",
            "fr" to "📊 Impact sur la performance",
            "es" to "📊 Impacto en el rendimiento",
            "pt" to "📊 Impacto no desempenho",
            "tr" to "📊 Performansa Etkisi",
            "id" to "📊 Dampak pada Performa",
            "hi" to "📊 प्रदर्शन पर प्रभाव",
            "ur" to "📊 کارکردگی پر اثر",
            "sw" to "📊 Athari kwenye Utendaji",
            "ru" to "📊 Влияние на показатели",
            "zh" to "📊 对生产性能的影响",
            "de" to "📊 Leistungsauswirkung",
            "it" to "📊 Impatto sulle prestazioni"
        ),
        "book_ribbon_diagnosis" to mapOf(
            "ar" to "🔬 التشخيص",
            "en" to "🔬 Diagnosis",
            "fr" to "🔬 Diagnostic",
            "es" to "🔬 Diagnóstico",
            "pt" to "🔬 Diagnóstico",
            "tr" to "🔬 Teşhis",
            "id" to "🔬 Diagnosis",
            "hi" to "🔬 निदान",
            "ur" to "🔬 تشخیص",
            "sw" to "🔬 Utambuzi",
            "ru" to "🔬 Диагностика",
            "zh" to "🔬 诊断方法",
            "de" to "🔬 Diagnose",
            "it" to "🔬 Diagnosi"
        ),
        "book_ribbon_prevention" to mapOf(
            "ar" to "🛡️ الوقاية",
            "en" to "🛡️ Prevention",
            "fr" to "🛡️ Prévention",
            "es" to "🛡️ Prevención",
            "pt" to "🛡️ Prevenção",
            "tr" to "🛡️ Korunma",
            "id" to "🛡️ Pencegahan",
            "hi" to "🛡️ रोकथाम",
            "ur" to "🛡️ احتیاطی تدابیر",
            "sw" to "🛡️ Kinga",
            "ru" to "🛡️ Профилактика",
            "zh" to "🛡️ 预防措施",
            "de" to "🛡️ Prävention",
            "it" to "🛡️ Prevenzione"
        ),
        "book_ribbon_important_note" to mapOf(
            "ar" to "⚠️ ملاحظة مهمة",
            "en" to "⚠️ Important Note",
            "fr" to "⚠️ Note importante",
            "es" to "⚠️ Nota importante",
            "pt" to "⚠️ Nota importante",
            "tr" to "⚠️ Önemli Not",
            "id" to "⚠️ Catatan Penting",
            "hi" to "⚠️ महत्वपूर्ण नोट",
            "ur" to "⚠️ اہم نوٹ",
            "sw" to "⚠️ Kumbuka Muhimu",
            "ru" to "⚠️ Важное примечание",
            "zh" to "⚠️ 重要提示",
            "de" to "⚠️ Wichtiger Hinweis",
            "it" to "⚠️ Nota importante"
        ),
        "book_chick_trachea" to mapOf(
            "ar" to "القصبة الهوائية",
            "en" to "Trachea",
            "fr" to "Trachée",
            "es" to "Tráquea",
            "pt" to "Traqueia",
            "tr" to "Soluk Borusu",
            "id" to "Trakea",
            "hi" to "श्वास नली",
            "ur" to "سانس کی نالی",
            "sw" to "Koo la Hewa",
            "ru" to "Трахея",
            "zh" to "气管",
            "de" to "Luftröhre",
            "it" to "Trachea"
        ),
        "book_chick_lungs" to mapOf(
            "ar" to "الرئتان",
            "en" to "Lungs",
            "fr" to "Poumons",
            "es" to "Pulmones",
            "pt" to "Pulmões",
            "tr" to "Akciğerler",
            "id" to "Paru-paru",
            "hi" to "फेफड़े",
            "ur" to "پھیپھڑے",
            "sw" to "Mapafu",
            "ru" to "Легкие",
            "zh" to "肺部",
            "de" to "Lunge",
            "it" to "Polmoni"
        ),
        "book_chick_digestive" to mapOf(
            "ar" to "الجهاز الهضمي",
            "en" to "Digestive Tract",
            "fr" to "Système digestif",
            "es" to "Aparato digestivo",
            "pt" to "Sistema digestivo",
            "tr" to "Sindirim Sistemi",
            "id" to "Saluran Cerna",
            "hi" to "पाचन तंत्र",
            "ur" to "نظامِ ہضم",
            "sw" to "Mfumo wa Mmeng'enyo",
            "ru" to "Пищеварение",
            "zh" to "消化道",
            "de" to "Verdauungstrakt",
            "it" to "Apparato digerente"
        ),
        "book_analytics_expenses_body_fmt" to mapOf(
            "ar" to "• إجمالي المصاريف المسجلة: {exp} {curr} | المداخيل: {inc} {curr}\n• التكلفة الفعلية للطائر الحي: {cpb} {curr}\n• تكلفة إنتاج 1 {kg}: {cpkg} {curr} / {kg}",
            "en" to "• Total Recorded Expenses: {exp} {curr} | Income: {inc} {curr}\n• Actual Cost per Live Bird: {cpb} {curr}\n• Production Cost per 1 {kg}: {cpkg} {curr} / {kg}",
            "fr" to "• Dépenses totales : {exp} {curr} | Revenus : {inc} {curr}\n• Coût réel par sujet vif : {cpb} {curr}\n• Coût de production d'1 {kg} : {cpkg} {curr} / {kg}",
            "es" to "• Gastos totales: {exp} {curr} | Ingresos: {inc} {curr}\n• Costo real por ave viva: {cpb} {curr}\n• Costo de producir 1 {kg}: {cpkg} {curr} / {kg}",
            "pt" to "• Despesas totais: {exp} {curr} | Receitas: {inc} {curr}\n• Custo real por ave viva: {cpb} {curr}\n• Custo de produção de 1 {kg}: {cpkg} {curr} / {kg}",
            "tr" to "• Toplam Gider: {exp} {curr} | Gelir: {inc} {curr}\n• Canlı Piliç Başına Maliyet: {cpb} {curr}\n• 1 {kg} Canlı Ağırlık Maliyeti: {cpkg} {curr} / {kg}",
            "id" to "• Total Pengeluaran: {exp} {curr} | Pendapatan: {inc} {curr}\n• Biaya Aktual per Ekor Hidup: {cpb} {curr}\n• Biaya Produksi 1 {kg}: {cpkg} {curr} / {kg}",
            "hi" to "• कुल दर्ज खर्च: {exp} {curr} | आय: {inc} {curr}\n• प्रति जीवित पक्षी लागत: {cpb} {curr}\n• 1 {kg} उत्पादन लागत: {cpkg} {curr} / {kg}",
            "ur" to "• کل اخراجات: {exp} {curr} | آمدنی: {inc} {curr}\n• فی زندہ پرندہ لاگت: {cpb} {curr}\n• 1 {kg} پیداواری لاگت: {cpkg} {curr} / {kg}",
            "sw" to "• Jumla ya Matumizi: {exp} {curr} | Mapato: {inc} {curr}\n• Gharama Halisi kwa Kuku Hai: {cpb} {curr}\n• Gharama ya Kuzalisha 1 {kg}: {cpkg} {curr} / {kg}",
            "ru" to "• Всего расходов: {exp} {curr} | Доход: {inc} {curr}\n• Себестоимость 1 головы: {cpb} {curr}\n• Себестоимость 1 {kg}: {cpkg} {curr} / {kg}",
            "zh" to "• 已记录总支出：{exp} {curr} | 收入：{inc} {curr}\n• 单只活鸡实际成本：{cpb} {curr}\n• 每 1 {kg} 活重生产成本：{cpkg} {curr} / {kg}",
            "de" to "• Gesamtausgaben: {exp} {curr} | Einnahmen: {inc} {curr}\n• Ist-Kosten pro Tier: {cpb} {curr}\n• Produktionskosten pro 1 {kg}: {cpkg} {curr} / {kg}",
            "it" to "• Spese totali: {exp} {curr} | Entrate: {inc} {curr}\n• Costo reale per capo vivo: {cpb} {curr}\n• Costo di produzione per 1 {kg}: {cpkg} {curr} / {kg}"
        ),
        "book_analytics_feed_body_fmt" to mapOf(
            "ar" to "• إجمالي العلف المستهلك: {used} {q} | المخزون المتبقي: {rem} {q}\n• متوسط الوزن الحالي: {wt} {kg} (معيار Ross 308 لليوم {day}: {targetWt} {kg})\n• معامل التحويل الغذائي (FCR): {fcr} (الهدف القياسي: {targetFcr})",
            "en" to "• Total Feed Consumed: {used} {q} | Remaining Stock: {rem} {q}\n• Current Avg Weight: {wt} {kg} (Ross 308 Day {day} target: {targetWt} {kg})\n• Feed Conversion Ratio (FCR): {fcr} (Standard target: {targetFcr})",
            "fr" to "• Aliment consommé : {used} {q} | Stock restant : {rem} {q}\n• Poids moyen actuel : {wt} {kg} (Cible Ross 308 J{day} : {targetWt} {kg})\n• Indice de consommation (FCR) : {fcr} (Cible : {targetFcr})",
            "es" to "• Alimento consumido: {used} {q} | Stock restante: {rem} {q}\n• Peso medio actual: {wt} {kg} (Meta Ross 308 Día {day}: {targetWt} {kg})\n• Conversión alimenticia (FCR): {fcr} (Meta: {targetFcr})",
            "pt" to "• Ração consumida: {used} {q} | Estoque restante: {rem} {q}\n• Peso médio atual: {wt} {kg} (Meta Ross 308 Dia {day}: {targetWt} {kg})\n• Conversão alimentar (FCR): {fcr} (Meta: {targetFcr})",
            "tr" to "• Tüketilen Yem: {used} {q} | Kalan Stok: {rem} {q}\n• Mevcut Ortalama Ağırlık: {wt} {kg} (Ross 308 Gün {day} hedef: {targetWt} {kg})\n• Yemden Yararlanma (FCR): {fcr} (Hedef: {targetFcr})",
            "id" to "• Total Pakan Terpakai: {used} {q} | Sisa Stok: {rem} {q}\n• Bobot Rata-rata: {wt} {kg} (Target Ross 308 Hari {day}: {targetWt} {kg})\n• Konversi Pakan (FCR): {fcr} (Target: {targetFcr})",
            "hi" to "• कुल खपत दाना: {used} {q} | शेष स्टॉक: {rem} {q}\n• वर्तमान औसत वजन: {wt} {kg} (Ross 308 दिन {day} लक्ष्य: {targetWt} {kg})\n• फ़ीड रूपांतरण (FCR): {fcr} (मानक लक्ष्य: {targetFcr})",
            "ur" to "• کل استعمال شدہ خوراک: {used} {q} | بقیہ اسٹاک: {rem} {q}\n• موجودہ اوسط وزن: {wt} {kg} (Ross 308 دن {day} ہدف: {targetWt} {kg})\n• ایف سی آر (FCR): {fcr} (ہدف: {targetFcr})",
            "sw" to "• Chakula Kilichotumika: {used} {q} | Akiba Iliyobaki: {rem} {q}\n• Uzito wa Sasa: {wt} {kg} (Lengo la Ross 308 Siku {day}: {targetWt} {kg})\n• Uwiano wa FCR: {fcr} (Lengo: {targetFcr})",
            "ru" to "• Израсходовано корма: {used} {q} | Остаток: {rem} {q}\n• Текущий вес: {wt} {kg} (Норма Ross 308 день {day}: {targetWt} {kg})\n• Конверсия корма (FCR): {fcr} (Норма: {targetFcr})",
            "zh" to "• 累计耗料：{used} {q} | 剩余库存：{rem} {q}\n• 当前均重：{wt} {kg}（Ross 308 第 {day} 天标准：{targetWt} {kg}）\n• 料肉比 (FCR)：{fcr}（标准目标：{targetFcr}）",
            "de" to "• Futterverbrauch: {used} {q} | Restbestand: {rem} {q}\n• Aktuelles Gewicht: {wt} {kg} (Ross 308 Tag {day} Ziel: {targetWt} {kg})\n• Futterverwertung (FCR): {fcr} (Ziel: {targetFcr})",
            "it" to "• Mangime consumato: {used} {q} | Scorta residua: {rem} {q}\n• Peso medio attuale: {wt} {kg} (Target Ross 308 Giorno {day}: {targetWt} {kg})\n• Indice di conversione (FCR): {fcr} (Target: {targetFcr})"
        ),
        "book_analytics_vitality_body_fmt" to mapOf(
            "ar" to "• الطيور الحية: {live} من أصل {placed} (اليوم {day})\n• نسبة الحيوية: {livPct}% | النفوق التراكمي: {mort} ({mortPct}% — المعيار الآمن < 3.5%)\n• حرارة العنبر الحالية: {temp}",
            "en" to "• Live Birds: {live} of {placed} (Day {day})\n• Livability: {livPct}% | Cumulative Mortality: {mort} ({mortPct}% — Safe < 3.5%)\n• Current Barn Temperature: {temp}",
            "fr" to "• Sujets vifs : {live} sur {placed} (Jour {day})\n• Viabilité : {livPct}% | Mortalité cumulée : {mort} ({mortPct}% — Norme < 3.5%)\n• Température actuelle : {temp}",
            "es" to "• Aves vivas: {live} de {placed} (Día {day})\n• Viabilidad: {livPct}% | Mortalidad acumulada: {mort} ({mortPct}% — Seguro < 3.5%)\n• Temperatura actual: {temp}",
            "pt" to "• Aves vivas: {live} de {placed} (Dia {day})\n• Viabilidade: {livPct}% | Mortalidade acumulada: {mort} ({mortPct}% — Seguro < 3.5%)\n• Temperatura atual: {temp}",
            "tr" to "• Canlı Sürü: {live} / {placed} (Gün {day})\n• Yaşama Gücü: %{livPct} | Toplam Ölüm: {mort} (%{mortPct} — Güvenli < %3.5)\n• Kümes Sıcaklığı: {temp}",
            "id" to "• Ayam Hidup: {live} dari {placed} (Hari {day})\n• Daya Hidup: {livPct}% | Mortalitas: {mort} ({mortPct}% — Aman < 3.5%)\n• Suhu Kandang Saat Ini: {temp}",
            "hi" to "• जीवित पक्षी: {live} / {placed} (दिन {day})\n• जीवित दर: {livPct}% | कुल मृत्यु: {mort} ({mortPct}% — सुरक्षित < 3.5%)\n• वर्तमान शेड तापमान: {temp}",
            "ur" to "• زندہ پرندے: {live} از {placed} (دن {day})\n• شرح بقا: {livPct}% | کل اموات: {mort} ({mortPct}% — محفوظ < 3.5%)\n• شیڈ کا موجودہ درجہ حرارت: {temp}",
            "sw" to "• Kuku Hai: {live} kati ya {placed} (Siku {day})\n• Uhai: {livPct}% | Vifo: {mort} ({mortPct}% — Salama < 3.5%)\n• Joto la Banda Sasa: {temp}",
            "ru" to "• Живое поголовье: {live} из {placed} (День {day})\n• Сохранность: {livPct}% | Падеж: {mort} ({mortPct}% — Норма < 3.5%)\n• Температура в птичнике: {temp}",
            "zh" to "• 存栏活鸡：{live} / {placed}（第 {day} 天）\n• 成活率：{livPct}% | 累计死淘：{mort}（{mortPct}% — 安全线 < 3.5%）\n• 当前舍温：{temp}",
            "de" to "• Lebende Tiere: {live} von {placed} (Tag {day})\n• Vitalität: {livPct}% | Gesamtmortalität: {mort} ({mortPct}% — Sicher < 3.5%)\n• Aktuelle Stalltemperatur: {temp}",
            "it" to "• Capi vivi: {live} su {placed} (Giorno {day})\n• Vitalità: {livPct}% | Mortalità cumulativa: {mort} ({mortPct}% — Sicuro < 3.5%)\n• Temperatura attuale: {temp}"
        )
    )

    data class LocalizedLessonText(
        val title: String,
        val specialtyBadge: String,
        val coreIdea: String,
        val whyImportant: List<String>,
        val stepsTitle: String,
        val steps: List<Pair<String, String>>,
        val goodCaption: String,
        val badCaption: String,
        val warningNote: String
    )

    fun localizeSubCardAndSpec(
        subCard: BookSubCardModel,
        spec: EncyclopediaPosterSpec,
        lang: AppLanguage
    ): Pair<BookSubCardModel, EncyclopediaPosterSpec> {
        if (lang == AppLanguage.AR) return subCard to spec
        val localizedBadge = localizeSpecialtyBadge(spec.specialtyBadge, lang)
        val localizedTitle = localizeLessonTitle(subCard.id, subCard.title, lang)
        val localizedCoreIdea = localizeCoreIdea(subCard.id, spec.coreIdea, lang)
        val localizedWhy = localizeWhyImportant(subCard.id, spec.whyImportantPoints, lang)
        val localizedStepsTitle = localizeStepsTitle(subCard.id, spec.timelineStepsTitle, lang)
        val localizedSteps = localizeTimelineSteps(subCard.id, spec.timelineSteps, lang)
        val localizedGoodCaption = localizeGoodCaption(subCard.id, spec.comparisonPair?.goodSummaryCaption ?: "", lang)
        val localizedBadCaption = localizeBadCaption(subCard.id, spec.comparisonPair?.badSummaryCaption ?: "", lang)
        val localizedHeroTop = localizeHeroCalloutTop(subCard.id, spec.heroCalloutTop, lang)
        val localizedHeroBottom = localizeHeroCalloutBottom(subCard.id, spec.heroCalloutBottom, lang)
        val localizedPolaroid = localizePolaroidCaption(subCard.id, spec.polaroidCaption, lang)

        val updatedCmp = spec.comparisonPair?.copy(
            goodTitle = Translations.tr("book_ribbon_readiness_signs", lang),
            badTitle = Translations.tr("book_ribbon_avoid_mistake", lang),
            goodPoints = listOf(localizedGoodCaption) + spec.comparisonPair.goodPoints.drop(1),
            badPoints = listOf(localizedBadCaption) + spec.comparisonPair.badPoints.drop(1),
            goodSummaryCaption = localizedGoodCaption,
            badSummaryCaption = localizedBadCaption
        )

        val updatedSpec = if (spec.isMedicalSixSectionLayout) {
            localizeMedicalSpec(subCard.id, spec, localizedBadge, localizedPolaroid, lang)
        } else {
            spec.copy(
                specialtyBadge = localizedBadge,
                coreIdea = localizedCoreIdea,
                whyImportantPoints = localizedWhy,
                heroCalloutTop = localizedHeroTop,
                heroCalloutBottom = localizedHeroBottom,
                polaroidCaption = localizedPolaroid,
                timelineStepsTitle = localizedStepsTitle,
                timelineSteps = localizedSteps,
                comparisonPair = updatedCmp
            )
        }

        return subCard.copy(title = localizedTitle, posterSpec = updatedSpec) to updatedSpec
    }

    private fun localizeSpecialtyBadge(arBadge: String, lang: AppLanguage): String {
        val code = lang.code
        return when {
            arBadge.contains("التحضين") || arBadge.contains("سلوك") -> mapOf(
                "en" to "Brooding & Heating", "fr" to "Chauffage & Démarrage", "es" to "Cría y Calefacción",
                "pt" to "Aquecimento Inicial", "tr" to "Civciv Isıtma", "id" to "Pemanasan Brooding",
                "hi" to "ब्रूडिंग और हीटिंग", "ur" to "بروڈنگ اور حرارت", "sw" to "Joto la Vifaranga",
                "ru" to "Брудер и обогрев", "zh" to "育雏与供暖", "de" to "Aufzucht & Heizung", "it" to "Riscaldamento"
            )[code] ?: "Brooding & Heating"
            arBadge.contains("التهوية") || arBadge.contains("الإضاءة") -> mapOf(
                "en" to "Ventilation & Air", "fr" to "Ventilation & Air", "es" to "Ventilación y Aire",
                "pt" to "Ventilação e Ar", "tr" to "Havalandırma", "id" to "Ventilasi & Udara",
                "hi" to "वेंटिलेशन और हवा", "ur" to "ہوا داری اور ماحول", "sw" to "Uingizaji Hewa",
                "ru" to "Вентиляция", "zh" to "通风与光照", "de" to "Lüftung & Klima", "it" to "Ventilazione"
            )[code] ?: "Ventilation & Air"
            arBadge.contains("التحصين") || arBadge.contains("الاستقبال") || arBadge.contains("المناعي") || arBadge.contains("المعززة") -> mapOf(
                "en" to "Vaccination Program", "fr" to "Programme Vaccinal", "es" to "Programa de Vacunación",
                "pt" to "Programa de Vacinas", "tr" to "Aşılama Programı", "id" to "Program Vaksinasi",
                "hi" to "टीकाकरण कार्यक्रम", "ur" to "ویکسینیشن پروگرام", "sw" to "Mpango wa Chanjo",
                "ru" to "Программа вакцинации", "zh" to "免疫接种程序", "de" to "Impfprogramm", "it" to "Programma Vaccinale"
            )[code] ?: "Vaccination Program"
            arBadge.contains("الأمراض") || arBadge.contains("الطوارئ") -> mapOf(
                "en" to "Respiratory & Gut Diseases", "fr" to "Maladies Aviaires", "es" to "Enfermedades Aviar",
                "pt" to "Doenças Aviárias", "tr" to "Solunum Hastalıkları", "id" to "Penyakit Ayam",
                "hi" to "श्वसन एवं आंत रोग", "ur" to "تنفسی و معوی امراض", "sw" to "Magonjwa ya Kuku",
                "ru" to "Болезни птиц", "zh" to "禽病防治", "de" to "Geflügelkrankheiten", "it" to "Malattie Aviarie"
            )[code] ?: "Avian Diseases"
            else -> mapOf(
                "en" to "Feeding Program", "fr" to "Programme Alimentaire", "es" to "Programa de Nutrición",
                "pt" to "Programa de Nutrição", "tr" to "Besleme Programı", "id" to "Program Pakan",
                "hi" to "पोषण कार्यक्रम", "ur" to "غذائی پروگرام", "sw" to "Mpango wa Lishe",
                "ru" to "Программа кормления", "zh" to "饲喂与营养方案", "de" to "Fütterungsprogramm", "it" to "Programma Alimentare"
            )[code] ?: "Feeding Program"
        }
    }

    private fun localizeLessonTitle(id: String, fallback: String, lang: AppLanguage): String {
        val c = lang.code
        return when (id) {
            "care_1" -> mapOf(
                "en" to "Pre-heat the Barn & Litter Before Chick Arrival",
                "fr" to "Préchauffer le bâtiment et la litière avant l'arrivée des poussins",
                "es" to "Precalentar el galpón y la cama antes de llegar los pollitos",
                "pt" to "Pré-aqueça o galpão e a cama antes da chegada dos pintinhos",
                "tr" to "Civcivler Gelmeden Önce Kümesi ve Altlığı Isıtın",
                "id" to "Panaskan Kandang & Sekam Sebelum DOC Tiba",
                "hi" to "चूजों के आने से पहले शेड और बिछावन को गर्म करें",
                "ur" to "چوزوں کی آمد سے قبل شیڈ اور بچھالی کو گرم کریں",
                "sw" to "Pasha Joto Banda na Maranda Kabla Vifaranga Kufika",
                "ru" to "Прогрейте птичник и подстилку до прибытия цыплят",
                "zh" to "雏鸡到达前预热鸡舍与垫料",
                "de" to "Stall und Einstreu vor Kükenankunft vorheizen",
                "it" to "Preriscaldare capannone e lettiera prima dell'arrivo dei pulcini"
            )[c] ?: "Pre-heat the Barn & Litter Before Chick Arrival"
            "care_2" -> mapOf(
                "en" to "Monitor Chick Brooding Behavior & Thermal Comfort",
                "fr" to "Surveiller le comportement des poussins au démarrage",
                "es" to "Monitorear el comportamiento de los pollitos en cría",
                "pt" to "Monitorar o comportamento dos pintinhos no aquecimento",
                "tr" to "Civcivlerin Brooding Davranışını Gözlemleyin",
                "id" to "Pantau Perilaku & Sebaran Anak Ayam di Brooder",
                "hi" to "ब्रूडिंग में चूजों के व्यवहार की निगरानी करें",
                "ur" to "بروڈنگ میں چوزوں کے رویے اور پھیلاؤ کی نگرانی",
                "sw" to "Fuatilia Tabia na Mtawanyiko wa Vifaranga",
                "ru" to "Контроль поведения цыплят в зоне обогрева",
                "zh" to "观察育雏期雏鸡分布与热舒适行为",
                "de" to "Kükenverhalten und Wärmekomfort überwachen",
                "it" to "Monitorare il comportamento dei pulcini in svezzamento"
            )[c] ?: fallback
            "care_3" -> mapOf(
                "en" to "Barn Minimum Ventilation & Fresh Air Flow",
                "fr" to "Ventilation minimale et qualité de l'air du bâtiment",
                "es" to "Ventilación mínima y calidad del aire del galpón",
                "pt" to "Ventilação mínima e qualidade do ar do galpão",
                "tr" to "Kümes Havalandırması ve Hava Kalitesi",
                "id" to "Ventilasi Minimum & Kualitas Udara Kandang",
                "hi" to "शेड वेंटिलेशन और ताजी हवा का संतुलन",
                "ur" to "شیڈ کی ہوا داری اور تازہ ہوا کا معیار",
                "sw" to "Uingizaji Hewa Bora na Ubora wa Hewa Bandani",
                "ru" to "Минимальная вентиляция и качество воздуха",
                "zh" to "鸡舍最小通风与空气质量管理",
                "de" to "Mindestlüftung und Luftqualität im Stall",
                "it" to "Ventilazione minima e qualità dell'aria"
            )[c] ?: fallback
            "care_4" -> mapOf(
                "en" to "Lighting Darkness Program, Density & Pre-Sale Prep",
                "fr" to "Programme lumineux, densité et préparation avant vente",
                "es" to "Programa de oscuridad, densidad y preparación pre-venta",
                "pt" to "Programa de escuro, densidade e preparo pré-venda",
                "tr" to "Karanlık Programı, Sıklık ve Satış Öncesi Hazırlık",
                "id" to "Program Gelap, Kepadatan & Persiapan Panen",
                "hi" to "अंधकार कार्यक्रम, घनत्व और बिक्री पूर्व तैयारी",
                "ur" to "روشنی کا شیڈول، گنجائش اور فروخت سے قبل تیاری",
                "sw" to "Mpango wa Giza, Msongamano na Maandalizi ya Mauzo",
                "ru" to "Световой режим, плотность посадки и подготовка к сдаче",
                "zh" to "黑暗程序、饲养密度与出栏前停料管理",
                "de" to "Dunkelprogramm, Besatzdichte & Verkaufsvorbereitung",
                "it" to "Programma luce/buio, densità e preparazione pre-vendita"
            )[c] ?: fallback
            "vax_1" -> "Chick Reception (Days 1–3): Electrolytes & AD3E Protocol"
            "vax_2" -> "First Respiratory Vaccine (Days 6–7): Newcastle B1 + IB"
            "vax_3" -> "Gumboro (IBD) Drinking Water Vaccination (Days 12–14)"
            "vax_4" -> "Newcastle Booster (Days 18–20) & Liver Mycotoxin Detox"
            "dis_1" -> mapOf(
                "en" to "Infectious Bronchitis (IB) — Avian Coronavirus",
                "fr" to "Bronchite Infectieuse (IB) — Coronavirus Aviaire",
                "es" to "Bronquitis Infecciosa (IB) — Coronavirus Aviar",
                "pt" to "Bronquite Infecciosa (IB) — Coronavírus Aviário",
                "tr" to "Enfeksiyöz Bronşit (IB) — Solunum Hastalığı",
                "id" to "Infectious Bronchitis (IB) — Bronkitis Menular",
                "hi" to "संक्रामक ब्रोंकाइटिस (IB) — श्वसन रोग",
                "ur" to "معدی برونکائٹس (IB) — تنفسی مرض",
                "sw" to "Bronchitis ya Kuambukiza (IB)",
                "ru" to "Инфекционный бронхит кур (ИБК / IB)",
                "zh" to "鸡传染性支气管炎 (IB)",
                "de" to "Infektiöse Bronchitis (IB)",
                "it" to "Bronchite Infettiva (IB)"
            )[c] ?: "Infectious Bronchitis (IB)"
            "dis_2" -> "Chronic Respiratory Disease (CRD / Mycoplasma)"
            "dis_3" -> "Coccidiosis, Mycotoxins & Gut Integrity"
            "dis_4" -> "Newcastle (ND), Gumboro (IBD) & Acute Heat Stress"
            "feed_1" -> "Starter Feed Phase (Days 1–10): 23% Protein Crumble"
            "feed_2" -> "Grower Feed (Days 11–21) & Back-Level Feeder Height"
            "feed_3" -> "Finisher Pellets (Days 22–45) & Quintal Stock Control"
            "feed_4" -> "Ross 308 Weight Curve & 1.7x Water-to-Feed Balance"
            else -> fallback
        }
    }

    private fun localizeCoreIdea(id: String, fallback: String, lang: AppLanguage): String {
        val c = lang.code
        if (id == "care_1") {
            return mapOf(
                "en" to "Pre-heating the barn floor and litter before chick arrival keeps body temperature stable and prevents cold-feet digestive shock.",
                "fr" to "Préchauffer le sol et la litière avant l'arrivée des poussins stabilise leur température et évite le choc thermique.",
                "es" to "Precalentar el galpón y la cama antes de la llegada mantiene estable la temperatura corporal del pollito.",
                "pt" to "Pré-aquecer o piso e a cama antes da chegada mantém a temperatura corporal estável e evita estresse.",
                "tr" to "Civcivler gelmeden önce kümes tabanını ve altlığı ısıtmak vücut ısısını korur ve sindirim şokunu önler.",
                "id" to "Memanaskan lantai kandang dan sekam sebelum DOC tiba menjaga suhu tubuh stabil dan mencegah stres dingin.",
                "hi" to "चूजों के आने से पहले शेड के फर्श और बिछावन को गर्म करने से शरीर का तापमान स्थिर रहता है।",
                "ur" to "چوزوں کی آمد سے پہلے شیڈ کے فرش اور بچھالی کو گرم کرنا جسمانی درجہ حرارت کو مستحکم رکھتا ہے۔",
                "sw" to "Kupasha joto sakafu na maranda kabla ya vifaranga kufika huweka joto la mwili sawa.",
                "ru" to "Предварительный прогрев пола и подстилки до посадки цыплят сохраняет температуру тела и защищает ЖКТ.",
                "zh" to "在雏鸡到达前预热鸡舍地面和垫料，可保持雏鸡体温稳定并防止脚冷引起的消化不良。",
                "de" to "Das Vorheizen von Stallboden und Einstreu hält die Körpertemperatur stabil und verhindert Kälteschock.",
                "it" to "Preriscaldare pavimento e lettiera prima dell'arrivo mantiene stabile la temperatura corporea dei pulcini."
            )[c] ?: fallback
        }
        return when (id) {
            "care_2" -> "Chick distribution around the brooder is the most accurate living thermometer for warmth, heat, or drafts."
            "care_3" -> "Minimum ventilation removes ammonia and moisture by mixing fresh cold air with warm air at the ceiling first."
            "care_4" -> "Providing 4–6 hours of darkness after Day 7 strengthens bones and heart, while 8–10h feed withdrawal protects carcass quality."
            "vax_1" -> "Lukewarm water (18–21°C) with electrolytes and AD3E+C in the first 6 hours relieves transport stress and primes immunity."
            "vax_2" -> "Eye-drop or coarse spray vaccination on Days 6–7 builds strong local respiratory immunity against Newcastle and IB."
            "vax_3" -> "Gumboro drinking-water vaccine protects the Bursa of Fabricius and requires chlorine-free water with skim milk stabilizer."
            "vax_4" -> "Booster vaccination on Days 18–20 secures immunity until market age, paired with mycotoxin binders and liver support."
            "feed_1" -> "High-protein (23%) fine crumble starter feed in Days 1–10 builds the digestive villi and skeletal frame for the whole cycle."
            "feed_2" -> "Adjusting feeder pans to bird back level and drinkers to eye level saves 3–5% wasted feed and keeps litter dry."
            "feed_3" -> "Finisher pellets represent 65%+ of total feed cost; durable 3.5mm pellets and Quintal inventory tracking maximize net profit."
            "feed_4" -> "Broilers drink 1.6–1.8 L of water per 1 kg of feed; any sudden drop in water intake is the earliest 24h warning of disease."
            else -> fallback
        }
    }

    private fun localizeWhyImportant(id: String, fallback: List<String>, lang: AppLanguage): List<String> {
        val c = lang.code
        if (id == "care_1") {
            return when (c) {
                "en" -> listOf(
                    "✅ Maintains stable temperature (33°C air, 29–30°C litter)",
                    "✅ Prevents body heat loss through chicks' feet",
                    "✅ Reduces early stress and weakness",
                    "✅ Lowers disease risk in the first critical days"
                )
                "fr" -> listOf(
                    "✅ Maintient une température stable (33°C air, 29–30°C litière)",
                    "✅ Empêche la perte de chaleur corporelle par les pattes",
                    "✅ Réduit le stress et la faiblesse au démarrage",
                    "✅ Diminue le risque de maladies dès les premiers jours"
                )
                "es" -> listOf(
                    "✅ Mantiene una temperatura estable (33°C aire, 29–30°C cama)",
                    "✅ Evita la pérdida de calor corporal por las patas",
                    "✅ Reduce el estrés y la debilidad inicial",
                    "✅ Disminuye el riesgo de enfermedades en los primeros días"
                )
                "pt" -> listOf(
                    "✅ Mantém temperatura estável (33°C ar, 29–30°C cama)",
                    "✅ Evita perda de calor corporal pelas patas",
                    "✅ Reduz o estresse e a fraqueza inicial",
                    "✅ Reduz o risco de doenças nos primeiros dias"
                )
                else -> listOf(
                    "✅ Maintains stable temperature (33°C air / 29–30°C litter)",
                    "✅ Prevents body heat loss from cold floor",
                    "✅ Reduces early brooding stress & weakness",
                    "✅ Lowers disease risk in the first days"
                )
            }
        }
        return listOf(
            "✅ Protects flock livability & early immunity",
            "✅ Optimizes feed conversion ratio (FCR)",
            "✅ Prevents respiratory & digestive stress",
            "✅ Matches Ross 308 target growth curve"
        )
    }

    private fun localizeStepsTitle(id: String, fallback: String, lang: AppLanguage): String {
        return Translations.tr("book_ribbon_how_to_prepare", lang)
    }

    private fun localizeTimelineSteps(
        id: String,
        fallback: List<InfographicTimelineStep>,
        lang: AppLanguage
    ): List<InfographicTimelineStep> {
        val c = lang.code
        if (id == "care_1") {
            val titles = when (c) {
                "en" -> listOf(
                    "Clean the barn and remove dust & waste",
                    "Spread clean dry litter 5 - 8 cm deep",
                    "Turn on heating 12 - 24 hours before arrival"
                )
                "fr" -> listOf(
                    "Nettoyer le bâtiment et éliminer la poussière",
                    "Étaler une litière propre et sèche (5 - 8 cm)",
                    "Allumer le chauffage 12 - 24 h avant l'arrivée"
                )
                "es" -> listOf(
                    "Limpiar el galpón y retirar polvo y residuos",
                    "Colocar cama limpia y seca de 5 - 8 cm",
                    "Encender la calefacción 12 - 24 h antes"
                )
                "pt" -> listOf(
                    "Limpar o galpão e remover poeira e resíduos",
                    "Colocar cama limpa e seca de 5 - 8 cm",
                    "Ligar o aquecimento 12 - 24 h antes da chegada"
                )
                else -> listOf(
                    "Clean the barn and remove dust & debris",
                    "Place clean dry litter 5 - 8 cm thick",
                    "Run heaters 12 - 24h before chick arrival"
                )
            }
            return fallback.mapIndexed { idx, step ->
                step.copy(title = titles.getOrElse(idx) { step.title }, subtitle = "")
            }
        }
        val genericEn = listOf(
            "1. Inspect & prepare barn equipment",
            "2. Apply Ross 308 standard setting",
            "3. Verify flock comfort & record daily"
        )
        return fallback.mapIndexed { idx, step ->
            step.copy(title = genericEn.getOrElse(idx) { step.title }, subtitle = "")
        }
    }

    private fun localizeGoodCaption(id: String, fallback: String, lang: AppLanguage): String {
        val c = lang.code
        return mapOf(
            "en" to "Warm barn, dry litter, and chicks spread calmly",
            "fr" to "Bâtiment chaud, litière sèche et poussins bien répartis",
            "es" to "Galpón cálido, cama seca y pollitos bien distribuidos",
            "pt" to "Galpão aquecido, cama seca e pintinhos bem distribuídos",
            "tr" to "Sıcak kümes, kuru altlık ve sakin dağılmış civcivler",
            "id" to "Kandang hangat, sekam kering, dan DOC menyebar tenang",
            "hi" to "गर्म शेड, सूखा बिछावन और आराम से फैले चूजे",
            "ur" to "گرم شیڈ، خشک بچھالی اور پرسکون پھیلے ہوئے چوزے",
            "sw" to "Banda lenye joto, maranda makavu na vifaranga waliotulia",
            "ru" to "Теплый птичник, сухая подстилка и спокойные цыплята",
            "zh" to "鸡舍温暖、垫料干燥、雏鸡均匀安静分布",
            "de" to "Warmer Stall, trockene Einstreu und ruhig verteilte Küken",
            "it" to "Capannone caldo, lettiera asciutta e pulcini ben distribuiti"
        )[c] ?: fallback
    }

    private fun localizeBadCaption(id: String, fallback: String, lang: AppLanguage): String {
        val c = lang.code
        return mapOf(
            "en" to "Never receive chicks in a cold barn or wet, dirty litter",
            "fr" to "Ne jamais recevoir les poussins sur une litière froide ou humide",
            "es" to "No reciba pollitos en un galpón frío o con cama húmeda",
            "pt" to "Não receba pintinhos em galpão frio ou cama úmida e suja",
            "tr" to "Civcivleri soğuk kümeste veya ıslak altlıkta karşılamayın",
            "id" to "Jangan terima DOC di kandang dingin atau sekam basah",
            "hi" to "ठंडे शेड या गीले-गंदे बिछावन में चूजों को कभी न रखें",
            "ur" to "ٹھنڈے شیڈ یا گیلی اور گندی بچھالی میں چوزے ہرگز نہ رکھیں",
            "sw" to "Usipokee vifaranga kwenye banda baridi au maranda لويا",
            "ru" to "Не принимайте цыплят в холодный птичник или сырую подстилку",
            "zh" to "切勿在寒冷鸡舍或潮湿脏污的垫料上接雏",
            "de" to "Küken niemals im kalten Stall oder auf nasser Einstreu empfangen",
            "it" to "Non accogliere i pulcini in capannone freddo o lettiera umida"
        )[c] ?: fallback
    }

    private fun localizeHeroCalloutTop(id: String, fallback: String, lang: AppLanguage): String {
        return mapOf(
            "en" to "Pre-heat Barn Early",
            "fr" to "Préchauffer le bâtiment",
            "es" to "Precalentar el galpón",
            "pt" to "Pré-aquecer o galpão",
            "tr" to "Kümesi Önceden Isıtın",
            "id" to "Panaskan Kandang",
            "hi" to "शेड पहले गर्म करें",
            "ur" to "شیڈ پہلے سے گرم کریں",
            "sw" to "Pasha Joto Mapema",
            "ru" to "Прогрейте заранее",
            "zh" to "提前预热鸡舍",
            "de" to "Stall vorheizen",
            "it" to "Preriscaldare"
        )[lang.code] ?: fallback
    }

    private fun localizeHeroCalloutBottom(id: String, fallback: String, lang: AppLanguage): String {
        return mapOf(
            "en" to "Clean & Dry Litter",
            "fr" to "Litière propre et sèche",
            "es" to "Cama limpia y seca",
            "pt" to "Cama limpa e seca",
            "tr" to "Temiz ve Kuru Altlık",
            "id" to "Sekam Bersih & Kering",
            "hi" to "साफ और सूखा बिछावन",
            "ur" to "صاف اور خشک بچھالی",
            "sw" to "Maranda Safi na Makavu",
            "ru" to "Чистая сухая подстилка",
            "zh" to "干净干燥的垫料",
            "de" to "Saubere Einstreu",
            "it" to "Lettiera pulita e asciutta"
        )[lang.code] ?: fallback
    }

    private fun localizePolaroidCaption(id: String, fallback: String, lang: AppLanguage): String {
        return mapOf(
            "en" to "Ross 308 Field Guide",
            "fr" to "Guide Ross 308",
            "es" to "Guía Ross 308",
            "pt" to "Guia Ross 308",
            "tr" to "Ross 308 Rehberi",
            "id" to "Panduan Ross 308",
            "hi" to "Ross 308 गाइड",
            "ur" to "Ross 308 گائیڈ",
            "sw" to "Mwongozo wa Ross 308",
            "ru" to "Справочник Ross 308",
            "zh" to "Ross 308 图鉴",
            "de" to "Ross 308 Leitfaden",
            "it" to "Guida Ross 308"
        )[lang.code] ?: fallback
    }

    private fun localizeMedicalSpec(
        id: String,
        spec: EncyclopediaPosterSpec,
        localizedBadge: String,
        localizedPolaroid: String,
        lang: AppLanguage
    ): EncyclopediaPosterSpec {
        return spec.copy(
            specialtyBadge = localizedBadge,
            polaroidCaption = localizedPolaroid,
            sec1Definition = when (id) {
                "dis_1" -> "Acute, highly contagious viral respiratory disease (IBV) causing tracheal rales, bronchial mucus plugs, and reduced weight gain."
                "dis_2" -> "Chronic respiratory infection (Mycoplasma gallisepticum) triggered by high ammonia (>10 ppm), causing airsacculitis and E.coli complications."
                "dis_3" -> "Intestinal parasitic disease (Eimeria spp.) thriving in wet litter (>30% moisture), causing bloody droppings and severe FCR loss."
                else -> "Critical emergency triad: Newcastle (neurological/respiratory), Gumboro (immunosuppression), and acute Heat Stress (>30°C)."
            },
            quickFacts = listOf(
                "📅 Susceptible Age" to "1 – 4 Weeks",
                "⏳ Duration" to "5 – 7 Days",
                "✥ Contagion" to "Very High",
                "📊 Mortality" to "1% – 10% (up to 30%)"
            ),
            anatomyLabels = listOf(
                InfographicAnatomyLabel("Nasal Sinuses", "Swelling & serous discharge"),
                InfographicAnatomyLabel("Trachea", "Inflammation & congestion"),
                InfographicAnatomyLabel("Bronchi", "Mucus accumulation & plugs"),
                InfographicAnatomyLabel("Lungs", "Labored breathing & hypoxia")
            ),
            zoomInsetTitle = "Bronchial Inflammation & Mucus Plugs",
            sec2SymptomCards = listOf(
                InfographicSymptomCard("🥀", "Lethargy & Low Appetite", "Drop in feed intake"),
                InfographicSymptomCard("📉", "Uneven Growth", "Poor flock uniformity"),
                InfographicSymptomCard("🪶", "Ruffled Feathers", "Huddling near heaters"),
                InfographicSymptomCard("💧", "Nasal & Eye Discharge", "Watery eyes & sinuses"),
                InfographicSymptomCard("😮‍💨", "Sneezing & Gasping", "Coughing & tracheal rales")
            ),
            sec3TreatmentSteps = listOf(
                "✅ Raise barn temperature +1–2°C to ease cold stress.",
                "✅ Improve minimum ventilation and purge ammonia.",
                "✅ Provide vitamins AD3E + C and bronchodilators.",
                "✅ Administer vet-prescribed antibiotic for secondary E.coli.",
                "✅ Support kidneys with electrolytes and respect withdrawal days."
            ),
            sec4PerformanceTable = listOf(
                InfographicPerformanceMetric("Mortality Rate", "↑ 1% – 10%", true),
                InfographicPerformanceMetric("Final Weight", "↓ 3% – 10%", true),
                InfographicPerformanceMetric("Feed Conversion (FCR)", "↑ +0.10 – 0.30", true),
                InfographicPerformanceMetric("Feed Intake", "↓ 5% – 20%", true),
                InfographicPerformanceMetric("Cycle Duration", "↑ +2 – 3 Days", true)
            ),
            sec5DiagnosisPoints = listOf(
                "✅ Rapidly spreading respiratory rales & sneezing.",
                "✅ Necropsy showing tracheal congestion & bronchial plugs.",
                "✅ Laboratory confirmation via PCR & ELISA."
            ),
            sec6PreventionIcons = listOf(
                "🐣" to "Source chicks from certified hatchery",
                "🛡️" to "Enforce strict farm biosecurity",
                "🧼" to "Thorough cleaning & disinfection",
                "💨" to "Optimize ventilation & air quality",
                "💉" to "Vaccinate per flock schedule"
            ),
            sec6ImportantNote = "⚠️ Important Note: Consult your veterinarian to align vaccine strains with local field challenges and maintain dry litter (<25% moisture)."
        )
    }
}
