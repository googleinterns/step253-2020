package com.google.sps.placeGuide.repository.impl;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.sps.placeGuide.PlaceGuide;
import com.google.appengine.api.datastore.GeoPt;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.jetbrains.annotations.Nullable;
import com.google.sps.placeGuide.repository.PlaceGuideRepository;

/** Class for handling place guide repository using datastore. */
public class DatastorePlaceGuideRepository implements PlaceGuideRepository {

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  public static final String ENTITY_KIND = "PlaceGuide";
  public static final String NAME_PROPERTY = "name";
  public static final String AUDIO_KEY_PROPERTY = "audioKey";
  public static final String CREATOR_ID_PROPERTY = "creatorId";
  public static final String PLACE_ID_PROPERTY = "placeId";
  public static final String IS_PUBLIC_PROPERTY = "isPublic";
  public static final String COORDINATE_PROPERTY = "coordinate";
  public static final String DESCRIPTION_PROPERTY = "description";
  public static final String LENGTH_PROPERTY = "length";
  public static final String IMAGE_KEY_PROPERTY = "imageKey";
  
  @Override
  public void savePlaceGuide(PlaceGuide placeGuide) {
    Entity placeGuideEntity = createPlaceGuideEntity(placeGuide);
    datastore.put(placeGuideEntity);
  }

  private Entity createPlaceGuideEntity(PlaceGuide placeGuide) {
    Entity placeGuideEntity;

    String name = placeGuide.getName();
    String audioKey = placeGuide.getAudioKey();
    String creatorId = placeGuide.getCreatorId();
    String placeId = placeGuide.getPlaceId();
    boolean isPublic = placeGuide.isPublic();
    GeoPt coordinate = placeGuide.getCoordinate();
    String description = placeGuide.getDescription();
    long length = placeGuide.getLength();
    String imageKey = placeGuide.getImageKey();

    if (Long.valueOf(placeGuide.getId()) != null) {
      // Use the original placeGuide's ID for when user edits a place guide.
      placeGuideEntity = new Entity(ENTITY_KIND, placeGuide.getId());
    } else {
      // Let the ID be automatically created if the place guide is new.
      placeGuideEntity = new Entity(ENTITY_KIND);

      // Update the {@code PlaceGuide} object with the updated placeGuide Id.
      placeGuide = new PlaceGuide
               .Builder(placeGuide.getName(), 
                        placeGuide.getAudioKey(), 
                        placeGuide.getCreatorId(), 
                        placeGuide.getCoordinate())
               .setId(placeGuideEntity.getKey().getId())
               .setPlaceId(placeGuide.getPlaceId())
               .setLength(placeGuide.getLength())
               .setDescription(placeGuide.getDescription())
               .setImageKey(placeGuide.getImageKey())
               .setPlaceGuideStatus(placeGuide.isPublic())
               .build();
    }

    placeGuideEntity.setProperty(NAME_PROPERTY, placeGuide.getName());
    placeGuideEntity.setProperty(AUDIO_KEY_PROPERTY, placeGuide.getAudioKey());
    placeGuideEntity.setProperty(CREATOR_ID_PROPERTY, placeGuide.getCreatorId());
    placeGuideEntity.setProperty(PLACE_ID_PROPERTY, placeGuide.getPlaceId());
    placeGuideEntity.setProperty(IS_PUBLIC_PROPERTY, placeGuide.isPublic());
    placeGuideEntity.setProperty(COORDINATE_PROPERTY, placeGuide.getCoordinate());
    placeGuideEntity.setProperty(DESCRIPTION_PROPERTY, placeGuide.getDescription());
    placeGuideEntity.setProperty(LENGTH_PROPERTY, placeGuide.getLength());
    placeGuideEntity.setProperty(IMAGE_KEY_PROPERTY, placeGuide.getImageKey());

    return placeGuideEntity;
  }

