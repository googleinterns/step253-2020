const placeZoom = 14;

// Specify different icons/colors for dynamically generated icons for each place type.
var PlaceType = {
    PUBLIC: {
        // Orange icon
        icon: null,
        iconColor: "de8a0b", 
    },
    PRIVATE: {
        // Yellow icon
        icon: null,
        iconColor: "f7ff05",
    },
    SEARCH_RESULT: {
        // Green icon
        icon: null,
        iconColor: "82d613",
    },
    SAVED_LOCATION: {
        // Blue icon
        icon: null,
        iconColor: "1d2480",
    },
    CURRENT_LOCATION: {
        icon: "./img/blue_dot.png",
    }
};

/**
 * This class holds a place's data and handles its representation on the map.
 * The place is represented by a marker corresponding to placeType, 
 * and it may also have an infoWindow, containing the name.
 * The place may be defined my specifying its position(@param positionLat, @param positionLng) and @param name, 
 * or by specifying a place from the Google Place database(@param mapsPlace).
 * Remark that mapsPlace has the higher priority(because it contains more information).
 * Whenever a new position is set, the mapsPlace is discarder, and vice versa.
 */
class Place {
    constructor(positionLat, positionLng, name, mapsPlace, placeType, hasInfoWindow) {
        this._mapsPlace = mapsPlace;
        if (this._mapsPlace != null) {
            this._position = this._mapsPlace.geometry.location;
            this.name = this._mapsPlace.name;
        } else {
            this._position = new google.maps.LatLng(positionLat, positionLng);
            this._name = name;
        }
        this._placeType = placeType;
        this._hasInfoWindow = hasInfoWindow;
        this._toSetVisiblePlace = null;
        this.setupRepresentationOnMap();
    }

    setupRepresentationOnMap() {
        this.setupMarker();
        if(this._hasInfoWindow) {
            this.setupInfoWindow();
            // The infowindow an be opened and closed by clicking the marker.
            this._marker.addListener('click', () => {
                if (this._infoWindowClosed) {
                    this.openInfoWindow();
                }
                else {
                    this.closeInfoWindow();
                }
            });
        }
    }

    closeInfoWindow() {
        this._infoWindow.close();
        this._infoWindowClosed = true;
    }

    openInfoWindow() {
        this._infoWindow.open(this._map, this._marker);
        this._infoWindowClosed = false;
    }

    setupMarker() {
        var markerIcon;
        if (this._placeType.icon != null) {
            markerIcon = this._placeType.icon;
        } else {
            markerIcon = getColoredMarkerIcon(this._placeType.iconColor);
        }
        this._marker = new google.maps.Marker( {
            position: this._position, 
            title: this._name, 
            icon: markerIcon,
        });
    }

    setupInfoWindow() {
        this._infoWindowClosed = true;
        this._infoWindow = new google.maps.InfoWindow({
            content: this.getInfoWindowContent(),
            maxWidth: 200, 
        });
    }

    getInfoWindowContent() {
        var content = "<h3>" + this._name + "</h3>";
        return content;
    }

    set visible(visibility) {
        this._marker.setVisible(visibility);
    }

    set draggable(draggability) {
        this._marker.setDraggable(draggability);
        var thisPlace = this;
        this._marker.addListener('drag', function() {
            thisPlace.position = this.getPosition();
        });
    }

    set map(newMap) {
        this._map = newMap;
        this._marker.setMap(newMap);
        var thisPlace = this;
        // Close the open infowindow if the user clicks anywhere else on the map.
        if(this._hasInfoWindow) {
            this._marker.getMap().addListener('click', function(mapsMouseEvent) {
                if (!this._infoWindowClosed) {
                    thisPlace.closeInfoWindow();
                }
            });
        }
    }

    removeMarkerFromMap() {
        this._map = null;
        this._marker.setMap(null);
    }

    get position() {
        return this._position;
    }

