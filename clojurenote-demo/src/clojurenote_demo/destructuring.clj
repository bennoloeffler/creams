(ns clojurenote-demo.destructuring)

(comment
  (def p (partial println))

  ;;
  ;; sequential: [a [b c]] & d :as all] origin
  ;; with strings, lists and vectors (nested)
  ;;
  (def two-nums '(5 6))
  (let [[a b] two-nums] (p a b))
  (let [[a] two-nums] (p a))
  (let [[a b c] two-nums] (p a b c))

  (def names ["Michael" "Amber" "Aaron" "Nick" "Earl" "Joe"])
  (let [[n1 & other-names] names]
    (p n1)
    (apply p other-names))
  (let [[n1 _ _ _ _ n6] names]
    (p n1 " " n6))
  (let [[n1 :as all-names] names]
    (p "the first of all is: " n1)
    (p "all are: " all-names))

  (def fruits ["apple" "orange" "strawberry" "peach" "pear" "lemon"])
  (let [[item1 _ item3 & remaining :as all-fruits] fruits]
    (p "1st and 3rd fruits are" item1 "and" item3)
    (p "All: " all-fruits)
    (p "Remaining: " remaining))

  (def my-line [[5 10] [10 20]])
  (let [[[x1 y1] [x2 y2]] my-line]
    (p "Line from (" x1 "," y1 ") to (" x2 ", " y2 ")"))
  (let [[[a b :as group1] [c d :as group2]] my-line]
    (p a b group1)
    (p c d group2))

  ;;
  ;; associative
  ;; {a :key} origin
  ;; {:keys [a b c] :as all :or {a default} } origin
  ;; {:strs ...} ; for string-keys
  ;; {:syms ...} ; for symbol-keys

  (def client {:name        "Super Co."
               :location    "Philadelphia"
               :description "The worldwide leader in plastic tableware."})
  (let [{name :name
         location :location
         category :category :or {category "Category not found"}} client]
    (p name location category))
  (let [{:keys [name location description category] :or {category "no"}} client]
    (p name location "(" category ") -" description))
  (let [{name :name :as all} client]
    (p "The name is" name "\nAll: " all))

  (defn configure [val options]
    (let [{:keys [debug verbose] :or {debug false, verbose false}} options]
      (p "val =" val " debug =" debug " verbose =" verbose)))
  (configure 12 {:debug true})

  (defn configure [val & {:keys [debug verbose]
                          :or {debug false, verbose false}}]
    (p "val =" val " debug =" debug " verbose =" verbose))
  (configure 12 :debug true))

