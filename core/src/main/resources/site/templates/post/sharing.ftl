<div class="sharing">
  <#if (site.twitter_tweet_button)!false == true>
  <#assign showTweetButton = true>
  <a href="http://twitter.com/share" class="twitter-share-button" data-url="${ site.url }${ page.url }" data-via="${ site.twitter_user }" data-counturl="${ site.url }${ page.url }" >Tweet</a>
  </#if>
  <#if (site.google_plus_one)!false == true>
  <#assign showGooglePlusOne = true>
  <div class="g-plusone" data-size="${ site.google_plus_one_size }"></div>
  </#if>
  <#if (site.facebook_like)!false == true>
  <#assign showFacebookLike = true>
    <div class="fb-like" data-send="true" data-width="450" data-show-faces="false"></div>
  </#if>
</div>
