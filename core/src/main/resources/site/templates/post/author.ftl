<#if (post.author)??>
  <#assign author = post.author>
<#elseif (page.author)??>
  <#assign author = page.author>
<#else>
  <#assign author = site.author>
</#if>
<#if author??><span class="byline author vcard"><@i18n.msg "Posted by"/> <span class="fn">${ author }</span></span></#if>