package com.quiniela.app.data.models

data class WCTeam(val name: String, val flag: String, val confederation: String)

object WorldCupTeams {

    val all: List<WCTeam> = listOf(
        // CONMEBOL
        WCTeam("Argentina",   "🇦🇷", "CONMEBOL"),
        WCTeam("Bolivia",     "🇧🇴", "CONMEBOL"),
        WCTeam("Brasil",      "🇧🇷", "CONMEBOL"),
        WCTeam("Chile",       "🇨🇱", "CONMEBOL"),
        WCTeam("Colombia",    "🇨🇴", "CONMEBOL"),
        WCTeam("Ecuador",     "🇪🇨", "CONMEBOL"),
        WCTeam("Paraguay",    "🇵🇾", "CONMEBOL"),
        WCTeam("Uruguay",     "🇺🇾", "CONMEBOL"),
        WCTeam("Venezuela",   "🇻🇪", "CONMEBOL"),
        // CONCACAF
        WCTeam("Canadá",      "🇨🇦", "CONCACAF"),
        WCTeam("Costa Rica",  "🇨🇷", "CONCACAF"),
        WCTeam("Cuba",        "🇨🇺", "CONCACAF"),
        WCTeam("Estados Unidos", "🇺🇸", "CONCACAF"),
        WCTeam("Guatemala",   "🇬🇹", "CONCACAF"),
        WCTeam("Honduras",    "🇭🇳", "CONCACAF"),
        WCTeam("Jamaica",     "🇯🇲", "CONCACAF"),
        WCTeam("México",      "🇲🇽", "CONCACAF"),
        WCTeam("Panamá",      "🇵🇦", "CONCACAF"),
        WCTeam("Trinidad y Tobago", "🇹🇹", "CONCACAF"),
        // UEFA
        WCTeam("Albania",     "🇦🇱", "UEFA"),
        WCTeam("Alemania",    "🇩🇪", "UEFA"),
        WCTeam("Austria",     "🇦🇹", "UEFA"),
        WCTeam("Bélgica",     "🇧🇪", "UEFA"),
        WCTeam("Croacia",     "🇭🇷", "UEFA"),
        WCTeam("Dinamarca",   "🇩🇰", "UEFA"),
        WCTeam("Escocia",     "🏴󠁧󠁢󠁳󠁣󠁴󠁿", "UEFA"),
        WCTeam("Eslovaquia",  "🇸🇰", "UEFA"),
        WCTeam("Eslovenia",   "🇸🇮", "UEFA"),
        WCTeam("España",      "🇪🇸", "UEFA"),
        WCTeam("Francia",     "🇫🇷", "UEFA"),
        WCTeam("Georgia",     "🇬🇪", "UEFA"),
        WCTeam("Hungría",     "🇭🇺", "UEFA"),
        WCTeam("Inglaterra",  "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "UEFA"),
        WCTeam("Italia",      "🇮🇹", "UEFA"),
        WCTeam("Noruega",     "🇳🇴", "UEFA"),
        WCTeam("Países Bajos","🇳🇱", "UEFA"),
        WCTeam("Polonia",     "🇵🇱", "UEFA"),
        WCTeam("Portugal",    "🇵🇹", "UEFA"),
        WCTeam("República Checa","🇨🇿", "UEFA"),
        WCTeam("Rumania",     "🇷🇴", "UEFA"),
        WCTeam("Serbia",      "🇷🇸", "UEFA"),
        WCTeam("Suecia",      "🇸🇪", "UEFA"),
        WCTeam("Suiza",       "🇨🇭", "UEFA"),
        WCTeam("Turquía",     "🇹🇷", "UEFA"),
        WCTeam("Ucrania",     "🇺🇦", "UEFA"),
        // CAF
        WCTeam("Algeria",     "🇩🇿", "CAF"),
        WCTeam("Camerún",     "🇨🇲", "CAF"),
        WCTeam("Costa de Marfil","🇨🇮", "CAF"),
        WCTeam("Egipto",      "🇪🇬", "CAF"),
        WCTeam("Ghana",       "🇬🇭", "CAF"),
        WCTeam("Guinea",      "🇬🇳", "CAF"),
        WCTeam("Mali",        "🇲🇱", "CAF"),
        WCTeam("Marruecos",   "🇲🇦", "CAF"),
        WCTeam("Nigeria",     "🇳🇬", "CAF"),
        WCTeam("Senegal",     "🇸🇳", "CAF"),
        WCTeam("Sudáfrica",   "🇿🇦", "CAF"),
        WCTeam("Tanzania",    "🇹🇿", "CAF"),
        WCTeam("Túnez",       "🇹🇳", "CAF"),
        // AFC
        WCTeam("Arabia Saudita","🇸🇦", "AFC"),
        WCTeam("Australia",   "🇦🇺", "AFC"),
        WCTeam("China",       "🇨🇳", "AFC"),
        WCTeam("Corea del Sur","🇰🇷", "AFC"),
        WCTeam("Emiratos Árabes","🇦🇪", "AFC"),
        WCTeam("Indonesia",   "🇮🇩", "AFC"),
        WCTeam("Irak",        "🇮🇶", "AFC"),
        WCTeam("Irán",        "🇮🇷", "AFC"),
        WCTeam("Japón",       "🇯🇵", "AFC"),
        WCTeam("Jordania",    "🇯🇴", "AFC"),
        WCTeam("Omán",        "🇴🇲", "AFC"),
        WCTeam("Qatar",       "🇶🇦", "AFC"),
        WCTeam("Uzbekistán",  "🇺🇿", "AFC"),
        // OFC
        WCTeam("Nueva Zelanda","🇳🇿", "OFC"),
        // Personalizado (si el equipo no está en la lista)
        WCTeam("Otro",        "🏳", ""),
    ).sortedWith(compareBy({ it.confederation == "Otro" }, { it.name }))

    fun flagFor(teamName: String): String =
        all.firstOrNull { it.name.equals(teamName, ignoreCase = true) }?.flag ?: "🏳"
}
