# Keep Firestore models used with toObject / toObjects
-keep class com.stepstack.app.data.Challenge { *; }
-keep class com.stepstack.app.data.ChallengeParticipant { *; }
-keep class com.stepstack.app.data.FirestoreUser { *; }
-keep class com.stepstack.app.data.Friendship { *; }
-keep class com.stepstack.app.data.ChallengeStatus { *; }
-keep class com.stepstack.app.data.ChallengeType { *; }
-keep class com.stepstack.app.data.ChallengeVisibility { *; }
-keep class com.stepstack.app.data.ParticipantStatus { *; }
-keep class com.stepstack.app.data.FriendshipStatus { *; }

# Keep Room entities
-keep @androidx.room.Entity class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}

# Keep kotlinx.serialization models
-keepattributes *Annotation*, ElementValueAttribute
-keepclassmembers class * {
    @kotlinx.serialization.Serializer *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}
