$(document).ready(function () {
    getDBList();
    $("#query").keypress(function (e) {
        if (e.which == 13) {
            queryFunction();
        }
    });
    //update currently selected database
    $(document).on("click", "#db-list .list-group-item", function () {
        $("#db-list .list-group-item").each(function () {
            $(this).removeClass('selected');
        });
        $(this).addClass('selected');
    });

    //update currently table database
    $(document).on("click", "#table-list .list-group-item", function () {
        $("#table-list .list-group-item").each(function () {
            $(this).removeClass('selected');
        });
        $(this).addClass('selected');
    });

//$( ':file' ).change( onFileChanged );
//$("input[type='file']").change( onFileChanged );
//$("input[type='file']").live('change', onFileChanged);
//openTerminal();
});

var dbOrPath;

var filePath = "/";
var anrPath = "/data/anr/";

const TYPE_DATABASE = 1;
const TYPE_FILE = 2;
var type = TYPE_DATABASE;

function openTerminal() {
    console.log("openTerminal 1");

    var result = $.ajax({
        url: "/openTerminal",
        async: false
    }).responseText;

    result = JSON.parse(result);
    if (result.isSuccessful) {
        window.open("/terminal?port=" + result.port);
    }
    else {
        showErrorInfo(result.error);
    }

}

var isDatabaseSelected = true;

function getData(tableName) {

    $.ajax({
        url: "/getTableData?tableName=" + tableName, success: function (result) {

            result = JSON.parse(result);
            inflateData(result);

        }
    });

}

function queryFunction() {

    var query = $('#query').val();

    $.ajax({
        url: "/query?dbname=" + dbOrPath + "&query="+escape(query), success: function (result) {

            result = JSON.parse(result);
            inflateData(result);

        }
    });

}

function downloadDb() {
    if (isDatabaseSelected) {
        $.ajax({
            url: "/downloadDb", success: function () {
                window.location = 'downloadDb';
            }
        });
    }
}


function getDBList() {

    $.ajax({
        url: "/getDbList", success: function (result) {

            result = JSON.parse(result);
            var dbList = result.rows;
            $('#db-list').empty();
            var isSelectionDone = false;

            console.log("pathname:" + location.pathname);
            var isLoadDir = location.pathname.length > 0 && location.pathname.indexOf("/", 1) != -1;
            console.log("isLoadDir:" + isLoadDir);

            for (var count = 0; count < dbList.length; count++) {
                if (dbList[count].indexOf("journal") == -1) {
                    var selected = isLoadDir && "Storage" == dbList[count] ? "selected" : "";
                    $("#db-list").append("<a href='#' id=" + dbList[count] + " class='list-group-item " + selected + "' onClick='openDatabaseAndGetTableList(\"" + dbList[count] + "\");'>" + dbList[count] + "</a>");
                    if (!isSelectionDone && !isLoadDir) {
                        isSelectionDone = true;
                        $('#db-list').find('a').trigger('click');
                    }
                }
            }

            if(isLoadDir){
                filePath = location.pathname;
                openDatabaseAndGetTableList("Storage");
            }

        }
    });

}

function getFileData(fileName) {

    $.ajax({
        url: "/getAllDataFromTheFile?fileName=" + fileName, success: function (result) {
            console.log("getFileData 1");
            result = JSON.parse(result);
            if (result.isFile) {
                console.log("getFileData 2");
                inflateFileData(result);
            } else {
                if (result.isSuccessful) {
//                                  result = JSON.parse(result);
                    var tableList = result.rows;
                    //                   var dbVersion = result.dbVersion;
                    $('#selected-db-info').removeClass('active');
                    $('#selected-db-info').addClass('disabled');
                    $("#selected-db-info").text("Storage Size : " + tableList.length);
                    $('#table-list').empty()
                    for (var count = 0; count < tableList.length; count++) {
                        var fileName = tableList[count];
                        $("#table-list").append("<a href='#' file-name='" + fileName + "' class='list-group-item' onClick='getFileData(\"" + fileName + "\");'>" + fileName + "</a>");
                    }

                    showFilePath(result.path);
//                                  $("#table-dir").remove();
//                                  $("#parent-table-dir").empty();
//                                  $("#parent-table-dir").append("<a href='#' class='panel-heading' onClick='backDirPath();'>" +result.info + "</a>");
                }
            }


        }
    });

}

