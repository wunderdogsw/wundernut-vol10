
/* doggorun.cc
 *
 * Entry for "Wundernut" - https://github.com/wunderdogsw/wunderpahkina-vol10
 * Written by Simon Lindholm - simon@datakod.se, 2019-05-31
 *
 * If we translate the keywords and operators to English, the language is essentially just a
 * sequence of statements in the form:
 *     var = <expr>
 *     if <expr> then <stmts> endif
 *     if <expr> then <stmts> else <stmts> endif
 *     while <expr> do <stmts> done
 *
 * An expression <expr> is one or more integers or variable names combined with the binary
 * operators +, -, *, < and >. It would be possible to of course to make a distinction
 * between expressions evaluating to integers (a + b) and expressions evaluating to bools
 * (a < b), but in the interest of keeping things as simple as possible, we're sticking to
 * just integers. A single expression where a statement otherwise would have been expected is
 * interpreted as a print-statement.
 *
 * The program is implemented in C++17, and it simply compiles the doggo-code into a bytecode
 * for a very simple virtual machine and then runs it. The vm has only three registers: a
 * program counter (pc), and two general purpose registers r0 and r1. There are 8
 * instructions it understands:
 *
 *     SET <value>       r0 = value
 *     SWAP              tmp = r0; r0 = r1; r1 = tmp
 *     SUB               r0 -= r1
 *     MUL               r0 *= r1
 *     LOAD <addr>       r0 = memory[addr]
 *     SAVE <addr>       memory[addr] = r0
 *     JMPP <addr>       if (r0 > 0) pc = addr
 *     PRINT             Write value of r0 to stdout
 *
 * There is no corresponding ADD to SUB, addition is instead implemented by first multiplying
 * with -1 and then subtracting. Likewise an unconditional jump is implemented with SET 1;
 * JMPP, and so on.
 *
 * The doggo-language is simple enough that we don't need an intermediary AST - we can just
 * generate the bytecode on the fly as we parse it.
 *
 * I've only tested this program in Linux, but there's nothing platform specific in here, so
 * it should at least in theory work on pretty much any other platform.
 * To build, simply type "make", or alternatively g++ -Wall -std=c++17 -o doggorun doggorun.cc
 *
 */

#include <iostream>
#include <fstream>
#include <memory>
#include <map>
#include <vector>
#include <deque>
#include <iterator>
#include <stdexcept>
#include <string_view>
#include <system_error>
#include <cstdint>
#include <cerrno>
#include <cctype>
using namespace std;

using word_t = int32_t;

enum Opcode : word_t { SET, SWAP, SUB, MUL, LOAD, SAVE, JMPP, PRINT };

static constexpr string_view
    If          = "RUF?",
    Then        = "VUH",
    Else        = "ROWH",
    EndIf       = "ARRUF",
    While       = "GRRR",
    Do          = "BOW",
    EndWhile    = "BORF",
    Assign      = "AWOO",
    Plus        = "WOOF",
    Minus       = "BARK",
    Multiply    = "ARF",
    LessThan    = "YIP",
    GreaterThan = "YAP" ;


class Program final {
    public:
        auto&  operator<<(int32_t value)   { code_.push_back(value); return *this; }
        auto&  operator>>(int32_t& addr)   { addr = code_.size(); return *this; }
        auto&  operator[](size_t i)        { return code_[i]; }
        word_t operator[](string_view var) { return vars_.try_emplace(string{var}, 1+vars_.size()).first->second; }

        void run() const {
            auto mem = make_unique<word_t[]>(1 + vars_.size());
            word_t r0 = 0, r1 = 0, pc = 0;
            while (pc < word_t(code_.size())) {
                switch (code_[pc++]) {
                    case SET:   { r0 = code_[pc++]; break; }
                    case SWAP:  { swap(r0, r1); break; }
                    case SUB:   { r0 -= r1; break; }
                    case MUL:   { r0 *= r1; break; }
                    case LOAD:  { r0 = mem[code_[pc++]]; break; }
                    case SAVE:  { mem[code_[pc++]] = r0; break; }
                    case JMPP:  { auto addr = code_[pc++]; if (r0 > 0) { pc = addr; } break; }
                    case PRINT: { cout << r0 << "\n"; break; }
                    default:    { throw runtime_error { "Invalid instruction" }; }
                }
            }
        }

