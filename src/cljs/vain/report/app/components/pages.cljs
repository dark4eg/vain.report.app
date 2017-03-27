(ns vain.report.app.components.pages
  (:require [re-frame.core :as rf]
            [vain.report.app.api :as api]
            [bidi.bidi :as bidi]
            [clojure.string :as string]
            [reagent.core :as r]
            [ajax.core :refer [GET POST]]
            [vain.report.app.config :as config]))

(def routes
  ["/"
   [["" :index]
    [["player/" :name] :player]
    [["hero/" :name] :hero]
    [["match/" :id] :match]
    [true :four-o-four]]])

(defmulti route identity)

(defmethod route :match []
  (let []
    [:div "match"]))

(defmethod route :player []
  (let [mode-list @(rf/subscribe [:matches/mode])
        hero-list @(rf/subscribe [:hero/stats])
        perf @(rf/subscribe [:player/perf])
        history @(rf/subscribe [:player/history])
        route @(rf/subscribe [:current/route])
        name (-> route :route-params :name)
        current @(rf/subscribe [:player/stats])
        player (if (= name (:player/attr.name current))
                 current
                 (api/player name))
        load? (not (= name (:player/attr.name current)))]
    (if load?
      [:img.page-loading {:src "/img/load.gif"}]
      [:div.row.center-xs
       [:div.col-xs-11.col-sm-8.col-md-4.col-lg-3
        [:h3 "player profile"]
        [:div.row
         [:div.col-xs-12.line
          [:span.title (:player/attr.name player)]
          [:span.value (:player/attr.stats.level player)]
          ]
         [:div.col-xs-12.line
          [:span.title "Matches played"]
          [:span.value (:player/attr.stats.played player)]
          ]
         [:div.col-xs-12.line
          [:span.title "Ranked matches"]
          [:span.value (:player/attr.stats.played_ranked player)]
          ]
         [:div.col-xs-12.line
          [:span.title "Win Rate"]
          [:span.value (str (int (* 100 (/ (:player/attr.stats.wins player)
                                           (:player/attr.stats.played player))))
                            "%")]]]]
       [:div.col-xs-12.col-sm-8.col-md-4.col-lg-3
        [:h3 "hero performance"]
        [:div.row.line
         [:div.col-xs-4.table-title "Hero"]
         [:div.col-xs-3.table-title "K.D.A"]
         [:div.col-xs-3.table-title "Win rate"]
         [:div.col-xs-2.table-title "Games"]]
        (for [[i p] (map vector (range) (->> perf
                                             (map (fn [e]
                                                    (merge {:id (first e)}
                                                           (last e))))
                                             (sort-by :kda)
                                             (reverse)))]
          (let [hero (first (filter #(= (:id p) (:db/id %)) hero-list))]
            [:div.row.middle-xs.line {:key (str "hero-ident-" i)}
             [:div.col-xs-4
              [:a {:href (bidi/path-for routes :hero :name (:hero/name hero))}
               (when-let [hero-id (:db/id hero)]
                 [:img {:src   (str "/img/hero/134x134/" hero-id ".jpg")
                        :style {:width  "100px"
                                :height "100px"}
                        }])]]
             [:div.col-xs-3.table-title (.toFixed (js/Number (:kda p)) 2)]
             [:div.col-xs-3.table-title (str (:winRate p) "%")]
             [:div.col-xs-2]]))
        ]
       [:div.col-xs-12.col-sm-8.col-md-4.col-lg-3
        [:h3 "match history"]
        (for [[i match] (map vector (range) history)]
          (let [participant (->> (:match/roster match)
                                 (map :roster/participant)
                                 (flatten)
                                 (filter (fn [e]
                                           (= (:db/id player) (-> e :participant/player :db/id))))
                                 (first)
                                 )
                mode (->> mode-list
                          (filter (fn [e]
                                    (= (-> match :match/attr.gameMode :db/id) (:db/id e))))
                          (first))
                hero (->> hero-list
                          (filter (fn [e]
                                    (= (-> participant :participant/attr.actor :db/id) (:db/id e))))
                          (first))]
            [:div.row.top-xs {:key (str "hero-ident-" i)}
             [:div.col-xs-4
              [:a {:href (str "/hero/" (:hero/name hero))}  ;TODO bug with dirty hack
               (when-let [hero-id (-> participant :participant/attr.actor :db/id)]
                 [:img {:src   (str "/img/hero/134x134/" hero-id ".jpg")
                        :style {:width  "100px"
                                :height "100px"}}])]]
             [:div.col-xs-8
              [:div.box
               [:div.row
                [:div.col-xs-12 {:style {:height          "14px"
                                         :backgroundColor (if (:participant/attr.stats.winner participant)
                                                            "#4CAF50"
                                                            "#FF8080")}}]]
               [:div.row {:style {:height "96px"}}
                [:div.col-xs-4
                 [:div.row.middle-xs
                  [:div.col-xs-12 {:style {:height "96px"}}
                   [:a.table-title {:href (str "/hero/" (:hero/name hero))}  ;TODO bug with dirty hack
                    (-> hero :hero/name)]]]]
                [:div.col-xs-4
                 [:div.row.middle-xs
                  [:div.col-xs-12.table-title {:style {:height "48px"}} (-> mode :gameMode/name)]
                  [:div.col-xs-12.table-title {:style {:height "48px"}} (if (:participant/attr.stats.winner participant)
                                                                          "Victory"
                                                                          "Defeat")]]]
                [:div.col-xs-4
                 [:div.row.middle-xs
                  [:div.col-xs-12.table-title {:style {:height "48px"}} "K/D/A"]
                  [:div.col-xs-12.table-title {:style {:height "48px"}} (str (:participant/attr.stats.kills participant) "/"
                                                                             (:participant/attr.stats.deaths participant) "/"
                                                                             (:participant/attr.stats.assists participant))]]]]]]
             [:div.col-xs-2]]
            ))
        ]
       ]

      )))

(defmethod route :hero []
  (let [hero-list @(rf/subscribe [:hero/stats])
        route @(rf/subscribe [:current/route])
        hero (->> hero-list
                  (filter (fn [e]
                            (= (-> route :route-params :name) (:hero/name e))))
                  (first))
        current @(rf/subscribe [:hero/current])
        load? (not (= (:db/id hero) (:id current)))
        player (if (= (:db/id hero) (:id current))
                 current
                 (api/hero (:db/id hero)))]
    [:div.row.center-xs
     [:div.col-xs-12.col-sm-6.col-md-4.col-lg-3
      [:h3 (str (:hero/name hero) " hero profile")]
      [:img {:src (str "/img/hero/300x300/" (:db/id hero) ".png")}]]
     [:div.col-xs-11.col-sm-6.col-md-4.col-lg-3.hero-stats
      [:div.row
       [:div.col-xs-12.line
        [:span.title "GAMES"]
        [:span.value (-> hero :hero/count.change)]]
       [:div.col-xs-12.line
        [:span.title "WIN RATE"]
        [:span.value (str (.toFixed (js/Number (* 100 (/ (-> hero :hero/count.winner) (-> hero :hero/count.change))))
                                    2)
                          "%")]]
       [:div.col-xs-12.line
        [:span.title "KILLS"]
        [:span.value (nth (-> hero :hero/kda) 0)]]
       [:div.col-xs-12.line
        [:span.title "DEATHS"]
        [:span.value (nth (-> hero :hero/kda) 1)]]
       [:div.col-xs-12.line
        [:span.title "ASSISTS"]
        [:span.value (nth (-> hero :hero/kda) 2)]]
       ]]
     (when load?
       [:div.col-xs-12
        [:img.page-partial-loading {:src "/img/load.gif"}]])
     (when (not load?)
       [:div.col-xs-12.hero-hr-list
        [:h3 "best teammates"]
        [:div {:className "row content-scroll-wrap"}
         (for [[i h] (map vector (range) (-> current :teamates))]
           (let [hero (->> hero-list
                           (filter (fn [e]
                                     (= (:hero/id h) (:db/id e))))
                           (first))]
             [:div {:key (str "hero-" i)}
              [:a {:href (bidi/path-for routes :hero :name (:hero/name hero))}
               [:img {:src (str "/img/hero/134x134/" (:db/id hero) ".jpg")
                      :alt (:hero/name hero)}]]
              ]))]])
     (when (not load?)
       [:div.col-xs-12.col-sm-10.col-md-8.col-lg-6.best-hero-player
        [:h3 (str "best " (:hero/name hero) " players")]
        [:div.row.line
         [:div.col-xs-6 {:style {:text-align "left"}}
          [:span {:style {:paddingLeft "10px"}} "Player"]]
         [:div.col-xs-3 "Kills"]
         [:div.col-xs-3 "Win rate"]]
        (for [[i p] (map vector (range) (reverse (sort-by :kills (-> current :player))))]
          (let [pp (:player p)]
            [:div.row.middle-xs.line {:key (str "hero-ident-" i)}
             [:div.col-xs-6 {:style {:text-align "left"}}
              [:a {:href (bidi/path-for routes :player :name (:player/attr.name pp))}
               (:player/attr.name pp)]]
             [:div.col-xs-3 (:kills p)]
             [:div.col-xs-3 (str (.toFixed (js/Number (* 100 (/ (:player/attr.stats.wins pp)
                                                                (:player/attr.stats.played pp))))
                                           2)
                                 "%")]]
            ))
        ])
     ]
    ))

(defn search-field [search-val]
  [:div.search-wrapper
   [:input {:className   "search-player"
            :type        "text"
            :placeholder "Enter player name..."
            :value       (:query @search-val)
            :on-change   #(let [query (-> % .-target .-value)]
                            (reset! search-val (assoc @search-val :query query))
                            (if (and (> (count query) 3))
                              (GET (str config/host "/api/player/search/" query "/")
                                   {:handler (fn [e]
                                               (when (string/starts-with? (:query @search-val) query)
                                                 (reset! search-val (assoc @search-val :result e))))})
                              (reset! search-val (assoc @search-val :result []))))}]
   (when (> (count (:query @search-val)) 0)
     [:span.bounceInDown.animated {:title                   "clear"
                                   :dangerouslySetInnerHTML {:__html "&times;"}
                                   :on-click                #(reset! search-val (assoc @search-val :query ""
                                                                                                   :result []))}])])

