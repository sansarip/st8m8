(ns assets.state-machine-samples)

(def^ :st8m8 {'a {:to-b 'b}
              'b {:to-c 'c
                  :to-a 'a}
              'c {}})

;; only necessary spaces
(def^:st8m8{'a{:to-b 'b}
            'b{:to-c 'c
               :to-a 'a}
            'c{}})

;; no new-lines
(def^:st8m8{'a{:to-b 'b}'b{:to-c 'c :to-a 'a}'c{}})

;; no def necessary - smallest unit
^:st8m8{'a{:to-b 'b}'b{:to-c 'c :to-a 'a}'c{}}



