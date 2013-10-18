<#if site.disqus_short_name??>
<section>
  <h2><@i18n.msg "Recent Comments"/></h2>
  <#-- access via https cause mixed content warning -->
  <#--
  <script type="text/javascript" src="//${site.disqus_short_name}.disqus.com/recent_comments_widget.js?num_items=5&hide_avatars=1&avatar_size=32&excerpt_length=100"></script>
  -->
 <script language="JavaScript">
  <!--
	var is_https = ('https:' == document.location.protocol);
	var rcw_script_src = (is_https ? 'https:' : 'http:') + '//${site.disqus_short_name}.disqus.com/recent_comments_widget.js?num_items=5&excerpt_length=100&hide_avatars=' + (is_https ? '1' : '0&avatar_size=32');
	var rcw_script = '<scr' + 'ipt type="text/javascript" src="' + rcw_script_src + '"></scr' + 'ipt>';
	document.writeln(rcw_script);
  //-->
  </script>
</section>
</#if>