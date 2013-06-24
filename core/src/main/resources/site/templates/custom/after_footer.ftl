<#--
  Add content to be output at the bottom of each page. (You might use this for analytics scripts, for example)
-->
<#if (highlighter?? && highlighter == "SyntaxHighlighter")>
<!-- START: Syntax Highlighter ComPress -->
<script type="text/javascript">
    SyntaxHighlighter.autoloader(
        'applescript			${root_url}/plugins/syntax-highlighter/scripts/shBrushAppleScript.js',
        'actionscript3 as3		${root_url}/plugins/syntax-highlighter/scripts/shBrushAS3.js',
        'bash shell				${root_url}/plugins/syntax-highlighter/scripts/shBrushBash.js',
        'coldfusion cf			${root_url}/plugins/syntax-highlighter/scripts/shBrushColdFusion.js',
        'cpp c					${root_url}/plugins/syntax-highlighter/scripts/shBrushCpp.js',
        'c# c-sharp csharp		${root_url}/plugins/syntax-highlighter/scripts/shBrushCSharp.js',
        'css					${root_url}/plugins/syntax-highlighter/scripts/shBrushCss.js',
        'delphi pascal pas		${root_url}/plugins/syntax-highlighter/scripts/shBrushDelphi.js',
        'diff patch			    ${root_url}/plugins/syntax-highlighter/scripts/shBrushDiff.js',
        'erl erlang				${root_url}/plugins/syntax-highlighter/scripts/shBrushErlang.js',
        'groovy					${root_url}/plugins/syntax-highlighter/scripts/shBrushGroovy.js',
        'java					${root_url}/plugins/syntax-highlighter/scripts/shBrushJava.js',
        'jfx javafx				${root_url}/plugins/syntax-highlighter/scripts/shBrushJavaFX.js',
        'js jscript javascript	${root_url}/plugins/syntax-highlighter/scripts/shBrushJScript.js',
        'perl pl				${root_url}/plugins/syntax-highlighter/scripts/shBrushPerl.js',
        'php					${root_url}/plugins/syntax-highlighter/scripts/shBrushPhp.js',
        'text plain				${root_url}/plugins/syntax-highlighter/scripts/shBrushPlain.js',
        'powershell ps          ${root_url}/plugins/syntax-highlighter/scripts/shBrushPowerShell.js',
        'py python				${root_url}/plugins/syntax-highlighter/scripts/shBrushPython.js',
        'ruby rails ror rb		${root_url}/plugins/syntax-highlighter/scripts/shBrushRuby.js',
        'sass scss              ${root_url}/plugins/syntax-highlighter/scripts/shBrushSass.js',
        'scala					${root_url}/plugins/syntax-highlighter/scripts/shBrushScala.js',
        'sql					${root_url}/plugins/syntax-highlighter/scripts/shBrushSql.js',
        'vb vbnet				${root_url}/plugins/syntax-highlighter/scripts/shBrushVb.js',
        'xml xhtml xslt html	${root_url}/plugins/syntax-highlighter/scripts/shBrushXml.js'
    );
    SyntaxHighlighter.defaults['auto-links'] = false;                 
    SyntaxHighlighter.defaults['toolbar'] = false;     
    SyntaxHighlighter.defaults['tab-size'] = 4;
    SyntaxHighlighter.all();
</script>
<!-- END: Syntax Highlighter ComPress -->
</#if>
