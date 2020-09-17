/**
 * This class holds a location's data, which can be specified by
 * coordinates or by a place_id, which will be decoded with PlacesService.
 */
class Location {
  constructor(position, placeId) {
    this._position = position;
    this._placeId = placeId;
    this._placeName = undefined;
  }

  get position() {
    return this._position;
  }

  get placeName() {
    if (this._placeName !== undefined) {
      const thisPlaceName = this._placeName;
      return new Promise(function(resolve, reject) {
        return thisPlaceName;
      });
    } else {
      const thisLocation = this;
      this.definePlaceName()
          .then(newPlaceName => {
            thisLocation._placeName = newPlaceName;
            return newPlaceName;
          });
    }
  }

  definePlaceName() {
    const request = {
      placeId: placeId,
      fields: ['name'],
    };
    return new Promise(function(resolve, reject) {
      const service = new google.maps.places.PlacesService(map);
      service.getDetails(request, (place, status) => {
        if (status === google.maps.places.PlacesServiceStatus.OK) {
          resolve(place.name);
        } else {
          reject(new Error('Couldn\'t find the place ' + placeId + ' because' +
              ' ' + status));
        }
      });
    });
  }
}
