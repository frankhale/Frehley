;
; This is a small text editor created for the purposes of learning more about ClojureScript and Node-Webkit
;
; Dependencies:
;
; Ace - http://ace.ajax.org
; ClojureScript - https://github.com/clojure/clojurescript
; Node-Webkit - https://github.com/rogerwang/node-webkit
;
; Frank Hale <frankhale@gmail.com>
; http://github.com/frankhale/editor
; 26 April 2014
;

(ns editor.core
	(:use [editor.util :only [log find-map find-map-without read-file-sync write-file-sync nw-refresh show-nw-dev-tools fs window nw-gui
                            watch-window-close-event create-option fill-select-with-options bind-element-event]])
	(:require [jayq.core :as jq]
			  [editor.ace :as frehley]))

; DOM Selectors
(def $editor (jq/$ :#editor))
(def $file-save-as-dialog (jq/$ :#fileSaveAsDialog))
(def $file-open-dialog (jq/$ :#fileOpenDialog))
(def $control-panel (jq/$ :#controlPanel))
(def $theme-switcher (jq/$ :#themeSwitcher))
(def $font-size-switcher (jq/$ :#fontSizeSwitcher))
(def $buffer-switcher (jq/$ :#bufferSwitcher))
(def $show-invisible-chars (jq/$ :#showInvisibleChars))
(def $show-indent-guides (jq/$ :#showIndentGuides))
(def $line-endings-switcher (jq/$ :#lineEndingsSwitcher))
(def $language-mode-switcher (jq/$ :#languageModeSwitcher))

; Requires and miscellaneous
(def fs-extra (js/require "fs-extra"))
(def path (js/require "path"))
(def editor (.edit js/ace "editor"))

(def config-file-path "resources/config/settings.json")
(def ace-resource-path "resources/scripts/ace")
(def new-buffer-name "New.txt")
(def editor-name "Frehley")

(def editor-state (atom []))
(def current-buffer (atom {}))

(def key-codes {:b 66
                :s 83
                :n 78
                :m 77
                :o 79
				:w 119
                :tab 9
                :f1 112
                :f12 123})

(defn fill-buffer-list-with-names []
	(let [names (map #(:file-name %) @editor-state)]
		;(log (str names))
		(jq/html $buffer-switcher "")
		(fill-select-with-options $buffer-switcher names)))

(defn set-editor-title 
	([]
		(set-editor-title (:file-name @current-buffer)))		
	([title]		
		(set! js/document.title (str editor-name " - [" title "] (" (.getLength (.getSession editor)) ")"))))
	
(defn switch-buffer [buffer]
	(reset! current-buffer buffer)
	;(log (str "session: " (:session buffer) " for: " (:file-name @current-buffer)))
	(.setSession editor (:session buffer))
	(.setUndoManager (:session buffer) (:undo-manager buffer))
	(set-editor-title (:file-name buffer))
	(jq/val $buffer-switcher (:file-name @current-buffer))
	;(log (str "file-path: " (:file-path buffer)))	
	(when-not (empty? (:file-path buffer))		
		(frehley/set-highlighting (:session buffer) (.extname path (:file-path buffer)) #(jq/val $language-mode-switcher %))))
	;(log (str"current buffer: "(:file-name @current-buffer))))

(defn insert-new-buffer 
	([]
		(insert-new-buffer nil))
	([file]
		(let [new-buffer (atom {})]
			(if-not (nil? file)
				(let [text (str (read-file-sync file))]
					(swap! new-buffer conj {:file-name (.-name file)
											:file-path (.-path file)
											:text text
											:session (js/ace.EditSession. text "text")}))
				(swap! new-buffer conj {:file-name (str new-buffer-name " " (alength (clj->js @editor-state)))
										:session (js/ace.EditSession. "" "text")}))
			(swap! new-buffer conj {:undo-manager (js/ace.UndoManager.)})
			;(log (str @new-buffer))
			(log (str "file name: " (:file-name @new-buffer)))
			(swap! editor-state conj @new-buffer)			
			(fill-buffer-list-with-names)
			(frehley/set-highlighting (:session @new-buffer) ".txt" #(jq/val $language-mode-switcher %))
			@new-buffer)))

(defn insert-new-buffer-and-switch []
	(switch-buffer (insert-new-buffer)))
			
(defn show-control-panel []
	"This toggles the editors visibility, the control panel is hidden beneath"
	(if (= "none" (jq/css $editor :display))
		(do
			(jq/css $editor {:display "block"})
			(jq/css $control-panel {:display "none"})
			(.updateFull (.-renderer editor))
			(set-editor-title))
		(do
			(jq/css $editor {:display "none"})
			(jq/css $control-panel {:display "block"})
			(set-editor-title "Control Panel"))))
				
(defn write-config []
	"Writes the editor configuration file to resources/config/settings.json"
	(let [config-file {:theme (jq/val $theme-switcher)
				 :font-size (jq/val $font-size-switcher)
				 :show-invisible-chars (jq/prop $show-invisible-chars "checked")
				 :show-indent-guides (jq/prop $show-indent-guides "checked")
				 :line-endings-mode (jq/val $line-endings-switcher)}
		  json (.stringify js/JSON (clj->js config-file))]
		(.mkdirsSync fs-extra (.dirname path config-file-path))
		(write-file-sync config-file-path json)))
	
(defn set-editor-props-from-config [config]
	(if config
		(do
		  (frehley/set-editor-theme editor (:theme config))
		  (frehley/set-editor-font-size editor (:font-size config))
		  (jq/val $theme-switcher (:theme config))
		  (jq/val $font-size-switcher (:font-size config))
		  (jq/prop $show-invisible-chars {:checked (:show-invisible-chars config)})
		  (frehley/show-invisible-chars editor (:show-invisible-chars config))
		  (jq/prop $show-indent-guides {:checked (:show-indent-guides config)})
		  (frehley/show-indent-guides editor (:show-indent-guides config))
		  (jq/val $line-endings-switcher (:line-endings-mode config)))
		(do
		  (frehley/set-editor-theme editor (jq/val $theme-switcher))
		  (frehley/set-editor-font-size editor (jq/val $font-size-switcher))
		  (write-config))))
	
(defn read-config [func]
	"Reads the editor configuration file from resources/config/settings.json"
	(let [exists (.existsSync fs config-file-path)]
		(if exists
			(.readFile fs config-file-path (fn [error data] (if-not error (func data))))
			(func nil))))

(defn open [files]
	(switch-buffer (first (map #(insert-new-buffer %) files))))
	
(defn open-file-dialog []
	(.trigger $file-open-dialog "click"))

(defn file-open-dialog-change-event [result]
  (let [files (array-seq (.-files result))]
	(open files)))

(defn save []
  (write-file-sync (:file-path @current-buffer) (.getValue editor)))

(defn save-or-save-as-file []
	(if (not (empty? (:file-path @current-buffer)))
		(save)
		(.trigger $file-save-as-dialog "click")))

;
; TODO: Need to update the buffer with the new name
;
		
(defn file-save-as-dialog-change-event [result]
  (let [files (array-seq (.-files result))
        file (first files)]
	(swap! current-buffer conj {:file-path (.-path file) 
								:file-name (.-name file)})
	(log (str "save-as: " @current-buffer))
	(log (str "save-as: " (:file-path @current-buffer)))
	(save)
	(switch-buffer @current-buffer)))

(defn cycle-buffer []
	(let [es-length (alength (to-array @editor-state))
		  old-current-buffer @current-buffer]
		(when (> es-length 1)
			(reset! editor-state (find-map-without @editor-state :file-name (:file-name old-current-buffer)))
			(log (str "es first: " (first @editor-state)))
			(switch-buffer (last @editor-state))
			(swap! editor-state conj old-current-buffer)
			(log (str "es after: " @editor-state)))))

(defn close-buffer []
  (when-not (empty? @editor-state)
    (when (js/confirm "Are you sure you want to close this buffer?")
		(let [new-state (find-map-without @editor-state :file-name (:file-name @current-buffer))]
			(log (str "new-state: " new-state))
			(reset! editor-state new-state)
			(fill-buffer-list-with-names) 
			(if-not (empty? @editor-state)
				(switch-buffer (last @editor-state))
				(switch-buffer (insert-new-buffer)))))))
	
(defn document-onkeydown [e]
	"Handles all of the custom key combos for the editor. All combos start with CTRL and then the key."
	(let [key-bind-with-ctrl (fn [k fun] (if (.-ctrlKey e) (and (= (.-keyCode e) (k key-codes))(fun))))
		key-bind (fn [k fun] (if (= (.-keyCode e) (k key-codes))(fun)))]
		(key-bind-with-ctrl :b nw-refresh)
		(key-bind-with-ctrl :n insert-new-buffer-and-switch)
		(key-bind-with-ctrl :o open-file-dialog)
		(key-bind-with-ctrl :s save-or-save-as-file)
		(key-bind-with-ctrl :m close-buffer)
		(key-bind-with-ctrl :tab cycle-buffer)
		(key-bind-with-ctrl :w write-config)
		(key-bind :f1 show-control-panel)		
		(key-bind :f12 show-nw-dev-tools)	
		e))	
			
(defn buffer-switcher-change-event [file-name]
	(let [buffer (first (find-map @editor-state :file-name file-name))]
		(switch-buffer buffer)))
	
(defn bind-events []
	(bind-element-event $file-open-dialog :change #(file-open-dialog-change-event %))
	(bind-element-event $file-save-as-dialog :change #(file-save-as-dialog-change-event %))
	(bind-element-event $buffer-switcher :change #(buffer-switcher-change-event (.-value %)))
	(bind-element-event $theme-switcher :change #(do (frehley/set-editor-theme editor (.-value %)) (write-config)))
	(bind-element-event $language-mode-switcher :change #(do (frehley/set-editor-highlighting-mode (.getSession editor) (.-value %))))
	(bind-element-event $font-size-switcher :change #(do (frehley/set-editor-font-size editor (.-value %)) (write-config)))
	(bind-element-event $show-invisible-chars :click #(frehley/show-invisible-chars editor (.-checked %)))
	(bind-element-event $show-indent-guides :click #(frehley/show-indent-guides editor (.-checked %)))
	(bind-element-event $line-endings-switcher :change #(frehley/set-line-endings-mode (.getSession editor) (.-value %))))

(defn document-ondrop [e]
	(let [files (array-seq (.-files (.-dataTransfer e)))
		  file (first files)]
		(jq/prevent e)
		(open files)))
	
(defn -init []
	(set! (.-ondragover js/window) (fn [e] (jq/prevent e)))
	(set! (.-ondrop js/window) (fn [e] (jq/prevent e)))
	(set! (.-ondrop js/document) (fn [e] (document-ondrop e)))
	(set! (.-onkeydown js/document) (fn [e] (document-onkeydown e)))
	(frehley/set-editor-theme editor "chaos")
	(frehley/load-and-enable-editor-snippets editor (.-config js/ace))
	(fill-select-with-options $theme-switcher (frehley/get-resource-list fs ace-resource-path "theme"))
	(fill-select-with-options $language-mode-switcher (frehley/get-resource-list fs ace-resource-path "mode"))
	(fill-select-with-options $font-size-switcher frehley/font-sizes)  
	(watch-window-close-event #(write-config))	
	(read-config #(set-editor-props-from-config (js->clj (.parse js/JSON %) :keywordize-keys true)))
	(bind-events)
	(insert-new-buffer-and-switch)
	(frehley/watch-editor-change-event (.getSession editor) (fn [e] 
		(set-editor-title))))
	
(jq/document-ready
 (-init))
