package com.google.sps.placeGuide;

import org.jetbrains.annotations.Nullable;

/** Class containing place guide's information. */
public class PlaceGuide {

  @Nullable
  private long id;

  private final String name;
  private final String audioKey; 
  private final String creatorId;
  private final boolean isPublic;

  // This is not the unique identifier of a place guide.
  private final String placeId;

  private final PlaceCoordinate coord;

  // Specify how long user usually spends to follow this place guide in minutes.
  @Nullable
  private final int length;

  @Nullable
  private final String desc, imgKey;

  private PlaceGuide(long id, String name, String audioKey, String creatorId, 
                                    String placeId, boolean isPublic, 
                                    PlaceCoordinate coord, int length, 
                                    String desc, String imgKey) {
    this.id = id;
    this.name = name;
    this.audioKey = audioKey;
    this.creatorId = creatorId;
    this.placeId = placeId;
    this.isPublic = isPublic;
    this.coord = coord;
    this.length = length;
    this.desc = desc;
    this.imgKey = imgKey;
  }

  public static class Builder {
    private long id;
    private final String name;
    private final String audioKey;
    private final String creatorId; 
    private boolean isPublic = false;
    private final String placeId;
    private final PlaceCoordinate coord;
    private int length;
    private String desc, imgKey;
        
    public Builder(String name, String audioKey, String creatorId, String placeId, 
                                                                   PlaceCoordinate coord) {
      this.name = name;
      this.audioKey = audioKey;
      this.creatorId = creatorId;
      this.placeId = placeId;
      this.coord = coord;
    }
    public Builder setId(long id) {
      this.id = id;
    }
    public Builder setPlaceGuideToPublic(boolean isPublic) {
      this.isPublic = isPublic;
      return this;
    }
    public Builder setLength(int length) {
      this.length = length;
      return this;
    }
    public Builder setDescription(String desc) {
      this.desc = desc;
      return this;
    }
    public Builder setImageUrl(String imgKey) {
      this.imgKey = imgKey;
      return this;
    }
    public UserAuthenticationStatus build() {
      return new PlaceGuide(id, name, audioKey, creatorId, placeId, isPublic, 
                                            coord, length, desc, imgKey);
    }
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getAudioKey() {
    return audioKey;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public String getPlaceId() {
    return placeId;
  }

  public PlaceCoordinate getCoordinate() {
    return coord;
  }

  public boolean isPublic() {
    return isPublic;
  }

  @Nullable
  public String getLength() {
    return length;
  }

  @Nullable
  public String getDescription() {
    return desc;
  }

  @Nullable
  public String getImageKey() {
    return imgKey;
  }
}