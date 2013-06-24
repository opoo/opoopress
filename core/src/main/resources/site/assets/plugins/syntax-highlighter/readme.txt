=== Syntax Highlighter Compress ===
Contributors: Andre Gärtner
Donate link: http://www.phodana.de/wordpress/wp-plugin-syntax-highlighter-compress/
Tags: syntax, highlight, highlighter, syntaxhighlighter, code, source, sourcecode, keyword, plugin, wordpress, css, php, html
Requires at least: 2.6
Tested up to: 3.2.1
Stable tag: 3.0.83.3

Syntax Highlighter ComPress is a plugin for code syntax highlighting. It loads fast on the website and code can pasted easily into Wordpress.

== Description ==

Syntax Highlighter ComPress is a simple Wordpress plugin, that is based on the latest <a href="http://alexgorbatchev.com/wiki/SyntaxHighlighter">Alex Gorbatchev's SyntaxHighlighter Script</a>. Code and source text of different mark-up or programming languages can be highlighted in Wordpress.

There are other WordPress plugin based on the SyntaxHighlighter Script but these cause long page loading times and they are difficult to handle. The advantage of Syntax Highlighter ComPress is that only necessary brush files will be loaded dynamically. Another advantage is that your code can easily pasted into your posts, no need to replace all '`<`' with '`&lt;`'.

Supported mark-up or programming are: AppleScript, ActionScript3, Bash/shell, Coldfusion, C#, C++, CSS, Delphi, Diff, Erlang, Groovy, JavaScript, Java, JavaFX, Perl, PHP, Plain Text, Python, Ruby, Scala, SQL, Visual Basic and XML.

The plugin is localized in English, German and Romanian.

== Installation ==

This plugin requires WordPress 2.6 or later.

1. Unzip and upload the files to your 'wp-content/plugins/' directory.
2. Activate the plugin by logging into your WordPress administration panel, going to 'Plugins', then clicking the  ‘Activate’ button for ‘Syntax Highlighter Compress’.
3. Done, enjoy it.

= Usage =

Usage with the TinyMCE button:

1. Create a new post or page in Wordpress or open an existing post/page. 
2. Place the cursor into the main textarea where you want to insert the code. 
3. Click on the new TinyMCE button, you can find after the activation of the plugin. 
4. Paste your code into the textarea of the popup and choose which syntax/brush (PHP, JavaScript, ...) you want to use.
5. Click on the 'Insert' button. That's all.

Usage without TinyMCE button:

1. Take your code and replace all '`<`' with '`&lt;`' (Seach & Replace). 
2. Then enclose your code with the pre Tag like this:

      `<pre class="brush:[code-alias]"> [Your Code Here] </pre>`
For the '[code-alias]' segment you have to choose the right syntax/brush alias. For all the available brush aliases check out <a href="http://alexgorbatchev.com/wiki/SyntaxHighlighter:Brushes">Alex Gorbatchev's Webpage</a>. PHP code should look like this:

      `<pre class="brush:php">
      &lt;?php
        $input = array("Neo", "Morpheus", "Trinity", "Cypher", "Tank");
        $rand_keys = array_rand($input, 2);
        echo $input[$rand_keys[0]] . "\n";
      ?>
      </pre>`
3. Paste final code into the main textarea of Wordpress. Be sure, that you do this in the HTML view.`

== Frequently Asked Questions ==

= Which browsers are supported? =

The most common browsers are supported.

= Which version of wordPress do I need? =

You need WordPress 2.6 or later.

== Screenshots ==

1. Examples (for a live demo, see the plugin's homepage)
2. TinyMCE Button and Popup for easy code pasting
3. Settings menu

== Changelog ==

= 3.0.83.3 =
* Added Romanian translation (THX to Alexander Ovsov - <a href="http://webhostinggeeks.com/science/">Web Geek Sciense</a>)

= 3.0.83.2 =
* Fixed problem with the tab-size input field
* Fixed some bugs in the autoload script
* Cleaner settings menu code

= 3.0.83.1 =
* Upgraded Syntax Highlighter script (version 3.0.83)
* New Dynamic brush loading: only necessary brush files get loaded
* Added settings menu where you can change the theme and other options

= 2.1.364.1 =
* First release, nothing changed so far

== Upgrade Notice ==

= 3.0.83.3 =
-

= 3.0.83.2 =
Use the buildin autoupdater of Wordpress or deactive the plugin and delete the syntax-highlighter-compress folder from the plugins folder. After that upload the new files from the download archive.

= 3.0.83.1 =
Use the buildin autoupdater of Wordpress or deactive the plugin and delete the syntax-highlighter-compress folder from the plugins folder. After that upload the new files from the download archive.

= 2.1.364.1 =
Nothing to update so far. Just install the plugin.