function inflateFileData(result) {
    console.log("getFileData 3");
    if (result.isSuccessful) {
        $("#db-data-div").remove();
        console.log("getFileData 4");
        if (result.error == null)
            $("#parent-data-div").append('<div id="db-data-div"><div class="panel-heading">' + result.info + '</div></div>');
        else {
            $("#parent-data-div").append('<div id="db-data-div"><a href="#" onClick="downloadDb();">Export Selected File : ' + result.path + '</a></div>');
        }
        console.log("getFileData 5");
        $('#selected-db-info').removeClass('disabled');
        $('#selected-db-info').addClass('active');
        isDatabaseSelected = true;
        $("#selected-db-info").text("Export Selected File : " + result.path);
    }
}

function openDatabaseAndGetTableList(db) {

    dbOrPath = db;
    if ("APP_SHARED_PREFERENCES" == db || "Storage" == db || "ANR" == db) {
        $('#run-query').removeClass('active');
        $('#run-query').addClass('disabled');
        $('#selected-db-info').removeClass('active');
        $('#selected-db-info').addClass('disabled');
        isDatabaseSelected = false;
        if("APP_SHARED_PREFERENCES" == db)
            $("#selected-db-info").text("SharedPreferences");
        else
            $("#selected-db-info").text(db);
    } else {
        $('#run-query').removeClass('disabled');
        $('#run-query').addClass('active');
        $('#selected-db-info').removeClass('disabled');
        $('#selected-db-info').addClass('active');
        isDatabaseSelected = true;
        $("#selected-db-info").text("Export Selected Database : " + db);
    }

//    if("Storage" != db) {
//    if(!db.startWith("Storage")) {
    if ("Storage" != db && "ANR" != db) {
        initScene(TYPE_DATABASE);
        $.ajax({
            url: "/getTableList?database=" + db, success: function (result) {

                result = JSON.parse(result);
                var tableList = result.rows;
                var dbVersion = result.dbVersion;
                if ("APP_SHARED_PREFERENCES" != db) {
                    $("#selected-db-info").text("Export Selected Database : " + db + " Version : " + dbVersion);
                }
                $('#table-list').empty()
                for (var count = 0; count < tableList.length; count++) {
                    var tableName = tableList[count];
                    $("#table-list").append("<a href='#' data-db-name='" + db + "' data-table-name='" + tableName + "' class='list-group-item' onClick='getData(\"" + tableName + "\");'>" + tableName + "</a>");
                }
                showTable();
            }
        });
    } else {
        initScene(TYPE_FILE);
        var path = filePath;
        if ("ANR" == dbOrPath)
            path = anrPath;

        $.ajax({
            url: "/getFileList?path=" + encodeURIComponent(path),
            success: function (result) {

                result = JSON.parse(result);
                handleFileList(result);
            }
        });

    }

}

function guid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

function refreshFileList() {
    var path = filePath;
    if ("ANR" == dbOrPath)
        path = anrPath;
    console.log("path:" + path);
    $.ajax({
        url: "/getFileList?path=" + encodeURIComponent(path),
        success: function (result) {
            result = JSON.parse(result);
            handleFileList(result);
        }
    });
}

function enterFile(path, isFile) {
    console.log(isFile);
    if (isFile) {
        console.log(path);
        window.open(path);
    }
    else {

        initScene(TYPE_FILE);
        if("Storage" == dbOrPath)
            filePath = path;
        else if ("ANR" == dbOrPath)
            anrPath = path;
//        filePath = path;
        refreshFileList();

    }

}

function backDirPath() {
    if("Storage" == dbOrPath && "/" != filePath){
        var index = filePath.substring(0, filePath.length - 1).lastIndexOf("/");
        filePath = filePath.substring(0, index + 1);
        initScene(TYPE_FILE);
        refreshFileList();
    }
    else if ("ANR" == dbOrPath && "/data/anr/" != anrPath){
        var index = anrPath.substring(0, anrPath.length - 1).lastIndexOf("/");
        anrPath = anrPath.substring(0, index + 1);
        initScene(TYPE_FILE);
        refreshFileList();
    }

//    if ("/" != filePath) {
//        var index = filePath.substring(0, filePath.length - 1).lastIndexOf("/");
//        filePath = filePath.substring(0, index + 1);
//        initScene(TYPE_FILE);
//        refreshFileList();
//    }
}

