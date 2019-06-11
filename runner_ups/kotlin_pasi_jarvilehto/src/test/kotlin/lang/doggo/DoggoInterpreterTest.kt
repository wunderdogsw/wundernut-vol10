package lang.doggo

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

internal class DoggoInterpreterTest : StringSpec({
    "official spec 1 evaluates to 11" {
        """
            lassie AWOO 5
            luna AWOO 6
            bailey AWOO lassie WOOF luna
            bailey
        """.evaluate().shouldBe(11)
    }

    "official spec 2 evaluates to 15" {
        """
            roi AWOO 5
            RUF? roi YAP 2 VUH
                roi AWOO roi ARF 3
            ROWH
                roi AWOO roi WOOF 100
            ARRUF
            roi
        """.evaluate().shouldBe(15)
    }

    "official spec 3 evaluates to 105" {
        """
            roi AWOO 5
            RUF? roi YIP 2 VUH
              roi AWOO roi ARF 3
            ROWH
              roi AWOO roi WOOF 100
            ARRUF
            roi
        """.evaluate().shouldBe(105)
    }

    "official spec 4 evaluates to 19" {
        """
            quark AWOO 6 BARK 2
            gromit AWOO 5
            milo AWOO 0
            GRRR milo YIP gromit BOW
                quark AWOO quark WOOF 3
                milo AWOO milo WOOF 1
            BORF
            quark

        """.evaluate().shouldBe(19)
    }

    "very important code evaluates to 64185" {
        """
            samantha AWOO 1
            hooch AWOO 500
            einstein AWOO 10
            fuji AWOO 0
            GRRR fuji YIP hooch BOW
                samantha AWOO samantha WOOF 3
                RUF? samantha YAP 100 VUH
                  samantha AWOO samantha BARK 1
                ROWH
                  einstein AWOO einstein WOOF 1
                  samantha AWOO samantha ARF einstein
                ARRUF
                fuji AWOO fuji WOOF 1
            BORF
            GRRR fuji YAP 0 BOW
                samantha AWOO samantha WOOF 375
                fuji AWOO fuji BARK 3
            BORF
            samantha
        """.evaluate().shouldBe(64185)
    }

    "program can consist of a single expression" {
        "3 WOOF 2".evaluate().shouldBe(5)
    }

    "else branch is optional" {
        """
            roi AWOO 5
            RUF? roi YAP 2 VUH
                roi AWOO roi ARF 3
            ARRUF
            roi
        """.evaluate().shouldBe(15)
    }
})
