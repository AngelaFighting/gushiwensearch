package elasticsearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.health.ClusterIndexHealth;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.google.gson.Gson;

import model.Article;

public class ESClient {

	/**传输请求的客户端**/
	private TransportClient client;

	/**初始化**/
	public void startUp() {
		if (client == null) {
			try {
				// 设置cluster.name				
				Settings settings = Settings.builder()
						.put("cluster.name", "elasticsearch").build();
				// 连接到Client, 如果连接到一个 Elasticsearch 集群，构建器可以接受多个地址。
				//Settings.EMPTY默认cluster.name为elasticsearch
				//client = new PreBuiltTransportClient(Settings.EMPTY);				        
				client = new PreBuiltTransportClient(settings)
						.addTransportAddress(
								new InetSocketTransportAddress(InetAddress
										.getByName("127.0.0.1"), 9300));
				System.out.println("成功链接到服务端");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}

	/**关闭**/
	public void shutDown() {
		client.close();
		System.out.println("链接关闭");
	}

	/**查看集群健康情况**/
	private void catClusterHealth(){
		ClusterHealthResponse healths = client.admin().cluster().prepareHealth().get(); 
		String clusterName = healths.getClusterName();              
		int numberOfDataNodes = healths.getNumberOfDataNodes();     
		int numberOfNodes = healths.getNumberOfNodes();     
		System.out.println("集群名："+clusterName
				+"\n数据节点数："+numberOfDataNodes
				+"\n节点数："+numberOfNodes);

		for (ClusterIndexHealth health : healths.getIndices().values()) { 
			String index = health.getIndex();                       
			int numberOfShards = health.getNumberOfShards();        
			int numberOfReplicas = health.getNumberOfReplicas();  
			System.out.println("索引名"+index
					+"\n分片数："+numberOfShards
					+"\n复制数："+numberOfReplicas);
			ClusterHealthStatus status = health.getStatus();
			if (!status.equals(ClusterHealthStatus.GREEN)) {
				throw new RuntimeException("Index is in " + status + " state"); 
			}
		}
	}	

	/** 利用ES自带的JSON生成器生成json数据**/
	private XContentBuilder generateJson() {
		XContentBuilder builder = null;
		try {
			builder = XContentFactory.jsonBuilder().startObject()
					.startObject("_all").field("enabled", false).endObject()
					.startObject("properties")
					//对作者进行分词
					.startObject("author").field("type","string")
					.field("analyzer","index_ansj")
					.field("search_analyzer","query_ansj").endObject()
					//对标题进行分词
					.startObject("title").field("type","string")
					.field("analyzer","index_ansj")
					.field("search_analyzer","query_ansj").endObject()
					//对原文进行分词
					.startObject("content").field("type","string")
					.field("analyzer","index_ansj")
					.field("search_analyzer","query_ansj").endObject()
					.endObject().endObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder;
	}
	private XContentBuilder generateJson(Map map) {
		XContentBuilder builder = null;
		try {
			builder = XContentFactory.jsonBuilder().startObject()
					.field("name", map.get("name"))
					.field("age", map.get("age"))
					.field("about", map.get("about")).endObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder;
	}

	/**创建索引**/
	private boolean createIndex(){
		CreateIndexResponse response=client.admin()
				.indices().prepareCreate("gushiwensearch")
				//设置3个主分片和2份复制,默认为5个主分片和1份复制
				//        .setSettings(Settings.builder()             
				//                .put("index.number_of_shards", 3)
				//                .put("index.number_of_replicas", 2)
				//        )
				.addMapping("shiwens", generateJson())
				.get();  
		return response.isAcknowledged();
	}

	/**PUT索引数据**/
	private IndexResponse putIndex() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", "Lilei");
		map.put("age", "38");
		map.put("about", "Lilei and Hanmeimei.");
		// 使用UUID.randomUUID()生成一个ID
		IndexResponse response = client
				.prepareIndex("vinux", "employee", UUID.randomUUID().toString())
				.setSource(generateJson(map)).get();
		System.out.println("返回数据****************\n 索引名称INDEX_NAME: "
				+ response.getIndex() + "\n" + "索引类别INDEX_TYPE: "
				+ response.getType() + "\n" + "版本VERSION: "
				+ response.getVersion() + "\n" + "索引ID: "
				+ response.getId());
		return response;
	}

	/** 读取Json文本内容,默认编码为UTF-8
	 * @param file 文本文件
	 * @return 
	 */
	private String readJson(File file){
		StringBuilder sb=new StringBuilder();
		try{           
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file),"utf-8"));
			String s;          
			while((s=br.readLine())!=null){               
				sb.append(s);
			}
			br.close();
		}catch (IOException ex) {
			ex.printStackTrace();
		}
		return sb.toString();
	}