function downloadFile(path) {
    window.location.href = '/dodownload?path=' + encodeURIComponent(path);
}

function editDatabase(path) {
    console.log(path);
    $.post( '/doedit', {
            'path': encodeURIComponent(path)
        }, function( result, status ) {


        result = JSON.parse(result);
        console.log(result);
            if ( result.isSuccessful ) {

                $('#db-list').find('a').removeClass('selected');
                $("#db-list").append("<a href='#' id='" + result.dbName + "' class='list-group-item selected' onClick='openDatabaseAndGetTableList(\"" + result.dbName + "\");'>" + result.dbName + "</a>");
//                $('#db-list').find('#' + result.dbName).trigger('click');
                openDatabaseAndGetTableList(result.dbName);
//$('#dict.db').addClass('selected');
//var dbName = result.dbName;
//                setTimeout("$('#" + dbName + "').trigger('click')",1000);
            } else {
                showErrorInfo( result.errorMessage );
            }
        }).fail(function() { // 503 etc.
            showErrorInfo( MSG.not_allow );
        });
}

function deleteFile(path) {
    console.log(path);

    var data = {
        'msg': MSG.del_msg
    }
    $.showModal( data, function() {
        $.post( '/dodelete', {
            'path': encodeURIComponent(path)
        }, function( data, status ) {
        console.log(data);
            if ( data == '0' ) {
                //window.location.reload();
                refreshFileList();
            } else {
                showErrorInfo( MSG.del_error );
            }
        }).fail(function() { // 503 etc.
            showErrorInfo( MSG.not_allow );
        });
    });

}

//////////////////////////////////////////////////////////

var Progress = function( $wrapper, $meter, id, period ) {
    this.$wrapper = $wrapper;
    this.$meter = $meter;
    if (id == undefined) {
        var $file = $wrapper.find( ':file' );
        this.id = $file.attr( 'id' );
    } else {
        this.id = id;
    }
    this.period = period == undefined ? 200 : period;

    this.$meterSpan = $meter.find( '> span' );
}

Progress.prototype.ready = function() {
    this.$wrapper.hide();
    this.$meter.show();
}

Progress.prototype.update = function() {
console.log("update");
    var self = this;
    $.post( '/doprogress', {
        'id': self.id
    }, function( data, status ) {
    console.log("data:" + data);
        if ( data !== '-1' ) {
            self.$meterSpan.css( 'width', data + '%' );
            if ( data == '100' ) {
                self.over();
            } else {
                setTimeout(function() {
                    self.update();
                }, self.period );
            }
        }
    });
}

Progress.prototype.delayUpdate = function( delay, wait, times ) {
    var self = this,
        wait = wait == undefined ? false : true;
    var delayFunc = self.update;
    if ( wait ) {
        times = times == undefined ? 5 : times;
        function reqProgress() {
            $.post( '/doprogress', {
                'id': self.id
            }, function( data, status ) {
                var func = reqProgress;
                if ( data !== '-1' ) {
                    func = self.update;
                    self.$meterSpan.css( 'width', data + '%' );
                }
                if (--times >= 0) {
                    setTimeout(function() {
                        func.call( self );
                    }, self.period );
                }
            });
        };
        delayFunc = reqProgress;
    }
    setTimeout(function() {
        delayFunc();
    }, delay );
}

Progress.prototype.over = function() {
    this.$wrapper.show();
    this.$meter.hide();
    this.$meterSpan.css( 'width', '50%' );
}

function onFileChanged() {
console.log("onFileChanged 1");
    var $file = $( this ),
        val = $file.val();
    if ( val ) {
    console.log("onFileChanged 2");
        var id = $file.attr( 'id' ),
            name = $file.attr( 'name' );

        var $parent = $( $file.parent() ),
            $meter = $( $parent.next()[0] );

        var progress = new Progress( $parent, $meter, id );
        progress.ready();
        progress.delayUpdate( 200, true );

        var path = filePath;
        if ("ANR" == dbOrPath)
            path = anrPath;
        var url = '/doupload?dir=' + encodeURIComponent(path + name) + '&id=' + encodeURIComponent(id);
        $.ajaxFileUpload({
            url: url,
            fileElementId: id,
            dataType: 'json',
            success: function( data, status ) {
                $( '#' + id ).change( onFileChanged );
                progress.over();
                if ( data == '' ) { // not 200.
                    showErrorInfo( MSG.not_allow );
                }
            },
            error: function( data, status, e ) {
                $( '#' + id ).change( onFileChanged );
                progress.over();
                showErrorInfo( MSG.up_error );
            }
        });
    }
}

