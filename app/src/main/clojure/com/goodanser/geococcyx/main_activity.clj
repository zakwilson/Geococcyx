(ns com.goodanser.geococcyx.main-activity
  "Neko UI DSL demo app with DrawerLayout navigation.

  This namespace is automatically loaded by ClojureActivity when
  com.example.clojuredroid.MainActivity is created.

  The app demonstrates neko features across multiple demo sections,
  accessible via a navigation drawer.

  To modify the UI live from the REPL:

    (require '[com.example.clojuredroid.main-activity :as ui])

    ;; Rebuild with current theme colors and reload:
    (ui/rebuild-ui-tree!)

    ;; Or set an arbitrary tree and reload:
    (reset! ui/ui-tree* [...])
    (ui/reload-ui!)"
  (:require [neko.ui :as ui]
            [neko.ui.support.material]
            [neko.ui.support.window-insets :as wini]
            [neko.resource :as res]
            [com.goodanser.geococcyx.repl :as repl]
            [com.goodanser.geococcyx.app :as app])
  (:use com.goodanser.geococcyx.common)
  (:import android.app.Activity
           android.view.View
           [com.google.android.material.tabs TabLayout TabLayout$Tab]
           [android.graphics Typeface]
           [android.widget EditText ScrollView TextView]
           com.goodanser.clj_android.runtime.ClojureActivity))

;; Guard: when rebuild-ui-tree! does its internal reset!, we don't want the
;; :reload watch to also fire — that would trigger a redundant second reload.
;; External resets (e.g. from the REPL) still trigger reload normally.
(defonce ^:private building? (volatile! false))

(declare reload-ui!)

(defn rebuild-ui-tree!
  "Rebuilds ui-tree* from the current theme and section UIs, then
  hot-reloads the live view. Call from the REPL after redefining this
  function to pick up structural or theme changes:
    (rebuild-ui-tree!)"
  []
  (vreset! building? true)
  (reset! ui-tree*
          [:linear-layout {:orientation :vertical
                           :layout-width :fill
                           :layout-height :fill
                           :insets-padding :top}
           [:tab-layout {:id ::tabs
                         :tab-mode :scrollable
                         :tab-gravity :fill
                         :layout-width :fill
                         :tab-content ["Runner" :repl
                                       "App" ::app/app]}]
           (repl/section-ui @activity* :repl)
           (app/section-ui @activity* ::app/app)])
  (vreset! building? false)
  (reload-ui!))

(defn make-ui
  "Renders @ui-tree* into a live view. Called by ClojureActivity.reloadUi."
  [^Activity activity]
  (reset! activity* activity)
  (let [root (ui/make-ui activity @ui-tree*)]
    (reset! root-view* root)
    (repl/init! activity root)
    root))

(defn on-create
  "Called automatically by ClojureActivity when the activity is created."
  [^Activity activity saved-instance-state]
  (wini/enable-edge-to-edge! activity)
  (reset! activity* activity)
  (rebuild-ui-tree!))

(defn reload-ui!
  "Hot-reload the UI from the REPL."
  []
  (when-let [activity (ClojureActivity/getInstance
                        "com.example.clojuredroid.main-activity")]
    (.reloadUi ^ClojureActivity activity)))

;; REPL hot-reload: (reset! ui-tree* [...]) triggers a live reload.
;; Internal rebuilds via rebuild-ui-tree! set building? and are ignored.
(add-watch ui-tree* :reload
           (fn [_ _ _ _]
             (when-not @building?
               (reload-ui!))))
