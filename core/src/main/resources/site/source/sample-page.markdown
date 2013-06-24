---
layout: page
title: Sample Page
comments: true
date: 2013-06-23 11:20
sidebar: false
xyz: "This is a custom text."
footer: false
---

This is a sample page.

The `front-matter` of this page is:

	layout: page
	title: Sample Page
	comments: true
	date: 2013-06-23 11:20
	sidebar: false
	xyz: "This is a custom text."

Variable output:

	${'$'}{site.url} = ${site.url}
	${'$'}{page.layout} = ${page.layout}
	${'$'}{page.title} = ${page.title}
	${'$'}{page.comments} = ${page.comments?string}
	${'$'}{page.date} = ${page.date?datetime}
	${'$'}{page.sidebar} = ${page.sidebar?string}
	${'$'}{page.xyz} = ${page.xyz}


A java code block here:

~~~java
import org.opoo.press.Plugin;
import org.opoo.press.Site;

public class MyPlugin implements Plugin{
	//Construct MyPlugin
	public void initialize(Site site){
		//
	}
}
~~~

