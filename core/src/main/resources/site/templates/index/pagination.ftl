<#assign x = paginator.pageNumber>
<#assign n = paginator.totalPages>
<span class="pagebar">
<span class="nolink-page">${x}/${n}</span>
<#if (paginator.previous)??>
<a rel="full-article" href="${root_url}${paginator.previous.url}" title="Previous Page">&lt;</a>
</#if>
<#if (x > 3)><a rel="full-article" href="${root_url}${page.getPage(1).url}">1</a></#if>
<#if (x > 4)><span class="nolink-page">...</span></#if>
<#if (x > 2)><a rel="full-article" href="${root_url}${page.getPage(x-2).url}">${x-2}</a></#if>
<#if (x > 1)><a rel="full-article" href="${root_url}${page.getPage(x-1).url}">${x-1}</a></#if>
<span class="current-page">${x}</span>
<#if (x < n)><a rel="full-article" href="${root_url}${page.getPage(x+1).url}">${x+1}</a></#if>
<#if (x < (n-1))><a rel="full-article" href="${root_url}${page.getPage(x+2).url}">${x+2}</a></#if>
<#if (x < (n-3))><span class="nolink-page">...</span></#if>
<#if (x < (n-2))><a rel="full-article" href="${root_url}${page.getPage(n).url}">${n}</a></#if>
<#if (paginator.next)??>
 <a rel="full-article" href="${root_url}${paginator.next.url}" title="Next Page">&gt;</a>
</#if>
</span>
<a class="next" href="${root_url}/archives">Blog Archives</a>
