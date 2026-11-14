package com.example.models

object Ross308DiseaseFeedPosters {

    fun posterForDisease(id: String): EncyclopediaPosterSpec? = when (id) {
        "dis_1" -> EncyclopediaPosterSpec(
            lessonNumber = 1,
            visualType = InfographicVisualType.DIS_IB_BRONCHITIS,
            specialtyBadge = "الأمراض التنفسية",
            englishSubtitleOrRule = "مرض البرونشيت المعدية — Infectious Bronchitis (IB)",
            coreIdea = "مرض فيروسي شديد العدوى يصيب الجهاز التنفسي العلوي والرئتين (وقد يؤثر على الكلى) مسبباً خسائر كبيرة في الوزن ومعامل التحويل.",
            diagramTitle = "مواقع الإصابة في الجهاز التنفسي",
            polaroidCaption = "التهاب القصبة والشعب IB",
            isMedicalSixSectionLayout = true,
            sec1Definition = "عدوى فيروسية حادة وسريعة الانتشار يسببها فيروس كورونا الطيري (IBV)، تصيب القصبة الهوائية والشعب الهوائية وتؤدي لالتهاب وتراكم مخاط وعدوى ثانوية.",
            quickFacts = listOf(
                "📅 العمر الأكثر عرضة" to "1 – 4 أسابيع",
                "⏳ مدة المرض" to "5 – 7 أيام",
                "✥ العدوى" to "مرتفعة جداً (سريعة الانتشار)",
                "📊 النفوق" to "1% – 10% وقد يرتفع إلى 30%"
            ),
            anatomyLabels = listOf(
                InfographicAnatomyLabel("الجيوب الأنفية", "التهاب وتورم وإفرازات أنفية وعينية"),
                InfographicAnatomyLabel("القصبة الهوائية", "التهاب وتضخم واحتقان الغشاء المخاطي"),
                InfographicAnatomyLabel("الشعب الهوائية", "التهاب وتراكم مخاط وسدادات تجبنية"),
                InfographicAnatomyLabel("الرئتان", "صعوبة في التنفس ونقص الأكسجين")
            ),
            zoomInsetTitle = "التهاب الشعب الهوائية وتراكم المخاط",
            pathogenCode = "IBV",
            sec2SymptomCards = listOf(
                InfographicSymptomCard("🥀", "خمول وانخفاض في الشهية", "تراجع سحب العلف والماء"),
                InfographicSymptomCard("📉", "تفاوت في النمو", "ضعف الأوزان وتأخر عن المعيار"),
                InfographicSymptomCard("🪶", "نفش الريش وقلة النشاط", "تجمع الصيصان حول مصدر التدفئة"),
                InfographicSymptomCard("💧", "إفرازات أنفية وعينية", "دموع وتورم حول العين والجيوب"),
                InfographicSymptomCard("😮‍💨", "عطاس وسعال وتنفس سريع", "حشرجة مسموعة وتنفس بفم مفتوح")
            ),
            sec3TreatmentSteps = listOf(
                "✅ رفع درجة حرارة العنبر 1–2°C لتقليل إجهاد البرد على الطيور.",
                "✅ تحسين التهوية وجودة الهواء وطرد غاز الأمونيا والرطوبة.",
                "✅ تقديم فيتامينات (AD3E + C) وموسعات شعب ومذيبات بلغم في الماء.",
                "✅ مضاد حيوي بوصفة بيطرية للسيطرة على العدوى البكتيرية الثانوية (E.coli).",
                "✅ غسيل كلوي وإلكترولايت عند تأثر الكلى مع الالتزام بفترة السحب."
            ),
            sec4PerformanceTable = listOf(
                InfographicPerformanceMetric("معدل النفوق", "↑ 1% – 10% (إلى 30%)", isNegativeImpact = true),
                InfographicPerformanceMetric("الوزن النهائي", "↓ 3% – 10% (إلى 20%)", isNegativeImpact = true),
                InfographicPerformanceMetric("التحويل الغذائي FCR", "↑ 0.10 – 0.30", isNegativeImpact = true),
                InfographicPerformanceMetric("استهلاك العلف", "↓ 5% – 20%", isNegativeImpact = true),
                InfographicPerformanceMetric("مدة الدورة", "↑ +2 – 3 أيام", isNegativeImpact = true)
            ),
            sec5DiagnosisPoints = listOf(
                "✅ ملاحظة الأعراض التنفسية السريعة الانتشار وحشرجة القصبة.",
                "✅ التشريح البيطري (احتقان القصبة وسدادات مخاطية في تفرع الشعب).",
                "✅ الفحوصات المخبرية المؤكدة (PCR & ELISA) والتفريق عن النيوكاسل."
            ),
            sec6PreventionIcons = listOf(
                "🐣" to "الحصول على كتاكيت من مصدر موثوق",
                "🛡️" to "تطبيق إجراءات الأمن الحيوي",
                "🧼" to "التنظيف والتطهير الدوري",
                "💨" to "تحسين التهوية وجودة الهواء",
                "💉" to "التطعيم حسب برنامج السلالة"
            ),
            sec6ImportantNote = "⚠️ ملاحظة مهمة: مناعة البرونشيت قصيرة الأمد نسبياً، وقد تحدث إصابة بعترات متحورة (Variant) رغم التحصين؛ لذا تجب استشارة البيطري لاختيار برنامج التطعيم المطابق لمنطقتك."
        )

        "dis_2" -> EncyclopediaPosterSpec(
            lessonNumber = 2,
            visualType = InfographicVisualType.DIS_CRD_MYCOPLASMA,
            specialtyBadge = "الأمراض التنفسية",
            englishSubtitleOrRule = "الميكوبلازما والأمراض التنفسية المزمنة — CRD / Mycoplasma",
            coreIdea = "مرض تنفسي مزمن تسببه بكتيريا Mycoplasma gallisepticum ويزداد شراسة مع ارتفاع غاز الأمونيا والغبار وتذبذب الحرارة.",
            diagramTitle = "مواقع الإصابة في القصبة والأكياس الهوائية",
            polaroidCaption = "التهاب الأكياس الهوائية CRD",
            isMedicalSixSectionLayout = true,
            sec1Definition = "التهاب تنفسي مزمن يصيب الجيوب والقصبة والأكياس الهوائية، ويتضاعف ببكتيريا E.coli مسبباً تجبن الأكياس الهوائية وتغطية القلب والكبد بغشاء فبريني.",
            quickFacts = listOf(
                "📅 العمر الأكثر عرضة" to "2 – 6 أسابيع",
                "⏳ مدة المرض" to "مزمن (7 – 14 يوماً)",
                "✥ العدوى" to "متوسطة إلى سريعة مع الأمونيا",
                "📊 النفوق" to "3% – 15% (يرتفع مع E.coli)"
            ),
            anatomyLabels = listOf(
                InfographicAnatomyLabel("الجيوب الحجاجية", "تورم الوجه تحت العين وإفرازات رغوية"),
                InfographicAnatomyLabel("القصبة الهوائية", "التهاب وتضخم وشخير ليلي مسموع"),
                InfographicAnatomyLabel("الشعب الهوائية", "التهاب وتراكم إفرازات مخاطية لزجة"),
                InfographicAnatomyLabel("الأكياس الهوائية والرئتان", "عتامة وتجبن أصفر وصعوبة في التنفس")
            ),
            zoomInsetTitle = "تجبن الأكياس الهوائية والتهاب الشعب",
            pathogenCode = "MG / CRD",
            sec2SymptomCards = listOf(
                InfographicSymptomCard("🥀", "خمول وانخفاض في الشهية", "تراجع تدريجي في سحب العلف"),
                InfographicSymptomCard("📉", "تفاوت في النمو", "ظهور طيور متقزمة وضعيفة الوزن"),
                InfographicSymptomCard("🪶", "نفش الريش وقلة النشاط", "انكماش الطيور وخشونة الريش"),
                InfographicSymptomCard("💧", "إفرازات أنفية وعينية", "رغوة في العين وتورم الجيوب"),
                InfographicSymptomCard("😮‍💨", "عطاس وسعال وتنفس سريع", "شخير وحشرجة تزداد وضوحاً بالليل")
            ),
            sec3TreatmentSteps = listOf(
                "✅ إصلاح التهوية الدنيا فوراً لطرد الأمونيا (< 10 ppm) والغبار.",
                "✅ تقديم موسعات شعب وزيوت عطرية (منثول وأوكالبتوس) في ماء الشرب.",
                "✅ مضاد حيوي نوعي للميكوبلازما (تيلوزين / تيامولين / دوكسي) بوصفة بيطرية.",
                "✅ دعم القطيع بفيتامين AD3E + C لترميم الأغشية المخاطية.",
                "✅ الالتزام الصارم بفترة السحب الدوائي قبل البيع."
            ),
            sec4PerformanceTable = listOf(
                InfographicPerformanceMetric("معدل النفوق", "↑ 3% – 15%", isNegativeImpact = true),
                InfographicPerformanceMetric("الوزن النهائي", "↓ 10% – 18%", isNegativeImpact = true),
                InfographicPerformanceMetric("التحويل الغذائي FCR", "↑ 0.15 – 0.28", isNegativeImpact = true),
                InfographicPerformanceMetric("استهلاك العلف", "↓ 8% – 20%", isNegativeImpact = true),
                InfographicPerformanceMetric("مدة الدورة", "↑ +2 – 4 أيام", isNegativeImpact = true)
            ),
            sec5DiagnosisPoints = listOf(
                "✅ سماع الشخير الليلي وملاحظة تورم الجيوب تحت العين.",
                "✅ التشريح البيطري (تجبن الأكياس الهوائية وغشاء فبريني حول القلب والكبد).",
                "✅ الفحص المخبري (اختبار التلزن السريع SPA وفحص PCR)."
            ),
            sec6PreventionIcons = listOf(
                "🐣" to "الحصول على كتاكيت من مصدر موثوق",
                "🛡️" to "تطبيق إجراءات الأمن الحيوي",
                "🧼" to "التنظيف والتطهير الدوري",
                "💨" to "تحسين التهوية وجودة الهواء",
                "💉" to "التطعيم حسب برنامج السلالة"
            ),
            sec6ImportantNote = "⚠️ ملاحظة مهمة: لا ينجح أي مضاد حيوي ضد CRD إطلاقاً إذا بقي غاز الأمونيا مرتفعاً أو الفرشة مبللة داخل العنبر."
        )

        "dis_3" -> EncyclopediaPosterSpec(
            lessonNumber = 3,
            visualType = InfographicVisualType.DIS_COCCIDIOSIS_GUT,
            specialtyBadge = "الأمراض المعوية",
            englishSubtitleOrRule = "الكوكسيديا والسموم الفطرية والإجهاد المعوي — Coccidiosis",
            coreIdea = "طفيلي معوي يدمر بطانة الأمعاء والأعورين في الفرشة الرطبة ويوقف امتصاص العلف، مما يرفع FCR ويمهد للكلوستريديا.",
            diagramTitle = "مواقع الإصابة في الأمعاء الدقيقة والأعورين",
            polaroidCaption = "تشخيص الكوكسيديا والزرق",
            isMedicalSixSectionLayout = true,
            sec1Definition = "إصابة طفيلية معوية (Eimeria spp.) تتكاثر في الفرشة الرطبة الدافئة (> 30% رطوبة)، تهاجم جدار الأمعاء والأعورين وتسبب نزيفاً وتآكل الزغابات.",
            quickFacts = listOf(
                "📅 العمر الأكثر عرضة" to "2 – 5 أسابيع",
                "⏳ مدة المرض" to "5 – 8 أيام",
                "✥ العدوى" to "مرتفعة جداً عبر الفرشة الرطبة",
                "📊 النفوق" to "3% – 20%"
            ),
            anatomyLabels = listOf(
                InfographicAnatomyLabel("الاثنا عشر والأمعاء", "التهاب وتضخم ونقاط نزفية على الجدار"),
                InfographicAnatomyLabel("الزغابات المعوية", "تآكل البطانة وضعف امتصاص الغذاء"),
                InfographicAnatomyLabel("الأعوران (Ceca)", "انتفاخ شديد وتراكم دم متجلط"),
                InfographicAnatomyLabel("لون الزرق", "إسهال مدمم أو بني داكن أو برتقالي مخاطي")
            ),
            zoomInsetTitle = "نزيف الأعورين وتلف الزغابات المعوية",
            pathogenCode = "Eimeria",
            sec2SymptomCards = listOf(
                InfographicSymptomCard("🥀", "خمول وانخفاض في الشهية", "انكماش الطيور وبهتان العرف"),
                InfographicSymptomCard("📉", "تفاوت في النمو", "توقف الزيادة اليومية وارتفاع FCR"),
                InfographicSymptomCard("🪶", "نفش الريش وقلة النشاط", "تهدل الأجنحة ووقوف الطيور بعيون مغمضة"),
                InfographicSymptomCard("🩸", "زرق مدمم أو بني داكن", "مع ظهور علف غير مهضوم في الفرشة"),
                InfographicSymptomCard("💧", "بلل وتكتل الفرشة", "ارتفاع الرطوبة حول خطوط المساقي")
            ),
            sec3TreatmentSteps = listOf(
                "✅ إصلاح تسريب المساقي فوراً وإزالة الفرشة الرطبة وفرش نشارة جافة.",
                "✅ تقديم مضاد كوكسيديا متخصص (تولترازوريل أو أمبروليوم) بوصفة بيطرية.",
                "✅ إضافة فيتامين K3 لوقف النزيف وفيتامين A لتجديد بطانة الأمعاء.",
                "✅ إعطاء أحماض عضوية وبروبيوتيك ومضاد سموم لمنع الكلوستريديا الثانوية.",
                "✅ تقليب الفرشة وتحسين سحب الرطوبة بالتهوية."
            ),
            sec4PerformanceTable = listOf(
                InfographicPerformanceMetric("معدل النفوق", "↑ 3% – 20%", isNegativeImpact = true),
                InfographicPerformanceMetric("الوزن النهائي", "↓ 10% – 22%", isNegativeImpact = true),
                InfographicPerformanceMetric("التحويل الغذائي FCR", "↑ 0.18 – 0.35", isNegativeImpact = true),
                InfographicPerformanceMetric("استهلاك العلف", "↓ 10% – 25%", isNegativeImpact = true),
                InfographicPerformanceMetric("مدة الدورة", "↑ +2 – 4 أيام", isNegativeImpact = true)
            ),
            sec5DiagnosisPoints = listOf(
                "✅ فحص لون الزرق الصباحي (دموي أحمر أو بني غامق أو برتقالي).",
                "✅ التشريح البيطري للأمعاء والأعورين وملاحظة البقع النزفية.",
                "✅ الفحص المجهري لمسحة الأمعاء لرؤية بويضات الأيميريا (Oocysts)."
            ),
            sec6PreventionIcons = listOf(
                "🐣" to "الحصول على كتاكيت من مصدر موثوق",
                "🛡️" to "تطبيق إجراءات الأمن الحيوي",
                "🧼" to "التنظيف والتطهير الدوري",
                "💨" to "تحسين التهوية وجودة الهواء",
                "🌾" to "حفظ العلف جافاً مع مضاد سموم"
            ),
            sec6ImportantNote = "⚠️ ملاحظة مهمة: المطهرات العادية لا تقتل بويضات الكوكسيديا؛ الحفاظ على جفاف الفرشة (< 25% رطوبة) وضبط ارتفاع المساقي هو خط الدفاع الأول."
        )

        "dis_4" -> EncyclopediaPosterSpec(
            lessonNumber = 4,
            visualType = InfographicVisualType.DIS_ND_IBD_HEAT,
            specialtyBadge = "الطوارئ الفيروسية والحرارية",
            englishSubtitleOrRule = "النيوكاسل والجمبورو والإجهاد الحراري — ND & IBD & Heat Stress",
            coreIdea = "حالات الطوارئ الحرجة التي تضرب الجهاز العصبي والمناعي والتنفسي وتتطلب تدخلاً فورياً لحماية القطيع من النفوق الحاد.",
            diagramTitle = "مواقع الإصابة الحرجة (الأعصاب، غدة فابريشيوس، والرئتان)",
            polaroidCaption = "طوارئ العنبر والوقاية",
            isMedicalSixSectionLayout = true,
            sec1Definition = "ثلاثية الطوارئ الكبرى: النيوكاسل (NDV) يصيب التنفس والأعصاب، الجمبورو (IBD) يدمر غدة المناعة، والإجهاد الحراري (> 30°C) يسبب الاختناق الحراري.",
            quickFacts = listOf(
                "📅 العمر الأكثر عرضة" to "2 – 6 أسابيع",
                "⏳ مدة المرض" to "حادة (3 – 7 أيام)",
                "✥ العدوى" to "شديدة الخطورة وسريعة جداً",
                "📊 النفوق" to "10% – 50% عند غياب التحصين/التبريد"
            ),
            anatomyLabels = listOf(
                InfographicAnatomyLabel("الجهاز العصبي والرقبة", "التواء الرقبة وشلل الأرجل في النيوكاسل"),
                InfographicAnatomyLabel("القصبة والرئتان", "احتقان شديد ولهاث سريع ونقص أكسجين"),
                InfographicAnatomyLabel("غدة فابريشيوس", "تورم ونزيف أو ضمور جيلاتيني في الجمبورو"),
                InfographicAnatomyLabel("المعدة الغدية والعضلات", "بقع نزفية مميزة على الفخذ والمعدة الغدية")
            ),
            zoomInsetTitle = "نزيف المعدة الغدية وغدة فابريشيوس",
            pathogenCode = "NDV / IBD",
            sec2SymptomCards = listOf(
                InfographicSymptomCard("🥀", "خمول وانخفاض في الشهية", "توقف مفاجئ عن الأكل وانهيار الحيوية"),
                InfographicSymptomCard("🌀", "التواء الرقبة واضطراب المشي", "أعراض عصبية ورعشة في النيوكاسل"),
                InfographicSymptomCard("🪶", "نفش الريش وقلة النشاط", "انكماش ونقر منطقة المجمع في الجمبورو"),
                InfographicSymptomCard("💚", "إسهال أخضر أو مائي أبيض", "جفاف حاد وإجهاد كلوي"),
                InfographicSymptomCard("😮‍💨", "عطاس وسعال وتنفس سريع", "لهاث عنيف وفرد للأجنحة في الحر")
            ),
            sec3TreatmentSteps = listOf(
                "✅ في الحر (> 30°C): ارفع العلف وقت الظهيرة وشغّل التبريد والشفاطات.",
                "✅ قدّم فيتامين C + إلكترولايت + بيكربونات الصوديوم في ماء بارد متجدد.",
                "✅ في الاشتباه الفيروسي: استدعِ الطبيب البيطري فوراً وطبّق العزل الصارم.",
                "✅ ادعم المناعة والكلى بفيتامين AD3E وغسيل كلوي لتقليل النفوق.",
                "✅ طهّر مداخل العنبر وامنع حركة العمال والمعدات بين العنابر."
            ),
            sec4PerformanceTable = listOf(
                InfographicPerformanceMetric("معدل النفوق", "↑ 10% – 50%", isNegativeImpact = true),
                InfographicPerformanceMetric("الوزن النهائي", "↓ 15% – 30%", isNegativeImpact = true),
                InfographicPerformanceMetric("التحويل الغذائي FCR", "↑ 0.25 – 0.45", isNegativeImpact = true),
                InfographicPerformanceMetric("استهلاك العلف", "↓ 15% – 35%", isNegativeImpact = true),
                InfographicPerformanceMetric("مدة الدورة", "↑ +3 – 5 أيام", isNegativeImpact = true)
            ),
            sec5DiagnosisPoints = listOf(
                "✅ رصد الأعراض العصبية (التواء الرقبة) أو اللهاث الحراري والنفوق المفاجئ.",
                "✅ التشريح البيطري (فحص غدة فابريشيوس والمعدة الغدية والقصبة).",
                "✅ الفحص المخبري المؤكد (PCR & HI) ومراجعة سجل حرارة العنبر."
            ),
            sec6PreventionIcons = listOf(
                "🐣" to "الحصول على كتاكيت من مصدر موثوق",
                "🛡️" to "تطبيق إجراءات الأمن الحيوي",
                "🧼" to "التنظيف والتطهير الدوري",
                "💨" to "تحسين التهوية وجودة الهواء",
                "💉" to "التطعيم حسب برنامج السلالة"
            ),
            sec6ImportantNote = "⚠️ ملاحظة مهمة: تصويم العلف خلال ساعات الذروة الحارة (11 صباحاً – 4 عصراً) مع تبريد ماء الشرب ينقذ الأوزان الثقيلة من النفوق المفاجئ."
        )

        else -> null
    }

