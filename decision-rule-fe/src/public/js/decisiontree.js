
var options = {

	colors: {
		nodeHighlight: "#2199e8"
	},
	layout: {
		divId: "tree-panel",
		svgWidth: 1200,
		svgHeight: 1000,
		svgMargin: {
			top: 20,
			right: 90,
			bottom: 30,
			left: 90
		},
		nodeWidth: 250,
		nodeHeight: 250,
		nodeMargin: {
			x: 100,
			y: 250
		},
		zoomScale: [-1, 100],
		transitionDuration: 750
	},

	operatorFunctions: {
		equal: function(a, b){
			return new Promise((resolve, reject) => {
				resolve(a == b);
			});
		},
		greater_than: function(a, b){
			return new Promise((resolve, reject) => {
				resolve(a > b);
			});
		}
	}
};

// + '<option value="1">" < "</option>'
			// + '<option value="2">" = "</option>'
			// + '<option value="3">" > "</option>'
			// + '<option value="4">" != "</option>'
			// + '<option value="5" >" IN "</option>'
			// + '<option value="6" >" NOT IN "</option>'
			// + '<option value="7">" <= "</option>'
			// + '<option value="8">" >= "</option>'

var OPERATION_TYPE =["N/A", " < ", " = ", " > ", " != ", " IN ", " NOT IN ", " <= ", " >= "];

var myBuilder;  
function createBuilder(treeData){
	myBuilder = new DecisionTreeBuilder(treeData, options);
}

function cleanBuidler(){
	// myBuilder.destroy();
	// $("#graph").append('<div id="tree-panel"></div>');
}


function loadAllMaps(){
	
	$.ajax({
		url: 'http://localhost:8080/api/rulemap/',
		dataType: "text",
		type: 'GET',
		async: false,
		// dataType: "text",
		cache: false
	})
	.done(function (json) {
		//console.log(json);
		if(json){
			$("#ruleMap").find('option')
				.remove()
				.end();
			let listRuleMap = JSON.parse(json);
			console.log(listRuleMap);
			$.each(listRuleMap,function(i,v){
				$("#ruleMap").append($('<option/>', { 
				value: v.id,
				text : v.name 
			}))});

			loadTreeMap();
		}
		
	})
	.fail(function (jqXHR, textStatus, errorThrown) {
		console.log(errorThrown);
		console.log("No rule Map found.");
		
	});
}

function createNewTreeMap(){
	let treeBody = {
		name: $("#treeNameInput").val()
	}

	$.ajax({
		url: 'http://localhost:8080/api/rulemap/',
		type: 'POST',
		dataType: "text",
		contentType: "application/json",
		data: JSON.stringify(treeBody),
		success: function(data) {
			//create node on graph
			console.log("Tree is created, id ", data);
			if(data > 0){
				alert("Create Tree successfully, id " + data);
				$("#ruleMap").append($('<option/>', { 
					value: data,
					text : treeBody.name 
				}));
				$("#ruleMap").val(data).trigger('change');
			   
			}else{
				alert("Save expression fail, code " + data);
			}
			
		},
		error: function (jqXHR, textStatus, errorThrown) {
			console.log(errorThrown);
			alert("Can not create expression ")
		}
	})
}

/**
 * Treemap load
 */
function loadTreeMap() {
	console.log("Load tree map "+ $('#ruleMap').val());
	cleanBuidler();
	$.ajax({
		url: 'http://localhost:8080/api/rulemap/tree/' + $('#ruleMap').val(),
		type: 'GET',
		dataType: "text",
		cache: false
	})
	.done(function (data) {
		console.log(data);
		if(data){
			let obj = JSON.parse(data);
			createBuilder(obj);
		}else{
			console.log("No node in tree, add root node");
			let treeData = {
				name: "Root Node",
				isRoot: true
			}
			createRuleNode(null, treeData);
		}
	})
	.fail(function (jqXHR, textStatus, errorThrown) {
		console.log(errorThrown);
	});
	
}

