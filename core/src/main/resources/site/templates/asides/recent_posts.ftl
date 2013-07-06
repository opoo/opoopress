<section>
  <h1><@i18n.msg "Recent Posts"/></h1>
  <ul id="recent_posts">
  
    <#if (site.posts?size <= site.recent_posts)>
	  <#assign recent_posts = site.posts >
	<#else>
	  <#assign recent_posts = site.posts[0..(site.recent_posts-1)]>
    </#if>
  
    <#list recent_posts as post>
      <li class="post">
        <a href="${ root_url }${ post.url }">${titlecase(post.title)}</a>
      </li>
    </#list>
  </ul>
</section>