	/**批量插入索引数据**/
	private void bulkInsert(String dirPath){
		File dir=new File(dirPath);
		File[] files=dir.listFiles();
		String json=null;
		int count=0;
		//开启批量插入
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		for(File file: files){
			json=readJson(file);
			count++;
			bulkRequest.add(client
					.prepareIndex("gushiwensearch", "shiwens",
							file.getName().replace(".json", ""))
					.setSource(json));
			if(count%1000==0){
				bulkRequest.get();
				System.out.println("提交了："+count);
			}			
		}
		bulkRequest.get();
		System.out.println("插入完毕");
	}

	/**获取索引数据**/
	public GetResponse getIndex(String id) {
		GetResponse getResponse = client
				.prepareGet("gushiwensearch", "shiwens", id)
				.get();
		//System.out.println("返回数据：" + getResponse.getSourceAsString());
		return getResponse;
	}

	/**MultiGetResponse多文档查询**/
	private MultiGetResponse multiIndex() {
		MultiGetResponse multiGetResponse = null;
		multiGetResponse = client.prepareMultiGet()
				.add("vinux", "employee", "1")// 单个ID查询
				.add("vinux", "employee", "2", "5")// 多个ID查询
				.add("megacorp", "employee", "2")// 另一个索引
				.get();
		for (MultiGetItemResponse multiGetItemResponse : multiGetResponse) {
			GetResponse getResponse = multiGetItemResponse.getResponse();
			if (getResponse.isExists()) { 
				System.out.println("多数据查询："+getResponse.getSourceAsString());
			}
		}
		return multiGetResponse;
	}

	/**DELETE 索引**/
	private DeleteResponse delIndex() {
		DeleteResponse delResponse = client.prepareDelete("vinux", "employee",
				"AVW_erhIwjGNSfVFDfzf").get();
		System.out.println("DEL:"+ delResponse.getVersion());
		return delResponse;
	}

