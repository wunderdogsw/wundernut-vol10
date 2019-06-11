package lang.doggo

import lang.doggo.generated.DoggoLexer
import lang.doggo.generated.DoggoParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

fun String.evaluate(): Long = byteInputStream()
    .let(CharStreams::fromStream)
    .let(::DoggoLexer)
    .let(::CommonTokenStream)
    .let(::DoggoParser)
    .let { parser ->
        DoggoVisitor().visitProgram(parser.program())
    }
