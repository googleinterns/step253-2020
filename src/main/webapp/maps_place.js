const placeZoom = 14;

// Get icons from the charts API
function getMarkerIcon(color) {
    var iconBase = 'https://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|';
    var markerIcon = {
        url: iconBase + color,
        scaledSize: new google.maps.Size(30, 46), // scaled size
        origin: new google.maps.Point(0,0), // origin
        anchor: new google.maps.Point(15, 45) // anchor
    }
    return markerIcon;
}

var PlaceType = {
    PUBLIC: {
        // Orange icon
        iconColor: "de8a0b", 
    },
    PRIVATE: {
        // Yellow icon
        iconColor: "f7ff05",
    },
    SEARCH_RESULT: {
        // Green icon
        iconColor: "82d613",
    },
    SAVED_LOCATION: {
        // Blue icon
        iconColor: "1d2480",
    }
};

class Place {
    // Either positionLat, positionLng and name, or mapsPlace will be specified.
    // The others will be null.
    // placeType is a value from PlaceType
    constructor(positionLat, positionLng, name, mapsPlace, placeType) {
        this._mapsPlace = mapsPlace;
        if (this._mapsPlace != null) {
            this._position = this._mapsPlace.geometry.location;
            this.name = this._mapsPlace.name;
        } else {
            this._position = new google.maps.LatLng(positionLat, positionLng);
            this._name = name;
        }
        this._placeType = placeType;
        this.setupRepresentationOnMap();
    }

    setupRepresentationOnMap() {
        this.setupMarker();
        this.setupInfoWindow();
        this._marker.addListener('click', () => {
            if (this._infoWindowClosed) {
                this._infoWindow.open(map, this._marker);
            }
            else {
                this._infoWindow.close();
            }
            this._infoWindowClosed = !this._infoWindowClosed;
        });
    }

    setupMarker() {
        this._marker = new google.maps.Marker( {
            position: this._position, 
            title: this._name, 
            icon: getMarkerIcon(this._placeType.iconColor)
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

    addMarkerToMap(map) {
        this._marker.setMap(map);
    }

    removeMarkerFromMap() {
        this._marker.setMap(null);
    }

    get position() {
        return this._position;
    }

    set position(pos) {
        this._position = pos;
    }

    centerMapAround(map) {
        if (this._mapsPlace != null && this._mapsPlace.geometry.viewPort) {
            map.fitBounds(mapsPlace.geometry.viewport);
        } else {
            map.setCenter(this._position);
            map.setZoom(placeZoom);
        }
    }
}

class PlaceGuide extends Place {
    constructor(databaseId, name, description, audioKey, audioLength, imgKey, positionLat, positionLng, placeId, creatorId, creatorName, placeType) {
        super(positionLat, positionLng, name, null, placeType);
        this._databaseId = databaseId;
        this._name = name;
        this._description = description;
        this._audioKey = audioKey;
        this._imgKey = imgKey;
        this._creatorId = creatorId;
        this._creatorName = creatorName;
        this.setupRepresentationOnMap();
    }

    getInfoWindowContent() {
        var content = "<h3>" + this._name + "</h3>" +
        "<h4> Created by: " + this._creatorName + "</h4>" + 
        "<p>" + this._description + "</p>";
        return content;
    }

    set position(pos) {
        super.position = pos;
        this._marker.setPosition(this._position);
    }
}