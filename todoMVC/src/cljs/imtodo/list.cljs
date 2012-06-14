(ns imtodo.list
  (:use-macros [c2.util :only [p pp bind!]])
  (:use [c2.core :only [unify]])
  (:require [imtodo.core :as core]
            [c2.dom :as dom]
            [c2.event :as event]))


(defn todo [t]
  (let [{:keys [completed? title]} t]
    [:li {:class (when completed? "completed")}
     [:div.view
      [:input.toggle {:type "checkbox"
                      :properties {:checked completed?}}]
      [:label title]
      [:button.destroy]]
     [:input.edit {:value title}]]))

(bind! "#main"
       [:section#main {:style {:display (when-not (seq @core/!todos) "none")}}
        [:input#toggle-all {:type "checkbox"
                            :properties {:checked (every? :completed? @core/!todos)}}]
        [:label {:for "toggle-all"} "Mark all as complete"]
        [:ul#todo-list (unify
                        (case @core/!filter
                          :active    (remove :completed? @core/!todos)
                          :completed (filter :completed? @core/!todos)
                          ;;default to showing all events
                          @core/!todos)
                        todo)]])


(bind! "#footer"
       [:footer#footer {:style {:display (when-not (seq @core/!todos) "none")}}

        (let [items-left (core/todo-count false)]
          [:span#todo-count
           [:strong items-left]
           (str " item" (if (= 1 items-left) "" "s") " left")])

        [:ul#filters
         (unify [:all :active :completed]
                (fn [type]
                  [:li
                   [:a {:class (if (= type @core/!filter) "selected" "")
                        :href (str "#/" (name type))}
                    (core/capitalize (name type))]]))]


        [:button#clear-completed
         {:style {:display (when (zero? (core/todo-count true)) "none")}}
         "Clear completed (" (core/todo-count true) ")"]])

;;;;;;;;;;;;;;;;;;;;;
;;Todo event handlers

(event/on "#todo-list" ".toggle" :click
          (fn [d _ e]
            (let [checked? (.-checked (.-target e))]
              (core/check-todo! d checked?))))

(event/on "#todo-list" ".destroy" :click
          (fn [d] (core/clear-todo! d)))


;;;;;;;;;;;;;;;;;;;;;;;;
;;Control event handlers

(event/on-raw "#toggle-all" :click
              (fn [e]
                (let [checked? (.-checked (.-target e))]
                  (core/check-all! checked?))))

(event/on-raw "#clear-completed" :click
              core/clear-completed!)

(let [$todo-input (dom/select "#new-todo")]
  (event/on-raw $todo-input :keypress
                (fn [e]
                  (when (= :enter (core/evt->key e))
                    (core/add-todo! (dom/val $todo-input))
                    (dom/val $todo-input "")))))
