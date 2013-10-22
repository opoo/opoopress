<#if site.github_user??>
<#assign showGitHubRepos = true>
<section>
  <h1>GitHub Repos</h1>
  <ul id="gh_repos">
    <li class="loading">Status updating...</li>
  </ul>
  <#if site.github_show_profile_link>
  <a href="https://github.com/${site.github_user}">@${site.github_user}</a> on GitHub
  </#if>
</section>
</#if>
