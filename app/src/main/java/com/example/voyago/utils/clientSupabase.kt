package com.example.voyago.utils

import android.os.Handler
import android.os.Looper
import com.example.voyago.MainActivity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseManager {
  private const val SUPABASE_URL = "https://gltggfvigxusvutxasym.supabase.co"
  private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdsdGdnZnZpZ3h1c3Z1dHhhc3ltIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc5ODk3NjIsImV4cCI6MjA2MzU2NTc2Mn0.gmCuZv6jJ1GEvkBqIUmOt1hZrb8PwzlMINxNzneG1Pg"

  // Inizializzazione lazy thread-safe
  val client: SupabaseClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    createSupabaseClient(
      supabaseUrl = SUPABASE_URL,
      supabaseKey = SUPABASE_KEY
    ) {
      install(GoTrue) {
        // Configurazione specifica per GoTrue
        alwaysAutoRefresh = true
      }
      install(Storage)
      install(Postgrest)
    }
  }

  // Funzione per inizializzare esplicitamente sul main thread
  fun initialize(application: MainActivity) {
    // Forza l'inizializzazione sul main thread
    if (Looper.myLooper() != Looper.getMainLooper()) {
      Handler(Looper.getMainLooper()).post {
        client // Questo forza l'inizializzazione
      }
    } else {
      client // Questo forza l'inizializzazione
    }
  }
}