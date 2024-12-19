
var waitForFinalEvent = (function () {
    var timers = {};
    return function (callback, ms, uniqueId) {
        if (!uniqueId) {
            uniqueId = "Don't call this twice without a uniqueId";
        }
        if (timers[uniqueId]) {
            clearTimeout(timers[uniqueId]);
        }
        timers[uniqueId] = setTimeout(callback, ms);
    };
})();
var TFunc = {
    setCookie: function (name, value, expires, path, domain, secure) {
        var today = new Date();
        today.setTime(today.getTime());
        var expires_date = new Date(today.getTime() + (expires));
        document.cookie = name + "=" + escape(value) + ((expires) ? ";expires=" + expires_date.toGMTString() : "") + ((path) ? ";path=" + path : "") + ((domain) ? ";domain=" + domain : "") + ((secure) ? ";secure" : "");
    },
    getCookie: function (name) {
        var start = document.cookie.indexOf(name + "=");
        var len = start + name.length + 1;
        if ((!start) && (name != document.cookie.substring(0, name.length))) {
            return null;
        }
        if (start == -1) return null;
        var end = document.cookie.indexOf(";", len);
        if (end == -1) end = document.cookie.length;
        return unescape(document.cookie.substring(len, end));
    },
    deleteCookie: function (name, path, domain) {
        if (this.getCookie(name))
            document.cookie = name + "=" + ((path) ? ";path=" + path : "") + ((domain) ? ";domain=" + domain : "") + ";expires=Mon, 11-November-1989 00:00:01 GMT";
    },
    addEvent: function (obj, eventName, func) {
        if (obj.attachEvent) {
            obj.attachEvent("on" + eventName, func);
        }
        else if (obj.addEventListener) {
            obj.addEventListener(eventName, func, true);
        }
        else {
            obj["on" + eventName] = func;
        }
    }
};



$(document).ready(function () {

    ///============================ API ===========================
    loadAllMaps();

    
    $('#ruleMap').change(function() {
        $("#tree-panel").empty();
        loadTreeMap();
    });
    
    
    $("#newTreeBtn").click(function(){
        $("#add-tree-modal").modal();
    });

    $("#createTreeBtn").click(function(){
        createNewTreeMap();
    });

    //========================================DIALOG CONFIG NODE========================================

    
    
    $("#saveRuleNode").click(function(e){
        // var parentDiv = $(this).parent()[0].parentNode;
        let expressionLinkStr ='';
        for(var i=0; i< expressionLinkArray.length; i++){
            if(expressionLinkArray[i]){
                expressionLinkStr += expressionLinkArray[i];
            }
        }
        let data={
            id: currentNodeConfigId,
            name: $("#nodeNameInput").val(),
			effDate: $("#nodeEffDate").val(),
			expDate: $("#nodeExpDate").val(),
			exitMap: $("#nodeIsExit").is(':checked') == true ? 1 : 0,
			eventId: $("#outputEvent").val(),
            expressionLink: expressionLinkStr
        }
        saveRuleoNode(data);
    });
    

	$("#addAction").click(function(){
        
        addActionOnUI(undefined);
    });
    
    
    $("#addCondition").click(function(){
        addExpressionOnUI(undefined);

    });

    $("#orOperator").click(function(){
        addOrOperatorUI();
    });

    $("#andOperator").click(function(){
        addAndOperatorUI();
    });

    $("#openParenthes").click(function(){
        addOpenParenthesUI();
    });

    $("#closeParenthes").click(function(){
        addCloseParenthesUI();
    });

});


function showGuide(){
    $("#guide-modal").modal();
}

function test(){
    window.open("/test", '_blank').focus();
}


// document.querySelector('.template').innerHTML = importForm;