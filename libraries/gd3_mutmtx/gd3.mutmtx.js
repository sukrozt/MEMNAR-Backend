! function() {

	var gd3 = {
		version: "0.2.1"
	};

	gd3.dispatch = d3.dispatch("sample", "interaction", "sort", "filterCategory", "filterType", "mutation", "filterMutationType");

	/****************************************************************************
	 *  						gd3 core libraries 
	 ****************************************************************************/

	// ============================================================
	//  						class.js 
	// ============================================================
	function gd3_class(ctor, properties) {
		try {
			for (var key in properties) {
				Object.defineProperty(ctor.prototype, key, {
					value: properties[key],
					enumerable: false
				});
			}
		} catch (e) {
			ctor.prototype = properties;
		}
	}

	// ============================================================
	//  						color.js 
	// ============================================================

	gd3.color = {};

	gd3.color.noData = '#eeeeee';

	gd3.color.categoryPalette;
	gd3.color.annotationPalettes = {};
	gd3.color.annotationToType = {};

	gd3.color.palettes = {};

	// colorbrewer paired qualitative paired scale with modified 2 and 1 element versions
	// Color blind safe!
	gd3.color.palettes.categorical_cbSafe = {
		1: ["#1f78b4"],
		2: ["#1f78b4", "#b2df8a"],
		3: ["#a6cee3", "#1f78b4", "#b2df8a"],
		4: ["#a6cee3", "#1f78b4", "#b2df8a", "#33a02c"]
	};

	// colorbrewer paired qualitative paired scale, but above range of colorblind friendly
	// Even though the two use the same scale, they are separated for clarity
	gd3.color.palettes.categorical = {
		5: ["#a6cee3", "#1f78b4", "#b2df8a", "#33a02c", "#fb9a99"],
		6: ["#a6cee3", "#1f78b4", "#b2df8a", "#33a02c", "#fb9a99", "#e31a1c"],
		7: ["#a6cee3", "#1f78b4", "#b2df8a", "#33a02c", "#fb9a99", "#e31a1c", "#fdbf6f"],
		8: ["#a6cee3", "#1f78b4", "#b2df8a", "#33a02c", "#fb9a99", "#e31a1c", "#fdbf6f", "#ff7f00"],
		9: ["#a6cee3", "#1f78b4", "#b2df8a", "#33a02c", "#fb9a99", "#e31a1c", "#fdbf6f", "#ff7f00", "#cab2d6"],
		10: ["#a6cee3", "#1f78b4", "#b2df8a", "#33a02c", "#fb9a99", "#e31a1c", "#fdbf6f", "#ff7f00", "#cab2d6", "#6a3d9a"],
		11: ["#a6cee3", "#1f78b4", "#b2df8a", "#33a02c", "#fb9a99", "#e31a1c", "#fdbf6f", "#ff7f00", "#cab2d6", "#6a3d9a", "#ffff99"],
		12: ["#a6cee3", "#1f78b4", "#b2df8a", "#33a02c", "#fb9a99", "#e31a1c", "#fdbf6f", "#ff7f00", "#cab2d6", "#6a3d9a", "#ffff99", "#b15928"]
	};

	gd3.color.palettes.annotation_discrete = [
		["#ad494a", "#a55194", "#8ca252", "#8c6d31", "#843c39", "#393b79", "#7b4173", "#637939", "#e7ba52", "#bd9e39", "#cedb9c", "#ce6dbd", "#d6616b", "#9c9ede", "#b5cf6b", "#5254a3", "#e7969c", "#6b6ecf", "#e7cb94", "#de9ed6"],
		["#fd8d3c", "#31a354", "#9e9ac8", "#969696", "#756bb1", "#3182bd", "#636363", "#e6550d", "#a1d99b", "#74c476", "#fdd0a2", "#bdbdbd", "#bcbddc", "#c6dbef", "#fdae6b", "#6baed6", "#dadaeb", "#9ecae1", "#c7e9c0", "#d9d9d9"]
	];

	// These default to the colorbrewer sequential, single-hue palettes
	// The blue scale has been discluded because of its use for the heatmap chart
	// Additionally, the ordering of scales is made to be as colorblind-friendly as possible
	gd3.color.palettes.annotation_continuous = [
		['rgb(247,252,245)', 'rgb(0,68,27)'],
		['rgb(252,251,253)', 'rgb(63,0,125)'],
		['rgb(240,240,240)', 'rgb(0,0,0)'],
		['rgb(255,245,235)', 'rgb(127,39,4)'],
		['rgb(255,245,240)', 'rgb(103,0,13)']
	];

	// The behavior for annotations is as follows:
	// annotations() : return the annotation palette object
	// annotations(key) : return the annotation palette object key's value
	// annotation(key, data) : set the annotation key's palette to have a domain of data
	//   --> The scale will default to discrete, unless data.length == 2 && typeof(each datum) == Number
	// annotation(key, data, type) : as before, except hardcode scale as "discrete" or "continuous"
	// annotation(key, data, type, colors) : as before, except hardcode in palette colors
	gd3.color.annotations = function() {
		if (arguments.length == 0) return gd3.color.annotationPalettes;
		if (arguments.length == 1) return gd3.color.annotationPalettes[arguments[0]];
		// Else, expect two arguments where the first is the name and the second is the type
		if (Object.prototype.toString.call(arguments[1]) !== '[object Array]') {
			throw 'annotations() must be passed: (1) the annotation name, (2) an array of annotation values' + ' OR the range of values, (3) [optionally] a string declaring if the data is "discrete"' + ' or "continuous"';
		}
		if (arguments.length > 2 && arguments[2] != "discrete" && arguments[2] != "continuous") {
			throw 'annotations() third argument must either be "discrete" or "continuous"';
		}

		var scale;

		var annotation = arguments[0],
			data = arguments[1];

		// Assign scale type
		var type;
		if (arguments.length > 2) type = arguments[2];
		else if (data.length == 2 && typeof(data[0]) === 'number' && typeof(data[1]) === 'number') type = 'continuous';
		else type = 'discrete';

		gd3.color.annotationToType[annotation] = type;

		// Define the type of scale and the domain
		if (type == 'continuous') {
			scale = d3.scale.linear().domain([d3.min(data), d3.max(data)]);
		} else {
			scale = d3.scale.ordinal().domain(data);
		}

		// Define the color scale range of the annotation
		var colors;
		if (arguments.length > 3) {
			if (Object.prototype.toString.call(arguments[3]) !== '[object Array]') {
				throw 'annotations()\'s third argument must be an array of colors you wish to use in your annotation scale';
			}
			colors = arguments[3];
		} else {
			var numOfType = Object.keys(gd3.color.annotationPalettes).filter(function(d) {
					return gd3.color.annotationToType[d] == type;
				}).length, // # of previously defined of this type of scale
				palettes = gd3.color.palettes;

			var paletteIndex;
			if (type == 'discrete') {
				paletteIndex = (numOfType + 1) % palettes.annotation_discrete.length;
			} else {
				paletteIndex = (numOfType + 1) % palettes.annotation_continuous.length;
			}

			var palette = (type == 'discrete' ? palettes.annotation_discrete : palettes.annotation_continuous)[paletteIndex];

			colors = palette;
		}
		scale.range(colors);

		// Define the annotation scale in the annotationPalettes object
		gd3.color.annotationPalettes[annotation] = scale;
	}

	// Create a palette for category data (e.g., cancer type) given the categories
	//  or given categories and colors
	// If no arguments are given, the function returns the current palette
	gd3.color.categories = function() {
		function isArrayTest() {
			for (var i = 0; i < arguments.length; i++) {
				var a = arguments[i];
				if (Object.prototype.toString.call(a) !== '[object Array]') {
					throw 'categories() must be passed: (1) an array of categories, (2) an array of categories' + ' and an array of colors';
				}
				if (a.length == 0) throw 'categories() must be passed non-empty arrays for arguments';
			}
		}

		if (arguments.length == 0) return gd3.color.categoryPalette;
		else if (arguments.length == 1) {
			var categories = arguments[0];
			isArrayTest(categories);

			var colors;
			if (categories.length < 5) {
				colors = gd3.color.palettes.categorical_cbSafe[categories.length];
			} else if (categories.length < 13) {
				colors = gd3.color.palettes.categorical[categories.length];
			} else {
				colors = d3.scale.category20().range();
			}

			gd3.color.categoryPalette = d3.scale.ordinal().domain(categories).range(colors);
		} else if (arguments.length > 1) {
			var categories = arguments[0],
				colors = arguments[1];

			isArrayTest(categories, colors);
			gd3.color.categoryPalette = d3.scale.ordinal().domain(categories).range(colors);
		}

		return gd3.color.categoryPalette;
	}

	// ============================================================
	//  				dataStructures.js 
	// ============================================================

	var gd3_data_structures = {
		UnionFind: function() {
			// Instance variables
			var weights = {},
				parents = {};

			// Find and return the name of the set containing the object
			function get(x) {
				// check for previously unknown object
				if (!(x in parents)) {
					parents[x] = x;
					weights[x] = 1;
					return x;
				} else {
					// find path of objects leading to the root
					var path = [x],
						root = parents[x],
						count = 0;

					while (root != path[path.length - 1] && count <= 15) {
						path.push(root);
						root = parents[root];
						count++;
					}

					// compress the path and return
					path.forEach(function(ancestor) {
						parents[ancestor] = root;
					});

					return root;
				}
			}

			// Find the sets containing the objects and merge them all
			function union(xs) {
				// Convert xs to a list if it isn't one already
				if (xs.constructor != Array) {
					xs = [xs];
				}

				// Merge all sets containing any x in xs
				var roots = xs.map(get),
					heaviest = d3.max(roots.map(function(r) {
						return [weights[r], r];
					}))[1];

				roots.forEach(function(r) {
					if (r != heaviest) {
						weights[heaviest] += weights[r];
						parents[r] = heaviest;
					}
				});
			}

			// Return a list of lists containing each group
			function groups() {
				var groupIndex = 0,
					groupToIndex = {},
					currentGroups = [
						[]
					];

				Object.keys(parents).forEach(function(n) {
					var group = get(n);
					if (!(group in groupToIndex)) groupToIndex[group] = groupIndex++;
					if (currentGroups.length <= groupToIndex[group]) currentGroups.push([]);
					currentGroups[groupToIndex[group]].push(+n);
				});

				return currentGroups;
			}

			return {
				get: get,
				union: union,
				groups: groups
			};
		}
	}

	// ============================================================
	//  				util.js 
	// ============================================================

	var gd3_util = {
		arraysEqual: function(a, b) {
			if (a === b) return true;
			if (a == null || b == null) return false;
			if (a.length != b.length) return false;

			// If you don't care about the order of the elements inside
			// the array, you should sort both arrays here.

			for (var i = 0; i < a.length; ++i) {
				if (a[i] !== b[i]) return false;
			}
			return true;
		},
		arrayToSet: function(a) {
			var seen = {};
			return a.filter(function(item) {
				return seen.hasOwnProperty(item) ? false : (seen[item] = true);
			});
		},
		allPairs: function(xs) {
			var n = xs.length,
				pairs = [];

			for (var i = 0; i < n; i++) {
				for (var j = i + 1; j < n; j++) {
					pairs.push([xs[i], xs[j]]);
				}
			}
			return pairs;
		},
		selectionSize: function(selection) {
			var n = 0;
			selection.each(function() {
				++n;
			});
			return n;
		}
	}



	/****************************************************************************
	 *  						gd3 mutmtx library  
	 ****************************************************************************/


	// ============================================================
	//  				mutmtx.js 
	// ============================================================

	gd3.mutationMatrix = function(params) {
		var params = params || {},
			style = mutmtxStyle(params.style || {});

		// mutmtxChart functions as a partial application, binding the given variables
		//   into the returned instance.
		return mutmtxChart(style);
	};

	// ============================================================
	//  				mutmtxChart.js 
	// ============================================================

	function mutmtxChart(style) {
		var categoriesToFilter = [],
			drawHoverLegend = false,
			drawLegend = false,
			showSortingMenu = false,
			drawCoverage = true,
			drawColumnLabels = true,
			showColumnCategories = true,
			linkRowLabelsToNCBI = true,
			stickyLegend = false,
			typesToFilter = [];

		var sortingOptionsData = [
			'First active row',
			'Column category',
			'Exclusivity',
			'Name'
		];

		function chart(selection) {
			selection.each(function(data) {
				data = mutmtxData(data);

				var height = style.height,
					width = style.width;

				// Determine coloration
				var d3color = d3.scale.category20(),
					colCategoryToColor = {},
					datasets = data.get('datasets');

				for (var i = 0; i < datasets.length; i++) {
					colCategoryToColor[datasets[i]] = d3color(i);
				}

				// Select the svg element, if it exists.
				var svg = d3.select(this)
					.selectAll('svg')
					.data([data])
					.enter()
					.append('svg');

				svg.attr('id', 'mutation-matrix')
					.attr('width', width)
					.attr('height', height + style.labelHeight)
					.attr('xmlns', 'http://www.w3.org/2000/svg');

				// Append the matrix/cell rendering area. This needs to be done this early
				//    for z-indexing purposes
				var matrix = svg.append('g');

				var rowLabelsG = svg.append('g')
					.attr('class', 'mutmtx-rowLabels'),
					rowLabels = rowLabelsG.selectAll('text')
					.data(data.get('labels').rows)
					.enter()
					.append('text')
					.attr('text-anchor', 'end')
					.attr('x', 0)
					.attr('y', function(d, i) {
						return style.rowHeight * data.labels.rows.indexOf(d) + style.rowHeight - 3
					})
					.style('font-family', style.fontFamily)
					.style('font-size', style.fontSize)
					.text(function(d) {
						return d;
					})

				// Link out row labels to NCBI
				if (linkRowLabelsToNCBI) {
					rowLabels.style('cursor', 'pointer')
						.on("click", function(d) {
							var gene = d.split(" (")[0];
							window.open('http://www.ncbi.nlm.nih.gov/gene/?term=(' + gene.toLowerCase() + '%20%5Bsym%5D%20AND%20human%20%5Borganism%5D)', '_blank');
						});
				}

				var maxTextWidth = -Infinity;
				rowLabels.each(function() {
					maxTextWidth = d3.max([maxTextWidth, this.getComputedTextLength()]);
				});

				if (data.annotations) {
					var names = Object.keys(data.annotations.sampleToAnnotations),
						categories = data.annotations.categories;

					var annRowLabelsG = svg.append('g').attr('class', 'mutmtx-annRowLabels')
						.attr('transform', 'translate(0,' + rowLabelsG.node().getBBox().height + ')');

					var annRowLabels = annRowLabelsG.selectAll('text')
						.data(categories)
						.enter()
						.append('text')
						.attr('text-anchor', 'end')
						.attr('y', function(d, i) {
							return (i + 1) * style.annotationRowHeight + (i + 1) * style.annotationRowSpacing;
						})
						.style('font-family', style.fontFamily)
						.style('font-size', style.annotationRowHeight)
						.text(function(d) {
							return d;
						});

					annRowLabels.each(function() {
						maxTextWidth = d3.max([maxTextWidth, this.getComputedTextLength()]);
					});

				}

				// Adjust the label width to minimize the label area and maximize matrix area
				rowLabels.attr('x', maxTextWidth)
				style.labelWidth = Math.ceil(maxTextWidth) + 5;

				style.matrixWidth = width - style.labelWidth;

				// Add horizontal rules to the table
				/*var rowNames = data.get('labels').rows,
					rowRules = svg.append('g')
					.attr('class', 'mutmtxRowRules')
					.selectAll('line')
					.data(rowNames)
					.enter()
					.append('line')
					.attr('x1', style.labelWidth)
					.attr('x2', style.labelWidth + style.matrixWidth)
					.attr('y1', function(d, i) {
						return style.rowHeight * rowNames.indexOf(d) + style.rowHeight
					})
					.attr('y2', function(d, i) {
						return style.rowHeight * rowNames.indexOf(d) + style.rowHeight
					})
					.style('stroke-width', '.5px')
					.style('stroke', '#ddd');*/
				var rowNames = '';

				data.reorderColumns();

				var wholeVisX = d3.scale.linear()
					.domain([0, data.get('labels').columns.length])
					.range([style.labelWidth, width]);

				var columnsG = matrix.append('g')
					.attr('class', '.mutmtxColumnsGroup');
				var columns = columnsG.selectAll('g')
					.data(data.get('ids').columns)
					.enter()
					.append('g')
					.attr('class', 'mutmtxColumn')
					.attr('id', function(d) {
						return d.key;
					})
					.attr('transform', function(d, i) {
						return 'translate(' + wholeVisX(i) + ',0)';
					});

				// adjust height based on column height
				svg.attr('height', function(d) {
					return Math.ceil(rowLabelsG.node().getBBox().height + 10);
				});

				// Render sample annotations should they exist
				if (data.annotations) {
					var annColoring = data.annotations.annotationToColor;
					annRowLabels.attr('x', style.labelWidth - 5)

					// For each coloring see:
					//    If there is a predefined categorical set, do nothing
					//    Elsetherwise define a scale
					Object.keys(annColoring).forEach(function(annotation, i) {
						// If the annotation is already defined, continue
						if (gd3.color.annotations(annotation)) {
							return;
						} else { // Else we need to create an annotation color
							var values = Object.keys(data.annotations.sampleToAnnotations).map(function(key) {
								return data.annotations.sampleToAnnotations[key][i];
							});
							values = d3.set(values).values();

							if (values.length <= 10) gd3.color.annotations(annotation, values, 'discrete');
							else {
								values = values.map(function(v) {
									return +v;
								});
								gd3.color.annotations(annotation, [d3.min(values), d3.max(values)], 'continuous');
							}
						}
					});

					// track the size of each text annotation for svg rescale
					var maxTextHeight = 0;

					// add annotation data for each sample in the matrix
					columns.each(function(annKey) {
						// Get the offset caused by the matrix cells
						var mtxOffset = style.rowHeight * data.ids.rows.length;

						// render annotation data;
						var aGroup = d3.select(this).append('g').attr('id', 'annotation-' + annKey);

						var annotationKey = names.reduce(function(prev, cur, i, array) {
							if (annKey.indexOf(cur) > -1) return cur;
							else return prev;
						}, null);

						var annData;
						// If there isn't annotation data, create place holders
						if (annotationKey == null) {
							annData = data.annotations.categories.map(function(d) {
								return null;
							});
						} else {
							// // Else, there is annotation data and render it as normal
							annData = data.annotations.sampleToAnnotations[annotationKey];
						}

						aGroup.selectAll('rect').data(annData).enter()
							.append('rect')
							.attr('height', style.annotationRowHeight)
							.attr('x', 0)
							.attr('y', function(d, i) {
								var spacing = style.annotationRowSpacing * (i + 1);
								return mtxOffset + spacing + style.annotationRowHeight * i;
							})
							.attr('width', 20)
							.style('fill', function(d, i) {
								if (d == null) return gd3.color.noData;
								var annotation = categories[i];
								return gd3.color.annotations(annotation)(d);
							});

						if (drawColumnLabels) {
							var annTextOffset = annData.length * (style.annotationRowHeight + style.annotationRowSpacing) + style.annotationRowSpacing + mtxOffset;

							/*var annText = aGroup.append('text')
								.attr('x', annTextOffset)
								.attr('text-anchor', 'start')
								.attr('transform', 'rotate(90)')
								.style('font-family', style.fontFamily)
								.style('font-size', style.annotationFontSize)
							 	.text(annotationKey);*/
							 var annText = aGroup.append('text')
								.attr('x', annTextOffset)
								.attr('text-anchor', 'start')
								.attr('transform', 'rotate(90)')
								.style('font-family', style.fontFamily)
								.style('font-size', style.annotationFontSize);	

							// width because of rotation
							var annTextHeight = annText.node().getBBox().width + style.annotationRowSpacing;
							maxTextHeight = d3.max([annTextHeight, maxTextHeight]);
						}

					});

					// Modify the SVG height based on the sample annotations
					var svgHeight = svg.attr('height'),
						numAnnotations = data.annotations.categories.length,
						svgHeight = parseInt(svgHeight) + numAnnotations * (style.annotationRowHeight + style.annotationRowSpacing);

					svg.attr('height', svgHeight + maxTextHeight);
				}

				// Zoom behavior

				/*var zoom = d3.behavior.zoom()
					.x(wholeVisX)
					.scaleExtent([1, 14])
					.on('zoom', function() {
						rerenderMutationMatrix();
					});
				svg.call(zoom);*/

				renderMutationMatrix();
				rerenderMutationMatrix();

				// Add the coverage (if necessary)
				if (drawCoverage) {
					selection.append("p")
						.attr('id', 'coverage-string')
						.style("float", "right")
						.html("<b>Coverage:</b> " + data.coverage());
				}

				// Listen for filtering events
				gd3.dispatch.on('filterCategory.mutmtx', function(d) {
					if (!d || !d.categories) return;

					categoriesToFilter = d.categories.filter(function(s) {
						return data.datasets.indexOf(s) > -1;
					});

					data.hiddenColumns.byCategory = {};

					Object.keys(data.maps.columnIdToCategory).forEach(function(cid) {
						var category = data.maps.columnIdToCategory[cid];
						if (categoriesToFilter.indexOf(category) > -1) {
							data.hiddenColumns.byCategory[cid] = category;
						}
					});

					data.reorderColumns(sortingOptionsData);
					data.recomputeLabels();
					rerenderMutationMatrix();
				});

				gd3.dispatch.on('filterType.mutmtx', function(d) {
					if (!d || !d.types) return;

					typesToFilter = d.types.filter(function(s) {
						return data.types.indexOf(s) > -1;
					});

					data.hiddenColumns.byType = {};

					Object.keys(data.maps.columnIdToTypes).forEach(function(cid) {
						var types = data.maps.columnIdToTypes[cid];
						data.hiddenColumns.byType[cid] = types.every(function(type) {
							return typesToFilter.indexOf(type) > -1;
						});
					});

					data.reorderColumns(sortingOptionsData);
					data.recomputeLabels();
					rerenderMutationMatrix();
				});

				if (drawLegend) drawLegendFn(selection.append('div').style('width', style.width));
				if (drawHoverLegend) {
					var container = selection.append('div'),
						legendHoverHeader = container.append('span')
						.style('cursor', 'pointer')
						.style('font-family', style.fontFamily)
						.style('font-size', style.fontSize + 'px')
						.text('Legend (mouse over)'),
						legend = container.append('div')
						.style('background', '#fff')
						.style('border', '1px solid #ccc')
						.style('padding', '10px')
						.style('position', 'absolute')
						.style('display', 'none')
						.style('visibility', 'hidden')
						.style('z-index', '10001');

					legendHoverHeader.on('click', function() {
						stickyLegend = stickyLegend ? false : true;
						legend.selectAll('*').remove();
						if (stickyLegend) drawHoverLegendFn(legend);
						else legend.style('display', 'none').style('visibility', 'hidden');
					});

					legendHoverHeader.on('mouseover', function() {
							if (stickyLegend) return;
							drawHoverLegendFn(legend);
						})
						.on('mouseout', function() {
							if (stickyLegend) return;
							legend.selectAll('*').remove();
							legend.style('display', 'none')
								.style('visibility', 'hidden');
						});
				}
				if (showSortingMenu) {
					drawSortingMenu();
				} else {
					// Sort samples by the given sorting options data
					gd3.dispatch.on('sort.mutmtx', function(d) {
						data.reorderColumns(d.sortingOptionsData);
						data.recomputeLabels();
						rerenderMutationMatrix(true);
					});
				}

				function drawHoverLegendFn(legend) {
					// make sure the width of the legend is less than the window size
					var legendW = Math.max(document.documentElement.clientWidth, window.innerWidth || 0);
					legendW = legendW - 20 - 20; // - 20 for page space, -20 for div padding
					legendW = legendW < style.width - 20 - 20 ? legendW : style.width - 20 - 20;

					var body = document.body,
						docElement = document.documentElement,
						legendHeaderBounds = legendHoverHeader.node().getBoundingClientRect(),

						clientTop = docElement.clientTop || body.clientTop || 0,
						clientLeft = docElement.clientLeft || body.clientLeft || 0,
						scrollLeft = window.pageXOffset || docElement.scrollLeft || body.scrollLeft,
						scrollTop = window.pageYOffset || docElement.scrollTop || body.scrollTop,

						top = legendHeaderBounds.top + scrollTop - clientTop,
						left = legendHeaderBounds.left + scrollLeft - clientLeft;

					legend.style('left', left)
						.style('top', top + legendHeaderBounds.height + 5)
						.style('display', 'block')
						.style('visibility', 'visible');

					if (stickyLegend) {
						legend.append('span').text('X')
							.style('color', '#aaa')
							.style('cursor', 'pointer')
							.style('float', 'right')
							.style('font-family', style.fontFamily)
							.on('click', function() {
								stickyLegend = false;
								legend.selectAll('*').remove();
								legend.style('display', 'none')
									.style('visibility', 'hidden');
							});
					}

					drawLegendFn(legend.style('width', legendW + 'px'));
				}

				// Legend should be a DIV d3 selection
				function drawLegendFn(legend) {
					legend.style('font-size', style.fontSize + 'px');

					if (showColumnCategories) {
						var columnCategories = legend.append('div')
							.style('min-width', legend.style('width'))
							.style('width', legend.style('width'));

						// Tabulate categories
						var categories = {};
						Object.keys(data.maps.columnIdToCategory).forEach(function(k) {
							categories[data.maps.columnIdToCategory[k]] = null;
						});
						categories = Object.keys(categories).sort();
						var categoryLegendKeys = columnCategories.selectAll('div')
							.data(categories)
							.enter()
							.append('div')
							.style('display', 'inline-block')
							.style('font-family', style.fontFamily)
							.style('font-size', style.fontSize)
							.style('margin-right', function(d, i) {
								return i == categories.length - 1 ? '0px' : '10px';
							})
							.on('click', function(d) {
								var filtering = categoriesToFilter;
								if (categoriesToFilter.indexOf(d) > -1) {
									filtering.splice(filtering.indexOf(d), 1);
									d3.select(this).style('opacity', 1);
								} else {
									filtering.push(d);
									d3.select(this).style('opacity', 0.2);
								}
								gd3.dispatch.filterCategory({
									categories: filtering
								});
							});
						// Append the color blocks
						categoryLegendKeys.append('div')
							.style('background', function(d) {
								if (gd3.color.categoryPalette) return gd3.color.categoryPalette(d);
								return colCategoryToColor[d];
							})
							.style('display', 'inline-block')
							.style('height', style.fontSize + 'px')
							.style('width', (style.fontSize / 2) + 'px');
						categoryLegendKeys.append('span')
							.style('display', 'inline-block')
							.style('margin-left', '2px')
							.text(function(d) {
								return d;
							});
						// Resize the category legend key widths based on max bounding box
						var categoryLegendKeyWidths = [];
						categoryLegendKeys.each(function() {
							var cWidth = this.getBoundingClientRect().width;
							categoryLegendKeyWidths.push(cWidth);
						});
						categoryLegendKeys.style('width', d3.max(categoryLegendKeyWidths) + 'px')
							.style('min-width', d3.max(categoryLegendKeyWidths) + 'px');
					}

					// Tabulate cell type glyphs, if present
					if (Object.keys(data.maps.cellTypeToGlyph).length > 1) {
						var cellTypes = legend.append('div'),
							cellTypesData = Object.keys(data.maps.cellTypeToGlyph);
						var cellTypeLegendKeys = cellTypes.selectAll('div')
							.data(cellTypesData)
							.enter()
							.append('div')
							.style('cursor', 'pointer')
							.style('display', 'inline-block')
							.style('font-family', style.fontFamily)
							.style('font-size', style.fontSize)
							.style('margin-right', function(d, i) {
								return i == cellTypesData.length - 1 ? '0px' : '10px';
							})
							.on('click', function(d) {
								var filtering = typesToFilter;
								if (typesToFilter.indexOf(d) > -1) {
									filtering.splice(filtering.indexOf(d), 1);
									d3.select(this).style('opacity', 1);
								} else {
									filtering.push(d);
									d3.select(this).style('opacity', 0.2);
								}
								gd3.dispatch.filterType({
									types: filtering
								});
							});;

						cellTypeLegendKeys.append('svg')
							.attr('height', function(d) {
								var tickType = data.maps.cellTypeToTick[d];
								if (tickType == 'down' || tickType == 'up') return (style.fontSize / 2) + 'px';
								else return style.fontSize + 'px';
							})
							.attr('width', style.fontSize + 'px')
							.style('background', d3color(0))
							.style('margin-right', '2px')
							.style('margin-bottom', function(d) {
								var tickType = data.maps.cellTypeToTick[d];
								if (tickType == 'up') return (style.fontSize / 2) + 'px';
								else '0px';
							})
							.each(function(type) {
								var glyph = data.maps.cellTypeToGlyph[type]
								if (!glyph || glyph == null) return;

								d3.select(this).append('path')
									.attr('d', function(type) {
										var diameter = style.fontSize - style.fontSize / 2;
										return d3.svg.symbol().type(glyph).size(diameter * diameter)();
									})
									.attr('transform', 'translate(' + (style.fontSize / 2) + ',' + (style.fontSize / 2) + ')')
									.style('fill', style.glyphColor)
									.style('stroke', style.glyphStrokeColor)
									.style('strokew-width', .5)
							});

						cellTypeLegendKeys.append('span')
							.text(function(d) {
								return data.maps.cellTypeToLabel[d];
							});
					}


					if (data.annotations) {
						var annotationLegends = legend.append('div')
							.selectAll('div')
							.data(data.annotations.categories)
							.enter()
							.append('div');

						annotationLegends.each(function(annotationName) {
							var thisEl = d3.select(this),
								scale = gd3.color.annotations(annotationName),
								scaleType = gd3.color.annotationToType[annotationName];

							thisEl.style('font-family', style.fontFamily)
								.style('font-size', style.fontSize);
							thisEl.append('span').text(annotationName + ': ');

							if (scaleType && scaleType == 'continuous') {
								var scaleHeight = style.fontSize,
									scaleWidth = style.fontSize * 5;

								thisEl.append('span').text(scale.min);
								var gradientSvg = thisEl.append('svg')
									.attr('height', scaleHeight)
									.attr('width', scaleWidth)
									.style('margin-left', '2px')
									.style('margin-right', '2px');
								thisEl.append('span').text(scale.domain());
								thisEl.selectAll('*').style('display', 'inline-block');

								// Create a unique ID for the color map gradient in case multiple heatmaps are made
								var now = Date.now(),
									gradientId = 'gd3-mutmtx-gradient' + now;

								// Configure the gradient to be mapped on to the legend
								var gradient = gradientSvg.append('svg:defs')
									.append('svg:linearGradient')
									.attr('id', gradientId)
									.attr('x1', '0%')
									.attr('y1', '0%')
									.attr('x2', '100%')
									.attr('y2', '0%');

								var scaleRange = scale.range();
								scaleRange.forEach(function(c, i) {
									gradient.append('svg:stop')
										.attr('offset', i * 1. / (scaleRange.length - 1))
										.attr('stop-color', c)
										.attr('stop-opacity', 1);
								});

								gradientSvg.append('rect')
									.attr('height', scaleHeight)
									.attr('width', scaleWidth)
									.attr('fill', 'url(#' + gradientId + ')');
							} else {
								var annKeys = thisEl.selectAll('div')
									.data(scale.domain())
									.enter()
									.append('div')
									.style('display', 'inline-block')
									.style('font-family', style.fontFamily)
									.style('font-size', style.fontSize)
									.style('margin-right', function(d, i) {
										return i == Object.keys(scale).length - 1 ? '0px' : '10px';
									});
								annKeys.append('div')
									.style('background', function(d) {
										return scale(d);
									})
									.style('display', 'inline-block')
									.style('height', style.fontSize + 'px')
									.style('width', (style.fontSize / 2) + 'px');
								annKeys.append('span')
									.style('display', 'inline-block')
									.style('margin-left', '2px')
									.text(function(d) {
										return d;
									});
							}
						});
					}

				}


				function drawSortingMenu() {
					var menu = selection.append('div');
					var title = menu.append('p')
						.style('cursor', 'pointer')
						.style('font-family', style.fontFamily)
						.style('font-size', style.fontSize + 'px')
						.style('margin-bottom', '0px')
						.text('Sort columns [+]');

					var optionsMenu = menu.append('ul')
						.style('display', 'none')
						.style('list-style', 'none')
						.style('margin-right', '0px')
						.style('margin-bottom', '0px')
						.style('margin-left', '0px')
						.style('margin-top', '0px')
						.style('padding-left', 0);

					title.on('click', function() {
						var optionsShown = optionsMenu.style('display') == 'block',
							display = optionsShown ? 'none' : 'block',
							visibility = optionsShown ? 'hidden' : 'visible';

						d3.select('p').text('Sort columns ' + (optionsShown ? '[+]' : '[-]'));

						optionsMenu.style('display', display);
						optionsMenu.style('visibility', visibility);
					});

					renderMenu();

					function renderMenu() {
						optionsMenu.selectAll('li').remove();
						var menuItem = optionsMenu.selectAll('li')
							.data(sortingOptionsData)
							.enter()
							.append('li')
							.style('font-family', style.fontFamily)
							.style('font-size', style.sortingMenuFontSize + 'px');

						// Populate each menu item with up/down sort toggles and text
						menuItem.each(function(menuText, menuPosition) {
							var texts = [(menuPosition + 1) + '. ', '↑', ' ', '↓', ' ', ' ', menuText],
								thisLi = d3.select(this);
							thisLi.selectAll('span')
								.data(texts)
								.enter()
								.append('span')
								.text(function(d) {
									return d;
								})
								.each(function(d, i) {
									// Define behavior for voting glyphs
									if (i != 1 && i != 3) return;
									d3.select(this).style('cursor', 'pointer')
										.on('mouseover', function() {
											d3.select(this).style('color', 'red');
										})
										.on('mouseout', function() {
											d3.select(this).style('color', style.fontColor);
										})
										.on('click', function() {
											if (i == 1 && menuPosition == 0) return;
											if (i == 3 && menuPosition == sortingOptionsData.length - 1) return;

											var neighbor = menuPosition + (i == 1 ? -1 : 1),
												neighborText = sortingOptionsData[neighbor];
											sortingOptionsData[neighbor] = menuText;
											sortingOptionsData[menuPosition] = neighborText;
											data.reorderColumns(sortingOptionsData);
											renderMenu();
											rerenderMutationMatrix(true);

											var orderedLabels = data.ids.columns.map(function(d) {
												return data.maps.columnIdToLabel[d];
											});

											gd3.dispatch.sort({
												columnLabels: orderedLabels,
												sortingOptionsData: sortingOptionsData
											});
										});
								});
						});
					}
				}


				function rerenderMutationMatrix(transition) {
					/*var t = zoom.translate(),
						tx = t[0],
						ty = t[1],
						scale = zoom.scale();

					tx = Math.min(tx, 0);

					zoom.translate([tx, ty]);*/

					// Update the row labels with their current counts
					rowLabels.data(data.labels.rows).text(function(d) {
						return d;
					});

					var colWidth = wholeVisX(1) - wholeVisX(0);
					if (transition && transition == true) {
						columns.transition().attr('transform', function(d) {
							var colIndex = data.ids.columns.indexOf(d);
							return 'translate(' + wholeVisX(colIndex) + ',0)';
						});
					} else {
						columns.attr('transform', function(d) {
							var colIndex = data.ids.columns.indexOf(d);
							return 'translate(' + wholeVisX(colIndex) + ',0)';
						});
					}

					// Fade columns that have categories or types in filter lists
					columns.style("opacity", 1);
					columns.filter(function(d) {
						return data.hiddenColumns.byCategory[d] || data.hiddenColumns.byType[d];
					}).style('opacity', 0.0);


					// Fade columns out of the viewport
					columns.filter(function(d) {
						return wholeVisX(data.ids.columns.indexOf(d)) < style.labelWidth;
					}).style("opacity", 0.2);

					// Redraw each cell and any glyphs the cell might have
					columns.selectAll('rect')
						.attr('class', function(d) {
							if (!d || !d.colId) return '';
							else return 'mutmtx-sampleMutationCells label' + data.ids.columns.indexOf(d.colId);
						})
						.attr('width', colWidth);

					columns.selectAll('.gd3mutmtx-cellClyph')
						.attr('transform', function(d) {
							var str = d3.select(this).attr('transform'),
								then = str.replace('translate', '').replace(')', '').split(','),
								x = colWidth / 2,
								y = +then[1],
								now = 'translate(' + x + ',' + y + ')';
							return now;
						})
						.attr('d', function(d) {
							var cellType = d.cell.type,
								glyph = data.maps.cellTypeToGlyph[cellType],
								gWidth = d3.min([colWidth, style.rowHeight - style.rowHeight / 2]);
							return d3.svg.symbol().type(glyph).size(gWidth * gWidth)();
						});

					columns.attr('class', function(d) {
						return 'mutmtxColumn label' + data.ids.columns.indexOf(d);
					});


					// Hide cells that are of a filtered type and/or category
					cells.style("opacity", function(d) {
						var visibleType = typesToFilter.indexOf(d.cell.type) === -1,
							visibleCategory = categoriesToFilter.indexOf(d.cell.dataset) === -1;
						return visibleType && visibleCategory ? 1 : 0;
					})
				}

				var cells;

				function renderMutationMatrix() {
					var colWidth = wholeVisX(1) - wholeVisX(0);

					cells = columns.append('g')
						.attr('class', 'mutmtx-sampleMutationCells')
						.selectAll('g')
						.data(function(colId) {
							var activeRows = data.matrix.columnIdToActiveRows[colId],
								colLabel = data.maps.columnIdToLabel[colId];

							return activeRows.map(function(rowId) {
								var rowLabel = data.maps.rowIdToLabel[rowId];
								return {
									colId: colId,
									row: rowId,
									rowLabel: rowLabel,
									colLabel: colLabel,
									cell: data.matrix.cells[[rowId, colId].join()]
								}
							});
						})
						.enter()
						.append('g');

					// For each cell append a rect and if appropriate a glyph on the rect
					cells.each(function(d) {
						var thisCell = d3.select(this),
							y = style.rowHeight * data.ids.rows.indexOf(d.row);

						thisCell.append('rect')
							.attr('data-column-id', d.colId)
							.attr('x', 0)
							.attr('y', function(d) {
								var tickType = data.maps.cellTypeToTick[d.cell.type];
								if (tickType == 'down') return y + style.rowHeight / 2;
								else return y;
							})
							.attr('height', function(d) {
								var tickType = data.maps.cellTypeToTick[d.cell.type];
								if (tickType == 'up' || tickType == 'down') return style.rowHeight / 2;
								else return style.rowHeight;
							})
							.attr('width', colWidth + 10)
							.style('fill', function() {
								if (gd3.color.categoryPalette) return gd3.color.categoryPalette(d.cell.dataset);
								return colCategoryToColor[d.cell.dataset];
							});

						var cellType = d.cell.type,
							glyph = data.maps.cellTypeToGlyph[cellType];

						if (glyph && glyph != null) {
							thisCell.append('path')
								.attr('class', 'gd3mutmtx-cellClyph')
								.attr('d', d3.svg.symbol().type(glyph).size(colWidth * colWidth))
								.attr('transform', 'translate(' + (colWidth / 2) + ',' + (y + style.rowHeight / 2) + ')')
								.style('fill', style.glyphColor)
								.style('stroke', style.glyphStrokeColor)
								.style('stroke-width', .5);
						}
					});

					///////////////////////////////////////////////////////////////////////
					// Add dispatch to outline mutations in the same sample
					// onmouseover

					// Select the sample names and the mutations, and give each of the
					// mutations a hidden stroke
					var columnNames = columns.selectAll("text");
					var rects = columns.select('g.mutmtx-sampleMutationCells')
						.selectAll("g")
						.selectAll("rect")
						.attr({
							"stroke-width": 1,
							"stroke": "black",
							"stroke-opacity": 0
						})

					// Define the dispatch events
					columns.select('g.mutmtx-sampleMutationCells')
						.selectAll('g')
						.on("mouseover.dispatch-sample", function(d) {
							gd3.dispatch.sample({
								sample: data.maps.columnIdToLabel[d.colId],
								over: true
							});
						}).on("mouseout.dispatch-sample", function(d) {
							gd3.dispatch.sample({
								sample: data.maps.columnIdToLabel[d.colId],
								over: false
							});
						}).on("click.dispatch-mutation", function(d) {
							gd3.dispatch.mutation({
								gene: d.rowLabel,
								dataset: d.cell.dataset,
								mutation_class: d.cell.type == "inactive_snv" ? "snv" : d.cell.type
							})
						});

					gd3.dispatch.sort({
						columnLabels: data.ids.columns.map(function(d) {
							return data.maps.columnIdToLabel[d];
						}),
						sortingOptionsData: sortingOptionsData
					});

					gd3.dispatch.on("sample.mutmtx", function(d) {
						var over = d.over, // flag if mouseover or mouseout
							sample = d.sample,
							columnsAffected = true;

						if (drawColumnLabels) {
							var affectedColumns = columnNames.filter(function(d) {
								return data.maps.columnIdToLabel[d] == sample;
							});

							if (gd3_util.selectionSize(affectedColumns)) {
								// Highlight the sample name
								columnNames.style({
									"opacity": over ? 0.25 : 1,
									"font-weight": "normal"
								});
								affectedColumns.style({
									"opacity": 1,
									"font-weight": over ? "bold" : "normal"
								});
							} else {
								columnsAffected = false;
							}
						}

						// Show the small stroke around each of the sample's mutations
						if (columnsAffected) {
							rects.attr("stroke-opacity", 0);
							rects.filter(function(d) {
								return data.maps.columnIdToLabel[d.colId] == sample;
							}).attr("stroke-opacity", over ? 1 : 0);
						}
					})
				}

				// A getter (which is why it's in the selection.data function)
				// for determining the order in which the samples are sorted
				chart.getOrderedColumnLabels = function(sortingOptionsData) {
					data.reorderColumns(sortingOptionsData);
					var orderedLabels = data.ids.columns.map(function(d) {
						return data.maps.columnIdToLabel[d];
					});
					return orderedLabels;
				}
			});
		}

		chart.showHoverLegend = function(state) {
			drawHoverLegend = state;
			return chart;
		}

		chart.showLegend = function(state) {
			drawLegend = state;
			return chart;
		}

		chart.showCoverage = function(state) {
			drawCoverage = state;
			return chart;
		}

		chart.showColumnLabels = function(state) {
			drawColumnLabels = state;
			return chart;
		}

		chart.showSortingMenu = function(state) {
			showSortingMenu = state;
			return chart;
		}

		chart.showColumnCategories = function(state) {
			showColumnCategories = state;
			return chart;
		}

		chart.linkRowLabelsToNCBI = function(state) {
			linkRowLabelsToNCBI = state;
			return chart;
		}

		return chart;
	}

	// ============================================================
	//  				mutmtxData.js 
	// ============================================================

	function mutmtxData(inputData) {
		var data = {
			datasets: [],
			glyphs: ['square', 'triangle-up', 'cross', 'circle', 'diamond', 'triangle-down'],
			hiddenColumns: {
				byCategory: [],
				byType: []
			},
			ids: {
				columns: [],
				rows: []
			},
			labels: {
				columns: [],
				rows: []
			},
			maps: {
				cellTypeToTick: inputData.cellTypeToTick || {
					snv: 'full',
					amp: 'up',
					del: 'down'
				},
				cellTypeToLabel: inputData.cellTypeToLabel || {
					snv: 'SNV',
					inactive_snv: 'Inactivating SNV',
					amp: 'Amplification',
					del: 'Deletion'
				},
				cellTypeToGlyph: inputData.cellTypeToGlyph || {
					snv: null,
					inactive_snv: 'square'
				},
				cellTypeToSortIndex: inputData.cellTypeToSortIndex || {
					snv: 0,
					inactive_snv: 1,
					del: 2,
					amp: 3
				},
				columnIdToLabel: {},
				columnIdToCategory: {},
				columnIdToTypes: {},
				rowIdToLabel: {}
			},
			matrix: {
				cells: {},
				columnIdToActiveRows: {},
				rowIdToActiveColumns: {}
			},
			types: []
		};

		data.coverage = function() {
			var mutatedSamples = d3.merge(data.ids.rows.map(function(d) {
					return data.matrix.rowIdToActiveColumns[d];
				})),
				numMutatedSamples = d3.set(mutatedSamples).values().length,
				s = ((numMutatedSamples * 100. / data.numSamples).toFixed(2)) + "%";
			return s + " (" + numMutatedSamples + "/" + data.numSamples + ")";
		}

		data.get = function(attr) {
			if (!attr) return null;
			else if (attr === 'datasets') return data.datasets;
			else if (attr === 'ids') return data.ids;
			else if (attr === 'labels') return data.labels;
		}

		data.reorderColumns = function(ordering) {
				// Sort by whether or not the column is visible (i.e., has been filtered)
				function sortByVisibility(c1, c2) {
					var c1Hidden = data.hiddenColumns.byCategory[c1] || data.hiddenColumns.byType[c1] ? true : false,
						c2Hidden = data.hiddenColumns.byCategory[c2] || data.hiddenColumns.byType[c2] ? true : false;

					if (c1Hidden == c2Hidden) return 0;
					else if (c1Hidden) return 1;
					else if (c2Hidden) return -1;
					else return 0;
				}

				// Sort by the column's most common cell type
				function sortByCellType(c1, c2) {
					var c1Type = data.maps.columnIdToTypes[c1][0],
						c2Type = data.maps.columnIdToTypes[c2][0];
					return d3.ascending(data.maps.cellTypeToSortIndex[c1Type], data.maps.cellTypeToSortIndex[c2Type]);
				}
				// Sort by how exclusive each column's mutations are with one another
				function sortByExclusivity(c1, c2) {
					var c1X = data.matrix.columnIdToActiveRows[c1].length > 1,
						c2X = data.matrix.columnIdToActiveRows[c2].length > 1;
					return d3.ascending(c1X, c2X);
				}
				// Sort by which column has more "top" activations in the rendered graphic
				function sortByFirstActiveRow(c1, c2) {
					var c1First = data.matrix.columnIdToActiveRows[c1][0],
						c2First = data.matrix.columnIdToActiveRows[c2][0];
					if (typeof(c1First) == 'undefined') c1First = Number.MAX_VALUE;
					if (typeof(c2First) == 'undefined') c2First = Number.MAX_VALUE;
					return d3.ascending(c1First, c2First);
				}
				// Sort by the name of the column
				function sortByName(c1, c2) {
					var c1Label = data.maps.columnIdToLabel[c1],
						c2Label = data.maps.columnIdToLabel[c2];

					return d3.ascending(c1Label, c2Label);
					//return d3.ascending(data.labels.columns[c1],data.labels.columns[c2]);
				}
				// Sort by the column category (i.e, color)
				function sortByColumnCategory(c1, c2) {
					return d3.ascending(data.maps.columnIdToCategory[c1], data.maps.columnIdToCategory[c2]);
				}

				// Sort the data based on input, or if none, on default ordering
				var sortFns;
				if (ordering) {
					sortFns = [sortByVisibility];
					ordering.forEach(function(d) {
						if (d == 'First active row') sortFns.push(sortByFirstActiveRow);
						if (d == 'Column category') sortFns.push(sortByColumnCategory);
						if (d == 'Exclusivity') sortFns.push(sortByExclusivity);
						if (d == 'Name') sortFns.push(sortByName);
					});
				} else {
					sortFns = [sortByVisibility, sortByFirstActiveRow, sortByColumnCategory, sortByExclusivity, sortByCellType, sortByName];
				}

				data.ids.columns.sort(function(c1, c2) {
					var sortResult;
					for (var i = 0; i < sortFns.length; i++) {
						sortResult = sortFns[i](c1, c2);
						if (sortResult != 0) {
							return sortResult;
						}
					}
					return sortResult;
				});
			} // end data.reorderColumns()

		data.recomputeLabels = function() {
			data.labels.rows = data.labels.rows.map(function(rowLabel) {
				var rowId = rowLabel.split(" (")[0],
					count = Object.keys(inputData.M[rowId]).reduce(function(sum, colId) {
						if (data.hiddenColumns.byCategory[colId] || data.hiddenColumns.byType[colId]) return sum;
						else return sum + 1;
					}, 0);
				return rowId + " (" + count + ")";
			});
		}

		function defaultParse() {
			// Scrape labels from the matrix
			inputData.samples.forEach(function(s) {
				data.maps.columnIdToLabel[s._id] = s.name;
				data.labels.columns.push(s.name);
			});

			// Determine the total number of samples across all types
			if (inputData.typeToSamples && inputData.sampleTypes) {
				data.numSamples = inputData.sampleTypes.reduce(function(total, t) {
					return total + inputData.typeToSamples[t].length;
				}, 0);
			} else {
				data.numSamples = inputData.samples.length;
			}

			var rowAndCount = [];
			if (inputData.ordered_row_labels) {
				inputData.ordered_row_labels.forEach(function(k) {
					rowAndCount.push([k, Object.keys(inputData.M[k]).length]);
				})
			} else {
				Object.keys(inputData.M).forEach(function(k, i) {
					// data.maps.rowIdToLabel[i.toString()] = k;
					var numSamples = Object.keys(inputData.M[k]).length;
					// data.labels.rows.push(k + ' ('+numSamples+')');
					rowAndCount.push([k, numSamples]);
				});

				rowAndCount.sort(function(a, b) {
					return a[1] < b[1] ? 1 : -1;
				});
			}
			var sortedRowIds = [];
			rowAndCount.forEach(function(d, i) {
				var name = d[0],
					numSamples = d[1];
				data.maps.rowIdToLabel[i.toString()] = name;
				data.labels.rows.push(name + ' (' + numSamples + ')');
				sortedRowIds.push(name);
			});


			data.ids.columns = Object.keys(data.maps.columnIdToLabel);
			data.ids.rows = Object.keys(data.maps.rowIdToLabel);

			// Make set of datasets in data
			var setOfDatasets = {};
			Object.keys(inputData.sampleToTypes).forEach(function(colId) {
				setOfDatasets[inputData.sampleToTypes[colId]] = null;
				data.maps.columnIdToCategory[colId] = inputData.sampleToTypes[colId];
			});
			data.datasets = Object.keys(setOfDatasets);

			// Build matrix data and maps
			var cellTypes = [];
			inputData.samples.forEach(function(d) {
				data.matrix.columnIdToActiveRows[d._id] = [];
				data.maps.columnIdToTypes[d._id] = [];
			});

			sortedRowIds.forEach(function(rowLabel, rowId) {
				var columns = Object.keys(inputData.M[rowLabel]);
				rowId = rowId.toString();
				// Add rowId -> columns mapping
				data.matrix.rowIdToActiveColumns[rowId] = columns;
				// Add columnId -> row mapping
				columns.forEach(function(colId) {
					// Add the row to the column
					data.matrix.columnIdToActiveRows[colId].push(rowId);

					// Add cell data
					var type = inputData.M[rowLabel][colId][0];
					data.matrix.cells[[rowId, colId].join()] = {
						dataset: inputData.sampleToTypes[colId],
						type: inputData.M[rowLabel][colId][0]
					};
					cellTypes.push(type);

					// Track the types of cells in the data
					data.maps.columnIdToTypes[colId].push(type);
				});
			}); // end matrix mapping

			// Remove repeat types
			data.types = cellTypes.filter(function(item, pos, self) {
				return self.indexOf(item) == pos;
			});

			// Process the column to type map s.t. there are no repeats and
			//   the map is ordered by population of each type
			Object.keys(data.maps.columnIdToTypes).forEach(function(colId) {
				var types = data.maps.columnIdToTypes[colId],
					typeLog = {};
				types.forEach(function(t) {
					if (!typeLog[t]) typeLog[t] = 0;
					typeLog[t] = typeLog[t] + 1;
				});

				types = Object.keys(typeLog);
				types.sort(function(a, b) {
					return typeLog[a] < typeLog[b];
				});
				data.maps.columnIdToTypes[colId] = types;
			});
			data.types.forEach(function(t) {
				if (!(t in data.maps.cellTypeToTick)) {
					data.maps.cellTypeToTick[t] = 'full';
				}
				if (!(t in data.maps.cellTypeToLabel)) {
					data.maps.cellTypeToLabel[t] = t.replace("_", " ");
				}
			})

			// Load the cell type to glyph mapping if it exists, else create it
			if (inputData.cellTypesToGlyph) {
				data.maps.cellTypeToGlyph = inputData.cellTypeToGlyph;
			} else {
				// Remove duplicates from the cellTypes array
				var typesTmp = {};
				cellTypes.forEach(function(t) {
					if (typesTmp[t] == undefined) typesTmp[t] = 0;
					typesTmp[t] = typesTmp[t] + 1;
				});
				var types = Object.keys(typesTmp).sort(function(a, b) {
					typesTmp[a] > typesTmp[b]
				});

				types.forEach(function(d, i) {
					if (d in data.maps.cellTypeToGlyph) return;
					if (data.maps.cellTypeToTick[d] != 'full') {
						data.maps.cellTypeToGlyph[d] = null;
					} else {
						data.maps.cellTypeToGlyph[d] = data.glyphs[i % data.glyphs.length];
					}
				});
			} // end glyph mapping
		}

		defaultParse();

		// sample annotation data processing, if present
		if (inputData.annotations) {
			data.annotations = inputData.annotations;
		} else {
			data.annotations = {
				categories: [],
				sampleToAnnotations: {},
				annotationToColor: {}
			};
			data.ids.columns.forEach(function(s) {
				data.annotations.sampleToAnnotations[data.maps.columnIdToLabel[s]] = [];
			});
		}

		// create simulated annotation data if it does not exist.
		// Object.keys(data.matrix.cells).forEach(function(key) {
		//   if (data.matrix.cells[key].annotation == undefined) {
		//     var vote = {
		//       type: 'vote',
		//       score: 100
		//     }
		//     var link = {
		//       type: 'link',
		//       href: 'http://www.cs.brown.edu',
		//       text: 'BrownCS'
		//     }
		//     data.matrix.cells[key].annotation = [
		//       {
		//         type: 'text',
		//         title: 'Sample',
		//         text: key
		//       },
		//       {
		//         type: 'table',
		//         header: ['Cancer', 'PMIDs', 'Votes'],
		//         data: [
		//           ['1', link, vote],
		//           ['4', link, vote]
		//         ]
		//       }
		//     ];
		//   }
		// }); // end simulated annotation data

		return data;
	}

	// ============================================================
	//  				mutmtxStyle.js 
	// ============================================================

	function mutmtxStyle(style) {
		return {
			animationSpeed: style.animationSpeed || 300,
			annotationContinuousScale: style.annotationContinuousScale || ['#fcc5c0', '#49006a'],
			annotationFontSize: style.annotationFontSize || 10,
			annotationRowHeight: style.annotationRowHeight || 10,
			annotationRowSpacing: style.annotationRowSpacing || 5,
			bgColor: style.bgColor || '#F6F6F6',
			blockColorMedium: style.blockColorMedium || '#95A5A6',
			blockColorStrongest: style.blockColorStrongest || '#2C3E50',
			boxMargin: style.boxMargin || 5, // assumes uniform margins on all sides
			colorSampleTypes: style.colorSampleTypes || true,
			coocurringColor: style.coocurringColor || 'orange',
			exclusiveColor: style.exclusiveColor || 'blue',
			fontColor: style.fontColor || '#000',
			fontFamily: 'Arial, "HelveticaNeue-Light", "Helvetica Neue Light", "Helvetica Neue", Helvetica, "Lucida Grande", sans-serif',
			fontSize: style.fontSize || 14,
			glyphColor: style.glyphColor || '#888',
			glyphStrokeColor: style.glyphStrokeColor || '#ccc',
			height: style.height || 300,
			rowHeight: style.rowHeight || 20,
			labelHeight: style.labelHeight || 40,
			labelWidth: style.labelWidth || 100,
			minBoxWidth: style.minBoxWidth || 20,
			mutationLegendHeight: style.mutationLegendHeight || 30,
			sampleStroke: style.sampleStroke || 1,
			sortingMenuFontSize: style.sortingMenuFontSize || 12,
			width: style.width || 600,
			zBottom: 0,
			zTop: 100
		};
	}

	// 
	this.gd3 = gd3;
}();