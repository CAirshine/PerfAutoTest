
/**
 * 绘图js方法
 */
function getParamByName(key) {
    var url = decodeURI(decodeURI(location.search)); // 获取url中含"?"符后的字串
    var theRequest = new Object();

    if (url.indexOf("?") != -1) {
        var str = url.substr(1);
        strs = str.split("&");
        for (var i = 0; i < strs.length; i++) {
            theRequest[strs[i].split("=")[0]] = unescape(strs[i].split("=")[1]);
        }
    }
    return theRequest[key];
}

// 解析json数据，获取json所包含的所有单板名称
function getBoards(jsonObj) {
    var boards = [];
    for (i = 0; i < jsonObj['boards'].length; i++) {
        for (var k in jsonObj['boards'][i]) {
            boards[i] = k;
        }
    }

    return boards;
}

// 解析json数据，根据单板名称获取json中包含的性能数据，并用数组保存下来
function getPerfInfoByBoard(jsonObj, board) {

    var infosSize = 0;
    var desc = board;
    var sends = [];
    var receives = [];
    var cpuuseds = [];
    var tpss = [];
    var delays = [];

    for (i = 0; i < jsonObj['boards'].length; i++) {

        // infos是json数组，包含的该单板的性能数据
        var infos = jsonObj['boards'][i][board];
        if (infos != undefined) {
            for (ii = 0; ii < infos.length; ii++) {
                var info = infos[ii];

                sends[ii] = info['send'];
                receives[ii] = info['receive'];
                cpuuseds[ii] = info['cpuused'];
                tpss[ii] = info['tps'];
                delays[ii] = info['delay'];

                infosSize++;
            }
        }
    }

    return {
        'infosSize': infosSize,
        'desc': desc,
        'sends': sends,
        'receives': receives,
        'cpuuseds': cpuuseds,
        'tpss': tpss,
        'delays': delays
    };
}

// 使用getPerfInfoByBoard中返回的数据绘制时延随TPS变化情况(其实TPS&delay信息应该是与单板无关的，但是为了解析方便，在各个单板的性能信息中均重复的统计了一份TPS&delay)
// 使用的时候直接取一个单板的PerfInfo绘图即可
function delayLine(src) {
	
	var infosSize = src['infosSize'];
    const data = [];
    for (var jj = 0; jj < infosSize; jj++) {
        var obj = {
            tps: src['tpss'][jj],
            delay: src['delays'][jj]
        };
        data.push(obj);
    }

    const chart = new G2.Chart({
        container: 'delayLine',
        forceFit: false,
        width: 1100,
        height: 400,
        padding: {
            top: 50,
            right: 200,
            bottom: 50,
            left: 100
        }
    });

    chart.source(data, {
    	
    	delay: {
            min: 0,
            type: 'linear',
            tickInterval: 10,
            alias: '平均时延(ms)'
        }
    });

    // 左侧 Y 轴，即delay轴
    chart.axis('delay', {
        label: {
            formatter: val => {
                    return val + 'ms'; // 格式化坐标轴显示
                },
                textStyle: {
                    fill: 'red'
                }
        },
        line: null,
        tickLine: null
    });

    // 设置横坐标单位
    chart.axis('tps', {
        label: {
            formatter: val => {
                return val + 'TPS';
            }
        }
    });
    /*
    chart.legend({
    	  title: null, // 不展示图例的标题
    });
	*/
    chart.tooltip(true, {
    	containerTpl: '<div class="g2-tooltip">'
    		  + '<div class="g2-tooltip-title" style="margin-bottom: 4px; color: white;"></div>'
    		  + '<ul class="g2-tooltip-list"></ul>'
    		  + '</div>',
        crosshairs: {
        	triggerOn: 'mousemove',
            type: 'cross',
            style: {
            	stroke: 'gray'
            }
        }
    });

    chart.line().position('tps*delay').color('red').shape('smooth'); // CPU
    chart.point().position('tps*delay').color('red').size(4).shape('circle').style({
        stroke: '#fff',
        lineWidth: 1
      });

    chart.render();
}

