(ns cia.stores.es.coa
  (:import java.util.UUID)
  (:require
   [schema.core :as s]
   [cia.schemas.coa :refer [COA
                            NewCOA
                            realize-coa]]
   [cia.stores.es.document :refer [create-doc
                                   update-doc
                                   get-doc
                                   delete-doc
                                   search-docs]]))

(def ^{:private true} mapping "coa")

(defn- make-id [schema j]
  (str "coa" "-" (UUID/randomUUID)))

(defn handle-create-coa [state new-coa]
  (let [id (make-id COA new-coa)
        realized (realize-coa new-coa id)]
    (create-doc (:conn state)
                (:index state)
                mapping
                realized)))

(defn handle-update-coa [state id new-coa]
  (update-doc (:conn state)
              (:index state)
              mapping
              id
              new-coa))

(defn handle-read-coa [state id]
  (get-doc (:conn state)
           (:index state)
           mapping
           id))

(defn handle-delete-coa [state id]
  (delete-doc (:conn state)
              (:index state)
              mapping
              id))

(defn handle-list-coas [state filter-map]
  (search-docs (:conn state)
               (:index state)
               mapping
               filter-map))
