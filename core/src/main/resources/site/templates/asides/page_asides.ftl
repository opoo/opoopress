<#if (site.page_asides)?? && (site.page_asides?size > 0)>
<#list site.page_asides as f>
	<#include f>
</#list>
<#else>
<#include "default_asides.ftl">
</#if>