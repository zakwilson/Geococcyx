(ns com.goodanser.geococcyx.common
  (:require [neko.reactive :refer [cell cell=]]
            [neko.resource :as res])
  (:import android.app.Activity))

(defonce activity* (atom nil))
(defonce root-view* (atom nil))
(defonce ui-tree* (atom []))

;; Theme color helper.
;;
;; Reads from @activity* at call time, so colors are always current.
;; Using functions (rather than atoms) means theme changes take effect
;; automatically: when the user toggles dark/light mode, Android recreates
;; the Activity, make-ui resets activity*, and the next rebuild-ui-tree! call
;; gets fresh colors without any explicit invalidation.
(defn tc [kw] (res/get-theme-color @activity* kw))
