package spider;

import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;

public class GuShiWenCrawler {

	/**网址头部**/
	private final static String headUrl = 
			"http://www.haoshiwen.org/type.php?";

	/**初始化开始爬取的链接**/
	private static String[] intiUrls(){
		String[] urls=new String[55];
		int count=0;
		for(int i=1;i<=11;i++){
			for(int j=1;j<=5;j++){
				urls[count++]=headUrl+"c="+i+"&x="+j;
			}
		}
		return urls;
	}

	public static void main(String[] args){
		GuShiWenPageProcessor processor=new GuShiWenPageProcessor();
		Spider.create(processor)//指定PageProcessor页面处理器
		.addUrl(intiUrls())//添加爬取链接
		//.addUrl("http://www.haoshiwen.org/type.php?c=1&x=5")
		//指定Pipeline结果处理对象，这里把结果保存成JSON文件
		.addPipeline(new JsonFilePipeline())
		.thread(5)//指定线程数
		.run();//开始爬虫
		System.out.println("诗词总数有："+processor.articleCount());//75604
		System.out.println("运行结束");
	}

}

