use pest::iterators::Pair;
use pest::Parser;
use pest_derive::Parser;
use std::collections::HashMap;

#[derive(Parser)]
#[grammar = "awoo_grammar.pest"]
pub struct AwooParser;

#[derive(Debug, Default)]
pub struct AwooInterpreter {
    pub vars: HashMap<String, i32>,
}

impl AwooInterpreter {
    pub fn run_statements(&mut self, statements: Pair<Rule>) -> Option<i32> {
        debug_assert_eq!(statements.as_rule(), Rule::statements);
        let mut res: Option<i32> = None;
        for inner_pair in statements.into_inner() {
            match inner_pair.as_rule() {
                Rule::statement => res = self.run_statement(inner_pair),
                _ => unreachable!(),
            };
        }
        res
    }

    fn run_statement(&mut self, statement: Pair<Rule>) -> Option<i32> {
        debug_assert_eq!(statement.as_rule(), Rule::statement);
        let statement = statement.into_inner().next().unwrap();
        match statement.as_rule() {
            Rule::var_definition => {
                self.run_var_def(statement);
                None
            }
            Rule::identifier => {
                let n = *self
                    .vars
                    .get(statement.as_str())
                    .expect("Could not find var");
                Some(n)
            }
            Rule::if_else => self.run_if_else(statement),
            Rule::while_loop => {
                self.run_while_loop(statement);
                None
            }
            _ => unreachable!(),
        }
    }

    fn run_if_else(&mut self, pair: Pair<Rule>) -> Option<i32> {
        debug_assert_eq!(pair.as_rule(), Rule::if_else);
        let mut pairs = pair.into_inner();
        let mut if_start = pairs.next().expect("Expected if_start pair").into_inner();
        let if_statements = pairs.next().expect("Expected if statements");
        let _ = pairs.next().expect("Expected else pair");
        let else_statements = pairs.next().expect("Expected else statements");
        let _ = pairs.next().expect("Expected endif pair");

        let if_expr_lhs = if_start
            .next()
            .expect("Expected if expression left hand side");
        let if_operand = if_start.next().expect("Expected if expression operand");
        let if_expr_rhs = if_start
            .next()
            .expect("Expected if expression right hand side");

        let lhs_result = self.run_expr(if_expr_lhs);
        let rhs_result = self.run_expr(if_expr_rhs);;
        let result = match if_operand.as_rule() {
            Rule::less_than => lhs_result < rhs_result,
            Rule::greater_than => lhs_result > rhs_result,
            _ => unreachable!(),
        };
        if result {
            self.run_statements(if_statements)
        } else {
            self.run_statements(else_statements)
        }
    }

    fn run_while_loop(&mut self, pair: Pair<Rule>) {
        debug_assert_eq!(pair.as_rule(), Rule::while_loop);
        let mut inner = pair.into_inner();
        let while_start = inner.next().unwrap();
        let statements = inner.next().unwrap();

        let mut while_start = while_start.into_inner();
        let expression_lhs = while_start.next().unwrap();
        let logical_op = while_start.next().unwrap();
        let expression_rhs = while_start.next().unwrap();

        match logical_op.as_rule() {
            Rule::less_than => {
                while self.run_expr(expression_lhs.clone()) < self.run_expr(expression_rhs.clone())
                {
                    self.run_statements(statements.clone());
                }
            }
            Rule::greater_than => {
                while self.run_expr(expression_lhs.clone()) > self.run_expr(expression_rhs.clone())
                {
                    self.run_statements(statements.clone());
                }
            }
            _ => unreachable!(),
        }
    }

    fn run_var_def(&mut self, pair: Pair<Rule>) {
        debug_assert_eq!(pair.as_rule(), Rule::var_definition);
        let mut pairs = pair.into_inner();
        let id = pairs.next().expect("Expected id token");
        let expr = pairs.next().expect("Expected expression");
        let expr_val = self.run_expr(expr);
        self.vars.insert(id.as_str().to_owned(), expr_val);
    }

    fn run_expr(&mut self, pair: Pair<Rule>) -> i32 {
        match pair.as_rule() {
            Rule::unary_expr => self.unary_expr(pair),
            Rule::binary_expr => self.binary_expr(pair),
            _ => unreachable!(),
        }
    }

    fn unary_expr(&self, pair: Pair<Rule>) -> i32 {
        debug_assert_eq!(pair.as_rule(), Rule::unary_expr);
        let operand = pair.into_inner().next().expect("Expected operand");
        match operand.as_rule() {
            Rule::identifier => *self.vars.get(operand.as_str()).expect("Var not found"),
            Rule::integer => operand
                .as_str()
                .parse::<i32>()
                .expect("Could not parse integer"),
            _ => unreachable!(),
        }
    }

