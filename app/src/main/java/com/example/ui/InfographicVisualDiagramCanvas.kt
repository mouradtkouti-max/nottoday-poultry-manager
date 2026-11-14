package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.EncyclopediaPosterSpec
import com.example.models.InfographicVisualType
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InfographicVisualDiagramCanvas(
    spec: EncyclopediaPosterSpec,
    isDark: Boolean,
    accentColor: Color,
    primaryText: Color,
    secondaryText: Color,
    modifier: Modifier = Modifier
) {
    val panelBg = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFBF2)
    val panelBorder = if (isDark) Color(0xFF334155) else Color(0xFFE7D8B9)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("infographic_canvas_${spec.visualType.name.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        color = panelBg,
        border = BorderStroke(1.2.dp, panelBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "🎨 ${spec.diagramTitle}",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                lineHeight = 17.sp
            )

            when (spec.visualType) {
                InfographicVisualType.CARE_PREHEAT_PREP -> PreheatFloorComparisonDiagram(isDark, accentColor, primaryText, secondaryText)
                InfographicVisualType.CARE_BROODING_BEHAVIOR -> BrooderDistributionQuadDiagram(isDark, primaryText, secondaryText)
                InfographicVisualType.CARE_VENTILATION_AIRFLOW -> BarnCrossSectionAirflowDiagram(isDark, accentColor, primaryText, secondaryText)
                InfographicVisualType.CARE_LIGHTING_PRESALE -> LightingAndWithdrawalDiagram(isDark, accentColor, primaryText, secondaryText)
                InfographicVisualType.DIS_IB_BRONCHITIS,
                InfographicVisualType.DIS_CRD_MYCOPLASMA,
                InfographicVisualType.DIS_COCCIDIOSIS_GUT,
                InfographicVisualType.DIS_ND_IBD_HEAT -> MedicalAnatomyCanvasSection(spec, isDark, primaryText, secondaryText)
                InfographicVisualType.VAX_RECEPTION_WATER,
                InfographicVisualType.VAX_NEWCASTLE_EYE_SPRAY,
                InfographicVisualType.VAX_GUMBORO_DRINKING,
                InfographicVisualType.VAX_BOOSTER_DETOX -> VaccinationRouteAndImmunityDiagram(spec.visualType, isDark, accentColor, primaryText, secondaryText)
                InfographicVisualType.FEED_STARTER_SPEC,
                InfographicVisualType.FEED_GROWER_HEIGHT,
                InfographicVisualType.FEED_FINISHER_PELLETS,
                InfographicVisualType.FEED_WEIGHT_WATER_CURVE -> FeedingAndWeightCurveDiagram(spec.visualType, isDark, accentColor, primaryText, secondaryText)
            }
        }
    }
}