function isPC() {
    var userAgentInfo = navigator.userAgent;
    var Agents = ["Android", "iPhone",
                "SymbianOS", "Windows Phone",
                "iPad", "iPod"];
    var flag = true;
    for (var v = 0; v < Agents.length; v++) {
        if (userAgentInfo.indexOf(Agents[v]) > 0) {
            flag = false;
            break;
        }
    }
    return flag;
}

if (!String.prototype.endsWith) {
	String.prototype.endsWith = function(search, this_len) {
		if (this_len === undefined || this_len > this.length) {
			this_len = this.length;
		}
        return this.substring(this_len - search.length, this_len) === search;
	};
}

function handleFileList(result) {

console.log("isPC:" + isPC());

    if("Storage" == dbOrPath){
        filePath = result.path;
        $("#dir-name").text(filePath);
    }
    else if ("ANR" == dbOrPath){
        anrPath = result.path;
        $("#dir-name").text(anrPath);
    }

    console.log("result.isSuccessful:" + result.isSuccessful);
    console.log("result.path:" + result.path);
    console.log("result.fileRows:" + result.fileRows);

    $("#file-list-table").remove();
//$("#div-file").append('<table class="display nowrap" cellpadding="0" border="0" cellspacing="0" width="100%" class="table table-striped table-bordered display" id="file-list-table"></table></div>');

var fixed = "";
var pullRight = "";
var pc = isPC();
if(pc){
fixed = "fixed";
pullRight = "pull-right";
}

    var tableStr = '<table class="display nowrap" cellpadding="0" border="0" cellspacing="0" width="100%" class="table table-striped table-bordered ' + fixed + ' display" id="file-list-table">';

    tableStr = tableStr + "<tr class='list-header height'>";

    for (i in result.colNames) {
        if(i == 0)
            tableStr = tableStr + "<td class='operateColumn'>" + result.colNames[i] + "</td>";
        else
            tableStr = tableStr + "<td class='detailsColumn'>" + result.colNames[i] + "</td>";
    }

    tableStr = tableStr + "</tr>";

    for (i in result.fileRows) {
        row = result.fileRows[i];
        console.log("fileRow.time:" + row.time);
        tableStr = tableStr + "<tr class='height'>";

        var href = "javascript:void(0);";
        if("icon dir" == row.clazz)
            href = "#";

        tableStr = tableStr + '<td><a class="' + row.clazz + '" href="' + href + '" onClick="enterFile(\'' + row.link + '\',' + row.is_file + ');">' + row.name + '</a></td>';
        tableStr = tableStr + '<td class="detailsColumn">' + row.size + '</td>';
        tableStr = tableStr + '<td class="detailsColumn">' + row.time + '</td>';
        tableStr = tableStr + '<td class="operateColumn">';

        if (row.can_download)
            tableStr = tableStr + '<button type="submit" onclick="downloadFile(\'' + row.link + '\')" class="btn btn-info ' + pullRight + ' small fbtn">下载</button>';

        if (row.can_delete)
            tableStr = tableStr + '<button type="submit" onclick="deleteFile(\'' + row.link + '\')" class="btn btn-info ' + pullRight + ' small fbtn">删除</button>';

        if (row.can_upload) {
            tableStr = tableStr + '<button type="submit" href="javascript:void(0);" class="btn btn-info ' + pullRight + ' small fbtn">上传<input id="' + guid() + '" type="file" name="' + row.name + '"></button>';
            tableStr = tableStr + '<div class="meter animate pull-right" style="display:none;"><span style="width: 50%"><span></span></span></div>';
        }

        if(row.is_file && row.name.endsWith(".db"))
            tableStr = tableStr + '<button type="submit" onclick="editDatabase(\'' + row.link + '\')" class="btn btn-info ' + pullRight + ' small fbtn">编辑</button>';

        tableStr = tableStr + "</td></tr>";

//$("#file-list").append("<a href='#' data-db-name='"+db+"' data-table-name='"+tableName+"' class='list-group-item' onClick='getData(\""+ tableName + "\");'>" +tableName + "</a>");

//for(var count = 0; count < tableList.length; count++){
//                 var tableName = tableList[count];
//                 $("#table-list").append("<a href='#' data-db-name='"+db+"' data-table-name='"+tableName+"' class='list-group-item' onClick='getData(\""+ tableName + "\");'>" +tableName + "</a>");
//               }
    }

    $("#div-file").append(tableStr);

    $( ':file' ).change( onFileChanged );

//var tableId = "#file-list-data";
//        if ($.fn.DataTable.isDataTable(tableId) ) {
//          $(tableId).DataTable().destroy();
//        }
//
//
//$("#file-list-div").remove();
////       $("#parent-table-dir").empty();
//       $("#div-file").append('<div id="file-list-div"><table class="display nowrap" cellpadding="0" border="0" cellspacing="0" width="100%" class="table table-striped table-bordered display" id="file-list-data"></table></div>');
//
////       var availableButtons;
////       if (result.isEditable) {
////            availableButtons = [
////                {
////                    text : 'Add',
////                    name : 'add' // don not change name
////                },
////                {
////                    extend: 'selected', // Bind to Selected row
////                    text: 'Edit',
////                    name: 'edit'        // do not change name
////                },
////                {
////                    extend: 'selected',
////                    text: 'Delete',
////                    name: 'delete'
////                }
////            ];
////       } else {
////            availableButtons = [];
////       }
//
//       $(tableId).dataTable({
//           "data": result.fileRows,
//           "columnDefs": ["123", "23", "12", "32"],
//           'bPaginate': true,
//           'searching': true,
//           'bFilter': true,
//           'bInfo': true,
//           "bSort" : true,
//           "scrollX": true,
//           "iDisplayLength": 10,
//           "dom": "Bfrtip",
//            select: 'single',
//            altEditor: true//,     // Enable altEditor
////            buttons: availableButtons
//       })
//
////       //attach row-updated listener
////       $(tableId).on('update-row.dt', function (e, updatedRowData, callback) {
////            var updatedRowDataArray = JSON.parse(updatedRowData);
////            //add value for each column
////            var data = columnHeader;
////            for(var i = 0; i < data.length; i++) {
////                data[i].value = updatedRowDataArray[i].value;
////                data[i].dataType = updatedRowDataArray[i].dataType;
////            }
////            //send update table data request to server
////            updateTableData(data, callback);
////       });
////
////
////       //attach delete-updated listener
////       $(tableId).on('delete-row.dt', function (e, updatedRowData, callback) {
////            var deleteRowDataArray = JSON.parse(updatedRowData);
////
////            console.log(deleteRowDataArray);
////
////            //add value for each column
////            var data = columnHeader;
////            for(var i = 0; i < data.length; i++) {
////                data[i].value = deleteRowDataArray[i].value;
////                data[i].dataType = deleteRowDataArray[i].dataType;
////
////            }
////
////            //send delete table data request to server
////            deleteTableData(data, callback);
////       });
////
////
////
////       $(tableId).on('add-row.dt', function (e, updatedRowData, callback) {
////                   var deleteRowDataArray = JSON.parse(updatedRowData);
////
////                   console.log(deleteRowDataArray);
////
////                   //add value for each column
////                   var data = columnHeader;
////                   for(var i = 0; i < data.length; i++) {
////                       data[i].value = deleteRowDataArray[i].value;
////                       data[i].dataType = deleteRowDataArray[i].dataType;
////                   }
////
////                   //send delete table data request to server
////                   addTableData(data, callback);
////              });
//
//       // hack to fix alignment issue when scrollX is enabled
//       $(".dataTables_scrollHeadInner").css({"width":"100%"});
//       $(".table ").css({"width":"100%"});

}