    set position(pos) {
        // Overwrite mapsPlace when position changes.
        if (this._mapsPlace != null ) {
            this.detachFromPlace();
        } 
        this._position = pos;
        this._marker.setPosition(this._position);
        this.onPositionChange();
    }

    get place() {
        return this._mapsPlace;
    }

    set place(newPlace) {
        this._mapsPlace = newPlace;
        // Overwrite position when mapsPlace changes.
        // use the position of the new mapsPlace.
        this._position = this._mapsPlace.geometry.location;
        this._name = this._mapsPlace.name;
        this._marker.setPosition(this._position);
        this.onPositionChange();
    }

    detachFromPlace() {
        this._mapsPlace = null;
    }

    centerMapAround() {
        if (this._mapsPlace != null && this._mapsPlace.geometry.viewport) {
            this._map.setZoom(placeZoom);
            this._map.fitBounds(this._mapsPlace.geometry.viewport);
        } else {
            this._map.setZoom(placeZoom);
            this._map.setCenter(this._position);
        }
    }

    /**
    * This function relies on Geocoder API to get the place
    * corresponding to an id, provided by the Places API,
    * and then update the mapsPlace.
    */
    updatePlaceAndCenterBasedOnId(id) {
        const geocoder = new google.maps.Geocoder();
        geocoder.geocode({placeId: id}, (results, status) => {
            if (status !== "OK") {
                window.alert(Place.GEOCODER_FAIL_MSG + status);
                return;
            }
            this.updatePlaceAndCenter(results[0]);
        });
    }

    updatePlaceAndCenter(newPlace) {
        this.place = newPlace;
        this.centerMapAround();
    }

    /** 
     * The attached place will be set to visible the this place's position changes. 
     * This functionality is used when the user saves the currently picked location.
     * Since the two markers would be overlapping 100%, the picked loctaion marker 
     * may not be visible, and thus draggable anymore.
     * To solve this, the saved location marker is made visible only when the currently picked 
     * location marker is moved away.
     */
    attachToSavePlace(toSavePlace) {
        this._toSavePlace = toSavePlace;
    }

    onPositionChange() {
        this.setVisibleTheToSavePlace();
        this.enableElement();
        if (this._setVisibleOnPositionChange) {
            this.visible = true;
            this._setVisibleOnPositionChange = false;
        }
    }

    setVisibleTheToSavePlace() {
        if (this._toSavePlace != null) {
            this._toSavePlace.visible = true;
            this.detachToSavePlace();
        }
    }

    detachToSavePlace() {
        this._toSavePlace = null;
    }

    setVisibleOnPositionChange() {
        this._setVisibleOnPositionChange = true;
    }

    enableElementOnPositionChange(element) {
        this._toEnableElement = element;
    }

    enableElement() {
        if (this._toEnableElement != null) {
            this._toEnableElement.disabled = false;
        }
    }
}

class PlaceGuide extends Place {
    constructor(databaseId, name, description, audioKey, audioLength, imgKey, positionLat, positionLng, placeId, creatorId, creatorName, placeType) {
        super(positionLat, positionLng, name, null, placeType, true);
        this._databaseId = databaseId;
        this._name = name;
        this._description = description;
        this._audioKey = audioKey;
        this._imgKey = imgKey;
        this._creatorId = creatorId;
        this._creatorName = creatorName;
        this.setupRepresentationOnMap();
    }

    // Specific infoWindowContent for PlaceGuides.
    getInfoWindowContent() {
        var content = "<h3>" + this._name + "</h3>" +
        "<h4> Created by: " + this._creatorName + "</h4>" + 
        "<p>" + this._description + "</p>";
        return content;
    }
}

// Get icons from the charts API.
function getColoredMarkerIcon(color) {
    var iconBase = 'https://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|';
    var markerIcon = {
        url: iconBase + color,
        scaledSize: new google.maps.Size(30, 46),
        origin: new google.maps.Point(0,0),
        anchor: new google.maps.Point(15, 45)
    }
    return markerIcon;
}