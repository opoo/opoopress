<#if (page.date)??>
	<#assign date = page.date>
<#elseif (post.date)??>
	<#assign date = post.date>
</#if>

<#if (page.date_formatted)??>
	<#assign date_formatted = page.date_formatted>
<#elseif (post.date_formatted)??>
	<#assign date_formatted = post.date_formatted>
</#if>

<#-- assign has_date = (date)?? -->

<#if (page.updated)??>
	<#assign updated_date = page.updated>
<#elseif (post.updated)??>
	<#assign updated_date = post.updated>
</#if>

<#if (page.updated_formatted)??>
	<#assign updated_formatted = page.updated_formatted>
<#elseif (post.updated_formatted)??>
	<#assign updated_formatted = post.updated_formatted>
</#if>

<#-- assign was_updated = (updated)?? -->

<#if date??>
<#assign time ="<time datetime=\"" + date?string("yyyy-MM-dd'T'HH:mm:ssZ") + "\" pubdate">
	<#if updated??>
		<#assign time = time + " data-updated=\"true\"">
	</#if>
<#assign time = time + ">" + date_formatted + "</time>">
</#if>

<#if updated_date??>
<#assign updated ="<time datetime=\"" + updated_date?string("yyyy-MM-dd'T'HH:mm:ssZ") + "\" class=\"updated\">Updated " + updated_formatted + "</time>">
<#assign was_updated = true>
</#if>
