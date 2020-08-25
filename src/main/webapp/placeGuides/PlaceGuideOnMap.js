class PlaceGuideOnMap {
    constructor(id, name, position, place, creator, description, placeType) {
        this._id = id;
        this._infoWindowClosed = true;
        this._infoWindow = PlaceGuideOnMap.getInfoWindow(name, position, place, creator, description);
        this._marker = PlaceGuideOnMap.getMarker(placeType, name, position);
        this._highlighted = false;
        this.closeInfoWindowOnMapClick();
        this.toggleInfoWindowOnMarkerClick();
        this.highlightOnMarkerDoubleClick();
        this.unhighlightOnMapClick();
        this.unhighlightOnMarkerClick();
    }

    get id() {
        return this._id;
    }

    isHighlighted() {
        return this._highlighted;
    }

    highlight() {
        this._highlighted = true;
        this._marker.setAnimation(google.maps.Animation.BOUNCE);
        this.openInfoWindow();
    }

    unhighlight() {
        console.log("finish animation");
        this._highlighted = false;
        this._marker.setAnimation(null);
        this.closeInfoWindow();
    }

    closeInfoWindow() {
        this._infoWindow.close();
        this._infoWindowClosed = true;
    }

    openInfoWindow() {
        this._infoWindow.open(map, this._marker);
        this._infoWindowClosed = false;
    }

    remove() {
        this._marker.setMap(null);
    }

    highlightOnMarkerDoubleClick() {
        var thisPlaceGuideOnMap = this;
        this._marker.addListener("dblclick", () => {
            if (!thisPlaceGuideOnMap.isHighlighted()) {
                placeGuideManager.highlightPlaceGuide(thisPlaceGuideOnMap.id);
            }
        });
    }

    toggleInfoWindowOnMarkerClick() {
        var thisPlaceGuideOnMap = this;
        this._marker.addListener('click', () => {
            if (thisPlaceGuideOnMap._infoWindowClosed) {
                thisPlaceGuideOnMap.openInfoWindow();
            } else {
                thisPlaceGuideOnMap.closeInfoWindow();
            }
        });
    }

    unhighlightOnMarkerClick() {
        var thisPlaceGuideOnMap = this;
        this._marker.addListener('click', () => {
            if (thisPlaceGuideOnMap.isHighlighted()) {
                placeGuideManager.unhighlightPlaceGuide();
            }
        });
    }

    closeInfoWindowOnMapClick() {
        var thisPlaceGuideOnMap = this;
        map.addListener('click', function (mapsMouseEvent) {
            if (!thisPlaceGuideOnMap._infoWindowClosed) {
                thisPlaceGuideOnMap.closeInfoWindow();
            }
        });
    }

    unhighlightOnMapClick() {
        var thisPlaceGuideOnMap = this;
        map.addListener('click', function (mapsMouseEvent) {
            if (thisPlaceGuideOnMap.isHighlighted()) {
                placeGuideManager.unhighlightPlaceGuide();
            }
        });
    }

    static getMarker(placeType, name, position) {
        var markerIcon = PlaceGuideOnMap.getMarkerIcon(placeType);
        var marker = new google.maps.Marker({
            position: position,
            title: name,
            icon: markerIcon,
            map: map,
        });
        return marker;
    }

    static getMarkerIcon(placeType) {
        var markerIcon;
        if (placeType.icon != null) {
            markerIcon = this._placeType.icon;
        } else {
            markerIcon = getColoredMarkerIcon(placeType.iconColor);
        }
        return markerIcon;
    }

    static getInfoWindow(name, position, place, creator, description) {
        return new google.maps.InfoWindow({
            content: PlaceGuideOnMap.getInfoWindowContent(name, position, place, creator, description),
            maxWidth: 200,
        });
    }

    static getInfoWindowContent(name, position, place, creator, description) {
        var placeName;
        if (place != null) {
            placeName = place.name;
        } else {
            placeName = position.toString();
        }
        var creatorName = creator.name;
        if (creatorName == undefined) {
            creatorName = creator.email;
        }
        var content = "<h3>" + name + "</h3>" +
            "<h4> Created by: " + creatorName + "</h4>" +
            "<h4> Place: " + placeName + "</h4>" +
            "<p>" + description + "</p>";
        return content;
    }
}