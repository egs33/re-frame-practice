(ns samples.core
  (:require [reagent.core :as r]))

(defn task-component [task delete-task toggle-completed]
  (let [is-completed (r/atom (:is-completed task))]
    (fn []
      [:li
       [:input {:type "checkbox"
                :on-change #(toggle-completed task)
                :checked @is-completed}]
       (:content task)
       [:button {:on-click #(delete-task task)} "delete"]])))

(defn task-input-component [add-task]
  (let [value (r/atom "")]
    (fn []
      [:div
       [:input {:value @value :on-change #(reset! value (-> % .-target .-value))}]
       [:button {:on-click (fn []
                             (add-task @value)
                             (reset! value ""))} "追加"]])))


(defn visible-tasks [tasks visible-state]
  (case visible-state
    "all" tasks
    "active" (remove :is-completed tasks)
    "completed" (filter :is-completed tasks)
    '[]))

(defn delete-task [target tasks]
  (remove #(= % target) tasks))

(defn toggle-completed [target tasks]
  (map (fn [t]
         (if (= target t)
           {:content (:content t)
            :is-completed (not (:is-completed t))}
           t)) tasks))

(defn my-root []
  (let [tasks (r/atom '[])
        visible-state (r/atom "all")]
    (fn []
      [:div
       [task-input-component #(swap! tasks (fn [orig] (conj orig {:content %
                                                                  :is-completed false})))]
       [:ul
        (for [task (visible-tasks @tasks @visible-state)]
          ^{:key {:content task}} [task-component
                                   task
                                   (fn [target] (swap! tasks (partial delete-task target)))
                                   (fn [target] (swap! tasks (partial toggle-completed target)))])]])))

(r/render [my-root]
          (.getElementById js/document "app"))
