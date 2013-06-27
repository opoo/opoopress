<#-- Load script if disquss comments are enabled and `page.comments` is either empty (index) or set to true -->
<#if site.disqus_short_name?? && (page.comments)!true != false>
<script type="text/javascript">
      var disqus_shortname = '${ site.disqus_short_name }';
      <#if (page.comments)!true == true>
        <#--`page.comments` can be only be set to true on pages/posts, so we embed the comments here. -->
        // var disqus_developer = 1;
        var disqus_identifier = '${ site.url }${root_url}${ page.url }';
        var disqus_url = '${ site.url }${root_url}${ page.url }';
        var disqus_script = 'embed.js';
      <#else>
        <#-- As `page.comments` is empty, we must be on the index page. -->
        var disqus_script = 'count.js';
      </#if>
    (function () {
      var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;
      dsq.src = 'http://' + disqus_shortname + '.disqus.com/' + disqus_script;
      (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);
    }());
</script>
</#if>