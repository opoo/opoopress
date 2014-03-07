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

<fieldset class="mobile-nav">
  <select onchange="if (this.value) { window.location.href = this.value;}">
    <option value=""><@i18n.msg "Navigate"/>&hellip;</option>
    <#list site.navs?keys as navLabel>
    <#assign navUrl = site.navs[navLabel]>
    <option value="${navUrl}"<#if (root_url + page.url) == navUrl> selected="selected"</#if>>&raquo; ${navLabel}</option>
    </#list>
  </select>
</fieldset>

<ul class="main-navigation">
<#list site.navs?keys as navLabel>
<li><a href="${site.navs[navLabel]}">${navLabel}</a></li>
</#list>
</ul>
