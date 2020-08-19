package com.google.sps;

import com.google.gson.Gson;
import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.RepositoryType;
import com.google.sps.placeGuide.PlaceGuide;
import com.google.sps.placeGuide.repository.PlaceGuideRepository;
import com.google.sps.placeGuide.repository.PlaceGuideRepositoryFactory;
import com.google.sps.placeGuide.repository.impl.DatastorePlaceGuideRepository;
import com.google.sps.user.repository.UserRepository;
import com.google.sps.user.repository.UserRepositoryFactory;
import com.google.sps.user.repository.impl.DatastoreUserRepository;
import com.google.sps.user.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public final class DatastorePlaceGuideRepositoryTest {
  
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
                                                            new LocalDatastoreServiceTestConfig());
  private final UserRepository userRepository = 
      UserRepositoryFactory.getUserRepository(RepositoryType.DATASTORE);

  public static final long A_PUBLIC_ID = 12345;
  public static final long B_PUBLIC_ID = 23456;
  public static final long A_PRIVATE_ID = 34567;
  public static final long B_PRIVATE_ID = 45678;
  public static final String NAME = "name";
  public static final String AUDIO_KEY = "audioKey";
  public static final String CREATOR_A_ID = "creatorA_Id";
  public static final String CREATOR_B_ID = "creatorB_Id";
  public static final String OTHER_USER_ID = "otherUserId";
  public static final String PLACE_ID = "placeId";
  public static final GeoPt COORDINATE = new GeoPt((float) 3.14, (float) 2.56);
  public static final boolean IS_PUBLIC = true;
  public static final long LENGTH = new Long(60);
  public static final String DESCRIPTION = "description";
  public static final String PREVIOUS_DESCRIPTION = "previous description";
  public static final String IMAGE_KEY = "imageKey";

  public static final String OTHER_USER_EMAIL = "otherUser@gmail.com";
  public static final Set<Long> OTHER_USER_BOOKMARKED_PLACE_GUIDES_IDS = new HashSet<>();
  public static final String CREATOR_A_EMAIL = "creatorA@gmail.com";
  public static final Set<Long> CREATOR_A_BOOKMARKED_PLACE_GUIDES_IDS = 
      new HashSet<>(Arrays.asList(A_PUBLIC_ID, B_PUBLIC_ID));

  private final User testUser = 
      new User.Builder(OTHER_USER_ID, OTHER_USER_EMAIL, OTHER_USER_BOOKMARKED_PLACE_GUIDES_IDS)
      .build();

  private final User userA = 
      new User.Builder(CREATOR_A_ID, CREATOR_A_EMAIL, CREATOR_A_BOOKMARKED_PLACE_GUIDES_IDS)
      .build();

  private final PlaceGuide testPublicPlaceGuideA = 
      new PlaceGuide.Builder(A_PUBLIC_ID, NAME, AUDIO_KEY, CREATOR_A_ID, COORDINATE)
      .setPlaceId(PLACE_ID)
      .setPlaceGuideStatus(IS_PUBLIC)
      .setLength(LENGTH)
      .setDescription(DESCRIPTION)
      .setImageKey(IMAGE_KEY)
      .build();

  private final PlaceGuide previousTestPublicPlaceGuideA = 
      new PlaceGuide.Builder(A_PUBLIC_ID, NAME, AUDIO_KEY, CREATOR_A_ID, COORDINATE)
      .setPlaceGuideStatus(IS_PUBLIC)
      .build();

  private final PlaceGuide testPrivatePlaceGuideA = 
      new PlaceGuide.Builder(A_PRIVATE_ID, NAME, AUDIO_KEY, CREATOR_A_ID, COORDINATE)
      .setPlaceId(PLACE_ID)
      .setLength(LENGTH)
      .setDescription(DESCRIPTION)
      .setImageKey(IMAGE_KEY)
      .build();

  private final PlaceGuide testPublicPlaceGuideB = 
      new PlaceGuide.Builder(B_PUBLIC_ID, NAME, AUDIO_KEY, CREATOR_B_ID, COORDINATE)
      .setPlaceId(PLACE_ID)
      .setPlaceGuideStatus(IS_PUBLIC)
      .setLength(LENGTH)
      .setDescription(DESCRIPTION)
      .setImageKey(IMAGE_KEY)
      .build();

  private final PlaceGuide testPrivatePlaceGuideB = 
      new PlaceGuide.Builder(B_PRIVATE_ID, NAME, AUDIO_KEY, CREATOR_B_ID, COORDINATE)
      .setPlaceId(PLACE_ID)
      .setLength(LENGTH)
      .setDescription(DESCRIPTION)
      .setImageKey(IMAGE_KEY)
      .build();

  private void saveTestPlaceGuidesEntities(List<PlaceGuide> placeGuides) {
    for (PlaceGuide placeGuide : placeGuides) {
      datastore.put(getEntityFromPlaceGuide(placeGuide));
    }
  }
  
  private Entity getEntityFromPlaceGuide(PlaceGuide placeGuide) {
    Entity placeGuideEntity = new Entity(DatastorePlaceGuideRepository.ENTITY_KIND, 
                                                          placeGuide.getId());
    placeGuideEntity.setProperty(DatastorePlaceGuideRepository.NAME_PROPERTY, placeGuide.getName());
    placeGuideEntity.setProperty(DatastorePlaceGuideRepository.AUDIO_KEY_PROPERTY, placeGuide.getAudioKey());
    placeGuideEntity.setProperty(DatastorePlaceGuideRepository.CREATOR_ID_PROPERTY, 
                                                          placeGuide.getCreatorId());
    placeGuideEntity.setProperty(DatastorePlaceGuideRepository.IS_PUBLIC_PROPERTY, 
                                                          placeGuide.isPublic());
    placeGuideEntity.setProperty(DatastorePlaceGuideRepository.PLACE_ID_PROPERTY, placeGuide.getPlaceId());
    placeGuideEntity.setProperty(DatastorePlaceGuideRepository.COORDINATE_PROPERTY, placeGuide.getCoordinate());
    placeGuideEntity.setProperty(DatastorePlaceGuideRepository.DESCRIPTION_PROPERTY, 
                                                          placeGuide.getDescription());
    placeGuideEntity.setProperty(DatastorePlaceGuideRepository.LENGTH_PROPERTY, placeGuide.getLength());
    placeGuideEntity.setProperty(DatastorePlaceGuideRepository.IMAGE_KEY_PROPERTY, placeGuide.getImageKey());
    return placeGuideEntity;
  }

  // Find out if the 2 lists of placeguides are equal.
  private boolean compare(List<PlaceGuide> a, List<PlaceGuide> b) {
    List<PlaceGuide> b_copy = new ArrayList<>(b);
    if (a.size() != b_copy.size()) {
      return false;
    }
    for (PlaceGuide a_pg : a) {
      boolean hasEqual = false;
      int index_b_copy = 0;
      while (index_b_copy < b_copy.size()) {
        if (a_pg.getId() == (b_copy.get(index_b_copy)).getId()) {
          hasEqual = true;
          b_copy.remove(index_b_copy);
          break;
        }
        index_b_copy++;
      }
      if (!hasEqual) {
        return false;
      }
    }
    return true;
  }

  private DatastoreService datastore;
  private PlaceGuideRepository placeGuideRepository;

  @Before
  public void setUp() {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
    placeGuideRepository = PlaceGuideRepositoryFactory.getPlaceGuideRepository(
                                                                        RepositoryType.DATASTORE);
  }

  @Test
  public void savePlaceGuide_noPreviousPlaceGuide_databaseContainsCreatedPlaceGuide() {
    placeGuideRepository.savePlaceGuide(testPublicPlaceGuideA);
    Key placeGuideKey = KeyFactory.createKey(DatastorePlaceGuideRepository.ENTITY_KIND, A_PUBLIC_ID);
    try {
      Entity result = datastore.get(placeGuideKey);
      Entity expected = getEntityFromPlaceGuide(testPublicPlaceGuideA);
      assertTrue(result.equals(expected));
    } catch (EntityNotFoundException e) {
      fail("Entity not found: " + e);
    }

  }

  @Test
  public void savePlaceGuide_previousPlaceGuideExists_databaseContainsEditedPlaceGuide() {
    List<PlaceGuide> testPlaceGuidesList = Arrays.asList(previousTestPublicPlaceGuideA, 
                                                         testPublicPlaceGuideB,
                                                         testPrivatePlaceGuideB);
    saveTestPlaceGuidesEntities(testPlaceGuidesList);
    placeGuideRepository.savePlaceGuide(testPublicPlaceGuideA);
    Key placeGuideKey = KeyFactory.createKey(DatastorePlaceGuideRepository.ENTITY_KIND, A_PUBLIC_ID);

    try {
      Entity result = datastore.get(placeGuideKey);
      Entity expected = getEntityFromPlaceGuide(testPublicPlaceGuideA);
      assertTrue(result.equals(expected));
    } catch (EntityNotFoundException e) {
      fail("Entity not found: " + e);
    }
  }

  @Test
  public void bookmarkPlaceGuide_existingUserAndExistingPlaceGuide_bookmarkedPlaceGuidesContainsPlaceGuideId() {
    // Save testUser to datastore using userRepository.
    userRepository.saveUser(testUser);
    
    // Store place guide to datastore.
    List<PlaceGuide> testPlaceGuidesList = Arrays.asList(testPublicPlaceGuideA,
                                                         testPublicPlaceGuideB,
                                                         testPrivatePlaceGuideB,
                                                         testPrivatePlaceGuideA);
    saveTestPlaceGuidesEntities(testPlaceGuidesList);

    // testUser is bookmarking public place guide a. 
    placeGuideRepository.bookmarkPlaceGuide(A_PUBLIC_ID, OTHER_USER_ID);

    // Check whether the place guide is inside testUser's bookmarkedPlaceGuides list.
    Set<Long> expected = new HashSet<>(Arrays.asList(A_PUBLIC_ID));
    Set<Long> result = userRepository.getUser(OTHER_USER_ID).getBookmarkedPlaceGuidesIds();
    assertEquals(expected, result);
  }

  @Test(expected = IllegalStateException.class)
  public void bookmarkPlaceGuide_InexistentUserAndExistingPlaceGuide_throwsError() {
    // Store place guide to datastore.
    List<PlaceGuide> testPlaceGuidesList = Arrays.asList(testPublicPlaceGuideA,
                                                         testPublicPlaceGuideB,
                                                         testPrivatePlaceGuideB,
                                                         testPrivatePlaceGuideA);
    saveTestPlaceGuidesEntities(testPlaceGuidesList);

    // testUser is bookmarking public place guide a. 
    placeGuideRepository.bookmarkPlaceGuide(A_PUBLIC_ID, OTHER_USER_ID);
  }

  @Test
  public void getAllPublicPlaceGuides_placeGuideExists_returnPlaceGuide() {
    List<PlaceGuide> testPlaceGuidesList = Arrays.asList(testPublicPlaceGuideA,
                                                         testPublicPlaceGuideB,
                                                         testPrivatePlaceGuideB,
                                                         testPrivatePlaceGuideA);
    saveTestPlaceGuidesEntities(testPlaceGuidesList);
    List<PlaceGuide> result = placeGuideRepository.getAllPublicPlaceGuides();
    List<PlaceGuide> expected = Arrays.asList(testPublicPlaceGuideA, testPublicPlaceGuideB);
    assertTrue(compare(expected, result));
  }

  @Test
  public void getAllPublicPlaceGuides_placeGuideDoesntExists_resultIsEmpty() {
    List<PlaceGuide> result = placeGuideRepository.getAllPublicPlaceGuides();
    assertTrue(result.isEmpty());
  }

  @Test
  public void getCreatedPlaceGuides_UserDoesntOwnAnyPlaceGuides_resultIsEmpty() {
    List<PlaceGuide> testPlaceGuidesList = Arrays.asList(testPublicPlaceGuideA,
                                                         testPublicPlaceGuideB,
                                                         testPrivatePlaceGuideB,
                                                         testPrivatePlaceGuideA);
    saveTestPlaceGuidesEntities(testPlaceGuidesList);
    List<PlaceGuide> result = placeGuideRepository.getCreatedPlaceGuides(OTHER_USER_ID);
    assertTrue(result.isEmpty());
  }

  @Test
  public void getCreatedPlaceGuides_UserOwnsPublicAndPrivate_resultContainsPlaceGuide() {
    List<PlaceGuide> testPlaceGuidesList = Arrays.asList(testPublicPlaceGuideA,
                                                         testPublicPlaceGuideB,
                                                         testPrivatePlaceGuideB,
                                                         testPrivatePlaceGuideA);
    saveTestPlaceGuidesEntities(testPlaceGuidesList);
    List<PlaceGuide> result = placeGuideRepository.getCreatedPlaceGuides(CREATOR_A_ID);
    List<PlaceGuide> expected = Arrays.asList(testPublicPlaceGuideA, testPrivatePlaceGuideA);
    assertTrue(compare(expected, result));
  }

  @Test
  public void getCreatedPlaceGuides_placeGuideDoesntExist_resultIsEmpty() {
    List<PlaceGuide> result = placeGuideRepository.getCreatedPlaceGuides(CREATOR_A_ID);
    assertTrue(result.isEmpty());
  }

  @Test
  public void getCreatedPublicPlaceGuides_placeGuideDoesntExist_resultIsEmpty() {
    List<PlaceGuide> result = placeGuideRepository.getCreatedPublicPlaceGuides(CREATOR_A_ID);
    assertTrue(result.isEmpty());
  }

  @Test
  public void getCreatedPublicPlaceGuides_userDoesntOwnAnyPublicPlaceGuides_resultIsEmpty() {
    List<PlaceGuide> testPlaceGuidesList = Arrays.asList(testPublicPlaceGuideB,
                                                         testPrivatePlaceGuideB,
                                                         testPrivatePlaceGuideA);
    saveTestPlaceGuidesEntities(testPlaceGuidesList);
    List<PlaceGuide> result = placeGuideRepository.getCreatedPublicPlaceGuides(CREATOR_A_ID);
    assertTrue(result.isEmpty());
  }

  @Test
  public void getCreatedPublicPlaceGuides_UserHasPublicPlaceGuide_resultHasPlaceGuide() {
    List<PlaceGuide> testPlaceGuidesList = Arrays.asList(testPublicPlaceGuideA,
                                                         testPublicPlaceGuideB,
                                                         testPrivatePlaceGuideB,
                                                         testPrivatePlaceGuideA);
    saveTestPlaceGuidesEntities(testPlaceGuidesList);
    List<PlaceGuide> result = placeGuideRepository.getCreatedPublicPlaceGuides(CREATOR_A_ID);
    List<PlaceGuide> expected = Arrays.asList(testPublicPlaceGuideA);
    assertTrue(compare(expected, result));
  }

  @Test
  public void getCreatedPrivatePlaceGuides_placeGuideDoesntExist_resultIsEmpty() {
    List<PlaceGuide> result = placeGuideRepository.getCreatedPrivatePlaceGuides(CREATOR_A_ID);
    assertTrue(result.isEmpty());
  }

  @Test
  public void getCreatedPrivatePlaceGuides_userDoesntOwnAnyPrivatePlaceGuides_resultIsEmpty() {
    List<PlaceGuide> testPlaceGuidesList = Arrays.asList(testPublicPlaceGuideB,
                                                         testPrivatePlaceGuideB,
                                                         testPublicPlaceGuideA);
    saveTestPlaceGuidesEntities(testPlaceGuidesList);
    List<PlaceGuide> result = placeGuideRepository.getCreatedPrivatePlaceGuides(CREATOR_A_ID);
    assertTrue(result.isEmpty());
  }

  @Test
  public void getCreatedPrivatePlaceGuides_UserHasPrivatePlaceGuide_resultHasPlaceGuide() {
    List<PlaceGuide> testPlaceGuidesList = Arrays.asList(testPublicPlaceGuideA,
                                                         testPublicPlaceGuideB,
                                                         testPrivatePlaceGuideB,
                                                         testPrivatePlaceGuideA);
    saveTestPlaceGuidesEntities(testPlaceGuidesList);
    List<PlaceGuide> result = placeGuideRepository.getCreatedPrivatePlaceGuides(CREATOR_A_ID);
    List<PlaceGuide> expected = Arrays.asList(testPrivatePlaceGuideA);
    assertTrue(compare(expected, result));
  }

  @Test
  public void getBookmarkedPlaceGuides_allPlaceGuidesExistAndUserExists_returnCorrespondingPlaceGuides() {
    List<PlaceGuide> testPlaceGuidesList = Arrays.asList(testPublicPlaceGuideA,
                                                         testPublicPlaceGuideB,
                                                         testPrivatePlaceGuideB,
                                                         testPrivatePlaceGuideA);
    saveTestPlaceGuidesEntities(testPlaceGuidesList);

    // Store user to database.
    userRepository.saveUser(userA);
    List<PlaceGuide> expected = Arrays.asList(testPublicPlaceGuideA, testPublicPlaceGuideB);
    List<PlaceGuide> result = placeGuideRepository.getBookmarkedPlaceGuides(CREATOR_A_ID);
    assertTrue(compare(expected, result));
  }

  @Test(expected = IllegalStateException.class)
  public void getBookmarkedPlaceGuides_nonExistentUser_throwsError() {
    List<PlaceGuide> testPlaceGuidesList = Arrays.asList(testPublicPlaceGuideA,
                                                         testPublicPlaceGuideB,
                                                         testPrivatePlaceGuideB,
                                                         testPrivatePlaceGuideA);
    saveTestPlaceGuidesEntities(testPlaceGuidesList);

    List<PlaceGuide> expected = Arrays.asList(testPublicPlaceGuideA, testPublicPlaceGuideB);
    List<PlaceGuide> result = placeGuideRepository.getBookmarkedPlaceGuides(CREATOR_A_ID);
    assertTrue(compare(expected, result));
  }

  @Test
  public void getBookmarkedPlaceGuides_allBookmarkedPlaceGuidesDontExistAnymore_bookmarkedPlaceGuidesBecomesEmpty() {
    // Store user to database.
    userRepository.saveUser(userA);

    List<PlaceGuide> expected = Arrays.asList();
    List<PlaceGuide> result = placeGuideRepository.getBookmarkedPlaceGuides(CREATOR_A_ID);
    assertEquals(expected, result);

    // Check if the user's {@code bookmarkedPlaceGuides} is empty. If it's empty, then datastore
    // will store it as a null value.
    User testUserA = userRepository.getUser(CREATOR_A_ID);
    Set<Long> expectedSet = new HashSet<>();
    assertEquals(expectedSet, testUserA.getBookmarkedPlaceGuidesIds());
  }

  @Test(expected = EntityNotFoundException.class)
  public void deletePlaceGuide_theChosenPlaceGuideHasBeenDeleted_throwExceptionSinceEntityDoesntExist() throws EntityNotFoundException{
    List<PlaceGuide> testPlaceGuidesList = Arrays.asList(testPublicPlaceGuideA,
                                                         testPublicPlaceGuideB,
                                                         testPrivatePlaceGuideB,
                                                         testPrivatePlaceGuideA);
    saveTestPlaceGuidesEntities(testPlaceGuidesList);
    placeGuideRepository.deletePlaceGuide(A_PUBLIC_ID);
    Key deletedEntityKey = KeyFactory.createKey(DatastorePlaceGuideRepository.ENTITY_KIND, 
                                                                              A_PUBLIC_ID);
    Entity deletedEntity = datastore.get(deletedEntityKey);
  }

  @Test(expected = IllegalStateException.class)
  public void removeBookmarkedPlaceGuide_userDoesntExist_throwsError() {
    placeGuideRepository.removeBookmarkedPlaceGuide(A_PUBLIC_ID, CREATOR_A_ID);
  }

  @Test
  public void removeBookmarkedPlaceGuide_userHasBookmarkedPlaceGuide_selectedPlaceGuideRemoved() {
    // Store user A.
    userRepository.saveUser(userA);

    placeGuideRepository.removeBookmarkedPlaceGuide(A_PUBLIC_ID, CREATOR_A_ID);
    User testUserA = userRepository.getUser(CREATOR_A_ID);
    Set<Long> expected = new HashSet<>(Arrays.asList(B_PUBLIC_ID));
    Set<Long> result = testUserA.getBookmarkedPlaceGuidesIds();
    assertEquals(expected, result);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }
}