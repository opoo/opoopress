<#if (site.post_asides)?? && (site.post_asides?size > 0)>
<#list site.post_asides as f>
	<#include f>
</#list>
<#else>
<#include "default_asides.ftl">
</#if>