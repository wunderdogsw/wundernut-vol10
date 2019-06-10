use crate::state::{Int, State};

#[derive(Debug)]
pub struct Program {
    pub statements: Statements,
    pub return_variable: String,
}

impl Program {
    pub fn execute(&self) -> Int {
        let mut state = State::new();
        self.statements.execute(&mut state);
        state.get(&self.return_variable)
    }
}

#[derive(Debug)]
pub struct Statements {
    pub statements: Vec<Statement>,
}

impl Statements {
    fn execute(&self, state: &mut State) {
        for statement in &self.statements {
            statement.execute(state)
        }
    }
}

#[derive(Debug)]
pub enum Statement {
    Set {
        variable: String,
        expression: Expression,
    },
    If {
        expression: ComparisonExpression,
        if_statements: Statements,
        else_statements: Statements,
    },
    While {
        expression: ComparisonExpression,
        statements: Statements,
    },
}

impl Statement {
    fn execute(&self, state: &mut State) {
        match self {
            Statement::Set {
                variable,
                expression,
            } => {
                let value = expression.evaluate(state);
                state.set(&variable, value);
            }
            Statement::If {
                expression,
                if_statements,
                else_statements,
            } => {
                if expression.evaluate(state) {
                    if_statements.execute(state)
                } else {
                    else_statements.execute(state)
                };
            }
            Statement::While {
                expression,
                statements,
            } => {
                while expression.evaluate(state) {
                    statements.execute(state)
                }
            }
        }
    }
}

#[derive(Debug)]
pub enum Value {
    Variable(String),
    Const(Int),
}

impl Value {
    fn get(&self, state: &State) -> Int {
        match self {
            Value::Variable(name) => state.get(name),
            Value::Const(value) => *value,
        }
    }
}

#[derive(Debug)]
pub enum Expression {
    Const(Int),
    Expression {
        left: Value,
        right: Value,
        operator: Operator,
    },
}

impl Expression {
    fn evaluate(&self, state: &mut State) -> Int {
        match self {
            Expression::Const(value) => *value,
            Expression::Expression {
                left,
                right,
                operator,
            } => {
                let left_value = left.get(state);
                let right_value = right.get(state);
                match operator {
                    Operator::Add => left_value + right_value,
                    Operator::Substract => left_value - right_value,
                    Operator::Multiply => left_value * right_value,
                }
            }
        }
    }
}

#[derive(Debug)]
pub enum Operator {
    Add,
    Substract,
    Multiply,
}

#[derive(Debug)]
pub struct ComparisonExpression {
    pub left: String,
    pub right: Value,
    pub operator: ComparisonOperator,
}

impl ComparisonExpression {
    fn evaluate(&self, state: &mut State) -> bool {
        let left_value = state.get(&self.left);
        let right_value = self.right.get(state);
        match self.operator {
            ComparisonOperator::GreaterThan => left_value > right_value,
            ComparisonOperator::LessThan => left_value < right_value,
        }
    }
}

#[derive(Debug)]
pub enum ComparisonOperator {
    GreaterThan,
    LessThan,
}
