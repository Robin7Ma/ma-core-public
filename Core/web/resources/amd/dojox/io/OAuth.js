//>>built
define("dojo/_base/kernel dojo/_base/lang dojo/_base/array dojo/_base/xhr dojo/dom dojox/encoding/digests/SHA1".split(" "),function(k,t,u,n,v,p){k.getObject("io.OAuth",!0,dojox);dojox.io.OAuth=new function(){function q(b,c){for(var a=c.consumer.key,d="",e=l.length,f=0;16>f;f++)d+=l.charAt(Math.floor(Math.random()*e));a={oauth_consumer_key:a,oauth_nonce:d,oauth_signature_method:c.sig_method||"HMAC-SHA1",oauth_timestamp:Math.floor((new Date).valueOf()/1E3)-2,oauth_version:"1.0"};c.token&&(a.oauth_token=
c.token.key);b.content=k.mixin(b.content||{},a)}function r(b){var c=[{}],a;if(b.form){b.content||(b.content={});a=k.byId(b.form);var d=a.getAttributeNode("action");b.url=b.url||(d?d.value:null);a=k.formToObject(a);delete b.form}a&&c.push(a);b.content&&c.push(b.content);var d="source protocol authority userInfo user password host port relative path directory file query anchor".split(" "),e=/^(?:([^:\/?#]+):)?(?:\/\/((?:(([^:@]*):?([^:@]*))?@)?([^:\/?#]*)(?::(\d*))?))?((((?:[^?#\/]*\/)*)([^?#]*))(?:\?([^#]*))?(?:#(.*))?)/.exec(b.url);
a={};for(var f=d.length;f--;)a[d[f]]=e[f]||"";d=a.protocol.toLowerCase();e=a.authority.toLowerCase();if("http"==d&&80==a.port||"https"==d&&443==a.port)-1<e.lastIndexOf(":")&&(e=e.substring(0,e.lastIndexOf(":")));a.url=d+"://"+e+(a.path||"/");if(a.query){var d=k.queryToObject(a.query),h;for(h in d)d[h]=encodeURIComponent(d[h]);c.push(d)}b._url=a.url;a=[];d=0;for(e=c.length;d<e;d++)for(h in f=c[d],f)if(k.isArray(f[h]))for(var g=0,l=f.length;g<l;g++)a.push([h,f[g]]);else a.push([h,f[h]]);b._parameters=
a;return b}function s(b,c,a){q(c,a);r(c);a=c._parameters;a.sort(function(a,b){return a[0]>b[0]?1:a[0]<b[0]?-1:a[1]>b[1]?1:a[1]<b[1]?-1:0});a=k.map(a,function(a){return g(a[0])+"\x3d"+g((""+a[1]).length?a[1]:"")}).join("\x26");return b.toUpperCase()+"\x26"+g(c._url)+"\x26"+g(a)}function m(b,c,a){var d=g(a.consumer.secret)+"\x26"+(a.token&&a.token.secret?g(a.token.secret):"");b=s(b,c,a);if((a=a.sig_method||"HMAC-SHA1")&&"PLAINTEXT"!=a&&"HMAC-SHA1"!=a)throw Error("dojox.io.OAuth: the only supported signature encodings are PLAINTEXT and HMAC-SHA1.");
d="PLAINTEXT"==a?d:p._hmac(b,d);c.content.oauth_signature=d;return c}var g=this.encode=function(b){return!(""+b).length?"":encodeURIComponent(b).replace(/\!/g,"%21").replace(/\*/g,"%2A").replace(/\'/g,"%27").replace(/\(/g,"%28").replace(/\)/g,"%29")};this.decode=function(b){var c=[];b=b.split("\x26");for(var a=0,d=b.length;a<d;a++)if(""!=b[a])if(-1<b[a].indexOf("\x3d")){var e=b[a].split("\x3d");c.push([decodeURIComponent(e[0]),decodeURIComponent(e[1])])}else c.push([decodeURIComponent(b[a]),null]);
return c};var l="0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";this.sign=function(b,c,a){return m(b,c,a)};this.xhr=function(b,c,a,d){m(b,c,a);return n(b,c,d)};this.xhrGet=function(b,c){return this.xhr("GET",b,c)};this.xhrPost=this.xhrRawPost=function(b,c){return this.xhr("POST",b,c,!0)};this.xhrPut=this.xhrRawPut=function(b,c){return this.xhr("PUT",b,c,!0)};this.xhrDelete=function(b,c){return this.xhr("DELETE",b,c)}};return dojox.io.OAuth});
//# sourceMappingURL=OAuth.js.map