package com.kpelykh.docker.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

/**
 *
 * @author Konstantin Pelykh (kpelykh@gmail.com)
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("RepoTags")
    private String[] repoTags;

    @JsonProperty("Repository")
    private String repository;

    @JsonProperty("Tag")
    private String tag;


    @JsonProperty("ParentId")
    private String parentId;

    @JsonProperty("Created")
    private long created;

    @JsonProperty("Size")
    private long size;

    @JsonProperty("VirtualSize")
    private long virtualSize;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getRepoTags() {
        return repoTags;
    }

    public void setRepoTags(String[] repoTags) {
        this.repoTags = repoTags;
	    Arrays.sort(this.repoTags);
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getVirtualSize() {
        return virtualSize;
    }

    public void setVirtualSize(long virtualSize) {
        this.virtualSize = virtualSize;
    }

    @Override
    public String toString() {
        return "Image{" +
                "virtualSize=" + virtualSize +
                ", id='" + id + '\'' +
                ", repoTags=" + Arrays.toString(repoTags) +
                ", repository='" + repository + '\'' +
                ", tag='" + tag + '\'' +
                ", parentId='" + parentId + '\'' +
                ", created=" + created +
                ", size=" + size +
                '}';
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Image image = (Image) o;

		if (created != image.created) return false;
		if (size != image.size) return false;
		if (virtualSize != image.virtualSize) return false;
		if (id != null ? !id.equals(image.id) : image.id != null) return false;
		if (parentId != null ? !parentId.equals(image.parentId) : image.parentId != null) return false;
		if (!Arrays.equals(repoTags, image.repoTags)) return false;
		if (repository != null ? !repository.equals(image.repository) : image.repository != null) return false;
		if (tag != null ? !tag.equals(image.tag) : image.tag != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (repoTags != null ? Arrays.hashCode(repoTags) : 0);
		result = 31 * result + (repository != null ? repository.hashCode() : 0);
		result = 31 * result + (tag != null ? tag.hashCode() : 0);
		result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
		result = 31 * result + (int) (created ^ (created >>> 32));
		result = 31 * result + (int) (size ^ (size >>> 32));
		result = 31 * result + (int) (virtualSize ^ (virtualSize >>> 32));
		return result;
	}
}

