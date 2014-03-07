<#--
<script type="text/javascript" src="${root_url}/javascripts/libs/modernizr.min.js"></script>
<script type="text/javascript" src="${root_url}/javascripts/libs/jquery.min.js"></script>
<script type="text/javascript" src="${root_url}/javascripts/libs/swfobject-modified.min.js"></script>
<script type="text/javascript" src="${root_url}/javascripts/libs/opoopress.js"></script>
-->
<script type="text/javascript" src="${root_url}/javascripts/opoopress.min.js"></script>
<script language="JavaScript">
<!--
    window.OpooPress = new OpooPressApp({siteUrl:'${site.url}',rootUrl:'${root_url}',pageUrl:'${page.url}',<#if page.title??>title:'${page.title}',</#if><#if showGitHubRepos??>github:{target:'#gh_repos',user:'${site.github_user}',count:${site.github_repo_count},skip_forks:${site.github_skip_forks?string}},</#if><#if showDeliciousLinks??>delicious:{user:'${site.delicious_user}',count:'${site.delicious_count}'},</#if>refreshRelativeTimes:<#if site.refreshRelativeTimes??>${site.refreshRelativeTimes?string}<#else>false</#if>,verbose:<#if site.verbose??>${site.verbose?string}<#else>false</#if>},{});
    OpooPress.init();

<#if site.disqus_short_name?? && (page.comments)!true == true>
<#assign fullUrl = site.url + root_url + page.url>
    var disqus_shortname = '${ site.disqus_short_name }';
    <#if index??><#if (site.disqus_show_comment_count)!false == true>OpooPress.showDisqusCommentCount();</#if><#else>
    // var disqus_developer = 1;
    var disqus_identifier = '${fullUrl}';
    var disqus_url = '${fullUrl}';
    <#if page.title??>var disqus_title = '${page.title}';</#if>
    //var disqus_category_id = '';
    OpooPress.showDisqusWidgets();</#if>
</#if>

<#if site.duoshuo_short_name?? && (page.comments)!true == true>
var duoshuoQuery = {short_name:"${site.duoshuo_short_name}"};
(function() {
    var ds = document.createElement('script');
    ds.type = 'text/javascript';ds.async = true;
    ds.src = 'http://static.duoshuo.com/embed.js';
    ds.charset = 'UTF-8';
    (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(ds);
})();
</#if>
//-->
</script>

<#if (highlighter?? && highlighter == "SyntaxHighlighter")>
<!-- START: Syntax Highlighter ComPress -->
<script type="text/javascript" src="${root_url}/plugins/syntax-highlighter/scripts/shCore.js"></script>
<script type="text/javascript" src="${root_url}/plugins/syntax-highlighter/scripts/shAutoloader.js"></script>
<script type="text/javascript">
    SyntaxHighlighter.autoloader(
        'applescript			${root_url}/plugins/syntax-highlighter/scripts/shBrushAppleScript.js',
        'actionscript3 as3		${root_url}/plugins/syntax-highlighter/scripts/shBrushAS3.js',
        'bash shell				${root_url}/plugins/syntax-highlighter/scripts/shBrushBash.js',
        'coldfusion cf			${root_url}/plugins/syntax-highlighter/scripts/shBrushColdFusion.js',
        'cpp c					${root_url}/plugins/syntax-highlighter/scripts/shBrushCpp.js',
        'c# c-sharp csharp		${root_url}/plugins/syntax-highlighter/scripts/shBrushCSharp.js',
        'css					${root_url}/plugins/syntax-highlighter/scripts/shBrushCss.js',
        'delphi pascal pas		${root_url}/plugins/syntax-highlighter/scripts/shBrushDelphi.js',
        'diff patch			    ${root_url}/plugins/syntax-highlighter/scripts/shBrushDiff.js',
        'erl erlang				${root_url}/plugins/syntax-highlighter/scripts/shBrushErlang.js',
        'groovy					${root_url}/plugins/syntax-highlighter/scripts/shBrushGroovy.js',
        'java					${root_url}/plugins/syntax-highlighter/scripts/shBrushJava.js',
        'jfx javafx				${root_url}/plugins/syntax-highlighter/scripts/shBrushJavaFX.js',
        'js jscript javascript	${root_url}/plugins/syntax-highlighter/scripts/shBrushJScript.js',
        'perl pl				${root_url}/plugins/syntax-highlighter/scripts/shBrushPerl.js',
        'php					${root_url}/plugins/syntax-highlighter/scripts/shBrushPhp.js',
        'text plain				${root_url}/plugins/syntax-highlighter/scripts/shBrushPlain.js',
        'powershell ps          ${root_url}/plugins/syntax-highlighter/scripts/shBrushPowerShell.js',
        'py python				${root_url}/plugins/syntax-highlighter/scripts/shBrushPython.js',
        'ruby rails ror rb		${root_url}/plugins/syntax-highlighter/scripts/shBrushRuby.js',
        'sass scss              ${root_url}/plugins/syntax-highlighter/scripts/shBrushSass.js',
        'scala					${root_url}/plugins/syntax-highlighter/scripts/shBrushScala.js',
        'sql					${root_url}/plugins/syntax-highlighter/scripts/shBrushSql.js',
        'vb vbnet				${root_url}/plugins/syntax-highlighter/scripts/shBrushVb.js',
        'xml xhtml xslt html	${root_url}/plugins/syntax-highlighter/scripts/shBrushXml.js'
    );
    SyntaxHighlighter.defaults['auto-links'] = false;                 
    SyntaxHighlighter.defaults['toolbar'] = false;     
    SyntaxHighlighter.defaults['tab-size'] = 4;
    SyntaxHighlighter.all();
</script>
<!-- END: Syntax Highlighter ComPress -->
</#if>

<#include "after_footer/facebook_like.ftl">
<#include "after_footer/google_plus_one.ftl">
<#include "after_footer/twitter_sharing.ftl">
<#include "after_footer/google_analytics.ftl">
