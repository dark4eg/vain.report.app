(ns vain.report.app.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [re-frisk.core :refer [enable-re-frisk!]]
              [vain.report.app.events]
              [bidi.bidi :as bidi]
              [accountant.core :as accountant]
              [goog.events :as events]
              [goog.history.EventType :as HistoryEventType]
              [vain.report.app.subs]
              [vain.report.app.config :as config]
              [vain.report.app.components.pages :as pages]
              [vain.report.app.api :as api]))

(defn app []
  (let [route (re-frame/subscribe [:current/route])]
    (fn []
      (let [page (:current-page @route)]
        [:div.app
         [:header
          [:a {:href (bidi/path-for pages/routes :index)}
           [:img {:className "logo"
                  :src       "/img/logo.png"
                  :alt       "vain.report"}]]]
         [:main (pages/route page)]
         [:footer]]))))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (enable-re-frisk!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [#'app]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db {}])
  (dev-setup)
  (api/init)
  (accountant/configure-navigation!
    {:nav-handler  (fn
                     [path]
                     (let [match (bidi/match-route pages/routes path)
                           current-page (:handler match)
                           route-params (:route-params match)
                           route @(re-frame/subscribe [:current/route])
                           current-scroll-top (.-pageYOffset js/window)]
                       (js/setTimeout #(do
                                         (.scroll js/window 0 (:scroll-top route))) 100)
                       (re-frame/dispatch-sync [:current/route {:current-page current-page
                                                          :route-params route-params
                                                          :scroll-top   current-scroll-top}])))
     :path-exists? (fn [path]
                     (boolean (bidi/match-route pages/routes path)))})
  (accountant/dispatch-current!)
  (mount-root))
