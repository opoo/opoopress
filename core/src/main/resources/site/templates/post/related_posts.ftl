<p>
  <h2><@i18n.msg "Related Posts"/></h2>
  <ul id="related-posts-list">
    <#list page.related_posts as post>
      <li class="post">
        <a href="${ root_url }${ post.url }">${titlecase(post.title)}</a>
        <div class="source right"><time datetime="${post.date?string("yyyy-MM-dd'T'HH:mm:ss")}">${post.date?string("yyyy-MM-dd")}</time></div>
      </li>
    </#list>
  </ul>
</p>