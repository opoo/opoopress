<#if (site.blog_index_asides)?? && (site.blog_index_asides?size > 0)>
<#list site.blog_index_asides as f>
	<#include f>
</#list>
<#else>
<#include "default_asides.ftl">
</#if>