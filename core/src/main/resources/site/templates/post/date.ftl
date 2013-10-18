<#assign _page = page>
<#if post??><#assign _page = post></#if>

<#if _page.date??><#assign date = _page.date></#if>
<#if _page.date_formatted??><#assign date_formatted = _page.date_formatted></#if>
<#if _page.updated??><#assign updated_date = _page.updated></#if>
<#if _page.updated_formatted??><#assign updated_formatted = _page.updated_formatted></#if>

<#if date??>
    <#assign time ="<time datetime=\"" + date?datetime?iso_local + "\" pubdate">
    <#if updated_date??>
        <#assign time = time + " data-updated=\"true\"">
    </#if>
    <#assign time = time + ">" + date_formatted + "</time>">
</#if>

<#if updated_date??>
    <#assign updated ="<time datetime=\"" + updated_date?datetime?iso_local + "\" class=\"updated\">Updated " + updated_formatted + "</time>">
    <#assign was_updated = true>
</#if>
