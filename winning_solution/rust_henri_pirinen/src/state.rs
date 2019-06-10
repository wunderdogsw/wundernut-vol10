use std::collections::HashMap;

pub type Int = i64;

pub struct State {
    variables: HashMap<String, Int>,
}

impl State {
    pub fn new() -> State {
        State {
            variables: HashMap::new(),
        }
    }

    pub fn get(&self, name: &String) -> Int {
        *self
            .variables
            .get(name)
            .expect(&format!("Undefined variable {}", name))
    }

    pub fn set(&mut self, name: &String, value: Int) {
        self.variables.insert(name.clone(), value);
    }
}
