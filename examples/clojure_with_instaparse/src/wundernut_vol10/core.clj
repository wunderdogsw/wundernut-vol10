(ns wundernut-vol10.core
  (:require [instaparse.core :as insta])
  (:gen-class))

(def doggocode "
statements = statement+
<statement> = assignment | identifier | if-statement | while-loop
if-statement = <'RUF?'> expression <'VUH'> statements <'ROWH'> statements <'ARRUF'>
while-loop = <'GRRR'> expression <'BOW'> statements <'BORF'>
assignment = identifier <'AWOO'> expression
<expression> = binary-expression | literal | identifier
binary-expression = expression operation expression
<operation> = add | sub | mul | gt | lt
add = <'WOOF'>
sub = <'BARK'>
mul = <'ARF'>
gt = <'YAP'>
lt = <'YIP'>
literal = #'[0-9]+'
identifier = #'[a-z]+'
")

(def parser (insta/parser doggocode :auto-whitespace (insta/parser "whitespace = #'\\s+'")))

(defmulti doggo-eval (fn [context ast]
                       (first ast)))

(defn eval-file [filename]
  (:value (doggo-eval {} (parser (slurp filename)))))

(defmethod doggo-eval
  :statements
  [context [_ & statements]]
  (reduce (fn [{:keys [context]} statement]
            (doggo-eval context statement))
          {:context context}
          statements))

(defmethod doggo-eval
  :if-statement
  [context [_ condition then else]]
  (if (:value (doggo-eval context condition))
    (doggo-eval context then)
    (doggo-eval context else)))

(defmethod doggo-eval
  :while-loop
  [context [_ condition statements]]
  (loop [context context]
    (if (:value (doggo-eval context condition))
      (recur (:context (doggo-eval context statements)))
      {:context context})))

(defmethod doggo-eval
  :assignment
  [context [_ [_ identifier] expression]]
  (let [{:keys [value]} (doggo-eval context expression)]
    {:context (assoc context identifier value)}))

(defmethod doggo-eval
  :binary-expression
  [context [_ exp1 [op] exp2]]
  (let [val1 (:value (doggo-eval context exp1))
        val2 (:value (doggo-eval context exp2))
        f (case op
            :add +
            :sub -
            :mul *
            :gt >
            :lt <)]
    {:context context
     :value   (f val1 val2)}))

(defmethod doggo-eval
  :literal
  [context [_ literal]]
  {:context context
   :value   (Long/parseLong literal)})

(defmethod doggo-eval
  :identifier
  [context [_ identifier]]
  {:context context
   :value   (get context identifier)})

(defn -main [filename]
  (println (eval-file filename)))
