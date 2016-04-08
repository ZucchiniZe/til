(ns til.analytics)

(defn- remove-key [map key]
  (apply dissoc map [key]))

(defn track [{:keys [event] :as properties}]
  (js/analytics.track event #js (remove-key properties :event)))

(defn identify [{:keys [userid] :as traits}]
  (js/analytics.identify userid #js (remove-key traits :userid)))

(defn page [name]
  (js/analytics.page name))

(defn alias [userid]
  (js/analytics.alias userid))
