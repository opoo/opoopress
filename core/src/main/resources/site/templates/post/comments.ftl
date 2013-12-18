<#-- page.comments is not set or true -->
<#if site.disqus_short_name?? && (page.comments)!true == true><#assign showComments = true>
  <section>
    <h1><@i18n.msg "Comments"/></h1>
    <div id="disqus_thread" aria-live="polite"><#include "disqus_thread.ftl"></div>
  </section>
</#if>
<#if site.duoshuo_short_name?? && (page.comments)!true == true><#assign showComments = true>
  <section>
    <h1><@i18n.msg "Comments"/></h1>
    <div id="ds-thread" class="ds-thread" data-thread-key="${root_url}${page.url}"<#if page.title??> data-title="${page.title}"></#if></div>
  </section>
</#if>