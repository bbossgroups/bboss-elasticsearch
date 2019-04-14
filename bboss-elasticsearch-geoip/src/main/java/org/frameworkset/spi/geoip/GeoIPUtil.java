package org.frameworkset.spi.geoip;
/**
 * Copyright 2008 biaoping.yin
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.frameworkset.elasticsearch.entity.geo.GeoPoint;
import org.frameworkset.spi.remote.http.HttpRequestUtil;
import org.frameworkset.spi.remote.http.MapResponseHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Description: 通过淘宝api获取ip对应的相关信息</p>
 * <p>返回参数详解
 *
 * code
 * 状态码，正常为0，异常的时候为非0。
 * data
 * 查询到的结果。
 * country
 * 国家。
 * country_id
 * 国家代码。
 * area
 * 地区名称（华南、华北...）。
 * area_id
 * 地区编号。
 * region
 * 省名称。
 * region_id
 * 省编号。
 * city
 * 市名称。
 * city_id
 * 市编号。
 * county
 * 县名称。
 * county_id
 * 县编号。
 * isp
 * ISP服务商名称（电信/联通/铁通/移动...）。
 * isp_id
 * ISP服务商编号。
 * ip
 * 查询的IP地址。</p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/3/25 18:45
 * @author biaoping.yin
 * @version 1.0
 */
public class GeoIPUtil {
	private GeoIPFilter geoIPFilter;

	public GeoIPFilter getGeoIPFilter() {
		return geoIPFilter;
	}

	public void init(){
		geoIPFilter = new GeoIPFilter(database,asnDatabase,cachesize);
	}

	public void setGeoIPFilter(GeoIPFilter geoIPFilter) {
		this.geoIPFilter = geoIPFilter;
	}

	private  String database;

	public String getAsnDatabase() {
		return asnDatabase;
	}

	public void setAsnDatabase(String asnDatabase) {
		this.asnDatabase = asnDatabase;
	}

	private  String asnDatabase;
	private int cachesize;
	public GeoIPUtil(){

	}
	public String getIpUrl() {
		return ipUrl;
	}

	public void setIpUrl(String ipUrl) {
		this.ipUrl = ipUrl;
	}

	private String ipUrl;
	public static void main(String[] args) {
		// 测试ip 221.232.245.73 湖北武汉
		String ip = "localhost";
		String address = "";
		try {
			GeoIPUtil addressUtils = new GeoIPUtil();
			addressUtils.setIpUrl("http://ip.taobao.com/service/getIpInfo.php");
         	address = addressUtils.getAddressResult("218.104.155.137");

		} catch (Exception e) {
			 e.printStackTrace();
		}
        System.out.println(address);
         // 输出结果为：中国 湖北省 武汉市
	}


//	/**
//	 * @param content
//	 *   请求的参数 格式为：name=xxx&pwd=xxx
//	 *   服务器端请求编码。如GBK,UTF-8等
//	 * @return
//	 * @throws UnsupportedEncodingException
//	 */
//	public String getAddresses(String content ) throws UnsupportedEncodingException {
//		// 这里调用pconline的接口
//		String urlStr = ipUrl;
//		// 从http://whois.pconline.com.cn取得IP所在的省市区信息
//		String returnStr = this.getResult(urlStr, content );
//		if (returnStr != null) {
//			// 处理返回的省市区信息
//			System.out.println("IP====="+returnStr);
//			String[] temp = returnStr.split(",");
//			if(temp.length<3){
//				return "0";                                        //无效IP，局域网测试
//			}
//			String region = (temp[5].split(":"))[1].replaceAll("\"", "");
//			region = decodeUnicode(region);                        // 省
//			System.out.println("region = "+region);
//
//			String country = "";
//			String area = "";
//			// String region = "";
//			String city = "";
//			String county = "";
//			String isp = "";
//			System.out.println("temp的长度="+temp.length);
//			for (int i = 0; i < temp.length; i++) {
//				switch (i) {
//					//　如果使用的是新浪的接口，那这里的需要修改，case:3 4 5分别对应国家，省，市区
//					case 1:
//						country = (temp[i].split(":"))[2].replaceAll("\"", "");
//						country = decodeUnicode(country);            // 国家
//						break;
//					case 3:
//						area = (temp[i].split(":"))[1].replaceAll("\"", "");
//						area = decodeUnicode(area);                // 地区
//						break;
//					case 5:
//						region = (temp[i].split(":"))[1].replaceAll("\"", "");
//						region = decodeUnicode(region);            // 省份
//						break;
//					case 7:
//						city = (temp[i].split(":"))[1].replaceAll("\"", "");
//						city = decodeUnicode(city);                // 市区
//						break;
//					case 9:
//						county = (temp[i].split(":"))[1].replaceAll("\"", "");
//						county = decodeUnicode(county);            // 地区
//						break;
//					case 11:
//						isp = (temp[i].split(":"))[1].replaceAll("\"", "");
//						isp = decodeUnicode(isp);                 // ISP公司
//						break;
//				}
//			}
////			System.out.println(country+"="+area+"="+region+"="+city+"="+county+"="+isp);
//			return region;
//		}
//		return null;
//	}

