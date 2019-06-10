mod program;
mod state;

use std::fs;

#[macro_use]
extern crate lalrpop_util;
lalrpop_mod!(pub parser);

fn run_file(path: &'static str) -> state::Int {
    let program_parser = parser::ProgramParser::new();
    let source = fs::read_to_string(path).expect("Invalid source file");
    let program = program_parser.parse(&source).expect("Parsing failed");
    program.execute()
}

fn main() -> () {
    println!("{}", run_file("./data/task.woof"));
}

#[cfg(test)]
mod tests {
    use super::run_file;

    #[test]
    fn test1() {
        let result = run_file("./data/src1.woof");
        assert_eq!(result, 11);
    }

    #[test]
    fn test2() {
        let result = run_file("./data/src2.woof");
        assert_eq!(result, 15);
    }

    #[test]
    fn test3() {
        let result = run_file("./data/src3.woof");
        assert_eq!(result, 105);
    }

    #[test]
    fn test4() {
        let result = run_file("./data/src4.woof");
        assert_eq!(result, 19);
    }
}
