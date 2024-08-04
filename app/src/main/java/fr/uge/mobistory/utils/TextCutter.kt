package fr.uge.mobistory.utils

/**
 * Replaces \n string into return lines code (\n).
 *
 * @param bigText String.
 * @return String, formatted String with correct return line.
 */
fun returnLineFormat(bigText: String): String {
    return bigText.replace("\\n", "\n").replace("\\t", "\t")
}

/**
 * Cuts the long text into smaller text with their header.
 *
 * @param bigText String.
 * @return Map of String and String, the header and the content.
 */
fun textCutter(bigText: String): LinkedHashMap<String, String> {
    println(bigText)
    var firstLine = true
    val hashmap = LinkedHashMap<String, String>()
    val regex = Regex("=+ .* =+")
    val matchResults = regex.findAll(bigText)
    matchResults.forEachIndexed { index, matching ->
        if (firstLine) {
            hashmap[""] = bigText.substring(0, matching.range.first)
            firstLine = false
        } else {
            val min = matching.range.last + 1
            val max = if (index + 1 < matchResults.count()) {
                val nextValue = matchResults.elementAt(index + 1)
                nextValue.range.first
            } else {
                bigText.length //- 1
            }
            hashmap[matching.value] = bigText.substring(min, max)
        }
    }
    return hashmap
}