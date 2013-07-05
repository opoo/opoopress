<#include "links_macro.ftl">

<#if (page.categories)??>
<#assign categories = page.categories>
<#elseif (post.categories)??>
<#assign categories = post.categories>
</#if>
<#if categories?? && (categories?size > 0)>
<span class="categories">属于 <@category_links categories/> 分类</span>
</#if>

<#if (page.tags)??>
<#assign tags = page.tags>
<#elseif (post.tags)??>
<#assign tags = post.tags>
</#if>

<#if tags?? && (tags?size > 0)>
<span class="categories">被贴了 <@tag_links tags/> 标签</span>
</#if>