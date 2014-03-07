<#assign x = paginator.pageNumber>
<#assign n = paginator.totalPages>
<span class="pagebar">
<span class="nolink-page" title="第 ${x} 页，共 ${n} 页">${x}/${n}</span>
<#if (paginator.previous)??>
<a rel="full-article" href="${root_url}${paginator.previous.url}" title="上一页">&lt;</a>
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
 <a rel="full-article" href="${root_url}${paginator.next.url}" title="下一页">&gt;</a>
</#if>
</span>
<a class="next" href="${root_url}/archives">文章目录</a>