function createRuleNode(parentNode, nodeData){
	nodeData.ruleMapId=$('#ruleMap').val();
	$.ajax({
		url: 'http://localhost:8080/api/rulenode/',
		type: 'POST',
		dataType: "text",
		contentType: "application/json",
		data: JSON.stringify(nodeData),
		success: function(data) {
			//create node on graph
			console.log("Node is created, id ", data);
			if(data > 0){
				nodeData.id = data;
				if(!parentNode){
					createBuilder(nodeData);
				}else{
					myBuilder.addChildNodes(parentNode, nodeData);
				}
			}else{
			    alert("Create Node fail, code " + data);
			}
			
		},
		error: function (jqXHR, textStatus, errorThrown) {
			console.log(errorThrown);
			alert("Can not create Node ")
		}
	})
}

function addNodes(node){

	var newNodesData = 
		{
			"name": "Empty",
			"branch": "YES",
		}
	;

	createRuleNode(node,newNodesData);
}

function addYesNode(node){

	var newNodesData = 
		{
			"name": "Child YES",
			"branch": "YES",
			"parentId": node.data.id
		}
	;


	createRuleNode(node,newNodesData);
}

function addNoNode(node){

	var newNodesData = 

		{
			"name": "Child NO",
			"branch": "NO",
			"parentId": node.data.id
		}
	;

	createRuleNode(node,newNodesData);
}

var currentNodeConfigId;
var currentNodeGraph;
function configNode(node){
	$.ajax({
		url: 'http://localhost:8080/api/rulenode/' + node.data.id,
		type: 'GET',
		dataType: "text",
		cache: false
	})
	.done(function (data) {
		console.log(data);
		if(data){
			
			currentNodeConfigId = node.data.id;
			currentNodeGraph = node;
			let nodeObj = JSON.parse(data);
			node.data.ruleProperties = nodeObj;
			$('#config-node-modal').modal();
			$("#nodeNameInput").val(nodeObj.ruleName);
			$("#nodeEffDate").val(nodeObj.effDate);
			$("#nodeExpDate").val(nodeObj.expDate);
			$("#nodeIsExit").prop("checked",nodeObj.isExitNode == 1);
			
			
			getListEvent(nodeObj.eventId);
			getListFunction();
			getListProperty();

			//action list
			if(nodeObj.actionList){
				$("#actionGroup").empty();
				
				$.each(nodeObj.actionList,function(i,v){
					console.log(v);
					addActionOnUI(v);
				});
			}
			//parse expression
			parseExpressionLink(nodeObj.ruleExpress);

			//exp list
			if(nodeObj.ruleExpList){
				$("#conditionGroup").empty();
				$.each(nodeObj.ruleExpList,function(i,v){
					console.log(v);
					addExpressionOnUI(v);
					
				});
			}

		}else{
			alert("Not found node ", node.data.name)
		}
	})
	.fail(function (jqXHR, textStatus, errorThrown) {
		
		console.log(errorThrown);
		alert("Error get node ", node.data.name)
	});
}



function saveRuleoNode(nodeData){
	$.ajax({
		url: 'http://localhost:8080/api/rulenode/',
		type: 'PUT',
		dataType: "text",
		contentType: "application/json",
		data: JSON.stringify(nodeData),
		success: function(data) {
			//create node on graph
			console.log("Node is saved, id ", data);
			if(data > 0){
				alert("Save Node successfully ");
				if(currentNodeGraph){
					updateNodeData(currentNodeGraph,nodeData);
				}
			}else{
			    alert("Save Node fail, code " + data);
			}
			
		},
		error: function (jqXHR, textStatus, errorThrown) {
			console.log(errorThrown);
			alert("Can not save Node ")
		}
	})
}


function deleteRuleNode(node){
	let currentRuleMap = $('#ruleMap').val();
	$.ajax({
		url: 'http://localhost:8080/api/rulenode/' + node.data.id,
		type: 'DELETE',
		dataType: "text",
		success: function(data) {
			//create node on graph
			console.log("Node is deleted, id ", data);
			if(data > 0){
				node.data.id = "";
				node.data.name="Empty Node"
				// node.id ="";
				if(node.data.isRoot){

					window.location.reload();
					$("#ruleMap").val(currentRuleMap).change();
					alert("Delete successfully all node of tree. Add new root node");
				}else{
					pruneNode(node);
				}
				
				
			}else{
			    alert("Deleted Node fail, code " + data);
			}
		},
		error: function (jqXHR, textStatus, errorThrown) {
			console.log(errorThrown);
			alert("Can not create Node ")
		}
	})
}

