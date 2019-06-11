#!/usr/bin/env perl
%m=(qw!GRRR while( ROWH }else{ WOOF + AWOO = BARK - ARF * RUF? if( ARRUF } YIP <
YAP > VUH ){ BOW ){ BORF }!,"\n",';') and undef $/ or $_ = <> and s/(@{[join "|"
,map quotemeta,keys%m]}|[_a-z]\w*)/$m{$1}||"\$m$1"/eg and print eval,"\n"
