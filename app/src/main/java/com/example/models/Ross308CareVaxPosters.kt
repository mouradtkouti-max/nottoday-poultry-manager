package com.example.models

object Ross308CareVaxPosters {

    fun posterForCare(id: String): EncyclopediaPosterSpec? = when (id) {
        "care_1" -> EncyclopediaPosterSpec(
            lessonNumber = 1,
            visualType = InfographicVisualType.CARE_PREHEAT_PREP,
            specialtyBadge = "التحضين والتدفئة",
            englishSubtitleOrRule = "Disinfection & Pre-Heating (33.5°C Air / 30°C Litter)",
            coreIdea = "تطهير العنبر وتدفئة الهواء (33.5°C) والفرشة (30°C) قبل الوصول بـ 24 ساعة يمنع صدمة البرد.",
            whyImportantPoints = listOf(
                "✅ حرارة ثابتة (33.5°C هواء و30°C فرشة)",
                "✅ عزل برودة الإسمنت بفرشة 5–8 سم شتاءً",
                "✅ تسريع امتصاص كيس المح والحيوية",
                "✅ خفض نفوق الأسبوع الأول المبكر"
            ),
            diagramTitle = "لوحة المشهد البصري: الدفاية المشتعلة وسمك الفرشة 5–8 سم",
            heroCalloutTop = "هواء 33.5°C • فرشة 30°C",
            heroCalloutBottom = "نشارة جافة 5–8 سم",
            polaroidCaption = "تحضين دافئ 33.5°C",
            timelineStepsTitle = "⚙️ كيف أجهّز العنبر؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "🧹", "التطهير والغسل\n(قبل 48 ساعة)", "تنظيف الأرضية\nوتعقيم خطوط الماء"),
                InfographicTimelineStep(2, "🪵", "فرش النشارة\n(قبل 24 ساعة)", "فرشة جافة ونظيفة\nبسمك 5 - 8 سم"),
                InfographicTimelineStep(3, "🔥", "التدفئة المسبقة\n(قبل 24 ساعة)", "هواء 33.5°C\nوفرشة 30°C")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات النجاح",
                goodPoints = listOf(
                    "هواء 33.5°C وفرشة دافئة 30°C",
                    "نشارة جافة بسمك 5–8 سم",
                    "أرجل صوص دافئة عند اللمس"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "استقبال الصيصان في أرضية باردة أو رطبة",
                    "تشغيل الدفايات لحظة الوصول فقط",
                    "فرشة رقيقة (< 3 سم) تسرب البرد"
                ),
                goodSummaryCaption = "عنبر دافئ 33.5°C\nوفرشة جافة 5–8 سم",
                badSummaryCaption = "أرضية باردة أو\nفرشة رطبة (< 3 سم)"
            ),
            warningNote = "⚠️ الأرضية الباردة تسرق حرارة بطن الصوص وأقدامه!"
        )

