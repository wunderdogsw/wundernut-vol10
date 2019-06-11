This is my entry for the Wundernut-doggolang challenge
https://github.com/wunderdogsw/wunderpahkina-vol10

I've only tested it in Linux, but it's implemented in vanilla C++ 17, so
at least in theory it should work on pretty much any system with a modern
enough C++ compiler.

I've tried to maintain some balance between minimalism and readability,
the whole source is around 200 lines if not counting comments.

To build and run, simply:
    make
    ./doggorun programs/5.txt

The source file doggorun.cc has some info on how it it's implemented.

As a small bonus, I've also included a 256-byte perl-script that I
hacked together before noticing the rules stated that find-and-
replace solutions would be frowned upon...

    bonus/doggoeval.pl programs/5.txt

/ Simon Lindholm - 2019-05-31, simon@datakod.se

