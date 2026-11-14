package com.example.models

enum class InfographicVisualType {
    CARE_PREHEAT_PREP,
    CARE_BROODING_BEHAVIOR,
    CARE_VENTILATION_AIRFLOW,
    CARE_LIGHTING_PRESALE,
    VAX_RECEPTION_WATER,
    VAX_NEWCASTLE_EYE_SPRAY,
    VAX_GUMBORO_DRINKING,
    VAX_BOOSTER_DETOX,
    DIS_IB_BRONCHITIS,
    DIS_CRD_MYCOPLASMA,
    DIS_COCCIDIOSIS_GUT,
    DIS_ND_IBD_HEAT,
    FEED_STARTER_SPEC,
    FEED_GROWER_HEIGHT,
    FEED_FINISHER_PELLETS,
    FEED_WEIGHT_WATER_CURVE
}

data class InfographicTimelineStep(
    val stepNumber: Int,
    val emoji: String,
    val title: String,
    val subtitle: String
)

data class InfographicComparisonPair(
    val goodTitle: String,
    val goodPoints: List<String>,
    val badTitle: String,
    val badPoints: List<String>,
    val goodSummaryCaption: String = "",
    val badSummaryCaption: String = ""
)

data class InfographicAnatomyLabel(
    val partName: String,
    val lesionNote: String
)

data class InfographicSymptomCard(
    val emoji: String,
    val title: String,
    val subtitle: String
)

data class InfographicPerformanceMetric(
    val metricLabel: String,
    val deltaValue: String,
    val isNegativeImpact: Boolean = true
)

data class EncyclopediaPosterSpec(
    val lessonNumber: Int = 1,
    val visualType: InfographicVisualType,
    val specialtyBadge: String,
    val englishSubtitleOrRule: String,
    val coreIdea: String,
    val whyImportantPoints: List<String> = emptyList(),
    val diagramTitle: String,
    val heroCalloutTop: String = "سخّن العنبر مسبقاً",
    val heroCalloutBottom: String = "فرشة نظيفة وجافة",
    val polaroidCaption: String = "تحضين مثالي 33°C",
    val timelineStepsTitle: String = "",
    val timelineSteps: List<InfographicTimelineStep> = emptyList(),
    val comparisonPair: InfographicComparisonPair? = null,
    val actionStepsTitle: String = "",
    val actionSteps: List<String> = emptyList(),
    val warningNote: String = "",
    // Medical Disease Anatomy & Performance Book Page Layout (Branch 3)
    val isMedicalSixSectionLayout: Boolean = false,
    val sec1Definition: String = "",
    val quickFacts: List<Pair<String, String>> = emptyList(),
    val anatomyLabels: List<InfographicAnatomyLabel> = emptyList(),
    val zoomInsetTitle: String = "التهاب الشعب الهوائية وتراكم المخاط",
    val pathogenCode: String = "IBV",
    val sec2SymptomCards: List<InfographicSymptomCard> = emptyList(),
    val sec3TreatmentSteps: List<String> = emptyList(),
    val sec4PerformanceTable: List<InfographicPerformanceMetric> = emptyList(),
    val sec5DiagnosisPoints: List<String> = emptyList(),
    val sec6PreventionIcons: List<Pair<String, String>> = emptyList(),
    val sec6ImportantNote: String = "",
    // Optional performance/target table for Vaccination & Feeding posters
    val tableTitle: String = "",
    val tableHeaders: List<String> = emptyList(),
    val tableRows: List<List<String>> = emptyList()
) {
    val lessonNumberFormatted: String
        get() = String.format(java.util.Locale.US, "%02d", lessonNumber.coerceIn(1, 99))

    val branchLessonNumber: Int
        get() = ((lessonNumber.coerceAtLeast(1) - 1) % 4) + 1

    val branchLessonNumberFormatted: String
        get() = String.format(java.util.Locale.US, "%02d", branchLessonNumber)
}