	/**UPDATE第一种方式：通过创建UpdateRequest对象，然后将其发送到客户端进行修改**/
	private UpdateResponse updateRequest() {
		UpdateRequest updateRequest = new UpdateRequest("vinux", "employee",
				"1");
		UpdateResponse updateResp = null;
		try {

			// 利用ES自带的XContentFactory.jsonBuilder()方法生成JSON数据
			updateRequest.doc(XContentFactory.jsonBuilder().startObject()
					.field("age", "10")// 将年龄改成10岁
					.endObject());
			// updateRequest.script(new Script("ctx._source.age = \"100\""));
			updateResp = client.update(updateRequest).get();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return updateResp;
	}

	/**UPDATE第二种方式：利用prepareUpdate()方法**/
	private UpdateResponse updateIndex() {
		UpdateResponse updateResponse = null;
		try {
			updateResponse = client
					.prepareUpdate("vinux", "employee", "1")
					.setDoc(XContentFactory.jsonBuilder().startObject()
							.field("age", "100")// 将年龄改成100岁
							.endObject()).get();
			// script方式创建文档
			// updateResponse = client
			// .prepareUpdate("vinux", "employee", "1")
			// .setScript(
			// new Script("ctx._source.age = \"100\"",
			// ScriptService.ScriptType.INLINE, null, null))
			// .get();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return updateResponse;
	}

	/**第三种方式：使用upsert()方法，如果索引存在就修改，没有就add**/
	private UpdateResponse upsert() {
		UpdateResponse updateResponse = null;
		IndexRequest indexRequest = null;
		try {
			// 新建
			indexRequest = new IndexRequest("vinux", "employee", "7")
					.source(XContentFactory.jsonBuilder().startObject()
							.field("name", "zhanzhan").field("age", "40")
							.endObject());
			// 修改
			UpdateRequest updateRequest = new UpdateRequest("vinux",
					"employee", "7").doc(
							XContentFactory.jsonBuilder().startObject()
							.field("age", "1000").endObject()).upsert(
									indexRequest);// upsert()方法
			updateResponse = client.update(updateRequest).get();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return updateResponse;
	}
	
	/**SearchResponse对象查询**/
	private SearchResponse searchIndex() {
		SearchResponse searchResponse = 
				client.prepareSearch("gushiwensearch")// 文档名称
				.setTypes("shiwens")// 类型		
				// 设置查询类型 1.SearchType.DFS_QUERY_THEN_FETCH = 精确查询 2.SearchType.SCAN = 扫描查询,无序
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)		
				.setExplain(true)// 设置是否按查询匹配度排序
				.setQuery(QueryBuilders.matchPhraseQuery("author", "屈原")) 
				.get();
		//默认返回前10条数据
		SearchHit[] searchHits = searchResponse.getHits().hits();
		for (SearchHit searchHit : searchHits) {
			System.out.println("Id:"+searchHit.getId()
				+"\n分数："+searchHit.getScore()
				+"\n内容："+ searchHit.getSourceAsString());//获取source数据	
		}
		return searchResponse;
	}
	
	/**Scroll**/
	private SearchResponse scrollIndex(){
		QueryBuilder qb = QueryBuilders.termQuery("content", "李白");
		SearchResponse scrollResp = 
				client.prepareSearch("gushiwensearch")// 文档名称
				.setTypes("shiwens")// 类型
		        .setScroll(new TimeValue(60000))// 设置滚动搜索的超时时间
		        .setQuery(qb)
		        .setSize(10).get(); //max of 100 hits will be returned for each scroll
		//Scroll until no hits are returned
		int count=0;
		do {
		    for (SearchHit hit : scrollResp.getHits().getHits()) {
		    	System.out.println("Id:"+hit.getId()
				+"\n分数："+hit.getScore()
				+"\n内容："+ hit.getSourceAsString());//获取source数据
		    }
		    count+=scrollResp.getHits().getHits().length;
		    scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
		    		.setScroll(new TimeValue(60000)).execute().actionGet();
		} while(scrollResp.getHits().getHits().length != 0); 
		System.out.println(count);
		// Zero hits mark the end of the scroll and the while loop.
		return scrollResp;
	}
	
	/**多个查询**/
	private MultiSearchResponse multiSearch(){
		SearchRequestBuilder srb1 = client
				.prepareSearch().setQuery(
						QueryBuilders.queryStringQuery("屈原"));
		SearchRequestBuilder srb2 = client
				.prepareSearch().setQuery(
						QueryBuilders.matchQuery("author", "屈原"))
				.setSize(5);
		MultiSearchResponse sr = client.prepareMultiSearch()
				.add(srb1)
				.add(srb2)
				.get();
		// You will get all individual responses from MultiSearchResponse#getResponses()
		long nbHits = 0;
		for (MultiSearchResponse.Item item : sr.getResponses()) {
			SearchResponse response = item.getResponse();
			SearchHit[] searchHits = response.getHits().hits();
			for (SearchHit searchHit : searchHits) {
				System.out.println("Id:"+searchHit.getId()
					+"\n分数："+searchHit.getScore()+"\n内容："
						+ searchHit.getSourceAsString());//获取source数据	
			}
			nbHits += response.getHits().getTotalHits();
		}
		System.out.println(nbHits);
		return sr;
	}
	
	/**聚合**/
	private SearchResponse aggregations(){
		SearchResponse sr = client.prepareSearch()
				.setQuery(QueryBuilders.matchAllQuery())
				.addAggregation(
						AggregationBuilders.terms("agg1").field("field"))
				.addAggregation(
						AggregationBuilders.dateHistogram("agg2")
						.field("birth")
						.dateHistogramInterval(DateHistogramInterval.YEAR))
				.get();
		// Get your facet results
		Terms agg1 = sr.getAggregations().get("agg1");
		DateHistogramInterval agg2 = sr.getAggregations().get("agg2");
		return sr;
	}
	
	/**QueryBuilder查询**/
	private SearchResponse QueryBuilder() {
		HighlightBuilder highlightBuilder=new HighlightBuilder();	
		// 查询参数
		//QueryBuilder matchQuery = QueryBuilders.matchQuery("author", "屈原");
		QueryBuilder qb = QueryBuilders.multiMatchQuery(
			    "屈原", "author", "title","content");
		// 设置Client连接
		SearchResponse searchRequestBuilder = 
				client.prepareSearch("gushiwensearch")
				.setTypes("shiwens")
				.setQuery(qb)
				.highlighter(highlightBuilder.field("author").field("title").field("content"))
				.setSize(20).get();
		for (SearchHit hit : searchRequestBuilder.getHits().getHits()) {
	    	System.out.println("Id:"+hit.getId()
			+"\n分数："+hit.getScore());//+"\n内容："
				//+ hit.getSourceAsString());//获取source数据
			Map<String,HighlightField> map=hit.getHighlightFields();
			for(Map.Entry<String, HighlightField> me: map.entrySet()){
				System.out.println(me.getKey()+" "+me.getValue().fragments()[0]);
			}
	    }
		return searchRequestBuilder;
	}

	public List<Article> searchIndex(String scope,String query){
		List<Article> articleList=new ArrayList<Article>();
		QueryBuilder qb = null;
		if(scope.equals("all")){			
			qb = QueryBuilders.multiMatchQuery(
				    query, "author", "title","content");
//			qb = QueryBuilders.boolQuery()
//					.should(QueryBuilders.termQuery("author", query))
//					.should(QueryBuilders.termQuery("title", query))
//					.should(QueryBuilders.termQuery("content", query));
		}
		if(scope.equals("author")){
			qb = QueryBuilders.termQuery("author", query);
		}
		if(scope.equals("title")){
			qb = QueryBuilders.termQuery("title", query);
		}
		if(scope.equals("content")){
			qb = QueryBuilders.termQuery("content", query);
		}
		SearchResponse scrollResp = 
				client.prepareSearch("gushiwensearch")// 文档名称
				.setTypes("shiwens")// 类型
				// 设置查询类型 1.SearchType.DFS_QUERY_THEN_FETCH = 精确查询 2.SearchType.SCAN = 扫描查询,无序
				//.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)		
				//.setExplain(true)// 设置是否按查询匹配度排序
		        .setScroll(new TimeValue(60000))// 设置滚动搜索的超时时间
		        .setQuery(qb)
		        .setSize(100).get(); //max of 100 hits will be returned for each scroll
		//int count=0;
		Gson gson=new Gson();
		//Scroll until no hits are returned
		do {
		    for (SearchHit hit : scrollResp.getHits().getHits()) {
		    	Article article=gson.fromJson(hit.getSourceAsString(), Article.class);
		    	article.setId(hit.getId());
		    	article.setScore(hit.getScore());	
		    	String content=article.getContent()
		    			.replaceAll("(<.*?br.*?>)|(<.*?p.*?>)|(\n)", "");
				int maxIndex=content.length()<50?content.length():50;
				article.setContent(content.substring(0, maxIndex));
//				System.out.println(article.getContent());
				articleList.add(article);
		    }
		    //count+=scrollResp.getHits().getHits().length;
		    scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
		    		.setScroll(new TimeValue(60000)).execute().actionGet();
		} while(scrollResp.getHits().getHits().length != 0); 
		// Zero hits mark the end of the scroll and the while loop.
		//System.out.println(count);
		//System.out.println(articleList);
		return articleList;
	}

	public static void main(String args[]){
		ESClient client=new ESClient();
		client.startUp();
		//System.out.println(client.createIndex());
		//client.bulkInsert("D://data//webmagic//www.haoshiwen.org");
		//client.getIndex("f78ff59fc43e14752f8c240faa2e8241");
		//client.searchIndex();
		//client.multiSearch();
		//client.scrollIndex();
		//client.QueryBuilder();
		client.searchIndex("content", "屈原");
		client.shutDown();
	}
}
