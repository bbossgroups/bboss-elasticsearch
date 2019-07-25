package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * health status index    uuid                   pri rep docs.count docs.deleted store.size pri.store.size
 * yellow open   twitter  u8FNjxh8Rfy_awN11oDKYQ   1   1       1200            0     88.1kb         88.1kb
 * green  open   twitter2 nYFWZEO7TUiOjLQXBaYJpA   5   0          0            0       260b           260b
 * .watcher-history-3-2017.07.02
 */
public class ESIndice {
    private String health;
    private String status ;
    private String index;
    private String indexName;
    private String uuid  ;
    private int pri ;
    private int rep ;
    @JsonProperty("docs.count")
    private long docsCcount;
    @JsonProperty("docs.deleted")
    private long docsDeleted ;
    @JsonProperty("store.size")
    private String storeSize ;
    @JsonProperty("pri.store.size")
    private String priStoreSize;
    private Date genDate;
    private Map<String,String> otherDatas;

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getPri() {
        return pri;
    }

    public void setPri(int pri) {
        this.pri = pri;
    }

    public int getRep() {
        return rep;
    }

    public void setRep(int rep) {
        this.rep = rep;
    }

    public long getDocsCcount() {
        return docsCcount;
    }

    public void setDocsCcount(long docsCcount) {
        this.docsCcount = docsCcount;
    }

    public long getDocsDeleted() {
        return docsDeleted;
    }

    public void setDocsDeleted(long docsDeleted) {
        this.docsDeleted = docsDeleted;
    }

    public String getStoreSize() {
        return storeSize;
    }

    public void setStoreSize(String storeSize) {
        this.storeSize = storeSize;
    }

    public String getPriStoreSize() {
        return priStoreSize;
    }

    public void setPriStoreSize(String priStoreSize) {
        this.priStoreSize = priStoreSize;
    }

    public Date getGenDate() {
        return genDate;
    }

    public void setGenDate(Date genDate) {
        this.genDate = genDate;
    }

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
    public void addOtherData(String name,String value){
        if(otherDatas == null)
            otherDatas = new HashMap<String, String>();
        otherDatas.put(name,value);
    }
    public Map<String, String> getOtherDatas() {
        return otherDatas;
    }
}
