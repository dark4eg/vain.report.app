(ns vain.report.app.api
  (:require [promesa.core :as p]
            [ajax.core :refer [GET POST]]
            [re-frame.core :as rf]
            [vain.report.app.config :as config]))

;(defn request [{:keys [uri params cb] :as request}]
;  (if-let [s (uri api)]
;    (go (let [response (<! (((:type (uri api)) http-method) (str host (:url s)) params))]
;          (let [r (:body response)]
;            (if (= "user unauthorized" (:msg r))
;              (js/alert "Ошибка получения данных")
;              (cb response)))))
;    (js/Error. (str "not found value for key " uri))))



(defn- requestGET [{:keys [uri params cb] :as request}]
  (p/promise (fn [resolve reject]
               (GET uri {:handler #(resolve (cb %))
                         :error-handler #(reject)}))))

(defn- player-stats [player]
  (let [id (:db/id player)
        p (p/all [(requestGET {:uri (str config/host "/api/player/" id "/perf/")
                               :params {}
                               :cb (fn [e] e)})
                  (requestGET {:uri (str config/host "/api/player/" id "/match/?limit=" 5 "&offset=" 0)
                               :cb (fn [e] e)})])]
    (p/then p (fn [[perf match-history]]
                (rf/dispatch [:player player perf match-history])))))

(defn player [player]
  (requestGET {:uri (str config/host "/api/player/" player "/")
               :cb (fn [e] (player-stats e))}))

(defn hero [id]
  (let [p (p/all [(requestGET {:uri (str config/host "/api/stats/hero/" id "/player")
                               :params {}
                               :cb (fn [e] e)})
                  (requestGET {:uri (str config/host "/api/stats/hero/" id "/teammates")
                               :cb (fn [e] e)})])]
    (p/then p (fn [[top-players teammates]]
                (rf/dispatch [:hero id top-players teammates])))))

(defn init []
  (let [p (p/all [(requestGET {:uri (str config/host "/api/stats/match/mode/")
                               :params {}
                               :cb (fn [e] e)})
                  (requestGET {:uri (str config/host "/api/stats/match/regions/")
                               :cb (fn [e] e)})
                  (requestGET {:uri (str config/host "/api/stats/hero/")
                               :cb (fn [e] e)})])]
    (p/then p (fn [[modes regions hero-stats]]
                (rf/dispatch [:init/data modes regions hero-stats])))))
