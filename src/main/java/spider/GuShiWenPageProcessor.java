package spider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Article;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

public class GuShiWenPageProcessor implements PageProcessor{

	/**开始链接**/
	private final static String URL_START = 
			"http://www\\.haoshiwen\\.org/type\\.php\\?"
			+"c=\\d+&x=[1-5]$";	
	/**列表链接**/
	private final static String URL_LIST = 
			"http://www\\.haoshiwen\\.org/type\\.php\\?"
			+"c=\\d+&x=[1-5]&page=\\d+";
	/**诗词链接**/
	private final static String URL_ARTICLE = 
			"http://www\\.haoshiwen\\.org/view\\.php\\?id=\\d+";
	/**翻译链接**/
	private final static String URL_TRANSLATION = 
			"http://www\\.haoshiwen\\.org/show\\.php\\?t=2&id=\\d+";
	/**赏析链接**/
	private final static String URL_APPRECIATION = 
			"http://www\\.haoshiwen\\.org/show\\.php\\?t=1&id=\\d+";
	/**暂存Article**/
	private static Map<String,Article> articleMap =
			new HashMap<String,Article>();
	/**暂存article的类型**/
	private static Map<String,String> articleType = 
			new HashMap<String,String>();
	
	/**获取article总数**/
	public static int articleCount(){
		return articleType.size();
	}
	
