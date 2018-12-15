package elasticsearch;

import java.util.List;

import org.elasticsearch.action.get.GetResponse;

import com.google.gson.Gson;

import model.Article;

public class ESOperator {

	public Article getArticle(String id){
		ESClient client=new ESClient();
		client.startUp();
		GetResponse response=client.getIndex(id);
		String json=response.getSourceAsString();
		Gson gson=new Gson();
		Article article=gson.fromJson(json, Article.class);
		//System.out.println(article);
		client.shutDown();
		return article;
	}
	
	public List<Article> getArticleList(String scope,String query){
		ESClient client=new ESClient();
		client.startUp();
		List<Article> articleList=client.searchIndex(scope, query);
		client.shutDown();
		return articleList;
	}

	public static void main(String args[]){
		ESOperator op=new ESOperator();
		op.getArticle("326d4766513fea1dabc99e8c2ee107e3");
	}
}
