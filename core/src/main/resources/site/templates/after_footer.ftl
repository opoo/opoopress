<#--
<script type="text/javascript" src="${root_url}/javascripts/libs/modernizr.min.js"></script>
<script type="text/javascript" src="${root_url}/javascripts/libs/jquery.min.js"></script>
<script type="text/javascript" src="${root_url}/javascripts/libs/swfobject-modified.min.js"></script>
<script type="text/javascript" src="${root_url}/javascripts/libs/opoopress.js"></script>
-->
<script type="text/javascript" src="${root_url}/javascripts/opoopress.min.js"></script>
<script language="JavaScript">
<!--
    window.OpooPress = new OpooPressApp({siteUrl:'${site.url}',rootUrl:'${root_url}',pageUrl:'${page.url}',<#if page.title??>title:'${page.title}',</#if><#if showGitHubRepos??>github:{target:'#gh_repos',user:'${site.github_user}',count:${site.github_repo_count},skip_forks:${site.github_skip_forks?string}},</#if><#if showDeliciousLinks??>delicious:{user:'${site.delicious_user}',count:'${site.delicious_count}'},</#if>refreshRelativeTimes:true,verbose:true},{});
    OpooPress.init();

<#if site.disqus_short_name?? && (page.comments)!true == true && (site.disqus_show_comment_count)!false == true>
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
//-->
</script>
<#include "facebook_like.ftl">
<#include "google_plus_one.ftl">
<#include "twitter_sharing.ftl">
<#include "google_analytics.ftl">
<#include "custom/after_footer.ftl">