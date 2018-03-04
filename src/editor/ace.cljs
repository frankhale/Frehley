; Frank Hale <frankhale@gmail.com>
; http://github.com/frankhale/Frehley
; 4 March 2018

(ns editor.ace
  (:use [editor.util :only [log]]))

(def font-sizes ["12px"
                 "14px"
                 "16px"
                 "18px"
                 "20px"
                 "22px"
                 "24px"])

(defn set-editor-highlighting-mode
  ([session mode]
    ;(log (str "setting language mode to: " mode " for session:" session))
   (set-editor-highlighting-mode session mode nil))
  ([session mode func]
    ;(log (str "setting language mode to: " mode " for session:" session))
   (if mode
     (.setMode session (str "ace/mode/" mode))
     (.setMode session))
   (if func
     (func mode))))

; The new 'kr' theme that was introduced in the 1.8.2014 Ace build has some none standard naming
; when compared to the other themes.
(defn set-editor-theme [editor theme]
  (let [t (if (= theme "kr") "kr_theme" theme)]
  ;(log (str "set-editor-theme: " t))
   (.setTheme editor (str "ace/theme/" t))))

(defn set-editor-font-size [editor size]
  (.setFontSize editor size))

(defn watch-editor-change-event [session func]
  (.on session "change" (fn [e] (func e))))

(defn load-and-enable-editor-snippets [editor config]
  (.loadModule config "ace/ext/language_tools"
    #(.setOptions editor (clj->js {:enableBasicAutocompletion true :enableSnippets true}))))

(defn set-highlighting [session ext func]
  ; This needs to be rewritten in a more functional way, this is out of control!
  (case ext
    ".less" (set-editor-highlighting-mode session "less" func)
    ".css" (set-editor-highlighting-mode session "css" func)
    ".html" (set-editor-highlighting-mode session "html" func)
    ".htm" (set-editor-highlighting-mode session "html" func)
    ".coffee" (set-editor-highlighting-mode session "coffee" func)
    ".clj" (set-editor-highlighting-mode session "clojure" func)
    ".cljs" (set-editor-highlighting-mode session "clojure" func)
    ".js" (set-editor-highlighting-mode session "javascript" func)
    ".java" (set-editor-highlighting-mode session "java" func)
    ".cs" (set-editor-highlighting-mode session "csharp" func)
    ".ps" (set-editor-highlighting-mode session "powershell" func)
    ".rb" (set-editor-highlighting-mode session "ruby" func)
    ".c" (set-editor-highlighting-mode session "c_cpp" func)
    ".cc" (set-editor-highlighting-mode session "c_cpp" func)
    ".cpp" (set-editor-highlighting-mode session "c_cpp" func)
    ".h" (set-editor-highlighting-mode session "c_cpp" func)
    ".hh" (set-editor-highlighting-mode session "c_cpp" func)
    ".txt" (set-editor-highlighting-mode session "text" func)
    (set-editor-highlighting-mode session nil nil)))

(defn clear-editor [editor]
  (.setValue editor ""))

(defn show-invisible-chars [editor result]
  ;(log (str "show-invisible-chars: " result))
  (if result
    (.setShowInvisibles editor true)
    (.setShowInvisibles editor false)))

(defn show-indent-guides [editor result]
  ;(log (str "show-indent-guides: " result))
  (if result
    (.setDisplayIndentGuides editor true)
    (.setDisplayIndentGuides editor false)))

(defn show-gutter [editor result]
  ;(log (str "show-gutter: " result))
  (if result
    (.setShowGutter (.-renderer editor) true)
    (.setShowGutter (.-renderer editor) false)))

(defn set-line-endings-mode [editor mode]
  ;(log (str "setting line endings to: " mode))
  (.setNewLineMode (.getSession editor) mode))

(defn set-line-wrap [editor lines]
  (let [session (.getSession editor)]
    (if (> lines 0)
      (do
        (.setUseWrapMode session true)
        (.setWrapLimitRange session lines lines))
      (do
        (.setUseWrapMode session false)
        (.setWrapLimitRange session nil nil)))))

(defn set-print-margin [editor column]
  (.setPrintMarginColumn editor column))

(defn get-resource-list [fs resource-path prefix]
  (let [files (array-seq (.readdirSync fs resource-path))
        regex (js/RegExp (str "^" prefix "-(.*)\\.js$"))
        resources (map #(second (re-find regex %)) files)]
    (filter (comp not nil?) resources)))
