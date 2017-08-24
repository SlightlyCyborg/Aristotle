(ns elliot.common-fns)


(defn mapply
  "Takes a function and a map and supplies the map as keyword arguments 
  to the function"
  [f & args]
  (apply f (apply concat (butlast args) (last args))))

