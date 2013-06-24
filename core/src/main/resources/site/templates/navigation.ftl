<ul class="subscription" data-subscription="rss<#if site.subscribe_email??> email</#if>">
  <li><a href="${ site.subscribe_rss }" rel="subscribe-rss" title="subscribe via RSS">RSS</a></li>
  <#if site.subscribe_email??>
    <li><a href="${ site.subscribe_email }" rel="subscribe-email" title="subscribe via email">Email</a></li>
  </#if>
</ul>
  <#if site.simple_search??>
<form action="${ site.simple_search }" method="get">
  <fieldset role="search">
    <input type="hidden" name="q" value="site:${ site.url}" />
    <input class="search" type="text" name="q" results="0" placeholder="<@i18n.msg 'Search'/>"/>
  </fieldset>
</form>
 </#if>
<#include "custom/navigation.ftl">
