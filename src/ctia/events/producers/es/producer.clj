(ns ctia.events.producers.es.producer
  (:require
   [schema.core :as s]
   [clojurewerkz.elastisch.native.document :as native-document]
   [clojurewerkz.elastisch.rest.document :as rest-document]
   [ctia.lib.es.index :refer [ESConnState]]
   [ctia.lib.es.slice :refer [memoized-create-slice-alias!
                              memoized-create-index-alias!
                              date->slice-props]]
   [ctia.events.schemas :refer [Event]]
   [ctia.events.producer :refer [IEventProducer]]))

(s/defn transform-fields [e :- Event]
  "for ES compat, transform field data to a map"
  (if-let [fields (:fields e)]
    (assoc e :fields
           (map #(hash-map :field (first %)
                           :action (second %)
                           :change (last %)) fields)) e))

(defn create-doc-fn [conn]
  (if (:uri conn)
    rest-document/create
    native-document/create))

(s/defn handle-produce-event :- s/Str
  "given a conn state and an event write the event to ES"
  [state :- ESConnState
   event :- Event]

  (let [slice-props
        (date->slice-props (:timestamp event)
                           (:index state)
                           (get-in state [:props :slice]))]

    (memoized-create-index-alias! state
                                  (:name slice-props)
                                  (:filter slice-props))

    (:_id ((create-doc-fn (:conn state))
           (:conn state)
           (:name slice-props)
           "event"
           (transform-fields event)
           :routing (:name slice-props)))))

(defrecord EventProducer [state]
  IEventProducer
  (produce-event [_ event]
    (handle-produce-event state event)))
