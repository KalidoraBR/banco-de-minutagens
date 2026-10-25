# O agendador do Android (WorkManager) cria o banco interno dele por reflexão:
# o otimizador não pode remover o construtor das classes geradas do Room.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class androidx.work.impl.** { *; }