    private:
        vector<word_t>      code_ { };
        map<string,word_t>  vars_ { };
};


class TokenList final {
    public:
        TokenList(const std::string& path) {
            auto in = ifstream { path };
            if (not in) {
                throw system_error { errno, system_category() };
            }
            using iter = istream_iterator<string>;
            copy(iter{in}, iter{}, back_inserter(tokens_));
        }

        bool          empty() const { return tokens_.empty(); }
        const string& peek() const { return tokens_.empty() ? empty_ : tokens_.front(); }
        string        get() { auto tok = tokens_.front(); tokens_.pop_front(); return tok; }
        bool          try_get(string_view str) { return peek() == str ? (tokens_.pop_front(), true) : false; }
        void          unget(string token) { tokens_.push_front(token); }

        void expect(string_view str) {
            if (get() != str) {
                throw runtime_error { "Parse error, expected " + string{str} };
            }
        }

    private:
        string empty_ = "";
        deque<string>  tokens_ { };
};


void parse_expression(TokenList& tokens, Program& prog) {
    auto token = tokens.get();
    auto op    = tokens.peek();

    bool has_op = op == Plus or op == Minus or op == Multiply or op == LessThan or op == GreaterThan;

    if (has_op) {
        tokens.get();
        parse_expression(tokens, prog);
        prog << SWAP;
    }

    try {
        auto value = stoi(string{token});
        prog << SET << value;
    } catch (const invalid_argument&) {
        prog << LOAD << prog[token];
    }

    if (has_op) {
        if (op == Plus) {
            prog << SAVE << 0 << SET << -1 << MUL << SWAP << LOAD << 0 << SUB;
        } else if (op == Minus) {
            prog << SUB;
        } else if (op == Multiply) {
            prog << MUL;
        } else if (op == LessThan) {
            prog << SWAP << SUB;
        } else if (op == GreaterThan) {
            prog << SUB;
        }
    }
}


void parse_statement(TokenList& tokens, Program& prog) {
    if (tokens.try_get(If)) {
        parse_expression(tokens, prog);
        tokens.expect(Then);
        word_t jmp1, jmp2, jmp3 = 0, if_start, else_start = 0, end_addr;
        prog << JMPP >> jmp1 << -1 << SET << 1 << JMPP >> jmp2 << -1 >> if_start;
        while (not tokens.try_get(EndIf)) {
            if (tokens.try_get(Else)) {
                prog << SET << 1 << JMPP >> jmp3 << -1 >> else_start;
            } else {
                parse_statement(tokens, prog);
            }
        }
        prog >> end_addr;
        prog[jmp1] = if_start;
        prog[jmp2] = else_start ? else_start : end_addr;
        if (else_start) {
            prog[jmp3] = end_addr;
        }
    } else if (tokens.try_get(While)) {
        word_t start_addr, jmp, end_addr;
        prog >> start_addr;
        parse_expression(tokens, prog);
        tokens.expect(Do);
        prog << SWAP << SET << -1 << MUL << SWAP << SET << -1 << SWAP << SUB << JMPP >> jmp << -1;
        while (not tokens.try_get(EndWhile)) {
            parse_statement(tokens, prog);
        }
        prog << SET << 1 << JMPP << start_addr >> end_addr;
        prog[jmp] = end_addr;
    } else {
        auto var = tokens.get();
        if (tokens.try_get(Assign)) {
            parse_expression(tokens, prog);
            prog << SAVE << prog[var];
        } else {
            tokens.unget(var);
            parse_expression(tokens, prog);
            prog << PRINT;
        }
    }
}


int main(int argc, char *argv[]) {
    if (argc < 2) {
        cerr << "usage: " << argv[0] << " <file(s).txt>\n";
        return 1;
    }

    try {
        for (int i = 1; i < argc; i++) {
            auto prog = Program { };
            auto tokens = TokenList(argv[i]);
            while (not tokens.empty()) {
                parse_statement(tokens, prog);
            }
            prog.run();
        }
        return 0;
    } catch (const exception& e) {
        cerr << argv[0] << ": " << e.what() << "\n";
        return 1;
    }

    return 0;
}


