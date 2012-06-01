package tv.acfun.util;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpRequestExecutor;
import org.json.external.JSONArray;
import org.json.external.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import acfun.domain.AcfunContent;
import android.util.Log;
import android.widget.Toast;



public class Parser {
	
	public static ArrayList<HashMap<String, String>> ParserAcId(String id) throws IOException{
		
		
		
		ArrayList<HashMap<String, String>> parts = new ArrayList<HashMap<String, String>>();
		
		if(Integer.parseInt(id)>327496){
			URL lurl;
			try {
				lurl = new URL("http://www.acfun.tv/api/content.aspx?query="+id);
				
				HttpURLConnection conn = (HttpURLConnection) lurl.openConnection();
				conn.setConnectTimeout(6 * 1000);
				if (conn.getResponseCode() != 200)
					throw new RuntimeException("请求url失败");
				InputStream is = conn.getInputStream();
				String jsonstring = readData(is, "UTF8");
				conn.disconnect();
				
				JSONObject jsonObject = new JSONObject(jsonstring);
				JSONArray jsonArray = jsonObject.getJSONArray("content");
				
				for(int i = 0;i<jsonArray.length();i++){
					
					JSONObject job = (JSONObject) jsonArray.get(i);
					String id1 = null;
					String regex = "\'id\':\'(.\\d+)";
					Pattern pattern = Pattern.compile(regex);
					 Matcher matcher = pattern.matcher(job.toString());
					 while(matcher.find()){
						 id1 = matcher.group(1);
						 break;
					 }
					 System.out.println(job.toString());
					 System.out.println(id1);
					 URL getvidurl = new URL("http://www.acfun.tv/api/getVideoByID.aspx?vid="+id1);
					 
					 HttpURLConnection vidcon = (HttpURLConnection) getvidurl.openConnection();
						conn.setConnectTimeout(6 * 1000);
						if (conn.getResponseCode() != 200)
							throw new RuntimeException("请求url失败");
						InputStream getvidis = vidcon.getInputStream();
						String vidjsonstring = readData(getvidis, "UTF8");
						vidcon.disconnect();
						JSONObject vidjsonObject = new JSONObject(vidjsonstring);
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("type", vidjsonObject.get("vtype").toString());
						map.put("id", vidjsonObject.get("vid").toString());
						parts.add(map);
				}
				

				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
//		Connection c = Jsoup.connect("http://www.acfun.tv/m/art.php?aid="+id);
//		Document doc = c.get();
//		Elements ems = doc.getElementsByTag("embed");
//		
//		if(ems.size()==0){
//			HashMap<String, String> map = new HashMap<String, String>();
//			map.put("type", "");
//			map.put("id", "");
//			//327496
//			parts.add(map);
//		}else{
//			for(Element em:ems){
//				String fvars = em.attr("flashvars");
//				if(fvars!=null&&!fvars.equals("")&&fvars!=""){
//					String type = null;
//					String id1 = null;
//					String regex = "id=(\\w+)";
//					Pattern pattern = Pattern.compile(regex);
//					 Matcher matcher = pattern.matcher(fvars);
//					 while(matcher.find()){
//						 id1 = matcher.group(1);
//						 break;
//					 }
//					
//						String regext = "type(|\\w)=(\\w*)";
//						Pattern patternt = Pattern.compile(regext);
//						 Matcher matchert = patternt.matcher(fvars);
//						 while(matchert.find()){
//							 type = matchert.group(2);
//						 }
//					
//					
//					HashMap<String, String> map = new HashMap<String, String>();
//					map.put("type", type);
//					map.put("id", id1);
//					map.put("vars", fvars);
//					parts.add(map);
//					
//				}else{
//					if(em.attr("src").split("\\?").length>1){
//						String attr = em.attr("src").split("\\?")[1];
//						String type=attr.split("\\&")[1].split("\\=")[1];
//						String id1 = attr.split("\\&")[0].split("\\=")[1];
//						HashMap<String, String> map = new HashMap<String, String>();
//						map.put("type", type);
//						map.put("id", id1);
//						map.put("vars", attr);
//						parts.add(map);
//					}else{
//						String attr = em.attr("src");
//						HashMap<String, String> map = new HashMap<String, String>();
//						map.put("type", "game");
//						map.put("id", attr);
//						
//						parts.add(map);
//					}
//				}
//				
//				}
//		}
		return parts;
	}
	
	public static ArrayList<String> ParserVideopath(String type,String id) throws Exception{
		if(type.equals("sina")){
			//新浪
			return getSinaflv(id);
		}else if(type.equals("youku")){
			return ParserYoukuFlv(id);
		}else if(type.equals("qq")){
			return ParserQQvideof(id);
		}else if(type.equals("tudou")){
			
		}
		
		return null;
	}
	
	public static ArrayList<String> getSinaflv(String id) throws IOException{
		ArrayList<String> paths = new ArrayList<String>();
		String url = "http://v.iask.com/v_play.php?vid="+id;
		Connection c = Jsoup.connect(url);
		Document doc = c.get();
		Elements ems = doc.getElementsByTag("url");
		for(Element em:ems){
			paths.add(em.text());
		}
		
		return paths;
	}
	
	public static String ParserQQvideo(String vid) throws IOException{
		String url = "http://vv.video.qq.com/geturl?ran=0.16436194255948067&otype=xml&vid="+vid+"&platform=1&format=2";
		Connection c = Jsoup.connect(url);
		Document doc = c.get();
		Elements ems = doc.getElementsByTag("url");
		String vurls[] = ems.text().split("\\?");
		return vurls[0];
	}
	public static ArrayList<String> ParserQQvideof(String vid) throws IOException{
		String url = "http://web.qqvideo.tc.qq.com/" + vid + ".flv";
		ArrayList<String> urls = new ArrayList<String>();
		urls.add(url);
		return urls;
	}
	
	public static String ParserTudouvideo(String iid) throws IOException{
		String url = "http://v2.tudou.com/v?st=1%2C2%2C3%2C4%2C99&it="+iid;
		Connection c = Jsoup.connect(url);
		Document doc = c.get();
		Elements ems = doc.getElementsByTag("f");
		
		for(Element em:ems){
			em.attr("brt");
			String vurl[] = em.text().split("\\?");
		}
		return iid;
	}
	
	
	public static ArrayList<String> ParserYoukuFlv(String id) throws Exception{
		double seed = 0;
		String key1;
		String key2;
		String fileids = null;
		String fileid = null;
		ArrayList<String> K = new ArrayList<String>();
		URL url = new URL(
				"http://v.youku.com/player/getPlayList/VideoIDS/"+id+"/timezone/+08/version/5/source/video?n=3&ran=4656");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(6 * 1000);
		if (conn.getResponseCode() != 200)
			throw new RuntimeException("请求url失败");
		InputStream is = conn.getInputStream();
		String jsonstring = readData(is, "UTF8");
		conn.disconnect();
		
		
		String regexstring = "\"seed\":(\\d+),.+\"key1\":\"(\\w+)\",\"key2\":\"(\\w+)\"";
		Pattern pattern = Pattern.compile(regexstring);
		 Matcher matcher = pattern.matcher(jsonstring);
		 while(matcher.find()){
			 seed = Double.parseDouble(matcher.group(1));
			 key1 = matcher.group(2);
			 key2 = matcher.group(3);
		 }
		 	
		 	Pattern patternf = Pattern.compile("\"streamfileids\":\\{(.+?)\\}");

			 Matcher matcherf = patternf.matcher(jsonstring);
			 while(matcherf.find()){
				 fileids = matcherf.group(1);
			 }
			 
			 	Pattern patternfid = Pattern.compile("\"flv\":\"(.+?)\"");
			 	Matcher matcherfid = patternfid.matcher(fileids);
				 while(matcherfid.find()){
					 fileid = matcherfid.group(1);
				 }
				 	
				 String no =null;
				 	Pattern patternc = Pattern.compile("\"flv\":\\[(.+?)\\]");
				 	Matcher matcherc = patternc.matcher(jsonstring);
					 while(matcherc.find()){
						 no = matcherc.group(0);
					 }		 
					 
					 JSONArray array = new JSONArray(no.substring(6));
					 
					 for(int i=0;i<array.length();i++){
						 JSONObject job = (JSONObject) array.get(i);
						 K.add("?K=" + job.getString("k")+ ",k2:" + job.getString("k2"));
					 }
					 
					 String sid = genSid();
						//生成fileid
					 String rfileid = getFileID(fileid, seed);
					 ArrayList<String> paths = new ArrayList<String>();			 
		for (int i = 0; i < K.size(); i++)
		{
			//得到地址
			String u = "http://f.youku.com/player/getFlvPath/sid/" + "00" + "_" + String.format("%02d", i) +
				"/st/" + "flv" + "/fileid/" + rfileid.substring(0, 8) + String.format("%02d", i)
				+ rfileid.substring(10) + K.get(i);
			paths.add(u);
		}
		
		ArrayList<String> rpaths = new ArrayList<String>();
		for(String path:paths){
			rpaths.add(getLocationJump(path, false, false));
		}
		return rpaths;
	}
	
	
	/*感谢c大提供的方法-cALMER-flvshow -w-*/
	public static String getLocationJump(String httpurl,String agent,boolean followRedirects){
		String location=httpurl;
		try{
		 URL url = new URL(httpurl);
		 HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		 if(!followRedirects){
			 conn.setInstanceFollowRedirects(false);
			 conn.setFollowRedirects(false);
		 }
         
		 conn.addRequestProperty("User-Agent", agent);
     	 conn.setRequestProperty("User-Agent", agent);
         location=conn.getHeaderField("Location");
         if(location==null){
        	 location=httpurl;
         }
        if(!location.equalsIgnoreCase(httpurl)){
        	 location=getLocationJump(location,agent,followRedirects);
        	 
         }
         }catch (FileNotFoundException e) {
	            e.printStackTrace();
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	       return location;
	}
	 
	/*感谢c大提供的方法-cALMER-flvshow*/
	 public static String getLocationJump(String paramString, boolean paramBoolean1, boolean paramBoolean2)
	  {
	    String str = "Lavf52.106.0";
	    if (!paramBoolean1)
	      str = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.16 (KHTML, like Gecko) Chrome/10.0.648.151 Safari/534.16";
	    return getLocationJump(paramString, str, paramBoolean2);
	  }
	
	public static AcfunContent getContent(String id) throws IOException{
		Connection c = Jsoup.connect("http://www.acfun.tv/api/?id="+id+"&type=xml&current=yes&charset=utf8");
		Document doc = c.get();
		AcfunContent content = new AcfunContent();
		content.setArctitle(doc.getElementsByTag("arctitle").text());
		//content.setID( doc.getElementsByTag("ID").text());
		content.setPubdate(doc.getElementsByTag("pubdate").text());
		if(doc.getElementsByTag("typeid").size()!=0){
			content.setTypeid(doc.getElementsByTag("typeid").text());
		}else{
			content.setTypeid("0");
		}
		//content.setMemberID(doc.getElementsByTag("memberID").text());
		content.setUsername(doc.getElementsByTag("username").text());
		content.setDescription(doc.getElementsByTag("description").text());
		//content.setVideo(doc.getElementsByTag("video").text());
	//	content.setTypename( doc.getElementsByTag("typename").text());
		content.setKeywords(doc.getElementsByTag("keywords").text());
		content.setClick(doc.getElementsByTag("click").text());
		content.setStow(doc.getElementsByTag("stow").text());
		
		return content;
		
	}
	
	
	public static String ParserYoukuvideo(String id) throws Exception{

		URL url = new URL(
				"http://v.youku.com/player/getPlayList/VideoIDS/"+id+"/timezone/+08/version/5/source/video?n=3&ran=4656");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(6 * 1000);
		if (conn.getResponseCode() != 200)
			throw new RuntimeException("请求url失败");
		InputStream is = conn.getInputStream();
		String jsonstring = readData(is, "UTF8");
		conn.disconnect();
		JSONObject jsonObject = new JSONObject(jsonstring);
		
		JSONArray jsarray = jsonObject.getJSONArray("data");
		JSONObject obj = (JSONObject) jsarray.get(0);
		Double seed = obj.getDouble("seed");
		String key1 = obj.getString("key1");
		String key2 = obj.getString("key2");
		JSONObject obj1 = obj.getJSONObject("streamfileids");
		String flvfileid = obj1.getString("flv");
		String mp4fileid = obj1.getString("mp4");
		JSONObject obj2 = obj.getJSONObject("segs");
		JSONObject objflv = (JSONObject) obj2.getJSONArray("flv").get(0);
		JSONObject objmp4 = (JSONObject) obj2.getJSONArray("mp4").get(0);
		
		int flvno = objflv.getInt("no");
		String flvk = objflv.getString("k");
		
		int mp4no = objmp4.getInt("no");
		String mp4k = objmp4.getString("k");	
		
		for (int i = 0; i < mp4no+1; i++)
		{
			//得到地址
			String u = "http://f.youku.com/player/getFlvPath/sid/" + genSid() + "_" + String.format("%02d", i) +
				"/st/" + "flv" + "/fileid/" + getFileID(flvfileid, seed).substring(0, 8) + String.format("%02d", i)
				+ getFileID(flvfileid, seed).substring(10) + "?K=" + flvk;
			Log.i("youku", u);
		}
		return id;
	}
	
	
	public static String genKey(String key1, String key2) {
		int key = Long.valueOf("key1", 16).intValue();
		key ^= 0xA55AA5A5;
		return "key2" + Long.toHexString(key);
	}
	
	public static String getFileIDMixString(double seed) {
		StringBuilder mixed = new StringBuilder();
		StringBuilder source = new StringBuilder(
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ/\\:._-1234567890");
		int index, len = source.length();
		for (int i = 0; i < len; ++i) {
			seed = (seed * 211 + 30031) % 65536;
			index = (int) Math.floor(seed / 65536 * source.length());
			mixed.append(source.charAt(index));
			source.deleteCharAt(index);
		}
		return mixed.toString();
	}
	
	public static String getFileID(String fileid,double seed) {
		String mixed = getFileIDMixString(seed);
		String[] ids = fileid.split("\\*");
		StringBuilder realId = new StringBuilder();
		int idx;
		for (int i = 0; i < ids.length; i++) {
			idx = Integer.parseInt(ids[i]);
			realId.append(mixed.charAt(idx));
		}
		return realId.toString();
	}
	
	public static String genSid() {
		int i1 = (int) (1000 + Math.floor(Math.random() * 999));
		int i2 = (int) (1000 + Math.floor(Math.random() * 9000));
		return System.currentTimeMillis() + "" + i1 + "" + i2;
	}
	
	  
	public static String readData(InputStream inSream, String charsetName) throws Exception{
	    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	    byte[] buffer = new byte[1024];
	    int len = -1;
	    while( (len = inSream.read(buffer)) != -1 ){
	        outStream.write(buffer, 0, len);
	    }
	    byte[] data = outStream.toByteArray();
	    outStream.close();
	    inSream.close();
	    return new String(data, charsetName);
	}
	public static ArrayList<ArrayList<HashMap<String, String>>> getHotdata(String address) throws IOException{
		Connection c = Jsoup.connect(address);
		Document doc = c.get();
		Elements ems = doc.getElementsByAttributeValue("class", "cate_section");
		ArrayList<ArrayList<HashMap<String, String>>> hots = new ArrayList<ArrayList<HashMap<String, String>>>();

		for (Element em : ems) {
			ArrayList<HashMap<String, String>> chots = new ArrayList<HashMap<String, String>>();
			Elements trs = em.getElementsByTag("tr");
			trs.remove(0);
			for (Element tem : trs) {
				HashMap<String, String> map = new HashMap<String, String>();
				Elements tds = tem.getElementsByTag("td");
				String link =  tds.first().getElementsByTag("a").attr("href");
				String strs[] = link.split("/");
				link = strs[4].substring(2);
				map.put("link", link);
				map.put("title", tds.first().text());
				map.put("hit", tds.last().text());
				chots.add(map);
			}
			hots.add(chots);
		}
		return hots;
	}
	
	public static ArrayList<HashMap<String ,String>> getComment(String url) throws IOException{
		URL lurl;
		try {
			lurl = new URL(url);
			ArrayList<HashMap<String, String>> comments = new ArrayList<HashMap<String,String>>();
			HttpURLConnection conn = (HttpURLConnection) lurl.openConnection();
			conn.setConnectTimeout(6 * 1000);
			if (conn.getResponseCode() != 200)
				throw new RuntimeException("请求url失败");
			InputStream is = conn.getInputStream();
			String jsonstring = readData(is, "UTF8");
			conn.disconnect();

			JSONObject jsonObject = new JSONObject(jsonstring);
			JSONArray jsonArray = jsonObject.getJSONArray("commentList");
			if(jsonArray.length()>0){
				JSONObject comjsonobj = (JSONObject) jsonObject.get("commentContentArr");
				for(int i = 0 ;i<jsonArray.length();i++){
					HashMap<String, String> map = new HashMap<String, String>();
					JSONObject contentobj = comjsonobj.getJSONObject("c"+jsonArray.get(i).toString());
					map.put("user",(String) contentobj.get("userName"));
					map.put("time", (String) contentobj.get("postDate"));
					map.put("comment", (String) contentobj.get("content"));
					comments.add(map);
				}
				return comments;
			}else{
				return null;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static ArrayList<HashMap<String ,String>> getCommentwithpage(String url) throws IOException{
		Connection c = Jsoup.connect(url);
		Document doc = c.get();
		Elements ems = doc.getElementsByAttributeValue("class", "i");
		ArrayList<HashMap<String, String>> comments = new ArrayList<HashMap<String,String>>();
		if(!ems.isEmpty()){
			ems.remove(0);
			for(Element em:ems){
				HashMap<String, String> map = new HashMap<String, String>();
				String user = em.getElementsByAttributeValue("class", "g").first().text();
				map.put("user",user);
				String time = em.getElementsByAttributeValue("class", "b").first().text();
				map.put("time", time);
				String comment = em.text();
				map.put("comment", comment);
				comments.add(map);
			}
			return comments;
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("user","");
		map.put("time", "");
		map.put("comment", "...");
		comments.add(map);
		return comments;
	}
	
	
}
