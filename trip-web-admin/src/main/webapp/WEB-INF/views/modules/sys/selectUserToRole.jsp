<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>分配角色</title>
	<meta name="decorator" content="blank"/>
	<%@include file="/WEB-INF/views/include/treeview.jsp" %>
	<script type="text/javascript">
	
		var selectedTree;//zTree已选择对象
		
		// 初始化
		$(document).ready(function(){
			selectedTree = $.fn.zTree.init($("#selectedTree"), setting, selectedNodes);
            $.fn.zTree.init($("#userTree"), setting, allUserNodes);
		});

		var setting = {view: {selectedMulti:false,nameIsHTML:true,showTitle:false,dblClickExpand:false},
				data: {simpleData: {enable: true}},
				callback: {onClick: treeOnClick}};
		
		var pre_selectedNodes =[
   		        <c:forEach items="${userList}" var="sysUser">
   		        {id:"${sysUser.id}",
   		         pId:"0",
   		         name:"<font color='red' style='font-weight:bold;'>${sysUser.name}</font>"},
   		        </c:forEach>];
		
		var selectedNodes =[
		        <c:forEach items="${userList}" var="sysUser">
		        {id:"${sysUser.id}",
		         pId:"0",
		         name:"<font color='red' style='font-weight:bold;'>${sysUser.name}</font>"},
		        </c:forEach>];

        var allUserNodes =[
            <c:forEach items="${allUserList}" var="sysUser">
            {id:"${sysUser.id}",
                pId:"0",
                name:"<font style='font-weight:bold;'>${sysUser.name}</font>"},
            </c:forEach>];
		
		var pre_ids = "${selectIds}".split(",");
		var ids = "${selectIds}".split(",");
		
		//点击选择项回调
		function treeOnClick(event, treeId, treeNode, clickFlag){
			$.fn.zTree.getZTreeObj(treeId).expandNode(treeNode);
			if("userTree"==treeId){
				if($.inArray(String(treeNode.id), ids)<0){
					selectedTree.addNodes(null, treeNode);
					ids.push(String(treeNode.id));
				}
			}
			if("selectedTree"==treeId){
				if($.inArray(String(treeNode.id), pre_ids)<0){
					selectedTree.removeNode(treeNode);
					ids.splice($.inArray(String(treeNode.id), ids), 1);
				}else{
					top.$.jBox.tip("角色原有成员不能清除！", 'info');
				}
			}
		}
		function clearAssign(){
			var submit = function (v, h, f) {
			    if (v == 'ok'){
					var tips="";
					if(pre_ids.sort().toString() == ids.sort().toString()){
						tips = "未给角色【${role.name}】分配新成员！";
					}else{
						tips = "已选人员清除成功！";
					}
					ids=pre_ids.slice(0);
					selectedNodes=pre_selectedNodes;
					$.fn.zTree.init($("#selectedTree"), setting, selectedNodes);
			    	top.$.jBox.tip(tips, 'info');
			    } else if (v == 'cancel'){
			    	// 取消
			    	top.$.jBox.tip("取消清除操作！", 'info');
			    }
			    return true;
			};
			var tips="确定清除角色【${role.name}】下的已选人员？";
			top.$.jBox.confirm(tips, "清除确认", submit);
		}
	</script>
</head>
<body>
	<div id="assignRole" class="row-fluid span6">
		<div class="span3">
			<p>待选人员：</p>
			<div id="userTree" class="ztree"></div>
		</div>
		<div class="span3" style="padding-left:16px;border-left: 1px solid #A8A8A8;">
			<p>已选人员：</p>
			<div id="selectedTree" class="ztree"></div>
		</div>
	</div>
</body>
</html>