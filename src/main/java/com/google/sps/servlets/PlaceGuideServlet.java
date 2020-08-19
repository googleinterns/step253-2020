package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.placeGuideWithUserPair.PlaceGuideWithUserPair;
import com.google.sps.data.RepositoryType;
import com.google.sps.data.PlaceGuideQueryType;
import com.google.sps.placeGuide.PlaceGuide;
import com.google.sps.placeGuide.repository.PlaceGuideRepository;
import com.google.sps.placeGuide.repository.PlaceGuideRepositoryFactory;
import com.google.sps.placeGuide.repository.impl.DatastorePlaceGuideRepository;
import com.google.appengine.api.datastore.GeoPt;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.lang.IllegalStateException;
import java.io.IOException;

/**
 * This servlet handles placeguide's data.
 */
@WebServlet("/place-guide-data")
public class PlaceGuideServlet extends HttpServlet {
  
  private final String userId;

  // For production.
  public PlaceGuideServlet() {
    this(UserServiceFactory.getUserService().getCurrentUser().getUserId());  
  }

  // For testing.
  public PlaceGuideServlet(String userId) {
    this.userId = userId;
  }

  public static final String ID_INPUT = "id";
  public static final String NAME_INPUT = "name";
  public static final String AUDIO_KEY_INPUT = "audioKey";
  public static final String PLACE_ID_INPUT = "placeId";
  public static final String IS_PUBLIC_INPUT = "isPublic";
  public static final String IS_PUBLIC_INPUT_VALUE = "public";
  public static final String LATITUDE_INPUT = "latitude";
  public static final String LONGITUDE_INPUT = "longitude";
  public static final String DESCRIPTION_INPUT = "description";
  public static final String LENGTH_INPUT = "length";
  public static final String IMAGE_KEY_INPUT = "imageKey";
  public static final String PLACE_GUIDE_QUERY_TYPE_PARAMETER = "placeGuideType";
  public static final String REGION_CORNERS_PARAMETER = "regionCorners";

  private final PlaceGuideRepository placeGuideRepository = 
      PlaceGuideRepositoryFactory.getPlaceGuideRepository(RepositoryType.DATASTORE);

