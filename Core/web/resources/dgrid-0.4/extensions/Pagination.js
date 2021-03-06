//>>built
define("dgrid-0.4/extensions/Pagination","../_StoreMixin dojo/_base/declare dojo/_base/array dojo/_base/lang dojo/on dojo/query dojo/string dojo/has dojo/when put-selector/put ../util/misc dojo/i18n!./nls/pagination dojo/_base/sniff xstyle/css!../css/extensions/Pagination.css".split(" "),function(v,w,x,y,p,q,z,D,r,e,A,B){function C(b){b.noDataNode?(e(b.noDataNode,"!"),delete b.noDataNode):b.cleanup();b.contentNode.innerHTML=""}function t(b){if(b.loadingNode)e(b.loadingNode,"!"),delete b.loadingNode;
else if(b._oldPageNodes){for(var a in b._oldPageNodes)b.removeRow(b._oldPageNodes[a]);delete b._oldPageNodes}delete b._isLoading}return w(v,{rowsPerPage:10,pagingTextBox:!1,previousNextArrows:!0,firstLastArrows:!1,pagingLinks:2,pageSizeOptions:null,showLoadingMessage:!0,i18nPagination:B,showFooter:!0,_currentPage:1,buildRendering:function(){this.inherited(arguments);var b=this,a=this.paginationNode=e(this.footerNode,"div.dgrid-pagination"),c=this.paginationStatusNode=e(a,"div.dgrid-status"),g=this.i18nPagination;
c.tabIndex=0;this._updatePaginationSizeSelect();this._updateRowsPerPageOption();this._updatePaginationStatus(this._total);a=this.paginationNavigationNode=e(a,"div.dgrid-navigation");this.firstLastArrows&&(c=this.paginationFirstNode=e(a,"span.dgrid-first.dgrid-page-link","\u00ab"),c.setAttribute("aria-label",g.gotoFirst),c.tabIndex=0);this.previousNextArrows&&(c=this.paginationPreviousNode=e(a,"span.dgrid-previous.dgrid-page-link","\u2039"),c.setAttribute("aria-label",g.gotoPrev),c.tabIndex=0);this.paginationLinksNode=
e(a,"span.dgrid-pagination-links");this.previousNextArrows&&(c=this.paginationNextNode=e(a,"span.dgrid-next.dgrid-page-link","\u203a"),c.setAttribute("aria-label",g.gotoNext),c.tabIndex=0);this.firstLastArrows&&(c=this.paginationLastNode=e(a,"span.dgrid-last.dgrid-page-link","\u00bb"),c.setAttribute("aria-label",g.gotoLast),c.tabIndex=0);this._listeners.push(p(a,".dgrid-page-link:click,.dgrid-page-link:keydown",function(a){if(!("keydown"===a.type&&13!==a.keyCode)){a=this.className;var c,g;b._isLoading||
-1<a.indexOf("dgrid-page-disabled")||(c=b._currentPage,g=Math.ceil(b._total/b.rowsPerPage),this===b.paginationPreviousNode?b.gotoPage(c-1):this===b.paginationNextNode?b.gotoPage(c+1):this===b.paginationFirstNode?b.gotoPage(1):this===b.paginationLastNode?b.gotoPage(g):"dgrid-page-link"===a&&b.gotoPage(+this.innerHTML))}}))},destroy:function(){this.inherited(arguments);this._pagingTextBoxHandle&&this._pagingTextBoxHandle.remove()},_updatePaginationSizeSelect:function(){var b=this.pageSizeOptions,a=
this.paginationSizeSelect,c;if(b&&b.length){a||(a=this.paginationSizeSelect=e(this.paginationNode,"select.dgrid-page-size[aria-label\x3d"+this.i18nPagination.rowsPerPage+"]"),c=this._paginationSizeChangeHandle=p(a,"change",y.hitch(this,function(){this.set("rowsPerPage",+this.paginationSizeSelect.value)})),this._listeners.push(c));for(c=a.options.length=0;c<b.length;c++)e(a,"option",b[c],{value:b[c],selected:this.rowsPerPage===b[c]});this._updateRowsPerPageOption()}else if((!b||!b.length)&&a)e(a,"!"),
this.paginationSizeSelect=null,this._paginationSizeChangeHandle.remove()},_setPageSizeOptions:function(b){this.pageSizeOptions=b&&b.sort(function(a,b){return a-b});this._updatePaginationSizeSelect()},_updateRowsPerPageOption:function(){var b=this.rowsPerPage,a=this.pageSizeOptions,c=this.paginationSizeSelect;c&&(0>x.indexOf(a,b)?this._setPageSizeOptions(a.concat([b])):c.value=""+b)},_setRowsPerPage:function(b){this.rowsPerPage=b;this._updateRowsPerPageOption();this.gotoPage(1)},_updateNavigation:function(){function b(a,
b){var d,f;c.pagingTextBox&&a===m&&1<k?(d=e(l,"input.dgrid-page-input[type\x3dtext][value\x3d$]",m),d.setAttribute("aria-label",g.jumpPage),c._pagingTextBoxHandle=p(d,"change",function(){var a=+this.value;!isNaN(a)&&(0<a&&a<=k)&&c.gotoPage(+this.value)}),h&&"INPUT"===h.tagName&&d.focus()):(f=a===m,d=e(l,"span"+(f?".dgrid-page-disabled":"")+".dgrid-page-link",a+(b?" ":"")),d.setAttribute("aria-label",g.gotoPage),d.tabIndex=f?-1:0,s===a&&(f?a<k?s++:u.focus():d.focus()),f||(u=d))}function a(a,b){e(a,
(b?".":"!")+"dgrid-page-disabled");a.tabIndex=b?-1:0}var c=this,g=this.i18nPagination,l=this.paginationLinksNode,m=this._currentPage,f=this.pagingLinks,d=this.paginationNavigationNode,k=Math.ceil(this._total/this.rowsPerPage),n=this._pagingTextBoxHandle,h=document.activeElement,s,u;!h||!A.contains(this.paginationNavigationNode,h)?h=null:"dgrid-page-link"===h.className&&(s=+h.innerHTML);n&&n.remove();l.innerHTML="";q(".dgrid-first, .dgrid-previous",d).forEach(function(b){a(b,1===m)});q(".dgrid-last, .dgrid-next",
d).forEach(function(b){a(b,m>=k)});if(f&&0<k){b(1,!0);d=m-f;for(2<d?e(l,"span.dgrid-page-skip","..."):d=2;d<Math.min(m+f+1,k);d++)b(d,!0);m+f+1<k&&e(l,"span.dgrid-page-skip","...");1<k&&b(k)}else c.pagingTextBox&&b(m);h&&-1===h.tabIndex&&(f=q('[tabindex\x3d"0"]',this.paginationNavigationNode),h===this.paginationPreviousNode||h===this.paginationFirstNode?h=f[0]:f.length&&(h=f[f.length-1]),h&&h.focus())},_updatePaginationStatus:function(b){var a=this.rowsPerPage,c=Math.min(b,(this._currentPage-1)*a+
1);this.paginationStatusNode.innerHTML=z.substitute(this.i18nPagination.status,{start:c,end:Math.min(b,c+a-1),total:b})},refresh:function(b){var a=this,c=b&&b.keepCurrentPage?Math.min(this._currentPage,Math.ceil(this._total/this.rowsPerPage)):1;this.inherited(arguments);return this.gotoPage(c).then(function(b){setTimeout(function(){p.emit(a.domNode,"dgrid-refresh-complete",{bubbles:!0,cancelable:!1,grid:a})},0);return b})},_onNotification:function(b,a,c){b=this.rowsPerPage;var g=this._currentPage*
b;"add"===a.type&&a.index<g||"delete"===a.type&&a.previousIndex<g||"update"===a.type&&Math.floor(a.index/b)!==Math.floor(a.previousIndex/b)?this.gotoPage(Math.min(this._currentPage,Math.ceil(a.totalLength/this.rowsPerPage))):c===this._renderedCollection&&a.totalLength!==this._total&&(this._updatePaginationStatus(a.totalLength),this._updateNavigation())},renderQueryResults:function(b,a){var c=this,g=this.inherited(arguments);a||(this._topLevelRequest&&(this._topLevelRequest.cancel(),delete this._topLevelRequest),
"function"===typeof g.cancel&&(this._topLevelRequest=g),r(g,function(){c._topLevelRequest&&delete c._topLevelRequest}));return g},insertRow:function(){var b=this._oldPageNodes,a=this.inherited(arguments);b&&a===b[a.id]&&delete b[a.id];return a},gotoPage:function(b){var a=this,c=(this._currentPage-1)*this.rowsPerPage;if(!this._renderedCollection)return r([]);this._renderedCollection.releaseRange&&this._renderedCollection.releaseRange(c,c+this.rowsPerPage);return this._trackError(function(){var c=a.rowsPerPage,
l=(b-1)*c,m={start:l,count:c},f,d=a.contentNode,k,n,h;if(a.showLoadingMessage)C(a),k=a.loadingNode=e(d,"div.dgrid-loading"),k.innerHTML=a.loadingMessage;else{a._oldPageNodes=k={};d=d.children;n=0;for(h=d.length;n<h;n++)k[d[n].id]=d[n]}a._isLoading=!0;f=a._renderedCollection.fetchRange({start:l,end:l+c});return a.renderQueryResults(f,null,m).then(function(d){t(a);a.scrollTo({y:0});a._rows&&(a._rows.min=l,a._rows.max=l+c-1);r(f.totalLength,function(c){c||(a.noDataNode&&(e(a.noDataNode,"!"),delete a.noDataNode),
a.noDataNode=e(a.contentNode,"div.dgrid-no-data"),a.noDataNode.innerHTML=a.noDataMessage);a._total=c;a._currentPage=b;a._rowsOnPage=d.length;a._updatePaginationStatus(c);a._updateNavigation()});return f},function(b){t(a);throw b;})})}})});
//# sourceMappingURL=Pagination.js.map