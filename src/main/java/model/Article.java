package model;

public class Article {
	
	/**类型**/
	private String type;
	/**作者**/
	private String author;
	/**朝代**/
	private String dynasty;
	/**作者简介**/
	private String author_info;
	/**标题**/
	private String title;
	/**原文**/
	private String content;
	/**译文**/
	private String translation;
	/**注释**/
	private String comment;
	/**赏析**/
	private String appreciation;
	/**UUID**/
	private String id;
	/**匹配度**/
	private float score;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getDynasty() {
		return dynasty;
	}
	public void setDynasty(String dynasty) {
		this.dynasty = dynasty;
	}
	public String getAuthor_info() {
		return author_info;
	}
	public void setAuthor_info(String author_info) {
		this.author_info = author_info;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTranslation() {
		return translation;
	}
	public void setTranslation(String translation) {
		this.translation = translation;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getAppreciation() {
		return appreciation;
	}
	public void setAppreciation(String appreciation) {
		this.appreciation = appreciation;
	}
	
	public String toString(){
		return "Article:{id="+id+",score="+score+",type="+type
				+",dynasty="+dynasty+",author="+author
				+",author_info="+author_info+",title="+title+",content="
				+content+",translation="+translation+",comment="+comment
				+",appreciation="+appreciation+"}";
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}

	
}