  /**
   * Saves the recently submitted place guide data.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PlaceGuide placeGuide = getPlaceGuideFromRequest(request);
    placeGuideRepository.savePlaceGuide(placeGuide);
    response.sendRedirect("/createPlaceGuide.html");
  }

  /**
   * Returns the data of the placeguide(s) asked by the user who is currently logged in.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String placeGuideQueryTypeString = request.getParameter(PLACE_GUIDE_QUERY_TYPE_PARAMETER);
    PlaceGuideQueryType placeGuideQueryType = PlaceGuideQueryType.valueOf(placeGuideQueryTypeString);
    GeoPt northEastCorner = null;
    GeoPt southWestCorner = null;
    if (placeGuideQueryType.requiresCoordinates()) {
        String regionCornersString = request.getParameter(REGION_CORNERS_PARAMETER);
        String[] cornerCoordinates = regionCornersString.split(",");
        southWestCorner = new GeoPt(
                Float.parseFloat(cornerCoordinates[0]), Float.parseFloat(cornerCoordinates[1]));
        northEastCorner = new GeoPt(
                Float.parseFloat(cornerCoordinates[2]), Float.parseFloat(cornerCoordinates[3]));
    }
    List<PlaceGuide> placeGuides =
            getPlaceGuides(placeGuideQueryType, northEastCorner, southWestCorner);
    List<PlaceGuideWithUserPair> placeGuideWithUserPairs = getPlaceGuideWithUserPairs(placeGuides);
    response.setContentType("application/json;");
    response.getWriter().println(convertToJsonUsingGson(placeGuideWithUserPairs));
  }

  private List<PlaceGuideWithUserPair> getPlaceGuideWithUserPairs(List<PlaceGuide> placeGuides) {
      List<PlaceGuideWithUserPair> placeGuideWithUserPairs = new ArrayList<>();
      for(PlaceGuide placeGuide : placeGuides) {
          placeGuideWithUserPairs
                  .add(PlaceGuideWithUserPair.createPlaceGuideWithUserPair(placeGuide));
      }
      return placeGuideWithUserPairs;
  }

  private List<PlaceGuide> getPlaceGuides(PlaceGuideQueryType placeGuideQueryType,
                                          GeoPt northEastCorner,
                                          GeoPt southWestCorner) {
    List<PlaceGuide> placeGuides;
    switch(placeGuideQueryType) {
      case ALL_PUBLIC:
        placeGuides = placeGuideRepository.getAllPublicPlaceGuides();
        break;
      case CREATED_ALL:
        placeGuides = placeGuideRepository.getCreatedPlaceGuides(userId);
        break;
      case CREATED_PUBLIC:
        placeGuides = placeGuideRepository.getCreatedPublicPlaceGuides(userId);
        break;
      case CREATED_PRIVATE:
        placeGuides = placeGuideRepository.getCreatedPrivatePlaceGuides(userId);
        break;
      case ALL_PUBLIC_IN_MAP_AREA:
        placeGuides = placeGuideRepository
                .getAllPublicPlaceGuidesInMapArea(northEastCorner, southWestCorner);
        break;
      case CREATED_ALL_IN_MAP_AREA:
        placeGuides = placeGuideRepository
                .getCreatedPlaceGuidesInMapArea(userId, northEastCorner, southWestCorner);
        break;
      case CREATED_PUBLIC_IN_MAP_AREA:
        placeGuides = placeGuideRepository
                .getCreatedPublicPlaceGuidesInMapArea(userId, northEastCorner, southWestCorner);
        break;
      case CREATED_PRIVATE_IN_MAP_AREA:
        placeGuides = placeGuideRepository
                .getCreatedPrivatePlaceGuidesInMapArea(userId, northEastCorner, southWestCorner);
        break;
      default:
        throw new IllegalStateException("Place Guide type does not exist!");
    }
    return placeGuides;
  }

  private PlaceGuide getPlaceGuideFromRequest(HttpServletRequest request) {
    String name = request.getParameter(NAME_INPUT);
    String audioKey = request.getParameter(AUDIO_KEY_INPUT); // Get from Blobstore.
    float latitude = Float.parseFloat(request.getParameter(LATITUDE_INPUT));
    float longitude = Float.parseFloat(request.getParameter(LONGITUDE_INPUT));
    GeoPt coordinate = new GeoPt(latitude, longitude);
    String idStringValue = request.getParameter(ID_INPUT);
    long id;
    if (!idStringValue.isEmpty()) {
      id = Long.parseLong(idStringValue);
    } else {
      // Create PlaceGuide entity id.
      Entity placeGuideEntity = new Entity(DatastorePlaceGuideRepository.ENTITY_KIND);
      id = placeGuideEntity.getKey().getId();
    }
    PlaceGuide.Builder newPlaceGuideBuilder = new PlaceGuide.Builder(id, name, audioKey, userId, 
                                                                     coordinate);

    String publicPlaceGuideStringValue = request.getParameter(IS_PUBLIC_INPUT);
    if (publicPlaceGuideStringValue.equals(IS_PUBLIC_INPUT_VALUE)) {
      newPlaceGuideBuilder.setPlaceGuideStatus(true);
    }
    String length = request.getParameter(LENGTH_INPUT);
    if (!length.isEmpty()) {
      newPlaceGuideBuilder.setLength(Long.parseLong(length));
    }
    String placeId = request.getParameter(PLACE_ID_INPUT);
    if (!placeId.isEmpty()) {
      newPlaceGuideBuilder.setPlaceId(placeId);
    }
    String description = request.getParameter(DESCRIPTION_INPUT);
    if (!description.isEmpty()) {
      newPlaceGuideBuilder.setDescription(description);
    }
    String imageKey = request.getParameter(IMAGE_KEY_INPUT);
    if (!imageKey.isEmpty()) {
      newPlaceGuideBuilder.setImageKey(imageKey);
    }
    return newPlaceGuideBuilder.build();
  }

  private String convertToJsonUsingGson(Object o) {
    Gson gson = new Gson();
    String json = gson.toJson(o);
    return json;
  }
}