<#macro pageLayout><#include "_default.ftl"><@defaultLayout>
<div>
<article role="article">
  <#if (page.title)??>
  <header>
    <h1 class="entry-title">${titlecase(page.title)}</h1>
    <#if (page.date)??><p class="meta"><#include "post/date.ftl">${ time }</p></#if>
  </header>
  </#if>
  <#nested>
  <#if (page.footer)!true == true>
    <footer>
      <#if (page.date)?? || (page.author)?? ><p class="meta">
        <#if (page.author)?? ><#include "post/author.ftl"></#if>
        <#include "post/date.ftl"><#if was_updated??>${updated}<#else>${time}</#if>
        <#if (page.categories)?? ><#include "post/categories.ftl"> </#if>
      </p></#if>
      <#if (page.sharing)!true == true>
        <#include "post/sharing.ftl">
	  </#if>
    </footer>
 </#if>
</article>
<#include "post/comments.ftl">
</div>
<#if !(page.sidebar)?? || ((page.sidebar)?string != "false")>
<aside class="sidebar">
	<#include "asides/page_asides.ftl">
</aside>
</#if>
</@defaultLayout>
</#macro>