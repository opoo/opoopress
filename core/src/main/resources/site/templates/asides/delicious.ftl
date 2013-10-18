<#if site.delicious_user?? >
<#assign showDeliciousLinks = true>
<section>
  <h1>On Delicious</h1>
  <div id="delicious"></div>
  <p><a href="http://delicious.com/${ site.delicious_user }">My Delicious Bookmarks &raquo;</a></p>
</section>
</#if>