(defn search-result [regions search-val]
  (let [items (:result @search-val)]
    (when (> (count items) 0)
      [:div.row.center-xs
       (for [[i r] (map vector (range) items)]
         [:div.col-xs-12.line-search-result.slideInDown.animated {:key (str "s-result-" i)}
          [:a.finded-player {:href (bidi/path-for routes :player :name (-> r :player/attr.name))}
           (-> r :player/attr.name) " - " (->> regions
                                               (filter #(= (:db/id %) (-> r :player/region :db/id)))
                                               (first)
                                               :region/name)]])
       ]))
  )

(def search-val (r/atom {:query  ""
                         :result []}))

(defmethod route :index []
  (let [mmodes @(rf/subscribe [:matches/mode])
        mregions @(rf/subscribe [:matches/region])
        top-players @(rf/subscribe [:player/top])
        hero-stats @(rf/subscribe [:hero/stats])
        load? (and (nil? mmodes) (nil? mregions) (nil? hero-stats))]
    (if load?
      [:img.page-loading {:src "/img/load.gif"}]
      [:div.row.center-xs
       [:div.col-xs-12
        [:h3 "search player"]
        [search-field search-val]

        [search-result mregions search-val]
        ]
       [:div.col-xs-11.col-sm-8.col-md-4.col-lg-3
        [:h3 "total matches stats"]
        [:div.row
         [:div.col-xs-12.line
          [:span.title "Played matches"]
          [:span.value (apply + (map #(:gameMode/count.match %) mmodes))]]
         (for [[i mmode] (map vector (range) (reverse (sort-by :gameMode/count.match
                                                               (filter #(> (:gameMode/count.match %) 0)
                                                                       mmodes))))]
           [:div.col-xs-12.line {:key (str "pm-" i)}
            [:span.title (:gameMode/name mmode)]
            [:span.value (:gameMode/count.match mmode)]])]]
       [:div.col-xs-11.col-sm-8.col-md-4.col-lg-3
        [:h3 "matches played by region"]
        [:div.row
         (for [[i mmode] (map vector (range) (reverse (sort-by :region/count.match mregions)))]
           [:div.col-xs-12.line {:key (str "mp-" i)}
            [:span.title (:region/name mmode)]
            [:span.value (:region/count.match mmode)]])]]
       [:div.col-xs-11.col-sm-8.col-md-3.col-lg-3
        [:h3 "most powerfull players"]
        [:div.row
         (for [[i player] (map vector (range)
                               (reverse (sort-by :player/attr.stats.wins top-players)))]
           [:div.col-xs-12.line {:key (str "mpp-" i)}
            [:a {:href (bidi/path-for routes :player :name (:player/attr.name player))}
             [:span.title (:player/attr.name player)]
             [:span.value (:player/attr.stats.wins player)]]])]]
       [:div.col-xs-12.hero-hr-list
        [:h3 "most played heroes"]
        [:div {:className "row content-scroll-wrap"}
         (for [[i hero] (map vector (range)
                             (reverse (sort-by :hero/count.change hero-stats)))]
           [:div {:key (str "mph-" i)}
            [:a {:href (bidi/path-for routes :hero :name (:hero/name hero))}
             [:img {:src (str "/img/hero/134x134/" (:db/id hero) ".jpg")
                    :alt (:hero/name hero)}]]])]]
       [:div.col-xs-12.hero-hr-list
        [:h3 "highest win rate on this week"]
        [:div {:className "row content-scroll-wrap"}
         (for [[i hero] (map vector (range)
                             (reverse (sort-by :hero/count.winner hero-stats)))]
           [:div {:key (str "hgrw-" i)}
            [:a {:href (bidi/path-for routes :hero :name (:hero/name hero))}
             [:img {:src (str "/img/hero/134x134/" (:db/id hero) ".jpg")
                    :alt (:hero/name hero)}]]])]]])))

(defmethod route :four-o-four []
  "Non-existing routes go here"
  [:span
   [:h1 "404"]
   [:p "What you are looking for, "]
   [:p "I do not have."]])

(defmethod route :default []
  "Configured routes, missing an implementation, go here"
  [:span
   [:h1 "404: My bad"]
   [:pre.verse
    "This page should be here,
but I never created it."]])