var listEvent;
function getListEvent(eventId){
	$.ajax({
		url: 'http://localhost:8080/api/event',
		type: 'GET',
		dataType: "text",
		async: false,
		cache: false
	})
	.done(function (data) {
		console.log(data);
		if(data){
			listEvent = JSON.parse(data);
			$("#outputEvent").empty();
			$("#outputEvent").append($('<option/>', { 
				value: 0,
				text : "N/A" 
			}));
			$.each(listEvent,function(i,v){
				
				if(eventId && eventId ==v.id){
					$("#outputEvent").append($('<option/>',{ 
						value: v.id,
						text : v.name 
					}).attr('selected', true));
				}else{
					$("#outputEvent").append($('<option/>', { 
						value: v.id,
						text : v.name 
					}))
				}
				
			});
		}else{
			alert("Not found any event ", node.data)
		}
	})
	.fail(function (jqXHR, textStatus, errorThrown) {
		
		console.log(errorThrown);
		alert("Error get list event ", node.data)
	});	
}

var listFunction;
function getListFunction(){
	$.ajax({
		url: 'http://localhost:8080/api/function',
		type: 'GET',
		dataType: "text",
		async: false,
		cache: false
	})
	.done(function (data) {
		console.log(data);
		if(data){
			listFunction = JSON.parse(data);
			
		}else{
			alert("Not found any fucntion ", node.data)
		}
	})
	.fail(function (jqXHR, textStatus, errorThrown) {
		
		console.log(errorThrown);
		alert("Error get list fucntion ", node.data)
	});	
}


var listProperty;
function getListProperty(){
	$.ajax({
		url: 'http://localhost:8080/api/property',
		type: 'GET',
		dataType: "text",
		async: false,
		cache: false
	})
	.done(function (data) {
		console.log(data);
		if(data){
			listProperty = JSON.parse(data);
			
		}else{
			alert("Not found any property ", node.data)
		}
	})
	.fail(function (jqXHR, textStatus, errorThrown) {
		
		console.log(errorThrown);
		alert("Error get list property ", node.data)
	});	
}

function saveRuleAction(actionRule, parentDiv){
	actionRule.ruleNodeId = currentNodeConfigId;
	let typeHTTP = (parentDiv.id == undefined || parentDiv.id == "")? 'POST' : 'PUT';
	actionRule.actionId = parentDiv.id;
	$.ajax({
		url: 'http://localhost:8080/api/ruleaction/',
		type: typeHTTP,
		dataType: "text",
		contentType: "application/json",
		data: JSON.stringify(actionRule),
		success: function(data) {
			//create node on graph
			console.log("Action is created, id ", data);
			if(data > 0){
				actionRule.id = data;
				// if(parentDiv){
					parentDiv.id = data;
					alert("Save action successfully, actionId " + data);
				// }
			}else{
			    alert("Save action fail, code " + data);
			}
			
		},
		error: function (jqXHR, textStatus, errorThrown) {
			console.log(errorThrown);
			alert("Can not save action ")
		}
	})
}

function deleteRuleAction(parentDiv){
	$.ajax({
		url: 'http://localhost:8080/api/ruleaction/' + currentNodeConfigId +'/' +parentDiv[0].id,
		type: 'DELETE',
		dataType: "text",
		success: function(data) {
			//create node on graph
			console.log("Delete action result ", data);
			if(data > 0){
				parentDiv.remove();
			}else{
			    alert("Delete action fail, code " + data);
			}
			
		},
		error: function (jqXHR, textStatus, errorThrown) {
			console.log(errorThrown);
			alert("Can not delete action ")
		}
	})
}

