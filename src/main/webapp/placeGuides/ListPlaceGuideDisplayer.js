/**
 * This class is responsible for representing
 * the placeGuides on a scrollable list.
 */

class ListPlaceGuideDisplayer {

  static PAGE = {
    "DISCOVER": {
      listTitle: "Discover Guides",
      listSubTitle: "in selected map area"
    },
    "MY_GUIDES": {
      listTitle: "My Guides",
      listSubTitle: "in selected map area"
    },
    "BOOKMARKED_PLACEGUIDES": {
      listTitle: "Bookmarked Guides",
      listSubTitle: ""
    }
  }

  constructor(page) {
    this._listPlaceGuideDisplayerDiv = 
        document.getElementById("listPlaceGuideDisplayer");
    this._listPlaceGuideDisplayerDiv.classList.add(
        "list-group", 
        "my-place-guide-list", 
        "list-place-guide-displayer", 
        "form-card");
    this._page = page;
    this._listPlaceGuideDisplayerDiv.appendChild(
        this.createListTitle(
            ListPlaceGuideDisplayer.PAGE[page.name]));
  }

  get listPlaceGuideDisplayerDiv() {
    return this._listPlaceGuideDisplayerDiv;
  }

  update(placeGuides) {
    const placeGuideIdsToBeRemoved = [];
    this._listPlaceGuideDisplayerDiv.childNodes.forEach(function(placeGuide) {
      if (!placeGuides.hasOwnProperty(ListPlaceGuideDisplayer.extractIdFromDivId(placeGuide.id))) {
        placeGuideIdsToBeRemoved.push(placeGuide.id);
      } else {
        delete placeGuides[ListPlaceGuideDisplayer.extractIdFromDivId(placeGuide.id)];
      }
    });

    // for (var index = 0;index < placeGuideIdsToBeRemoved.length;index++) {
    //   this.remove(placeGuideIdsToBeRemoved[index]);
    // }
    // this.addPlaceGuidesToList(placeGuides);
    console.log(placeGuideIdsToBeRemoved);
    console.log(placeGuides);
  }

  static extractIdFromDivId(placeGuideDivId) {
    return Number(placeGuideDivId.slice(18, placeGuideDivId.length - 1));
  }

  removeAllPlaceGuidesFromList() {
    while (this._listPlaceGuideDisplayerDiv.firstChild) {
      this._listPlaceGuideDisplayerDiv.removeChild(
          this._listPlaceGuideDisplayerDiv.lastChild);
    };
  }

  addPlaceGuidesToList(placeGuides) {
    for (var placeGuideId in placeGuides) {
      if (placeGuides.hasOwnProperty(placeGuideId)) {
        var constructedPlaceGuideOnListDiv = 
            this.constructPlaceGuideOnListDivFromPlaceGuide(
                placeGuides[placeGuideId]);
        this._listPlaceGuideDisplayerDiv.appendChild(
            constructedPlaceGuideOnListDiv);
      }
    }
  }

  remove(placeGuideId) {
    const placeGuideOnListDivId = 
        ListPlaceGuideDisplayer.getPlaceGuideOnListDivId(placeGuideId);
    const placeGuideDiv = document.getElementById(placeGuideOnListDivId);
    if (this._listPlaceGuideDisplayerDiv.contains(placeGuideDiv)) {
      this._listPlaceGuideDisplayerDiv.removeChild(placeGuideDiv);
    }
    return placeGuideDiv;
  }

  static getPlaceGuideOnListDivId(placeGuideId) {
    return "placeGuideOnList-" + "{" + placeGuideId + "}";
  }

   // Move the highlighted place guide to top of list.
  highlight(placeGuideId) {
    const placeGuideDiv = 
        this.remove(placeGuideId);
    this.insertDivAfterTitle(placeGuideDiv);
    PlaceGuideOnList.highlight(placeGuideId);
  }

  insertDivAfterTitle(placeGuideDiv) {
    this._listPlaceGuideDisplayerDiv.insertBefore(
        placeGuideDiv, 
        this._listPlaceGuideDisplayerDiv.firstChild.nextSibling);
  }

  unhighlight(placeGuideId) {
    PlaceGuideOnList.unhighlight(placeGuideId);
  }

  constructPlaceGuideOnListDivFromPlaceGuide(placeGuide) {
    const mapsPlace = placeGuide.location.mapsPlace;
    var placeName;
    var placeId;
    if (mapsPlace == null) {
      placeName = null;
      placeId = null;
    } else {
      placeName = mapsPlace.name;
      placeId = mapsPlace.place_id;
    }
    return new PlaceGuideOnList(
        placeGuide.id,
        placeName,
        placeId,
        placeGuide.name,
        placeGuide.creator,
        placeGuide.description,
        placeGuide.audioKey,
        placeGuide.audioLength,
        placeGuide.isPublic,
        placeGuide.imgKey,
        placeGuide.createdByCurrentUser,
        placeGuide.bookmarkedByCurrentUser,
        placeGuide.location.position.lat(),
        placeGuide.location.position.lng()).placeGuideOnListDiv;
  }

  createListTitle(placeGuideDisplayHeading) {
    const listTitleDiv = document.createElement("div");
    listTitleDiv.setAttribute("id", "listTitle");
    listTitleDiv.classList.add(
        "list-group-item", 
        "flex-column",
        "align-items-start");
    listTitleDiv.style.backgroundColor = "#80ba83";
    listTitleDiv.style.color = "white";
    const listTitleElement = document.createElement("h4");
    listTitleElement.innerText = placeGuideDisplayHeading.listTitle;
    listTitleDiv.appendChild(listTitleElement);

    const listSubtitleElement = document.createElement("p");
    listSubtitleElement.innerText = placeGuideDisplayHeading.listSubTitle;
    listTitleDiv.appendChild(listSubtitleElement);
    return listTitleDiv;
  }

}