$( document ).ready(function() {

   console.log("pathname:" + location.pathname);
   console.log("href:" + location.href);
   console.log("host:" + location.host);

   var host = location.host;
   var index = host.indexOf(":");
   if (index != -1) {
      host = host.substr(0, index);
      console.log("new host:" + host);
   }

   var a=GetRequest();
   var port=a['port'];
   console.log("port:" + port);
   openTerminal(host, port);
});

function openTerminal(ip, port) {
   console.log("openTerminal 1");

   var term;
   var wsUrl = "ws://" + ip + ":" + port
   websocket = new WebSocket(wsUrl);//new 一个websocket实例
   websocket.onopen = function(evt) {//打开连接websocket

      term = new Terminal(100, parseInt(window.screen.availHeight/20), function(key) {
            websocket.send(key);
            console.log("send:" + key);
      });

      term.open();
      $('.terminal').detach().appendTo('#container-terminal');
      //term.focus();
      //term.open(document.getElementById('container-terminal'));//屏幕将要在哪里展示，就是屏幕展示的地方
      websocket.onmessage = function(evt) {//接受到数据
          console.log("receive : " + evt.data.charCodeAt(0))

          term.write(evt.data);//把接收的数据写到这个插件的屏幕上
          console.log("receive:" + evt.data)

      }
      websocket.onclose = function(evt) {//websocket关闭
         　　term.write("Session terminated");
         　　term.destroy();//屏幕关闭
      }
      websocket.onerror = function(evt) {//额处理
         　　if (typeof console.log == "function") {
           　　　　console.log(evt)
         　　}
      }
   }


}

function GetRequest() {
   var url = location.search; //获取url中"?"符后的字串
   var theRequest = new Object();
   if (url.indexOf("?") != -1) {
      var str = url.substr(1);
      strs = str.split("&");
      for (var i = 0; i < strs.length; i++) {
         theRequest[strs[i].split("=")[0]] = decodeURIComponent(strs[i].split("=")[1]);
      }
   }
   return theRequest;
}

function showSuccessInfo(message){
    var snackbarId = "snackbar";
    var snackbarElement = $("#"+snackbarId);
    snackbarElement.addClass("show");
    snackbarElement.css({"backgroundColor": "#5cb85c"});
    snackbarElement.html(message)
    setTimeout(function(){
        snackbarElement.removeClass("show");
    }, 3000);
}

function showErrorInfo(message){
    var snackbarId = "snackbar";
    var snackbarElement = $("#"+snackbarId);
    snackbarElement.addClass("show");
    snackbarElement.css({"backgroundColor": "#d9534f"});
    snackbarElement.html(message)
    setTimeout(function(){
        snackbarElement.removeClass("show");
    }, 3000);
}
