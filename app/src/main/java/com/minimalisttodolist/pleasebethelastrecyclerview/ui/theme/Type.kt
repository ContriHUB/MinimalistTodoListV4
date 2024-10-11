package com.minimalisttodolist.pleasebethelastrecyclerview.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.minimalisttodolist.pleasebethelastrecyclerview.R

object CustomFonts {
    private val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )

    private val DefaultFont = GoogleFont("Open Sans")
    private val SerifFont = GoogleFont("Playfair Display")
    private val SansSerifFont = GoogleFont("Montserrat")
    private val MonospaceFont = GoogleFont("Roboto Mono")
    private val CursiveFont = GoogleFont("Dancing Script")

    val Default = FontFamily(
        Font(googleFont = DefaultFont, fontProvider = provider, weight = FontWeight.Light),
        Font(googleFont = DefaultFont, fontProvider = provider, weight = FontWeight.Normal),
        Font(googleFont = DefaultFont, fontProvider = provider, weight = FontWeight.Bold),
        Font(googleFont = DefaultFont, fontProvider = provider, weight = FontWeight.Light, FontStyle.Italic),
        Font(googleFont = DefaultFont, fontProvider = provider, weight = FontWeight.Normal, FontStyle.Italic),
        Font(googleFont = DefaultFont, fontProvider = provider, weight = FontWeight.Bold, FontStyle.Italic),
    )

    val Serif = FontFamily(
        Font(googleFont = SerifFont, fontProvider = provider, weight = FontWeight.Light),
        Font(googleFont = SerifFont, fontProvider = provider, weight = FontWeight.Normal),
        Font(googleFont = SerifFont, fontProvider = provider, weight = FontWeight.Bold),
        Font(googleFont = SerifFont, fontProvider = provider, weight = FontWeight.Light, FontStyle.Italic),
        Font(googleFont = SerifFont, fontProvider = provider, weight = FontWeight.Normal, FontStyle.Italic),
        Font(googleFont = SerifFont, fontProvider = provider, weight = FontWeight.Bold, FontStyle.Italic),
    )

    val SansSerif = FontFamily(
        Font(googleFont = SansSerifFont, fontProvider = provider, weight = FontWeight.Light),
        Font(googleFont = SansSerifFont, fontProvider = provider, weight = FontWeight.Normal),
        Font(googleFont = SansSerifFont, fontProvider = provider, weight = FontWeight.Bold),
        Font(googleFont = SansSerifFont, fontProvider = provider, weight = FontWeight.Light, FontStyle.Italic),
        Font(googleFont = SansSerifFont, fontProvider = provider, weight = FontWeight.Normal, FontStyle.Italic),
        Font(googleFont = SansSerifFont, fontProvider = provider, weight = FontWeight.Bold, FontStyle.Italic),
    )

    val Monospace = FontFamily(
        Font(googleFont = MonospaceFont, fontProvider = provider, weight = FontWeight.Light),
        Font(googleFont = MonospaceFont, fontProvider = provider, weight = FontWeight.Normal),
        Font(googleFont = MonospaceFont, fontProvider = provider, weight = FontWeight.Bold),
        Font(googleFont = MonospaceFont, fontProvider = provider, weight = FontWeight.Light, FontStyle.Italic),
        Font(googleFont = MonospaceFont, fontProvider = provider, weight = FontWeight.Normal, FontStyle.Italic),
        Font(googleFont = MonospaceFont, fontProvider = provider, weight = FontWeight.Bold, FontStyle.Italic),
    )

    val Cursive = FontFamily(
        Font(googleFont = CursiveFont, fontProvider = provider, weight = FontWeight.Light),
        Font(googleFont = CursiveFont, fontProvider = provider, weight = FontWeight.Normal),
        Font(googleFont = CursiveFont, fontProvider = provider, weight = FontWeight.Bold),
        Font(googleFont = CursiveFont, fontProvider = provider, weight = FontWeight.Light, FontStyle.Italic),
        Font(googleFont = CursiveFont, fontProvider = provider, weight = FontWeight.Normal, FontStyle.Italic),
        Font(googleFont = CursiveFont, fontProvider = provider, weight = FontWeight.Bold, FontStyle.Italic),
    )
}
