;
; This is a small text editor created for the purposes of learning 
; more about ClojureScript and Node-Webkit.
;
; Dependencies:
;
; Ace - http://ace.ajax.org
; ClojureScript - https://github.com/clojure/clojurescript
; Node-Webkit - https://github.com/rogerwang/node-webkit
;
; Frank Hale <frankhale@gmail.com>
; http://github.com/frankhale/editor
; 11 May 2014
;

(ns editor.core
	(:require [jayq.core :as jq]
			  [editor.ace :as frehley]
			  [editor.util :as util]))

; DOM Selectors
(def $editor (jq/$ :#editor))
(def $file-save-as-dialog (jq/$ :#fileSaveAsDialog))
(def $file-open-dialog (jq/$ :#fileOpenDialog))
(def $control-panel (jq/$ :#controlPanel))
(def $about (jq/$ :#about))
(def $theme-switcher (jq/$ :#themeSwitcher))
(def $font-size-switcher (jq/$ :#fontSizeSwitcher))
(def $buffer-switcher (jq/$ :#bufferSwitcher))
(def $show-invisible-chars (jq/$ :#showInvisibleChars))
(def $show-indent-guides (jq/$ :#showIndentGuides))
(def $show-gutter (jq/$ :#showGutter))
(def $line-wrap (jq/$ :#lineWrap))
(def $print-margin (jq/$ :#printMargin))
(def $line-endings-switcher (jq/$ :#lineEndingsSwitcher))
(def $language-mode-switcher (jq/$ :#languageModeSwitcher))
(def $notification (jq/$ :#notification))
(def $help (jq/$ :#help))

(def pages [$editor $control-panel $about $help])

; Requires and miscellaneous
(def fs-extra (js/require "fs-extra"))
(def path (js/require "path"))
(def editor (.edit js/ace "editor"))
(def markdown (js/require "marked"))

(def config-file-path "resources/config/settings.json")
(def ace-resource-path "resources/scripts/ace")
(def new-buffer-name "New.txt")
(def editor-name "Frehley")

(def welcome-title (str "Welcome to " editor-name))
(def warning-close-buffer "Warning: Are you sure you want to close this buffer?")
(def warning-close-all-buffers "Warning: Are you sure you want to close all buffers?")

(def notification-fade-out-speed 1250)

(def ace-themes (frehley/get-resource-list util/fs ace-resource-path "theme"))

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
				:f2 113
				:f3 114
				:f10 121
				:f11 122
                :f12 123})

(defn fill-buffer-list-with-names []
	(let [names (map #(:file-name %) (sort-by :file-name @editor-state))]
		;(util/log (str names))
		(jq/html $buffer-switcher "")
		(util/fill-select-with-options $buffer-switcher names)))

(defn set-editor-title 
	([]
		(set-editor-title (:file-name @current-buffer)))		
	([title]
		(if (not= title (:file-name @current-buffer))
			(set! js/document.title (str editor-name " - [" title "]"))
			(set! js/document.title (str editor-name " - [" title "] (Lines: " (.getLength (.getSession editor)) ")")))))
	
(defn switch-buffer [buffer]
	;(util/log (str "switching-buffer: " buffer))
	(reset! current-buffer buffer)
	;(util/log (str "session: " (:session buffer) " for: " (:file-name @current-buffer)))
	(.setSession editor (:session buffer))
	(.setUndoManager (:session buffer) (:undo-manager buffer))
	(set-editor-title (:file-name buffer))
	(jq/val $buffer-switcher (:file-name @current-buffer))
	;(util/log (str "file-path: " (:file-path buffer)))	
	(when-not (empty? (:file-path buffer))		
		(frehley/set-highlighting (:session buffer) (.extname path (:file-path buffer)) #(jq/val $language-mode-switcher %))))
	;(util/log (str"current buffer: "(:file-name @current-buffer))))

(defn insert-new-buffer 
	([]
		(insert-new-buffer nil))
	([file]
		(let [new-buffer (atom {})]
			(if-not (nil? file)
				(let [text (str (util/read-file-sync file))]
					(swap! new-buffer conj {:file-name (.-name file)
											:file-path (.-path file)
											:text text
											:session (js/ace.EditSession. text "text")}))
				(swap! new-buffer conj {:file-name (str new-buffer-name " " (alength (clj->js @editor-state)))
										:session (js/ace.EditSession. "" "text")}))
			(swap! new-buffer conj {:undo-manager (js/ace.UndoManager.)})
			;(util/log (str @new-buffer))
			;(util/log (str "file name: " (:file-name @new-buffer)))
			(swap! editor-state conj @new-buffer)			
			(fill-buffer-list-with-names)
			(frehley/set-highlighting (:session @new-buffer) ".txt" #(jq/val $language-mode-switcher %))
			(frehley/watch-editor-change-event (:session @new-buffer) 
				(fn [e]
					(do
						(swap! current-buffer conj {:text (.getValue editor)})						
						;(util/log (str (alength (to-array (:text @current-buffer)))))
						(if (> (alength (to-array (:text @current-buffer))) 0)
							(set-editor-title (str (:file-name @current-buffer) "*"))
							(set-editor-title)))))
			@new-buffer)))

(defn insert-new-buffer-and-switch []
	(switch-buffer (insert-new-buffer)))

(defn rerender-editor []
	(.updateFull (.-renderer editor)))
	
(defn toggle-page [elem & {:keys [func] :or {func nil}}]
	(doall (map #(jq/fade-out % "fast") pages))
	(if (.is elem ":visible")
		(do
			(jq/fade-in $editor "fast")
			(rerender-editor)
			(set-editor-title))
		(do			
			(jq/fade-in elem "fast")
			(when func
				(func)))))
				
(defn write-config []
	"Writes the editor configuration file to resources/config/settings.json"
	(let [config-file {:theme (jq/val $theme-switcher)
				 :font-size (jq/val $font-size-switcher)
				 :show-invisible-chars (jq/prop $show-invisible-chars "checked")
				 :show-indent-guides (jq/prop $show-indent-guides "checked")
				 :show-gutter (jq/prop $show-gutter "checked")
				 :line-wrap (jq/prop $line-wrap "checked")
				 :print-margin (jq/prop $print-margin "checked")
				 :line-endings-mode (jq/val $line-endings-switcher)}
		  json (.stringify js/JSON (clj->js config-file))]
		(.mkdirsSync fs-extra (.dirname path config-file-path))
		(util/write-file-sync config-file-path json)))
	
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
		  (jq/prop $show-gutter {:checked (:show-gutter config)})
		  (frehley/show-gutter editor (:show-gutter config))
		  (jq/val $line-endings-switcher (:line-endings-mode config))
		  (if (= (:line-wrap config) true)
			(frehley/set-line-wrap editor 80)
			(frehley/set-line-wrap editor 0))		  
		  (jq/prop $line-wrap {:checked (:line-wrap config)})
		  (if (= (:print-margin config) true)
			(frehley/set-print-margin editor 80)
			(frehley/set-print-margin editor -1))
		  (util/log (str "print-margin: " (:print-margin config)))
		  (jq/prop $print-margin {:checked (:print-margin config)})
		  (frehley/set-line-endings-mode editor (:line-endings-mode config)))	  
		(do
		  (frehley/set-editor-theme editor (jq/val $theme-switcher))
		  (frehley/set-editor-font-size editor (jq/val $font-size-switcher))
		  (write-config))))
	
(defn read-config [func]
	"Reads the editor configuration file from resources/config/settings.json"
	(let [exists (.existsSync util/fs config-file-path)]
		(if exists
			(.readFile util/fs config-file-path (fn [error data] (if-not error (func data))))
			(func nil))))

(defn open [files]
	(switch-buffer (first (doall (map #(insert-new-buffer %) files)))))
	
(defn open-file-dialog []
	(jq/trigger $file-open-dialog "click"))

(defn file-open-dialog-change-event [result]
	(let [files (array-seq (.-files result))]
		(open files)))

(defn save []
	(util/write-file-sync (:file-path @current-buffer) (:text @current-buffer))
	(set-editor-title))

(defn save-or-save-as-file []
	(if (not (empty? (:file-path @current-buffer)))
		(save)
		(jq/trigger $file-save-as-dialog "click")))
	
(defn file-save-as-dialog-change-event [result]
	(let [files (array-seq (.-files result))
		  file (first files)]
		(swap! current-buffer conj {:file-path (.-path file) 
									:file-name (.-name file)})
		;(util/log (str "save-as: " @current-buffer))
		;(util/log (str "save-as: " (:file-path @current-buffer)))
		(save)
		(switch-buffer @current-buffer)))

(defn cycle-buffer []
	(when (> (alength (to-array @editor-state)) 1)
		(let [curr-index (first (util/indices #(= @current-buffer %) @editor-state))
			  first-part (take curr-index @editor-state)
			  last-part (util/nthrest @editor-state curr-index)
			  new-buffer-order (flatten (merge first-part last-part))]			  
			  (switch-buffer (second new-buffer-order)))))

(defn cycle-editor-themes []
	(when (> (alength (to-array ace-themes)) 1)		
		(let [curr-theme (jq/val $theme-switcher)
			  curr-index (first (util/indices #(= curr-theme %) ace-themes))
			  first-part (take curr-index ace-themes)
			  last-part (util/nthrest ace-themes curr-index)
			  new-theme-order (flatten (merge first-part last-part))
			  next-theme (second new-theme-order)]
			;(util/log (str "next-theme: " next-theme))				
			(jq/val $theme-switcher next-theme)
			(.trigger $theme-switcher "change"))))
			  
(defn close-buffer []
	(when-not (empty? @editor-state)
		(when (js/confirm warning-close-buffer)
			(let [new-state (util/find-map-without @editor-state :file-name (:file-name @current-buffer))]
				;(util/log (str "new-state: " new-state))
				(reset! editor-state new-state)
				(fill-buffer-list-with-names) 
				(if-not (empty? @editor-state)
					(switch-buffer (last @editor-state))
					(switch-buffer (insert-new-buffer)))))))

(defn close-all-buffers []
	(when (js/confirm warning-close-all-buffers)
		(reset! editor-state [])
		(fill-buffer-list-with-names) 
		(switch-buffer (insert-new-buffer))))

(defn editor-state-without-new-empty-files []
	; Need to filter the editor-state such that all files starting with 
	; new-buffer-name and having a no text are eliminated and the new state is returned
	(let [new-state (filter #(if-not (and (util/starts-with (:file-name %) new-buffer-name) (empty? (:text %))) %) @editor-state)]
	;(util/log (str new-state))
	new-state))

(defn document-onkeydown [e]
	"Handles all of the custom key combos for the editor. All combos start with CTRL and then the key."
	;(util/log (str "Keycode: " (.-keyCode e)))
	(let [key-bind-with-ctrl (fn [k fun] (when (and (.-ctrlKey e) (not (.-altKey e)) (= (.-keyCode e) (k key-codes)) (do (fun) (jq/prevent e)))))
		  key-bind-with-ctrl-alt (fn [k fun] (when (and (.-ctrlKey e) (.-altKey e) (= (.-keyCode e) (k key-codes)) (do (fun) (jq/prevent e)))))
		  key-bind-with-alt (fn [k fun] (when (and (.-altKey e) (= (.-keyCode e) (k key-codes)) (do (fun) (jq/prevent e)))))
		  key-bind (fn [k fun] (when (and (not (.-altKey e)) (not (.-ctrlKey e)) (= (.-keyCode e) (k key-codes))) (do (fun) (jq/prevent e))))]
		(key-bind-with-ctrl-alt :b util/nw-refresh)
		(key-bind-with-ctrl :n insert-new-buffer-and-switch)
		(key-bind-with-ctrl :o open-file-dialog)
		(key-bind-with-ctrl :s save-or-save-as-file)	
		(key-bind-with-ctrl :m close-buffer)
		(key-bind-with-ctrl-alt :m close-all-buffers)
		(key-bind-with-ctrl :tab cycle-buffer)
		(key-bind-with-ctrl :w write-config)
		(key-bind :f2 #(toggle-page $control-panel :func (fn [] (set-editor-title "Control Panel"))))		
		(key-bind :f3 cycle-editor-themes)
		(key-bind :f10 #(toggle-page $help :func (fn [] (set-editor-title "Help"))))
		(key-bind :f11 #(toggle-page $about :func (fn [] (set-editor-title "About"))))
		(key-bind :f12 util/show-nw-dev-tools)	
		e))
			
(defn buffer-switcher-change-event [file-name]
	(let [buffer (first (util/find-map @editor-state :file-name file-name))]
		(switch-buffer buffer)))

(defn display-notification [msg]
	(jq/html $notification msg)	
	(jq/fade-in $notification "slow" 
		(fn [] (.setTimeout js/window
			#(jq/fade-out $notification "slow") notification-fade-out-speed))))
	
(defn bind-events []
	(util/bind-element-event $file-open-dialog :change #(file-open-dialog-change-event %))
	(util/bind-element-event $file-save-as-dialog :change #(file-save-as-dialog-change-event %))
	(util/bind-element-event $buffer-switcher :change #(do (buffer-switcher-change-event (.-value %)) (toggle-page $control-panel :func (fn [] (set-editor-title "Control Panel")))))
	(util/bind-element-event $theme-switcher :change #(do (frehley/set-editor-theme editor (.-value %)) (display-notification (str "Theme: " (.-value %))) (write-config)))
	(util/bind-element-event $language-mode-switcher :change #(do (frehley/set-editor-highlighting-mode (.getSession editor) (.-value %))))
	(util/bind-element-event $font-size-switcher :change #(do (frehley/set-editor-font-size editor (.-value %)) (write-config)))
	(util/bind-element-event $show-invisible-chars :click #(frehley/show-invisible-chars editor (.-checked %)))
	(util/bind-element-event $show-indent-guides :click #(frehley/show-indent-guides editor (.-checked %)))
	(util/bind-element-event $show-gutter :click #(frehley/show-gutter editor (.-checked %)))
	(util/bind-element-event $line-wrap :click #(frehley/set-line-wrap editor 80))
	(util/bind-element-event $print-margin :click #((if (.-checked %) 
		(frehley/set-print-margin editor 80) (frehley/set-print-margin editor -1))))
	(util/bind-element-event $line-endings-switcher :change #(frehley/set-line-endings-mode editor (.-value %))))

(defn document-ondrop [e]
	(let [files (array-seq (.-files (.-dataTransfer e)))]
		(jq/prevent e)
		(reset! editor-state (editor-state-without-new-empty-files))
		(open files)))
	
(defn -init []
	;
	; The following hackery is to get around the key stealing by Ace. I wanted to use
	; the F2 key and apparently Ace is stilling the events on that key. This clears it up!
	;
	; Got the idea from here: http://japhr.blogspot.com/2013/03/ace-events-removing-and-handling.html
	;
	(set! (.-origOnCommandKey (.-keyBinding editor)) (.-onCommandKey (.-keyBinding editor)))
	(set! (.-onCommandKey (.-keyBinding editor)) (fn [e h k] 
															(do
																(let [key-code (.-keyCode e)]
																	; it is what it is, don't laugh!
																	(when (= key-code 113)
																		(document-onkeydown e))
																	(this-as x (.origOnCommandKey x e h k))))))
	(set! (.-ondragover js/window) (fn [e] (jq/prevent e)))
	(set! (.-ondrop js/window) (fn [e] (jq/prevent e)))
	(set! (.-ondrop js/document) (fn [e] (document-ondrop e)))
	(set! (.-onkeydown js/document) (fn [e] (document-onkeydown e)))
	;(util/log (markdown (util/open-about-page)))
	(jq/html $about (markdown (util/open-about-page)))	
	(frehley/show-gutter editor false)
	(frehley/set-editor-theme editor "chaos")
	(frehley/load-and-enable-editor-snippets editor (.-config js/ace))
	(util/fill-select-with-options $theme-switcher ace-themes)
	(util/fill-select-with-options $language-mode-switcher (frehley/get-resource-list util/fs ace-resource-path "mode"))
	(util/fill-select-with-options $font-size-switcher frehley/font-sizes)  
	(util/watch-window-close-event #(write-config))	
	(read-config #(set-editor-props-from-config (js->clj (.parse js/JSON %) :keywordize-keys true)))
	(bind-events)
	(insert-new-buffer-and-switch))
	;(show-start-page))
	
(jq/document-ready
 (-init))
