<#if (site.tags?size > 0)>
<section>
  <h1><@i18n.msg "Tags"/></h1>
  <ul id="tags">
<#list site.tags?sort_by("postSize")?reverse as tag>
	<li class="tag"><span><a href="${root_url}${tag.url}">${tag.name}</a></span><span class="count">${tag.postSize}</span></li>
</#list>
  </ul>
</section>
</#if>