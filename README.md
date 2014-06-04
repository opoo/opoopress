# About OpooPress

OpooPress Blogging Framework is a java based static blog generator, it's also a static site generator. The site is [www.opoopress.com](http://www.opoopress.com/).

Here are some main features:

- **Simple** - No more databases, no more runtime language (such as php, java), a web server is sufficient
- **Fast** - A very fast generate engine by Java, [generate 1000 posts in several seconds](http://opoo.org/why-i-develop-opoopress/)
- **Static** - Markdown/Textile/WikiText, FreeMarker, HTML & CSS & Images go in, completely static sites come out ready for deployment to anywhere
- **Blog-aware** - Permalinks, tree categories, tags, pages, posts, archives, RSS feed.

OpooPress also have these features:

- A semantic HTML5 template
- A Mobile first responsive layout (rotate, or resize your browser and see)
- Built in 3rd party support for Twitter, Google Plus One, Disqus Comments, Pinboard, Delicious, and Google Analytics
- Easy theming with Compass and Sass

(Note: *description above comes from Octopress, OpooPress have the same features actually*)

- Supports many deployment methods (by [Apache Maven Wagon](http://maven.apache.org/wagon/))
- Supports code syntax highlighting (by [Syntax Highlighter Compress](http://alexgorbatchev.com/SyntaxHighlighter/))
- Supports Locale (Currently only `en_US` and `zh_CN`, default use the locale of JVM)
- Supports Plugin, easy to customize or secondary develop
- Customisable templates ([FreeMarker](http://www.freemarker.org/))
- Supports custom meta data that is exposed to templates, pages or posts

## Post Format
- HTML - (\*.html)
- [Markdown](http://daringfireball.net/projects/markdown/) by [Txtmark](https://github.com/rjeschke/txtmark) - (\*.md, \*.markdown)
- [Textile](http://textile.sitemonks.com/) by [Textile-j](https://java.net/projects/textile-j)(also see [Mylyn WikiText](http://wiki.eclipse.org/Mylyn/Incubator/WikiText)) - (\*.textile)
- [WikiText](http://wiki.eclipse.org/Mylyn/WikiText) - (\*.textile, \*.tracwiki, \*.mediawiki, \*.twiki, \*.confluence)

## Migration
- Octopress
- WordPress

## Deployment
- File
- HTTP
- FTP
- SSH/SCP
- WebDAV
- [Github Pages](http://www.opoopress.com/en/docs/github-pages/)

# Getting Started

Check the documentation to kown how to [download](http://www.opoopress.com/en/download/) and [install](http://www.opoopress.com/en/docs/installation/) OpooPress.

Stable releases are available from [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Corg.opoo.press), and snapshot builds are available from the [Sonatype OSS snapshots repository](https://oss.sonatype.org/index.html#nexus-search;quick~org.opoo.press).

## Source Code
You can access the source code at: [https://github.com/opoo/opoopress](https://github.com/opoo/opoopress)

## Demo Sites
- [OpooPress.com](http://www.opoopress.com/)
- [Opoo.org](http://opoo.org/)

## License

Copyright 2013 Alex Lin.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  <http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


## Feedback / Contact
If you want to see new features in OpooPress, don't hesitate to offer suggestions, clone the repository on GitHub. If you find errors you can [post issues](https://github.com/opoo/opoopress/issues) or send me a pull request.

Send a message to `opoo#users.sf.net` with any requests/feedback!