        "care_2" -> EncyclopediaPosterSpec(
            lessonNumber = 2,
            visualType = InfographicVisualType.CARE_BROODING_BEHAVIOR,
            specialtyBadge = "التحضين والتدفئة",
            englishSubtitleOrRule = "Chick Reception, Distribution & Crop Fill > 95%",
            coreIdea = "توزيع الصيصان المتوازن وامتلاء الحوصلة (>95% خلال 24 ساعة) هما أدق مقياس لنجاح الاستقبال.",
            whyImportantPoints = listOf(
                "✅ توزيع متوازن يكشف راحة الصيصان فوراً",
                "✅ فحص الحوصلة (>95% لينة وممتلئة)",
                "✅ كشف التيارات الباردة قبل ظهور الإجهاد",
                "✅ بلوغ وزن 190–200 غ في اليوم السابع"
            ),
            diagramTitle = "مخطط علوي لتوزيع الصيصان وفحص الحوصلة (>95%)",
            heroCalloutTop = "توزيع متوازن 100%",
            heroCalloutBottom = "امتلاء الحوصلة >95%",
            polaroidCaption = "استقبال وفحص الحوصلة",
            timelineStepsTitle = "⚙️ كيف أراقب الاستقبال والحوصلة؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "💧", "الماء والعلف\n(أول ساعتين)", "ماء فاتر أولاً\nثم علف على الورق"),
                InfographicTimelineStep(2, "🐣", "توزيع الصيصان\n(كل 3 ساعات)", "انتشار متوازن\nوزقزقة هادئة"),
                InfographicTimelineStep(3, "🖐️", "فحص الحوصلة\n(بعد 24 ساعة)", "امتلاء > 95%\nحوصلة لينة ممتلئة")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات النجاح",
                goodPoints = listOf(
                    "توزيع متوازن وزقزقة هادئة",
                    "حوصلة لينة وممتلئة (> 95%)",
                    "أقدام دافئة ونشاط مستمر"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "تجمع تحت الدفاية أو هروب للزوايا",
                    "تجاهل شقوق الستائر والتيارات الباردة",
                    "بقاء الحواصل فارغة بعد 24 ساعة"
                ),
                goodSummaryCaption = "توزيع متوازن 100%\nوامتلاء حوصلة > 95%",
                badSummaryCaption = "تكدس الصيصان أو\nحوصلة فارغة وجافة"
            ),
            warningNote = "⚠️ راقب توزيع الصيصان وجسّ الحواصل، لا تعتمد على الترمومتر وحده!"
        )

        "care_3" -> EncyclopediaPosterSpec(
            lessonNumber = 3,
            visualType = InfographicVisualType.CARE_VENTILATION_AIRFLOW,
            specialtyBadge = "التهوية وجودة الهواء",
            englishSubtitleOrRule = "Minimum Ventilation: NH3 < 10 ppm & Humidity 50–60%",
            coreIdea = "التهوية الدنيا تضبط الأمونيا (<10 ppm) والرطوبة (50–60%) بمزج الهواء النقي في السقف دون تيار بارد.",
            whyImportantPoints = listOf(
                "✅ إبقاء غاز الأمونيا أقل من 10 ppm",
                "✅ ضبط الرطوبة النسبية عند 50–60%",
                "✅ مزج الهواء بالسقف دون تيار بارد",
                "✅ حماية الرئتين وجفاف الفرشة"
            ),
            diagramTitle = "مقطع عرضي للتهوية الدنيا (الفتحات العلوية ➔ السقف ➔ الطيور)",
            heroCalloutTop = "أمونيا < 10 ppm",
            heroCalloutBottom = "رطوبة 50–60% بلا تيار",
            polaroidCaption = "تهوية علوية آمنة",
            timelineStepsTitle = "⚙️ كيف أضبط التهوية الدنيا؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "🪟", "الفتحات العلوية\n(زاوية السقف)", "توجيه الهواء\nنحو سقف العنبر"),
                InfographicTimelineStep(2, "⏲️", "مؤقت الشفاط\n(دورات منتظمة)", "سحب مستمر\nدون صدمة برد"),
                InfographicTimelineStep(3, "💨", "جودة الهواء\n(فحص يومي)", "أمونيا < 10 ppm\nورطوبة 50–60%")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات النجاح",
                goodPoints = listOf(
                    "هواء نقي بلا رائحة أمونيا لاذعة",
                    "رطوبة 50–60% وفرشة هشة جافة",
                    "لا تكثف مائي على الجدران"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "كتم العنبر في البرد لتوفير الغاز",
                    "إسقاط تيار هواء بارد على الطيور",
                    "فرشة مبللة تطلق الأمونيا والكوكسيديا"
                ),
                goodSummaryCaption = "أمونيا < 10 ppm\nوفرشة جافة وهواء نقي",
                badSummaryCaption = "كتم العنبر أو\nتيار بارد على الطيور"
            ),
            warningNote = "⚠️ كل 1% ارتفاع رطوبة فوق 30% بالفرشة يضاعف غاز الأمونيا!"
        )

        "care_4" -> EncyclopediaPosterSpec(
            lessonNumber = 4,
            visualType = InfographicVisualType.CARE_LIGHTING_PRESALE,
            specialtyBadge = "الإضاءة وما قبل البيع",
            englishSubtitleOrRule = "4–6h Darkness, Standard Density & 8–10h Pre-Sale Fasting",
            coreIdea = "تطبيق 4–6 ساعات إظلام والكثافة القياسية وتصويم العلف 8–10 ساعات قبل البيع يحمي القلب والذبيحة.",
            whyImportantPoints = listOf(
                "✅ إظلام 4–6 ساعات يمنع الاستسقاء والموت المفاجئ",
                "✅ كثافة قياسية (10–14 طائر/م²) للنمو المتجانس",
                "✅ تصويم العلف 8–10 ساعات مع بقاء الماء",
                "✅ التأكد من انتهاء فترة السحب الدوائي"
            ),
            diagramTitle = "مخطط برنامج الإظلام (4–6 س) وتصويم ما قبل البيع (8–10 س)",
            heroCalloutTop = "إظلام 4–6 ساعات",
            heroCalloutBottom = "تصويم علف 8–10 ساعات",
            polaroidCaption = "الإظلام وما قبل البيع",
            timelineStepsTitle = "⚙️ كيف أطبّق الإضاءة وتصويم البيع؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "🌙", "برنامج الإظلام\n(بعد اليوم 7)", "4–6 ساعات ظلام\nلتقوية الهيكل"),
                InfographicTimelineStep(2, "🌾", "تصويم العلف\n(قبل 8–10 س)", "رفع المعالف\nمع بقاء الماء"),
                InfographicTimelineStep(3, "💧", "تجهيز الصيد\n(فترة السحب)", "إضاءة خافتة\nوخلو من الأدوية")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات النجاح",
                goodPoints = listOf(
                    "تصويم علف 8–10 س مع بقاء الماء",
                    "كثافة معتدلة وإظلام 4–6 ساعات",
                    "انتهاء فترة السحب قبل التحميل"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "ترك الإضاءة 24 ساعة طوال الدورة",
                    "قطع ماء الشرب أثناء فترة التصويم",
                    "التصويم > 12 ساعة ينقص وزن الذبيحة"
                ),
                goodSummaryCaption = "إظلام 4–6 س وتصويم\n8–10 س مع الماء",
                badSummaryCaption = "إضاءة 24 ساعة أو\nقطع ماء الشرب"
            ),
            warningNote = "⚠️ ارفع العلف 8–10 ساعات قبل البيع ولا تقطع الماء أبداً!"
        )

        else -> null
    }

    fun posterForVax(id: String): EncyclopediaPosterSpec? = when (id) {
        "vax_1" -> EncyclopediaPosterSpec(
            lessonNumber = 1,
            visualType = InfographicVisualType.VAX_RECEPTION_WATER,
            specialtyBadge = "برنامج التحصينات",
            englishSubtitleOrRule = "Hydration, Electrolytes & AD3E Reception Protocol (Days 1-3)",
            coreIdea = "تقديم ماء فاتر (18–21°C) مع أملاح الإماهة وفيتامين AD3E+C خلال الساعات الـ 6 الأولى يعوض إجهاد النقل وينشط المناعة.",
            whyImportantPoints = listOf(
                "✅ يعوض السوائل والأملاح المفقودة أثناء الفقس والنقل",
                "✅ ينشط الأغشية المخاطية لاستقبال لقاحات الأسبوع الأول",
                "✅ يسرّع امتصاص كيس المح ويرفع حيوية الصيصان",
                "✅ يحمي الكلى ويمنع الجفاف المبكر والتصاق المجمع"
            ),
            diagramTitle = "مخطط تحضير ماء الاستقبال (18-21°C) ومنحنى المناعة الأولية",
            heroCalloutTop = "ماء فاتر 18–21°C",
            heroCalloutBottom = "إلكترولايت + AD3E",
            polaroidCaption = "إماهة ومناعة اليوم 1",
            timelineStepsTitle = "⚙️ كيف أطبّق برنامج الاستقبال المائي؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "🚿", "اغسل وعقّم خزان وخطوط الشرب ثم اشطفها جيداً", "تخلص من البيوفيلم وبقايا المطهرات قبل وصول الكتاكيت"),
                InfographicTimelineStep(2, "🌡️", "اضبط حرارة الماء عند 18 - 21°C وأضف الإلكترولايت", "قدّم المحلول أول 6–12 ساعة لتعويض الجفاف فوراً"),
                InfographicTimelineStep(3, "🧪", "قدّم فيتامين AD3E + C لمدة 3 أيام متتالية", "مع خفض ارتفاع المساقي وضبط ضغط الحلمات للصوص")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات الجاهزية",
                goodPoints = listOf(
                    "ماء نظيف فاتر (18–21°C) خالٍ من الكلور والرواسب",
                    "وصول أكثر من 95% من الصيصان للماء خلال ساعتين",
                    "حيوية عالية وحوصلة لينة ممتلئة بالماء والعلف"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "لا تقدم ماءً بارداً جداً أو ساخناً من الأنابيب تحت الدفايات",
                    "الاستخدام العشوائي للمضادات الحيوية القوية في اليوم الأول",
                    "تقديم العلف قبل أن تشرب الصيصان الماء وتترطب أمعاؤها"
                ),
                goodSummaryCaption = "ماء فاتر نقي وفيتامينات AD3E تمنح انطلاقة مناعية قوية",
                badSummaryCaption = "ماء ساخن في الأنابيب أو مضادات عشوائية تضعف الكلى والأمعاء"
            ),
            tableTitle = "📊 التأثير على مناعة وحيوية القطيع",
            tableHeaders = listOf("المؤشر", "تطبيق صحيح", "إهمال الاستقبال"),
            tableRows = listOf(
                listOf("نسبة الحيوية بالأسبوع 1", "99.0%+ ✓", "تراجع 2–4% ↓"),
                listOf("وزن اليوم السابع", "190–205 غ ↑", "أقل من 165 غ ↓"),
                listOf("استجابة لقاح اليوم 7", "مناعة متجانسة ✓", "تفاوت الأجسام المضادة ↓")
            ),
            warningNote = "⚠️ الماء هو اللقاح الأول للصوص؛ نظافة الخطوط يومياً تمنع 70% من إسهالات الأسبوع الأول."
        )

        "vax_2" -> EncyclopediaPosterSpec(
            lessonNumber = 2,
            visualType = InfographicVisualType.VAX_NEWCASTLE_EYE_SPRAY,
            specialtyBadge = "برنامج التحصينات",
            englishSubtitleOrRule = "Newcastle (Hitchner B1 / Clone 30) + IB (Days 6-7)",
            coreIdea = "التحصين بالتقطير العيني أو الرش الخشن في اليوم 6–7 يبني مناعة موضعية قوية في غدة هاردير والقصبة الهوائية ضد النيوكاسل وIB.",
            whyImportantPoints = listOf(
                "✅ التقطير العيني يضمن حصول كل طائر على جرعة كاملة 100%",
                "✅ الرش الخشن (100–150 ميكرون) يغطي الأغشية التنفسية العليا",
                "✅ سلسلة التبريد (2–8°C) تحافظ على حيوية الفيروس اللقاحي",
                "✅ يسد فجوة المناعة بعد انخفاض الأجسام المضادة الأمية"
            ),
            diagramTitle = "مخطط التقطير العيني والرش الخشن (100-150µm) في اليوم 6-7",
            heroCalloutTop = "سلسلة تبريد 2–8°C",
            heroCalloutBottom = "تقطير عيني / رش خشن",
            polaroidCaption = "تحصين اليوم 6-7",
            timelineStepsTitle = "⚙️ كيف أنفّذ التحصين التنفسي الأول؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "🧊", "احفظ الأمبولات في الثلج وأذبها بماء مقطر بارد", "تأكد من خلو الماء تماماً من الكلور أو أي مطهر"),
                InfographicTimelineStep(2, "🌬️", "أوقف الشفاطات وخفّض الإضاءة لمدة 20 دقيقة", "لتهدئة الطيور ومنع تطاير رذاذ اللقاح خارج العنبر"),
                InfographicTimelineStep(3, "⏱️", "أنهِ التحصين خلال 45 دقيقة وادعم بـ AD3E+C", "لتخفيف رد الفعل التنفسي وتنشيط تكوين الأجسام المضادة")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات الجاهزية",
                goodPoints = listOf(
                    "قطيع سليم ونشيط وقطرات رش متجانسة تستقر بالعين",
                    "إنهاء اللقاح بسرعة قبل ارتفاع حرارة المحلول",
                    "إعادة تشغيل التهوية اللطيفة بعد 20 دقيقة من الرش"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "لا تستخدم ماء الصنبور المكلور أو الرش الضبابي الناعم جداً",
                    "تحصين قطيع يعاني من برد أو إصابة تنفسية نشطة",
                    "ترك المراوح تعمل أثناء الرش مما يضيع اللقاح في الهواء"
                ),
                goodSummaryCaption = "تقطير عيني أو رش خشن مضبوط يمنح مناعة موضعية 98%+",
                badSummaryCaption = "كلور في الماء أو رش ضبابي ناعم يفسد اللقاح ويجهد الرئة"
            ),
            tableTitle = "📊 التأثير على مناعة القطيع",
            tableHeaders = listOf("المعيار", "التغطية المستهدفة", "الفائدة الحقلية"),
            tableRows = listOf(
                listOf("تغطية التقطير العيني", "98% – 100%", "أعلى مناعة موضعية IgA"),
                listOf("تغطية الرش الخشن", "92% – 95%", "سرعة تنفيذ للقطعان الكبيرة"),
                listOf("توقيت ذروة المناعة", "بعد 7–10 أيام", "حماية ضد IB وNDV")
            ),
            warningNote = "⚠️ يُمنع تحصين القطيع المريض أو المجهد حرارياً دون استشارة الطبيب البيطري."
        )

        "vax_3" -> EncyclopediaPosterSpec(
            lessonNumber = 3,
            visualType = InfographicVisualType.VAX_GUMBORO_DRINKING,
            specialtyBadge = "برنامج التحصينات",
            englishSubtitleOrRule = "Gumboro (IBD) Drinking Water Vaccination (Days 12-14)",
            coreIdea = "لقاح الجمبورو في مياه الشرب يحمي غدة فابريشيوس (مصنع المناعة) ويتطلب إزالة الكلور تماماً وتعطيشاً محسوباً.",
            whyImportantPoints = listOf(
                "✅ يحمي غدة فابريشيوس من التدمير وفقدان المناعة",
                "✅ إزالة الكلور والحليب الخالي الدسم يحميان الفيروس اللقاحي",
                "✅ التعطيش المحسوب (1–2 ساعة) يضمن شرب جميع الطيور معاً",
                "✅ يمنع العدوى الثانوية بـ E.coli والكوكسيديا"
            ),
            diagramTitle = "مخطط تحصين مياه الشرب (تعطيش -> مثبت حليب -> فتح تحت الماء)",
            heroCalloutTop = "ماء منزوع الكلور",
            heroCalloutBottom = "فتح الأمبولة تحت الماء",
            polaroidCaption = "تحصين الجمبورو 12-14",
            timelineStepsTitle = "⚙️ كيف أحصّن الجمبورو في ماء الشرب؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "🚫", "أوقف الكلور والمطهرات قبل التحصين بـ 48 ساعة", "واغسل خطوط الشرب بالماء النقي فقط"),
                InfographicTimelineStep(2, "⏳", "عطّش القطيع لمدة 1 - 2 ساعة في الصباح الباكر", "لا تزد مدة التعطيش في الطقس الحار لتجنب التدافع"),
                InfographicTimelineStep(3, "🥛", "أضف مثبت اللقاح (2 غ/لتر حليب) وافتح الأمبولة تحت الماء", "وزّع المحلول ليُشرب بالكامل خلال 90–120 دقيقة")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات الجاهزية",
                goodPoints = listOf(
                    "ماء نقي مع مثبت حليب خالي الدسم قبل اللقاح بـ 15 دقيقة",
                    "شرب جميع الطيور بهدوء وانتهاء المحلول خلال ساعتين",
                    "حماية كاملة لغدة فابريشيوس وتجانس مناعي عالٍ"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "لا تترك أي أثر للكلور أو اليود أو الأحماض في خزان اللقاح",
                    "فتح الأمبولات في الهواء الساخن أو بقاء المحلول أكثر من 3 ساعات",
                    "التعطيش الجائر الذي يسبب زحاماً وغرقاً حول المساقي"
                ),
                goodSummaryCaption = "ماء بلا كلور + مثبت حليب + شرب كامل خلال ساعتين",
                badSummaryCaption = "بقايا كلور ومطهرات تقتل لقاح الجمبورو خلال ثوانٍ"
            ),
            tableTitle = "📊 التأثير على مناعة القطيع",
            tableHeaders = listOf("العنصر", "بدون تحصين سليم", "مع بروتوكول IQ Notebook"),
            tableRows = listOf(
                listOf("سلامة غدة فابريشيوس", "ضمور وتلف مناعي ↓", "مناعة متكاملة ✓"),
                listOf("التهاب ثانوي (E.coli)", "ارتفاع حاد ↑", "مقاومة عالية ✓"),
                listOf("التجانس النهائي", "تفاوت كبير ↓", "تجانس > 90% ✓")
            ),
            warningNote = "⚠️ افتح أمبولة اللقاح دائماً وهي مغمورة تحت سطح الماء حتى لا يفقد التركيز الفيروسي."
        )

        "vax_4" -> EncyclopediaPosterSpec(
            lessonNumber = 4,
            visualType = InfographicVisualType.VAX_BOOSTER_DETOX,
            specialtyBadge = "برنامج التحصينات",
            englishSubtitleOrRule = "Newcastle Booster (LaSota / Clone 30) + Mycotoxin Detox (Days 18-20)",
            coreIdea = "الجرعة التنشيطية في اليوم 18–20 ترفع الأجسام المضادة لنيوكاسل حتى التسويق، مع دعم الكبد والكلى بمضادات السموم الفطرية.",
            whyImportantPoints = listOf(
                "✅ ترفع حائط الصد المناعي ضد النيوكاسل حتى عمر البيع",
                "✅ مضاد السموم الفطرية يحمي الكبد من سموم العلف النامي والناهي",
                "✅ الغسيل الكلوي ينشط الأيض ويزيد سحب العلف",
                "✅ يقلل النفوق المتأخر في الأوزان الثقيلة بعد اليوم 28"
            ),
            diagramTitle = "مخطط ذروة المناعة بعد الجرعة المعززة (اليوم 18-20) وحماية الكبد",
            heroCalloutTop = "تعزيز نيوكاسل صباحاً",
            heroCalloutBottom = "مضاد سموم + غسيل كلوي",
            polaroidCaption = "الجرعة المعززة 18-20",
            timelineStepsTitle = "⚙️ كيف أنفّذ الجرعة المعززة وحماية الكبد؟",
            timelineSteps = listOf(
                InfographicTimelineStep(1, "🌅", "قدّم لقاح LaSota / Clone 30 عند الفجر في ماء نقي", "مع مثبت حليب خالي الدسم وإنهاء الشرب خلال ساعتين"),
                InfographicTimelineStep(2, "🌬️", "اضبط التهوية الدافئة وموسع الشعب لمدة 48 ساعة", "لتجنب احتقان القصبة الهوائية بعد الجرعة التنشيطية"),
                InfographicTimelineStep(3, "🧪", "قدّم كورس مضاد سموم فطرية ومنشط كبدي 3 أيام", "لحماية الكبد والكلى عند الانتقال للعلف الناهي عالي الطاقة")
            ),
            comparisonPair = InfographicComparisonPair(
                goodTitle = "✅ علامات الجاهزية",
                goodPoints = listOf(
                    "تنفيذ اللقاح صباحاً لقطيع سليم تنفسياً وبماء منزوع الكلور",
                    "دعم بفيتامين AD3E+C وموسع شعب بعد 24 ساعة",
                    "كبد سليم وتحويل ممتاز حتى نهاية الدورة"
                ),
                badTitle = "❌ تجنّب هذا الخطأ",
                badPoints = listOf(
                    "لا تخلط اللقاح مع مضادات حيوية أو فيتامينات في نفس الخزان",
                    "إعطاء عترة LaSota قوية لقطيع مصاب بـ CRD دون علاج مسبق",
                    "إهمال مضادات السموم عند تغيير شحنة العلف"
                ),
                goodSummaryCaption = "مناعة عالية مستمرة وكبد نشيط حتى يوم التسويق",
                badSummaryCaption = "خلط اللقاح بالأدوية أو إهمال السموم الفطرية في العلف"
            ),
            tableTitle = "📊 التأثير على مناعة القطيع",
            tableHeaders = listOf("المرحلة", "حالة المناعة", "الإجراء الداعم"),
            tableRows = listOf(
                listOf("اليوم 18–20", "تعزيز LaSota / Clone 30", "حماية ضد النيوكاسل حتى البيع"),
                listOf("اليوم 21–24", "كورس مضاد سموم + غسيل كلوي", "حماية الكبد من إجهاد العلف الناهي"),
                listOf("اليوم 28+", "فيتامينات AD3E + C عند اللزوم", "دعم التحويل والحيوية")
            ),
            warningNote = "⚠️ لا تخلط أي دواء أو فيتامين أو كلور داخل خزان ماء التحصين أبداً."
        )

        else -> null
    }
}
