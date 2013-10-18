<fieldset class="mobile-nav">
  <select onchange="if (this.value) { window.location.href = this.value;}">
    <option value=""><@i18n.msg "Navigate"/>&hellip;</option>
    <#list site.navs?keys as navLabel>
    <#assign navUrl = site.navs[navLabel]>
    <option value="${navUrl}"<#if (root_url + page.url) == navUrl> selected="selected"</#if>>&raquo; ${navLabel}</option>
    </#list>
  </select>
</fieldset>

<ul class="main-navigation">
<#list site.navs?keys as navLabel>
<li><a href="${site.navs[navLabel]}">${navLabel}</a></li>
</#list>
</ul>
