var currentLocationMarker = null;
var trackUser;
var watchPositionId;
var placeZoom = 14;

function createMap() {
    var myMapOptions = {
        zoom: 2,
        center: new google.maps.LatLng(0, 0),
        mapTypeId: 'roadmap',
    };
    const map = new google.maps.Map(
        document.getElementById('map'), myMapOptions); 
    addGoToMyLocationControl(map);
    initAutocomplete(map);
}

function initAutocomplete(map) {
    const input = document.getElementById("search-box");
    var autocomplete = new google.maps.places.Autocomplete(input);
    map.controls[google.maps.ControlPosition.TOP_CENTER].push(input);

    var searchedPlaceMarker = new google.maps.Marker({
        map: map,
    });
    searchedPlaceMarker.setVisible(false);
    searchedPlaceMarker.setMap(map);

    autocomplete.setFields(['address_components', 'geometry', 'icon', 'name']);

    autocomplete.addListener('place_changed', function() {
        searchedPlaceMarker.setVisible(false);
        var place = autocomplete.getPlace();
        if (!place.geometry) {
            // User entered the name of a Place that was not suggested and
            // pressed the Enter key, or the Place Details request failed.
            window.alert("Place not found: '" + place.name + "'");
            return;
        }

        // If the place has a geometry, then present it on a map.
        if (place.geometry.viewport) {
            map.fitBounds(place.geometry.viewport);
        } else {
            map.setCenter(place.geometry.location);
            map.setZoom(placeZoom);
        }

        searchedPlaceMarker.setPosition(place.geometry.location);
        searchedPlaceMarker.setVisible(true);
    });
}

function addGoToMyLocationControl(map) {
    var imgId = "myLocationIcon";
    const myLocationControlDiv = createControlDiv("Go to my location", "./img/my_location.svg", imgId);
    myLocationControlDiv.index = 1;
    map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(myLocationControlDiv);
    trackUser = false;
    myLocationControlDiv.addEventListener("click", () => {
        var img = document.getElementById(imgId);
        trackUser = !trackUser;
        console.log("trackuser=" + trackUser);
        if (trackUser) {
            img.src = "./img/my_location_active.svg";
            if (navigator.geolocation) {
                console.log("start watchPosition");
                watchPositionId = navigator.geolocation.watchPosition(position => showCurrentlocation(position, map), error => watchPositionError(error));
                centerMapToCurrentLocation(map);
            } else {
                alert("The browser doesn't support geolocation.");
            }
        } else {
             console.log("stop watchPosition");
            navigator.geolocation.clearWatch(watchPositionId);
            removeCurrentLocationMarker();
            img.src = "./img/my_location.svg";
        }
    });
}

function centerMapToCurrentLocation(map) {
    navigator.geolocation.getCurrentPosition(function(position) {
        currentPos = {
            lat: position.coords.latitude,
            lng: position.coords.longitude
        };
        map.setCenter(currentPos);
        map.setZoom(placeZoom);
    },  error => {
        showError(error);
    });
}

function showCurrentlocation(position, map) {
    console.log("new position displayed");
    currentPos = {
        lat: position.coords.latitude,
        lng: position.coords.longitude
    };
    showCurrentLocationMarker(map, currentPos);
}

function watchPositionError(error) {
    showError(error);
    removeCurrentLocationMarker();
}

function showCurrentLocationMarker(map, currentPos) {
    if (currentLocationMarker === null) {
        currentLocationMarker = new google.maps.Marker({
            map: map,
            position: currentPos,
            icon: "./img/blue_dot.png"
        });
    } 
    console.log("currentLocationMarker: " + currentLocationMarker);
    currentLocationMarker.setMap(map);
    currentLocationMarker.setPosition(currentPos);
}

function removeCurrentLocationMarker(){
    if (currentLocationMarker != null) {
        currentLocationMarker.setMap(null);
    }
}

function showError(error) {
  switch(error.code) {
    case error.PERMISSION_DENIED:
      alert("Please allow location permission!.");
      break;
    case error.POSITION_UNAVAILABLE:
      alert("Location information is unavailable.");
      break;
    case error.TIMEOUT:
      alert("The request to get user location timed out.");
      break;
    case error.UNKNOWN_ERROR:
     alert("An unknown error occurred while geolocating.");
      break;
  }
}

function createControlDiv(title, imgSrc, imgId) {
  const controlDiv = document.createElement("div");
  controlDiv.setAttribute('class', 'control');
  controlDiv.title = title;
  if (imgSrc != null ) {
    const controlImg = document.createElement("img");
    controlImg.setAttribute('id', imgId);
    controlImg.src = imgSrc;
    controlDiv.appendChild(controlImg);
  } 
  return controlDiv;
}