@Composable
private fun PreheatFloorComparisonDiagram(
    isDark: Boolean,
    accentColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Warm Prepared Litter
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = if (isDark) Color(0xFF064E3B).copy(alpha = 0.45f) else Color(0xFFECFDF5),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(84.dp)) {
                    val w = size.width
                    val h = size.height
                    // Litter layer 5-8 cm
                    drawRoundRect(
                        color = Color(0xFFD97706).copy(alpha = 0.35f),
                        topLeft = Offset(0f, h * 0.68f),
                        size = Size(w, h * 0.30f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    // Heat waves rising
                    for (i in 1..4) {
                        val x = w * (i * 0.2f)
                        drawLine(
                            color = Color(0xFFEA580C),
                            start = Offset(x, h * 0.66f),
                            end = Offset(x, h * 0.44f),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }
                    // Happy spread chicks
                    val chickOffsets = listOf(0.22f, 0.48f, 0.76f)
                    chickOffsets.forEach { cx ->
                        drawCircle(
                            color = Color(0xFFF59E0B),
                            radius = h * 0.11f,
                            center = Offset(w * cx, h * 0.50f)
                        )
                    }
                    // Heater glow top
                    drawRoundRect(
                        color = Color(0xFFEA580C),
                        topLeft = Offset(w * 0.32f, h * 0.06f),
                        size = Size(w * 0.36f, h * 0.14f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
                Text("✅ فرشة 29–30°C (5-8 سم)", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = accentColor, textAlign = TextAlign.Center)
                Text("أقدام دافئة وانتشار مريح", fontSize = 10.sp, color = secondaryText, textAlign = TextAlign.Center)
            }
        }

        // Cold Concrete Floor
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            color = if (isDark) Color(0xFF450A0A).copy(alpha = 0.45f) else Color(0xFFFEF2F2),
            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(84.dp)) {
                    val w = size.width
                    val h = size.height
                    // Cold concrete slab
                    drawRoundRect(
                        color = Color(0xFF60A5FA).copy(alpha = 0.45f),
                        topLeft = Offset(0f, h * 0.74f),
                        size = Size(w, h * 0.24f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    // Cold arrows piercing feet
                    for (i in 1..3) {
                        val x = w * (0.35f + i * 0.08f)
                        drawLine(
                            color = Color(0xFF2563EB),
                            start = Offset(x, h * 0.80f),
                            end = Offset(x, h * 0.52f),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }
                    // Huddled shivering chicks
                    listOf(0.42f, 0.50f, 0.58f).forEach { cx ->
                        drawCircle(
                            color = Color(0xFF94A3B8),
                            radius = h * 0.11f,
                            center = Offset(w * cx, h * 0.58f)
                        )
                    }
                }
                Text("❌ أرضية باردة (< 22°C)", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626), textAlign = TextAlign.Center)
                Text("برودة أقدام واضطراب هضمي", fontSize = 10.sp, color = primaryText, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun BrooderDistributionQuadDiagram(
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color
) {
    val zones = listOf(
        Triple("❄️ تجمع تحت الدفاية", "حرارة منخفضة / برد", Color(0xFF3B82F6)),
        Triple("☀️ توزيع متوازن 100%", "حرارة مثالية وراحة تامة", Color(0xFF10B981)),
        Triple("🔥 ابتعاد وتنفس سريع", "حرارة مرتفعة جداً", Color(0xFFEF4444)),
        Triple("💨 تجمع في جهة واحدة", "وجود تيار هوائي بارد", Color(0xFFF59E0B))
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(zones.subList(0, 2), zones.subList(2, 4)).forEachIndexed { rowIdx, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEachIndexed { colIdx, (title, sub, ringColor) ->
                    val mode = rowIdx * 2 + colIdx
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color.White,
                        border = BorderStroke(1.dp, ringColor.copy(alpha = 0.55f))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Canvas(modifier = Modifier.size(86.dp)) {
                                val r = size.minDimension * 0.44f
                                val c = Offset(size.width / 2f, size.height / 2f)
                                drawCircle(color = ringColor.copy(alpha = 0.14f), radius = r, center = c)
                                drawCircle(color = ringColor, radius = r, center = c, style = Stroke(width = 3f))
                                // Central heater
                                drawCircle(color = Color(0xFFEA580C), radius = r * 0.20f, center = c)
                                val dotColor = if (mode == 1) Color(0xFF059669) else Color(0xFFD97706)
                                when (mode) {
                                    0 -> { // Huddled around center heater
                                        for (k in 0 until 10) {
                                            val ang = (k * 36.0) * Math.PI / 180.0
                                            drawCircle(dotColor, 4.5f, Offset(c.x + (r * 0.32f * cos(ang)).toFloat(), c.y + (r * 0.32f * sin(ang)).toFloat()))
                                        }
                                    }
                                    1 -> { // Evenly spread
                                        for (k in 0 until 12) {
                                            val ang = (k * 30.0) * Math.PI / 180.0
                                            val dist = if (k % 2 == 0) r * 0.52f else r * 0.78f
                                            drawCircle(dotColor, 4.5f, Offset(c.x + (dist * cos(ang)).toFloat(), c.y + (dist * sin(ang)).toFloat()))
                                        }
                                    }
                                    2 -> { // Pushed against perimeter walls
                                        for (k in 0 until 12) {
                                            val ang = (k * 30.0) * Math.PI / 180.0
                                            drawCircle(dotColor, 4.5f, Offset(c.x + (r * 0.86f * cos(ang)).toFloat(), c.y + (r * 0.86f * sin(ang)).toFloat()))
                                        }
                                    }
                                    else -> { // Draft from left pushing chicks right
                                        drawLine(Color(0xFF2563EB), Offset(0f, c.y), Offset(c.x - r * 0.1f, c.y), strokeWidth = 4f, cap = StrokeCap.Round)
                                        for (k in -4..4) {
                                            val ang = (k * 16.0) * Math.PI / 180.0
                                            drawCircle(dotColor, 4.5f, Offset(c.x + (r * 0.72f * cos(ang)).toFloat(), c.y + (r * 0.72f * sin(ang)).toFloat()))
                                        }
                                    }
                                }
                            }
                            Text(title, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = primaryText, textAlign = TextAlign.Center)
                            Text(sub, fontSize = 9.5.sp, color = secondaryText, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarnCrossSectionAirflowDiagram(
    isDark: Boolean,
    accentColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(138.dp)) {
            val w = size.width
            val h = size.height
            val wallColor = if (isDark) Color(0xFF64748B) else Color(0xFF334155)
            // Barn roof & walls cross-section
            val barnPath = Path().apply {
                moveTo(w * 0.08f, h * 0.86f)
                lineTo(w * 0.08f, h * 0.40f)
                lineTo(w * 0.50f, h * 0.12f)
                lineTo(w * 0.92f, h * 0.40f)
                lineTo(w * 0.92f, h * 0.86f)
                close()
            }
            drawPath(barnPath, color = wallColor, style = Stroke(width = 4f))
            // Litter floor
            drawRect(Color(0xFFD97706).copy(alpha = 0.25f), Offset(w * 0.09f, h * 0.78f), Size(w * 0.82f, h * 0.08f))
            // Birds on floor
            for (i in 0..6) {
                drawCircle(Color(0xFFF59E0B), 6f, Offset(w * (0.18f + i * 0.10f), h * 0.75f))
            }
            // Blue fresh cold air from upper-left inlet rising to ceiling
            drawArrowSegment(Offset(w * 0.03f, h * 0.44f), Offset(w * 0.26f, h * 0.28f), Color(0xFF2563EB))
            drawArrowSegment(Offset(w * 0.26f, h * 0.28f), Offset(w * 0.48f, h * 0.20f), Color(0xFF3B82F6))
            // Orange warm air mixing at ceiling and descending gently
            drawArrowSegment(Offset(w * 0.48f, h * 0.20f), Offset(w * 0.66f, h * 0.45f), Color(0xFFF97316))
            drawArrowSegment(Offset(w * 0.66f, h * 0.45f), Offset(w * 0.52f, h * 0.65f), Color(0xFFEA580C))
            // Exhaust pulling NH3 & moisture out right wall fan
            drawArrowSegment(Offset(w * 0.66f, h * 0.65f), Offset(w * 0.95f, h * 0.52f), Color(0xFF10B981))
            drawCircle(Color(0xFF059669), 11f, Offset(w * 0.92f, h * 0.52f), style = Stroke(width = 3f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), color = Color(0xFFDBEAFE)) {
                Text("🔵 هواء نقي من الفتحات العلوية", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), modifier = Modifier.padding(6.dp), textAlign = TextAlign.Center)
            }
            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), color = Color(0xFFFFEDD5)) {
                Text("🟠 اختلاط دافئ بالسقف ثم نزول لطيف", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9A3412), modifier = Modifier.padding(6.dp), textAlign = TextAlign.Center)
            }
            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), color = Color(0xFFD1FAE5)) {
                Text("🟢 سحب الأمونيا والرطوبة للخارج", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46), modifier = Modifier.padding(6.dp), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun LightingAndWithdrawalDiagram(
    isDark: Boolean,
    accentColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(92.dp)) {
            val d = size.minDimension * 0.84f
            val tl = Offset((size.width - d) / 2f, (size.height - d) / 2f)
            // 18h light arc
            drawArc(Color(0xFFF59E0B), startAngle = -90f, sweepAngle = 270f, useCenter = false, topLeft = tl, size = Size(d, d), style = Stroke(16f, cap = StrokeCap.Round))
            // 6h darkness arc
            drawArc(Color(0xFF1E3A8A), startAngle = 180f, sweepAngle = 90f, useCenter = false, topLeft = tl, size = Size(d, d), style = Stroke(16f, cap = StrokeCap.Round))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("🌙 برنامج الإظلام: 4–6 ساعات يومياً بعد اليوم 7", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
            Text("• يقوي العظام والقلب ويمنع الموت المفاجئ والاستسقاء.", fontSize = 10.sp, color = primaryText)
            Text("⏱️ تصويم العلف: 8–10 ساعات قبل التحميل (مع بقاء الماء ✅).", fontSize = 10.sp, color = secondaryText)
        }
    }
}

@Composable
private fun MedicalAnatomyCanvasSection(
    spec: EncyclopediaPosterSpec,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(132.dp)) {
            val w = size.width
            val h = size.height
            val cx = w * 0.50f
            when (spec.visualType) {
                InfographicVisualType.DIS_IB_BRONCHITIS,
                InfographicVisualType.DIS_CRD_MYCOPLASMA -> {
                    // Avian Respiratory Tract: Sinuses -> Trachea -> Bronchi -> Lungs -> Air Sacs
                    drawCircle(Color(0xFFEF4444).copy(alpha = 0.25f), 16f, Offset(cx, h * 0.16f))
                    drawCircle(Color(0xFFEF4444), 16f, Offset(cx, h * 0.16f), style = Stroke(3f))
                    // Trachea
                    drawLine(Color(0xFFF97316), Offset(cx, h * 0.24f), Offset(cx, h * 0.52f), strokeWidth = 10f, cap = StrokeCap.Round)
                    // Bronchi fork
                    drawLine(Color(0xFFEA580C), Offset(cx, h * 0.52f), Offset(cx - w * 0.12f, h * 0.66f), strokeWidth = 7f, cap = StrokeCap.Round)
                    drawLine(Color(0xFFEA580C), Offset(cx, h * 0.52f), Offset(cx + w * 0.12f, h * 0.66f), strokeWidth = 7f, cap = StrokeCap.Round)
                    // Left & Right Lungs
                    drawOval(Color(0xFFFB7185).copy(alpha = 0.75f), Offset(cx - w * 0.23f, h * 0.58f), Size(w * 0.16f, h * 0.28f))
                    drawOval(Color(0xFFFB7185).copy(alpha = 0.75f), Offset(cx + w * 0.07f, h * 0.58f), Size(w * 0.16f, h * 0.28f))
                    if (spec.visualType == InfographicVisualType.DIS_CRD_MYCOPLASMA) {
                        // Air sacs with caseous yellow markers
                        drawOval(Color(0xFFFBBF24).copy(alpha = 0.45f), Offset(cx - w * 0.18f, h * 0.78f), Size(w * 0.14f, h * 0.18f))
                        drawOval(Color(0xFFFBBF24).copy(alpha = 0.45f), Offset(cx + w * 0.04f, h * 0.78f), Size(w * 0.14f, h * 0.18f))
                    }
                    // Callout pointer lines
                    drawLine(Color(0xFFDC2626), Offset(cx - 18f, h * 0.16f), Offset(w * 0.18f, h * 0.16f), strokeWidth = 2.5f)
                    drawLine(Color(0xFFEA580C), Offset(cx + 8f, h * 0.38f), Offset(w * 0.82f, h * 0.34f), strokeWidth = 2.5f)
                    drawLine(Color(0xFFD97706), Offset(cx - w * 0.08f, h * 0.58f), Offset(w * 0.18f, h * 0.56f), strokeWidth = 2.5f)
                    drawLine(Color(0xFFE11D48), Offset(cx + w * 0.16f, h * 0.72f), Offset(w * 0.82f, h * 0.76f), strokeWidth = 2.5f)
                }
                InfographicVisualType.DIS_COCCIDIOSIS_GUT -> {
                    // Digestive Tract: Crop -> Gizzard -> Small Intestine -> Swollen Hemorrhagic Ceca
                    drawCircle(Color(0xFFF59E0B).copy(alpha = 0.65f), 14f, Offset(cx - w * 0.10f, h * 0.18f))
                    drawCircle(Color(0xFFD97706), 16f, Offset(cx, h * 0.38f))
                    // Intestine loop
                    val gut = Path().apply {
                        moveTo(cx, h * 0.44f)
                        cubicTo(cx + w * 0.18f, h * 0.52f, cx - w * 0.18f, h * 0.66f, cx, h * 0.76f)
                    }
                    drawPath(gut, Color(0xFFEC4899), style = Stroke(width = 8f, cap = StrokeCap.Round))
                    // Swollen Hemorrhagic Ceca (2 pouches)
                    drawOval(Color(0xFF991B1B), Offset(cx - w * 0.18f, h * 0.68f), Size(w * 0.12f, h * 0.24f))
                    drawOval(Color(0xFF991B1B), Offset(cx + w * 0.06f, h * 0.68f), Size(w * 0.12f, h * 0.24f))
                    drawLine(Color(0xFFDC2626), Offset(cx - w * 0.12f, h * 0.78f), Offset(w * 0.15f, h * 0.80f), strokeWidth = 2.5f)
                    drawLine(Color(0xFFBE185D), Offset(cx + w * 0.06f, h * 0.56f), Offset(w * 0.84f, h * 0.54f), strokeWidth = 2.5f)
                }
                else -> {
                    // Emergency ND / IBD / Heat Stress: CNS + Bursa + Thermal Thermometer >30°C
                    drawCircle(Color(0xFF7C3AED).copy(alpha = 0.25f), 20f, Offset(w * 0.24f, h * 0.46f))
                    drawCircle(Color(0xFF7C3AED), 20f, Offset(w * 0.24f, h * 0.46f), style = Stroke(3f))
                    drawOval(Color(0xFFDC2626).copy(alpha = 0.30f), Offset(w * 0.43f, h * 0.34f), Size(w * 0.14f, h * 0.32f))
                    drawOval(Color(0xFFDC2626), Offset(w * 0.43f, h * 0.34f), Size(w * 0.14f, h * 0.32f), style = Stroke(3f))
                    // Thermometer bar >30°C
                    drawRoundRect(Color(0xFFE2E8F0), Offset(w * 0.74f, h * 0.16f), Size(18f, h * 0.68f), CornerRadius(9f, 9f))
                    drawRoundRect(Color(0xFFEF4444), Offset(w * 0.74f, h * 0.32f), Size(18f, h * 0.52f), CornerRadius(9f, 9f))
                }
            }
        }

        // 4 Anatomical Callout Cards
        if (spec.anatomyLabels.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                spec.anatomyLabels.chunked(2).forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        pair.forEach { label ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) Color(0xFF1E293B) else Color.White,
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("📍 ${label.partName}", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626))
                                    Text(label.lesionNote, fontSize = 9.5.sp, color = secondaryText, lineHeight = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VaccinationRouteAndImmunityDiagram(
    type: InfographicVisualType,
    isDark: Boolean,
    accentColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(94.dp)) {
            val w = size.width
            val h = size.height
            // Immunity curve rising across Day 1 -> 7 -> 14 -> 21 -> 35
            val pts = listOf(
                Offset(w * 0.08f, h * 0.76f),
                Offset(w * 0.30f, h * 0.56f),
                Offset(w * 0.55f, h * 0.36f),
                Offset(w * 0.88f, h * 0.16f)
            )
            for (i in 0 until pts.size - 1) {
                drawLine(accentColor, pts[i], pts[i + 1], strokeWidth = 5f, cap = StrokeCap.Round)
            }
            pts.forEach { p ->
                drawCircle(Color.White, 7f, p)
                drawCircle(accentColor, 7f, p, style = Stroke(3f))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val routeLabel = when (type) {
                InfographicVisualType.VAX_RECEPTION_WATER -> "💧 ماء فاتر 18–21°C + AD3E"
                InfographicVisualType.VAX_NEWCASTLE_EYE_SPRAY -> "👁️ تقطير عيني / 💨 رش خشن"
                InfographicVisualType.VAX_GUMBORO_DRINKING -> "💧 تعطيش ساعتين + ماء بلا كلور"
                else -> "🌅 شرب صباحي + مضاد سموم"
            }
            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), color = accentColor.copy(alpha = 0.12f)) {
                Text(routeLabel, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = accentColor, modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center)
            }
            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), color = if (isDark) Color(0xFF1E293B) else Color.White) {
                Text("📈 ذروة مناعة القطيع حتى البيع", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = primaryText, modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun FeedingAndWeightCurveDiagram(
    type: InfographicVisualType,
    isDark: Boolean,
    accentColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(106.dp)) {
            val w = size.width
            val h = size.height
            if (type == InfographicVisualType.FEED_GROWER_HEIGHT) {
                // Feeder pan alignment at bird back level
                drawLine(Color(0xFF64748B), Offset(w * 0.30f, 0f), Offset(w * 0.30f, h * 0.45f), strokeWidth = 3f)
                drawRoundRect(Color(0xFFDC2626), Offset(w * 0.20f, h * 0.45f), Size(w * 0.20f, h * 0.16f), CornerRadius(6f, 6f))
                // Bird back-level reference dashed/solid line
                drawLine(accentColor, Offset(w * 0.14f, h * 0.48f), Offset(w * 0.86f, h * 0.48f), strokeWidth = 2.5f)
                // Bird body silhouette
                drawOval(Color(0xFFF59E0B), Offset(w * 0.55f, h * 0.48f), Size(w * 0.22f, h * 0.26f))
                drawCircle(Color(0xFFF59E0B), 10f, Offset(w * 0.54f, h * 0.42f))
                drawLine(Color(0xFFD97706), Offset(w * 0.64f, h * 0.74f), Offset(w * 0.64f, h * 0.90f), strokeWidth = 3f)
                drawLine(Color(0xFFD97706), Offset(w * 0.70f, h * 0.74f), Offset(w * 0.70f, h * 0.90f), strokeWidth = 3f)
            } else {
                // Ross 308 Weight Growth Bar Chart (D7, D14, D21, D28, D35, D42)
                val barHeights = listOf(0.14f, 0.26f, 0.42f, 0.60f, 0.78f, 0.94f)
                val barW = w / (barHeights.size * 1.8f)
                barHeights.forEachIndexed { idx, frac ->
                    val x = w * 0.08f + idx * (barW * 1.75f)
                    val barH = h * 0.80f * frac
                    drawRoundRect(
                        color = accentColor.copy(alpha = 0.55f + idx * 0.07f),
                        topLeft = Offset(x, h * 0.88f - barH),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
            }
        }
        Text(
            text = if (type == InfographicVisualType.FEED_GROWER_HEIGHT) {
                "📏 حافة المعلف = مستوى ظهر الطائر (يقلل هدر العلف > 3%)"
            } else {
                "📈 منحنى الوزن القياسي Ross 308: يوم 7 (190غ) -> يوم 21 (1000غ) -> يوم 35 (2250غ) -> يوم 42 (2950غ)"
            },
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = primaryText
        )
    }
}

private fun DrawScope.drawArrowSegment(start: Offset, end: Offset, color: Color) {
    drawLine(color = color, start = start, end = end, strokeWidth = 4f, cap = StrokeCap.Round)
    drawCircle(color = color, radius = 4.5f, center = end)
}
