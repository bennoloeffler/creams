(ns clojurenote-demo.bel-script
  (:import (com.evernote.edam.notestore NotesMetadataResultSpec NoteFilter)
           (com.evernote.edam.type NoteSortOrder)))
(use 'clojurenote.notes)

(def en-user {:access-token
              "S=s1:U=934f2:E=184087a6d80:C=17cb0c94180:P=1cd:A=en-devtoken:V=2:H=1b0f770234a35b691c8e14bc8f9d0fac"
              :notestore-url
              "https://sandbox.evernote.com/shard/s1/notestore"})


(def empty-en {:notebooks {}
               :notes     {}})


(def state-en (atom empty-en))

(defn reset-notebooks []
  (swap! state-en assoc :notebooks {}))

(defn reset-notes []
  (swap! state-en assoc :notes {}))


;;
;; id
;;

(def current-id (atom 0))

(defn next-id! []
  (swap! current-id inc)
  @current-id)

;;
;; forward and backward mapping
;; forward  k -> v
;, backward v -> k
;;
(def empty-fbm {:k->v {}
                :v->k {}})
(def forward-backward-mapping (atom empty-fbm))

(defn init-fbm []
  (reset! forward-backward-mapping empty-fbm))

(defn fbm-k
  "get key from value"
  [v]
  (-> @forward-backward-mapping :v->k (get v)))

(defn fbm-v
  "get value from key"
  [k]
  (-> @forward-backward-mapping :k->v (get k)))

(defn check-no-double-entry [k v]
  (let [check-has-k (fbm-v k)
        check-has-v (fbm-k v)]
    (when (and check-has-k check-has-v)
      (throw (ex-info (str "both, key and value already exist in mapping: " k " " v)
                      {:key            k
                       :existing-value check-has-k
                       :existing-key   check-has-v
                       :value          v})))
    (when check-has-k
      (throw (ex-info (str "key already exists with value: " k)
                      {:key k :existing-value check-has-k})))
    (when check-has-v
      (throw (ex-info (str "value already exists with key:" v)
                      {:existing-key check-has-v :value v})))
    true))

(defn- put-both
  "atomic implementation of putting both"
  [fbm k v]
  (-> fbm
      (assoc-in [:k->v k] v)
      (assoc-in [:v->k v] k)))

(defn fbm
  "create a forward-backward mapping of key-value"
  [k v]
  {:pre [(some? k) (some? v) (check-no-double-entry k v)]}
  (swap! forward-backward-mapping put-both k v))


(comment
  (fbm 3 "*abc-xyz")
  (fbm-k "abc-xyz")
  (fbm-v 3))

#_(defn map-short-id-to-guid!
    [guid]
    (let [check-has-guid (contains? (set (vals (:id-map @state))) guid)]
      (if check-has-guid
        (throw (ex-info "guid already exists" {:guid guid}))
        (let [id (next-id!)]
          (swap! state assoc-in [:id-map id] guid)))))

#_(defn guid->short-id
    "is a guid cashed?
   if not return nil"
    [guid])

#_(defn short-id->guid
    "get the guid of short-id - or null"
    [short-id]
    (-> @state :id-map short-id))

#_(defn read-all-notes
    "read guid, name and provide a human readable id"
    [en-user guid-or-short-id]
    (->>
      (list-notebooks en-user)
      (map-indexed
        (fn [idx x]
          (let [b (bean x)])
          {:short-id idx}
          :guid (:guid b)
          :name (:name b)
          :notebook b))))

(defn read-all-notebooks
  "read guid, name and provide a human readable id"
  [en-user]
  (->>
    (list-notebooks en-user)
    (map
      (fn [x]
        (let [b (bean x)]
          {:short-id (next-id!)
           :name     (:name b)
           :guid     (:guid b)})))))
;:notebook b})))))


(defn read-all-notes
  "read guid, name and provide a human readable id"
  [en-user notebook-short-id]
  (->>
    (basic-notes-for-notebook en-user
                              (fbm-v notebook-short-id)
                              :result-spec (doto
                                             (NotesMetadataResultSpec.)
                                             (.setIncludeTitle true)
                                             (.setIncludeNotebookGuid true)
                                             (.setIncludeAttributes true)
                                             (.setIncludeContentLength true)
                                             (.setIncludeCreated true)
                                             (.setIncludeDeleted true)
                                             (.setIncludeContentLength true)
                                             (.setIncludeAttributes true)
                                             (.setIncludeLargestResourceMime true)
                                             (.setIncludeLargestResourceSize true))
                              #_:note-filter #_(doto (NoteFilter.)
                                                 (.setNotebookGuid (fbm-v notebook-short-id))
                                                 (.setOrder (.getValue NoteSortOrder/CREATED))
                                                 (.setAscending false))

                              #_:offset      #_0
                              #_:max-notes   #_100)
    (map
      (fn [x]
        (let [b (bean x)]
          ;(println "x: " x)
          ;(println "b: " b)
          {:short-id       (next-id!)
           :title          (:title b)
           :note           (bean (get-note en-user (:guid b)))
           :guid           (:guid b)
           :note-meta-data b})))))

(defn cache-ids! [k-notes-or-notebooks]
  (for [nb (k-notes-or-notebooks @state-en)]
    (fbm (:short-id nb) (:guid nb))))

(defn cache-all-notebooks!
  "read all notebooks and cache in local @state."
  [en-user]
  (swap! state-en update-in [:notebooks]
         (fn [_] (read-all-notebooks en-user)))
  (cache-ids! :notebooks))

(defn cache-all-notes!
  "read all notes of all notebooks and cache in local @state."
  [en-user]
  (let [notes (->> (:notebooks @state-en)
                   (map :short-id)
                   (map (fn [short-id]
                          (println "note, short-id: " short-id)
                          (read-all-notes en-user short-id)))
                   flatten)]
    (swap! state-en assoc :notes notes))
  (cache-ids! :notes))


(defn reset-data! []
  (reset-notebooks)
  (reset-notes)
  (init-fbm))

(defn cache-all-data [en-user]
  (reset-data!)
  (cache-all-notebooks! en-user)
  (cache-all-notes! en-user))

(defn create-n
  [title, txt & short-id-nb])
(defn show-n
  [short-id])
(defn save-n
  [short-id, add-txt])
(defn del-n [short-id])
(defn create-nb [name])
(defn show-nb [short-id])

(comment
  (reset-data!)
  (cache-all-notebooks! en-user)
  (cache-all-notes! en-user)


  (read-all-notes en-user 1)
  (get-note en-user (fbm-v 1)))
;(cache-notebook-ids!))

@state-en
@forward-backward-mapping