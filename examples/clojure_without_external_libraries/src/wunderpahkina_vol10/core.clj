(ns wunderpahkina-vol10.core
  (:require [clojure.string :as s])
  (:gen-class))

(defn- parse-int [s]
  (Integer/parseInt s))

(defn- int-or-symbol [s]
  (try
    {:type :int
     :value (parse-int s)}
    (catch NumberFormatException _
      {:type :symbol
       :name s})))

(defn- ->token [[fst snd & rst]]
  (cond
    (= snd "AWOO") {:type :assign
                    :name fst
                    :value (->token rst)}
    (= snd "ARF") {:type :multiply
                   :left (int-or-symbol fst)
                   :right (->token rst)}
    (= snd "WOOF") {:type :plus
                    :left (int-or-symbol fst)
                    :right (->token rst)}
    (= snd "BARK") {:type :minus
                    :left (int-or-symbol fst)
                    :right (->token rst)}
    (= snd "YAP") {:type :greater-than
                   :left (int-or-symbol fst)
                   :right (->token rst)}
    (= snd "YIP") {:type :less-than
                   :left (int-or-symbol fst)
                   :right (->token rst)}
    (#{"VUH" "BOW"} snd) (->token [fst])
    (= fst "RUF?") {:type :if
                    :condition (->token (concat [snd] rst))}
    (= fst "ROWH") {:type :else}
    (= fst "GRRR") {:type :while
                    :condition (->token (concat [snd] rst))}
    (= fst "ARRUF") {:type :end-if}
    (= fst "BORF") {:type :end-while}
    (nil? snd) (int-or-symbol fst)
    :else (throw (Exception. (str "unknown '" fst snd rst "'")))))

(declare split-to-actions)

(defn- create-if [fst rst]
  (let [if-body (take-while #(not= (:type %) :end-if) rst)
        remaining (rest (drop-while #(not= (:type %) :end-if) rst))
        [then _ else] (partition-by #(= (:type %) :else) if-body)]
    {:action (assoc fst :then (split-to-actions then) :else (split-to-actions else)) :rst remaining}))

(defn- create-when [fst rst]
  (let [while-body (take-while #(not= (:type %) :end-while) rst)
        remaining (rest (drop-while #(not= (:type %) :end-while) rst))]
    {:action (assoc fst :body (split-to-actions while-body)) :rst remaining}))

(defn- next-action [[{t :type :as token} & rst]]
  (cond
    (= t :if) (create-if token rst)
    (= t :while) (create-when token rst)
    :else {:action token :rst rst}))

(defn- split-to-actions [tokens]
  (loop [result []
         {:keys [action rst]} (next-action tokens)]
    (if (seq rst)
      (recur (conj result action) (next-action rst))
      (conj result action))))

(declare ->value)

(defn- ->value [symbols {t :type left :left right :right value :value symbol-name :name}]
  (case t
    :plus (+ (->value symbols left) (->value symbols right))
    :minus (- (->value symbols left) (->value symbols right))
    :multiply (* (->value symbols left) (->value symbols right))
    :symbol (get symbols symbol-name)
    :int value))

(defn- ->assign [{symbol-name :name value :value}]
  (fn [symbols] (assoc symbols symbol-name (->value symbols value))))

(defn- ->print [value]
  (fn [symbols]
    (println (->value symbols value))
    symbols))

(defn- reducer-chain [reducers]
  (apply comp (reverse reducers)))

(defn- ->comparison [{t :type left :left right :right}]
  (fn [symbols]
    (if (= t :less-than)
      (< (->value symbols left) (->value symbols right))
      (> (->value symbols left) (->value symbols right)))))

(declare action->reducer)

(defn- ->if [{:keys [condition then else]}]
  (fn [symbols]
    (if ((->comparison condition) symbols)
      ((reducer-chain (map action->reducer then)) symbols)
      ((reducer-chain (map action->reducer else)) symbols))))

(defn- ->while [{:keys [condition body]}]
  (fn [input-symbols]
    (loop [symbols input-symbols]
      (if ((->comparison condition) symbols)
        (recur ((reducer-chain (map action->reducer body)) symbols))
        symbols))))

(defn- action->reducer [{t :type :as action}]
  (case t
    :assign (->assign action)
    :symbol (->print action)
    :if (->if action)
    :while (->while action)
    (throw (Exception. (str "Unknown action " action)))))

(defn preprocess-input [filename]
  (println (str "-- " filename " --"))
  (->> (s/split (slurp filename) #"\n")
       (map #(s/split % #"\s+"))
       (map #(remove s/blank? %))))

(defn ->actions [input-rows]
  (->> input-rows
       (map ->token)
       split-to-actions))

(defn interpret [actions]
  (let [intepreter (->> actions
                        (map action->reducer)
                        reducer-chain)
        symbol-map {}]
    (intepreter symbol-map)))

(defn -main [filename]
  (->> (preprocess-input filename)
       ->actions
       interpret))

