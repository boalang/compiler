;;; Directory Local Variables
;;; For more information see (info "(emacs) Directory Variables")

((java-mode . ((tab-width . 8)
               (c-basic-offset . 8)
               (indent-tabs-mode . t)
               (ws-trim-mode . t)
               (eval . (progn
                         (c-set-offset 'arglist-cont-nonempty '++)
                         (c-set-offset 'case-label '+)))))
 (protobuf-mode . ((tab-width . 8)
                   (c-basic-offset . 8)
                   (indent-tabs-mode . t)
                   (ws-trim-mode . t))))
