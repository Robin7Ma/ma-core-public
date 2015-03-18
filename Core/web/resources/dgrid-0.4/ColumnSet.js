//>>built
define("dgrid-0.4/ColumnSet","dojo/_base/declare dojo/_base/lang dojo/on dojo/aspect dojo/query dojo/has ./util/misc put-selector/put dojo/_base/sniff xstyle/css!./css/columnset.css".split(" "),function(z,A,l,s,n,g,t,p){function B(a,b){var c=a._columnSetScrollLefts;n(".dgrid-column-set",b).forEach(function(a){a.scrollLeft=c[a.getAttribute(h)]})}function r(a,b){1!==a.nodeType&&(a=a.parentNode);for(;a&&!n.matches(a,".dgrid-column-set["+h+"]",b);)a=a.parentNode;return a}function q(a){var b=g("pointer");
return b?(a=C[a]||a,"MS"===b.slice(0,2)?"MSPointer"+a.slice(0,1).toUpperCase()+a.slice(1):"pointer"+a):"touch"+a}function y(a,b,c){b=b.getAttribute(h);a=a._columnSetScrollers[b];c=a.scrollLeft+c;a.scrollLeft=0>c?0:c}g.add("event-mousewheel",function(a,b,c){return"undefined"!==typeof c.onmousewheel});g.add("event-wheel",function(a){var b=!1;try{a.WheelEvent("wheel"),b=!0}catch(c){}return b});var h="data-dgrid-column-set-id",C={start:"down",end:"up"},D=g("touch")&&function(a){return function(b,c){var d=
[l(b,q("start"),function(c){if(!a._currentlyTouchedColumnSet){var d=r(c.target,b);if(d&&(!c.pointerType||"touch"===c.pointerType||2===c.pointerType))a._currentlyTouchedColumnSet=d,a._lastColumnSetTouchX=c.clientX,a._lastColumnSetTouchY=c.clientY}}),l(b,q("move"),function(b){if(null!==a._currentlyTouchedColumnSet){var d=r(b.target);d&&(c.call(null,a,d,a._lastColumnSetTouchX-b.clientX),a._lastColumnSetTouchX=b.clientX,a._lastColumnSetTouchY=b.clientY)}}),l(b,q("end"),function(){a._currentlyTouchedColumnSet=
null})];return{remove:function(){for(var a=d.length;a--;)d[a].remove()}}}},E=g("event-mousewheel")||g("event-wheel")?function(a){return function(b,c){return l(b,g("event-wheel")?"wheel":"mousewheel",function(d){var e=r(d.target,b);e&&(d=d.deltaX||-d.wheelDeltaX/3)&&c.call(null,a,e,d)})}}:function(a){return function(b,c){return l(b,".dgrid-column-set["+h+"]:MozMousePixelScroll",function(b){1===b.axis&&c.call(null,a,this,b.detail)})}};return z(null,{postCreate:function(){var a=this;this.inherited(arguments);
this.on(E(this),y);if(g("touch"))this.on(D(this),y);this.on(".dgrid-column-set:dgrid-cellfocusin",function(b){a._onColumnSetCellFocus(b,this)})},columnSets:[],createRowCells:function(a,b,c,d){for(var e=p("table.dgrid-row-table"),g=p(e,"tbody tr"),f=0,k=this.columnSets.length;f<k;f++){var l=p(g,a+".dgrid-column-set-cell.dgrid-column-set-"+f+" div.dgrid-column-set["+h+"\x3d"+f+"]"),m;m=c||this.subRows;if(!m||!m.length)m=void 0;else{for(var n=[],r=f+"-",q=0,s=m.length;q<s;q++){var u=m[q],v=[];v.className=
u.className;for(var w=0,t=u.length;w<t;w++){var x=u[w];null!=x.id&&0===x.id.indexOf(r)&&v.push(x)}n.push(v)}m=n}l.appendChild(this.inherited(arguments,[a,b,m||this.columnSets[f],d]))}return e},renderArray:function(){for(var a=this.inherited(arguments),b=0;b<a.length;b++)B(this,a[b]);return a},renderHeader:function(){function a(){d._positionScrollers()}this.inherited(arguments);var b=this.columnSets,c=this._columnSetScrollers,d=this,e;this._columnSetScrollerContents={};this._columnSetScrollLefts={};
if(c)for(e in c)p(c[e],"!");else s.after(this,"resize",a,!0),s.after(this,"styleColumn",a,!0),this._columnSetScrollerNode=p(this.footerNode,"+div.dgrid-column-set-scroller-container");c=this._columnSetScrollers={};e=0;for(c=b.length;e<c;e++)this._putScroller(b[e],e);this._positionScrollers()},styleColumnSet:function(a,b){var c=this.addCssRule("#"+t.escapeCssIdentifier(this.domNode.id)+" .dgrid-column-set-"+t.escapeCssIdentifier(a,"-"),b);this._positionScrollers();return c},_destroyColumns:function(){var a=
this.columnSets.length,b,c,d,e,g,f,k,h;for(b=0;b<a;b++){f=this.columnSets[b];c=0;for(e=f.length;c<e;c++){k=f[c];d=0;for(g=k.length;d<g;d++)h=k[d],"function"===typeof h.destroy&&h.destroy()}}this.inherited(arguments)},configStructure:function(){this.columns={};this.subRows=[];for(var a=0,b=this.columnSets.length;a<b;a++)for(var c=this.columnSets[a],d=0;d<c.length;d++)c[d]=this._configColumns(a+"-"+d+"-",c[d]);this.inherited(arguments)},_positionScrollers:function(){var a=this.domNode,b=this._columnSetScrollers,
c=this._columnSetScrollerContents,d=this.columnSets,e=0,l=0,f,k;f=0;for(d=d.length;f<d;f++)k=n(".dgrid-column-set["+h+'\x3d"'+f+'"]',a)[0],e=k.offsetWidth,k=k.firstChild.offsetWidth,c[f].style.width=k+"px",b[f].style.width=e+"px",9>g("ie")&&(b[f].style.overflowX=k>e?"scroll":"auto"),k>e&&l++;this._columnSetScrollerNode.style.bottom=this.showFooter?this.footerNode.offsetHeight+"px":"0";this.bodyNode.style.bottom=l?g("dom-scrollbar-height")+(g("ie")?1:0)+"px":"0"},_putScroller:function(a,b){var c=this._columnSetScrollers[b]=
p(this._columnSetScrollerNode,"span.dgrid-column-set-scroller.dgrid-column-set-scroller-"+b+"["+h+"\x3d"+b+"]");this._columnSetScrollerContents[b]=p(c,"div.dgrid-column-set-scroller-content");l(c,"scroll",A.hitch(this,"_onColumnSetScroll"))},_onColumnSetScroll:function(a){var b=a.target.scrollLeft;a=a.target.getAttribute(h);var c;this._columnSetScrollLefts[a]!==b&&(n(".dgrid-column-set["+h+'\x3d"'+a+'"], .dgrid-column-set-scroller['+h+'\x3d"'+a+'"]',this.domNode).forEach(function(a,e){a.scrollLeft=
b;e||(c=a.scrollLeft)}),this._columnSetScrollLefts[a]=c)},_setColumnSets:function(a){this._destroyColumns();this.columnSets=a;this._updateColumns()},_onColumnSetCellFocus:function(a,b){var c=a.target,d=b.getAttribute(h),d=this._columnSetScrollers[d];if(c.offsetLeft-d.scrollLeft+c.offsetWidth>b.offsetWidth||d.scrollLeft>c.offsetLeft)c=c.offsetLeft,d=b.getAttribute(h),this._columnSetScrollers[d].scrollLeft=0>c?0:c}})});
//# sourceMappingURL=ColumnSet.js.map