function initScene(t) {
    if (t != type) {
        type = t;
        if (type == TYPE_DATABASE) {
            $("#div-table").show();
            $("#parent-data-div").show();
            $("#div-file").hide();
        }
        else if (type == TYPE_FILE) {
            $("#div-table").hide();
            $("#parent-data-div").hide();
            $("#div-file").show();
        }
    }
}

function showTable() {
    $("#parent-table-dir").empty();
    $("#parent-table-dir").append("<div class='panel-heading'>Tables</div>");
//   $("#parent-table-dir").css({"width":"100%"});
}

function showFilePath(filePath) {
    $("#parent-table-dir").empty();
    $("#parent-table-dir").append("<div class='panel-heading'><a href='#' onClick='backDirPath();'>" + filePath + "</a></div>");
//   $("#parent-table-dir").css({"width":"100%"});
}

function backDirPath0() {
//console.log("backDirPath 1");
    $.ajax({
        url: "/backDirPath", success: function (result) {

            result = JSON.parse(result);
            if (result.isSuccessful) {
                var tableList = result.rows;
//                   var dbVersion = result.dbVersion;
                $('#selected-db-info').removeClass('active');
                $('#selected-db-info').addClass('disabled');
                $("#selected-db-info").text("Storage Size : " + tableList.length);
                $('#table-list').empty()
                for (var count = 0; count < tableList.length; count++) {
                    var fileName = tableList[count];
                    $("#table-list").append("<a href='#' file-name='" + fileName + "' class='list-group-item' onClick='getFileData(\"" + fileName + "\");'>" + fileName + "</a>");
                }

                showFilePath(result.path);
//                   $("#table-dir").remove();
//                   $("#parent-table-dir").empty();
//                   $("#parent-table-dir").append("<a href='#' class='panel-heading' onClick='backDirPath();'>" +result.info + "</a>");
            }
        }
    });
}