function addActionOnUI(ruleAction){
	let funtionOptions = '';
	$.each(listFunction,function(i,v){
		if(ruleAction && v.id == ruleAction.funcId){
			funtionOptions += '<option value="' + v.id + '"' + ' selected ' + ' >' + v.name + '</option>';  
		}else{
			funtionOptions += '<option value="' + v.id + '">' + v.name + '</option>';              
		}
	});

	let propertyLeftOptions = '';
	$.each(listProperty,function(i,v){
		if(ruleAction && v.id == ruleAction.srcPropId){
			propertyLeftOptions += '<option value="' + v.id + '"' + ' selected ' + ' >' + v.name + '</option>';  
		}else{
			propertyLeftOptions += '<option value="' + v.id + '">' + v.name + '</option>';              
		}
	});


	let propertyRightOptions = '';
	$.each(listProperty,function(i,v){
		if(ruleAction && v.id == ruleAction.destPropId){
			propertyRightOptions += '<option value="' + v.id + '"' + ' selected ' + ' >' + v.name + '</option>';  
		}else{
			propertyRightOptions += '<option value="' + v.id + '">' + v.name + '</option>';              
		}
	});

	let param1='<input type="text" class="param1"> ';
	if(ruleAction && ruleAction.param1){
		param1='<input type="text" class="param1" value="' +ruleAction.param1  +'"> ';
	}

	let param2='<input type="text" class="param2"> ';
	if(ruleAction && ruleAction.param2){
		param2='<input type="text" class="param2" value="' +ruleAction.param2  +'"> ';
	}


	var actiondivText  = '<div class="form-group">' 
			+ '<label>Function</label><select name="actionFunction" class="functions">'
			+ '<option value="0"> N/A </option>'
			+ funtionOptions 
			+'</select>'
			+ '<label>Input property</label><select name="inProperty" class="properties">'
			+ '<option value="0"> N/A </option>'
			+ propertyLeftOptions +'</select>'
		
			+ '<label>Param 1</label>'
			+ param1 
			+ '<label>Param 2</label>'
			+ param2
			+ '<label>Output property</label>'
			+'<select name="outProperty" class="properties">'
			+ '<option value="0"> N/A </option>'
			+ propertyRightOptions +'</select>'
			// + '<div  style="text-align: right; padding-top: 5px;">' 
				+ '<label class="saveAction btn btn-success btn-default" style="text-align: right; margin: 10px;">Save</label>'
				+ '<label  class="deleteAction btn btn-danger btn-default" style="text-align: right; margin: 5px;">Delete</label>'
			// + '</div>'
		+ '</div>';
	
	let actionDiv =    $("#actionGroup").append(actiondivText).children().last()[0];
	if(ruleAction && ruleAction.actionId){
		actionDiv.id = ruleAction.actionId;
	}
	

	$(".saveAction").each(function(){
		$(this).click(function(e){
			e.preventDefault();
			var parentDiv = $(this).parent()[0];

			let actionRule = {
				funcId: parseInt(parentDiv.children[1].value),
				srcPropId: parseInt(parentDiv.children[3].value),
				param1: parentDiv.children[5].value,
				param2: parentDiv.children[7].value,
				destPropId: parseInt(parentDiv.children[9].value)
			}

			if(actionRule.funcId <=0 || actionRule.srcPropId <= 0 || actionRule.destPropId <=0){
				alert("Please Choose function, input property, output property");
				return;
			}
							
			saveRuleAction(actionRule, parentDiv);

		});
	});

	$(".deleteAction").each(function(){
		$(this).click(function(e){
			e.preventDefault();
			var parentDiv = $(this).parent();
			let actionId =parentDiv[0].id;
			if(actionId){
				deleteRuleAction(parentDiv);
			}else{
				parentDiv.remove();
				// alert("Can not delete action");

			}
			
		});
	});
};

var expressionLinkArray =[]
var EXPRESSION_LINL_OFFSET_INDEX = 3;

function parseExpressionLink(strExpLink){
	// expressionLinkArray = [undefined, undefined, undefined]
	expressionLinkArray = [];
	if(strExpLink && strExpLink != "null"){
		
		let operationPosition = -1;
		for (var i = 0; i < strExpLink.length; i++) {
			let c = strExpLink.charAt(i);
			if(c == '(' || c == ')' || c=='n' || c== 'o'){
				let expressionId = strExpLink.substring(operationPosition+1,i);
				if(expressionId){
					expressionLinkArray.push(expressionId);
				}
				operationPosition =i;
				expressionLinkArray.push(c);
			}
		}

		if(operationPosition < strExpLink.length -1){
			let expressionId = strExpLink.substring(operationPosition+1,strExpLink.length);
			expressionLinkArray.push(expressionId);
		}
	}
	
}

