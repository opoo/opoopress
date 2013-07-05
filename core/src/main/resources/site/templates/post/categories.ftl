<#include "links_macro.ftl">

<#if (page.categories)??>
<#assign categories = page.categories>
<#elseif (post.categories)??>
<#assign categories = post.categories>
</#if>
<#if categories?? && (categories?size > 0)>
<span class="categories">Filed under: <@category_links categories/></span>
</#if>

<#if (page.tags)??>
<#assign tags = page.tags>
<#elseif (post.tags)??>
<#assign tags = post.tags>
</#if>

<#if tags?? && (tags?size > 0)>
<span class="categories">Tags: <@tag_links tags/></span>
</#if>