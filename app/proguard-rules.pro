# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# الحفاظ على أرقام الأسطر البرمجية لمعرفة مكان أي خطأ بدقة في تقارير الأعطال
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# حماية جميع نماذج البيانات (FarmModel, CycleModel, DailyReportModel, TransactionModel) من التغيير
-keep class com.example.models.** { *; }

# حماية خصائص القراءة والكتابة الخاصة بقاعدة بيانات Firebase Firestore
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# الحفاظ على الأنواع العامة (Generics) والتعليقات التوضيحية اللازمة لتحويل البيانات (JSON)
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
