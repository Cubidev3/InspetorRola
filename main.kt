import dev.kord.core.Kord
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent

const val token = "<Insert your token here>"

val wordsToSearchFor = listOf<String>("penis", "rosca", "furico", "sexo", "buceta", "giromba", "gabryeltwink", "jocagay", "twink", "daddy", "yametekudasai", "suruba", "pinto", "xvideos")

suspend fun main() {
    val bot = Kord(token)

    bot.on<MessageCreateEvent>
    {
        if (message.author?.isBot!!) return@on
        exposeHiddenWords(message)
    }

    bot.login {
        // we need to specify this to receive the content of messages
        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent
    }
}

suspend fun exposeHiddenWords(message: Message)
{
    wordsToSearchFor.forEach { word ->
        val hiddenWordIndices = hiddenWordIndices(message.content, word)
        if (hiddenWordIndices.count() != word.count()) return@forEach

        message.reply { content = hiddenWordWasFoundMessage(message.content, hiddenWordIndices)}
    }
}

fun hiddenWordIndices(message: String, hiddenWord: String) : List<Int>
{
    val indicesFound = mutableListOf<Int>()

    message.split(" ").map { it.first() }.forEachIndexed { index, initial ->
        val matchesHiddenChar = initial.lowercaseChar() == hiddenWord.getOrNull(indicesFound.count())?.lowercaseChar()
        if (matchesHiddenChar) indicesFound.add(index)
    }

    return indicesFound
}

fun hiddenWordWasFoundMessage(message: String, wordIndices: List<Int>) : String
{
    val words = message.split(" ")

    val messageHead = wordIndices.zipWithNext { index, nextIndex ->
        when (nextIndex) {
            index + 1 -> "${words[index].replaceFirstChar { "**${it.uppercaseChar()}**" }} "
            else -> "${words[index].replaceFirstChar { "**${it.uppercaseChar()}**" }}.. .."
        }
    }.joinToString("")

    // As a side effect of using zipWithNext, the last element is removed (because there is no next element to it)
    val last = words[wordIndices.last()].replaceFirstChar { "**${it.uppercaseChar()}**" }

    val wordFoundMessage = wordIndices.joinToString("") { index -> words[index].first().uppercase() }

    return "// $messageHead $last + \n" +
            "Um **$wordFoundMessage** Secreto foi Encontrado!"
}