    fn binary_expr(&self, pair: Pair<Rule>) -> i32 {
        debug_assert_eq!(pair.as_rule(), Rule::binary_expr);
        let mut pairs = pair.into_inner();
        let a = self.get_val(&pairs.next().unwrap());
        let operation = pairs.next().unwrap();
        let b = self.get_val(&pairs.next().unwrap());

        match operation.as_rule() {
            Rule::add => a + b,
            Rule::sub => a - b,
            Rule::mul => a * b,
            _ => unreachable!(),
        }
    }

    fn get_val(&self, pair: &Pair<Rule>) -> i32 {
        match pair.as_rule() {
            Rule::identifier => *self.vars.get(pair.as_str()).unwrap(),
            Rule::integer => pair.as_str().parse::<i32>().unwrap(),
            _ => unimplemented!(),
        }
    }
}

fn main() {
    let stmts1 = AwooParser::parse(Rule::statements, include_str!("../awoo1.txt")).unwrap();
    let stmts2 = AwooParser::parse(Rule::statements, include_str!("../awoo2.txt")).unwrap();
    let stmts3 = AwooParser::parse(Rule::statements, include_str!("../awoo3.txt")).unwrap();
    let stmts4 = AwooParser::parse(Rule::statements, include_str!("../awoo4.txt")).unwrap();
    let stmts5 = AwooParser::parse(Rule::statements, include_str!("../awoo5.txt")).unwrap();

    let mut all = vec![stmts1, stmts2, stmts3, stmts4, stmts5];
    let all = all.iter_mut().map(|s| s.next().unwrap());
    for statements in all {
        let mut awoo_interp = AwooInterpreter::default();
        let result = awoo_interp.run_statements(statements);
        println!("AwooInterp:{}", result.unwrap());
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use pest::iterators::Pairs;

    #[test]
    fn parse_ident() {
        AwooParser::parse(Rule::identifier, "foobar").unwrap();
    }

    #[test]
    fn parse_simple() {
        let txt = r#"AWOO"#;
        AwooParser::parse(Rule::assign, txt).unwrap();

        let txt = r#"WOOF"#;
        AwooParser::parse(Rule::add, txt).unwrap();

        let txt = r#"5 WOOF 5"#;
        AwooParser::parse(Rule::expression, txt).unwrap();
    }

    #[test]
    fn parse_nonzero_int() {
        AwooParser::parse(Rule::integer, "12345").unwrap();
    }

    #[test]
    fn parse_identifier() {
        AwooParser::parse(Rule::identifier, "lassie").unwrap();
    }

    #[test]
    fn parse_var_assign() {
        let txt = r#"lassie AWOO 5"#;
        let _ = AwooParser::parse(Rule::statements, txt).unwrap();
    }

    #[test]
    fn parse_var_assign_expression() {
        let txt = r#"lassie AWOO 5 WOOF 5"#;
        AwooParser::parse(Rule::var_definition, txt).unwrap();
    }

    #[test]
    fn parse_1() {
        println!("foo");
        use pest::Parser;
        let txt = include_str!("../awoo1.txt");
        let mut stmts: Pairs<_> = AwooParser::parse(Rule::statements, txt).unwrap();
        let pair: Pair<_> = stmts.next().unwrap();
        let mut interp = AwooInterpreter::default();
        assert_eq!(interp.run_statements(pair), Some(11));
    }

    #[test]
    fn parse_2() {
        use pest::Parser;
        let txt = include_str!("../awoo2.txt");
        let mut stmts: Pairs<_> = AwooParser::parse(Rule::statements, txt).unwrap();
        let pair: Pair<_> = stmts.next().unwrap();
        let mut interp = AwooInterpreter::default();
        assert_eq!(interp.run_statements(pair), Some(15));
    }

    #[test]
    fn parse_3() {
        use pest::Parser;
        let txt = include_str!("../awoo3.txt");
        let mut stmts: Pairs<_> = AwooParser::parse(Rule::statements, txt).unwrap();
        let pair: Pair<_> = stmts.next().unwrap();
        let mut interp = AwooInterpreter::default();
        assert_eq!(interp.run_statements(pair), Some(105));
    }

    #[test]
    fn parse_4() {
        use pest::Parser;
        let txt = include_str!("../awoo4.txt");
        let mut stmts: Pairs<_> = AwooParser::parse(Rule::statements, txt).unwrap();
        let pair: Pair<_> = stmts.next().unwrap();
        let mut interp = AwooInterpreter::default();
        assert_eq!(interp.run_statements(pair), Some(19));
    }

    #[test]
    fn parse_5() {
        use pest::Parser;
        let txt = include_str!("../awoo5.txt");
        let mut stmts: Pairs<_> = AwooParser::parse(Rule::statements, txt).unwrap();
        let pair: Pair<_> = stmts.next().unwrap();
        let mut interp = AwooInterpreter::default();
        assert_eq!(interp.run_statements(pair), Some(64185));
    }
}
