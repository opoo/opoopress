<p>
  <h2><@i18n.msg "Related Posts"/></h2>
  <ul style="padding-left:40px;padding-top:-20px;">
    <#list page.related_posts as post>
      <li class="post">
        <a href="${ root_url }${ post.url }"><#if (site.titlecase)!false = true>${titlecase(post.title)}<#else>${post.title}</#if></a>
      </li>
    </#list>
  </ul>
</p>