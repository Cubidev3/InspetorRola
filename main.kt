package penisdetbot

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.flow.*

suspend fun main () {
    val wordsToCheck: List<String> = listOf("buceta", "rosca", "rola", "penis", "pinto", "hentai", "yamete")
    val client = Kord("<Your Token>")

    client.events
        .filterIsInstance<MessageCreateEvent>()
        .map { it.message }
        .filter { it.author?.isBot == false }
        .onEach {
            for (wordToCheck in wordsToCheck) {
                val hidden = getHiddenWordsInPhraseOrNothing(wordToCheck, it.content)
                if (hidden.isNotEmpty()) {
                    if (it.author != null) {
                        it.channel.createMessage(hiddenMessageDiscoveredMessage(wordToCheck, hidden, it.author!!.id.value))
                    }
                }
            }
        }
        .launchIn(client)

    client.login {
        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent
    }
}

fun getHiddenWordsInPhraseOrNothing(hiddenWord: String, phrase: String): List<List<String>> {
    val words = phrase.split(' ')
    val hiddenWordsIndexes: MutableList<Int> = mutableListOf()
    val hiddenWordsList: MutableList<List<String>> = mutableListOf()

    for (word in words.indices) {
        if (wordStartWith(words[word], hiddenWord[hiddenWordsIndexes.size])) hiddenWordsIndexes.add(word)
        if (hiddenWordsIndexes.size == hiddenWord.length) {
            var hiddenWords = mutableListOf<String>()
            for (i in hiddenWordsIndexes.indices) {
                var wordToAdd = "${words[hiddenWordsIndexes[i]]}"
                if ((i == 0 && hiddenWordsIndexes[i] != 0) || (i != 0 && (hiddenWordsIndexes[i] - hiddenWordsIndexes[i - 1]) > 1)) {
                    wordToAdd = "..$wordToAdd"
                }

                if ((i == hiddenWordsIndexes.lastIndex && hiddenWordsIndexes[i] != words.lastIndex) || (i != hiddenWordsIndexes.lastIndex && (hiddenWordsIndexes[i + 1] - hiddenWordsIndexes[i]) > 1)) {
                    wordToAdd = "$wordToAdd.."
                }
                hiddenWords.add(wordToAdd)
            }

            hiddenWordsList.add(hiddenWords)
            hiddenWordsIndexes.clear()
        }
    }

    return hiddenWordsList
}

fun wordStartWith(word: String, specialLetter: Char): Boolean {
    val firstLetter = word.firstOrNull() ?: return false
    if (firstLetter.lowercaseChar() != specialLetter) return false
    return true
}

fun hiddenMessageDiscoveredMessage(hiddenWord: String, words: List<List<String>>, messageOwner: ULong): String {
    var message = "<@$messageOwner> \n"
    for (wordlist in words) {
        message += "// "
        for (word in wordlist) {
            message += ("${word.slice(0 until word.indexOfFirst { it != '.' } )}**${word.first{it != '.'}.uppercase()}**${word.slice((word.indexOfFirst { it != '.' } + 1) until word.length)} ")
        }
        message += "\n"
    }
    message += "\n"
    val sex = if (hiddenWord.last() == 'a') 'a' else 'o'
    message += "**${hiddenWord.uppercase()}** Secret$sex Detectad$sex! \n"

    return message
}