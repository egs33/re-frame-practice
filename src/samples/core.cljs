(ns samples.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))


(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:tasks []
    :visible-state "all"}))

(rf/reg-event-db
 :add-task
 (fn [db [_ content]]
   (assoc db :tasks (conj (:tasks db) {:content content
                                       :is-completed false}))))

(rf/reg-event-db
 :delete-task
 (fn [db [_ task]]
   (assoc db :tasks (remove #(= % task) (:tasks db)))))

(rf/reg-event-db
 :toggle-completed
 (fn [db [_ task]]
   (assoc db :tasks (map #(if (= task %)
                            (assoc % :is-completed (not (:is-completed %)))
                            %) (:tasks db)))))

(rf/reg-event-db
 :set-visible-state
 (fn [db [_ state]]
   (assoc db :visible-state state)))

(rf/reg-event-db
 :clear-completed
 (fn [db]
   (assoc db :tasks (remove :is-completed (:tasks db)))))


(rf/reg-sub
 :visible-tasks
 (fn [{:keys [tasks visible-state]} _]
   (case visible-state
     "all" tasks
     "active" (remove :is-completed tasks)
     "completed" (filter :is-completed tasks)
     [])))

(rf/reg-sub
 :get-visible-state
 (fn [{:keys [visible-state]} _]
   visible-state))


(defn task-component [task]
  [:div
   [:input {:type "checkbox"
            :on-change #(rf/dispatch [:toggle-completed task])
            :checked (:is-completed task)}]
   (:content task)
   [:button {:on-click #(rf/dispatch [:delete-task task])} "delete"]])

(defn task-input-component []
  (let [value (r/atom "")]
    (fn []
      [:div
       [:input {:value @value :on-change #(reset! value (-> % .-target .-value))}]
       [:button {:on-click (fn []
                             (rf/dispatch [:add-task @value])
                             (reset! value ""))}
        "Add"]])))

(defn visible-state-component []
  (let [states ["all" "active" "completed"]
        current-state @(rf/subscribe [:get-visible-state])]
    [:div
     (for [state states]
       [:label {:key state}
        [:input {:type "radio"
                 :name "state-radio"
                 :value state
                 :checked (= current-state state)
                 :on-change #(rf/dispatch [:set-visible-state (-> % .-target .-value)])}]
        state])]))

(defn my-root []
  (let [tasks @(rf/subscribe [:visible-tasks])]
    [:div
     [visible-state-component]
     [task-input-component]
     [:ul
      (for [task tasks]
        [:li {:key (:content task)} [task-component task]])]
     [:button {:on-click #(rf/dispatch [:clear-completed])} "Clear completed"]]))


(rf/dispatch-sync [:initialize])

(r/render [my-root]
          (.getElementById js/document "app"))
