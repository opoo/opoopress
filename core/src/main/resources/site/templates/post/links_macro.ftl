<#macro category_links categories>
<#assign n = 0>
<#list categories as category><#if n == 1>, </#if><a class="category" href="${root_url}${category.url}">${category.name}</a><#assign n = 1></#list>
</#macro>

<#macro tag_links tags>
<#assign n = 0>
<#list tags as tag><#if n == 1>, </#if><a class="tag" href="${root_url}${tag.url}">${tag.name}</a><#assign n = 1></#list>
</#macro>
