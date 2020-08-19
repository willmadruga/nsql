#!/usr/bin/env bb
;;-*- mode: clojure -*-

;; Netsuite is dropping Basic Authentication in 2021.1
;; TODO: rewrite using TBA, OAUTH0
;;       lib: https://github.com/mattrepl/clj-oauth
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Utils ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro with-param
  "Calls a list of functions passing given parameter"
  [p fn-list]
  `(for [fn ~fn-list] (fn ~p)))

(defn exec-path
  "Returns current executing path"
  []
  (str/join "/" (pop (str/split *file* #"/"))))

(defn print-rows
  "Prints resultset rows line by line."
  [resultset]
  (doseq [row (json/decode resultset )]
    (prn (json/encode row))))

(defn format-with-jq
  "Format output using jq binary"
  [content]
  ;; (shell/sh content) ;; how to pipe to jq?
  ;; on terminal this is the best:
  ;; ./nsql "select ..." | jq -r | jq
  ;; jq -r for raw then just jq...
  content
  )

(def missing-input "Missing SuiteQL query")

;; Config ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn load-config
  "Load configuration"
  []
  (edn/read-string (slurp (io/reader (apply str [(exec-path) "/config.edn"] )))))

;; Netsuite ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn build-ns-url
  "Builds account-specific Netsuite URL for RESTlet execution"
  [conf]
  (apply str ["https://"
              (clojure.string/lower-case (clojure.string/replace (conf :account) "_" "-"))
              ".restlets.api.netsuite.com/app/site/hosting/restlet.nl?"
              "script=" (conf :script-id) "&"
              "deploy=" (conf :deploy-id) ]))

(defn build-ns-header
  "Builds Netsuite Authorization Header"
  [conf]
  {"Content-Type" "application/json"
   "Authorization"
   (apply str ["NLAuth nlauth_account=" (conf :account)
               ", nlauth_email=" (conf :email)
               ", nlauth_signature=" (conf :passwd)
               ", nlauth_role=" (conf :role)]) })

(defn build-ns-payload
  "Builds expected RESTlet payload"
  [query]
  (json/generate-string { :query query }))

;; Main ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(if (some? *command-line-args*)
  (format-with-jq
   (print-rows
    (let [req-options (with-param (load-config) [build-ns-url build-ns-header])]
      ((curl/post
        (first req-options) ;; url
        {
         :headers (second req-options)
         :body (first (with-param (first *command-line-args*) [build-ns-payload]))
         })
       :body)))) ;; response body
  (println missing-input))