	public String getAddressResult(String ip)  {

		StringBuilder url = new StringBuilder();
		url.append(ipUrl).append("?ip=").append(ip);
		Map header = new HashMap();
		header.put("Content-Type","text/html;charset=UTF-8");
		header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
		try {
			return HttpRequestUtil.httpGetforString(url.toString(),header);
		} catch (Exception e) {
			url.setLength(0);
			url.append("获取运营商区域信息异常:").append(ipUrl).append("?ip=").append(ip)
					.append(",User-Agent:Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36")
					.append(",Content-Type:text/html;charset=UTF-8");
			throw new GeoIPHandlerException("获取运营商区域信息异常:"+url.toString(),e);
		}
	}

	public IpInfo getAddressMapResult(String ip)  {





			ip = "240e:c0:f450:cb84:5dc4:928c:cd42:342b";
		//从geolite2获取ip地址信息
		Map<String,Object> geoData_ = this.geoIPFilter.handleIp(ip);
//			Map<String,Object> taobaodata = HttpRequestUtil.httpGetforString(url.toString(),header,new MapResponseHandler());

//			ip = "117.158.148.162";
//			geoData_ = this.geoIPFilter.handleIp(ip);


		if(geoData_ != null && geoData_.size() > 0) {//处理从geolite2获取ip地址信息
			Map<String, Object> asnData_ = this.geoIPFilter.handleIpAsn(ip);
			IpInfo ipInfo = new IpInfo();
			ipInfo.setIp(ip);
			ipInfo.setRegionId((String)geoData_.get("regionCode"));
			ipInfo.setArea("");
			ipInfo.setAreaId("");
			ipInfo.setCity((String)geoData_.get("cityName"));
			ipInfo.setCityId("");
			ipInfo.setCountry((String)geoData_.get("countryName"));
			ipInfo.setCountryId((String)geoData_.get("countryCode2"));
			ipInfo.setCounty((String)geoData_.get("regionName"));
			ipInfo.setCountyId((String)geoData_.get("regionCode"));
			ipInfo.setIsp((String)asnData_.get("asOrg"));
			ipInfo.setIspId((Integer)asnData_.get("asn"));
			ipInfo.setRegion((String)geoData_.get("regionName"));
			ipInfo.setRegionId((String)geoData_.get("regionCode"));
			Double latitude = (Double) geoData_.get("latitude");
			Double longitude = (Double) geoData_.get("longitude");
			GeoPoint geoPoint = new GeoPoint();
			geoPoint.setLat(latitude);
			geoPoint.setLon(longitude);
			ipInfo.setGeoPoint(geoPoint);
			return ipInfo;
		}
		else{//如果没有
			StringBuilder url = new StringBuilder();
			url.append(ipUrl).append("?ip=").append(ip);
			try {
				Map header = new HashMap();
//		StringBuilder url = new StringBuilder();
				header.put("Content-Type","text/html;charset=UTF-8");
				header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
				Map<String, Object> data = HttpRequestUtil.httpGetforString(url.toString(), header, new MapResponseHandler());
				Integer code = (Integer) data.get("code");
				if (code != null && code == 0) {
					Map<String, Object> ipdata = (Map<String, Object>) data.get("data");
					IpInfo ipInfo = new IpInfo();
					ipInfo.setArea((String) ipdata.get("area"));
					ipInfo.setAreaId((String) ipdata.get("area_id"));
					ipInfo.setCity((String) ipdata.get("city"));
					ipInfo.setCityId((String) ipdata.get("city_id"));
					ipInfo.setCountry((String) ipdata.get("country"));
					ipInfo.setCountryId((String) ipdata.get("country_Id"));
					ipInfo.setCounty((String) ipdata.get("county"));
					ipInfo.setCountyId((String) ipdata.get("county_id"));
					ipInfo.setIp((String) ipdata.get("ip"));
					ipInfo.setIsp((String) ipdata.get("isp"));
					ipInfo.setIspId((Integer) ipdata.get("isp_id"));
					ipInfo.setRegion((String) ipdata.get("region"));
					ipInfo.setRegionId((String) ipdata.get("region_id"));
					//					Float latitude = (Float) geoData_.get("latitude");
					//					Float longitude = (Float) geoData_.get("longitude");
					//					ipInfo.setLatitude(latitude);
					//					ipInfo.setLongitude(longitude);
					return ipInfo;
				} else {
					IpInfo ipInfo = new IpInfo();
					ipInfo.setArea("未知");
					ipInfo.setAreaId("未知");
					ipInfo.setCity("未知");
					ipInfo.setCityId("未知");
					ipInfo.setCountry("未知");
					ipInfo.setCountryId("未知");
					ipInfo.setCounty("未知");
					ipInfo.setCountyId("未知");
					ipInfo.setIp(ip);
					ipInfo.setIsp("未知");
					ipInfo.setIspId(null);
					ipInfo.setRegion("未知");
					ipInfo.setRegionId("未知");
					return ipInfo;
				}
			}
			 catch (Exception e) {
				url.setLength(0);
				url.append("获取运营商区域信息异常:").append(ipUrl).append("?ip=").append(ip)
						.append(",User-Agent:Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36")
						.append(",Content-Type:text/html;charset=UTF-8");
				throw new GeoIPHandlerException("获取运营商区域信息异常:"+url.toString(),e);
			}
		}
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public int getCachesize() {
		return cachesize;
	}

	public void setCachesize(int cachesize) {
		this.cachesize = cachesize;
	}


//	/**
//	 * unicode 转换成 中文
//	 *
//	 * @author fanhui 2007-3-15
//	 * @param theString
//	 * @return
//	 */
//	public static String decodeUnicode(String theString) {
//		char aChar;
//		int len = theString.length();
//		StringBuffer outBuffer = new StringBuffer(len);
//		for (int x = 0; x < len;) {
//			aChar = theString.charAt(x++);
//			if (aChar == '\\') {
//				aChar = theString.charAt(x++);
//				if (aChar == 'u') {
//					int value = 0;
//					for (int i = 0; i < 4; i++) {
//						aChar = theString.charAt(x++);
//						switch (aChar) {
//							case '0':
//							case '1':
//							case '2':
//							case '3':
//							case '4':
//							case '5':
//							case '6':
//							case '7':
//							case '8':
//							case '9':
//								value = (value << 4) + aChar - '0';
//								break;
//							case 'a':
//							case 'b':
//							case 'c':
//							case 'd':
//							case 'e':
//							case 'f':
//								value = (value << 4) + 10 + aChar - 'a';
//								break;
//							case 'A':
//							case 'B':
//							case 'C':
//							case 'D':
//							case 'E':
//							case 'F':
//								value = (value << 4) + 10 + aChar - 'A';
//								break;
//							default:
//								throw new IllegalArgumentException(
//										"Malformed  encoding.");
//						}
//					}
//					outBuffer.append((char) value);
//				} else {
//					if (aChar == 't') {
//						aChar = '\t';
//					} else if (aChar == 'r') {
//						aChar = '\r';
//					} else if (aChar == 'n') {
//						aChar = '\n';
//					} else if (aChar == 'f') {
//						aChar = '\f';
//					}
//					outBuffer.append(aChar);
//				}
//			} else {
//				outBuffer.append(aChar);
//			}
//		}
//		return outBuffer.toString();
//	}
}
