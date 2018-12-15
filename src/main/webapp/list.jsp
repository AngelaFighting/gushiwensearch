<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="elasticsearch.ESOperator,java.util.List,model.Article" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<title>查询结果</title>
		<link href="http://cdn.static.runoob.com/libs/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">		
		<link href="public.css" rel="stylesheet"/>
	</head>
	<body>
		<div id="query">
			<form method="get" action="list.jsp">
				<div class="form-group">
					<div class="col-xs-5">
						<input type="text" placeholder="请输入查询内容" 
							name="query" class="form-control"
							value="<%=request.getParameter("query") %>" >					
					</div>
					<div class="col-xs-2">
						<button type="submit" 
							class="btn btn-primary form-control submit_btn">
							搜索
						</button>	
					</div>
					<div class="col-xs-5">
						<ul class="list-inline">
					    	<li>
								<div class="radio">
									<label>
								    	<input type="radio" name="scope" value="all" 
								    	checked
								    	>综合 
								  	</label>
								</div>
							</li>
							<li>
								<div class="radio">
									<label>
								    	<input type="radio" name="scope" value="author"
								    	<%
								    	if(request.getParameter("scope").equals("author"))
								    		out.println("checked");
								    	%>
								    	>作者  
								  	</label>
								</div>	
							</li>
							<li>
								<div class="radio">
									<label>
								    	<input type="radio" name="scope" value="title"
								    	<%
								    	if(request.getParameter("scope").equals("title"))
								    		out.println("checked");
								    	%>
								    	>标题  
								  	</label>
								</div>	
							</li>
							<li>
								<div class="radio">
									<label>
								    	<input type="radio" name="scope" value="content"
								    	<%
								    	if(request.getParameter("scope").equals("content"))
								    		out.println("checked");
								    	%>
								    	>正文 
								  	</label>
								</div>
							</li>
						</ul>
					</div>
			    </div>
			</form>
		</div>
		
		<hr />
		<div id="result">
			<% 
			ESOperator op=new ESOperator();
			List<Article> articleList=op.getArticleList(request.getParameter("scope"),
						request.getParameter("query").trim());
			%>
			<h5>共有<%=articleList.size() %>个相关结果</h5>
			<ul class="list-group">
			<% for(Article article: articleList){ %>
				<li class="list-group-item">
					<div>
						<h4><a href="detail.jsp?id=<%=article.getId() %>">
						<%=article.getTitle() %></a></h4>
						<h5>作者：<%=article.getAuthor() %>
						&nbsp;&nbsp;&nbsp;&nbsp;匹配度：
						<%=article.getScore() %>
						</h5>
						<p><%=article.getContent() %>
						<a href="detail.jsp?id=<%=article.getId() %>">
						...</a></p>
					</div>
				</li>
			<%} %>
			</ul>
		</div>
		<hr />
		<div id="footer">
			<address>
				&copy;2016古诗文搜索引擎
				<br />
				Alpha开发小组
			</address>		
		</div>
	</body>
</html>