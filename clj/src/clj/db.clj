(ns clj.db
  (:use (clojure.java [jdbc :as jdbc]))
  (:import com.mchange.v2.c3p0.ComboPooledDataSource))

;;; Using Sqlite
(def db-spec {:classname "org.sqlite.JDBC"
              :subprotocol "sqlite"
              :subname "test.db"})      ; => #'clj.db/db-spec
;; create Table and CRUD
(jdbc/db-do-commands
 db-spec
 (jdbc/create-table-ddl :authors
                        [:id "integer primary key"]
                        [:first_name "varchar"]
                        [:last_name "varchar"])) ; => (0)

(jdbc/insert! db-spec :authors
              {:first_name "Martin" :last_name "Liu"}
              {:first_name "Daisy" :last_name "Chu"}) ; => ({:last_insert_rowid() 1} {:last_insert_rowid() 2})

(jdbc/query db-spec ["SELECT * FROM authors"]) ; => ({:last_name "Liu", :first_name "Martin", :id 1} {:last_name "Chu", :first_name "Daisy", :id 2})

;; transaction
(jdbc/delete! db-spec :authors
              ["id = ?" 2])             ; => (1)

;;; Pools
(defn pooled-spec
  [{:keys [classname subprotocol subname username password] :as other-spec}]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass classname)
               (.setJdbcUrl (str "jdbc:" subprotocol ":" subname))
               (.setUser username)
               (.setPassword password))]
    {:datasource cpds}))

(def pooled-db (pooled-spec db-spec))   ; => #'clj.db/pooled-db
(jdbc/query pooled-db
               ["SELECT * FROM authors"]) ; => ({:last_name "Liu", :first_name "Martin", :id 1})

(jdbc/execute! pooled-db
               ["drop table authors"])  ; => [0]
