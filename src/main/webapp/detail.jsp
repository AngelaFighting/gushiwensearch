<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="elasticsearch.ESOperator,model.Article" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>详情页</title>
		<link href="http://cdn.static.runoob.com/libs/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">		
		<!-- jQuery文件，务必在bootstrap.min.js 之前引入 -->
		<script src="http://cdn.static.runoob.com/libs/jquery/2.1.1/jquery.min.js"></script>	
		<!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
		<script src="http://cdn.static.runoob.com/libs/bootstrap/3.3.7/js/bootstrap.min.js"></script>	
		<link href="public.css" rel="stylesheet"/>
	</head>
	<body>
	<% 
	ESOperator op=new ESOperator();
	Article article=op.getArticle(request.getParameter("id")); 
	%>
		<div id="query">
			<form method="get" action="list.jsp">
				<div class="form-group">
					<div class="col-xs-5">
						<input type="text" placeholder="请输入查询内容" 
							name="query" class="form-control">						
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
			    </div>
			</form>
		</div>
		<hr />
		<div id="detail">
			<div class="content">
				<h4><%=article.getTitle() %></h4>
				<h5>朝代：<%=article.getDynasty() %></h5>
				<h5>作者：<%=article.getAuthor() %></h5>
				<h5>类型：<%=article.getType() %></h5>
				<p><%=article.getContent() %></p>
			</div>
			<hr />

			<ul id="other" class="nav nav-tabs">
				<li class="active">
					<a href="#author" data-toggle="tab">
						作者简介
					</a>
				</li>
				<li>
					<a href="#translation" data-toggle="tab">
						译文
					</a>
				</li>
				<li>
					<a href="#comment" data-toggle="tab">
						注释
					</a>
				</li>
				<li>
					<a href="#appreciation" data-toggle="tab">
						赏析
					</a>
				</li>
			</ul>
			
			<div id="otherContent" class="tab-content">
				<div class="tab-pane fade in active" id="author">
					<p>
					<%
					if(article.getAuthor_info()!=null)
						out.println(article.getAuthor_info());
					%>
					</p>
				</div>
				<div class="tab-pane fade" id="translation">
					<p>
					<%
					if(article.getTranslation()!=null)
						out.println(article.getTranslation());
					%>
					</p>
				</div>
				<div class="tab-pane fade" id="comment">
					<p>
					<%
					if(article.getComment()!=null)
						out.println(article.getComment());
					%>
					</p>
				</div>
				<div class="tab-pane fade" id="appreciation">
					<p>
					<%
					if(article.getAppreciation() !=null)
						out.println(article.getAppreciation() );
					%>
					</p>
				</div>
			</div>

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
