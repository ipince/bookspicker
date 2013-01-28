<!DOCTYPE html>
<!-- <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">-->
<!-- The HTML 4.01 Transitional DOCTYPE declaration-->
<!-- above set at the top of the file will set     -->
<!-- the browser's rendering engine into           -->
<!-- "Quirks Mode". Replacing this declaration     -->
<!-- with a "Standards Mode" doctype is supported, -->
<!-- but may lead to some differences in layout.   -->

<html>
  <head>
  
    <%
    	boolean local = request.getRemoteHost().equals("127.0.0.1");
    %>
  
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">

    <!--                                                               -->
    <!-- Consider inlining CSS to reduce the number of requested files -->
    <!--                                                               -->
    <link rel="icon" type="image/vnd.microsoft.icon" href="/favicon-bp.ico">

    <!--                                           -->
    <!-- Any title is fine                         -->
    <!--                                           -->
    <title>BooksPicker | The fastest and cheapest way to get your college textbooks</title>
    
    <!--                                           -->
    <!-- This script loads your compiled module.   -->
    <!-- If you add any GWT meta tags, they must   -->
    <!-- be added before this line.                -->
    <!--                                           -->
    <script type="text/javascript" language="javascript" src="bookspicker/bookspicker.nocache.js"></script>

	<%
    	// Only enable google analytics on production
		if (!local) {
	%>
	<!-- Start Google Analytics code -->
	<script type="text/javascript">

  	var _gaq = _gaq || [];
  	_gaq.push(['_setAccount', 'UA-17994591-1']);
  	_gaq.push(['_trackPageview']);

  	(function() {
	    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
	    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
	    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  	})();
	</script>
	<!-- End of Google Analytics code -->
	<%
		}
  	%>
</head>

  <body>

    <!-- OPTIONAL: include this if you want history support -->
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
    
    <!-- RECOMMENDED if your web app will not function without JavaScript enabled -->
    <noscript>
      <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
        Your web browser must have JavaScript enabled
        in order for this application to display correctly.
      </div>
    </noscript>
    
    <%
    	// Only add Olark and UserVoice in production version
		if (!local) {
  	%>
    <!-- Begin Olark Chat -->
    <script type="text/javascript">
    (function(){document.write(unescape('%3Cscript src=%27' + (document.location.protocol == 'https:' ? "https:" : "http:") + '//static.olark.com/js/wc.js%27 type=%27text/javascript%27%3E%3C/script%3E'));})();</script><div id="olark-data"><a class="olark-key" id="olark-9584-295-10-6839" title="Powered by Olark" href="http://olark.com/about" rel="nofollow">Powered by Olark</a></div> <script type="text/javascript"> wc_init();
    </script>
    <!-- /End Olark Chat -->
    
	<!--  UserVoice code for feedback tab  -->
    <script type="text/javascript">
    var uservoiceOptions = {
    	key: 'bookspicker',
    	host: 'bookspicker.uservoice.com', 
    	forum: '69025',
    	alignment: 'right',
    	background_color:'#000000', 
    	text_color: 'white',
    	hover_color: '#f2cc0f',
    	lang: 'en',
    	showTab: true
  	};
  	function _loadUserVoice() {
    	var s = document.createElement('script');
    	s.src = ("https:" == document.location.protocol ? "https://" : "http://") + "uservoice.com/javascripts/widgets/tab.js";
    	document.getElementsByTagName('head')[0].appendChild(s);
  	}
  	_loadSuper = window.onload;
	window.onload = (typeof window.onload != 'function') ? _loadUserVoice : function() { _loadSuper(); _loadUserVoice(); };
	</script>
    <%
		}
    %>
    
  </body>
</html>