    fun posterForFeed(id: String): EncyclopediaPosterSpec? = when (id) {
        "feed_1" -> EncyclopediaPosterSpec(
            lessonNumber = 1,
            visualType = InfographicVisualType.FEED_STARTER_SPEC,
            specialtyBadge = "برنامج التغذية",
            englishSubtitleOrRule = "Three Feed Phases: Starter 23% ➔ Grower 22% ➔ Finisher 20.5%",
            coreIdea = "توزيع العلف على 3 مراحل (بادئ 23% ➔ نامي 22% ➔ ناهي 20.5% بروتين) يلبي احتياج النمو ويوفر التكلفة.",
            whyImportantPoints = listOf(
                "✅ بادئ 23% بروتين (أيام 1–10) لبناء الأعضاء",
                "✅ نامي 22% بروتين (أيام 11–21) للهيكل واللحم",
                "✅ ناهي 20.5% بروتين (22–45 يوم) للوزن النهائي",
                "✅ يرفع كفاءة التحويل ويخفض تكلفة القنطار"
            ),
            diagramTitle = "لوحة مراحل العلف الثلاث (بادئ 23% ➔ نامي 22% ➔ ناهي 20.5%)",
            heroCalloutTop = "23% ➔ 22% ➔ 20.5%",
            heroCalloutBottom = "بادئ ➔ نامي ➔ ناهي",
            polaroidCaption = "مراحل العلف الثلاث",
            timelineStepsTitle = "⚙️ كيف أطبّق مراحل العلف الثلاث؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "🌾", "العلف البادئ\n(يوم 1-10)", "بروتين 23%\nمحبب ناعم"),
                InfographicTimelineStep(2, "🌿", "العلف النامي\n(يوم 11-21)", "بروتين 22%\nلبناء الهيكل"),
                InfographicTimelineStep(3, "🌽", "العلف الناهي\n(يوم 22-45)", "بروتين 20.5%\nللتسمين")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات النجاح",
                goodPoints = listOf(
                    "الالتزام بنسبة البروتين والطاقة بكل مرحلة",
                    "قوام علف متماسك خالٍ من البودرة",
                    "وزن 190 غ باليوم 7 و1 كغ باليوم 21"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "تقديم علف خشن أو ناهي مبكراً للصيصان",
                    "استمرار العلف البادئ طوال الدورة",
                    "إهمال تاريخ صلاحية ورطوبة العلف"
                ),
                goodSummaryCaption = "حوصلة ممتلئة بماء وعلف\nوضبط المعلفة بمستوى الظهر",
                badSummaryCaption = "تغيير العلف فجأة\nبدون خلط تدريجي"
            ),
            tableTitle = "📊 جدول مراحل العلف الثلاث (Ross 308)",
            tableHeaders = listOf("المرحلة", "العمر", "البروتين والطاقة"),
            tableRows = listOf(
                listOf("البادئ Starter", "1 – 10 أيام", "23% بروتين • 3000 kcal"),
                listOf("النامي Grower", "11 – 21 يوماً", "22% بروتين • 3100 kcal"),
                listOf("الناهي Finisher", "22 – 45 يوماً", "20.5% بروتين • 3200 kcal")
            ),
            warningNote = "⚠️ كل مرحلة تغذية مصممة بدقة لعمر الأمعاء وحجم منقار الطائر!"
        )

