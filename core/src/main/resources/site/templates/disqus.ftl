<#-- Load script if disquss comments are enabled and `page.comments` is either empty (index) or set to true -->
<#if site.disqus_short_name?? && (page.comments)!true == true>
<script type="text/javascript">
	var disqus_shortname = '${ site.disqus_short_name }';
	<#if (page.comments)?? && page.comments == true>
    <#--`page.comments` can be only be set to true on pages/posts, so we embed the comments here. -->
    // var disqus_developer = 1;
    var disqus_identifier = '${ site.url }${root_url}${ page.url }';
    var disqus_url = '${ site.url }${root_url}${ page.url }';
    <#if page.title??>var disqus_title = '${page.title}';<#else>//no title</#if>
	// var disqus_category_id = '';

	(function() {
	    var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;
	    dsq.src = '//' + disqus_shortname + '.disqus.com/embed.js';
	    (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);
	})();
        
 	<#else>
    <#-- As `page.comments` is empty, we must be on the index page. -->
    (function () {
    	var s = document.createElement('script'); s.async = true;
    	s.type = 'text/javascript';
    	s.src = '//' + disqus_shortname + '.disqus.com/count.js';
    	(document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(s);
    }());
	</#if>
</script>
</#if>