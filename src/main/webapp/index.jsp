<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>首页</title>
		<!-- 新 Bootstrap 核心 CSS 文件 -->
		<link href="http://cdn.static.runoob.com/libs/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">		
		<link href="public.css" rel="stylesheet"/>
	</head>
	<body>
		<div id="index">
			<div id="logo">				
				<h1 class="text-center">
					古<img src="img/logo.png" width=100 />诗文
				</h1>
			</div>
			<form method="get" action="list.jsp">
				<div class="form-group">
					<div class="col-xs-9">
						<input type="text" placeholder="请输入查询内容" 
							name="query" class="form-control">
						<ul class="list-inline">
					    	<li>
								<div class="radio">
									<label>
								    	<input type="radio" name="scope" value="all" checked>综合 
								  	</label>
								</div>
							</li>
							<li>
								<div class="radio">
									<label>
								    	<input type="radio" name="scope" value="author">作者  
								  	</label>
								</div>	
							</li>
							<li>
								<div class="radio">
									<label>
								    	<input type="radio" name="scope" value="title">标题  
								  	</label>
								</div>	
							</li>
							<li>
								<div class="radio">
									<label>
								    	<input type="radio" name="scope" value="content">正文 
								  	</label>
								</div>
							</li>
						</ul>
					</div>
					<div class="col-xs-3">
						<button type="submit" 
							class="btn btn-primary form-control submit_btn">
							搜索
						</button>	
					</div>				
			    </div>			    
			</form>
		</div>
		<div id="footer">
			<address>
				&copy;2016古诗文搜索引擎
				<br />
				Alpha开发小组
			</address>		
		</div>
	</body>
</html>
