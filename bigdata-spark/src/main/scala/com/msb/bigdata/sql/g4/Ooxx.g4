grammar Ooxx;


oxinit  :  '{' value (',' value)*  '}';

value   :   XX
        |   oxinit;



XX : [0-9]+;
WS  : [ \t\r\n]+  -> skip;