function saveRuleExpression(expression, parentDiv){
	expression.ruleNodeId = currentNodeConfigId;
	let typeHTTP = (parentDiv.id == undefined || parentDiv.id == "")? 'POST' : 'PUT';
	expression.id = parentDiv.id;
	$.ajax({
		url: 'http://localhost:8080/api/expression/',
		type: typeHTTP,
		dataType: "text",
		contentType: "application/json",
		data: JSON.stringify(expression),
		success: function(data) {
			//create node on graph
			console.log("expression is created, id ", data);
			if(data > 0){
				
				expression.id = data;
				// if(parentDiv){
				parentDiv.id = data;
				console.log(parentDiv.parentNode);
				alert("Save expression successfully, expressionId " + data);
				let index = $(parentDiv).index();	
				expressionLinkArray[index] =parentDiv.id;
				
			}else{
			    alert("Save expression fail, code " + data);
			}
			
		},
		error: function (jqXHR, textStatus, errorThrown) {
			console.log(errorThrown);
			alert("Can not create expression ")
		}
	})
}

function deleteRuleExpression(parentDiv){
	$.ajax({
		url: 'http://localhost:8080/api/expression/' +parentDiv.id,
		type: 'DELETE',
		dataType: "text",
		success: function(data) {
			//create node on graph
			console.log("Delete expression result ", data);
			if(data > 0){
				expressionLinkArray.splice($(parentDiv).index(), 1);
				parentDiv.remove();
			}else{
			    alert("Delete expression fail, code " + data);
			}
			
		},
		error: function (jqXHR, textStatus, errorThrown) {
			console.log(errorThrown);
			alert("Can not expression ")
		}
	})
}