function inflateData(result) {

    if (result.isSuccessful) {

        if (!result.isSelectQuery) {
            showSuccessInfo("Query Executed Successfully");
            return;
        }

        var columnHeader = result.tableInfos;

        // set function to return cell data for different usages like set, display, filter, search etc..
        for (var i = 0; i < columnHeader.length; i++) {
            columnHeader[i]['targets'] = i;
            columnHeader[i]['data'] = function (row, type, val, meta) {
                var dataType = row[meta.col].dataType;
                if (type == "sort" && dataType == "boolean") {
                    return row[meta.col].value ? 1 : 0;
                }
                return row[meta.col].value;
            }
        }
        var columnData = result.rows;
        var tableId = "#db-data";
        if ($.fn.DataTable.isDataTable(tableId)) {
            $(tableId).DataTable().destroy();
        }

        $("#db-data-div").remove();
//       $("#parent-table-dir").empty();
        $("#parent-data-div").append('<div id="db-data-div"><table class="display nowrap" cellpadding="0" border="0" cellspacing="0" width="100%" class="table table-striped table-bordered display" id="db-data"></table></div>');

        var availableButtons;
        if (result.isEditable) {
            availableButtons = [
                {
                    text: 'Add',
                    name: 'add' // don not change name
                },
                {
                    extend: 'selected', // Bind to Selected row
                    text: 'Edit',
                    name: 'edit'        // do not change name
                },
                {
                    extend: 'selected',
                    text: 'Delete',
                    name: 'delete'
                }
            ];
        } else {
            availableButtons = [];
        }

        $(tableId).dataTable({
            "data": columnData,
            "columnDefs": columnHeader,
            'bPaginate': true,
            'searching': true,
            'bFilter': true,
            'bInfo': true,
            "bSort": true,
            "scrollX": true,
            "iDisplayLength": 10,
            "dom": "Bfrtip",
            select: 'single',
            altEditor: true,     // Enable altEditor
            buttons: availableButtons
        })

        //attach row-updated listener
        $(tableId).on('update-row.dt', function (e, updatedRowData, callback) {
            var updatedRowDataArray = JSON.parse(updatedRowData);
            //add value for each column
            var data = columnHeader;
            for (var i = 0; i < data.length; i++) {
                data[i].value = updatedRowDataArray[i].value;
                data[i].dataType = updatedRowDataArray[i].dataType;
            }
            //send update table data request to server
            updateTableData(data, callback);
        });


        //attach delete-updated listener
        $(tableId).on('delete-row.dt', function (e, updatedRowData, callback) {
            var deleteRowDataArray = JSON.parse(updatedRowData);

            console.log(deleteRowDataArray);

            //add value for each column
            var data = columnHeader;
            for (var i = 0; i < data.length; i++) {
                data[i].value = deleteRowDataArray[i].value;
                data[i].dataType = deleteRowDataArray[i].dataType;

            }

            //send delete table data request to server
            deleteTableData(data, callback);
        });


        $(tableId).on('add-row.dt', function (e, updatedRowData, callback) {
            var deleteRowDataArray = JSON.parse(updatedRowData);

            console.log(deleteRowDataArray);

            //add value for each column
            var data = columnHeader;
            for (var i = 0; i < data.length; i++) {
                data[i].value = deleteRowDataArray[i].value;
                data[i].dataType = deleteRowDataArray[i].dataType;
            }

            //send delete table data request to server
            addTableData(data, callback);
        });

        // hack to fix alignment issue when scrollX is enabled
        $(".dataTables_scrollHeadInner").css({"width": "100%"});
        $(".table ").css({"width": "100%"});
    } else {
        if (!result.isSelectQuery) {
            showErrorInfo("Query Execution Error");
        } else {
            showErrorInfo("Some Error Occurred");
        }
    }

}

