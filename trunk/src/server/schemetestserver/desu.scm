(require scheme/async-channel)
(require scheme/base)
(require srfi/9)

(define output-port-list '())
(define thread-list '())
(define agregation-channel (make-async-channel))
(define name-hash (make-hash))
(define name "trololo1")
;(define port (string->number (vector-ref (current-command-line-arguments) 0)))

(define (make-processing-thread pin pout handler)
  (set! thread-list (cons (thread 
                           (lambda () (peer-processing pin pout handler))) thread-list)))

(define (clear-lists)
  (for-each close-input-port port-list)
  (for-each kill-thread thread-list)
  (set! port-list '())
  (set! thread-list '())
  )
(define (add-port pin pout handler)
  (set! port-list (cons pin port-list))
  (set! thread-list 
        (cons (make-processing-thread pin pout handler) 
              thread-list))
  )

(define (make-peer-processing-thread index pin pout handler send-chennel)
  (define def-handler 
    (lambda (fail)
      (printf "error in read. close port~%")
      (delete-connection index)
   ;   (write '(error read) pout)
   ;   (flush-output pout)
      (close-input-port pin)
      (close-output-port pout)
      ))
  (thread 
   (lambda () 
     (with-handlers ((exn:fail:read? def-handler)
                     (exn:fail:network? def-handler))
       (let loop ()
         (let ((msg (read pin)))
           (if (not (eof-object? msg))
               (begin ;(handler msg pin pout)
                      (handler msg send-chennel pin pout) 
                      (loop))
               (begin 
                 (printf "port closed~%")
                 (async-channel-put send-chennel `(closed ,index))
                 (close-input-port pin)
                 (close-output-port pout))
               ))
         )))))

(define (set-name index name)
  (hash-set! name-hash index name))
(define (print-message index msg)
  (printf "~a>~a~%" (hash-ref name-hash index index) msg))

;(define (make-indexed-handler index)
;  (lambda (msg pin pout)
;    (when (cons? msg) 
;      (case (car msg)
;        ((hi) (printf "~a change name to ~a~%" (hash-ref name-hash index index) (cdr msg))
;              (set-name index (cdr msg)))
;        ((message) (print-message index (cdr msg)))))
;    (cons index msg)))
(define (write-default-error pout)
  (write '(error 0) pout)(newline pout)
  (flush-output pout))

(define (make-indexed-handler index)
  (lambda (msg ch pin pout)
    (with-handlers ((exn:fail:contract? (lambda (fail)
                                          (write-default-error pout))
                                        ))
        (if (member (car msg) '(message move name))
           (async-channel-put ch `(,(car msg) ,index ,@(cdr msg)))
           (write-default-error pout)))))  
  
(define get-index
  (let ((n 0))
    (lambda ()
      (set! n (+ n 1)) n)))

(define (add-peer index pin pout)
  (set! output-port-list (cons pout output-port-list))
  (make-peer-processing-thread index pin pout (make-indexed-handler index) 
                               agregation-channel))

(define (start-listener-on-port port ch)
  (define (add-peer+ index pin pout)
  ;  (set! output-port-list (cons pout output-port-list))
    (make-peer-processing-thread index pin pout (make-indexed-handler index) 
                                 ch))
  (thread
   (lambda ()
     (let ((lst (tcp-listen port)))
       (let loop ()
         (let-values (((in out) (tcp-accept lst)))
           (printf "*new connect~%")
           (let ((index (get-index)))
             (async-channel-put ch (list 'connect index in out))
             (add-peer+ index in out)))
         (loop))))))

(define-record-type :connection
  (new-connection index pin pout point modifer)
  connection?
  (index connection-index)
  (pin   connection-in)
  (pout  connection-out)
  (point connection-point-get)
  (modifer connection-modifer-get connection-modifer-set!))
(define-record-type :poing
  (make-point x y)
  point?
  (x point-x-get point-x-set!)
  (y point-y-get point-y-set!))
(define (point-set! p x y)
  (point-x-set! p x)
  (point-y-set! p y))
(define (empty-modifer tick) #t)
(define desu-speed 0.07)
(define (s2 x) (* x x))
(define (make-move-modifer p start-tick x y)
  (let* ((old-x (point-x-get p))
         (old-y (point-y-get p))
         (t-all (truncate 
                 (/ (sqrt (+ (s2 (- x old-x))
                             (s2 (- y old-y))))
                    desu-speed))))
    (lambda (end-tick)
      (let ((t (- end-tick start-tick)))
        (printf "all:~a\tdtick:~a~%" t-all t)
        (if (> t t-all)
            (point-set! p x y)
            (let ((c (/ t t-all)))
              (point-set! p 
                          (+ old-x (truncate (* c (- x old-x))))
                          (+ old-y (truncate (* c (- y old-y))))))))
      #t)))                          
  
(define connection-records-hash (make-hash))

(define x-len 300)
(define y-len 300)

(define (add-connection index pin pout)
  (let ((x (random x-len))
        (y (random y-len))
        (ctick (get-tick)))
    (write-all-from index `(newplayer ,index ,x ,y))
    (write `(hello ,index ,ctick ,x ,y) pout)(newline pout)
    (hash-for-each connection-records-hash
                   (lambda (it-index conn)
                     (force-modifer ctick conn)
                     (let ((p (connection-point-get conn)))
                       (write (list 'newplayer it-index
                                  (point-x-get p) (point-y-get p)) pout)
                       (newline pout))))
    (flush-output pout)
    (hash-set! connection-records-hash index (new-connection index pin pout 
                                                             (make-point x y) 
                                                             empty-modifer))))

(define (delete-connection index)
  (write-all-from index `(delplayer ,index))
  (hash-remove! connection-records-hash index))

(define (write-all-from index msg)
  (hash-for-each connection-records-hash 
                 (lambda (it-index conn)
                   (with-handlers ((exn:fail:read? (lambda (fail)
                                                       (delete-connection it-index))))
                     (when (not (eq? it-index index))
                       (let ((out (connection-out conn)))
                         (write msg out)(newline out)
                         (flush-output out)))))))

(define (write-all msg)
  (hash-for-each connection-records-hash 
                 (lambda (it-index conn)
                   (let ((out (connection-out conn)))
                     (with-handlers ((exn:fail:read? (lambda (fail)
                                                       (delete-connection it-index))))
                       (write msg out)(newline out)
                       (flush-output out))))))
(define (force-modifer tick conn)
  ((connection-modifer-get conn) tick))
(define (mod-index tick index x y)
  (let* ((conn (hash-ref connection-records-hash index))
         (mod  (connection-modifer-get conn)))
    (mod tick)
    (connection-modifer-set! conn 
                             (make-move-modifer (connection-point-get conn) tick x y))))
    
(define (processing-loop ch)
  (let loop ()
    (let ((it (async-channel-get ch)))
      (case (car it)
        ((connect) (apply add-connection (cdr it)))
        ((move) (let ((ctick (get-tick)))
                  (mod-index ctick (cadr it) (caddr it) (cadddr it))
                  (write-all `(,(car it) ,(cadr it) ,ctick ,@(cddr it)))))
        ((message) (write-all-from (cadr it) `(message ,(cadr it) ,@(cddr it))))
        ((closed) (delete-connection (cadr it)))
        )
      )(loop)))

;(define (start-thread-on-port port)
;  (thread
;   (lambda ()
;     (let ((lst (tcp-listen port)))
;       (let loop ()
;         (let-values (((in out) (tcp-accept lst)))
;           (printf "*new connect~%")
;           (write `(hi . ,name) out)
;           (flush-output out)
;           (add-peer (get-index) in out)
;           )(loop))))))

(define (add-connect host port)
  (printf "add peer ~a:~a~%" host port)
  (with-handlers ((exn:fail:network? (lambda (fail)
                                       (printf "failed connect to ~a:~a~%" host port))))
    (let-values (((in out) (tcp-connect (symbol->string host) port)))
      (add-peer (get-index) in out))))
    ;(call-with-values (tcp-connect (symbol->string host) port) add-peer)))
  
(define (write-all1 msg)
  (for-each (lambda (port)
              (write `(message . ,msg) port)
              (flush-output port))
            output-port-list))

(define (clear-closed)
  (printf "clear closed ~%")
  (set! output-port-list
        (filter (lambda (port) (not (port-closed? port))) output-port-list)))
            
(define (command-loop)
  (let loop ()
    (let ((it (read)))
      (when (string? it)
        (write-all it))
      (when (cons? it)
        (case (car it)
          ((clear) (clear-closed))
          ((peer)  (add-connect (cadr it) (caddr it))))))
    (loop)))

;(start-thread-on-port port)
;(command-loop)

(define (get-tick1)
  (truncate (/ (current-milliseconds) 20)))

(define (get-tick)
  (+ (* (current-seconds) 1000)
     (truncate (modulo (current-milliseconds) 1000))))

(define (read-async-channel-mailbox ch)
  (let loop ((it (async-channel-try-get ch)))
    (when it
      (printf "~a~%" it)
      (loop (async-channel-try-get ch)))))

(define mch (make-async-channel))
(define (rmailbox) (read-async-channel-mailbox mch))
(define (setup-on-port port)
  (start-listener-on-port port mch))

(let ((cla (current-command-line-arguments)))
 (if (= (vector-length cla) 1)
     (begin (setup-on-port (string->number (vector-ref cla 0)))
            (processing-loop mch))
     (printf "usage: mzscheme --script desu.scm portno~%")))