function addExpressionOnUI(expression){
	let funtionLeftOptions = '';
	$.each(listFunction,function(i,v){
		if(expression && v.id == expression.functionLeftId){
			funtionLeftOptions += '<option value="' + v.id + '"' + ' selected ' + ' >' + v.name + '</option>';  
		}else{
			funtionLeftOptions += '<option value="' + v.id + '">' + v.name + '</option>';              
		}
	});

	let funtionRightOptions = '';
	$.each(listFunction,function(i,v){
		if(expression && v.id == expression.functionRightId){
			funtionRightOptions += '<option value="' + v.id + '"' + ' selected ' + ' >' + v.name + '</option>';  
		}else{
			funtionRightOptions += '<option value="' + v.id + '">' + v.name + '</option>';              
		}
	});

	let propertyLeftOptions = '';
	$.each(listProperty,function(i,v){
		if(expression && v.id == expression.propertyLeftId){
			propertyLeftOptions += '<option value="' + v.id + '"' + ' selected ' + ' >' + v.name + '</option>';  
		}else{
			propertyLeftOptions += '<option value="' + v.id + '">' + v.name + '</option>';              
		}
	});


	let propertyRightOptions = '';
	$.each(listProperty,function(i,v){
		if(expression && v.id == expression.propertyRightId){
			propertyRightOptions += '<option value="' + v.id + '"' + ' selected ' + ' >' + v.name + '</option>';  
		}else{
			propertyRightOptions += '<option value="' + v.id + '">' + v.name + '</option>';              
		}
	});

	let leftParam1='<input type="text" class="param1"> ';
	if(expression && expression.paramFirstLeft){
		leftParam1='<input type="text" class="param1" value="' +expression.paramFirstLeft  +'"> ';
	}

	let leftParam2='<input type="text" class="param2"> ';
	if(expression && expression.paramSecondLeft){
		leftParam2='<input type="text" class="param2" value="' +expression.paramSecondLeft  +'"> ';
	}

	let rightParam1='<input type="text" class="param1"> ';
	if(expression && expression.paramFirstRight){
		rightParam1='<input type="text" class="param1" value="' +expression.paramFirstRight  +'"> ';
	}

	let rightParam2='<input type="text" class="param2"> ';
	if(expression && expression.paramSecondRight){
		rightParam2='<input type="text" class="param2" value="' +expression.paramSecondRight  +'"> ';
	}

	let isOperand = '<input type="checkbox" value="0" >';
	if(expression && expression.typeOperand == 1){
		isOperand='<input type="checkbox" value="1" checked>' ;
	}

	let operations = '';
	let i =1;
	for(i=1; i<9;i++){
		if(expression && i == expression.operation){
			operations += '<option value="' + i + '"' + ' selected ' + ' >' +OPERATION_TYPE[i] + '</option>';  
		}else{
			operations += '<option value="' + i + '">' + OPERATION_TYPE[i] + '</option>';              
		}
	}

        
	let conditiondiv = '<div class="form-group" style="margin: 10px 0px; background-color: #e1e1d0; padding: 15px;">' 
			+ '<label style="font-size: 16px"><span class="glyphicon glyphicon-object-align-left "></span>Condition</label></br>'
			+ '<label>Left side: Function</label>'
			+ '<select name="functionLeft" class="functions">' 
			+ '<option value="0"> N/A </option>'
			+ funtionLeftOptions+'</select>'
			+ '<label>Input property</label>'
			+ '<select name="leftProperty" class="properties">' 
			+ '<option value="0"> N/A </option>'
			+ propertyLeftOptions + '</select>'
			+ '<label>Param 1</label>'
			+ leftParam1 
			+ '<label>Param 2</label>'
			+ leftParam2
			+ '</br>'

			+ '<label style="margin:15px 0px;">Operator  </label>'
			+ '<select name="operator">'
			+ '<option value="0"> N/A </option>'
			+ operations
			// + '<option value="1">" < "</option>'
			// + '<option value="2">" = "</option>'
			// + '<option value="3">" > "</option>'
			// + '<option value="4">" != "</option>'
			// + '<option value="5" >" IN "</option>'
			// + '<option value="6" >" NOT IN "</option>'
			// + '<option value="7">" <= "</option>'
			// + '<option value="8">" >= "</option>'
			+ '</select>'
			+ '</br>'


			+ '<label>Right side: </label>'
			+ '<label style="margin-left: 10px;margin-right: 5px; "> Is Operand  </label>'
			+ isOperand
			
			+ '<label style="margin-left: 20px">Param 1</label>'
			+ rightParam1
			+ '<label>Param 2</label>'
			+ rightParam2
			+ '<label>Input property</label>'
			+ '<select name="rightProperty" class="properties">'
			+ '<option value="0"> N/A </option>'
			+ propertyRightOptions+ '</select>'
			+ '<label style="margin-left: 10px">Function</label>'
			+ '<select name="functionRight" class="functions">' 
			+ '<option value="0"> N/A </option>'
			+ funtionRightOptions + '</select>'
			+ '<div  style="text-align: right; padding-top: 15px;">' 
				+ '<label class="saveCondition btn btn-success btn-default" style="margin: 10px;">Save</label>'
				+ '<label  class="deleteCondition btn btn-danger btn-default" style="margin: 5px;">Delete</label>'
			+ '</div>'
		+ '</div>';
        
		let expressionDiv =  $("#conditionGroup").append(conditiondiv).children().last()[0];
		let expressionIndex = $(expressionDiv).index();
		if(expression && expression.id){
			expressionDiv.id = expression.id;
		}

		expressionLinkArray[expressionIndex] = expressionDiv.id;

		let nextEx = expressionLinkArray[expressionIndex+1];
		if(nextEx){
			if (nextEx == '('){
				addOpenParenthesUI();
			}else if (nextEx == ')'){
				addCloseParenthesUI();
			}else if (nextEx == 'n'){
				addAndOperatorUI();
			}
			else if (nextEx == 'o'){
				addOrOperatorUI();
			}
		}
		
		

        $(".saveCondition").each(function(){
            $(this).click(function(e){
				e.preventDefault();
				var parentDiv = $(this).parent()[0].parentNode;
	
				let expressionData = {
					functionLeftId: parseInt(parentDiv.children[3].value),
					propertyLeftId: parseInt(parentDiv.children[5].value),
					paramFirstLeft: parentDiv.children[7].value,
					paramSecondLeft: parentDiv.children[9].value,
					operation: parseInt(parentDiv.children[12].value),
					typeOperand: parseInt(parentDiv.children[16].checked == true ? 1: 0),
					
					paramFirstRight: parentDiv.children[18].value,
					paramSecondRight: parentDiv.children[20].value,
					propertyRightId: parseInt(parentDiv.children[22].value),
					functionRightId: parseInt(parentDiv.children[24].value),
				}
	
				if(expressionData.operation <=0){
					alert("Please Choose operation");
					return;
				}
								
				saveRuleExpression(expressionData, parentDiv);
	
            });
        });

        $(".deleteCondition").each(function(){
            $(this).click(function(e){
                e.preventDefault();
				var parentDiv = $(this).parent()[0].parentNode;
				let result =parentDiv.id;
				if(result){
					deleteRuleExpression(parentDiv);
				}else{
					expressionLinkArray.splice($(parentDiv).index(), 1);
					parentDiv.remove();
					// alert("Can not delete action");

				}
			});
        });
};