        "feed_2" -> EncyclopediaPosterSpec(
            lessonNumber = 2,
            visualType = InfographicVisualType.FEED_GROWER_HEIGHT,
            specialtyBadge = "برنامج التغذية",
            englishSubtitleOrRule = "Feeder & Drinker Height Alignment at Bird Back Level",
            coreIdea = "ضبط ارتفاع المعالف والمساقي بمستوى ظهر الطائر يمنع هدر 3%–5% من العلف ويمنع بلل الفرشة.",
            whyImportantPoints = listOf(
                "✅ حافة المعلف توازي مستوى ظهر الطائر",
                "✅ توفير 3%–5% من فاتورة العلف المهدور",
                "✅ إبقاء الفرشة جافة ومنع الكوكسيديا",
                "✅ مستوى العلف بالطبق لا يتجاوز الثلث"
            ),
            diagramTitle = "مخطط ضبط ارتفاع المعلف والمسقى بمستوى ظهر الطائر",
            heroCalloutTop = "المعلف بمستوى الظهر",
            heroCalloutBottom = "توفير 3%–5% من العلف",
            polaroidCaption = "ضبط ارتفاع المعالف",
            timelineStepsTitle = "⚙️ كيف أضبط المعالف والمساقي؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "📏", "ارتفاع المعلف\n(كل 2-3 أيام)", "بمستوى ظهر الطائر\nلمنع بعثرة العلف"),
                InfographicTimelineStep(2, "🥣", "مستوى الطبق\n(ثلث فقط)", "ضبط بوابة العلف\nوتوفير 3%–5%"),
                InfographicTimelineStep(3, "💧", "خطوط الشرب\n(مستوى العين)", "رأس مرفوع قليلاً\nوفرشة جافة تماماً")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات النجاح",
                goodPoints = listOf(
                    "حافة المعلف بمستوى ظهر الطائر تماماً",
                    "صفر علف مبعثر داخل نشارة الفرشة",
                    "طيور تشرب برأس مرفوع دون بلل"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "ترك المعالف على الأرض يبدد 4% علفاً",
                    "ملء الأطباق للحافة أو دخول الطيور فيها",
                    "انخفاض المساقي يبلل الفرشة تحت الحلمات"
                ),
                goodSummaryCaption = "معلف بمستوى الظهر\nصفر هدر وفرشة جافة",
                badSummaryCaption = "معلف منخفض مبعثر\nأو مسقى يبلل الفرشة"
            ),
            tableTitle = "📊 أثر ضبط المعالف والمساقي على التكلفة",
            tableHeaders = listOf("العامل", "ضبط صحيح", "إهمال الارتفاع"),
            tableRows = listOf(
                listOf("هدر العلف بالفرشة", "أقل من 0.5% ✓", "هدر 3% – 5% ✗"),
                listOf("جفاف الفرشة", "جافة < 25% ✓", "مبللة > 35% ✗"),
                listOf("معامل التحويل FCR", "تحسن -0.06 ✓", "ارتفاع +0.08 ✗")
            ),
            warningNote = "⚠️ إذا رأيت علفاً داخل الفرشة فالمعلف منخفض جداً أو ممتلئ زيادة!"
        )

        "feed_3" -> EncyclopediaPosterSpec(
            lessonNumber = 3,
            visualType = InfographicVisualType.FEED_FINISHER_PELLETS,
            specialtyBadge = "برنامج التغذية",
            englishSubtitleOrRule = "3-Day Gradual Feed Transition (75/25 ➔ 50/50 ➔ 25/75) & Mycotoxin Binders",
            coreIdea = "تغيير العلف تدريجياً خلال 3 أيام (75/25 ثم 50/50 ثم 25/75) مع مضاد السموم يحمي الأمعاء والكبد.",
            whyImportantPoints = listOf(
                "✅ اليوم 1: خلط 75% علف قديم + 25% جديد",
                "✅ اليوم 2: خلط 50% قديم + 50% جديد",
                "✅ اليوم 3: خلط 25% قديم + 75% جديد",
                "✅ إضافة مضاد سموم فطرية وحماية الكبد"
            ),
            diagramTitle = "مخطط قاعدة الخلط التدريجي للعلف (3 أيام) ومضاد السموم",
            heroCalloutTop = "75/25 ➔ 50/50 ➔ 25/75",
            heroCalloutBottom = "مضاد سموم + رصيد بالقنطار",
            polaroidCaption = "الخلط التدريجي 3 أيام",
            timelineStepsTitle = "⚙️ كيف أبدّل العلف بأمان؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "🔄", "اليوم الأول\n(نسبة 75 / 25)", "75% علف قديم\n+ 25% علف جديد"),
                InfographicTimelineStep(2, "⚖️", "اليوم 2 و 3\n(50/50 ➔ 25/75)", "تدرج سلس يحمي\nالزغابات المعوية"),
                InfographicTimelineStep(3, "🧪", "مضاد السموم\n(تخزين جاف)", "تخزين على طبالي\nومراقبة بالقنطار")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات النجاح",
                goodPoints = listOf(
                    "خلط تدريجي 3 أيام دون إسهال أو توقف",
                    "تخزين الأكياس على طبالي جافة بالقنطار (ق)",
                    "كورس مضاد سموم عند كل نقلة علف"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "تبديل نوع العلف فجأة في يوم واحد",
                    "تخزين أكياس العلف على أرضية رطبة",
                    "تقديم علف متعفن أو مفتت كبودرة"
                ),
                goodSummaryCaption = "خلط تدريجي 3 أيام\n+ مضاد سموم وتخزين جاف",
                badSummaryCaption = "تغيير مفاجئ للعلف أو\nتخزين رطب يولد سموماً"
            ),
            tableTitle = "📊 جدول الخلط التدريجي خلال 3 أيام (Ross 308)",
            tableHeaders = listOf("يوم الانتقال", "نسبة العلف السابق", "نسبة العلف الجديد"),
            tableRows = listOf(
                listOf("اليوم الأول للنقلة", "75%", "25% + مضاد سموم"),
                listOf("اليوم الثاني للنقلة", "50%", "50% + فيتامينات"),
                listOf("اليوم الثالث للنقلة", "25%", "75% ثم 100% باليوم 4")
            ),
            warningNote = "⚠️ التبديل المفاجئ للعلف يسبب صدمة هضمية وإسهالاً ويرفع FCR فوراً!"
        )

        "feed_4" -> EncyclopediaPosterSpec(
            lessonNumber = 4,
            visualType = InfographicVisualType.FEED_WEIGHT_WATER_CURVE,
            specialtyBadge = "برنامج التغذية",
            englishSubtitleOrRule = "Ross 308 Weight Curve to 2.8 kg, Ideal FCR & 1.7x Water Rule",
            coreIdea = "مراقبة الوزن نحو 2.8 كغ مع نسبة الماء للعلف (1.7 لتر/كغ) تكشف المرض قبل 24 ساعة وتضبط FCR.",
            whyImportantPoints = listOf(
                "✅ بلوغ 190 غ (يوم 7) و1.0 كغ (يوم 21)",
                "✅ الوصول لوزن 2.8 كغ بمعامل تحويل ~1.65",
                "✅ ميزان الماء: 1.7 لتر لكل 1 كغ علف",
                "✅ تسجيل القنطار (ق) والبيع الجزئي بدقة"
            ),
            diagramTitle = "منحنى الوزن القياسي Ross 308 حتى 2.8 كغ وميزان الماء (1.7x)",
            heroCalloutTop = "1.7 لتر ماء / 1 كغ علف",
            heroCalloutBottom = "الهدف: 2.8 كغ • FCR 1.65",
            polaroidCaption = "منحنى الوزن وFCR",
            timelineStepsTitle = "⚙️ كيف أراقب الأوزان والماء؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "💧", "ميزان الماء\n(صباحاً يومياً)", "1.7 لتر ماء\nلكل 1 كغ علف"),
                InfographicTimelineStep(2, "🌾", "سجل العلف\n(بالقنطار ق)", "1 قنطار = 100 كغ\nوحساب البيع الجزئي"),
                InfographicTimelineStep(3, "⚖️", "هدف الأوزان\n(يوم 7 إلى 42)", "190 غ ➔ 1 كغ\n➔ 2.8 كغ (FCR 1.65)")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات النجاح",
                goodPoints = listOf(
                    "ثبات نسبة الماء 1.65–1.80 ومطابقة الجدول",
                    "إدراج المبيعات الجزئية ضمن الكتلة الحية",
                    "معامل كفاءة EPEF أعلى من 340"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "تجاهل هبوط شرب الماء المفاجئ صباحاً",
                    "حساب FCR دون أوزان الفرز والبيع الجزئي",
                    "إهمال الوزن الأسبوعي لعينة القطيع"
                ),
                goodSummaryCaption = "ماء 1.7x ووزن 2.8 كغ\nبمعامل FCR مثالي",
                badSummaryCaption = "تجاهل هبوط الماء أو\nإهمال حساب البيع الجزئي"
            ),
            tableTitle = "📊 جدول الأوزان القياسية ومعامل FCR نحو 2.8 كغ",
            tableHeaders = listOf("اليوم", "الوزن القياسي", "FCR القياسي", "نسبة الماء/العلف"),
            tableRows = listOf(
                listOf("اليوم 7", "190 غ", "0.85", "1.7 لتر / كغ"),
                listOf("اليوم 14", "520 غ", "1.08", "1.7 لتر / كغ"),
                listOf("اليوم 21", "1,000 غ", "1.25", "1.7 لتر / كغ"),
                listOf("اليوم 28", "1,580 غ", "1.38", "1.7–1.8 لتر / كغ"),
                listOf("اليوم 35", "2,250 غ", "1.50", "1.7–2.2 لتر / كغ"),
                listOf("اليوم 42", "2,800 غ", "1.65", "1.7–2.5 لتر / كغ")
            ),
            warningNote = "⚠️ راقب عداد الماء يومياً؛ الطائر المريض يتوقف عن الشرب قبل النفوق بـ 24 ساعة!"
        )

        else -> null
    }
}

object Ross308PosterCatalog {
    fun forSubCard(id: String): EncyclopediaPosterSpec? {
        return Ross308CareVaxPosters.posterForCare(id)
            ?: Ross308CareVaxPosters.posterForVax(id)
            ?: Ross308DiseaseFeedPosters.posterForDisease(id)
            ?: Ross308DiseaseFeedPosters.posterForFeed(id)
    }

    fun allSixteenOrderedIds(): List<String> = listOf(
        "care_1", "care_2", "care_3", "care_4",
        "vax_1", "vax_2", "vax_3", "vax_4",
        "dis_1", "dis_2", "dis_3", "dis_4",
        "feed_1", "feed_2", "feed_3", "feed_4"
    )
}
