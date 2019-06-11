(ns pahkina.core
  (:require [instaparse.core :as insta]))

(def values (atom {}))

(defn parse-int [s]
  (if (number? s)
    s
    (Integer. (re-find  #"\d+" s ))))

(defn evil [[t & exps]]
  (case t
    :print (println (evil (first exps)))
    :number (parse-int (first exps))
    :word (parse-int ((keyword (first exps)) @values))
    :add (+ (evil (first exps)) (evil (last exps)))
    :mul (* (evil (first exps)) (evil (last exps)))
    :sub (- (evil (first exps)) (evil (last exps)))
    :less (< (evil (first exps)) (evil (last exps))) ; YIP
    :more (> (evil (first exps)) (evil (last exps))) ; YAP
    :param (swap! values assoc (keyword (last (first exps))) (evil (last exps)))
    :if (let [[e [_ & tl] [_ & fl]] exps]
           (if (evil e)
             (when (not (nil? tl)) (doseq [p tl] (evil p)))
             (when (not (nil? fl)) (doseq [p fl] (evil p)))))
    :loop (let [[e & l] exps]
            (while (evil e)
              (doseq [p l] (evil p))))))

(def parser
  (insta/parser
    "<S> = AB*
    <AB> = print | param | loop | if

    param = word <' AWOO '> (token | ( add | mul | sub)) <(nl)?>
    loop = <'GRRR '> (less | more) <' BOW' nl> (param|if)* <'BORF' (nl?)>
    if = <'RUF? '> (less | more) <' VUH' nl> true-list <'ROWH' nl> false-list <'ARRUF' (nl)?>

    print = word (<nl>)?

    true-list = (param)*
    false-list = (param)*

    add = token <' WOOF '> token
    mul = token <' ARF '> token
    sub = token <' BARK '> token

    less = token <' YIP '> token
    more = token <' YAP '> token

    <token> = number | word

    nl = '\n'
    word = #'[a-zA-Z]+'
    number = #'[0-9]+'"))

(defn test1 []
  (parser "lassie AWOO 5
luna AWOO 6
bailey AWOO lassie WOOF luna
bailey"))

(defn test2 []
  (parser "roi AWOO 5
RUF? roi YAP 2 VUH
roi AWOO roi ARF 3
ROWH
roi AWOO roi WOOF 100
ARRUF
roi"))

(defn test3 []
  (parser "roi AWOO 5
RUF? roi YIP 2 VUH
roi AWOO roi ARF 3
ROWH
roi AWOO roi WOOF 100
ARRUF
roi"))

(defn test4 []
  (parser "quark AWOO 6 BARK 2
gromit AWOO 5
milo AWOO 0
GRRR milo YIP gromit BOW
quark AWOO quark WOOF 3
milo AWOO milo WOOF 1
BORF
quark"))

(defn task []
  (parser "samantha AWOO 1
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
samantha"))

(defn evil-all [l]
  (doseq [p l] (evil p)))

(defn -main [& args]
  ;(evil-all (test1))
  ;(evil-all (test2))
  ;(evil-all (test3))
  ;(evil-all (test4))
  (println "OIKEA VASTAUS ON:")
  (evil-all (task)))
