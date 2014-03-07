<#if (page.no_header)!false == false>
  <header>
	<#if index??>
      <h1 class="entry-title"><a href="${ root_url }${ post.url }">${titlecase(post.title)}</a></h1>
	<#else>
      <h1 class="entry-title">${titlecase(page.title)}</h1>
	</#if>

	<#if (page.meta)!true == true>
      <p class="meta">
		<#include "date.ftl">${time}
		<#if site.disqus_short_name?? && (page.comments)!true == true && (post.comments)!true == true && (site.disqus_show_comment_count)!false == true>
         | <a href="<#if index??>${root_url}${post.url}</#if>#disqus_thread"><@i18n.msg "Comments"/></a>
        </#if><#if site.duoshuo_short_name?? && (page.comments)!true == true && (post.comments)!true == true && (site.duoshuo_show_comment_count)!false == true>
         | <#if index??><a href="${root_url}${post.url}#ds-thread"><span class="ds-thread-count" data-thread-key="${root_url}${post.url}"><@i18n.msg "Comments"/></span></a><#else><a href="#ds-thread"><@i18n.msg "Comments"/></a></#if>
        </#if>
        <#-- permalink -->
        <#-- &bull; <a rel="bookmark" href="${root_url}<#if index??>${post.url}<#else>${page.url}</#if>">${ site.permalink_label }</a> -->
      </p>
    </#if>
  </header>
</#if>

<#if index??>
  <div class="entry-content">${post.excerpt}</div>
  <#if post.excerpted == true>
    <footer>
      <a rel="full-article" href="${ root_url }${ post.url }">${site.excerpt_link}</a>
    </footer>
  </#if>
<#else>
<#-- <div class="entry-content">${content}</div> -->
</#if>
