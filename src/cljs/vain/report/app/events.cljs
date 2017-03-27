(ns vain.report.app.events
    (:require [re-frame.core :as re-frame]
              [vain.report.app.db :as db]))

(re-frame/reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(re-frame/reg-event-db
  :current/route
  (fn [db [_ page]]
    (assoc db :current/route page)))

(re-frame/reg-event-db
  :player
  (fn [db [_ stats perf history]]
    (assoc db :player/stats stats
              :player/perf perf
              :player/history history)))

(re-frame/reg-event-db
  :hero
  (fn [db [_ id top-players teammates]]
    (assoc db :hero/current {:id id
                             :player top-players
                             :teamates teammates})))

(re-frame/reg-event-db
  :state/load
  (fn [db [_ state]]
    (assoc db :state/load state)))

(re-frame/reg-event-db
  :init/data
  (fn [db [_ modes regions hero-stats]]
    (assoc db :matches/mode modes
              :matches/region regions
              :hero/stats hero-stats)))