	private Site site=Site.me().setCycleRetryTimes(5)
			.setRetryTimes(5).setSleepTime(100)
			.setUserAgent("Mo zilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
            .setCharset("UTF-8");
	
	/**
	 * 获取article的类型
	 * @param url 开始链接或列表链接，从中提取出article类型Num
	 * @return
	 */
	private static String getType(Selectable url){
		String type=null;
		int type_num=Integer.parseInt(url.regex("c=\\d+&x=([1-5)])").toString());
		switch(type_num){
			case 1:
				type="诗";break;
			case 2:
				type="词";break;
			case 3:
				type="曲";break;
			case 4:
				type="文言文";break;
			case 5:
				type="辞赋";break;
		}	
		return type;
	}
	
	/**保存Article**/
	private void saveArticle(Article article,Page page){
		page.putField("type", article.getType());
		page.putField("dynasty", article.getDynasty());
		page.putField("author", article.getAuthor());
		page.putField("author_info", article.getAuthor_info());
		page.putField("title", article.getTitle());
		page.putField("content", article.getContent());
		page.putField("translation", article.getTranslation());
		page.putField("comment", article.getComment());
		page.putField("appreciation", article.getAppreciation());
	}
	
	/**提取想要的信息**/
	public void process(Page page){
		if(page.getUrl().regex(URL_START).match()){
			//获取页数OK
			String pageStr=page.getHtml()
					.xpath("//div[@class='pages']")
					.regex("/type.php\\?c=\\d+&amp;x=[1-5]&amp;page=(\\d+)\">尾页</a>")
					.toString();
			//System.out.println("开始："+page.getUrl()+" 页数"+pageStr);
			if(pageStr!=null){
				int page_num=Integer.parseInt(pageStr);
				//System.out.println(page_num);
				List<String> pageUrl=new ArrayList<String>();
				//把其余页的url添加到Request队列中
				for(int i=2;i<=page_num;i++){
					pageUrl.add(page.getUrl()+"&page="+i);
				}
				page.addTargetRequests(pageUrl);
			}
			//添加起始页的古诗文列表
			List<String> articleUrl = page.getHtml()
					.xpath("//div[@class='typeleft']/div[@class='sons']")
					.regex(URL_ARTICLE)
					.all();
			//System.out.println(articleUrl);
			page.addTargetRequests(articleUrl);
			page.setSkip(true);//跳过这个页面
			//获取类型
			String type=getType(page.getUrl());
			for(String url: articleUrl){
				articleType.put(url, type);
			}
		}
		if(page.getUrl().regex(URL_LIST).match()){
			//System.out.println("列表："+page.getUrl());
			//古诗文列表
			List<String> articleUrl = page.getHtml()
					.xpath("//div[@class='typeleft']/div[@class='sons']")
					.regex(URL_ARTICLE)
					.all();
			//System.out.println(articleUrl);
			page.addTargetRequests(articleUrl);
			page.setSkip(true);//跳过这个页面
			//获取类型
			String type=getType(page.getUrl());
			for(String url: articleUrl){
				articleType.put(url, type);
			}
		}
		else if(page.getUrl().regex(URL_ARTICLE).match()){
			System.out.println("诗词："+page.getUrl());
			Html html=page.getHtml();
			Article article=new Article();
			//类型
			article.setType(articleType.get(page.getUrl().toString()));
			//朝代
			String dynasty=html
					.xpath("//div[@class='son2']")
					.regex("<span>朝代：</span>(.*?)</p>").toString();
			//System.out.println(dynasty);
			article.setDynasty(dynasty);
			//作者
			String author=html
					.xpath("//div[@class='son2']")
					.regex("<span>作者：</span>(.*?)</p>")
					.toString().replaceAll("</?a.*?>", "");
			//System.out.println(author);
			article.setAuthor(author);
			if(!author.equals("佚名")){
				//作者简介
				String author_info=html
						.regex("<div class=\"son5\" style=\"overflow:auto;\">"
						+ ".*</a> ("+author+".*?)<a.*?>\\.\\.\\.</a>")
						.toString();
				//System.out.println(author_info);
				article.setAuthor_info(author_info);
			}
			//标题
			String title=html.xpath("div[@class='son1']/h1/text()")
					.toString();
			//System.out.println(title);
			article.setTitle(title);
			//原文
			String content=html
					.xpath("div[@class='son2']")
					.regex("<span>原文：</span></p>(.*?)</div>")
					.toString();
			//System.out.println(content);
			article.setContent(content);				
			//译文链接
			String fanyi_url=html
					.xpath("div[@class='son5']").links()
					.regex(URL_TRANSLATION).toString();		
			//赏析链接
			String shangxi_url=html
					.xpath("div[@class='son5']").links()
					.regex(URL_APPRECIATION).toString();		
			//System.out.println("翻译："+fanyi_url);
			//System.out.println("赏析："+shangxi_url);
			if(fanyi_url==null&&shangxi_url==null){	
				//如果没有译文和赏析，则直接保存该Article对象
				saveArticle(article,page);
			}
			else{
				//否则，则把Article存在articleMap，等待信息被补齐才保存
				if(fanyi_url!=null){
					article.setTranslation(fanyi_url);
					page.addTargetRequest(fanyi_url);
				}
				if(shangxi_url!=null){
					article.setAppreciation(shangxi_url);
					page.addTargetRequest(shangxi_url);
				}
				articleMap.put(page.getUrl().toString(), article);
				page.setSkip(true);//跳过这个页面
			}			
		}
		else if(page.getUrl().regex(URL_TRANSLATION).match()){
			Html html=page.getHtml();
			String article_url=html
					.xpath("//div[@class='sontitle']/span/a/@href")
					.toString();
			//System.out.println("诗词的链接"+article_url);
			//翻译标题
			String tran_title=html
					.xpath("//div[@class='shileft']/div[@class='son1']/h1/text()")
					.toString();
			//System.out.println(tran_title);
			String translation=null;
			String comment=null;
			if(tran_title.endsWith("译文及注释")){
				translation=html
						.xpath("//div[@class='shangxicont']")
						.regex("<p><strong>译文.*?</strong>(.*?)</p>")
						.toString();
				if(translation!=null)//去掉无关内容 
					translation=translation.replaceAll("</?a.*?>", "");
				comment=html
						.xpath("//div[@class='shangxicont']")
						.regex("<p><strong>注释.*?</strong>(.*?)</p>")
						.toString();
				if(comment!=null)
					comment=comment.replaceAll("</?a.*?>", "");
				if(translation==null&&comment==null){
					//译文和注释被合并在了一起
					translation=html
							.xpath("//div[@class='shangxicont']")
							.regex("<p>作者：佚名</p>(.*?)<p style=")
							.toString();
					if(translation!=null)
						translation=translation.replaceAll("</?a.*?>", "");
				}
			}else{
				//只有译文
				if(tran_title.endsWith("译文")){
					translation=html
						.xpath("//div[@class='shangxicont']")
						.regex("<p>作者：佚名</p>(.*?)<p style=")
						.toString();
					if(translation!=null)//去掉无关内容
						translation=translation.replaceAll("</?a.*?>", "");
				}
				//只有注释
				if(tran_title.endsWith("注释")){
					comment=html
						.xpath("//div[@class='shangxicont']")
						.regex("<p>作者：佚名</p>(.*?)<p style=")
						.toString();
					if(comment!=null)
						comment=comment.replaceAll("</?a.*?>", "");
				}
			}
			//System.out.println(translation);
			//System.out.println(comment);
			Article article=articleMap.get(article_url);
			String appreciation=article.getAppreciation();
			if(appreciation!=null&&appreciation.startsWith("http")){
				articleMap.get(article_url).setTranslation(translation);
				articleMap.get(article_url).setComment(comment);;
				page.setSkip(true);//跳过这个页面
			}else{
				article.setTranslation(translation);
				article.setComment(comment);
				saveArticle(article,page);
				articleMap.remove(article_url);				
			}
		}
		else if(page.getUrl().regex(URL_APPRECIATION).match()){
			Html html=page.getHtml();
			String article_url=html
					.xpath("//div[@class='sontitle']/span/a/@href")
					.toString();
			//System.out.println("诗词的链接"+article_url);
			String appre_title=html
					.xpath("//div[@class='shileft']/div[@class='son1']/h1")
					.toString();
			//System.out.println(appre_title);
			String appreciation=html
					.xpath("//div[@class='shangxicont']")
					.regex("<p>作者：佚名</p>(.*?)<p style=")
					.toString();
			if(appreciation!=null)
				appreciation=appreciation.replaceAll("</?a.*?>", "");
			//System.out.println(appreciation);
			Article article=articleMap.get(article_url);
			String translation=article.getTranslation();
			if(translation!=null&&translation.startsWith("http")){
				articleMap.get(article_url).setAppreciation(appre_title+appreciation);
				page.setSkip(true);//跳过这个页面
			}else{
				article.setAppreciation(appre_title+appreciation);
				saveArticle(article,page);
				articleMap.remove(article_url);
			}
		}	
	}	
	
	public Site getSite() {
        return site;
	}
	
}
