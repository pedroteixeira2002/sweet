package com.cmu.sweet.helpers

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.Locale

class LanguageContextWrapper(base: Context) : ContextWrapper(base) {

    companion object {
        fun wrap(context: Context, language: String): ContextWrapper {
            val locale = when (language) {
                "Portuguese" -> Locale.Builder().setLanguage("pt").setRegion("PT").build()
                "Spanish" -> Locale.Builder().setLanguage("es").setRegion("ES").build()
                else -> Locale.Builder().setLanguage("en").setRegion("US").build()
            }

            Locale.setDefault(locale)

            val config = context.resources.configuration

            config.setLocale(locale)
            config.setLocales(LocaleList(locale))
            val newContext = context.createConfigurationContext(config)
            return LanguageContextWrapper(newContext)
        }
    }
}
