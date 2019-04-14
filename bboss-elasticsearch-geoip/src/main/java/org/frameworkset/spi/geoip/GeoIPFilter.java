/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.frameworkset.spi.geoip;

import com.maxmind.db.CHMCache;
import com.maxmind.db.InvalidDatabaseException;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.AsnResponse;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.model.IspResponse;
import com.maxmind.geoip2.record.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class GeoIPFilter {
  private static Logger logger = LoggerFactory.getLogger(GeoIPFilter.class);
  // The free GeoIP2 databases
  private static final String CITY_LITE_DB_TYPE = "GeoLite2-City";
  private static final String COUNTRY_LITE_DB_TYPE = "GeoLite2-Country";
  private static final String ASN_LITE_DB_TYPE = "GeoLite2-ASN";

  // The paid GeoIP2 databases
  private static final String CITY_DB_TYPE = "GeoIP2-City";
  private static final String CITY_AFRICA_DB_TYPE = "GeoIP2-City-Africa";
  private static final String CITY_ASIA_PACIFIC_DB_TYPE = "GeoIP2-City-Asia-Pacific";
  private static final String CITY_EUROPE_DB_TYPE = "GeoIP2-City-Europe";
  private static final String CITY_NORTH_AMERICA_DB_TYPE = "GeoIP2-City-North-America";
  private static final String CITY_SOUTH_AMERICA_DB_TYPE = "GeoIP2-City-South-America";
  private static final String COUNTRY_DB_TYPE = "GeoIP2-Country";
  private static final String ISP_DB_TYPE = "GeoIP2-ISP";

  private final Set<Fields> desiredFields;
  private final Set<Fields> asnDesiredFields;
  private final DatabaseReader databaseReader;
  private final DatabaseReader asnDatabaseReader;

  /**
   *
   * @param databasePath
   */
  public GeoIPFilter(String databasePath,String asnDatabasePath){
      this(databasePath,  asnDatabasePath,2000);
  }
  /**
   *
   * @param databasePath
   * @param cacheSize 默认值 1000
   */
  public GeoIPFilter(String databasePath,String asnDatabasePath, int cacheSize) {

    final File database = new File(databasePath);
    final File asnDatabase = new File(asnDatabasePath);
    try {
      this.databaseReader = new DatabaseReader.Builder(database).withCache(new CHMCache(cacheSize)).build();
      this.asnDatabaseReader = new DatabaseReader.Builder(asnDatabase).withCache(new CHMCache(cacheSize)).build();
    } catch (InvalidDatabaseException e) {
      throw new IllegalArgumentException("The database provided is invalid or corrupted.", e);
    } catch (IOException e) {
      throw new IllegalArgumentException("The database provided was not found in the path", e);
    }
    this.desiredFields = createDesiredFields();
    this.asnDesiredFields = createAsnDesiredFields();
  }

  private Set<Fields> createDesiredFields() {
    Set<Fields> desiredFields = null;
    String databaseType = databaseReader.getMetadata().getDatabaseType();
    if (databaseType.equals(CITY_LITE_DB_TYPE)
            || databaseType.equals(CITY_DB_TYPE)
            || databaseType.equals(CITY_AFRICA_DB_TYPE)
            || databaseType.equals(CITY_ASIA_PACIFIC_DB_TYPE)
            || databaseType.equals(CITY_EUROPE_DB_TYPE)
            || databaseType.equals(CITY_NORTH_AMERICA_DB_TYPE)
            || databaseType.equals(CITY_SOUTH_AMERICA_DB_TYPE)) {

      desiredFields = Fields.DEFAULT_CITY_FIELDS;
    }
    else if (databaseType.equals(COUNTRY_LITE_DB_TYPE)
            || databaseType.equals(COUNTRY_DB_TYPE) ) {
      desiredFields = Fields.DEFAULT_COUNTRY_FIELDS;
    }
    else if (databaseType.equals(ISP_DB_TYPE) ) {
        desiredFields = Fields.DEFAULT_ISP_FIELDS;
    }
    else if (databaseType.equals(ASN_LITE_DB_TYPE) ){
        desiredFields = Fields.DEFAULT_ASN_LITE_FIELDS;

    }
    return desiredFields;
  }

  private Set<Fields> createAsnDesiredFields() {
    Set<Fields> desiredFields = null;

        desiredFields = Fields.DEFAULT_ASN_LITE_FIELDS;

    return desiredFields;
  }
  public Map<String, Object>  handleIpAsn(String ip) {
    if (ip.trim().isEmpty()){
      return new HashMap<String, Object>();
    }

    Map<String, Object> geoData = new HashMap<String, Object>();

    try {
      final InetAddress ipAddress = InetAddress.getByName(ip);

          geoData = retrieveAsnGeoData(ipAddress);


    } catch (UnknownHostException e) {
      logger.debug("IP Field contained invalid IP address or hostname. exception={}", e);
    } catch (AddressNotFoundException e) {
      logger.debug("IP not found! exception={}", e);
    } catch (GeoIp2Exception   e) {
      logger.debug("GeoIP2 Exception. exception={}", e);
    }
    catch (  IOException e) {
      logger.debug("GeoIP2 Exception. exception={}", e);
    }
    return geoData;
//    return applyGeoData(geoData, event);
  }
  public Map<String, Object>  handleIp(String ip) {
    if (ip.trim().isEmpty()){
      return new HashMap<String, Object>();
    }

    Map<String, Object> geoData = new HashMap<String, Object>();

    try {
      final InetAddress ipAddress = InetAddress.getByName(ip);
      String databaseType = databaseReader.getMetadata().getDatabaseType();
      if(databaseType.equals(CITY_LITE_DB_TYPE)
              || databaseType.equals(CITY_DB_TYPE)
              || databaseType.equals(CITY_AFRICA_DB_TYPE)
              || databaseType.equals(CITY_ASIA_PACIFIC_DB_TYPE)
              || databaseType.equals(CITY_EUROPE_DB_TYPE)
              || databaseType.equals(CITY_NORTH_AMERICA_DB_TYPE)
              || databaseType.equals(CITY_SOUTH_AMERICA_DB_TYPE) ) {


          geoData = retrieveCityGeoData(ipAddress);
      }
      else if(databaseType.equals(COUNTRY_LITE_DB_TYPE)
              || databaseType.equals(COUNTRY_DB_TYPE)) {

        geoData = retrieveCountryGeoData(ipAddress);
      }
      else if(databaseType.equals(ASN_LITE_DB_TYPE)) {
          geoData = retrieveAsnGeoData(ipAddress);
      }
      else if(databaseType.equals(ISP_DB_TYPE)) {
        geoData = retrieveIspGeoData(ipAddress);
      }
      else{
          throw new IllegalStateException("Unsupported database type " + databaseReader.getMetadata().getDatabaseType() + "");
      }
    } catch (UnknownHostException e) {
      logger.debug("IP Field contained invalid IP address or hostname. exception={}", e);
    } catch (AddressNotFoundException e) {
      logger.debug("IP not found! exception={}", e);
    } catch (GeoIp2Exception  e) {
      logger.debug("GeoIP2 Exception. exception={}", e);
    }
    catch (  IOException e) {
      logger.debug("GeoIP2 Exception. exception={}", e);
    }
    return geoData;
//    return applyGeoData(geoData, event);
  }

  private Map<String,Object> retrieveCityGeoData(InetAddress ipAddress) throws GeoIp2Exception, IOException {
    CityResponse response = databaseReader.city(ipAddress);
    Country country = response.getCountry();
    City city = response.getCity();
    Location location = response.getLocation();
    Continent continent = response.getContinent();
    Postal postal = response.getPostal();
    Subdivision subdivision = response.getMostSpecificSubdivision();
    Map<String, Object> geoData = new HashMap<String, Object>();

    // if location is empty, there is no point populating geo data
    // and most likely all other fields are empty as well
    if (location.getLatitude() == null && location.getLongitude() == null) {
      return geoData;
    }

    for (Fields desiredField : this.desiredFields) {
      switch (desiredField) {
        case CITY_NAME:
          String cityName = city.getNames().get("zh-CN");
          if (cityName != null) {
            geoData.put(Fields.CITY_NAME.fieldName(), cityName);
          }
          break;
        case CONTINENT_CODE:
          String continentCode = continent.getCode();
          if (continentCode != null) {
            geoData.put(Fields.CONTINENT_CODE.fieldName(), continentCode);
          }
          break;
        case CONTINENT_NAME:
          String continentName = continent.getNames().get("zh-CN");
          if (continentName != null) {
            geoData.put(Fields.CONTINENT_NAME.fieldName(), continentName);
          }
          break;
        case COUNTRY_NAME:
          String countryName = country.getNames().get("zh-CN");
          if (countryName != null) {
            geoData.put(Fields.COUNTRY_NAME.fieldName(), countryName);
          }
          break;
        case COUNTRY_CODE2:
          String countryCode2 = country.getIsoCode();
          if (countryCode2 != null) {
            geoData.put(Fields.COUNTRY_CODE2.fieldName(), countryCode2);
          }
          break;
        case COUNTRY_CODE3:
          String countryCode3 = country.getIsoCode();
          if (countryCode3 != null) {
            geoData.put(Fields.COUNTRY_CODE3.fieldName(), countryCode3);
          }
          break;
        case IP:
          geoData.put(Fields.IP.fieldName(), ipAddress.getHostAddress());
          break;
        case POSTAL_CODE:
          String postalCode = postal.getCode();
          if (postalCode != null) {
            geoData.put(Fields.POSTAL_CODE.fieldName(), postalCode);
          }
          break;
        case DMA_CODE:
          Integer dmaCode = location.getMetroCode();
          if (dmaCode != null) {
            geoData.put(Fields.DMA_CODE.fieldName(), dmaCode);
          }
          break;
        case REGION_NAME:
          String subdivisionName = subdivision.getNames().get("zh-CN");
          if (subdivisionName != null) {
            geoData.put(Fields.REGION_NAME.fieldName(), subdivisionName);
          }
          break;
        case REGION_CODE:
          String subdivisionCode = subdivision.getIsoCode();
          if (subdivisionCode != null) {
            geoData.put(Fields.REGION_CODE.fieldName(), subdivisionCode);
          }
          break;
        case TIMEZONE:
          String locationTimeZone = location.getTimeZone();
          if (locationTimeZone != null) {
            geoData.put(Fields.TIMEZONE.fieldName(), locationTimeZone);
          }
          break;
        case LOCATION:
          Double latitude = location.getLatitude();
          Double longitude = location.getLongitude();
          if (latitude != null && longitude != null) {
            Map<String, Object> locationObject = new HashMap<String, Object>();
            locationObject.put("lat", latitude);
            locationObject.put("lon", longitude);
            geoData.put(Fields.LOCATION.fieldName(), locationObject);
          }
          break;
        case LATITUDE:
          Double lat = location.getLatitude();
          if (lat != null) {
            geoData.put(Fields.LATITUDE.fieldName(), lat);
          }
          break;
        case LONGITUDE:
          Double lon = location.getLongitude();
          if (lon != null) {
            geoData.put(Fields.LONGITUDE.fieldName(), lon);
          }
          break;
      }
    }

    return geoData;
  }

  private Map<String,Object> retrieveCountryGeoData(InetAddress ipAddress) throws GeoIp2Exception, IOException {
    CountryResponse response = databaseReader.country(ipAddress);
    Country country = response.getCountry();
    Continent continent = response.getContinent();
    Map<String, Object> geoData = new HashMap<String, Object>();

    for (Fields desiredField : this.desiredFields) {
      switch (desiredField) {
        case IP:
          geoData.put(Fields.IP.fieldName(), ipAddress.getHostAddress());
          break;
        case COUNTRY_CODE2:
          String countryCode2 = country.getIsoCode();
          if (countryCode2 != null) {
            geoData.put(Fields.COUNTRY_CODE2.fieldName(), countryCode2);
          }
          break;
        case COUNTRY_NAME:
          String countryName = country.getNames().get("zh-CN");
          if (countryName != null) {
            geoData.put(Fields.COUNTRY_NAME.fieldName(), countryName);
          }
          break;
        case CONTINENT_NAME:
          String continentName = continent.getNames().get("zh-CN");
          if (continentName != null) {
            geoData.put(Fields.CONTINENT_NAME.fieldName(), continentName);
          }
          break;
      }
    }

    return geoData;
  }

  private Map<String, Object> retrieveIspGeoData(InetAddress ipAddress) throws GeoIp2Exception, IOException {
    IspResponse response = databaseReader.isp(ipAddress);

    Map<String, Object> geoData = new HashMap<String, Object>();
    for (Fields desiredField : this.desiredFields) {
      switch (desiredField) {
        case IP:
          geoData.put(Fields.IP.fieldName(), ipAddress.getHostAddress());
          break;
        case AUTONOMOUS_SYSTEM_NUMBER:
          Integer asn = response.getAutonomousSystemNumber();
          if (asn != null) {
            geoData.put(Fields.AUTONOMOUS_SYSTEM_NUMBER.fieldName(), asn);
          }
          break;
        case AUTONOMOUS_SYSTEM_ORGANIZATION:
          String aso = response.getAutonomousSystemOrganization();
          if (aso != null) {
            geoData.put(Fields.AUTONOMOUS_SYSTEM_ORGANIZATION.fieldName(), aso);
          }
          break;
        case ISP:
          String isp = response.getIsp();
          if (isp != null) {
            geoData.put(Fields.ISP.fieldName(), isp);
          }
          break;
        case ORGANIZATION:
          String org = response.getOrganization();
          if (org != null) {
            geoData.put(Fields.ORGANIZATION.fieldName(), org);
          }
          break;
      }
    }

    return geoData;
  }

  private Map<String, Object> retrieveAsnGeoData(InetAddress ipAddress) throws GeoIp2Exception, IOException {
    AsnResponse response = asnDatabaseReader.asn(ipAddress);
//    CityResponse cresponse = databaseReader.city(ipAddress);
    Map<String, Object> geoData = new HashMap<String, Object>();
    for (Fields desiredField : this.asnDesiredFields) {
      switch (desiredField) {
        case IP:
          geoData.put(Fields.IP.fieldName(), ipAddress.getHostAddress());
          break;
        case AUTONOMOUS_SYSTEM_NUMBER:
          Integer asn = response.getAutonomousSystemNumber();
          if (asn != null) {
            geoData.put(Fields.AUTONOMOUS_SYSTEM_NUMBER.fieldName(), asn);
          }
          break;
        case AUTONOMOUS_SYSTEM_ORGANIZATION:
          String aso = response.getAutonomousSystemOrganization();
          if (aso != null) {
            geoData.put(Fields.AUTONOMOUS_SYSTEM_ORGANIZATION.fieldName(), aso);
          }
          break;
      }
    }

    return geoData;
  }
}
