(ns vain.report.app.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
  :current/route
  (fn [db _]
    (:current/route db)))

(re-frame/reg-sub
  :matches/mode
  (fn [db _]
    (:matches/mode db)))

(re-frame/reg-sub
  :matches/region
  (fn [db _]
    (:matches/region db)))

(re-frame/reg-sub
  :item/list
  (fn [db _]
    (:item/list db)))

(re-frame/reg-sub
  :player/top
  (fn [db _]
    (:player/top db)))

(re-frame/reg-sub
  :hero/stats
  (fn [db _]
    (:hero/stats db)))

(re-frame/reg-sub
  :player/stats
  (fn [db _]
    (:player/stats db)))

(re-frame/reg-sub
  :player/perf
  (fn [db _]
    (:player/perf db)))

(re-frame/reg-sub
  :player/history
  (fn [db _]
    (:player/history db)))

(re-frame/reg-sub
  :hero/current
  (fn [db _]
    (:hero/current db)))

(re-frame/reg-sub
  :state/load
  (fn [db _]
    (:state/load db)))