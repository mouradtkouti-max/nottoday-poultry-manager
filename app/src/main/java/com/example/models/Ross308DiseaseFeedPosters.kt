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
            englishSubtitleOrRule = "Starter Feed Phase (Days 1-10): 23% Protein • 3000 kcal/kg",
            coreIdea = "تغذية الأيام الـ 10 الأولى بالبروتين العالي (23%) وقوام المحبب الناعم (Crumble) تبني الجهاز الهضمي والهيكل الأساسي للقطيع.",
            whyImportantPoints = listOf(
                "✅ بروتين 23% وطاقة 3000 kcal يبنيان العضلات والأعضاء الحيوية",
                "✅ قوام محبب ناعم (Crumble) يناسب منقار الصوص الصغير",
                "✅ يحقق هدف وزن اليوم السابع (190–200 غ) بمعامل FCR ~0.85",
                "✅ ينشط الزغابات المعوية ويرفع كفاءة الهضم طوال الدورة"
            ),
            diagramTitle = "لوحة تغذية البادئ (Crumble 23%) وأطباق التحضين في الأيام 1-10",
            heroCalloutTop = "بروتين 23% • Crumble",
            heroCalloutBottom = "هدف اليوم 7: 190–200غ",
            polaroidCaption = "علف بادئ محبب ناعم",
            timelineStepsTitle = "⚙️ كيف أدير العلف البادئ في الأيام 1–10؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "🥣", "افرد ورق التحضين وأطباق العلف في 50% من المساحة", "قدّم وجبات طازجة متكررة لتحفيز الصيصان على الأكل"),
                InfographicTimelineStep(2, "🧹", "نظّف الأطباق من الشوائب والفرشة يومياً", "ولا تملأ الطبق لأكثر من ثلثه لمنع نثر العلف وهدره"),
                InfographicTimelineStep(3, "🔄", "طبّق خلطاً انتقالياً في اليوم 9 - 10 (75% بادئ + 25% نامي)", "لمنع صدمة الجهاز الهضمي عند التحول للعلف النامي")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات الجاهزية",
                goodPoints = listOf(
                    "امتلاء الحوصلة لدى > 95% من الصيصان بعد 24 ساعة",
                    "علف محبب ناعم متجانس خالٍ من الغبار والبودرة",
                    "الوصول لوزن 190–200 غ باليوم 7 و310 غ باليوم 10"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "لا تقدم علفاً خشناً كبير الحبة أو تملأ الأطباق حتى تفيض بالفرشة",
                    "ترك العلف يتكتل تحت رطوبة الدفايات والمساقي",
                    "تغيير نوع العلف فجأة في يوم واحد دون تدرج"
                ),
                goodSummaryCaption = "علف بادئ ناعم طازج ووزن مثالي 190 غ في اليوم السابع",
                badSummaryCaption = "علف خشن أو أطباق مملوءة تفيض بالفرشة وتسبب هدر العلف"
            ),
            tableTitle = "📊 جدول المواصفات والأهداف القياسية (الأيام 1–10)",
            tableHeaders = listOf("البند", "المواصفة القياسية", "الهدف المستهدف"),
            tableRows = listOf(
                listOf("البروتين والطاقة", "23% بروتين • 3000 kcal", "قوام محبب ناعم Crumble"),
                listOf("الوزن اليوم 7 / 10", "190 غ / 310 غ", "4.7x وزن الفقس باليوم 7"),
                listOf("الاستهلاك التراكمي", "~270 غ / طائر", "معامل تحويل FCR ~0.92")
            ),
            warningNote = "⚠️ كل يوم تأخير في نمو الأسبوع الأول لا يُعوَّض بالكامل قبل البيع."
        )

        "feed_2" -> EncyclopediaPosterSpec(
            lessonNumber = 2,
            visualType = InfographicVisualType.FEED_GROWER_HEIGHT,
            specialtyBadge = "برنامج التغذية",
            englishSubtitleOrRule = "Grower Feed (Days 11-21) & Feeder/Drinker Back-Level Alignment",
            coreIdea = "ضبط ارتفاع المعالف والمساقي يومياً بمستوى ظهر الطائر يمنع هدر العلف بنسبة 3% إلى 5% ويرفع كفاءة النمو في مرحلة النامي.",
            whyImportantPoints = listOf(
                "✅ يرفع الوزن من 310 غ إلى 1.0 كغ (بروتين 22% • 3100 kcal)",
                "✅ ضبط حافة المعلف بمستوى ظهر الطائر يمنع نثر العلف بالفرشة",
                "✅ ضبط ارتفاع المساقي يمنع بلل الفرشة والكوكسيديا",
                "✅ يحافظ على تجانس القطيع (CV% < 10%) ومعامل FCR ~1.25"
            ),
            diagramTitle = "مخطط ضبط ارتفاع المعلف والمسقى بمستوى ظهر الطائر (الأيام 11-21)",
            heroCalloutTop = "المعلف بمستوى الظهر",
            heroCalloutBottom = "توفير 3%–5% من العلف",
            polaroidCaption = "ضبط ارتفاع المعالف",
            timelineStepsTitle = "⚙️ كيف أضبط المعالف في مرحلة النامي؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "📏", "ارفع سلاسل المعالف والمساقي كل 2 - 3 أيام", "بحيث توازي حافة طبق العلف مستوى ظهر الطائر تماماً"),
                InfographicTimelineStep(2, "🥣", "اضبط بوابة الطبق ليبقى العلف عند ثلث الارتفاع", "مما يمنع الطيور من اللعب بالعلف وبعثرته على الأرض"),
                InfographicTimelineStep(3, "⚖️", "زِن عينة أسبوعية (100 طائر) في اليوم 14 و21", "وقارن متوسط الوزن مع معيار Ross 308 (520 غ ثم 1000 غ)")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات الجاهزية",
                goodPoints = listOf(
                    "حافة المعلف بمستوى ظهر الطائر ولا يوجد علف مبعثر بالفرشة",
                    "مساقي مضبوطة الارتفاع والضغط وفرشة جافة تماماً",
                    "بلوغ وزن 1.0 كغ في اليوم 21 بمعامل تحويل 1.25"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "لا تترك المعالف منخفضة على الأرض فتهدر 4% من فاتورة العلف",
                    "ملء الأطباق حتى الحافة أو ترك طيور تدخل داخل المعلف",
                    "إهمال وزن العينة الأسبوعية وعدم اكتشاف التفاوت مبكراً"
                ),
                goodSummaryCaption = "معالف بمستوى ظهر الطائر، صفر هدر، ووزن 1 كغ باليوم 21",
                badSummaryCaption = "معالف منخفضة على الأرض تسبب نثر العلف وتلوثه بالفرشة"
            ),
            tableTitle = "📊 جدول أهداف مرحلة النامي (الأيام 11–21)",
            tableHeaders = listOf("العمر", "الوزن القياسي", "FCR المستهدف"),
            tableRows = listOf(
                listOf("اليوم 14", "520 غ", "1.08"),
                listOf("اليوم 21", "1,000 غ (1.0 كغ)", "1.25"),
                listOf("مواصفة النامي", "22% بروتين • 3100 kcal", "استهلاك المرحلة ~1.0 كغ/طائر")
            ),
            warningNote = "⚠️ إذا وجدت علفاً داخل الفرشة فالمعلف منخفض جداً أو ممتلئ أكثر من اللازم."
        )

        "feed_3" -> EncyclopediaPosterSpec(
            lessonNumber = 3,
            visualType = InfographicVisualType.FEED_FINISHER_PELLETS,
            specialtyBadge = "برنامج التغذية",
            englishSubtitleOrRule = "Finisher Feed (Days 22-45): 20.5% Protein • 3200 kcal • 3.5mm Pellets",
            coreIdea = "تمثل مرحلة الناهي أكثر من 65% من تكلفة العلف الكلية؛ لذا فإن جودة الحبة (Pellets) وإدارة المخزون بالقنطار هما مفتاح صافي الربح.",
            whyImportantPoints = listOf(
                "✅ طاقة 3200 kcal/kg تدفع زيادة الوزن اليومية حتى 2.95 كغ",
                "✅ حبيبات Pellets متماسكة (3.5 ملم) تقلل الفاقد وتزيد السحب",
                "✅ متابعة المخزون بالقنطار (1 ق = 100 كغ) تحمي من انقطاع العلف",
                "✅ ضبط موعد البيع قبل تدهور FCR يعظم هامش الربح الصافي"
            ),
            diagramTitle = "مخطط إدارة علف الناهي (65% من التكلفة) والمخزون بالقنطار (ق)",
            heroCalloutTop = "حبيبات Pellets 3.5mm",
            heroCalloutBottom = "المخزون بالقنطار (ق)",
            polaroidCaption = "علف ناهي عالي الطاقة",
            timelineStepsTitle = "⚙️ كيف أدير العلف الناهي والمخزون؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "🔍", "افحص صلابة حبيبات الـ Pellets وخلوها من البودرة", "وارفض الشحنات ذات نسبة الناعم العالية أو الرطوبة"),
                InfographicTimelineStep(2, "📦", "راقب رصيد المخزن بالقنطار (ق) واطلب قبل النفاد بـ 3 أيام", "احفظ الأكياس على طبالي خشبية بعيداً عن رطوبة الجدران"),
                InfographicTimelineStep(3, "⚖️", "احسب تكلفة الكيلو أسبوعياً وحدد العمر الأمثل للبيع", "استخدم الميزان السريع لتسجيل المبيعات الجزئية والكلية")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات الجاهزية",
                goodPoints = listOf(
                    "حبيبات Pellets متماسكة ومخزون كافٍ مسجل بالقنطار (ق)",
                    "معامل تحويل FCR بين 1.48 و1.65 عند عمر التسويق",
                    "تخزين جاف ومهوى يمنع نمو الفطريات والسموم"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "لا تقدم علفاً مفتتاً كالبودرة أو تترك المخزن ينفد فجأة",
                    "تخزين أكياس العلف مباشرة على الأرضية الرطبة",
                    "تأخير بيع القطيع بعد اليوم 45 دون جدوى اقتصادية"
                ),
                goodSummaryCaption = "حبيبات Pellets صلبة، مخزون آمن بالقنطار، وربحية عالية",
                badSummaryCaption = "علف مفتت كبودرة أو انقطاع مفاجئ للعلف يوقف نمو القطيع"
            ),
            tableTitle = "📊 جدول أهداف مرحلة الناهي (الأيام 22–45)",
            tableHeaders = listOf("العمر", "الوزن القياسي", "FCR المستهدف"),
            tableRows = listOf(
                listOf("اليوم 28", "1.58 كغ", "1.38"),
                listOf("اليوم 35", "2.25 كغ", "1.50"),
                listOf("اليوم 42", "2.95 كغ", "1.65")
            ),
            warningNote = "⚠️ خفض هدر العلف بنسبة 5% فقط في مرحلة الناهي يعادل زيادة صافية ضخمة في هامش ربح الدورة."
        )

        "feed_4" -> EncyclopediaPosterSpec(
            lessonNumber = 4,
            visualType = InfographicVisualType.FEED_WEIGHT_WATER_CURVE,
            specialtyBadge = "برنامج التغذية",
            englishSubtitleOrRule = "45-Day Ross 308 Weight/FCR Curve & 1.7x Water-to-Feed Rule",
            coreIdea = "يستهلك الطائر 1.6 إلى 1.8 لتر ماء لكل 1 كغ علف؛ وأي انخفاض مفاجئ في شرب الماء هو أسرع إنذار مبكر للمرض قبل ظهور النافق بـ 24 ساعة.",
            whyImportantPoints = listOf(
                "✅ يحافظ على توازن الماء والعلف (1.7 لتر لكل 1 كغ علف)",
                "✅ يكشف بداية الأمراض مبكراً قبل 24 ساعة من ارتفاع النفوق",
                "✅ يحسب FCR الحقيقي شاملاً أوزان المبيعات الجزئية (Thinning)",
                "✅ يربط استهلاك القنطار (ق) اليومي بمنحنى وزن Ross 308"
            ),
            diagramTitle = "منحنى الوزن القياسي Ross 308 (اليوم 1-45) وميزان الماء والعلف (1.7x)",
            heroCalloutTop = "1.7 لتر ماء / 1 كغ علف",
            heroCalloutBottom = "هدف 42 يوم: 2.95 كغ",
            polaroidCaption = "منحنى الوزن والماء",
            timelineStepsTitle = "⚙️ كيف أراقب منحنى الوزن والماء يومياً؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "💧", "سجّل قراءة استهلاك الماء والعلف صباح كل يوم", "وتأكد أن نسبة الماء للعلف بين 1.6 و1.8 في الجو المعتدل"),
                InfographicTimelineStep(2, "🌾", "وثّق العلف المستهلك بالقنطار (1 ق = 100 كغ) في السجل", "ليتم خصمه آلياً من المخزن وحساب FCR الفعلي بدقة"),
                InfographicTimelineStep(3, "📈", "قارن متوسط وزن عنبرك مع جدول Ross 308 القياسي", "واستخدم مؤشر الكفاءة الأوروبي EPEF لتقييم نجاح الدورة")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات الجاهزية",
                goodPoints = listOf(
                    "ثبات نسبة الماء إلى العلف عند 1.65–1.80 ومطابقة وزن Ross 308",
                    "تسجيل المبيعات الجزئية (الفرز) بدقة ضمن الكتلة الحية",
                    "مؤشر كفاءة أوروبي (EPEF) أعلى من 340 نقطة"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "لا تتجاهل هبوط استهلاك الماء اليومي أو انسداد فلاتر الشرب",
                    "حساب FCR على الطيور المتبقية فقط بعد البيع الجزئي",
                    "ترك السجل اليومي بدون تدوين العلف والماء والنافق"
                ),
                goodSummaryCaption = "توازن مثالي بين الماء والعلف ومنحنى وزن يطابق Ross 308",
                badSummaryCaption = "تجاهل هبوط شرب الماء أو إهمال تسجيل العلف والأوزان"
            ),
            tableTitle = "📊 جدول الأوزان القياسية واستهلاك العلف وميزان الماء (1–45 يوم)",
            tableHeaders = listOf("اليوم", "الوزن القياسي", "FCR القياسي", "نسبة الماء/العلف"),
            tableRows = listOf(
                listOf("اليوم 7", "190 غ", "0.85", "1.7 لتر / كغ"),
                listOf("اليوم 14", "520 غ", "1.08", "1.7 لتر / كغ"),
                listOf("اليوم 21", "1,000 غ", "1.25", "1.7 لتر / كغ"),
                listOf("اليوم 28", "1,580 غ", "1.38", "1.7–1.8 لتر / كغ"),
                listOf("اليوم 35", "2,250 غ", "1.50", "1.7–2.2 لتر / كغ"),
                listOf("اليوم 42–45", "2,800–2,950 غ", "1.65", "1.7–2.5 لتر / كغ")
            ),
            warningNote = "⚠️ راقب عداد الماء يومياً قبل فحص المعالف؛ الطائر المريض يتوقف عن الشرب أولاً!"
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