//send update database request to server
function updateTableData(updatedData, callback) {
    //get currently selected element
    var selectedTableElement = $("#table-list .list-group-item.selected");

    var filteredUpdatedData = updatedData.map(function (columnData) {
        return {
            title: columnData.title,
            isPrimary: columnData.isPrimary,
            value: columnData.value,
            dataType: columnData.dataType
        }
    });
    //build request parameters
    var requestParameters = {};
    requestParameters.dbName = selectedTableElement.attr('data-db-name');
    requestParameters.tableName = selectedTableElement.attr('data-table-name');
    requestParameters.updatedData = JSON.stringify(filteredUpdatedData);

    //execute request
    $.ajax({
        url: "/updateTableData",
        type: 'GET',
        data: requestParameters,
        success: function (response) {
            response = JSON.parse(response);
            if (response.isSuccessful) {
                console.log("Data updated successfully");
                callback(true);
                showSuccessInfo("Data Updated Successfully");
            } else {
                console.log("Data updated failed");
                callback(false);
            }
        }
    })
}

function deleteTableData(deleteData, callback) {

    var selectedTableElement = $("#table-list .list-group-item.selected");
    var filteredUpdatedData = deleteData.map(function (columnData) {
        return {
            title: columnData.title,
            isPrimary: columnData.isPrimary,
            value: columnData.value,
            dataType: columnData.dataType
        }
    });

    //build request parameters
    var requestParameters = {};
    requestParameters.dbName = selectedTableElement.attr('data-db-name');
    requestParameters.tableName = selectedTableElement.attr('data-table-name');
    requestParameters.deleteData = JSON.stringify(filteredUpdatedData);

    //execute request
    $.ajax({
        url: "/deleteTableData",
        type: 'GET',
        data: requestParameters,
        success: function (response) {
            response = JSON.parse(response);
            if (response.isSuccessful) {
                console.log("Data deleted successfully");
                callback(true);
                showSuccessInfo("Data Deleted Successfully");
            } else {
                console.log("Data delete failed");
                callback(false);
            }
        }
    })
}

function addTableData(deleteData, callback) {

    var selectedTableElement = $("#table-list .list-group-item.selected");
    var filteredUpdatedData = deleteData.map(function (columnData) {
        return {
            title: columnData.title,
            isPrimary: columnData.isPrimary,
            value: columnData.value,
            dataType: columnData.dataType
        }
    });

    console.log(filteredUpdatedData);

    //build request parameters
    var requestParameters = {};
    requestParameters.dbName = selectedTableElement.attr('data-db-name');
    requestParameters.tableName = selectedTableElement.attr('data-table-name');
    requestParameters.addData = JSON.stringify(filteredUpdatedData);

    console.log(requestParameters);

    //execute request
    $.ajax({
        url: "/addTableData",
        type: 'GET',
        data: requestParameters,
        success: function (response) {
            response = JSON.parse(response);
            if (response.isSuccessful) {
                console.log("Data Added successfully");
                callback(true);
                getData(requestParameters.tableName);
                showSuccessInfo("Data Added Successfully");
            } else {
                console.log("Data Adding failed");
                callback(false);
            }
        }
    });
}

function showSuccessInfo(message) {
    var snackbarId = "snackbar";
    var snackbarElement = $("#" + snackbarId);
    snackbarElement.addClass("show");
    snackbarElement.css({"backgroundColor": "#5cb85c"});
    snackbarElement.html(message)
    setTimeout(function () {
        snackbarElement.removeClass("show");
    }, 3000);
}

function showErrorInfo(message) {
    var snackbarId = "snackbar";
    var snackbarElement = $("#" + snackbarId);
    snackbarElement.addClass("show");
    snackbarElement.css({"backgroundColor": "#d9534f"});
    snackbarElement.html(message)
    setTimeout(function () {
        snackbarElement.removeClass("show");
    }, 3000);
}
