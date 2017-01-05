import io.kotlintest.specs.StringSpec
import okio.Buffer
import rx.Emitter
import rx.Observable


/***

--- Day 5: How About a Nice Game of Chess? ---

You are faced with a security door designed by Easter Bunny engineers that seem to have acquired most of their security knowledge by watching hacking movies.

The eight-character password for the door is generated one character at a time by finding the MD5 hash of some Door ID (your puzzle input) and an increasing integer index (starting with 0).

A hash indicates the next character in the password if its hexadecimal representation starts with five zeroes. If it does, the sixth character in the hash is the next character of the password.

For example, if the Door ID is abc:

The first index which produces a hash that starts with five zeroes is 3231929, which we find by hashing abc3231929; the sixth character of the hash, and thus the first character of the password, is 1.
5017308 produces the next interesting hash, which starts with 000008f82..., so the second character of the password is 8.
The third time a hash starts with five zeroes is for abc5278568, discovering the character f.
In this example, after continuing this search a total of eight times, the password is 18f47a30.

Given the actual Door ID, what is the password?

Your puzzle answer was 1a3099aa.

The first half of this puzzle is complete! It provides one gold star: *

--- Part Two ---

As the door slides open, you are presented with a second door that uses a slightly more inspired security mechanism. Clearly unimpressed by the last version (in what movie is the password decrypted in order?!), the Easter Bunny engineers have worked out a better solution.

Instead of simply filling in the password from left to right, the hash now also indicates the position within the password to fill. You still look for hashes that begin with five zeroes; however, now, the sixth character represents the position (0-7), and the seventh character is the character to put in that position.

A hash result of 000001f means that f is the second character in the password. Use only the first result for each position, and ignore invalid positions.

For example, if the Door ID is abc:

The first interesting hash is from abc3231929, which produces 0000015...; so, 5 goes in position 1: _5______.
In the previous method, 5017308 produced an interesting hash; however, it is ignored, because it specifies an invalid position (8).
The second interesting hash is at index 5357525, which produces 000004e...; so, e goes in position 4: _5__e___.
You almost choke on your popcorn as the final character falls into place, producing the password 05ace8e3.

Given the actual Door ID and this new method, what is the password? Be extra proud of your solution if it uses a cinematic "decrypting" animation.

Your puzzle input is still uqwqemis.

 */
data class HashResult(val key: String, val number: Int, val md5: String, val char6: Char, val char7: Char)

class Day5 : StringSpec() { init {

    val input = "uqwqemis"

    val solution2 = "694190cd"

    "Part 2 Solution : $solution2" {
        val password = "xxxxxxxx".toMutableList()

        fun passwordNotComplete(): Boolean =
            password.any { it == 'x' }

        validHashes(input)
            .filter { it.char6 in '0'..'7' }
            .takeWhile { passwordNotComplete() }
            .subscribe({ result ->
                val index = result.char6.toInt() - '0'.toInt()
                if (password[index] == 'x') {
                    password[index] = result.char7
                }
            }, { e ->
                e shouldBe null
            }, { ->
                password.asString() shouldBe solution2
            })
    }

    "Interesting hashes start with 00000[letter]" {
        val room = "abc"
        val tests = table(
            headers("number", "letter"),
            row(3231929, '1'),
            row(5017308, '8'),
            row(5278568, 'f')
        )
        forAll(tests) { number, letter ->
            val md5 = "$room$number".md5().debug("md5($number)")
            md5 should startWith("00000")
            md5[5] shouldBe letter
        }
    }

    val examplePassword = "18f47a30"
    "Example Password : $examplePassword" {
        val list = validHashes("abc").take(8).toBlocking().toIterable().toList()
        list.take(3).map { it.number } shouldBe listOf(3231929, 5017308, 5278568)
        val password = list.map { it.char6 }.asString()
        password shouldBe examplePassword
    }

    val solution = "1a3099aa"
    "Part 1 Solution : $solution" {
        val list = validHashes(input).take(8).toBlocking().toIterable().toList()
        val password = list.map { it.char6 }.asString()
        password shouldBe solution
    }


}
}


private fun validHashes(key: String): Observable<HashResult>
    = Observable.fromEmitter(
    { emitter: Emitter<HashResult> ->
        var i = 0
        var found = 0
        var cancelled = false
        emitter.setCancellation { cancelled = true }
        var md5: String
        while (i < Integer.MAX_VALUE && !cancelled) {
            md5 = "$key$i".md5()
            if (md5.startsWith("00000")) {
                val next = HashResult(key, i, md5, md5[5], md5[6])
                println("Found $next")
                emitter.onNext(next)
                found++
            }
            i++
        }
        emitter.onCompleted()

    }, Emitter.BackpressureMode.DROP)

fun String.md5(): String {
    return Buffer().write(toByteArray()).md5().hex()
}
