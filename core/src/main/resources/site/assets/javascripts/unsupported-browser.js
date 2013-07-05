//document.writeln("<div class='unsupported-browser'>Please note that OpooPress does not support Internet Explorer versions 7 or 8.</div>");

var __zh = false;
try{__zh = ($("html").attr("lang").toLowerCase().indexOf("zh_") == 0);}catch(e){}
var __html = "";
if(__zh){
	__html += '<div class="unsupported-browser">';
	__html += '<div class="container clearfix">';
	__html += '  <h5>请注意 OpooPress 将不再支持 Internet Explorer 7 或 8 。</h5>';
	__html += '  <p>推荐您升级到最新的 <a href="https://ie.microsoft.com/">Internet Explorer</a>, <a href="https://chrome.google.com">Google Chrome</a>, 或者 <a href="https://mozilla.org/firefox/">Firefox</a>。</p>';
	__html += '  <p>如果您使用 IE 9 或以上版本，请确保 <a href="http://windows.microsoft.com/zh-CN/windows7/webpages-look-incorrect-in-Internet-Explorer">关闭 "兼容性视图"</a>。</p>';
	__html += '</div>';
	__html += '</div>';
}else{
	__html += '<div class="unsupported-browser">';
	__html += '<div class="container clearfix">';
	__html += '  <h5>Please note that OpooPress no longer supports Internet Explorer versions 7 or 8.</h5>';
	__html += '  <p>We recommend upgrading to the latest <a href="https://ie.microsoft.com/">Internet Explorer</a>, <a href="https://chrome.google.com">Google Chrome</a>, or <a href="https://mozilla.org/firefox/">Firefox</a>.</p>';
	__html += '  <p>If you are using IE 9 or later, make sure you <a href="http://windows.microsoft.com/en-US/windows7/webpages-look-incorrect-in-Internet-Explorer">turn off "Compatibility View"</a>.</p>';
	__html += '</div>';
	__html += '</div>';
}
document.writeln(__html);