  @Override
  public List<PlaceGuide> getAllPublicPlaceGuides() {
    Filter queryFilter = new FilterPredicate(IS_PUBLIC_PROPERTY, FilterOperator.EQUAL, true);
    Query query = new Query(ENTITY_KIND).setFilter(queryFilter);
    return getPlaceGuidesList(query);
  }

  @Override
  public List<PlaceGuide> getCreatedPlaceGuides(String creatorId) {
    Filter queryFilter = new FilterPredicate(CREATOR_ID_PROPERTY, FilterOperator.EQUAL, creatorId);
    Query query = new Query(ENTITY_KIND).setFilter(queryFilter);
    return getPlaceGuidesList(query);
  }

  @Override
  public List<PlaceGuide> getCreatedPublicPlaceGuides(String creatorId) {
    Filter queryFilter = CompositeFilterOperator.and(Arrays.asList(
                        new FilterPredicate(CREATOR_ID_PROPERTY, FilterOperator.EQUAL, creatorId),
                        new FilterPredicate(IS_PUBLIC_PROPERTY, FilterOperator.EQUAL, true)));
    Query query = new Query(ENTITY_KIND).setFilter(queryFilter);
    return getPlaceGuidesList(query);
  }

  @Override
  public List<PlaceGuide> getCreatedPrivatePlaceGuides(String creatorId) {
    Filter queryFilter = CompositeFilterOperator.and(Arrays.asList(
                        new FilterPredicate(CREATOR_ID_PROPERTY, FilterOperator.EQUAL, creatorId),
                        new FilterPredicate(IS_PUBLIC_PROPERTY, FilterOperator.EQUAL, false)));
    Query query = new Query(ENTITY_KIND).setFilter(queryFilter);
    return getPlaceGuidesList(query);
  }

  private List<PlaceGuide> getPlaceGuidesList(Query query) {
    PreparedQuery results = datastore.prepare(query);
    List<PlaceGuide> createdPlaceGuides = new ArrayList<>();
    for (Entity placeGuideEntity : results.asIterable()) {
      PlaceGuide placeGuide = getPlaceGuideFromEntity(placeGuideEntity);
      createdPlaceGuides.add(placeGuide);
    }
    return Collections.unmodifiableList(createdPlaceGuides);
  }

  private PlaceGuide getPlaceGuideFromEntity(Entity placeGuideEntity) {
    long id = placeGuideEntity.getKey().getId();
    String name = (String) placeGuideEntity.getProperty(NAME_PROPERTY);
    String audioKey = (String) placeGuideEntity.getProperty(AUDIO_KEY_PROPERTY);
    String creatorId = (String) placeGuideEntity.getProperty(CREATOR_ID_PROPERTY);
    String placeId = (String) placeGuideEntity.getProperty(PLACE_ID_PROPERTY);
    GeoPt coordinate = (GeoPt) placeGuideEntity.getProperty(COORDINATE_PROPERTY);
    boolean isPublic = (boolean) placeGuideEntity.getProperty(IS_PUBLIC_PROPERTY);
    String description = (String) placeGuideEntity.getProperty(DESCRIPTION_PROPERTY);
    long length = (long) placeGuideEntity.getProperty(LENGTH_PROPERTY);
    String imageKey = (String) placeGuideEntity.getProperty(IMAGE_KEY_PROPERTY);
    PlaceGuide placeGuide = new PlaceGuide
                            .Builder(name, audioKey, creatorId, coordinate)
                            .setId(id)
                            .setPlaceId(placeId)
                            .setLength(length)
                            .setDescription(description)
                            .setImageKey(imageKey)
                            .setPlaceGuideStatus(isPublic)
                            .build();
    return placeGuide;
  }

  @Override
  public void deletePlaceGuide(long placeGuideId) {
    Key placeGuideEntityKey = KeyFactory.createKey(ENTITY_KIND, placeGuideId);
    datastore.delete(placeGuideEntityKey);
  }
}