(function () {

	'use strict';

	let DecisionTreeBuilder = function(data, options){

		// init layout from options
		let margin = options.layout.svgMargin;
		let	width = options.layout.svgWidth - options.layout.svgMargin.left - options.layout.svgMargin.right;
		let height = options.layout.svgHeight - options.layout.svgMargin.top - options.layout.svgMargin.bottom;
		let nodeWidth = options.layout.nodeWidth;
		let nodeHeight = options.layout.nodeHeight;
		let nodeMargin = options.layout.nodeMargin;
		let duration = options.layout.transitionDuration;
		let divId = '#' + options.layout.divId;

		/* -------------------------- Public properties --------------------------------*/
		// note that for flexibility; we are exposing some key components of the tree, use at your own risk!

		let self = this;
		let nodeIndex = 0;

		this.options = options;
		this.operatorFunctions = options.operatorFunctions;

		this.root = null;
		this.treeData = data;
		this.nodes = null;
		this.links = null;

		this.treemap = d3.tree()
			.nodeSize([nodeWidth + nodeMargin.x, nodeHeight + nodeMargin.y])
			.separation(function(a, b) {
				return a.parent == b.parent ? 1 : 1.25;
			});

		// Assigns parent, children, height, depth
		this.root = d3.hierarchy(this.treeData, function (d) {
			return d.children;
		});
		this.root.id = this.treeData.id;
		console.log("==>Root node : ", this.root);

		var zoom = d3.zoom()
			.scaleExtent([1/4, 4])
			.on('zoom', function () {
				treePanel.attr('transform',
					'translate(' + d3.event.transform.x +','+ d3.event.transform.y + ')'
					+   'scale(' + d3.event.transform.k + ')');
			});
		
		// create our SVG and groups (treePanel group nesting required to encapsulate zooming behaviour)
		let svg = d3.select(divId).append("svg")
			.attr("width", "100%")
			.attr("height", height + margin.top + margin.bottom)
			.append("g")
			.attr("transform", "translate(0,-9999999) scale(1)")
			.attr("id", "treePanel");
		let treePanel = d3.select('#treePanel');
		let svgHandle = d3.select('svg');
		svgHandle.call(zoom);

		// deselect if click off node
		d3.select(divId).on('click', function(event){
			let isNode = d3.event.target.className.baseVal.indexOf('node-rect') != -1;
			if(!isNode){
				self.setHighlighted(false, true);
				_broadcastNode(null);
			}
		});

		/* -------------------------- Public methods --------------------------------*/

		this.destroy = function(){
			d3.select(divId).select("svg").remove();
		};

		/**
		 * For my use-case this eval functionality will be implemented server-side, however here's an
		 * example of how to test a given target object against the tree rules to get a result.
		 * @param target
		 * @returns {*}
		 */
		this.queryDecisionTree = function(target){

			let nodes = this.treeData.descendants();
			let root = nodes[0];
			let decisionPath = '';

			return evaluateDecisionNode(root);

			/**
			 * Recursively evaluates decision nodes, returns a result and
			 * the corresponding binary path it took to reach the result
			 * @param node
			 * @returns {*}
			 */
			function evaluateDecisionNode(node){

				return new Promise((resolve, reject) => {

					// if no more children, we have a result
					if (!node.children) resolve({result: node.data.branch, path: decisionPath});

					// decision node property
					let rules = node.data.rules;
					if(!rules) reject("All decision nodes must have rules defined for tree to be evaluated.");

					// evaluates all rules into a boolean array
					testRulesArray(rules, target).then((results) => {

						/*
							 We need to split by OR conditions, e.g.
							 0: (0 AND 1 AND 2) OR
							 1: (3 AND 4) OR
							 2: (5 AND 6)

							 Then, we simply test if any of the groups are truthy for the decision to be true.
						 */

						let orConditions = rules.filter(function(r){
							return r.condition && r.condition == 'OR';
						});

						// group rules by AND's
						let ruleGroups = { "0": [] };
						let arrIndex = 0;
						let groupIndex = 0;

						if(orConditions.length > 0){
							rules.forEach(function(r){

								if(!r.condition || r.condition == 'AND'){
									ruleGroups[groupIndex].push(results[arrIndex]);
								}

								else if(r.condition && r.condition == 'OR'){
									groupIndex++;
									ruleGroups[groupIndex] = [results[arrIndex]];
								}

								arrIndex++;
							});
						}

						else {
							ruleGroups[0] = results;
						}

						let decisionResult = false;
						let ruleGroupKeys = Object.keys(ruleGroups);
						ruleGroupKeys.forEach(function(key){
							if(ruleGroups[key].every(function(result){ return result == true; })){
								decisionResult = true;
							}
						});

						decisionPath += decisionResult ? 1 : 0;

						// step into true branch
						if (decisionResult) {
							resolve(evaluateDecisionNode(node.children[1]));
						}
						// step into false branch
						else {
							resolve(evaluateDecisionNode(node.children[0]));
						}

					});


				});

			}

		};

		/**
		 * Creates a Promise to test each individual rule.
		 * This could be improved, by failing fast where possible rather than always testing all conditions.
		 * @param rules
		 * @param testTarget
		 * @returns {Promise}
		 */
		function testRulesArray(rules, testTarget){

			let promises = [];

			rules.forEach(function(rule){

				let promise = new Promise((resolve, reject) => {

					// if single rule, simple
					let decisionProperty = rule.property;
					let decisionOperatorType = rule.operator;
					let decisionValue = rule.value;

					// the target value to test
					// this *might* be async if we don't pre-request all possible properties
					// in fact, this value might be dependant on the operator in some cases, for example if
					// the value represents 'was target classified as x within the last y weeks?'
					// perhaps the value request should live in the OPERATOR for complex/compound queries?
					let testValue = testTarget[decisionProperty];

					//console.log(decisionProperty + ': ' + testValue + ' ' + decisionOperatorType + ' ' + decisionValue);

					// check the condition of truthy node
					let operator = self.operatorFunctions[decisionOperatorType];

					operator(testValue, decisionValue).then((decisionTruthy) => {
						resolve(decisionTruthy);
					});
				});

				promises.push(promise);
			});

			return Promise.all(promises)

		}

		/**
		 * Returns a stripped back clone of the given node (has d3 properties removed etc).
		 * Given the circular dependencies caused by d3's parent/child objects, you may wish to use
		 * this or something similar when you run into these problems (also directly modifying the `node`
		 * which is broadcast via `nodeClick` is not recommended).
		 * @param node
		 */
		this.cloneAndStripNode = function(node){

			// stringify does the heavy lifting, just delete parent to prevent circular dependencies
			let target = JSON.stringify(node, function(key, value){

				// parent creates circular dependency, ignore it
				if(key == "parent"){
					return undefined;
				}
				return value;
			});

			target = JSON.parse(target);
			stripNode(target);

			/**
			 * A recursive function to crawl all nodes and children to
			 * strip the d3 data properties we don't want to serialise
			 * @param node
			 */
			function stripNode(node){

				// strip this node
				delete node.height;
				delete node.depth;
				delete node.id;
				delete node.parent;
				delete node.x;
				delete node.x0;
				delete node.y;
				delete node.y0;

				// move data properties into object root
				node['name'] = node.data.name;
				if(node.data.rules) node['rules'] = node.data.rules;
				if(node.data.branch) node['branch'] = node.data.branch;
				delete node.data;

				// strip its children
				if(node.children){
					node.children.forEach((child) => {
						stripNode(child);
					});
				}

			}

			return target;

		};


		/**
		 * @summary Returns a stringified representation of the tree.
		 */
		this.serialiseTreeToJSON = function(){

			let nodes = this.treeData.descendants();

			// stringify does the heavy lifting, just stip parent to prevent circular dependencies
			let root = JSON.stringify(nodes[0], function(key, value){

				// parent creates circular dependency, ignore it
				if(key == "parent"){
					return undefined;
				}
				return value;
			});

			root = JSON.parse(root);
			stripNode(root);

			/**
			 * A recursive function to crawl all nodes and children to
			 * strip the d3 data properties we don't want to serialise
			 * @param node
			 */
			function stripNode(node){

				// strip this node
				delete node.height;
				delete node.depth;
				delete node.id;
				delete node.parent;
				delete node.x;
				delete node.x0;
				delete node.y;
				delete node.y0;

				// move data properties into object root
				node['name'] = node.data.name;
				node['id'] = node.data.id;
				node['parentId'] = node.data.parentId;
				// if(node.data.rules) node['rules'] = node.data.rules;
				if(node.data.branch) node['branch'] = node.data.branch;
				delete node.data;

				// strip its children
				if(node.children){
					node.children.forEach((child) => {
						stripNode(child);
					});
				}

			}

			return JSON.stringify(root);
		};

		/**
		 * @summary Prunes the target decision node and all of its children.
		 * If target node is not a decision node, no action taken.
		 * @param node
		 */
		this.pruneNode = function(node){
			if(node && node.children && node.children.length > 0){
				delete node.children;
				delete node.data.children;
				delete node.data.rules;
				this.update(node);
				_broadcastNode(node);
			}
		};

		/**
		 * @summary Experimental (use `pruneNode` instead). The tree should only be *pruned* at
		 * decision nodes to remain valid (so we don’t end up with single children).
		 * @param node
		 */
		this.deleteNode = function(node){

			var self = this;

			_visit(this.treeData, function(d) {
				if(d.children) {
					for (var child of d.children) {
						if (child == node) {
							//d.children = _.without(d.children, child);
							d.children = _filterChildren(d.children, [child]);
							self.update(self.root);
							break;
						}
					}
				}
			},

			function(d) {
				return d.children && d.children.length > 0 ? d.children : null;
			});
		};

		/**
		 * @summary Overwrite the existing nodes data, use with caution!
		 * (you'll probably corrupt tree if not mindful of preserving existing relationships).
		 * Preferred: use `updateDecisionNodeData`
		 * @param node
		 * @param {Object} newData: data to update node
		 */
		this.updateNodeData = function(node, newData){
			node.data = newData;
			this.update(node);
		};

		/**
		 * @summary Update the existing node, and child data while preserving decision nodes relationship with parent.
		 * @param node
		 * @param {Object} newData: data to update node, including newData.children
		 */
		this.updateDecisionNodeData = function(node, newData){

			// parent, only update name, property
			node.data.name = newData.name;

			// if(!node.data.children) node.data.children = [];

			// // set rules
			// // if(newData.rules) node.data.rules = newData.rules;

			// // FALSEY child
			// node.data.children[0].name = newData.children[0].name;
			// // // decision
			// // if(node.data.children[0].hasOwnProperty('rules')) node.data.children[0].rules = newData.children[0].rules || null;
			// // leaf
			// if(newData.children[0].branch) node.data.children[0].branch = newData.children[0].branch;

			// // TRUTHY child
			// node.data.children[1].name = newData.children[1].name;
			// // // decision
			// // if(node.data.children[1].hasOwnProperty('rules')) node.data.children[1].rules = newData.children[1].rules || null;
			// // leaf
			// if(newData.children[1].branch) node.data.children[1].branch = newData.children[1].branch;

			this.update(node);
			this.setHighlighted(node, true);
			_broadcastNode(node);
		};

		/**
		 * Add newChildren to the original leaf node, which becomes a decision node.
		 * @param originalNode
		 * @param newChildren
		 */
		this.addChildNodes = function(originalNode, newChildren){

			// you can only add child nodes to a leaf
			if(!originalNode || originalNode.children && originalNode.children.length == 2) return;
			let d = newChildren;
			// newChildren.forEach(function(d){
				
				// Creates a Node from newNode object, see https://github.com/d3/d3-hierarchy
				console.log("Add node with data: ", newChildren);
				var newNode = d3.hierarchy(d);

				// setup the nodes properties
				// depth zero for the root node, and increasing by one for each descendant generation.
				newNode.depth = originalNode.depth + 1;
				// zero for leaf nodes, and the greatest distance from any descendant leaf for internal nodes
				// TODO this is not working?
				newNode.height = originalNode.height - 1;
				//the parent node, or null for the root node.
				newNode.parent = originalNode;
				// uid
				newNode.id  =d.id;

				// If no child array, create an empty array
				if(!originalNode.children){
					originalNode.children = [];
				}
				if(!originalNode.data){
					originalNode.data = {};
					
				}

				if(!originalNode.data.children){
					originalNode.data.children = [];
				}
				console.log("==>New node : ", newChildren);
				// Push it to parent.children array
				if(d.branch === "YES"){
					originalNode.children.splice(0, 0, newNode);
					originalNode.data.children.splice(0, 0,newNode.data);
				}else{
					originalNode.children.splice(1, 0, newNode);
					originalNode.data.children.splice(1, 0,newNode.data);
				}
				// originalNode.children.push(newNode);
				// originalNode.data.children.push(newNode.data);

			// });

			this.update(originalNode);
			this.setHighlighted(originalNode, true);
			_broadcastNode(originalNode);
		};

		this.centerNode = function(source) {
			let t = d3.zoomTransform(svgHandle.node());
			let x = -source.y0;
			let y = -source.x0;
			x = x * t.k + self.options.layout.svgWidth / 2;
			y = y * t.k + self.options.layout.svgHeight / 2;
			svgHandle.transition().duration(duration)
				.call( zoom.transform, d3.zoomIdentity.translate(x,y).scale(t.k) );
		};

		// Collapse the node and all it's children
		this.collapse = function(d) {
			if (d.children) {
				d._children = d.children;
				d._children.forEach(self.collapse);
				d.children = null
			}
		};

		this.expand = function(d) {
			if (d._children) {
				d.children = d._children;
				d.children.forEach(self.expand);
				d._children = null
			}
		};

		this.collapseAll = function(){
			this.root.children.forEach(self.collapse);
			this.update(root);
		};

		this.expandAll = function(){
			this.root.children.forEach(self.expand);
			this.update(root);
		};

		let _previousNode;
		this.setHighlighted = function(node, ignoreToggle){

			// clear highlighting
			d3.selectAll(".node").select('rect')
				.style("fill", function(d){
					return (!d._children && !d.children) ? "#CCC" : "#FFF";
				});

			// highlight target node
			if(node && (node.id != _previousNode) || node.id && ignoreToggle){
				d3.select("#node-"+node.id).select('rect')
					.style("fill", self.options.colors && self.options.colors.nodeHighlight || "#2199e8");
				_previousNode = node.id;
				return true;
			}

			_previousNode = null;
			return false;
		};

		/**
		 * Auto fit zoom to bounds of the treen nodes,
		 * thanks to http://bl.ocks.org/TWiStErRob/raw/b1c62730e01fe33baa2dea0d0aa29359/
		 * @param paddingPercent
		 * @param transitionDuration
		 */
		this.fitBounds = function(paddingPercent, transitionDuration){

			var bounds = treePanel.node().getBBox();
			var parent = treePanel.node().parentElement;
			var fullWidth = parent.clientWidth,
				fullHeight = parent.clientHeight;
			var width = bounds.width,
				height = bounds.height;
			var midX = bounds.x + width / 2,
				midY = bounds.y + height / 2;
			if (width == 0 || height == 0) return; // nothing to fit
			var scale = (paddingPercent || 0.75) / Math.max(width / fullWidth, height / fullHeight);
			var translate = [fullWidth / 2 - scale * midX, fullHeight / 2 - scale * midY];

			svgHandle
				.transition()
				.duration(transitionDuration || 0) // milliseconds
				.call( zoom.transform, d3.zoomIdentity.translate( translate[0], translate[1] ).scale(scale) );

		};

		/**
		 * Adjust the current view position by given xy offset, animated by duration if supplied.
		 * @param offset
		 */
		this.adjustBounds = function(offset){
			svgHandle
				.transition()
				.duration(offset.duration || 0) // milliseconds
				.call(zoom.translateBy, offset.x, offset.y);
		};

		this.update = function(source) {

			// Assigns the x and y position for the nodes
			this.treeData = this.treemap(this.root);

			// Compute the new tree layout.
			this.nodes = this.treeData.descendants();
			this.links = this.treeData.descendants().slice(1);

			// Normalize for fixed-depth.
			this.nodes.forEach(function (d) {
				d.y = d.depth * nodeMargin.y;
			});

			// ****************** Nodes section ***************************

			// Update the nodes...
			let node = svg.selectAll('g.node')
				.data(self.nodes, function (d) {
					return d.id || (d.id = ++nodeIndex);
				});

			// Enter any new nodes at the parent's previous position.
			let nodeEnter = node.enter().append('g')
				.attr('class', 'node')
				.attr("id", function(d){ return "node-"+d.id; })
				.attr("transform", function (d) {
					if(source.x0) return "translate(" + source.x0 + "," + source.y0 + ")";
				})
				.on('click', _click)
				.on('dblclick', _dblclick);

			// RECT NODES

			let nodeRects = node.selectAll("rect.node-rect");

			// add any new rect nodes
			nodeEnter.append("rect")
				.attr("width", nodeWidth / 2)
				.attr("class", function(d){

					// decision
					if(d.data.children){
						return "node-rect";
					}
					// truthy child
					// else if(d.parent && d.parent.children[1] && d.parent.children[1].data.name == d.data.name){
					if(d.data.branch === "NO") {
						return "node-rect falsey-node";
					}
					// falsey child
					else {
						return "node-rect truthy-node";
					}
				})
				.attr("height", function(d){
					return (!d._children && !d.children) ? nodeHeight / 3 : nodeHeight / 2;
				})
				.attr("transform", function (d) {
					return (!d._children && !d.children) ? "" : "rotate(45)";
				})
				.attr("x", -(nodeWidth / 4))
				.attr("y", function(d){
					return (!d._children && !d.children) ? -(nodeHeight / 5.5) : -(nodeHeight / 4);
				})
				.attr("stroke", "black")
				.attr("stroke-width", 2)
				.attr('cursor', 'pointer')
				.style("fill", function (d) {
					return (!d._children && !d.children) ? "#CCC" : "#FFF";
				});

			// we also need to trigger an update for the other nodes because the addition of
			// new nodes means there's a leaf that needs to update to a decision node
			nodeRects.attr("height", function(d){
					return (!d._children && !d.children) ? nodeHeight / 3 : nodeHeight / 2;
				})
				.attr("transform", function (d) {
					return (!d._children && !d.children) ? "" : "rotate(45)";
				})
				.attr("x", -(nodeWidth / 4))
				.attr("y", function(d){
					return (!d._children && !d.children) ? -(nodeHeight / 5.5) : -(nodeHeight / 4);
				})
				.attr("class", function(d){
					// decision
					if(d.data.children){
						return "node-rect";
					}
					// truthy child
					// else if(d.parent && d.parent.children[1] && d.parent.children[1].data.name == d.data.name){
					if(d.data.branch === "NO") {	
						return "node-rect falsey-node";
					}
					// falsey child
					else {
						return "node-rect truthy-node";
					}
				})
				.style("fill", function (d) {
					return (!d._children && !d.children) ? "#CCC" : "#FFF";
				});


			// edit node names if exist and no new nodes
			let rectLabel = node.selectAll("text.node-name");
			if(!rectLabel.empty() && rectLabel.size() == this.nodes.length){
				rectLabel.text(function (d) {
					return d.data.name;
				})
			}
			else {
				// add new node names
				nodeEnter.append('text')
					.attr("dy", ".35em")
					.attr("class", "node-name")
					.attr("text-anchor", "middle")
					.text(function (d) {
						return d.data.name;
					});
			}

			// add new link labels
			nodeEnter.append('text')
				.attr("dy", ".65em")
				.attr("class", "link-label")
				.attr("x", function (d) {
					if(d.parent){
						return (d.parent.x - d.x) / 2;
					}
				})
				.attr("y", function (d) {
					if(d.parent){
						return (d.parent.y - d.y) / 2;
					}
				})
				.attr("text-anchor", "middle")
				.text(function (d) {
					// not root
					if(d.parent) {
						// falsey child
						// if(d.parent  && d.parent.children[1] && d.parent.children[1].data.name == d.data.name){
						if(d.data.branch === "NO") {
							return "NO";
						}
						// truthy child
						else {
							return "YES";
						}
					}
				});

			// update existing link text and position
			// this could be improved by only calling this when we need to..
			var linkLabel = node.selectAll("text.link-label");
			linkLabel.text(function (d) {
					// not root
					if(d.parent) {
						// falsey child
						// if(d.parent && d.parent.children[1] && d.parent.children[1].data.name == d.data.name){
						if(d.data.branch === "NO") {	
							return "NO";
						}
						// truthy child
						else {
							return "YES";
						}
					}
				})
				.attr("x", function (d) {
					if(d.parent){
						return (d.parent.x - d.x) / 2;
					}
				})
				.attr("y", function (d) {
					if(d.parent){
						return (d.parent.y - d.y) / 2;
					}
				});

			// UPDATE
			let nodeUpdate = nodeEnter.merge(node);

			// Transition to the proper position for the node
			nodeUpdate.transition()
				.duration(duration)
				.attr("transform", function (d) {
					return "translate(" + d.x + "," + d.y + ")";
				});

			// RECT
			// Update the node attributes and style
			nodeUpdate.select('rect.node')
				.attr("width", nodeWidth)
				.attr("height", nodeHeight)
				.style("fill", function (d) {
					return d._children ? "lightsteelblue" : "#fff";
				})
				.attr('cursor', 'pointer');

			// Remove any exiting nodes
			let nodeExit = node.exit().transition()
				.duration(duration)
				.attr("transform", function (d) {
					return "translate(" + source.x + "," + source.y + ")";
				})
				.remove();

			// On exit reduce the node rect size to 0
			nodeExit.select('rect')
				.attr("width", 0)
				.attr("height", 0);

			// On exit reduce the opacity of text labels
			nodeExit.select('text')
				.style('fill-opacity', 1e-6);

			// ****************** links section ***************************

			// Update the links...
			let link = svg.selectAll('path.link')
				.data(self.links, function (d) {
					return d.id;
				}).attr("class", function(d){
					// truthy child
					if(d.parent && d.parent.children[1] && d.parent.children[1].data.name == d.data.name){
						return "link falsey-link";
					}
					// falsey child
					else {
						return "link truthy-link";
					}
				});

			// Enter any new links at the parent's previous position.
			let linkEnter = link.enter().insert('path', "g")
				.attr("class", function(d){
					// truthy child
					// if(d.parent && d.parent.children[1] && d.parent.children[1].data.name == d.data.name){
					if(d.data.branch === "NO") {
						return "link falsey-link";
					}
					// falsey child
					else {
						return "link truthy-link";
					}
				})
				.attr('d', function (d) {
					// if no previous pos, just use zero
					if(!source.x0) return  _diagonal({x: 0, y: 0}, {x: 0, y: 0});
					let o = {x: source.x0, y: source.y0};
					return _diagonal(o, o)
				});

			// UPDATE
			let linkUpdate = linkEnter.merge(link);

			// Transition back to the parent element position
			linkUpdate.transition()
				.duration(duration)
				.attr('d', function (d) {
					return _diagonal(d, d.parent)
				});

			// Remove any exiting links
			let linkExit = link.exit().transition()
				.duration(duration)
				.attr('d', function (d) {
					let o = {x: source.x, y: source.y};
					return _diagonal(o, o)
				})
				.remove();

			// Store the old positions for transition.
			self.nodes.forEach(function (d) {
				d.x0 = d.x;
				d.y0 = d.y;
			});

			function _diagonal(s, d) {

				let path = `M ${s.x} ${s.y}
				            C ${(s.x + d.x) / 2} ${s.y},
				              ${(s.x + d.x) / 2} ${d.y},
				              ${d.x} ${d.y}`;

				return path
			}

			function _click(d) {
				let active = self.setHighlighted(d, false);
				_broadcastNode(active ? d : null);
			}

			function _dblclick(d) {
				let active = self.setHighlighted(d, false);
				_broadcastNode2(active ? d : null, true);
			}

		};

		function _broadcastNode(node){
			var evt = new CustomEvent('nodeClick', { detail: node});
			window.dispatchEvent(evt);
		}

		function _broadcastNode2(node, dblclick){
			if(node){
				var evt = new CustomEvent('nodeClick', { detail: { node, action: dblclick ? "configNode" : undefined }});
			// console.log("Dbclick ", evt);

				window.dispatchEvent(evt);
			}
			
		}


		/* -------------------------- Private methods --------------------------------*/

		/**
		 * @summary A recursive helper function for performing some setup by walking through all nodes
		 * @param parent
		 * @param visitFn
		 * @param childrenFn
		 */
		function _visit(parent, visitFn, childrenFn) {
			if (!parent) return;

			visitFn(parent);

			var children = childrenFn(parent);
			if (children) {
				var count = children.length;
				for (var i = 0; i < count; i++) {
					_visit(children[i], visitFn, childrenFn);
				}
			}
		}

		/**
		 * Return an array of children, without those to toRemove (similar to underscores _.without)
		 * @param children
		 * @param toRemove
		 * @returns {*}
		 * @private
		 */
		function _filterChildren(children, toRemove){

			for(var i = children.length - 1; i >= 0; i--){
				for(var j = 0; j < toRemove.length; j++){
					if(children[i] && ( children[i].id === toRemove[j].id )){
						children.splice(i, 1);
					}
				}
			}

			return children;

		}

		this.update(this.root);


		setTimeout(() => {
			self.fitBounds(0.70, 0);
		}, 1000);

	};


	/*------------------------------ behold the state of js modules.. -------------------------------*/

	// AMD support
	if (typeof define === 'function' && define.amd) {
		define(function () { return DecisionTreeBuilder; });
	}

	// CommonJS and Node.js module support.
	else if (typeof exports !== 'undefined') {

		// Support Node.js specific `module.exports` (which can be a function)
		if (typeof module !== 'undefined' && module.exports) {
			exports = module.exports = DecisionTreeBuilder;
		}
		// But always support CommonJS module 1.1.1 spec (`exports` cannot be a function)
		exports.DecisionTreeBuilder = DecisionTreeBuilder;
	}

	// stick it in the window
	else {
		window.DecisionTreeBuilder = DecisionTreeBuilder;
	}

})(this);