// 使用getPerfInfoByBoard中返回的数据绘制各个单板的CPU使用率、发送带宽占用、接收带宽占用的图像
function boardLine(src) {

    var infosSize = src['infosSize'];
    const data = [];
    for (var jj = 0; jj < infosSize; jj++) {
        var obj = {
            tps: src['tpss'][jj],
            receive: src['receives'][jj],
            send: src['sends'][jj],
            cpuused: src['cpuuseds'][jj]
        };
        data.push(obj);
    }

    const chart = new G2.Chart({
        container: src['desc'],
        forceFit: false,
        width: 1100,
        height: 400,
        padding: {
            top: 50,
            right: 200,
            bottom: 50,
            left: 100
        }
    });

    chart.source(data, {
    	
        cpuused: {
            min: 0,
            type: 'linear',
            tickInterval: 50,
            alias: 'CPU占用(%)'
        },
        receive: {
            min: 0,
            type: 'linear',
            alias: '接收带宽占用(KB/s)'
        },
        send: {
            min: 0,
            type: 'linear',
            alias: '发送带宽占用(KB/s)'
        }
    });

    // 左侧 Y 轴，即cpuused轴
    chart.axis('cpuused', {
        label: {
            formatter: val => {
                    return val + ' %'; // 格式化坐标轴显示
                },
                textStyle: {
                    fill: 'red'
                }
        },
        line: null,
        tickLine: null
    });

    // 右侧第一个 Y 轴，receive
    chart.axis('receive', {
        line: null,
        tickLine: null,
        label: {
            formatter: val => {
            	return val + ''; // 格式化坐标轴显示
            },
            textStyle: {
            	fill: 'green'
            }
        }
    });

    // 右侧第二个 Y 轴，即send
    chart.axis('send', {
        line: null,
        tickLine: null,
        label: {
            offset: 80,
            formatter: val => {
            	return val + 'KB/s'; // 格式化坐标轴显示
            },
            textStyle: {
            	fill: 'blue'
            }
        }
    });

    // 设置横坐标单位
    chart.axis('tps', {
        label: {
            formatter: val => {
                return val + 'TPS';
            }
        }
    });

    chart.legend({
    	position: 'top'
    });
    
    chart.tooltip(true, {
    	containerTpl: '<div class="g2-tooltip">'
  		  + '<div class="g2-tooltip-title" style="margin-bottom: 4px; color: white;"></div>'
  		  + '<ul class="g2-tooltip-list"></ul>'
  		  + '</div>',
        crosshairs: {
        	triggerOn: 'mousemove',
            type: 'cross',
            style: {
            	stroke: 'black'
            }
        }
    });

    chart.line().position('tps*cpuused').color('red').shape('smooth'); // CPU
    chart.point().position('tps*cpuused').color('red').size(4).shape('circle').style({
        stroke: '#fff',
        lineWidth: 1
      });

    chart.line().position('tps*receive').color('green').shape('smooth'); // receive
    chart.point().position('tps*receive').color('green').size(4).shape('circle').style({
        stroke: '#fff',
        lineWidth: 1
      });

    chart.line().position('tps*send').color('blue').shape('smooth'); // send
    chart.point().position('tps*send').color('blue').size(4).shape('circle').style({
        stroke: '#fff',
        lineWidth: 1
      });

    chart.render();
}

// 解析json数据，绘制表格
function makeTable(jsonObj) {
	
	var makeTableBoards = getBoards(jsonObj);
	
	var makeTableBoardPerfInfo = getPerfInfoByBoard(jsonObj, makeTableBoards[0]);
	
	var makeTableTPSs = makeTableBoardPerfInfo['tpss'];
	var makeTableDelays = makeTableBoardPerfInfo['delays'];
	
	document.write("<table  border='1' align='center'>");
	
	// 绘制表头
	document.write('<tr>');
	document.write('<td rowspan="2" align="center">TPS</td>');
	document.write('<td rowspan="2" align="center">AvgDelay</td>');
	for (makeTablej = 0; makeTablej < makeTableBoards.length; makeTablej++) {
		document.write('<td colspan="3" align="center">' + makeTableBoards[makeTablej] + '</td>');
	}
	document.write('</tr>');
	document.write('<tr>');
	for (makeTablej = 0; makeTablej < makeTableBoards.length; makeTablej++) {
		document.write('<td align="center">CPU(%)</td>');
		document.write('<td align="center">Send(kB/s)</td>');
		document.write('<td align="center">Recelve(kB/s)</td>');
	}
	document.write('</tr>');
	
	// 绘制数据体
	for (makeTablei = 0; makeTablei < makeTableTPSs.length; makeTablei++) {
		document.write('<tr>');
		
		document.write('<td>' + makeTableTPSs[makeTablei] + '</td>');
		
		document.write('<td>' + makeTableDelays[makeTablei] + '</td>');
		
		for (makeTablej = 0; makeTablej < makeTableBoards.length; makeTablej++) {
			
			var makeTableBoardPerfInfoTmp = getPerfInfoByBoard(jsonObj, makeTableBoards[makeTablej]);
			
			document.write('<td>' + makeTableBoardPerfInfoTmp['cpuuseds'][makeTablei] + '</td>');
			document.write('<td>' + makeTableBoardPerfInfoTmp['sends'][makeTablei] + '</td>');
			document.write('<td>' + makeTableBoardPerfInfoTmp['receives'][makeTablei] + '</td>');
		}
		
		document.write('</tr>');
	}
	document.write('</table>');
}