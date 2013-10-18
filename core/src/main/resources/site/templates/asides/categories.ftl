<#if site.categories?? &&  (site.categories?size > 0)>
<#macro subcategory parent level>
<#if (parent.children?size > 0)>
    <ul class="children${level}">
<#list parent.children as child>
	<#if (child.posts?size > 0)>
	<li class="category"><a href="${root_url}${child.url}">${child.name}</a><span class="count right">${child.posts?size}</span>
	<@subcategory parent=child level=level+1/></li>
	</#if>
</#list>
    </ul>
</#if>
</#macro>

<section>
  <h1><@i18n.msg "Categories"/></h1>
  <ul id="categories">
<#list site.categories as category>
	<#if !(category.parent)?? && (category.posts?size > 0)>
	<li class="category"><a href="${root_url}${category.url}">${category.name}</a><span class="count right">${category.posts?size}</span>
	<@subcategory parent=category level=1/></li>
	</#if>
</#list>
  </ul>
</section>
</#if>