function addAndOperatorUI(){
	var div = '<div style="text-align: center;">' 
            + '<label class="btn btn-success btn-default">AND</label>'
			+ '<label class="deleteAnd btn btn-danger btn-default" style="margin-left: 15px;">Delete</label>'
            + '</div>';
    let element = $("#conditionGroup").append(div).children().last()[0];
	let index = $(element).index();
	expressionLinkArray[index] = 'n';

	$(".deleteAnd").each(function(){
		$(this).click(function(e){
			e.preventDefault();
			var parentDiv = $(this).parent()[0];
			
			expressionLinkArray.splice($(parentDiv).index(), 1);
			parentDiv.remove();
			
		});
	});
}

function addOrOperatorUI(){
	var div = '<div style="text-align: center;">' 
		+ '<label class="btn btn-success btn-default">OR</label>'
		+ '<label class="deleteOr btn btn-danger btn-default" style="margin-left: 15px;">Delete</label>'
        + '</div>';

		
	let element = $("#conditionGroup").append(div).children().last()[0];
	let index = $(element).index();
	expressionLinkArray[index] = 'o';

	$(".deleteOr").each(function(){
		$(this).click(function(e){
			e.preventDefault();
			var parentDiv = $(this).parent()[0];
			
			expressionLinkArray.splice($(parentDiv).index(), 1);
			parentDiv.remove();
			
		});
	});
}

function addOpenParenthesUI(){
	var div = '<div style="text-align: center;">' 
		+ '<label class="btn btn-success btn-default"> ( </label>'
		+ '<label class="deleteOpenParenthes btn btn-danger btn-default" style="margin-left: 15px;">Delete</label>'
        + '</div>';

	let element = $("#conditionGroup").append(div).children().last()[0];
	let index = $(element).index();
	expressionLinkArray[index] = '(';

	$(".deleteOpenParenthes").each(function(){
		$(this).click(function(e){
			e.preventDefault();
			var parentDiv = $(this).parent()[0];
			
			expressionLinkArray.splice($(parentDiv).index(), 1);
			parentDiv.remove();
			
		});
	});
}

function addCloseParenthesUI(){
	var div = '<div style="text-align: center;">' 
            + '<label class="btn btn-success btn-default"> ) </label>'
			+ '<label class="deleteCloseParenthes btn btn-danger btn-default" style="margin-left: 15px;">Delete</label>'
			+ '</div>';
	
    let element = $("#conditionGroup").append(div).children().last()[0];
	let index = $(element).index();
	expressionLinkArray[index] = ')';

	$(".deleteCloseParenthes").each(function(){
		$(this).click(function(e){
			e.preventDefault();
			var parentDiv = $(this).parent()[0];
			
			expressionLinkArray.splice($(parentDiv).index(), 1);
			parentDiv.remove();
			
		});
	});
}


function pruneNode(node){
	myBuilder.deleteNode(node);
}

function serialise(){
	var tree = myBuilder.serialiseTreeToJSON();
	console.log(tree);
	alert(tree);

	
}

function updateNodeData(node, newData){

	myBuilder.updateDecisionNodeData(node, newData);
}

function queryTree(){

	

	// var carExample = {
	// 	"isPublic": false,
	// 	"hasWheels": true
	// };


	myBuilder.queryDecisionTree(carExample2).then((result) => {
		alert(JSON.stringify(result));
		console.log(JSON.stringify(result));
	});

}

window.addEventListener('nodeClick', function (e) {
	var node = e.detail;
	if(!node){
		return;
	}
	var action = $("input:radio[name ='nodeAction']:checked").val();
	$('.nodeAction').prop('checked', false);
	if(!action){
		action = node.action;
	}
	console.log('nodeClick action',action);
	

	switch(action){

		case "addRootNode":
			addNodes(node);
			break;
		case "addYesNode":
			addYesNode(node);
			break;
		case "addNoNode":
			addNoNode(node);
			break;

		case "pruneNode":
			deleteRuleNode(node);
			break;

		case "updateDecisionNodeData":
			updateDecisionNodeData(node);
			break;

		case "configNode":
			node = node.node;
			console.log("config node ", node);
			
			configNode(node);
			break;

	}
});



