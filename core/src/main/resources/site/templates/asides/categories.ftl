<#if site.categories?? &&  (site.categories?size > 0)>
<#macro subcategory parent level>
<#if (parent.children?size > 0)>
    <ul class="children${level}">
<#list parent.children as child>
	<li class="category"><a href="${root_url}${child.url}">${child.name}</a> (${child.posts?size})</li>
	<@subcategory parent=child level=level+1/>
</#list>
    </ul>
</#if>
</#macro>

<section>
  <h1><@i18n.msg "Categories"/></h1>
  <ul id="categories">
<#list site.categories as category>
	<#if !(category.parent)??>
	<li class="category"><a href="${root_url}${category.url}">${category.name}</a> (${category.posts?size})</li>
	<@subcategory parent=category level=1/>
	</#if>
</#list>
  </ul>
</section>
</#if>
